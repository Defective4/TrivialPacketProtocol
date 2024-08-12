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

public class PacketRegistry {
    public static boolean registeringDisabled, unregisteringDisabled;
    private static final Map<Integer, PacketFactory<?>> BUILTIN = Map
            .of(0, AuthPacket.FACTORY, 1, AuthSuccessPacket.FACTORY, 2, DisconnectPacket.FACTORY, 3, PingPacket.FACTORY,
                    4, CommandPacket.FACTORY, 5, CommandResponsePacket.FACTORY);
    private static final Map<Integer, PacketFactory<?>> FACTORIES = new HashMap<>();

    static {
        FACTORIES.putAll(BUILTIN);
    }

    public static void disableRegistering() {
        registeringDisabled = true;
    }

    public static void disableUnregistering() {
        unregisteringDisabled = true;
    }

    public static PacketFactory<?> getFactoryForID(int id) {
        return FACTORIES.get(id);
    }

    public static int getIDForPacketClass(Class<? extends Packet> packetClass) {
        return FACTORIES
                .entrySet()
                .stream()
                .filter(val -> val.getValue().getPacketClass() == packetClass)
                .findFirst()
                .orElseThrow()
                .getKey();
    }

    public static boolean isBuiltIn(Packet packet) {
        return BUILTIN.containsKey(packet.getId());
    }

    public static void registerPacketFactory(int id, PacketFactory<?> factory) {
        Objects.requireNonNull(factory);
        if (registeringDisabled) throw new IllegalStateException("Factory registering is disabled");
        if (id < 0) throw new IllegalArgumentException("id < 0");
        if (FACTORIES.containsKey(id)) throw new IllegalArgumentException("Packet " + id + " is already registered.");
        if (BUILTIN.values().stream().anyMatch(f -> f.getPacketClass() == factory.getPacketClass()))
            throw new IllegalArgumentException("Can't register built-in packets");
        FACTORIES.put(id, factory);
    }

    public static boolean unregisterPacketFactory(int id) {
        if (unregisteringDisabled) throw new IllegalStateException("Factory unregistering is disabled");
        if (BUILTIN.containsKey(id)) throw new IllegalArgumentException("Can't unregister built-in packets");
        return FACTORIES.remove(id) != null;
    }
}
