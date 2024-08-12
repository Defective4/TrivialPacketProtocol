package io.github.defective4.trivialpacket.server.packet.handler;

import java.io.IOException;
import java.util.Objects;

import io.github.defective4.trivialpacket.common.packet.handler.PacketHandler;
import io.github.defective4.trivialpacket.common.packet.handler.PacketReceiver;
import io.github.defective4.trivialpacket.common.packet.twoway.CommandPacket;
import io.github.defective4.trivialpacket.common.packet.twoway.CommandResponsePacket;
import io.github.defective4.trivialpacket.common.packet.twoway.DisconnectPacket;
import io.github.defective4.trivialpacket.common.packet.twoway.PingPacket;
import io.github.defective4.trivialpacket.server.ClientConnection;
import io.github.defective4.trivialpacket.server.CmdServer;
import io.github.defective4.trivialpacket.server.event.ServerListener;

/**
 * A default packet handler added to new {@link CmdServer}s
 */
@SuppressWarnings("javadoc")
public class ServerSidePacketHandler extends PacketHandler {
    private final ClientConnection connection;
    private final CmdServer server;

    /**
     * @param  connection
     * @param  server
     * @throws NullPointerException if connection or server is null
     */
    public ServerSidePacketHandler(ClientConnection connection, CmdServer server) {
        Objects.requireNonNull(connection);
        Objects.requireNonNull(server);
        this.server = server;
        this.connection = connection;
    }

    @PacketReceiver
    public void onCommand(CommandPacket e) throws Exception {
        for (ServerListener ls : server.getListeners())
            ls.commandReceived(connection, e.getCommand(), e.getArguments());
    }

    @PacketReceiver
    public void onDisconnect(DisconnectPacket e) throws IOException {
        throw new IOException("Client has disconnected: " + e.getReason());
    }

    @PacketReceiver
    public void onPing(PingPacket e) throws Exception {
        for (ServerListener ls : server.getListeners()) ls.clientPinged(e.getId());
        connection.sendPacket(new PingPacket(e.getId()));
    }

    @PacketReceiver
    public void onResponse(CommandResponsePacket e) throws Exception {
        for (ServerListener ls : server.getListeners()) ls.responseReceived(connection, e.getData());
    }
}
