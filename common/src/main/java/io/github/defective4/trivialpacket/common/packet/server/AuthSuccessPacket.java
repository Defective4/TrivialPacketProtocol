package io.github.defective4.trivialpacket.common.packet.server;

import java.io.DataOutputStream;

import io.github.defective4.trivialpacket.common.packet.Packet;
import io.github.defective4.trivialpacket.common.packet.PacketFactory;

/**
 * Authentication success packet.
 */
@SuppressWarnings("javadoc")
public class AuthSuccessPacket extends Packet {

    public static final PacketFactory<AuthSuccessPacket> FACTORY = new PacketFactory<>(
            AuthSuccessPacket.class) {

        @Override
        protected AuthSuccessPacket createPacket(byte[] data) throws Exception {
            return new AuthSuccessPacket();
        }
    };

    @Override
    protected void writePacket(DataOutputStream str) {}

}
