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

    /**
     * Constructs a new packet. <br>
     * The packet has to be registered in {@link PacketFactoryRegistry} before
     * construction.
     */
    protected Packet() {
        try {
            id = PacketFactoryRegistry.getIDForPacketClass(getClass());
        } catch (Exception e) {
            throw new IllegalStateException("Packet " + getClass().getName() + " not registered");
        }
    }

    /**
     * Get this packet's ID
     *
     * @return packet ID
     */
    public int getId() {
        return id;
    }

    /**
     * Write this packet to the output stream
     *
     * @param  str         output stream
     * @throws IOException thrown if there was an error writing to the output stream
     */
    public void writeToStream(DataOutputStream str) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                DataOutputStream wrapper = new DataOutputStream(buffer)) {
            writePacketData(wrapper);
            byte[] rawData = buffer.toByteArray();
            str.writeInt(rawData.length + 1);
            str.writeByte(id);
            str.write(rawData);
        }
    }

    /**
     * Write packet data to the output buffer
     *
     * @param  buffer      buffer stream
     * @throws IOException
     */
    protected abstract void writePacketData(DataOutputStream buffer) throws IOException;

    /**
     * Reads a packet from the input stream
     *
     * @param  is        input stream to read from
     * @return           read packet, or <code>null</code> if the packet was not
     *                   recognized
     * @throws Exception if there was an error reading packet
     */
    public static Packet readFromStream(DataInputStream is) throws Exception {
        int length = is.readInt();
        byte id = is.readByte();
        byte[] data = new byte[length - 1];
        is.readFully(data);
        PacketFactory<?> factory = PacketFactoryRegistry.getFactoryForID(id);
        return factory == null ? null : factory.createPacket(data);
    }
}
