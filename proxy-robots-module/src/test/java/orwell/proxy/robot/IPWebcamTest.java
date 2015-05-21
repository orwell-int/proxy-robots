package orwell.proxy.robot;

import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigCamera;
import orwell.proxy.mock.MockedConfigCamera;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Michaël Ludmann on 5/21/15.
 */
public class IPWebcamTest {
    private final static Logger logback = LoggerFactory.getLogger(IPWebcamTest.class);
    private MockedConfigCamera configCamera;

    @TestSubject
    private IPWebcam ipWebcam;

    @Before
    public void setUp() {
        logback.info("IN");
        configCamera = new MockedConfigCamera();
        logback.info("OUT");
    }

    @Test
    public void testGetUrl() throws Exception {
        ipWebcam = new IPWebcam(configCamera);
        assertEquals("http://mockedIp:777/mockedResourcePath", ipWebcam.getUrl());
    }

    @Test
    public void testGetUrl_FromNullConfig() {
        ipWebcam = new IPWebcam((ConfigCamera) null);
        assertNull(ipWebcam.getUrl());
    }

    @Test
    public void testGetUrl_EmptyResourcePath() throws Exception {
        configCamera.setResourcePath("");
        ipWebcam = new IPWebcam(configCamera);
        assertEquals("http://mockedIp:777", ipWebcam.getUrl());
    }

    @Test
    public void testGetUrl_NullResourcePath() throws Exception {
        configCamera.setResourcePath(null);
        ipWebcam = new IPWebcam(configCamera);
        assertEquals("http://mockedIp:777", ipWebcam.getUrl());
    }

    @Test
    public void testGetUrl_ignoreBadPort() throws Exception {
        configCamera.setPort(-1);
        ipWebcam = new IPWebcam(configCamera);
        assertEquals("http://mockedIp/mockedResourcePath", ipWebcam.getUrl());
    }

    @Test
    public void testGetUrl_URLConstructor() throws Exception{
        final URL url = new URL("http://fake.url");
        ipWebcam = new IPWebcam(url);
        assertEquals("http://fake.url", ipWebcam.getUrl());
    }

    @After
    public void tearDown() throws Exception {

    }
}
