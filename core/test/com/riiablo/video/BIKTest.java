package com.riiablo.video;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;

public class BIKTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    com.riiablo.logger.LogManager.setLevel("com.riiablo.video", Level.TRACE);
  }

  @Test
  public void load_bik240() {
    FileHandle handle = Gdx.files.internal("test\\New_Bliz640x240.bik");
    ByteBuf buffer = Unpooled.wrappedBuffer(handle.readBytes());
    BIK.loadFromByteBuf(buffer);

  }

  @Test
  public void load_bik480() {
    FileHandle handle = Gdx.files.internal("test\\New_Bliz640x480.bik");
    ByteBuf buffer = Unpooled.wrappedBuffer(handle.readBytes());
    BIK.loadFromByteBuf(buffer);
  }

  @Test
  public void read_bik480() {
    FileHandle handle = Gdx.files.internal("test\\New_Bliz640x480.bik");
    ByteBuf buffer = Unpooled.wrappedBuffer(handle.readBytes());
    BIK bik = BIK.loadFromByteBuf(buffer);
    bik.decode(0);

    AudioDevice audio = Gdx.audio.newAudioDevice(44100, false);
    // TODO: create video tool
    // TODO: test audio playback
    // TODO: test injecting bik audio into AudioDevice stream
    // TODO: test AudioDevice android support
  }
}