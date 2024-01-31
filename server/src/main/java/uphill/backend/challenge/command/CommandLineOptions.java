package uphill.backend.challenge.command;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uphill.backend.challenge.Server;

public class CommandLineOptions {

    private static final Logger LOGGER = Logger.getLogger(Server.class);
    private boolean debug;

    public CommandLineOptions(String[] args) {
        Options options = new Options();
        options.addOption("d", "debug", false, "Enable debug mode");

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            debug = cmd.hasOption("debug");
        } catch (ParseException e) {
            LOGGER.error("An error occurred while parsing debug command line option. Error: " + e.getMessage());
        }
    }

    public boolean isDebug() {
        return debug;
    }
}
