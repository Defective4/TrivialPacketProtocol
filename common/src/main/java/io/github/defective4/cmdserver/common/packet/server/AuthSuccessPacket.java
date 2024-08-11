package io.github.defective4.cmdserver.common.packet.server;

import io.github.defective4.cmdserver.common.packet.Packet;

public class AuthSuccessPacket extends Packet {

    public AuthSuccessPacket() {
        this(new byte[0]);
    }

    public AuthSuccessPacket(byte[] data) {
        super(data);
    }

}
