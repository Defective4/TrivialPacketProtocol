package io.github.defective4.cmdserver.server.event;

import io.github.defective4.cmdserver.server.ClientConnection;

public interface ServerListener {
    void clientAuthorized(ClientConnection connection) throws Exception;

    void clientConnected(ClientConnection connection) throws Exception;

    void clientPinged(long id) throws Exception;

    void commandReceived(ClientConnection connection, String command, String[] args) throws Exception;

    void responseReceived(ClientConnection connection, byte[] data) throws Exception;
}
