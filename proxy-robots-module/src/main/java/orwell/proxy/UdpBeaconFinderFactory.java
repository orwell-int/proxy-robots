package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.IConfigUdpBroadcast;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by Michaël Ludmann on 5/27/15.
 */
public final class UdpBeaconFinderFactory {
    private final static Logger logback = LoggerFactory.getLogger(UdpBeaconFinderFactory.class);

    public static UdpBeaconFinder fromParameters(final int port, final int attempts,
                                             final int timeoutPerAttemptMs) {
        try {
            final DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(timeoutPerAttemptMs);
            final UdpBeaconFinder udpBeaconFinder = new UdpBeaconFinder(datagramSocket,
                    port, new UdpBeaconDecoder());
            udpBeaconFinder.setMaxAttemptsNumber(attempts);
            return udpBeaconFinder;
        } catch (final SocketException e) {
            logback.error(e.getMessage());
            return null;
        }
    }

    public static UdpBeaconFinder fromConfig(final IConfigUdpBroadcast configUdpBroadcast) {
        if (null == configUdpBroadcast) {
            return null;
        } else {
            return fromParameters(configUdpBroadcast.getPort(), configUdpBroadcast.getAttempts(),
                    configUdpBroadcast.getTimeoutPerAttemptMs());
        }
    }
}
