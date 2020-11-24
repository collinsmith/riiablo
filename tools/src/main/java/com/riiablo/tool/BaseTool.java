package com.riiablo.tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.badlogic.gdx.ApplicationListener;

public class BaseTool implements ApplicationListener {
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
    System.err.println(t.getMessage());
    printHelp(cmd, options);
    System.exit(0);
  }

  protected void handleCliOptions(String cmd, Options options, CommandLine cli) {
    if (cli.hasOption("help")) {
      printHelp(cmd, options);
      System.exit(0);
    }
  }

  private void printHelp(String cmd, Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(cmd, options, true);
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
