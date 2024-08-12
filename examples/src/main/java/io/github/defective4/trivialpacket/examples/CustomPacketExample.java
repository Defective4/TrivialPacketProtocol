package io.github.defective4.trivialpacket.examples;

import java.io.IOException;

import io.github.defective4.trivialpacket.client.CmdClient;
import io.github.defective4.trivialpacket.client.event.ClientAdapter;
import io.github.defective4.trivialpacket.common.packet.Packet;
import io.github.defective4.trivialpacket.common.packet.PacketRegistry;
import io.github.defective4.trivialpacket.server.ClientConnection;
import io.github.defective4.trivialpacket.server.CmdServer;
import io.github.defective4.trivialpacket.server.event.ServerAdapter;

public class CustomPacketExample {

    public static class ExamplePacket extends Packet {

        private final String string;

        public ExamplePacket(byte[] data) {
            super(data);
            this.string = new String(data);
        }

        public ExamplePacket(String string) {
            this(string.getBytes());
        }

        public String getString() {
            return string;
        }

    }

    public static void main(String[] args) {
        try {
            PacketRegistry.registerNewPacket(ExamplePacket.class);

            CmdServer server = new CmdServer("localhost", 8083, null);
            server.addListener(new ServerAdapter() {

                @Override
                public void customPacketReceived(ClientConnection connection, Packet packet) throws Exception {
                    System.out.println("Received custom packet: " + packet.getClass());
                    if (packet instanceof ExamplePacket example) {
                        System.out.println(example.getString());
                        connection.disconnect("Closed");
                        server.close();
                    }
                }

            });

            new Thread(() -> {
                try {
                    server.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            try (CmdClient client = new CmdClient("localhost", 8083, null)) {
                client.addListener(new ClientAdapter() {

                    @Override
                    public void authorized() throws Exception {
                        client.sendPacket(new ExamplePacket("Test String"));
                    }

                });
                client.connect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
