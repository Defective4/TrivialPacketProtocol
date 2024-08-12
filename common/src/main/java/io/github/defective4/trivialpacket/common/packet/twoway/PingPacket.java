package io.github.defective4.trivialpacket.common.packet.twoway;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.github.defective4.trivialpacket.common.packet.Packet;
import io.github.defective4.trivialpacket.common.packet.PacketFactory;

@SuppressWarnings("javadoc")
public class PingPacket extends Packet {

    public static final PacketFactory<PingPacket> FACTORY = new PacketFactory<>(PingPacket.class) {

        @Override
        protected PingPacket createPacket(byte[] data) throws Exception {
            return new PingPacket(ByteBuffer.wrap(data).getLong());
        }
    };

    private final long id;

    public PingPacket(long id) {
        this.id = id;
    }

    @Override
    protected void writePacket(DataOutputStream str) throws IOException {
        str.writeLong(id);
    }

}
