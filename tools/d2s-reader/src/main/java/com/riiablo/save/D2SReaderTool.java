package com.riiablo.save;

import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.Files;
import com.riiablo.Riiablo;
import com.riiablo.attributes.StatListReader;
import com.riiablo.codec.StringTBLs;
import com.riiablo.io.ByteInput;
import com.riiablo.item.ItemReader;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.mpq.MPQFileHandleResolver;
import com.riiablo.tool.HeadlessTool;
import com.riiablo.tool.Tool;
import com.riiablo.util.InstallationFinder;

public class D2SReaderTool extends Tool {
  private static final Logger log = LogManager.getLogger(D2SReaderTool.class);

  public static void main(String[] args) {
    LogManager.setLevel(D2SReaderTool.class.getCanonicalName(), Level.INFO);
    HeadlessTool.create(D2SReaderTool.class, "d2s-reader", args).start();
  }

  @Override
  protected String getHelpHeader() {
    return "Deserializes and debugs D2S files.\n" +
        "E.g., {cmd} character1.d2s character2";
  }

  @Override
  protected void createCliOptions(Options options) {
    super.createCliOptions(options);

    options.addOption(Option
        .builder("d")
        .longOpt("d2")
        .desc("directory containing D2 MPQ files")
        .hasArg()
        .argName("path")
        .build());

    options.addOption(Option
        .builder("s")
        .longOpt("saves")
        .desc("directory containing D2 character save files (*.d2s)")
        .hasArg()
        .argName("path")
        .build());
  }

  @Override
  protected void handleCliOptions(String cmd, Options options, CommandLine cli) {
    super.handleCliOptions(cmd, options, cli);

    final InstallationFinder finder = InstallationFinder.getInstance();

    final FileHandle d2Home;
    if (cli.hasOption("d2")) {
      d2Home = new FileHandle(cli.getOptionValue("d2"));
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
        System.err.println("Unable to locate any D2 installation!");
        printHelp(cmd, options);
        System.exit(0);
        return;
      }
    }
    log.debug("d2Home: {}", d2Home);
    Riiablo.home = d2Home;

    final Array<FileHandle> d2SaveDirs;
    if (cli.hasOption("saves")) {
      FileHandle d2Saves = new FileHandle(cli.getOptionValue("saves"));
      if (!InstallationFinder.containsSaves(d2Saves)) {
        log.warn("'saves' does not contain any save files: " + d2Saves);
      }

      d2SaveDirs = new Array<>();
    } else {
      d2SaveDirs = finder.getSaveDirs(d2Home);
    }
    log.debug("d2SaveDirs: {}", d2SaveDirs);

    String[] d2sFilePaths = cli.getArgs();
    log.debug("d2sFilePaths: {}", Arrays.toString(d2sFilePaths));
    d2sFiles = new Array<>(d2sFilePaths.length);
    if (d2sFilePaths.length == 0) {
      System.err.println("No d2s files were provided!");
      printHelp(cmd, options);
      System.exit(0);
    } else {
      FileHandle handle;
      for (int i = 0; i < d2sFilePaths.length; i++) {
        String d2sFile = StringUtils.appendIfMissingIgnoreCase(
            d2sFilePaths[i],
            FilenameUtils.EXTENSION_SEPARATOR_STR + D2S.EXT);
        handle = new FileHandle(d2sFile);
        if (handle.exists()) {
          d2sFiles.add(handle);
        } else {
          for (FileHandle saveDir : d2SaveDirs) {
            handle = saveDir.child(d2sFile);
            if (handle.exists()) {
              d2sFiles.add(handle);
            } else {
              System.err.println("Unable to locate " + handle);
            }
          }
        }
      }
    }
    log.debug("d2sFilePaths: {}", d2sFiles);
  }

  Array<FileHandle> d2sFiles;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    Riiablo.home = Gdx.files.absolute(Riiablo.home.path());
    Riiablo.assets = new AssetManager();
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.files = new Files(Riiablo.assets);
    Riiablo.string = new StringTBLs(Riiablo.mpqs);

    LogManager.setLevel("com.riiablo.save", Level.ALL);
    LogManager.setLevel("com.riiablo.item", Level.ALL);
    LogManager.setLevel("com.riiablo.attributes", Level.DEBUG);

    ItemReader itemReader = new ItemReader();
    StatListReader statReader = new StatListReader();
    D2SReader serializer = D2SReader.INSTANCE;
    for (FileHandle d2sFile : d2sFiles) {
      log.info("Reading {}...", d2sFile);
      ByteInput byteInput = ByteInput.wrap(d2sFile.readBytes());
      D2S d2s = serializer.readD2S(byteInput);
      serializer.readRemaining(d2s, byteInput, statReader, itemReader);
    }

    Gdx.app.exit();
  }

  @Override
  public void dispose() {
    Riiablo.assets.dispose();
  }
}
