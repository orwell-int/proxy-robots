package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by parapampa on 11/04/15.
 */
@RunWith(PowerMockRunner.class)
public class TankTest {
    final static Logger logback = LoggerFactory.getLogger(TankTest.class);

    @TestSubject
    private Tank tank;

    @Before
    public void setUp() {
        logback.info("IN");
        tank = new Tank("BtName", "BtId", Camera.getMock(), "Image");
        logback.info("OUT");
    }

    @Test
    /**
     * 1. Tank reads a RFID value: first read of ServerRobotState is not null
     * 2. Nothing happens: second read of ServerRobotState is null
     * 3. Tank reads a Color value: third read of ServerRobotState is not null
     */
    public void testGetAndClearZmqServerRobotState() {
        logback.info("IN");

        UnitMessage unitMessage = new UnitMessage(UnitMessageType.Rfid, "123");
        tank.receivedNewMessage(unitMessage);

        assertNotNull(tank.getAndClearZmqServerRobotStateBytes());

        assertNull(tank.getAndClearZmqServerRobotStateBytes());

        unitMessage = new UnitMessage(UnitMessageType.Colour, "2");
        tank.receivedNewMessage(unitMessage);

        assertNotNull(tank.getAndClearZmqServerRobotStateBytes());

        logback.info("OUT");
    }

    @After
    public void tearDown() {

    }
}
