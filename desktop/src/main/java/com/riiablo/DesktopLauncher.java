package com.riiablo;

import android.support.annotation.NonNull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.cvar.Cvar;
import com.riiablo.cvar.CvarStateAdapter;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.util.InstallationFinder;

public class DesktopLauncher {
  private static final Logger log = LogManager.getLogger(DesktopLauncher.class);

  public static void main(String[] args) {
    Options options = new Options()
        .addOption(Option
            .builder("h")
            .longOpt("help")
            .desc("prints this message")
            .build())
        .addOption(Option
            .builder("v")
            .longOpt("viewport")
            .desc("viewport size (default 854x480)")
            .hasArg()
            .argName("size")
            .build())
        .addOption(Option
            .builder("w")
            .longOpt("windowed")
            .desc("forces windowed mode")
            .build())
        .addOption(Option
            .builder("f")
            .longOpt("fps")
            .desc("force enables fps counter")
            .build())
        .addOption(Option
            .builder("l")
            .longOpt("log-level")
            .desc("log verbosity for debugging purposes")
            .hasArg()
            .argName("level")
            .build())
        .addOption(Option
            .builder("g") // for graphics
            .longOpt("allow-software-mode")
            .desc("allows software OpenGL rendering if hardware acceleration is not available")
            .build())
        .addOption(Option
            .builder("d")
            .longOpt("d2")
            .desc("directory containing D2 MPQ files")
            .hasArg()
            .argName("path")
            .build())
        .addOption(Option
            .builder("s")
            .longOpt("saves")
            .desc("directory containing D2 character save files (*.d2s)")
            .hasArg()
            .argName("path")
            .build())
        ;

    CommandLine cmd = null;
    try {
      CommandLineParser parser = new DefaultParser();
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      log.error(e.getMessage(), e);
      System.err.println(e.getMessage());
      System.out.println("For usage, use -help option");
    } finally {
      if (cmd != null) {
        if (cmd.hasOption("help")) {
          log.debug("--help");
          HelpFormatter formatter = new HelpFormatter();
          formatter.printHelp("riiablo", options);
          System.exit(0);
          return;
        }
      }
    }

    // TODO: fix requiring bootstrapping this logger here
    //       loggers don't load contexts until client init (at the end of #main())
    LogManager.setLevel(DesktopLauncher.class.getName(), Level.DEBUG);

    final Level logLevel;
    if (cmd != null && cmd.hasOption("log-level")) {
      String optionValue = cmd.getOptionValue("log-level");
      log.debug("--log-level={}", optionValue);
      logLevel = Level.valueOf(optionValue, Level.WARN);
    } else {
      logLevel = Level.WARN;
    }
    log.debug("logLevel: {}", logLevel);
    LogManager.setLevel(DesktopLauncher.class.getName(), logLevel);

    final InstallationFinder finder = InstallationFinder.getInstance();

    final FileHandle d2Home;
    if (cmd != null && cmd.hasOption("d2")) {
      String optionValue = cmd.getOptionValue("d2");
      log.debug("--d2={}", optionValue);
      d2Home = new FileHandle(optionValue);
      if (!InstallationFinder.isD2Home(d2Home)) {
        throw new GdxRuntimeException("'d2' does not refer to a valid D2 installation: " + d2Home);
      }
    } else {
      log.trace("Locating D2 installations...");
      Array<FileHandle> homeDirs = finder.getHomeDirs();
      log.trace("D2 installations: {}", homeDirs);
      if (homeDirs.size > 0) {
        d2Home = homeDirs.first();
      } else {
        d2Home = new FileHandle(SystemUtils.USER_HOME).child("riiablo");
        d2Home.mkdirs();
      }
    }
    log.debug("d2Home: {}", d2Home);

    final FileHandle d2Saves;
    if (cmd != null && cmd.hasOption("saves")) {
      String optionValue = cmd.getOptionValue("saves");
      log.debug("--saves={}", optionValue);
      d2Saves = new FileHandle(optionValue);
      if (!InstallationFinder.containsSaves(d2Saves)) {
        log.warn("'saves' does not contain any save files: " + d2Saves);
      }
    } else {
      log.trace("Locating D2 saves...");
      Array<FileHandle> saveDirs = finder.getSaveDirs(d2Home);
      log.trace("D2 saves: {}", saveDirs);
      d2Saves = saveDirs.first();
    }
    log.debug("d2Saves: {}", d2Saves);

    final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Riiablo";
    config.addIcon("ic_launcher_128.png", Files.FileType.Internal);
    config.addIcon("ic_launcher_32.png",  Files.FileType.Internal);
    config.addIcon("ic_launcher_16.png",  Files.FileType.Internal);
    config.resizable = false;
    config.allowSoftwareMode = cmd != null && cmd.hasOption("allow-software-mode");

    int width = 854, height = 480;
    if (cmd != null && cmd.hasOption("viewport")) {
      String optionValue = cmd.getOptionValue("viewport");
      log.debug("--viewport={}", optionValue);
      String[] optionValues = StringUtils.split(optionValue, 'x');
      if (optionValues.length != 2) {
        System.err.println("'viewport' should be formatted like 854x480");
        System.exit(0);
        return;
      }

      width = NumberUtils.toInt(optionValues[0], width);
      height = NumberUtils.toInt(optionValues[1], height);
    }
    log.debug("viewport: {}x{}", width, height);

    config.width = width;
    config.height = height;
    final Client client = new Client(d2Home, d2Saves, height);
    if (cmd != null) {
      client.setWindowedForced(cmd.hasOption("windowed"));
      client.setDrawFPSForced(cmd.hasOption("fps"));
    }

    new LwjglApplication(client, config);
    if (cmd != null) {
      final int gdxLogLevel;
      switch (logLevel) {
        case DEBUG:
          gdxLogLevel = Application.LOG_DEBUG;
          break;
        case INFO:
        case WARN:
          gdxLogLevel = Application.LOG_INFO;
          break;
        case ERROR:
        case FATAL:
          gdxLogLevel = Application.LOG_ERROR;
          break;
        case OFF:
        case TRACE:
        default:
          gdxLogLevel = Application.LOG_NONE;
      }

      Gdx.app.setLogLevel(gdxLogLevel);
    }

    Cvars.Client.Windowed.addStateListener(new CvarStateAdapter<Boolean>() {
      @Override
      public void onChanged(@NonNull Cvar<Boolean> cvar, Boolean from, Boolean to) {
        if (!client.isWindowedForced()) {
          if (to && Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode((int) (Riiablo.DESKTOP_VIEWPORT_HEIGHT * 16f / 9f), Riiablo.DESKTOP_VIEWPORT_HEIGHT);
          } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
          }
        }
      }
    });

    Cvars.Client.Display.BackgroundFPSLimit.addStateListener(new CvarStateAdapter<Short>() {
      @Override
      public void onChanged(Cvar<Short> cvar, Short from, Short to) {
        config.backgroundFPS = to;
      }
    });

    Cvars.Client.Display.ForegroundFPSLimit.addStateListener(new CvarStateAdapter<Short>() {
      @Override
      public void onChanged(Cvar<Short> cvar, Short from, Short to) {
        config.foregroundFPS = to;
      }
    });
  }
}
