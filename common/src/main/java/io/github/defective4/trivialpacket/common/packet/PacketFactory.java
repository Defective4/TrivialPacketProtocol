package io.github.defective4.trivialpacket.common.packet;

public abstract class PacketFactory<T extends Packet> {

    private final Class<T> packetClass;

    public PacketFactory(Class<T> packetClass) {
        this.packetClass = packetClass;
    }

    public Class<T> getPacketClass() {
        return packetClass;
    }

    protected abstract T createPacket(byte[] data) throws Exception;

}
