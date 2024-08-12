package io.github.defective4.cmdserver.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.defective4.cmdserver.client.event.ClientListener;
import io.github.defective4.cmdserver.client.packet.handler.ClientSidePacketHandler;
import io.github.defective4.cmdserver.common.packet.Packet;
import io.github.defective4.cmdserver.common.packet.client.AuthPacket;
import io.github.defective4.cmdserver.common.packet.server.AuthSuccessPacket;
import io.github.defective4.cmdserver.common.packet.twoway.CommandPacket;
import io.github.defective4.cmdserver.common.packet.twoway.CommandResponsePacket;
import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;
import io.github.defective4.cmdserver.common.packet.twoway.PingPacket;
import io.github.defective4.cmdserver.common.ssl.SSLManager;

/**
 * The main client class.<br>
 * It's used to connect and exchange data with a CmdServer instance.<br>
 *
 * Example usage:
 *
 * <pre>
 * char[] token = "TOKEN".toCharArray();
 * try (CmdClient client = new CmdClient("localhost", 7561, token)) {
 *     client.addListener(new ClientAdapter() {
 *
 *         {@code @Override}
 *         public void authorized() {
 *             // Do something after authorized
 *         }
 *
 *     });
 *     client.connect();
 * } catch (Exception e) {
 *     e.printStackTrace();
 * }
 * </pre>
 */
public class CmdClient implements AutoCloseable {

    private final Certificate cert;
    private boolean connected = false;
    private final ClientSidePacketHandler handler = new ClientSidePacketHandler(this);

    private final String host;
    private DataInputStream is;
    private long lastPingID = -1;
    private final List<ClientListener> listeners = new CopyOnWriteArrayList<>();

    private DataOutputStream os;
    private final Timer pingTimer = new Timer(true);
    private final int port;

    private final Socket socket;
    private final char[] token;

    /**
     * Constructs a client with SSL disabled.<br>
     * The target server has to have SSL disabled too.
     *
     * @param  host                 host to connect to
     * @param  port                 target port
     * @param  token                Authorization token. It has to be the same token
     *                              as used on the target server. Can be
     *                              <code>null</code>
     * @throws NullPointerException if host is null
     */
    public CmdClient(String host, int port, char[] token) {
        Objects.requireNonNull(host);
        cert = null;
        this.host = host;
        this.port = port;
        this.token = token == null ? new char[0] : token;
        socket = new Socket();
    }

    /**
     * Constructs a client with SSL capabilities. <br>
     * The target server must have SSL enabled. <br>
     * Server's certificate must belong to the same chain as the one provided here,
     * otherwise the client will refuse to connect.
     *
     * @param  host                 host to connect to
     * @param  port                 target port
     * @param  token                authorization token. It has to be the same token
     *                              as used on the target server. Can be
     *                              <code>null</code>
     * @param  cert                 validation certificate.
     * @throws IOException          when there was an error initializing SSLSocket
     *                              from the provided certificate.
     * @throws NullPointerException if host or cert is null
     */
    public CmdClient(String host, int port, char[] token, Certificate cert) throws IOException {
        Objects.requireNonNull(host);
        Objects.requireNonNull(cert);
        this.token = token == null ? new char[0] : token;
        this.cert = cert;
        this.host = host;
        this.port = port;
        try {
            socket = SSLManager.mkSSLContext(cert, null).getSocketFactory().createSocket();
        } catch (
                UnrecoverableKeyException |
                KeyManagementException |
                KeyStoreException |
                NoSuchAlgorithmException |
                CertificateException e) {
            throw new IOException(e);
        }

    }

