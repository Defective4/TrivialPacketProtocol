package io.github.defective4.trivialpacket.common.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import io.github.defective4.trivialpacket.common.packet.client.AuthPacket;
import io.github.defective4.trivialpacket.common.packet.server.AuthSuccessPacket;
import io.github.defective4.trivialpacket.common.packet.twoway.CommandPacket;
import io.github.defective4.trivialpacket.common.packet.twoway.CommandResponsePacket;
import io.github.defective4.trivialpacket.common.packet.twoway.DisconnectPacket;
import io.github.defective4.trivialpacket.common.packet.twoway.PingPacket;

/**
 *
 */
public class PacketRegistry {
    private static final Map<Integer, Class<? extends Packet>> FIXED = Map
            .of(0, DisconnectPacket.class, 1, AuthPacket.class, 2, AuthSuccessPacket.class, 3, PingPacket.class, 4,
                    CommandPacket.class, 5, CommandResponsePacket.class);
    private static final Map<Integer, Class<? extends Packet>> PACKET_MAP = new HashMap<>();

    private static boolean unregisteringLocked, registeringLocked;

    static {
        PACKET_MAP.putAll(FIXED);
    }

    /**
     * Get packet registered under a specified ID
     *
     * @param  id packet's id
     * @return    registered packet, or <code>null</code> if none
     */
    public static Class<? extends Packet> getPacketForID(int id) {
        return PACKET_MAP.get(id);
    }

    /**
     * Locks packet registry, meaning no-one will be able to register new
     * packets.<br>
     * This method exists for security purposes. <br>
     * This action can't be reverted.
     */
    public static void lockPacketRegistering() {
        registeringLocked = true;
    }

    /**
     * Locks packet registry, meaning no-one will be able to unregister existing
     * packets.<br>
     * This method exists for security purposes. <br>
     * This action can't be reverted.
     */
    public static void lockPacketUnregistering() {
        unregisteringLocked = true;
    }

    /**
     * Registers a new packet. <br>
     * Keep in mind you can't register built-in packets.
     *
     * @param  packet                   the packet to register
     * @throws IllegalStateException    if registering is disabled
     * @throws IllegalArgumentException if trying to register a built-in packet
     * @throws NullPointerException     if packet is null
     */
    public static void registerNewPacket(Class<? extends Packet> packet) {
        Objects.requireNonNull(packet);
        if (registeringLocked) throw new IllegalStateException("Registering new packets is not allowed");
        if (FIXED.containsValue(packet)) throw new IllegalArgumentException("You can't unregister built-in packets");
        int freeID = 0;
        while (PACKET_MAP.containsKey(freeID)) freeID++;
        PACKET_MAP.put(freeID, packet);
    }

    /**
     * Unregisters an existing packet. <br>
     * Keep in mind you can't unregister built-in packets.
     *
     * @param  packet                   the packet you want to unregister
     * @throws NoSuchElementException   if the packet is not registered
     * @throws IllegalStateException    if unregistering packets is disabled
     * @throws IllegalArgumentException if trying to unregister a built-in packet
     */
    public static void unregisterPacket(Class<? extends Packet> packet) {
        Objects.requireNonNull(packet);
        if (unregisteringLocked) throw new IllegalStateException("Unregistering new packets is not allowed");
        if (FIXED.containsValue(packet)) throw new IllegalArgumentException("You can't unregister built-in packets");
        int id = getIDForClass(packet);
        PACKET_MAP.remove(id);
    }

    protected static int getIDForClass(Class<? extends Packet> packet) {
        return PACKET_MAP
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == packet)
                .findFirst()
                .orElseThrow()
                .getKey();
    }

    protected static boolean isBuiltIn(Class<? extends Packet> packet) {
        return FIXED.containsValue(packet);
    }
}
