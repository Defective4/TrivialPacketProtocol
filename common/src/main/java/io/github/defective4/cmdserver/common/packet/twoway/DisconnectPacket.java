package io.github.defective4.cmdserver.common.packet.twoway;

import java.nio.charset.StandardCharsets;

import io.github.defective4.cmdserver.common.packet.Packet;

/**
 * A disconnect packet. <br>
 * Sent by both parties when they need to notify the receiver they are going to
 * close the connection
 */
public class DisconnectPacket extends Packet {

    private final String reason;

    @SuppressWarnings("javadoc")
    public DisconnectPacket(byte[] data) {
        super(data);
        reason = new String(data, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("javadoc")
    public DisconnectPacket(String reason) {
        this(reason.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @return disconnect reason
     */
    public String getReason() {
        return reason;
    }

}
