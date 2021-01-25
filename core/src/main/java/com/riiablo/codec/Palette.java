package com.riiablo.codec;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

public class Palette {
  private static final String TAG = "Palette";
  private static final boolean DEBUG = false;

  public static final int COLORS = 256;

  public final int colors[];

  private Palette(int[] colors) {
    this.colors = colors;
  }

  public int[] get() {
    return colors;
  }

  public static int a(int color) {
    return color & 0xFF;
  }

  public static int r(int color) {
    return color >>> 24;
  }

  public static int g(int color) {
    return (color >>> 16) & 0xFF;
  }

  public static int b(int color) {
    return (color >>> 8) & 0xFF;
  }

  public static int abgr8888(int a, int b, int g, int r) {
    return (r << 24) | (g << 16) | (b << 8) | a;
  }

  public static int r8(int r) {
    return r << 24;
  }

  public static int a8(int a) {
    return a & 0xFF;
  }

  public static int a8(byte a) {
    return a & 0xFF;
  }

  public Texture render() {
    Pixmap palettePixmap = new Pixmap(COLORS, 1, Pixmap.Format.RGBA8888);
    palettePixmap.getPixels().asIntBuffer().put(colors);
    Texture texture = new Texture(palettePixmap);
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    palettePixmap.dispose();
    return texture;
  }

  /**
   * Renders a pixmap as a sheet with each pixel taking {@code cellsize} square pixels. Used to show
   * a more user-readable representation of the palette.
   */
  public Texture render(int cellsize) {
    final int cells   = 16;
    final int size    = cells * cellsize;
    final int rows    = cells;
    final int columns = cells;

    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    for (int r = 0, i = 0, x = 0, y = 0; r < rows; r++, x = 0, y += cellsize) {
      for (int c = 0; c < columns; c++, i++, x += cellsize) {
        pixmap.setColor(colors[i] | 0xFF); // Removes alpha transparency
        pixmap.fillRectangle(x, y, cellsize, cellsize);
      }
    }

    Texture texture = new Texture(pixmap);
    texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    pixmap.dispose();
    return texture;
  }

  public static Palette loadFromStream(InputStream in) {
    try {
      byte[] data = new byte[COLORS * 3];
      IOUtils.readFully(in, data);
      return loadFromArray(data);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load palette from stream.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  public static Palette loadFromFile(FileHandle file) {
    byte[] data = file.readBytes();
    return loadFromArray(data);
  }

  private static Palette loadFromArray(byte[] data) {
    int r, g, b;
    int[] colors = new int[COLORS];
    for (int i = 0, j = 0; i < colors.length; i++) {
      b = (data[j++] & 0xFF);
      g = (data[j++] & 0xFF);
      r = (data[j++] & 0xFF);
      colors[i] = abgr8888(0xFF, b, g, r);
    }

    colors[0] &= 0xFFFFFF00;
    if (DEBUG) {
      StringBuilder builder = new StringBuilder(1024);
      for (int i = 0; i < COLORS; i++) {
        builder.append(String.format("#%08X", colors[i]));
        builder.append(", ");
      }

      builder.setLength(builder.length() - 2);
      Gdx.app.debug(TAG, "colors = [" + builder.toString() + "]");
    }

    return new Palette(colors);
  }
}
