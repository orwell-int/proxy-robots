package orwell.proxy;

import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Created by Michaël Ludmann on 5/26/15.
 */
@SuppressWarnings("unused")
@RunWith(JUnit4.class)
public class UdpBeaconFinderFactoryTest {
    private final static Logger logback = LoggerFactory.getLogger(UdpBeaconFinderFactoryTest.class);
    private final static int BROADCAST_PORT = 9999;
    private final static int BROADCAST_ATTEMPT_NB = 5;
    private final static int BROADCAST_TIMEOUT_MS = 100;

    @TestSubject
    private UdpBeaconFinder udpBeaconFinder;

    @Before
    public void setUp() throws Exception {
        logback.info("IN");

        logback.info("OUT");
    }

    @Test
    public void testFromParameters() throws Exception {
        udpBeaconFinder = UdpBeaconFinderFactory.fromParameters(BROADCAST_PORT,
                BROADCAST_ATTEMPT_NB, BROADCAST_TIMEOUT_MS);
        udpBeaconFinder.startBroadcasting();

        // There is no server running (or mock simulating it)
        // so the beacon should fail by reaching its max attempts of broadcast
        assertEquals(BROADCAST_ATTEMPT_NB, udpBeaconFinder.getNumberOfPerformedAttempts());
    }


}
