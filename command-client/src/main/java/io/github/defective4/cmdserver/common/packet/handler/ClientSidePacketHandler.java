package io.github.defective4.cmdserver.common.packet.handler;

import java.io.IOException;

import io.github.defective4.cmdserver.common.CmdClient;
import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;
import io.github.defective4.cmdserver.common.packet.twoway.PingPacket;

public class ClientSidePacketHandler extends PacketHandler {
    private final CmdClient client;

    public ClientSidePacketHandler(CmdClient client) {
        this.client = client;
    }

    @PacketReceiver
    public void onDisconnect(DisconnectPacket packet) throws IOException {
        client.getListeners().forEach(ls -> ls.disconnected(packet));
        client.close();
    }

    @PacketReceiver
    public void onPing(PingPacket e) throws IOException {
        long id = e.getId();
        if (id != client.getLastPingID()) client.disconnect("Otrzymano nieprawidłowy keep-alive");
        client.setLastPingID(-1);
        client.getListeners().forEach(ls -> ls.serverPingReceived(id));
    }
}
