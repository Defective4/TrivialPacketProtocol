package io.github.defective4.cmdserver.common.event;

import io.github.defective4.cmdserver.common.ClientConnection;

public class ServerAdapter implements ServerListener {

    @Override
    public void clientConnected(ClientConnection connection) {}

    @Override
    public void clientPinged(long id) {}

}
