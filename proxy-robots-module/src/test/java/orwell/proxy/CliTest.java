package orwell.proxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.Configuration;
import orwell.proxy.config.source.ConfigurationFile;
import orwell.proxy.config.source.ConfigurationResource;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by Michaël Ludmann on 6/4/15.
 */
@RunWith(JUnit4.class)
public class CliTest {
    private final static Logger logback = LoggerFactory.getLogger(CliTest.class);
    private final static String FILE_NAME = "fileTest";
    private final static String FILE_EXT = ".xml";
    private final static String URL_STRING = "http://url.test";
    private Cli cli;
    private String[] cliInput;
    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        logback.debug(">>>>>>>>> IN");
    }

    @Test
    public void testHelp() throws Exception {
        cliInput = new String[]{"-h"};
        cli = new Cli(cliInput);
        configuration = cli.parse();

        assertNull(configuration);
    }

    @Test
    public void testFile() throws Exception {
        File file = null;
        try {
            // Create empty file
            file = File.createTempFile(FILE_NAME, FILE_EXT);
        } catch (final IOException e) {
            fail(e.toString());
        }

        cliInput = new String[]{"-f", file.getPath()};
        cli = new Cli(cliInput);
        configuration = cli.parse();

        assertNotNull(configuration);
        assertTrue(configuration instanceof ConfigurationFile);
    }

    @Test
    public void testFile_notFound() throws Exception {
        cliInput = new String[]{"-f", FILE_NAME + FILE_EXT};
        cli = new Cli(cliInput);
        configuration = cli.parse();

        assertNull(configuration);
    }

    @Test
    public void testResource() throws Exception {
        cliInput = new String[]{};
        cli = new Cli(cliInput);
        configuration = cli.parse();

        assertNotNull(configuration);
        assertTrue(configuration instanceof ConfigurationResource);
    }

    @Test
    public void testUrl() throws Exception {
        cliInput = new String[]{"-u", URL_STRING};
        cli = new Cli(cliInput);
        configuration = cli.parse();

        assertNull(configuration);
    }

    @Test
    public void testParse_MutuallyExclusiveOptions() throws Exception {
        cliInput = new String[]{"-f", FILE_NAME + FILE_EXT, "-u", URL_STRING};
        cli = new Cli(cliInput);
        configuration = cli.parse();

        assertNull(configuration);
    }

    @Test
    public void testParse_ParseException() throws Exception {
        cliInput = new String[]{"-n", "NicolasCage"};
        cli = new Cli(cliInput);
        configuration = cli.parse();

        assertNull(configuration);
    }

    @Test
    public void testParse_UnknownParameter() throws Exception {
        cliInput = new String[]{"NicolasCage"};
        cli = new Cli(cliInput);
        configuration = cli.parse();

        assertNull(configuration);
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
