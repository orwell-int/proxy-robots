package orwell.proxy.robot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot;

import static org.junit.Assert.*;


/**
 * Created by Michaël Ludmann on 5/13/15.
 */
public class RfidSensorTest {
    final static Logger logback = LoggerFactory.getLogger(RfidSensorTest.class);
    private final static String FIRST_VALUE = "11111111";
    private final static String SECOND_VALUE = "22222222";
    private final static String NO_RFID_VALUE = "0";
    private RfidSensor rfidSensor;

    @Before
    public void setUp() throws Exception {
        logback.info("IN");
        rfidSensor = new RfidSensor();
    }

    @Test
    public void testEmpty() throws Exception {
        assertTrue(rfidSensor.getRfidSensorReads().isEmpty());
    }

    @Test
    public void testClear() throws Exception {
        rfidSensor.clear();
        assertTrue(rfidSensor.getRfidSensorReads().isEmpty());

        rfidSensor.setValue(FIRST_VALUE);
        assertFalse(rfidSensor.getRfidSensorReads().isEmpty());

        rfidSensor.clear();
        assertTrue(rfidSensor.getRfidSensorReads().isEmpty());
    }


    @Test
    public void testSetValue() throws Exception {
        rfidSensor.setValue(FIRST_VALUE);
        assertEquals(FIRST_VALUE, rfidSensor.getRfidSensorReads().getFirst().getRfid());
        assertEquals(Robot.Status.ON, rfidSensor.getRfidSensorReads().getFirst().getStatus());
    }


    @Test
    public void testSetValue_SecondValue() throws Exception {
        rfidSensor.setValue(FIRST_VALUE);
        assertEquals(FIRST_VALUE, rfidSensor.getRfidSensorReads().getFirst().getRfid());
        assertEquals(Robot.Status.ON, rfidSensor.getRfidSensorReads().getFirst().getStatus());
        rfidSensor.setValue(SECOND_VALUE);
        // Now we should have:
        // Second Rfid ON -> First Rfid OFF -> First Rfid ON
        assertEquals(SECOND_VALUE, rfidSensor.getRfidSensorReads().getFirst().getRfid());
        assertEquals(Robot.Status.ON, rfidSensor.getRfidSensorReads().getFirst().getStatus());

        assertEquals(3, rfidSensor.getRfidSensorReads().size());

        // Check that a "First Rfid" was indeed inserted and set to OFF
        rfidSensor.getRfidSensorReads().pop();
        assertEquals(FIRST_VALUE, rfidSensor.getRfidSensorReads().getFirst().getRfid());
        assertEquals(Robot.Status.OFF, rfidSensor.getRfidSensorReads().getFirst().getStatus());
    }


    @Test
    public void testSetValue_SameValue() throws Exception {
        rfidSensor.setValue(FIRST_VALUE);
        rfidSensor.setValue(FIRST_VALUE);
        // Now we should have:
        // First Rfid ON
        assertEquals(1, rfidSensor.getRfidSensorReads().size());
        assertEquals(Robot.Status.ON, rfidSensor.getRfidSensorReads().getFirst().getStatus());
    }


    @Test
    public void testSetValue_NoRfid() throws Exception {
        rfidSensor.setValue(NO_RFID_VALUE);
        assertTrue(rfidSensor.getRfidSensorReads().isEmpty());
    }


    @Test
    public void testSetValue_TransitionToNoRfid() throws Exception {
        rfidSensor.setValue(FIRST_VALUE);
        rfidSensor.setValue(NO_RFID_VALUE);
        // Now we should have:
        // First Rfid OFF -> First Rfid ON
        assertEquals(2, rfidSensor.getRfidSensorReads().size());
        assertEquals(FIRST_VALUE, rfidSensor.getRfidSensorReads().getFirst().getRfid());
        assertEquals(Robot.Status.OFF, rfidSensor.getRfidSensorReads().getFirst().getStatus());
        assertEquals(FIRST_VALUE, rfidSensor.getRfidSensorReads().getLast().getRfid());
        assertEquals(Robot.Status.ON, rfidSensor.getRfidSensorReads().getLast().getStatus());
    }


    @Test
    public void testSetValue_TransitionFromNoRfid() throws Exception {
        rfidSensor.setValue(NO_RFID_VALUE);
        rfidSensor.setValue(FIRST_VALUE);
        // Now we should have:
        // First Rfid ON
        assertEquals(1, rfidSensor.getRfidSensorReads().size());
        assertEquals(FIRST_VALUE, rfidSensor.getRfidSensorReads().getFirst().getRfid());
        assertEquals(Robot.Status.ON, rfidSensor.getRfidSensorReads().getFirst().getStatus());
    }

    @After
    public void tearDown() throws Exception {
        logback.info("OUT");
    }
}
