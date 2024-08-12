package io.github.defective4.cmdserver.client.event;

import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;

public interface ClientListener {
    void authorized() throws Exception;

    void commandReceived(String command, String[] args) throws Exception;

    void disconnected(DisconnectPacket packet) throws Exception;

    void responseReceived(byte[] data) throws Exception;

    void serverPinged(long id) throws Exception;

    void serverPingReceived(long id) throws Exception;
}
