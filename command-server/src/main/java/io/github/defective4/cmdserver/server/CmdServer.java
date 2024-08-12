package io.github.defective4.cmdserver.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLContext;

import io.github.defective4.cmdserver.common.ssl.SSLManager;
import io.github.defective4.cmdserver.server.event.ServerListener;

public class CmdServer implements AutoCloseable {

    private final List<ServerListener> listeners = new CopyOnWriteArrayList<>();
    private final int port;
    private final ServerSocket server;
    private final char[] token;

    public CmdServer(Certificate cert, PrivateKey key, int port, char[] token) throws NoSuchAlgorithmException,
            KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
        SSLContext context = SSLManager.mkSSLContext(cert, key);
        server = context.getServerSocketFactory().createServerSocket();
        this.port = port;
        this.token = token;
    }

    public CmdServer(int port, char[] token) throws IOException {
        server = new ServerSocket();
        this.port = port;
        this.token = token;
    }

    public void addListener(ServerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void close() throws IOException {
        server.close();
    }

    public List<ServerListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public void start() throws IOException {
        server.bind(new InetSocketAddress(port));
        while (!isClosed()) {
            try (ClientConnection client = new ClientConnection(server.accept(), this)) {
                for (ServerListener ls : listeners) ls.clientConnected(client);
                client.handle();
            } catch (SocketException e) {} catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected char[] getToken() {
        return token;
    }

    private boolean isClosed() {
        return server.isClosed();
    }

}
