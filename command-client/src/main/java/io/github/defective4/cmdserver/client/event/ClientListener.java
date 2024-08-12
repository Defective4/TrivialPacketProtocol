package io.github.defective4.cmdserver.client.event;

import io.github.defective4.cmdserver.client.CmdClient;
import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;

/**
 * Listener for client events. You can use it together with
 * {@link CmdClient#addListener(ClientListener)}.
 */
public interface ClientListener {
    /**
     * Called when the client got authorized on the server.<br>
     * It's safe to send data to the server at this point.
     *
     * @throws Exception
     */
    void authorized() throws Exception;

    /**
     * Called when a command was received from the server.
     *
     * @param  command   command name
     * @param  args      command arguments. Can't be <code>null</code>
     * @throws Exception
     */
    void commandReceived(String command, String[] args) throws Exception;

    /**
     * Called when the server sends a {@link DisconnectPacket}. <br>
     * This method is NOT called if the server closes the connection without sending
     * the packet!
     *
     * @param  packet    received disconnect packet containing the reason
     * @throws Exception
     */
    void disconnected(DisconnectPacket packet) throws Exception;

    /**
     * Called when the server sends a response to client's command.<br>
     *
     * @param  data      received data
     * @throws Exception
     */
    void responseReceived(byte[] data) throws Exception;

    /**
     * Called when client sends ping packet to the server.
     *
     * @param  id        ID sent in the packet
     * @throws Exception
     */
    void serverPinged(long id) throws Exception;

    /**
     * Called when the server responds to sent ping packet
     *
     * @param  id        received ID
     * @throws Exception
     */
    void serverPingReceived(long id) throws Exception;
}
