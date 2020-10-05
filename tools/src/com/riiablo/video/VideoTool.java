package com.riiablo.video;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;

public class VideoTool extends ApplicationAdapter {
  private static final String TAG = "VideoTool";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = TAG;
    config.resizable = false;
    config.width = 640;
    config.height = 480;
    config.vSyncEnabled = false;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new VideoTool(), config);
  }

  VideoTool() {}

  VideoPlayer player;

  @Override
  public void create() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    FileHandle handle = Gdx.files.internal("test\\New_Bliz640x480.bik");
    ByteBuf buffer = Unpooled.wrappedBuffer(handle.readBytes());
    BIK bik = BIK.loadFromByteBuf(buffer);

    player = new VideoPlayer();
    player.play(bik);
  }

  @Override
  public void resize(int width, int height) {
    player.resize(width, height);
  }

  @Override
  public void render() {
    System.out.println("update");
    player.update(Gdx.graphics.getDeltaTime());
  }

  @Override
  public void dispose() {
    player.dispose();
  }
}
