package orwell.proxy;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.Configuration;
import orwell.proxy.config.source.ConfigurationFile;
import orwell.proxy.config.source.ConfigurationResource;
import orwell.proxy.config.source.NotFileException;

import java.io.FileNotFoundException;

/**
 * Created by Michaël Ludmann on 5/5/15.
 */
public class Cli {
    public final static String CUSTOM_CONFIG_FILEPATH_INSIDE_JAR = "/config.xml";
    private final static Logger logback = LoggerFactory.getLogger(Cli.class);
    private final Options options = new Options();
    private String[] args = null;

    public Cli(final String[] args) {
        this.args = args;

        final Option optionHelp = new Option("h", "help", false, "shows help and exits program.");
        options.addOption(optionHelp);

        // Add a new optionGroup to make --file and --url mutually exclusive
        final Option optionFile = new Option("f", "file", true, "optional filepath for external configuration file");
        final Option optionUrl = new Option("u", "url", true, "optional url for external configuration file, NOT HANDLED YET");
        final OptionGroup optionGroup = new OptionGroup();
        optionGroup.setRequired(false);
        optionGroup.addOption(optionFile);
        optionGroup.addOption(optionUrl);

        options.addOptionGroup(optionGroup);
    }

    /**
     * Extract the config parameters used later by the ConfigFactory
     *
     * @return null if help is called or error happens during parsing
     */
    public Configuration parse() {
        final CommandLineParser parser = new BasicParser();
        final CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                return help();

            if (cmd.hasOption("f")) {
                return configurationFromFile(cmd.getOptionValue("f"));
            } else if (cmd.hasOption("u")) {
                return configurationFromUrl(cmd.getOptionValue("u"));
            } else if (0 < args.length) {
                logback.warn("Unknown parameter: " + args[0]);
                return help();
            } else {
                return configurationFromResource();
            }

        } catch (final ParseException e) {
            logback.error("Failed to parse command line properties", e);
            return help();
        }
    }

    private Configuration help() {
        // This prints out some help
        final HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("-f filepath OR -u http://fake.url", options);
        return null;
    }

    private Configuration configurationFromFile(final String filePath) {
        try {
            return new ConfigurationFile(filePath);
        } catch (final FileNotFoundException e) {
            logback.error(e.getMessage());
            return null;
        } catch (final NotFileException e) {
            logback.error("Found a directory instead of a file: " + e.getMessage());
            return null;
        }
    }

    private Configuration configurationFromUrl(final String url) {
        logback.info("Using file retrieved from URL given as parameter: " + url);
        logback.warn("URL USE CASE NOT HANDLED YET - sorry!");
        return null;
    }

    private Configuration configurationFromResource() {
        logback.info("No argument given to jar, trying to find custom resource configuration file: " +
                CUSTOM_CONFIG_FILEPATH_INSIDE_JAR);
        return new ConfigurationResource(CUSTOM_CONFIG_FILEPATH_INSIDE_JAR);
    }
}
