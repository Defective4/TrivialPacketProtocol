package io.github.defective4.trivialpacket.common.packet.client;

import java.nio.charset.StandardCharsets;

import io.github.defective4.trivialpacket.common.packet.Packet;

/**
 * Authentication packet.
 */
@SuppressWarnings("javadoc")
public class AuthPacket extends Packet {
    private final char[] token;

    public AuthPacket(byte[] data) {
        super(data);
        token = new String(data, StandardCharsets.UTF_8).toCharArray();
    }

    public AuthPacket(char[] token) {
        this(new String(token).getBytes(StandardCharsets.UTF_8));
    }

    public char[] getToken() {
        return token;
    }

}
