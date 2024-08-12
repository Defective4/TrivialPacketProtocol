package io.github.defective4.cmdserver.server.event;

import io.github.defective4.cmdserver.server.ClientConnection;

public abstract class ServerAdapter implements ServerListener {

    @Override
    public void clientAuthorized(ClientConnection connection) throws Exception {}

    @Override
    public void clientConnected(ClientConnection connection) throws Exception {}

    @Override
    public void clientPinged(long id) throws Exception {}

    @Override
    public void commandReceived(ClientConnection connection, String command, String[] args) throws Exception {}

    @Override
    public void responseReceived(ClientConnection connection, byte[] data) throws Exception {}

}
