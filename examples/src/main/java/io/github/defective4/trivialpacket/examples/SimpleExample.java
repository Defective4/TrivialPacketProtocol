package io.github.defective4.trivialpacket.examples;

import io.github.defective4.trivialpacket.client.CmdClient;
import io.github.defective4.trivialpacket.client.event.ClientAdapter;
import io.github.defective4.trivialpacket.server.ClientConnection;
import io.github.defective4.trivialpacket.server.CmdServer;
import io.github.defective4.trivialpacket.server.event.ServerAdapter;

/**
 * A minimal client and server example
 */
public class SimpleExample {

    public static void main(String[] args) {
        int port = 8080;
        String serverToken = "TOKEN";
        String clientToken = "TOKEN"; // You have to keep the two tokens the same for authorization to work

        new Thread(() -> {
            try (CmdServer server = new CmdServer("localhost", port, serverToken.toCharArray())) {
                server.addListener(new ServerAdapter() {

                    @Override
                    public void clientDisconnected(ClientConnection connection) throws Exception {
                        server.close();
                    }

                    @Override
                    public void commandReceived(ClientConnection connection, String command, String[] args)
                            throws Exception {
                        connection
                                .respond(String
                                        .format("Command: %s, Arguments: [%s]", command, String.join(", ", args))
                                        .getBytes());
                    }
                });

                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        try (CmdClient client = new CmdClient("localhost", port, clientToken.toCharArray())) {
            Thread.sleep(500);
            client.addListener(new ClientAdapter() {

                @Override
                public void authorized() throws Exception {
                    client.sendCommand("echo", "Hello", "World");
                }

                @Override
                public void responseReceived(byte[] data) throws Exception {
                    System.out.println(new String(data));
                    client.disconnect("Disconnected");
                }

            });
            client.connect();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

}
