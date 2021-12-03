package com.riiablo.file;

import io.netty.util.internal.PlatformDependent;
import java.util.Arrays;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.BufferUtils;

import com.riiablo.codec.util.BBox;
import com.riiablo.file.Dc6.Dc6Direction;
import com.riiablo.file.Dc6.Dc6Frame;
import com.riiablo.graphics.PaletteIndexedPixmap;
import com.riiablo.io.ByteInput;

public class Dc6Decoder {
  static final boolean DEBUG = !true;
  static final int MAX_WIDTH = Dc6.PAGE_SIZE;
  static final int MAX_HEIGHT = Dc6.PAGE_SIZE;

  final byte[] bmp = PlatformDependent.allocateUninitializedArray(MAX_WIDTH * MAX_HEIGHT); // 256x256 px

  public void decode(Dc6 dc6, int d) {
    decode(dc6, d, dc6.directions[d]);
  }

  void decode(Dc6 dc6, int d, Dc6Direction dir) {
    decodeFrames(dir, dc6.numFrames);
  }

  void decodeFrames(Dc6Direction dir, int numFrames) {
    final Dc6Frame[] frames = dir.frames;
    for (int f = 0, s = numFrames; f < s; f++) {
      decodeFrame(dir, frames[f], f);

      final BBox box = frames[f].box;
      final Pixmap[] pixmap = dir.pixmap;
      final Pixmap p = pixmap[f] = new PaletteIndexedPixmap(box.width, box.height);
      BufferUtils.copy(bmp, 0, p.getPixels().rewind(), box.width * box.height);
    }
  }

  void decodeFrame(Dc6Direction dir, Dc6Frame frame, int f) {
    final ByteInput in = frame.in;

    final int width = frame.width;
    final int height = frame.height;
    int x = 0;
    int y = height - 1;

    int rawIndex = 0;
    for (final int s = frame.length; rawIndex < s;) {
      int chunkSize = in.read8u();
      rawIndex++;
      if (chunkSize == 0x80) {
        // eol
        Arrays.fill(bmp, y * width + x, (y + 1) * width, (byte) 0);
        x = 0;
        y--;
      } else if ((chunkSize & 0x80) != 0) {
        // number of transparent pixels
        final int length = (chunkSize & 0x7f);
        Arrays.fill(bmp, y * width + x, y * width + x + length, (byte) 0);
        x += length;
      } else {
        // number of colors to read
        assert chunkSize + x <= width : "chunkSize(" + chunkSize + ") + x(" + x + ") > width(" + width + ")";
        in.readBytes(bmp, y * width + x, chunkSize);
        rawIndex += chunkSize;
        x += chunkSize;
      }
    }

    assert rawIndex == frame.length : "rawIndex(" + rawIndex + ") != frame.length(" + frame.length + ")";
  }
}
