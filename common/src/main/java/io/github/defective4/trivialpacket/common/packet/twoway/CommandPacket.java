package io.github.defective4.trivialpacket.common.packet.twoway;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.github.defective4.trivialpacket.common.packet.Packet;
import io.github.defective4.trivialpacket.common.packet.PacketFactory;

/**
 * Command packet
 */
@SuppressWarnings("javadoc")
public class CommandPacket extends Packet {

    public static final PacketFactory<CommandPacket> FACTORY = new PacketFactory<>(CommandPacket.class) {

        @Override
        protected CommandPacket createPacket(byte[] data) throws Exception {
            try (ByteArrayInputStream buffer = new ByteArrayInputStream(data);
                    DataInputStream wrapper = new DataInputStream(buffer)) {
                String command = wrapper.readUTF();
                String[] arguments = new String[wrapper.readInt()];
                for (int i = 0; i < arguments.length; i++) arguments[i] = wrapper.readUTF();
                return new CommandPacket(command, arguments);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    };

    private final String[] arguments;
    private final String command;

    public CommandPacket(String command, String[] arguments) {
        this.arguments = arguments;
        this.command = command;
    }

    public String[] getArguments() {
        return arguments;
    }

    public String getCommand() {
        return command;
    }

    @Override
    protected void writePacket(DataOutputStream str) throws IOException {
        str.writeUTF(command);
        str.writeInt(arguments.length);
        for (String arg : arguments) str.writeUTF(arg);
    }

}
