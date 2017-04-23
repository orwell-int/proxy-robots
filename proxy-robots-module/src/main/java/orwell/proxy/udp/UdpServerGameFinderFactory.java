package orwell.proxy.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.elements.IConfigUdpServerGameFinder;

import java.net.DatagramSocket;
import java.net.SocketException;

public final class UdpServerGameFinderFactory {
    private final static Logger logback = LoggerFactory.getLogger(UdpServerGameFinderFactory.class);

    public static UdpServerGameFinder fromParameters(final int port, final int attempts,
                                                     final int timeoutPerAttemptMs) {
        try {
            final DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(timeoutPerAttemptMs);
            final UdpServerGameFinder udpServerGameFinder = new UdpServerGameFinder(datagramSocket,
                    port, new UdpBroadcastDataDecoder());
            udpServerGameFinder.setMaxAttemptsNumber(attempts);
            return udpServerGameFinder;
        } catch (final SocketException e) {
            logback.error(e.getMessage());
            return null;
        }
    }

    public static UdpServerGameFinder fromConfig(final IConfigUdpServerGameFinder configUdpBroadcast) {
        if (null == configUdpBroadcast) {
            return null;
        } else {
            return fromParameters(configUdpBroadcast.getPort(), configUdpBroadcast.getAttempts(),
                    configUdpBroadcast.getTimeoutPerAttemptMs());
        }
    }
}
