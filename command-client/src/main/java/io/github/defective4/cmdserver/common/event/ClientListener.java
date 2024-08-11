package io.github.defective4.cmdserver.common.event;

import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;

public interface ClientListener {
    void authorized();

    void disconnected(DisconnectPacket packet);

    void serverPinged(long id);

    void serverPingReceived(long id);
    
    void commandReceived(String command, String[] args);
}
