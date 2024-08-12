package io.github.defective4.trivialpacket.common.packet.twoway;

import java.io.DataOutputStream;
import java.io.IOException;

import io.github.defective4.trivialpacket.common.packet.Packet;
import io.github.defective4.trivialpacket.common.packet.PacketFactory;

/**
 * Command response packet
 */
@SuppressWarnings("javadoc")
public class CommandResponsePacket extends Packet {

    public static final PacketFactory<CommandResponsePacket> FACTORY = new PacketFactory<>(
            CommandResponsePacket.class) {

        @Override
        protected CommandResponsePacket createPacket(byte[] data) throws Exception {
            return new CommandResponsePacket(data);
        }
    };

    private final byte[] data;

    public CommandResponsePacket(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    protected void writePacketData(DataOutputStream str) throws IOException {
        str.write(data);
    }

}
