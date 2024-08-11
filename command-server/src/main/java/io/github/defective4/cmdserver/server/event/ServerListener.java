package io.github.defective4.cmdserver.server.event;

import io.github.defective4.cmdserver.server.ClientConnection;

public interface ServerListener {
    void clientConnected(ClientConnection connection);

    void clientPinged(long id);

    void clientAuthorized(ClientConnection connection);

    void commandReceived(ClientConnection connection, String command, String[] args);
}
