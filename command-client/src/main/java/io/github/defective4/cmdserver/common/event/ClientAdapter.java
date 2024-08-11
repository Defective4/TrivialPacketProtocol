package io.github.defective4.cmdserver.common.event;

import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;

public class ClientAdapter implements ClientListener {

    @Override
    public void authorized() {}

    @Override
    public void disconnected(DisconnectPacket packet) {}

    @Override
    public void serverPinged(long id) {}

    @Override
    public void serverPingReceived(long id) {}

}
