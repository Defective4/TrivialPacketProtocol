package io.github.defective4.cmdserver.common.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

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
public class Packet {
    private final byte[] data;
    private final int id;

    /**
     * Constructs a new packet.
     *
     * @param  data
     * @throws NullPointerException if data is null
     */
    protected Packet(byte[] data) {
        Objects.requireNonNull(data);
        id = PacketRegistry.getIDForClass(getClass());
        this.data = data;
    }

    /**
     * Write the packet to stream
     *
     * @param  str                  output stream
     * @throws IOException          when there was an error writing packet data
     * @throws NullPointerException if str is null
     */
    public void writeToStream(DataOutputStream str) throws IOException {
        Objects.requireNonNull(str);
        str.writeInt(data.length + 1);
        str.writeByte(id);
        str.write(data);
    }

    /**
     * Read packet data from the stream
     *
     * @param  isr                  input stream
     * @return                      read packet
     * @throws IOException          if there was an error reading the packet
     * @throws NullPointerException if isr is null
     */
    public static Packet readFromStream(DataInputStream isr) throws IOException {
        Objects.requireNonNull(isr);
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
