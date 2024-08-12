package io.github.defective4.trivialpacket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import io.github.defective4.trivialpacket.common.ssl.SSLManager;
import io.github.defective4.trivialpacket.common.token.FixedTokenProvider;
import io.github.defective4.trivialpacket.common.token.TokenProvider;
import io.github.defective4.trivialpacket.server.event.ServerListener;

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
    private ExecutorService pool;
    private int poolSize = 1;
    private final int port;
    private final ServerSocket server;
    private TokenProvider tokenProvider;

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
        tokenProvider = new FixedTokenProvider(token == null ? new char[0] : token);
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
        tokenProvider = new FixedTokenProvider(token == null ? new char[0] : token);
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
        pool.shutdownNow();
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
     * Get current thread pool size
     *
     * @return current thread pool size
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Get current token provider's class. <br>
     * For security reasons it's not supported to retrieve current token provider.
     * <br>
     * It still might be possible to do using reflection.
     *
     * @return current token provider implementation
     */
    public Class<? extends TokenProvider> getTokenProviderClass() {
        return tokenProvider.getClass();
    }

    /**
     * Set thread pool size. <br>
     * If there is no space for new threads in the current thread pool, no new
     * client will be able to connect. <br>
     * Default pool size for new server instances is <code>1</code><br>
     * You can only set thread pool size on unbound servers.
     *
     * @param  poolSize
     * @throws IllegalStateException    if the server is already bound
     * @throws IllegalArgumentException if poolSize is less than 1
     */
    public void setPoolSize(int poolSize) {
        if (server.isBound()) throw new IllegalStateException("Already bound");
        if (poolSize < 1) throw new IllegalArgumentException("poolSize can't be less than 1");
        this.poolSize = poolSize;
    }

    /**
     * Sets a new token provider. <br>
     *
     * @param  provider             a new token provider
     * @throws NullPointerException if provider is null
     */
    public void setTokenProvider(TokenProvider provider) {
        Objects.requireNonNull(provider);
        tokenProvider = provider;
    }

    /**
     * Bind this server and start listening for connections. <br>
     * To interact with connected clients use {@link ServerListener}
     *
     * @throws IOException           if there was an error starting the server
     * @throws IllegalStateException if the server is already bound
     */
    public void start() throws IOException {
        pool = poolSize == 1 ? Executors.newSingleThreadExecutor() : Executors.newFixedThreadPool(poolSize);
        if (server.isBound()) throw new IllegalStateException("Already bound");
        server.bind(new InetSocketAddress(port));
        while (!isClosed()) {
            Socket socket = server.accept();
            pool.submit(() -> {
                ClientConnection local = null;
                try (ClientConnection client = new ClientConnection(socket, this)) {
                    local = client;
                    for (ServerListener ls : listeners) ls.clientConnected(client);
                    client.handle();
                } catch (Exception e) {} finally {
                    for (ServerListener ls : listeners) try {
                        ls.clientDisconnected(local);
                    } catch (Exception e) {}
                }
            });
        }
    }

    /**
     * Get server's token. For internal use.
     *
     * @return server token
     */
    protected char[] getToken() {
        return tokenProvider.provide();
    }

    private boolean isClosed() {
        return server.isClosed();
    }

}
