package org.molgenis.vcf.annotate.gene;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.annotate.gene.AppCommandLineOptions.OPT_CONFIG;
import static org.molgenis.vcf.annotate.gene.AppCommandLineOptions.OPT_DEBUG;
import static org.molgenis.vcf.annotate.gene.AppCommandLineOptions.OPT_DEBUG_LONG;
import static org.molgenis.vcf.annotate.gene.AppCommandLineOptions.OPT_INPUT;
import static org.molgenis.vcf.annotate.gene.AppCommandLineOptions.OPT_OUTPUT;

import ch.qos.logback.classic.Level;
import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppCommandLineRunner implements CommandLineRunner {
  private static final Logger logger = LoggerFactory.getLogger(AppCommandLineRunner.class);

  private static final int STATUS_COMMAND_LINE_USAGE_ERROR = 64;

  private final String appName;
  private final String appVersion;

  AppCommandLineRunner(
      @Value("${app.name}") String appName, @Value("${app.version}") String appVersion) {
    this.appName = requireNonNull(appName);
    this.appVersion = requireNonNull(appVersion);
  }

  @Override
  public void run(String... args) {
    CommandLineParser commandLineParser = new DefaultParser();
    CommandLine commandLine = null;

    if (args.length == 1
        && (args[0].equals("-" + AppCommandLineOptions.OPT_VERSION)
            || args[0].equals("--" + AppCommandLineOptions.OPT_VERSION_LONG))) {
      logger.info("{} {}", appName, appVersion);
      return;
    }

    for (String arg : args) {
      if (arg.equals(OPT_DEBUG) || arg.equals(OPT_DEBUG_LONG)) {
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (!(rootLogger instanceof ch.qos.logback.classic.Logger)) {
          throw new ClassCastException("Expected root logger to be a logback logger");
        }
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.DEBUG);
        break;
      }
    }

    try {
      commandLine = commandLineParser.parse(AppCommandLineOptions.getAppOptions(), args);
      AppCommandLineOptions.validateCommandLine(commandLine);
    } catch (ParseException e) {
      logException(e);
      System.exit(STATUS_COMMAND_LINE_USAGE_ERROR);
    }
    String inputPath = commandLine.getOptionValue(OPT_INPUT);
    File inputFile = new File(inputPath);
    File configFile = new File(commandLine.getOptionValue(OPT_CONFIG));
    File outputFile;
    if (commandLine.hasOption(OPT_OUTPUT)) {
      outputFile = new File(commandLine.getOptionValue(OPT_OUTPUT));
    } else {
      outputFile = new File(inputPath.replace(".vcf", ".out.vcf"));
    }
    new GeneAnnotator().run(inputFile, outputFile, configFile);
  }

  @SuppressWarnings("java:S106")
  private void logException(ParseException e) {
    logger.error(e.getLocalizedMessage());

    // following information is only logged to system out
    System.out.println();
    HelpFormatter formatter = new HelpFormatter();
    formatter.setOptionComparator(null);
    String cmdLineSyntax = "java -jar " + appName + ".jar";
    formatter.printHelp(cmdLineSyntax, AppCommandLineOptions.getAppOptions(), true);
    System.out.println();
    formatter.printHelp(cmdLineSyntax, AppCommandLineOptions.getAppVersionOptions(), true);
  }
}
