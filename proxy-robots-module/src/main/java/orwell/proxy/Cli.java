package orwell.proxy;

import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigCli;
import orwell.proxy.config.EnumConfigFileType;

import java.io.File;

/**
 * Created by miludmann on 5/5/15.
 */
public class Cli {
    final static protected String DEFAULT_CONFIG_FILEPATH = "/configuration.xml";
    final static org.slf4j.Logger logback = LoggerFactory.getLogger(Cli.class);
    private String[] args = null;
    private Options options = new Options();

    public Cli(String[] args) {
        this.args = args;

        options.addOption("h", "help", false, "shows help.");
        options.addOption("f", "file", true, "optional filepath for external configuration file");
        options.addOption("u", "url", true, "optional url for external configuration file, NOT HANDLED YET");
    }

    public ConfigCli parse() {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                help();

            if (cmd.hasOption("f")) {
                return file(cmd.getOptionValue("f"));
            } else if (cmd.hasOption("u")) {
                return url(cmd.getOptionValue("u"));
            } else if (args.length > 0) {
                logback.warn("Unknown parameter: " + args[0]);
                help();
            } else {
                return resource();
            }

        } catch (ParseException e) {
            logback.error("Failed to parse command line properties", e);
            help();
        }
        return null;
    }

    private void help() {
        logback.warn("Exiting program");

        // This prints out some help
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("Main", options);
        System.exit(0);
    }

    private ConfigCli file(String filePath) {
        File f = new File(filePath);
        if (!f.exists() || f.isDirectory()) {
            logback.error("File " + filePath + " not found, exiting program");
            System.exit(0);
        }
        logback.info("Using configuration file given as parameter: " + filePath);
        return new ConfigCli(filePath, EnumConfigFileType.FILE);
    }

    private ConfigCli url(String url) {
        logback.info("Using file retrieved from URL given as parameter: " + url);
        return new ConfigCli(url, EnumConfigFileType.URL);
    }

    private ConfigCli resource() {
        logback.info("No argument given to jar, taking default resource configuration file " +
                DEFAULT_CONFIG_FILEPATH);
        return new ConfigCli(DEFAULT_CONFIG_FILEPATH, EnumConfigFileType.RESOURCE);
    }
}
