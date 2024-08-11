package io.github.defective4.cmdserver.common.packet.twoway;

import java.nio.charset.StandardCharsets;

import io.github.defective4.cmdserver.common.packet.Packet;

public class DisconnectPacket extends Packet {

    private final String reason;

    public DisconnectPacket(byte[] data) {
        super(data);
        reason = new String(data, StandardCharsets.UTF_8);
    }

    public DisconnectPacket(String reason) {
        this(reason.getBytes(StandardCharsets.UTF_8));
    }

    public String getReason() {
        return reason;
    }

}
