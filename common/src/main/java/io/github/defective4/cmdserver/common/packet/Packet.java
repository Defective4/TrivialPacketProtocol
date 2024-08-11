package io.github.defective4.cmdserver.common.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Packet {
    private final byte[] data;
    private final int id;

    protected Packet(byte[] data) {
        id = PacketRegistry.getIDForClass(getClass());
        this.data = data;
    }

    public void writeToStream(DataOutputStream str) throws IOException {
        str.writeInt(data.length + 1);
        str.writeByte(id);
        str.write(data);
    }

    public static Packet readFromStream(DataInputStream isr) throws IOException {
        byte[] data = new byte[isr.readInt()];
        isr.readFully(data);
        Class<? extends Packet> packetClass = PacketRegistry.getPacketForID(data[0]);
        if (packetClass == null) throw new IOException("Unknown packet: 0x" + Integer.toHexString(data[0] & 0xFF));
        try {
            Object copy = Arrays.copyOfRange(data, 1, data.length);
            return packetClass.getConstructor(byte[].class).newInstance(copy);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
