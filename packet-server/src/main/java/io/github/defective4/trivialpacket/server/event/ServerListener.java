package io.github.defective4.trivialpacket.server.event;

import io.github.defective4.trivialpacket.common.packet.Packet;
import io.github.defective4.trivialpacket.server.ClientConnection;

/**
 * A listener interface for server events. <br>
 * Use this to interact with connected clients.
 *
 * @see ServerAdapter
 */
public interface ServerListener {
    /**
     * Called when a client got authorized.
     *
     * @param  connection client connection
     * @throws Exception
     */
    void clientAuthorized(ClientConnection connection) throws Exception;

    /**
     * Called when a client connects. <br>
     * Do NOT send any data here, the client is not authorized yet.
     *
     * @param  connection client connection
     * @throws Exception
     */
    void clientConnected(ClientConnection connection) throws Exception;

    /**
     * Called when a client disconnects. <br>
     *
     * @param  connection client connection. Can be <code>null</code>
     * @throws Exception
     */
    void clientDisconnected(ClientConnection connection) throws Exception;

    /**
     * Called when a client sends a ping packet to the server
     *
     * @param  id
     * @throws Exception
     */
    void clientPinged(long id) throws Exception;

    /**
     * Called when a command is received from a client
     *
     * @param  connection client connection
     * @param  command    command name
     * @param  args       command arguments
     * @throws Exception
     */
    void commandReceived(ClientConnection connection, String command, String[] args) throws Exception;

    /**
     * Called when a custom packet is received
     * 
     * @param  connection connection
     * @param  packet     received packet
     * @throws Exception
     */
    void customPacketReceived(ClientConnection connection, Packet packet) throws Exception;

    /**
     * Called when a client responds to server's command
     *
     * @param  connection client connection
     * @param  data       received data
     * @throws Exception
     */
    void responseReceived(ClientConnection connection, byte[] data) throws Exception;
}
