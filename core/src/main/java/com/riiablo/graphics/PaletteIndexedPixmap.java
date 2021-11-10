package com.riiablo.graphics;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.BufferUtils;

public class PaletteIndexedPixmap extends Pixmap {
  public static final Pixmap.Format INDEXED = Format.Intensity;

  public PaletteIndexedPixmap(int width, int height) {
    super(width, height, INDEXED);
    setBlending(Blending.None);
    setFilter(Filter.NearestNeighbour);
  }

  public PaletteIndexedPixmap(int width, int height, byte[] data) {
    this(width, height);
    BufferUtils.copy(data, 0, getPixels(), data.length);
  }
}
