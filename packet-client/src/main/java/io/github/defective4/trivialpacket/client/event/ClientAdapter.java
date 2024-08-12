package io.github.defective4.trivialpacket.client.event;

import io.github.defective4.trivialpacket.common.packet.Packet;
import io.github.defective4.trivialpacket.common.packet.twoway.DisconnectPacket;

/**
 * This is an adapter class for {@link ClientListener}
 */
public abstract class ClientAdapter implements ClientListener {

    @Override
    public void authorized() throws Exception {}

    @Override
    public void commandReceived(String command, String[] args) throws Exception {}

    @Override
    public void customPacketReceived(Packet packet) throws Exception {}

    @Override
    public void disconnected(DisconnectPacket packet) throws Exception {}

    @Override
    public void responseReceived(byte[] data) throws Exception {}

    @Override
    public void serverPinged(long id) throws Exception {}

    @Override
    public void serverPingReceived(long id) throws Exception {}

}
