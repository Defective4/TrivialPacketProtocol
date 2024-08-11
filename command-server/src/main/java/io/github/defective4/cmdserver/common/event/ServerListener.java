package io.github.defective4.cmdserver.common.event;

import io.github.defective4.cmdserver.common.ClientConnection;

public interface ServerListener {
    void clientConnected(ClientConnection connection);

    void clientPinged(long id);

    void clientAuthorized(ClientConnection connection);
    
    void commandReceived(String command, String[] args);
}
