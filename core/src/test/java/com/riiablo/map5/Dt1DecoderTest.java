package com.riiablo.map5;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mockito.Mockito;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import com.riiablo.RiiabloTest;
import com.riiablo.graphics.PaletteIndexedPixmap;
import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteInputStream;
import com.riiablo.io.InvalidFormat;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

class Dt1DecoderTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    Gdx.gl = Mockito.mock();
    Dt1.MISSING_TEXTURE = new Texture(0, 0, PaletteIndexedPixmap.INDEXED);
    LogManager.setLevel("com.riiablo.map5", Level.TRACE);
  }

  private Dt1 testDt1(FileHandle handle) {
    try (InputStream in = handle.read()) {
      return Dt1Decoder.decode(handle, in);
    } catch (IOException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\TOWN\\floor.dt1")
  public void floor() {
    Dt1 dt1 = testDt1(Gdx.files.internal("test/floor.dt1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\TOWN\\floor.dt1 - blocks")
  public void floor_blocks() {
    FileHandle handle = Gdx.files.internal("test/floor.dt1");
    try (InputStream ins = handle.read()) {
      ByteInputStream in = ByteInputStream.wrap(ins);
      Dt1 dt1 = Dt1Decoder.decode(handle, in);
      ByteBuf buffer = Unpooled.wrappedBuffer(handle.readBytes());
      for (int i = 0, s = dt1.numTiles; i < s; i++) {
        ByteBuf slice = buffer.slice(dt1.blocksOffset(i), dt1.blocksLength(i));
        Dt1Decoder.decodeTile(dt1, ByteInput.wrap(slice), i);
      }
    } catch (IOException t) {
      ExceptionUtils.rethrow(t);
    }
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\TOWN\\towerb.dt1 - specials")
  public void special_blocks() {
    FileHandle handle = Gdx.files.internal("test/towerb.dt1");
    try (InputStream ins = handle.read()) {
      ByteInputStream in = ByteInputStream.wrap(ins);
      Dt1 dt1 = Dt1Decoder.decode(handle, in);
      ByteBuf buffer = Unpooled.wrappedBuffer(handle.readBytes());
      for (int i = 0, s = dt1.numTiles; i < s; i++) {
        ByteBuf slice = buffer.slice(dt1.blocksOffset(i), dt1.blocksLength(i));
        Dt1Decoder.decodeTile(dt1, ByteInput.wrap(slice), i);
      }
    } catch (IOException t) {
      ExceptionUtils.rethrow(t);
    }
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\TOWN\\fence.dt1 - blocks")
  public void fence_blocks() {
    FileHandle handle = Gdx.files.internal("test/fence.dt1");
    try (InputStream ins = handle.read()) {
      ByteInputStream in = ByteInputStream.wrap(ins);
      Dt1 dt1 = Dt1Decoder.decode(handle, in);
      ByteBuf buffer = Unpooled.wrappedBuffer(handle.readBytes());
      for (int i = 0, s = dt1.numTiles; i < s; i++) {
        ByteBuf slice = buffer.slice(dt1.blocksOffset(i), dt1.blocksLength(i));
        Dt1Decoder.decodeTile(dt1, ByteInput.wrap(slice), i);
      }
    } catch (IOException t) {
      ExceptionUtils.rethrow(t);
    }
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\OUTDOORS\\Outdoor1.dt1")
  public void Outdoor1() {
    assertThrows(InvalidFormat.class, () -> {
      Dt1 dt1 = testDt1(Gdx.files.internal("test/Outdoor1.dt1"));
    });
  }

  @Test
  @DisplayName("data\\global\\tiles\\expansion\\Town\\shrine.dt1")
  public void shrine() {
    Dt1 dt1 = testDt1(Gdx.files.internal("test/shrine.dt1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\expansion\\Town\\trees.dt1")
  public void trees() {
    Dt1 dt1 = testDt1(Gdx.files.internal("test/trees.dt1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT4\\Mesa\\inv_wall.dt1")
  public void inv_wall() {
    Dt1 dt1 = testDt1(Gdx.files.internal("test/inv_wall.dt1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT4\\Mesa\\inv_wall.dt1")
  public void inv_wall_blocks() {
    FileHandle handle = Gdx.files.internal("test/inv_wall.dt1");
    try (InputStream ins = handle.read()) {
      ByteInputStream in = ByteInputStream.wrap(ins);
      Dt1 dt1 = Dt1Decoder.decode(handle, in);
      ByteBuf buffer = Unpooled.wrappedBuffer(handle.readBytes());
      for (int i = 0, s = dt1.numTiles; i < s; i++) {
        ByteBuf slice = buffer.slice(dt1.blocksOffset(i), dt1.blocksLength(i));
        Dt1Decoder.decodeTile(dt1, ByteInput.wrap(slice), i);
      }
    } catch (IOException t) {
      ExceptionUtils.rethrow(t);
    }
  }
}
