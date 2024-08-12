package io.github.defective4.trivialpacket.common.packet.twoway;

import java.nio.ByteBuffer;

import io.github.defective4.trivialpacket.common.packet.Packet;

@SuppressWarnings("javadoc")
public class PingPacket extends Packet {

    private final long id;

    public PingPacket(byte[] data) {
        super(data);
        id = ByteBuffer.wrap(data).getLong();
    }

    public PingPacket(long id) {
        this(mkBytes(id));
    }

    public long getId() {
        return id;
    }

    private static byte[] mkBytes(long id) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(id);
        return buffer.array();
    }

}
