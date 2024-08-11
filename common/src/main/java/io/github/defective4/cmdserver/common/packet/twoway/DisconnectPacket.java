package io.github.defective4.cmdserver.common.packet.twoway;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.github.defective4.cmdserver.common.packet.Packet;

public class DisconnectPacket extends Packet {

    private final boolean allowReconnect;
    private final String reason;

    public DisconnectPacket(byte[] data) {
        super(data);
        allowReconnect = data[data.length - 1] > 0;
        reason = new String(Arrays.copyOf(data, data.length - 1), StandardCharsets.UTF_8);
    }

    public DisconnectPacket(String reason, boolean allowReconnect) {
        this(mkBytes(reason, allowReconnect));
    }

    public String getReason() {
        return reason;
    }

    public boolean isAllowReconnect() {
        return allowReconnect;
    }

    private static byte[] mkBytes(String reason, boolean allowReconnect) {
        byte[] strData = reason.getBytes(StandardCharsets.UTF_8);
        byte[] data = Arrays.copyOf(strData, strData.length + 1);
        data[data.length - 1] = (byte) (allowReconnect ? 1 : 0);
        return data;
    }

}
