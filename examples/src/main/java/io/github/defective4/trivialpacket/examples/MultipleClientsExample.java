package io.github.defective4.trivialpacket.examples;

import io.github.defective4.trivialpacket.client.CmdClient;
import io.github.defective4.trivialpacket.client.event.ClientAdapter;
import io.github.defective4.trivialpacket.server.ClientConnection;
import io.github.defective4.trivialpacket.server.CmdServer;
import io.github.defective4.trivialpacket.server.event.ServerAdapter;

public class MultipleClientsExample {

    public static void main(String[] args) {
        new Thread(() -> {
            try (CmdServer server = new CmdServer("localhost", 8082, null)) {
                server.setPoolSize(2);
                server.addListener(new ServerAdapter() {

                    @Override
                    public void clientAuthorized(ClientConnection connection) throws Exception {
                        System.out.println(connection.getInetAddress() + ":" + connection.getPort() + " authorized");
                    }

                });
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try (CmdClient client = new CmdClient("localhost", 8082, null)) {
                client.connect();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try (CmdClient client = new CmdClient("localhost", 8082, null)) {
                Thread.sleep(500);
                client.addListener(new ClientAdapter() {

                    @Override
                    public void authorized() throws Exception {
                        System.out.println("Second client connected, quiting...");
                        System.exit(0);
                    }

                });
                client.connect();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }).start();
    }

}