    /**
     * Add a listener to this client. <br>
     * Added listeners can't be removed afterwards.
     *
     * @param  listener
     * @throws NullPointerException is listener is null
     */
    public void addListener(ClientListener listener) {
        Objects.requireNonNull(listener);
        listeners.add(listener);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    /**
     * Connects this client. <br>
     * The connected client will perform the authorization sequence, and if
     * successful, the method will <strong>block</strong> and continuously receive
     * commands from the server. <br>
     * An exception will be thrown if an error occurs anytime during connection,
     * including authorization process and command loop.<br>
     * <strong>WARNING:</strong> you shouldn't send any data
     * ({@link #sendCommand(String, String...)} / {@link #respond(byte[])}) before
     * {@link ClientListener#authorized()} is called!
     *
     * @throws Exception             if any error occurs while connection is alive.
     * @throws IllegalStateException if the client is already connected
     */
    public void connect() throws Exception {
        if (connected) throw new IllegalStateException("Already connected");
        socket.connect(new InetSocketAddress(host, port));
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        connected = true;
        sendPacket(new AuthPacket(token));
        Packet authResponse = Packet.readFromStream(is);
        if (authResponse instanceof DisconnectPacket disconnectPacket) {
            throw new IOException("Server rejected the connection: " + disconnectPacket.getReason());
        }
        if (!(authResponse instanceof AuthSuccessPacket)) {
            throw new IOException("Received invalid packet during authentication: " + authResponse);
        }
        for (ClientListener ls : listeners) ls.authorized();
        pingTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    if (lastPingID != -1) {
                        disconnect("Timed out");
                        cancel();
                    }
                    lastPingID = System.currentTimeMillis();
                    sendPacket(new PingPacket(lastPingID));
                    for (ClientListener ls : listeners) ls.serverPinged(lastPingID);
                } catch (Exception e) {
                    cancel();
                    try {
                        close();
                    } catch (IOException e1) {}
                }
            }
        }, 0, 15000);
        while (!socket.isClosed()) {
            try {
                handler.handle(Packet.readFromStream(is));
            } catch (InvocationTargetException e) {
                throw e.getCause() == null ? e : new Exception(e.getCause());
            }
        }
    }

    /**
     * Sends a {@link DisconnectPacket} to the server and closes the underlying
     * connection.
     *
     * @param  reason               plain text reason
     * @throws IOException          if there was an error closing the client.
     * @throws NullPointerException if reason is null
     */
    public void disconnect(String reason) throws IOException {
        Objects.requireNonNull(reason);
        try {
            sendPacket(new DisconnectPacket(reason));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    /**
     * Get last pending keep-alive ID, used internally. <br>
     *
     * @return pending keep-alive ID, or <code>-1</code> if it was already
     *         acknowledged by the server.
     */
    public long getLastPingID() {
        return lastPingID;
    }

    /**
     * Get this client's listeners.
     *
     * @return unmodifiable list of client listeners
     */
    public List<ClientListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Check if the underlying socket is closed.<br>
     * This delegates to {@link Socket#isClosed()}
     *
     * @return <code>true</code> if the socket is closed, <code>false</code>
     *         otherwise
     */
    public boolean isClosed() {
        return socket.isClosed();
    }

    /**
     * Check if this client is connected to the server. <br>
     * Note this method will return <code>true</code> even if the client was closed.
     *
     * @return <code>true</code> if {@link #connect()} was called
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Respond to a command send by the server. <br>
     * By definition you should only use this to respond to the server's command,
     * but in reality you can send this data at any point during connection and the
     * default CmdServer implementation will handle it just fine.
     *
     * @param  data        data to send to the server
     * @throws IOException if there was an error sending data packet to the server
     */
    public void respond(byte[] data) throws IOException {
        sendPacket(new CommandResponsePacket(data));
    }

    /**
     * Send a command request to the server.
     *
     * @param  command              command name
     * @param  arguments            command arguments
     * @throws IOException          if there was an error sending data packet to the
     *                              server
     * @throws NullPointerException if command or any of the arguments is null
     */
    public void sendCommand(String command, String... arguments) throws IOException {
        Objects.requireNonNull(command);
        Objects.requireNonNull(arguments);
        for (String arg : arguments) Objects.requireNonNull(arg);
        sendPacket(new CommandPacket(command, arguments));
    }

    /**
     * Send a raw packet to the server.
     *
     * @param  packet               packet to send
     * @throws IOException          if there was an error sending data packet to the
     *                              server
     * @throws NullPointerException if packet is null
     */
    public void sendPacket(Packet packet) throws IOException {
        Objects.requireNonNull(packet);
        if (!connected) throw new IllegalStateException("Not connected");
        packet.writeToStream(os);
    }

    /**
     * Used internally.
     *
     * @param lastPingID
     */
    public void setLastPingID(long lastPingID) {
        this.lastPingID = lastPingID;
    }

}
