import org.apache.commons.cli.*;

import java.io.IOException;

public class CommitCollectorCMD {

    public static void main(String... args) {
        final Options options = new Options();
        options.addOption("d", true, "repo directory");
        options.addOption("o", true, "output csv file");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if(cmd.hasOption("d") && cmd.hasOption("o")) {
                CommitCollector commitCollector = new CommitCollector(cmd.getOptionValue("d"));
                commitCollector.collect().writeToCSV(cmd.getOptionValue("o"));
                System.out.println("FINISHED!");
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("commit-collector", options);
            }
        } catch (ParseException e) {
            System.err.println("Some error occurred!");
        } catch (IOException e) {
            System.err.println("Unable to write data!");
        }
    }

}
