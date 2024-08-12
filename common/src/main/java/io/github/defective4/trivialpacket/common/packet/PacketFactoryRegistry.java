package io.github.defective4.trivialpacket.common.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.defective4.trivialpacket.common.packet.client.AuthPacket;
import io.github.defective4.trivialpacket.common.packet.server.AuthSuccessPacket;
import io.github.defective4.trivialpacket.common.packet.twoway.CommandPacket;
import io.github.defective4.trivialpacket.common.packet.twoway.CommandResponsePacket;
import io.github.defective4.trivialpacket.common.packet.twoway.DisconnectPacket;
import io.github.defective4.trivialpacket.common.packet.twoway.PingPacket;

/**
 * Packet registry stores packet factories required to create packets of
 * different kinds. <br>
 * It stores both built-in factories that cannot be modified, and user-defined
 * ones.
 */
public class PacketFactoryRegistry {
    private static boolean registeringDisabled, unregisteringDisabled;
    private static final Map<Integer, PacketFactory<?>> BUILTIN = Map
            .of(0, AuthPacket.FACTORY, 1, AuthSuccessPacket.FACTORY, 2, DisconnectPacket.FACTORY, 3, PingPacket.FACTORY,
                    4, CommandPacket.FACTORY, 5, CommandResponsePacket.FACTORY);
    private static final Map<Integer, PacketFactory<?>> FACTORIES = new HashMap<>();

    static {
        FACTORIES.putAll(BUILTIN);
    }

    /**
     * Disables registering of new packet factories. <br>
     * This function exists for security reasons and cannot be reverted.
     */
    public static void disableRegistering() {
        registeringDisabled = true;
    }

    /**
     * Disables unregistering of existing packet factories. <br>
     * This function exists for security reasons and cannot be reverted.
     */
    public static void disableUnregistering() {
        unregisteringDisabled = true;
    }

    /**
     * Checks if the packet instance is built-in.
     *
     * @param  packet packet to check
     * @return        <code>true</code> if built-in
     */
    public static boolean isBuiltIn(Packet packet) {
        return BUILTIN.containsKey(packet.getId());
    }

    /**
     * Registers a new factory. <br>
     * IDs 0-5 are currently reserved for built-in packets and therefore cannot be
     * overridden.
     *
     * @param  id                       packet id. Can't be less than 0
     * @param  factory                  factory to register
     * @throws IllegalStateException    if registering is disabled
     * @throws IllegalArgumentException if id < 0, id is already used by another
     *                                  packet or if the id belongs to a built-in
     *                                  packet
     */
    public static void registerPacketFactory(int id, PacketFactory<?> factory) {
        Objects.requireNonNull(factory);
        if (registeringDisabled) throw new IllegalStateException("Factory registering is disabled");
        if (id < 0) throw new IllegalArgumentException("id < 0");
        if (FACTORIES.containsKey(id)) throw new IllegalArgumentException("Packet " + id + " is already registered.");
        if (BUILTIN.values().stream().anyMatch(f -> f.getPacketClass() == factory.getPacketClass()))
            throw new IllegalArgumentException("Can't register built-in packets");
        FACTORIES.put(id, factory);
    }

    /**
     * Unregisters an existing factory. <br>
     * IDs 0-5 are currently reserved for built-in packets and therefore cannot be
     * unregistered.
     *
     * @param  id                       packet ID
     * @return                          <code>true</code> if there was a factory
     *                                  under that ID
     * @throws IllegalStateException    if unregistering is disabled
     * @throws IllegalArgumentException if the ID belongs to a built-in packet
     */
    public static boolean unregisterPacketFactory(int id) {
        if (unregisteringDisabled) throw new IllegalStateException("Factory unregistering is disabled");
        if (BUILTIN.containsKey(id)) throw new IllegalArgumentException("Can't unregister built-in packets");
        return FACTORIES.remove(id) != null;
    }

    static PacketFactory<?> getFactoryForID(int id) {
        return FACTORIES.get(id);
    }

    static int getIDForPacketClass(Class<? extends Packet> packetClass) {
        return FACTORIES
                .entrySet()
                .stream()
                .filter(val -> val.getValue().getPacketClass() == packetClass)
                .findFirst()
                .orElseThrow()
                .getKey();
    }
}
