package io.github.defective4.cmdserver.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import io.github.defective4.cmdserver.common.packet.Packet;
import io.github.defective4.cmdserver.common.packet.client.AuthPacket;
import io.github.defective4.cmdserver.common.packet.handler.ServerSidePacketHandler;
import io.github.defective4.cmdserver.common.packet.server.AuthSuccessPacket;
import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;

public class ClientConnection implements AutoCloseable {
    private final ServerSidePacketHandler handler;
    private final DataInputStream is;
    private final DataOutputStream os;
    private final CmdServer server;
    private final Socket socket;

    public ClientConnection(Socket socket, CmdServer server) throws IOException {
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

    public void handle() throws Exception {
        if (!(Packet.readFromStream(is) instanceof AuthPacket authPacket))
            throw new IOException("Invalid auth packet received");
        if (!new String(server.getToken()).equals(new String(authPacket.getToken()))) {
            sendPacket(new DisconnectPacket("Received invalid token"));
            throw new IOException("Received invalid token");
        }
        sendPacket(new AuthSuccessPacket());
        while (!socket.isClosed()) {
            try {
                handler.handle(Packet.readFromStream(is));
            } catch (InvocationTargetException e) {
                throw e.getCause() == null ? e : new Exception(e.getCause());
            }
        }
    }

    public void sendPacket(Packet packet) throws IOException {
        packet.writeToStream(os);
    }

}
