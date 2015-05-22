package orwell.proxy;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigFactoryParameters;
import orwell.proxy.config.EnumConfigFileType;

import java.io.File;

/**
 * Created by Michaël Ludmann on 5/5/15.
 */
class Cli {
    private final static String CONFIG_FILEPATH_INSIDE_JAR = "/configuration.xml";
    private final static Logger logback = LoggerFactory.getLogger(Cli.class);
    private final Options options = new Options();
    private String[] args = null;

    public Cli(final String[] args) {
        this.args = args;

        final Option optionHelp = new Option("h", "help", false, "shows help.");
        options.addOption(optionHelp);

        // Add a new optionGroup to make --file and --url mutually exclusive
        final Option optionFile = new Option("f", "file", true, "optional filepath for external configuration file");
        final Option optionUrl = new Option("u", "url", true, "optional url for external configuration file, NOT HANDLED YET");
        final OptionGroup optionGroup = new OptionGroup();
        optionGroup.setRequired(true);
        optionGroup.addOption(optionFile);
        optionGroup.addOption(optionUrl);

        options.addOptionGroup(optionGroup);
    }

    /**
     * Extract the config parameters used later by the ConfigFactory
     * @return null if help is called or error happens during parsing
     */
    public ConfigFactoryParameters parse() {
        final CommandLineParser parser = new BasicParser();
        final CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                return help();

            if (cmd.hasOption("f")) {
                return file(cmd.getOptionValue("f"));
            } else if (cmd.hasOption("u")) {
                return url(cmd.getOptionValue("u"));
            } else if (0 < args.length) {
                logback.warn("Unknown parameter: " + args[0]);
                return help();
            } else {
                return resource();
            }

        } catch (final ParseException e) {
            logback.error("Failed to parse command line properties", e);
            return help();
        }
    }

    private ConfigFactoryParameters help() {
        logback.warn("Exiting program");

        // This prints out some help
        final HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("-f filepath OR -u http://fake.url", options);
        return null;
    }

    private ConfigFactoryParameters file(final String filePath) {
        final File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            logback.error("File " + filePath + " not found.");
            return null;
        }
        logback.info("Using configuration file given as parameter: " + filePath);
        return new ConfigFactoryParameters(filePath, EnumConfigFileType.FILE);
    }

    private ConfigFactoryParameters url(final String url) {
        logback.info("Using file retrieved from URL given as parameter: " + url);
        return new ConfigFactoryParameters(url, EnumConfigFileType.URL);
    }

    private ConfigFactoryParameters resource() {
        logback.info("No argument given to jar, taking default resource configuration file " +
                CONFIG_FILEPATH_INSIDE_JAR);
        return new ConfigFactoryParameters(CONFIG_FILEPATH_INSIDE_JAR, EnumConfigFileType.RESOURCE);
    }
}
