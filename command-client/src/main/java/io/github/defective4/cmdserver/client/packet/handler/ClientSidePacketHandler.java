package io.github.defective4.cmdserver.client.packet.handler;

import io.github.defective4.cmdserver.client.CmdClient;
import io.github.defective4.cmdserver.client.event.ClientListener;
import io.github.defective4.cmdserver.common.packet.handler.PacketHandler;
import io.github.defective4.cmdserver.common.packet.handler.PacketReceiver;
import io.github.defective4.cmdserver.common.packet.twoway.CommandPacket;
import io.github.defective4.cmdserver.common.packet.twoway.CommandResponsePacket;
import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;
import io.github.defective4.cmdserver.common.packet.twoway.PingPacket;

public class ClientSidePacketHandler extends PacketHandler {
    private final CmdClient client;

    public ClientSidePacketHandler(CmdClient client) {
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
