package io.github.defective4.cmdserver.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.defective4.cmdserver.common.event.ClientListener;
import io.github.defective4.cmdserver.common.packet.Packet;
import io.github.defective4.cmdserver.common.packet.client.AuthPacket;
import io.github.defective4.cmdserver.common.packet.handler.ClientSidePacketHandler;
import io.github.defective4.cmdserver.common.packet.server.AuthSuccessPacket;
import io.github.defective4.cmdserver.common.packet.twoway.DisconnectPacket;
import io.github.defective4.cmdserver.common.packet.twoway.PingPacket;

public class CmdClient implements AutoCloseable {

    private boolean connected = false;
    private final ClientSidePacketHandler handler = new ClientSidePacketHandler(this);
    private final String host;

    private DataInputStream is;
    private long lastPingID = -1;
    private final List<ClientListener> listeners = new CopyOnWriteArrayList<>();
    private DataOutputStream os;

    private final Timer pingTimer = new Timer(true);
    private final int port;
    private final Socket socket;

    private final char[] token;

    public CmdClient(String host, int port, char[] token) {
        this.token = token;
        socket = new Socket();
        this.host = host;
        this.port = port;
    }

    public void addListener(ClientListener listener) {
        listeners.add(listener);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public void connect() throws Exception {
        if (connected) throw new IllegalStateException("Already connected");
        socket.connect(new InetSocketAddress(host, port));
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        connected = true;
        sendPacket(new AuthPacket(token));
        Packet authResponse = Packet.readFromStream(is);
        if (authResponse instanceof DisconnectPacket disconnectPacket) {
            throw new IOException("Serwer odrzucił połączenie: " + disconnectPacket.getReason());
        }
        if (!(authResponse instanceof AuthSuccessPacket)) {
            throw new IOException("Otrzymano nieprawidłowy pakiet podczas logowania: " + authResponse);
        }
        listeners.forEach(ClientListener::authorized);
        pingTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    if (lastPingID != -1) {
                        disconnect("Timed out");
                        cancel();
                    }
                    lastPingID = System.currentTimeMillis();
                    sendPacket(new PingPacket(lastPingID));
                    listeners.forEach(ls -> ls.serverPinged(lastPingID));
                } catch (IOException e) {
                    cancel();
                    try {
                        close();
                    } catch (IOException e1) {}
                }
            }
        }, 0, 1000);
        while (!socket.isClosed()) {
            try {
                handler.handle(Packet.readFromStream(is));
            } catch (InvocationTargetException e) {
                throw e.getCause() == null ? e : new Exception(e.getCause());
            }
        }
    }

    public void disconnect(String reason) throws IOException {
        sendPacket(new DisconnectPacket(reason, false));
        close();
    }

    public long getLastPingID() {
        return lastPingID;
    }

    public List<ClientListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendPacket(Packet packet) throws IOException {
        if (!connected) throw new IllegalStateException("Not connected");
        packet.writeToStream(os);
    }

    public void setLastPingID(long lastPingID) {
        this.lastPingID = lastPingID;
    }

}
