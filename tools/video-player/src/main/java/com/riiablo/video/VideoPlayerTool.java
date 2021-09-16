package com.riiablo.video;

import java.io.FileNotFoundException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

import com.riiablo.Riiablo;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.mpq.MPQFileHandleResolver;
import com.riiablo.tool.LwjglTool;
import com.riiablo.tool.Tool;
import com.riiablo.util.InstallationFinder;

public class VideoPlayerTool extends Tool {
  private static final Logger log = LogManager.getLogger(VideoPlayerTool.class);

  public static void main(String[] args) {
    LogManager.setLevel(VideoPlayerTool.class.getCanonicalName(), Level.TRACE);
    LwjglTool.create(VideoPlayerTool.class, "video-player", args)
        .title("Video Player Tool")
        .size(640, 480, false) // default video size
        .start();
  }

  FileHandle home;
  String file;

  float canvasX, canvasY;
  float canvasWidth, canvasHeight;

  SpriteBatch batch;
  VideoPlayer player;

  @Override
  protected String getHelpHeader() {
    return "Plays a specified D2 video file.\n" +
        "E.g., {cmd} --file data/local/video/BlizNorth640x480.bik";
  }

  @Override
  protected void createCliOptions(Options options) {
    super.createCliOptions(options);
    options.addOption(Option
        .builder("f")
        .longOpt("file")
        .desc("path to the video file to play")
        .required()
        .hasArg()
        .argName("path")
        .build());
  }

  @Override
  protected void handleCliOptions(String cmd, Options options, CommandLine cli) throws Exception {
    super.handleCliOptions(cmd, options, cli);

    InstallationFinder finder = InstallationFinder.getInstance();
    home = finder.defaultHomeDir();

    String fileOptionValue = cli.getOptionValue("file");
    file = fileOptionValue;
    log.trace("file={}", file);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    Riiablo.home = home = Gdx.files.absolute(home.path());
    Riiablo.mpqs = new MPQFileHandleResolver();

    batch = new SpriteBatch();
    player = VideoPlayerCreator.createVideoPlayer();
    player.setOnCompletionListener(new VideoPlayer.CompletionListener() {
      @Override
      public void onCompletionListener(FileHandle file) {
        log.info("finished playing " + file.name());
      }
    });
    player.setOnVideoSizeListener(new VideoPlayer.VideoSizeListener() {
      @Override
      public void onVideoSize(float width, float height) {
        log.info("size " + width + "x" + height);
        canvasWidth = width;
        canvasHeight = height <= 240 ? height * 2 : height;
        canvasY = Gdx.graphics.getHeight() / 2 - canvasHeight / 2;
      }
    });
    Gdx.app.postRunnable(new Runnable() {
      @Override
      public void run() {
        try {
          // FileHandle handle = Riiablo.mpqs.resolve(file);
          // if (handle == null) throw new FileNotFoundException(file);
          FileHandle handle = Gdx.files.absolute(file);
          player.play(handle);
          player.setVolume(0.1f);
          log.info("playing " + handle);
        } catch (FileNotFoundException t) {
          log.error(ExceptionUtils.getRootCauseMessage(t), t);
        }
      }
    });
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    player.update();
    batch.begin();
    Texture frame = player.getTexture();
    if (frame != null) {
      batch.draw(frame, canvasX, canvasY, canvasWidth, canvasHeight);
    }
    batch.end();
  }

  @Override
  public void pause() {
    player.pause();
  }

  @Override
  public void resume() {
    player.resume();
  }

  @Override
  public void dispose() {
    batch.dispose();
    player.dispose();
  }
}
