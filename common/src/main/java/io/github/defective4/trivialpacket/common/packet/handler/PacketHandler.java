package io.github.defective4.trivialpacket.common.packet.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import io.github.defective4.trivialpacket.common.packet.Packet;

@SuppressWarnings("javadoc")
public abstract class PacketHandler {

    public void handle(Packet packet) throws Exception {
        for (Method m : getClass().getMethods()) if (m.isAnnotationPresent(PacketReceiver.class)) {
            Parameter[] params = m.getParameters();
            if (params.length == 1 && params[0].getType() == packet.getClass()) m.invoke(this, packet);
        }
    }
}
