package com.riiablo.graphics;

import com.badlogic.gdx.graphics.Pixmap;

public class PaletteIndexedPixmap extends Pixmap {
  public PaletteIndexedPixmap(int width, int height) {
    super(width, height, Format.Intensity);
    setBlending(Blending.None);
    setFilter(Filter.NearestNeighbour);
  }

  public PaletteIndexedPixmap(int width, int height, byte[] data) {
    this(width, height);
    getPixels().put(data).rewind();
  }
}
