package com.riiablo;

import android.support.annotation.NonNull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
import com.riiablo.util.InstallationFinder;

public class DesktopLauncher {
  public static void main(String[] args) {
    Options options = new Options()
        .addOption("help", false,
            "prints this message")
        .addOption("i", true,
            "width")
        .addOption("o", true,
            "height")
        .addOption("w", "windowed", false,
            "forces windowed mode")
        .addOption("fps", "drawFps", false,
            "force draws an FPS counter")
        .addOption("logLevel", true,
            "log verbosity for debugging purposes")
        .addOption("allowSoftwareMode", false,
            "allows software OpenGL rendering if hardware acceleration is not available")
        .addOption("home", true,
            "directory containing D2 MPQ files (defaults to user home directory)")
        .addOption("saves", true,
            "directory containing D2 Character save files (defaults to D2 home directory)")
        ;

    CommandLine cmd = null;
    try {
      CommandLineParser parser = new DefaultParser();
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      System.out.println("For usage, use -help option");
    } finally {
      if (cmd != null) {
        if (cmd.hasOption("help")) {
          HelpFormatter formatter = new HelpFormatter();
          formatter.printHelp("riiablo", options);
          System.exit(0);
        }
      }
    }

    final InstallationFinder finder = InstallationFinder.getInstance();
    final FileHandle home;
    if (cmd != null && cmd.hasOption("home")) {
      home = new FileHandle(cmd.getOptionValue("home"));
      if (!InstallationFinder.isD2Home(home)) {
        throw new GdxRuntimeException("home does not refer to a valid D2 installation");
      }
    } else {
      final Array<FileHandle> homeDirs = finder.getHomeDirs();
      if (homeDirs.size > 0) {
        home = homeDirs.first();
      } else {
        home = new FileHandle(SystemUtils.USER_HOME).child("riiablo");
        home.mkdirs();
      }
    }

    final FileHandle saves;
    if (cmd != null && cmd.hasOption("saves")) {
      saves = new FileHandle(cmd.getOptionValue("saves"));
    } else {
      final Array<FileHandle> saveDirs = finder.getSaveDirs(home);
      saves = saveDirs.first();
    }

    final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Riiablo";
    config.addIcon("ic_launcher_128.png", Files.FileType.Internal);
    config.addIcon("ic_launcher_32.png",  Files.FileType.Internal);
    config.addIcon("ic_launcher_16.png",  Files.FileType.Internal);
    config.resizable = false;
    //config.width  = 1280;//853;
    //config.height = 720;//480;
    config.allowSoftwareMode = cmd != null && cmd.hasOption("allowSoftwareMode");

    int width  = NumberUtils.toInt(cmd.getOptionValue('i', "854"));
    int height = NumberUtils.toInt(cmd.getOptionValue('o', "480"));
    config.width = width;
    config.height = height;
    final Client client = new Client(home, saves, height);
    if (cmd != null) {
      client.setWindowedForced(cmd.hasOption("w"));
      client.setDrawFPSForced(cmd.hasOption("fps"));
    }

    new LwjglApplication(client, config);
    if (cmd != null) {
      String logLevel = cmd.getOptionValue("logLevel", "info");
      if (logLevel.equalsIgnoreCase("none")) {
        Gdx.app.setLogLevel(Application.LOG_NONE);
      } else if (logLevel.equalsIgnoreCase("debug")) {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
      } else if (logLevel.equalsIgnoreCase("info")) {
        Gdx.app.setLogLevel(Application.LOG_INFO);
      } else if (logLevel.equalsIgnoreCase("error")) {
        Gdx.app.setLogLevel(Application.LOG_ERROR);
      }
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
