package io.github.defective4.trivialpacket.common.packet.twoway;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.github.defective4.trivialpacket.common.packet.Packet;
import io.github.defective4.trivialpacket.common.packet.PacketFactory;

/**
 * A disconnect packet. <br>
 * Sent by both parties when they need to notify the receiver they are going to
 * close the connection
 */
@SuppressWarnings("javadoc")
public class DisconnectPacket extends Packet {

    public static final PacketFactory<DisconnectPacket> FACTORY = new PacketFactory<>(DisconnectPacket.class) {

        @Override
        protected DisconnectPacket createPacket(byte[] data) throws Exception {
            return new DisconnectPacket(new String(data, StandardCharsets.UTF_8));
        }
    };

    private final String reason;

    public DisconnectPacket(String reason) {
        this.reason = reason;
    }

    /**
     * @return disconnect reason
     */
    public String getReason() {
        return reason;
    }

    @Override
    protected void writePacketData(DataOutputStream str) throws IOException {
        str.write(reason.getBytes(StandardCharsets.UTF_8));
    }

}
