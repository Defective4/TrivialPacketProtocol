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

public class CmdClient implements AutoCloseable {

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
    private final Certificate cert;

    public CmdClient(String host, int port, char[] token) throws IOException {
        this(host, port, token, null);
    }

    public CmdClient(String host, int port, char[] token, Certificate cert) throws IOException {
        this.token = token;
        this.cert = cert;
        this.host = host;
        this.port = port;
        try {
            socket = cert == null ? new Socket() : SSLManager.mkSSLContext(cert, null).getSocketFactory().createSocket();
        } catch (
                UnrecoverableKeyException |
                KeyManagementException |
                KeyStoreException |
                NoSuchAlgorithmException |
                CertificateException e) {
            throw new IOException(e);
        }

    }

    public void addListener(ClientListener listener) {
        listeners.add(listener);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public void sendCommand(String command, String... arguments) throws IOException {
        sendPacket(new CommandPacket(command, arguments));
    }

    public void respond(byte[] data) throws IOException {
        sendPacket(new CommandResponsePacket(data));
    }

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
        listeners.forEach(ClientListener::authorized);
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
                    listeners.forEach(ls -> ls.serverPinged(lastPingID));
                } catch (IOException e) {
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

    public void disconnect(String reason) throws IOException {
        sendPacket(new DisconnectPacket(reason));
        close();
    }

    public long getLastPingID() {
        return lastPingID;
    }

    public List<ClientListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendPacket(Packet packet) throws IOException {
        if (!connected) throw new IllegalStateException("Not connected");
        packet.writeToStream(os);
    }

    public void setLastPingID(long lastPingID) {
        this.lastPingID = lastPingID;
    }

}
