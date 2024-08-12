package io.github.defective4.trivialpacket.common.packet;

/**
 * Packet factory is used to create new instances of a packet
 *
 * @param <T> packet type
 */
public abstract class PacketFactory<T extends Packet> {

    private final Class<T> packetClass;

    /**
     * Creates a new factory bound to a specific packet class
     *
     * @param packetClass packet class
     */
    public PacketFactory(Class<T> packetClass) {
        this.packetClass = packetClass;
    }

    /**
     * Get factory's packet class
     *
     * @return packet class
     */
    public Class<T> getPacketClass() {
        return packetClass;
    }

    /**
     * Creates a new packet from raw data
     *
     * @param  data
     * @return           created packet. Can be <code>null</code>
     * @throws Exception
     */
    protected abstract T createPacket(byte[] data) throws Exception;

}
