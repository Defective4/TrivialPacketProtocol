package io.github.defective4.trivialpacket.common.packet.twoway;

import io.github.defective4.trivialpacket.common.packet.Packet;

/**
 * Command response packet
 */
@SuppressWarnings("javadoc")
public class CommandResponsePacket extends Packet {

    private final byte[] data;

    public CommandResponsePacket(byte[] data) {
        super(data);
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

}
