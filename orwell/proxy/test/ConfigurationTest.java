package orwell.proxy.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Configuration}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ConfigurationTest {

    @Test
    public void thisAlwaysPasses() {
    	assertEquals(1, 1);
    }

    @Test
    public void failure() {
    	assertEquals(1, 2);
    }
    
    @Test
    @Ignore
    public void thisIsIgnored() {
    }
}

