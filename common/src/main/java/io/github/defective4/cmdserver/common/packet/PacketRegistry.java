package io.github.defective4.cmdserver.common.packet;

import java.util.Map;

import io.github.defective4.cmdserver.common.packet.client.AuthPacket;
import io.github.defective4.cmdserver.common.packet.server.AuthSuccessPacket;
import io.github.defective4.cmdserver.common.packet.twoway.CommandPacket;
import io.github.defective4.cmdserver.common.packet.twoway.CommandResponsePacket;
import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;
import io.github.defective4.cmdserver.common.packet.twoway.PingPacket;

@SuppressWarnings("javadoc")
class PacketRegistry {
    private static final Map<Integer, Class<? extends Packet>> PACKET_MAP = Map
            .of(0, DisconnectPacket.class, 1, AuthPacket.class, 2, AuthSuccessPacket.class, 3, PingPacket.class, 4,
                    CommandPacket.class, 5, CommandResponsePacket.class);

    protected static int getIDForClass(Class<? extends Packet> packet) {
        return PACKET_MAP
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == packet)
                .findFirst()
                .orElseThrow()
                .getKey();
    }

    protected static Class<? extends Packet> getPacketForID(int id) {
        return PACKET_MAP.get(id);
    }
}
