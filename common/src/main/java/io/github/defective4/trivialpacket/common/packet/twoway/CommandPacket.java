package io.github.defective4.trivialpacket.common.packet.twoway;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import io.github.defective4.trivialpacket.common.packet.Packet;

/**
 * Command packet
 */
@SuppressWarnings("javadoc")
public class CommandPacket extends Packet {

    private String[] arguments;
    private String command;

    public CommandPacket(byte[] data) {
        super(data);
        readBytes(data);
    }

    public CommandPacket(String command, String[] arguments) {
        super(mkBytes(command, arguments));
    }

    public String[] getArguments() {
        return arguments;
    }

    public String getCommand() {
        return command;
    }

    private void readBytes(byte[] data) {
        try (ByteArrayInputStream buffer = new ByteArrayInputStream(data)) {
            try (DataInputStream wrapper = new DataInputStream(buffer)) {
                command = wrapper.readUTF();
                arguments = new String[wrapper.readInt()];
                for (int i = 0; i < arguments.length; i++) arguments[i] = wrapper.readUTF();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] mkBytes(String command, String[] args) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            try (DataOutputStream wrapper = new DataOutputStream(buffer)) {
                wrapper.writeUTF(command);
                wrapper.writeInt(args.length);
                for (String arg : args) wrapper.writeUTF(arg);
            }
            return buffer.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
