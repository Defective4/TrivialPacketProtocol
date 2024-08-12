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
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLContext;

import io.github.defective4.cmdserver.common.ssl.SSLManager;
import io.github.defective4.cmdserver.server.event.ServerListener;

/**
 * The main server class. <br>
 * It's used to accept connections and exchange data with
 * <code>CmdCLient</code>s
 *
 * Example usage:
 *
 * <pre>
 * char[] token = "TOKEN".toCharArray();
 * try (CmdServer server = new CmdServer(8080, token)) {
 *     server.addListener(new ServerAdapter() {
 *
 *         {@code @Override}
 *         public void clientAuthorized(ClientConnection connection) throws Exception {
 *             // do something with the authorized client
 *         }
 *
 *     });
 *     server.start();
 * } catch (Exception e) {
 *     e.printStackTrace();
 * }
 * </pre>
 */
public class CmdServer implements AutoCloseable {

    private final String host;
    private final List<ServerListener> listeners = new CopyOnWriteArrayList<>();
    private final int port;
    private final ServerSocket server;
    private final char[] token;

    /**
     * Creates a new command server with no SSL. <br>
     * All connecting clients must have SSL disabled.
     *
     * @param  host        host to listen on. Can be <code>null</code>
     * @param  port        port to listen on
     * @param  token       authorization token. Can be <code>null</code>
     * @throws IOException
     */
    public CmdServer(String host, int port, char[] token) throws IOException {
        server = new ServerSocket();
        this.port = port;
        this.token = token == null ? new char[0] : token;
        this.host = host;
    }

    /**
     * Creates a SSL-enabled command server. <br>
     * All connecting clients must have SSL enabled and they have to use the same
     * certificate as the one provided here.
     *
     * @param  host                      host to listen on. Can be <code>null</code>
     * @param  port                      port to listen on
     * @param  token                     authorization token. Can be
     *                                   <code>null</code>
     * @param  cert                      SSL certificate
     * @param  key                       key associated with the certificate
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     * @throws NullPointerException      if cert or key is null
     */
    public CmdServer(String host, int port, char[] token, Certificate cert, PrivateKey key)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException,
            UnrecoverableKeyException, KeyManagementException {
        Objects.requireNonNull(cert);
        Objects.requireNonNull(key);
        SSLContext context = SSLManager.mkSSLContext(cert, key);
        server = context.getServerSocketFactory().createServerSocket();
        this.port = port;
        this.token = token == null ? new char[0] : token;
        this.host = host;
    }

    /**
     * Add a listener to this server. <br>
     * Added listeners can't be removed.
     *
     * @param  listener             listener to add
     * @throws NullPointerException if listener is null
     */
    public void addListener(ServerListener listener) {
        Objects.requireNonNull(listener);
        listeners.add(listener);
    }

    @Override
    public void close() throws IOException {
        server.close();
    }

    /**
     * Get all server's listeners. <br>
     *
     * @return unmodifiable list of server listeners
     */
    public List<ServerListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Bind this server and start listening for connections. <br>
     * To interact with connected clients use {@link ServerListener}
     *
     * @throws IOException if there was an error starting the server
     */
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

    /**
     * Get server's token. For internal use.
     *
     * @return server token
     */
    protected char[] getToken() {
        return token;
    }

    private boolean isClosed() {
        return server.isClosed();
    }

}
