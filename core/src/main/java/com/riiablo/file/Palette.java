package com.riiablo.file;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.io.ByteInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class Palette implements Disposable {
  private static final Logger log = LogManager.getLogger(Palette.class);

  public static final int COLORS = 256;

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

  public static Palette read(ByteBuf buffer) {
    ByteInput in = ByteInput.wrap(buffer);

    int r, g, b;
    int[] colors = new int[COLORS];
    for (int i = 0; i < COLORS; i++) {
      b = in.read8u();
      g = in.read8u();
      r = in.read8u();
      colors[i] = abgr8888(0xFF, b, g, r);
    }

    colors[0] &= 0xFFFFFF00;
    return new Palette(colors);
  }

  final int[] colors;
  Texture texture;

  public Palette(int[] colors) {
    if (colors == null) throw new IllegalArgumentException("colors cannot be null");
    if (colors.length != COLORS) throw new IllegalArgumentException(
        "colors.length(" + colors.length + ") != COLORS(" + COLORS + ")");
    this.colors = colors;
  }

  @Override
  public void dispose() {
    log.trace("disposing palette textures");
    if (texture != null) {
      texture.dispose();
      texture = null;
    }
  }

  public int[] get() {
    return colors;
  }

  public Texture texture() {
    if (texture == null) {
      Pixmap palettePixmap = new Pixmap(COLORS, 1, Pixmap.Format.RGBA8888);
      palettePixmap.getPixels().asIntBuffer().put(colors);
      texture = new Texture(palettePixmap);
      texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
      texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
      palettePixmap.dispose();
    }

    return texture;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(4096);
    builder.append('[');
    for (int i = 0; i < COLORS; i++) {
      builder
          .append(String.format("#%08X", colors[i]))
          .append(", ")
      ;
    }

    builder.setLength(builder.length() - 2);
    builder.append(']');
    return new ToStringBuilder(this)
        .append("colors", builder)
        .toString();
  }
}
