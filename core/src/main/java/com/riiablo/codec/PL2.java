package com.riiablo.codec;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.riiablo.codec.Palette;

public class PL2 {
  private static final int COLORMAPS = 1714;
  private static final int TINTS     = 13;
  private static final int TINT_SIZE = 3 + Palette.COLORS;

  public static final int DEFAULT_INDEX = 31;

  public static final int TINT_WHITE  = COLORMAPS + 0;
  public static final int TINT_RED    = COLORMAPS + 1;
  public static final int TINT_GREEN  = COLORMAPS + 2;
  public static final int TINT_BLUE   = COLORMAPS + 3;
  public static final int TINT_GOLD   = COLORMAPS + 4;
  public static final int TINT_GREY   = COLORMAPS + 5;
  public static final int TINT_BLACK  = COLORMAPS + 6;
  public static final int TINT_7      = COLORMAPS + 7;
  public static final int TINT_ORANGE = COLORMAPS + 8;
  public static final int TINT_YELLOW = COLORMAPS + 9;
  public static final int TINT_10     = COLORMAPS + 10;
  public static final int TINT_11     = COLORMAPS + 11;
  public static final int TINT_12     = COLORMAPS + 12;

  public final byte colormaps[][];
  public final int  tints[];
  public final int  size;

  private PL2(byte[][] colormaps, int[] tints) {
    this.colormaps = colormaps;
    this.tints = tints;
    this.size = COLORMAPS + tints.length;
  }

  public byte[] getColormap(int index) {
    return colormaps[index];
  }

  public int getTint(int index) {
    return tints[index];
  }

  public Texture render() {
    Pixmap pl2Pixmap = new Pixmap(Palette.COLORS, size, Pixmap.Format.RGBA8888);
    ByteBuffer buffer = pl2Pixmap.getPixels();
    for (int i = 0, j = 0; i < size; i++) {
      //buffer.put(colormaps[i]);
      for (int k = 0; k < Palette.COLORS; k++, j += 4) {
        buffer.put(j, colormaps[i][k]);
      }
    }

    buffer.rewind();
    Texture texture = new Texture(pl2Pixmap);
    //texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    pl2Pixmap.dispose();
    return texture;
  }

  public Texture render(Palette palette) {
    int[] colors = palette.get();
    Pixmap pl2Pixmap = new Pixmap(Palette.COLORS, size, Pixmap.Format.RGBA8888);
    IntBuffer buffer = pl2Pixmap.getPixels().asIntBuffer();
    for (int i = 0, j = 0; i < size; i++) {
      for (int k = 0; k < Palette.COLORS; k++) {
        buffer.put(j++, colors[colormaps[i][k] & 0xFF]);
      }
    }

    Texture texture = new Texture(pl2Pixmap);
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    pl2Pixmap.dispose();
    return texture;
  }

  public static PL2 loadFromFile(FileHandle file) {
    return loadFromStream(file.read());
  }

  public static PL2 loadFromStream(InputStream in) {
    try {
      // Skip the internal palette data which was loaded in the corresponding .dat
      IOUtils.skipFully(in, 0x400);

      // TODO: It may be faster to implement using single dimension using offsets
      byte[][] colormaps = new byte[COLORMAPS + TINTS][Palette.COLORS];
      for (int i = 0; i < COLORMAPS; i++) {
        IOUtils.readFully(in, colormaps[i]);
      }

      int available = in.available();
      if (available % TINT_SIZE > 0) {
        throw new GdxRuntimeException("Remaining bytes does not match an expected tint count.");
      }

      final int tintsUsed = available / TINT_SIZE;
      int[] tints = new int[tintsUsed];
      for (int i = 0, r, g, b; i < tintsUsed; i++) {
        r = in.read();
        g = in.read();
        b = in.read();
        tints[i] = (r << 16) | (g << 8) | b;
      }

      // NOTE: LOADING PL2 is missing a tint, so readFully will not work here :/
      for (int i = 0; i < tintsUsed; i++) {
        IOUtils.readFully(in, colormaps[COLORMAPS + i]);
      }

      return new PL2(colormaps, tints);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load PL2 from stream.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

}
