package io.github.defective4.cmdserver.common.event;

import io.github.defective4.cmdserver.common.ClientConnection;

public abstract class ServerAdapter implements ServerListener {

    @Override
    public void clientConnected(ClientConnection connection) {}

    @Override
    public void clientPinged(long id) {}

    @Override
    public void clientAuthorized(ClientConnection connection) {}

    @Override
    public void commandReceived(ClientConnection connection, String command, String[] args) {}

}
