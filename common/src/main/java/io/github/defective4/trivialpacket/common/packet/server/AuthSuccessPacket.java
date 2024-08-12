package io.github.defective4.trivialpacket.common.packet.server;

import io.github.defective4.trivialpacket.common.packet.Packet;

/**
 * Authentication success packet.
 */
@SuppressWarnings("javadoc")
public class AuthSuccessPacket extends Packet {

    public AuthSuccessPacket() {
        this(new byte[0]);
    }

    public AuthSuccessPacket(byte[] data) {
        super(data);
    }

}