package io.github.defective4.cmdserver.server.event;

import io.github.defective4.cmdserver.server.ClientConnection;

public interface ServerListener {
    void clientAuthorized(ClientConnection connection);

    void clientConnected(ClientConnection connection);

    void clientPinged(long id);

    void commandReceived(ClientConnection connection, String command, String[] args);

    void responseReceived(ClientConnection connection, byte[] data);
}
