package io.github.defective4.trivialpacket.common.packet.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.github.defective4.trivialpacket.common.packet.Packet;
import io.github.defective4.trivialpacket.common.packet.PacketFactory;

/**
 * Authentication packet.
 */
@SuppressWarnings("javadoc")
public class AuthPacket extends Packet {

    public static final PacketFactory<AuthPacket> FACTORY = new PacketFactory<>(AuthPacket.class) {

        @Override
        protected AuthPacket createPacket(byte[] data) {
            return new AuthPacket(new String(data, StandardCharsets.UTF_8).toCharArray());
        }
    };

    private final char[] token;

    public AuthPacket(char[] token) {
        this.token = token;
    }

    public char[] getToken() {
        return token;
    }

    @Override
    protected void writePacket(DataOutputStream str) throws IOException {
        str.write(new String(token).getBytes(StandardCharsets.UTF_8));
    }

}
