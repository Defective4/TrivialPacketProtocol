package io.github.defective4.cmdserver.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

import io.github.defective4.cmdserver.common.packet.Packet;
import io.github.defective4.cmdserver.common.packet.client.AuthPacket;
import io.github.defective4.cmdserver.common.packet.server.AuthSuccessPacket;
import io.github.defective4.cmdserver.common.packet.twoway.CommandPacket;
import io.github.defective4.cmdserver.common.packet.twoway.CommandResponsePacket;
import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;
import io.github.defective4.cmdserver.server.event.ServerListener;
import io.github.defective4.cmdserver.server.packet.handler.ServerSidePacketHandler;

/**
 * Class representing a single client connection. <br>
 * You can interact with the connected client through this class's methods
 */
public class ClientConnection implements AutoCloseable {
    private final ServerSidePacketHandler handler;
    private final DataInputStream is;
    private final DataOutputStream os;
    private final CmdServer server;
    private final Socket socket;

    /**
     * Default constructor
     *
     * @param  socket
     * @param  server
     * @throws IOException
     * @throws NullPointerException if any of the arguments is null
     */
    protected ClientConnection(Socket socket, CmdServer server) throws IOException {
        Objects.requireNonNull(socket);
        Objects.requireNonNull(server);
        this.socket = socket;
        os = new DataOutputStream(socket.getOutputStream());
        is = new DataInputStream(socket.getInputStream());
        this.server = server;
        handler = new ServerSidePacketHandler(this, server);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    /**
     * See {@link Socket#getInetAddress()}
     *
     * @return underlying socket's inet address
     */
    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    /**
     * See {@link Socket#getPort()}
     *
     * @return underlying socket's port
     */
    public int getPort() {
        return socket.getPort();
    }

    /**
     * Respond to a command send by the client. <br>
     * By definition you should only use this to respond to the client's command,
     * but in reality you can send this data at any point during connection and the
     * default CmdClient implementation will handle it just fine.
     *
     * @param  data        data to send
     * @throws IOException when there was an error sending the packet
     */
    public void respond(byte[] data) throws IOException {
        sendPacket(new CommandResponsePacket(data));
    }

    /**
     * Send a command request to the client
     *
     * @param  command              command name
     * @param  args                 command arguments
     * @throws IOException          when there was an error sending the packet
     * @throws NullPointerException if command or any of the arguments is null
     */
    public void sendCommand(String command, String... args) throws IOException {
        Objects.requireNonNull(command);
        Objects.requireNonNull(args);
        for (String arg : args) Objects.requireNonNull(arg);
        sendPacket(new CommandPacket(command, args));
    }

    /**
     * Send a raw packet to the client.
     *
     * @param  packet               packet to send
     * @throws IOException          when there was an error sending the packet
     * @throws NullPointerException if the packet is null
     */
    public void sendPacket(Packet packet) throws IOException {
        Objects.requireNonNull(packet);
        packet.writeToStream(os);
    }

    /**
     * Starts the command loop.<br>
     * You shouldn't send any data before
     * {@link ServerListener#clientAuthorized(ClientConnection)} is called
     *
     * @throws Exception
     */
    protected void handle() throws Exception {
        if (!(Packet.readFromStream(is) instanceof AuthPacket authPacket))
            throw new IOException("Invalid auth packet received");
        if (!new String(server.getToken()).equals(new String(authPacket.getToken()))) {
            sendPacket(new DisconnectPacket("Received invalid token"));
            throw new IOException("Received invalid token");
        }
        sendPacket(new AuthSuccessPacket());
        for (ServerListener ls : server.getListeners()) ls.clientAuthorized(this);
        while (!socket.isClosed()) {
            try {
                handler.handle(Packet.readFromStream(is));
            } catch (InvocationTargetException e) {
                throw e.getCause() == null ? e : new Exception(e.getCause());
            }
        }
    }

}
