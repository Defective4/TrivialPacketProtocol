package io.github.defective4.trivialpacket.examples;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;

import io.github.defective4.trivialpacket.client.CmdClient;
import io.github.defective4.trivialpacket.client.event.ClientAdapter;
import io.github.defective4.trivialpacket.server.ClientConnection;
import io.github.defective4.trivialpacket.server.CmdServer;
import io.github.defective4.trivialpacket.server.event.ServerAdapter;

public class SSLExample {

    public static void main(String[] args) {
        try {
            PrivateKey key = KeyFactory
                    .getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(Path.of("ssl/key.pkcs8"))));
            Certificate cert;
            try (InputStream is = new FileInputStream("ssl/cert.pem")) {
                cert = CertificateFactory.getInstance("X509").generateCertificate(is);
            }

            CmdServer server = new CmdServer("localhost", 8081, null, cert, key);
            server.addListener(new ServerAdapter() {

                @Override
                public void clientAuthorized(ClientConnection connection) throws Exception {
                    System.out.println("[SERVER] [" + connection.getInetAddress() + "] AUTHORIZED");
                }

                @Override
                public void clientDisconnected(ClientConnection connection) throws Exception {
                    server.close();
                }

            });
            new Thread(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            Thread.sleep(500);

            try (CmdClient client = new CmdClient("localhost", 8081, null, cert)) {
                client.addListener(new ClientAdapter() {

                    @Override
                    public void authorized() throws Exception {
                        System.out.println("[CLIENT] AUTHORIZED");
                        client.disconnect("Disconnected");
                    }

                });
                client.connect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
