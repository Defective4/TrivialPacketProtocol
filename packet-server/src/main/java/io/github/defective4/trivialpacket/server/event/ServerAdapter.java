package io.github.defective4.trivialpacket.server.event;

import io.github.defective4.trivialpacket.server.ClientConnection;

/**
 * Adapter class for {@link ServerListener}
 */
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
