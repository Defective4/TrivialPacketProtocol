package io.github.defective4.trivialpacket.client.packet.handler;

import java.util.Objects;

import io.github.defective4.trivialpacket.client.CmdClient;
import io.github.defective4.trivialpacket.client.event.ClientListener;
import io.github.defective4.trivialpacket.common.packet.handler.PacketHandler;
import io.github.defective4.trivialpacket.common.packet.handler.PacketReceiver;
import io.github.defective4.trivialpacket.common.packet.twoway.CommandPacket;
import io.github.defective4.trivialpacket.common.packet.twoway.CommandResponsePacket;
import io.github.defective4.trivialpacket.common.packet.twoway.DisconnectPacket;
import io.github.defective4.trivialpacket.common.packet.twoway.PingPacket;

/**
 * This is the main packet handler added by default to new clients. <br>
 * Methods here should never be called manually.
 */
@SuppressWarnings("javadoc")
public class ClientSidePacketHandler extends PacketHandler {
    private final CmdClient client;

    /**
     * Constructs a new packet handler
     *
     * @param  client
     * @throws NullPointerException if client is null
     */
    public ClientSidePacketHandler(CmdClient client) {
        Objects.requireNonNull(client);
        this.client = client;
    }

    @PacketReceiver
    public void onCommand(CommandPacket packet) throws Exception {
        for (ClientListener ls : client.getListeners()) ls.commandReceived(packet.getCommand(), packet.getArguments());
    }

    @PacketReceiver
    public void onDisconnect(DisconnectPacket packet) throws Exception {
        for (ClientListener ls : client.getListeners()) ls.disconnected(packet);
        client.close();
    }

    @PacketReceiver
    public void onPing(PingPacket e) throws Exception {
        long id = e.getId();
        if (id != client.getLastPingID()) client.disconnect("Received invalid keep-alive packet");
        client.setLastPingID(-1);
        for (ClientListener ls : client.getListeners()) ls.serverPingReceived(id);
    }

    @PacketReceiver
    public void onResponse(CommandResponsePacket e) throws Exception {
        for (ClientListener ls : client.getListeners()) ls.responseReceived(e.getData());
    }
}
