package com.riiablo.tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.ApplicationListener;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class Tool implements ApplicationListener {
  private static final Logger log = LogManager.getLogger(Tool.class);

  protected void createCliOptions(Options options) {
    options.addOption(Option.builder("h")
        .longOpt("help")
        .desc("prints this message")
        .build());
  }

  protected CommandLine parseCliOptions(Options options, String[] args) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    return parser.parse(options, args);
  }

  protected void handleCliError(String cmd, Options options, Throwable t) {
    if (t instanceof ParseException) {
      log.debug(t.getMessage(), t);
    } else {
      log.fatal(t.getMessage(), t);
    }

    System.out.println(ExceptionUtils.getRootCauseMessage(t));
    printHelp(cmd, options);
    System.exit(0);
  }

  protected void handleCliOptions(String cmd, Options options, CommandLine cli) throws Exception {
    if (cli.hasOption("help")) {
      printHelp(cmd, options);
      System.exit(0);
    }
  }

  protected String getHelpHeader() {
    return null;
  }

  protected String getHelpFooter() {
    return null;
  }

  protected void printHelp(String cmd, Options options) {
    String header = getHelpHeader();
    if (header != null) {
      header = header.replace("{cmd}", cmd).trim();
      header = String.format("%n%s%n%n", header);
    }

    String footer = getHelpFooter();
    if (footer != null) {
      footer = footer.replace("{cmd}", cmd).trim();
    }

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(cmd, header, options, footer, true);
  }

  @Override
  public void create() {}

  @Override
  public void resize(int width, int height) {}

  @Override
  public void render() {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void dispose() {}
}
