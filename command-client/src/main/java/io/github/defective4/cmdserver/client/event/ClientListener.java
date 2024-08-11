package io.github.defective4.cmdserver.client.event;

import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;

public interface ClientListener {
    void authorized();

    void commandReceived(String command, String[] args);

    void disconnected(DisconnectPacket packet);

    void responseReceived(byte[] data);

    void serverPinged(long id);

    void serverPingReceived(long id);
}
