package io.github.defective4.cmdserver.client.event;

import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;

public abstract class ClientAdapter implements ClientListener {

    @Override
    public void authorized() {}

    @Override
    public void commandReceived(String command, String[] args) {}

    @Override
    public void disconnected(DisconnectPacket packet) {}

    @Override
    public void responseReceived(byte[] data) {}

    @Override
    public void serverPinged(long id) {}

    @Override
    public void serverPingReceived(long id) {}

}
