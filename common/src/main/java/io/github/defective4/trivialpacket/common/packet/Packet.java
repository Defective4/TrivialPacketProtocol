package io.github.defective4.trivialpacket.common.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The base packet class. <br>
 * A packet is used to transport various data between client and server. <br>
 *
 * The default packet structure is as follows: <br>
 *
 * <pre>
 * - Packet Length - 32 bit integer
 * - Packet ID - one byte
 * - Packet Data - byte array of any length
 * </pre>
 */
public abstract class Packet {

    private final int id;

    protected Packet() {
        try {
            id = PacketRegistry.getIDForPacketClass(getClass());
        } catch (Exception e) {
            throw new IllegalStateException("Packet " + getClass().getName() + " not registered");
        }
    }

    public int getId() {
        return id;
    }

    public void writeToStream(DataOutputStream str) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                DataOutputStream wrapper = new DataOutputStream(buffer)) {
            writePacket(wrapper);
            byte[] rawData = buffer.toByteArray();
            str.writeInt(rawData.length + 1);
            str.writeByte(id);
            str.write(rawData);
        }
    }

    protected abstract void writePacket(DataOutputStream str) throws IOException;

    public static Packet readFromStream(DataInputStream isr) throws Exception {
        int length = isr.readInt();
        byte id = isr.readByte();
        byte[] data = new byte[length - 1];
        isr.readFully(data);
        PacketFactory<?> factory = PacketRegistry.getFactoryForID(id);
        return factory == null ? null : factory.createPacket(data);
    }
}
