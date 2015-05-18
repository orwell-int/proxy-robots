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
public class ColourSensorTest {
    final static Logger logback = LoggerFactory.getLogger(ColourSensorTest.class);
    private final static String FIRST_VALUE = "1";
    private final static String SECOND_VALUE = "2";
    private ColourSensor colourSensor;

    @Before
    public void setUp() throws Exception {
        logback.info("IN");
        colourSensor = new ColourSensor();
    }

    @Test
    public void testEmpty() throws Exception {
        assertTrue(colourSensor.getColourSensorReads().isEmpty());
    }

    @Test
    public void testClear() throws Exception {
        colourSensor.clear();
        assertTrue(colourSensor.getColourSensorReads().isEmpty());

        colourSensor.setValue(FIRST_VALUE);
        assertFalse(colourSensor.getColourSensorReads().isEmpty());

        colourSensor.clear();
        assertTrue(colourSensor.getColourSensorReads().isEmpty());
    }


    @Test
    public void testSetValue() throws Exception {
        colourSensor.setValue(FIRST_VALUE);
        assertEquals(Integer.parseInt(FIRST_VALUE), colourSensor.getColourSensorReads().getFirst().getColour());
        assertEquals(Robot.Status.ON, colourSensor.getColourSensorReads().getFirst().getStatus());
    }


    @Test
    public void testSetValue_SecondValue() throws Exception {
        colourSensor.setValue(FIRST_VALUE);
        assertEquals(Integer.parseInt(FIRST_VALUE), colourSensor.getColourSensorReads().getFirst().getColour());
        assertEquals(Robot.Status.ON, colourSensor.getColourSensorReads().getFirst().getStatus());
        colourSensor.setValue(SECOND_VALUE);
        // Now we should have:
        // Second Colour ON -> First Colour OFF -> First Colour ON
        assertEquals(Integer.parseInt(SECOND_VALUE), colourSensor.getColourSensorReads().getFirst().getColour());
        assertEquals(Robot.Status.ON, colourSensor.getColourSensorReads().getFirst().getStatus());

        assertEquals(3, colourSensor.getColourSensorReads().size());

        // Check that a "First Colour" was indeed inserted and set to OFF
        colourSensor.getColourSensorReads().pop();
        assertEquals(Integer.parseInt(FIRST_VALUE), colourSensor.getColourSensorReads().getFirst().getColour());
        assertEquals(Robot.Status.OFF, colourSensor.getColourSensorReads().getFirst().getStatus());
    }


    @Test
    public void testSetValue_SameValue() throws Exception {
        colourSensor.setValue(FIRST_VALUE);
        colourSensor.setValue(FIRST_VALUE);
        // Now we should have:
        // First Colour ON
        assertEquals(1, colourSensor.getColourSensorReads().size());
        assertEquals(Robot.Status.ON, colourSensor.getColourSensorReads().getFirst().getStatus());
    }


    @After
    public void tearDown() throws Exception {
        logback.info("OUT");
    }
}
