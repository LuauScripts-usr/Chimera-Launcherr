package org.chimeramc.launcher.settings;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;

/**
 * Optional low-latency networking support ("Reduce Network Latency").
 *
 * Honest scope (a launcher cannot control server distance/ISP routing, so this never
 * promises "lowest ping"). What this actually does:
 *  - Disables Nagle's algorithm (TCP_NODELAY) on sockets created through {@link #createSocketFactory()},
 *    which are the launcher's own news/update HTTP connections.
 *  - Warm DNS lookups for known launcher endpoints so their IPs are cached before a session starts.
 *  - Marks the start/end of an active game session so automatic, non-essential background
 *    network callers (news polls, update checks) can be paused during gameplay.
 */
public final class LowLatencyNetworkManager {
    @SuppressLint("StaticFieldLeak")
    private static volatile Context sAppContext;

    private static final List<String> PREFETCH_HOSTS = Arrays.asList(
            "raw.githubusercontent.com",
            "api.github.com",
            "api.curseforge.com",
            "www.googleapis.com"
    );

    private static final ExecutorService DNS_EXECUTOR = Executors.newSingleThreadExecutor();

    private LowLatencyNetworkManager() {
    }

    public static void init(Context context) {
        sAppContext = context.getApplicationContext();
        FeatureSettings fs = FeatureSettings.getInstance();
        if (fs != null && fs.isReduceNetworkLatencyEnabled()) {
            prefetchDnsOnBackground();
        }
    }

    public static boolean isEnabled() {
        FeatureSettings fs = FeatureSettings.getInstance();
        return fs != null && fs.isReduceNetworkLatencyEnabled();
    }

    public static SocketFactory createSocketFactory() {
        return new SocketFactory() {
            private final SocketFactory delegate = SocketFactory.getDefault();

            @Override
            public Socket createSocket() throws IOException {
                return configure(delegate.createSocket());
            }

            @Override
            public Socket createSocket(String host, int port) throws IOException {
                return configure(delegate.createSocket(host, port));
            }

            @Override
            public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
                    throws IOException {
                return configure(delegate.createSocket(host, port, localHost, localPort));
            }

            @Override
            public Socket createSocket(InetAddress host, int port) throws IOException {
                return configure(delegate.createSocket(host, port));
            }

            @Override
            public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
                    throws IOException {
                return configure(delegate.createSocket(address, port, localAddress, localPort));
            }

            private Socket configure(Socket socket) throws IOException {
                try {
                    socket.setTcpNoDelay(true);
                } catch (IOException ignored) {
                }
                return socket;
            }
        };
    }

    public static void prefetchDnsOnBackground() {
        if (!isEnabled()) return;
        for (final String host : PREFETCH_HOSTS) {
            DNS_EXECUTOR.execute(() -> {
                try {
                    InetAddress.getAllByName(host);
                } catch (Exception ignored) {
                }
            });
        }
    }

    /**
     * True while a Minecraft session is running. Automatic (non user-initiated) network
     * callers should bail out early and use cached data while this is set.
     */
    public static boolean isGameSessionActive() {
        return GameQuietZoneHolder.active;
    }

    public static void setGameSessionActive(boolean active) {
        GameQuietZoneHolder.active = active;
    }

    private static final class GameQuietZoneHolder {
        private static volatile boolean active;
    }
}