package com.riiablo.file;

import io.netty.util.internal.PlatformDependent;
import java.util.Arrays;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.BufferUtils;

import com.riiablo.codec.util.BBox;
import com.riiablo.file.Dcc.DccDirection;
import com.riiablo.file.Dcc.DccFrame;
import com.riiablo.graphics.PaletteIndexedPixmap;

public final class DccDecoder {
  static final boolean DEBUG = !true;
  static final int MAX_FRAMES = 256;

  final DirectionBuffer directionBuffer = new DirectionBuffer();
  final FrameBuffer[] frameBuffer = new FrameBuffer[MAX_FRAMES]; {
    for (int i = 0; i < MAX_FRAMES; i++) {
      frameBuffer[i] = new FrameBuffer();
    }
  }
  final PixelBuffer pixelBuffer = new PixelBuffer();
  final Bitmap bmp = new Bitmap();

  public void decode(Dcc dcc, int d) {
    decode(dcc, d, dcc.directions[d]);
  }

  void decode(Dcc dcc, int d, DccDirection dir) {
    directionBuffer.clear(dir);
    pixelBuffer.clear(directionBuffer.size);
    decodeFrames(dir, dcc.numFrames);

    bmp.reset(dir);
    buildFrames(dir, dcc.numFrames);

    assert dir.equalCellBitStream.bitsRemaining() == 0;
    assert dir.pixelMaskBitStream.bitsRemaining() == 0;
    assert dir.encodingTypeBitStream.bitsRemaining() == 0;
    assert dir.rawPixelCodesBitStream.bitsRemaining() == 0;
    assert dir.pixelCodeAndDisplacementBitStream.bytesRemaining() == 0 :
        String.format("dcc: %s, dir: %d, bytesRemaining: %d",
            dcc.handle, d, dir.pixelCodeAndDisplacementBitStream.bytesRemaining());
  }

  /**
   * decodes the frame's cells into {@link #pixelBuffer}
   */
  void decodeFrames(DccDirection dir, int numFrames) {
    final DccFrame[] frames = dir.frames;
    for (int f = 0, s = numFrames; f < s; f++) {
      decodeFrame(dir, frames[f], f);
    }

    // map dcc pixel code to palette index
    final short[] PALETTE = dir.pixelValues;
    final byte[] pixels = pixelBuffer.pixels;
    // for (int c = 0, s = pixelBuffer.size; c < s; c++) {
    //   final int c0 = (c << 2);
    //   System.out.println(String.format("0x%08x",
    //             (pixels[c0 + 0] & 0xff)
    //           | (pixels[c0 + 1] & 0xff) << 8
    //           | (pixels[c0 + 2] & 0xff) << 16
    //           | (pixels[c0 + 3] & 0xff) << 24
    //   ));
    // }
    for (int c = 0, s = pixelBuffer.size << 2; c < s; c++) {
      pixels[c] = (byte) PALETTE[pixels[c] & 0xFF];
    }
    // for (int c = 0, s = pixelBuffer.size; c < s; c++) {
    //   final int c0 = (c << 2);
    //   System.out.println(String.format("0x%08x",
    //             (pixels[c0 + 0] & 0xff)
    //           | (pixels[c0 + 1] & 0xff) << 8
    //           | (pixels[c0 + 2] & 0xff) << 16
    //           | (pixels[c0 + 3] & 0xff) << 24
    //   ));
    // }
  }

  void decodeFrame(DccDirection dir, DccFrame frame, int f) {
    final FrameBuffer frameBuffer = this.frameBuffer[f];
    frameBuffer.clear(directionBuffer, frame);

    final int stride = directionBuffer.cellsW;
    final int dCellW = (frame.box.xMin - dir.box.xMin) / 4;
    final int dCellH = (frame.box.yMin - dir.box.yMin) / 4;
    final int fCellsW = frameBuffer.cellsW;
    final int fCellsH = frameBuffer.cellsH;
    if (DEBUG) System.out.println(fCellsW + "," + fCellsH + ";" + dCellW + "," + dCellH);

    boolean equalCell;
    int pixelMask, numPixels;
    boolean rawEncoded;
    int lastPixel, decodedPixels, pixelDisplacement;
    final int[] pixelStack = new int[4];
    final int[] NUM_PIXELS = PixelBuffer.PIXEL_TABLE;
    for (int cy = 0, fCell = 0; cy < fCellsH; cy++) {
      final int dCy = dCellH + cy;
      int dCell = dCy * stride + dCellW;
      for (int cx = 0; cx < fCellsW; cx++, dCell++, fCell++) {
        // System.out.println("  " + (cy * fStride + cx) + " " + dCell);
        // pixel buffer has written to dCell in previous frame
        if (pixelBuffer.touched(dCell)) {
          // System.out.println("nn");
          equalCell = dir.equalCellBitStreamSize > 0 && dir.equalCellBitStream.readBoolean();
          // System.out.print(equalCell ? "1 " : "0 ");
          if (equalCell) {
            // System.out.println();
            continue;
          } else {
            pixelMask = dir.pixelMaskBitStream.read7u(4);
            // System.out.printf("0x%01x%n", pixelMask);
          }
        } else {
          pixelMask = 0xf;
        }

        numPixels = NUM_PIXELS[pixelMask];
        rawEncoded = numPixels > 0 && dir.encodingTypeBitStreamSize > 0 && dir.encodingTypeBitStream.readBoolean();
        // System.out.println("  " + pixelMask + "(" + numPixels + ")" + " " + StringUtils.leftPad(Integer.toBinaryString(pixelMask), 4, '0'));

        pixelStack[0] = 0;
        pixelStack[1] = 0;
        pixelStack[2] = 0;
        pixelStack[3] = 0;
        lastPixel = 0;
        for (decodedPixels = 0; decodedPixels < numPixels; decodedPixels++) {
          int pixel;
          if (rawEncoded) {
            pixel = dir.rawPixelCodesBitStream.read15u(8);
          } else {
            int pixelCode = lastPixel;
            do {
              pixelDisplacement = dir.pixelCodeAndDisplacementBitStream.read7u(4);
              pixelCode += pixelDisplacement;
            } while (pixelDisplacement == 0xf);
            pixel = pixelCode;
          }

          // System.out.println("    " + decodedPixels + ":" + pixel);
          // pixel = PALETTE[pixel]; // map dcc pixel code to palette index
          // break on consecutive equal pixel codes
          if (pixel == lastPixel) break;
          pixelStack[decodedPixels] = lastPixel = pixel;
        }

        // System.out.printf("  %02x %02x %02x %02x%n", pixels[0], pixels[1], pixels[2], pixels[3]);
        pixelBuffer.enqueue(dCell, f, fCell, pixelMask, pixelStack, decodedPixels);
        if (DEBUG) System.out.println("  -> "
            + dCell
            + "=" + pixelBuffer.frame[pixelBuffer.size - 1]
            + ":" + String.format("0x%08x",
                      (pixelBuffer.pixels[((pixelBuffer.size - 1) << 2) + 0] & 0xff)
                    | (pixelBuffer.pixels[((pixelBuffer.size - 1) << 2) + 1] & 0xff) << 8
                    | (pixelBuffer.pixels[((pixelBuffer.size - 1) << 2) + 2] & 0xff) << 16
                    | (pixelBuffer.pixels[((pixelBuffer.size - 1) << 2) + 3] & 0xff) << 24
            )
        );
      }
    }
  }

  /**
   * consumes {@link #pixelBuffer} and remaining pixel codes from dcc stream to
   * construct frames into {@link #bmp}
   */
  void buildFrames(DccDirection dir, int numFrames) {
    final Pixmap[] pixmap = dir.pixmap;
    for (int f = 0, s = numFrames; f < s; f++) {
      bmp.flip();
      buildFrame(dir, f);
      final Pixmap p = pixmap[f] = new PaletteIndexedPixmap(dir.box.width, dir.box.height);
      BufferUtils.copy(bmp.frontBuffer, 0, p.getPixels().rewind(), bmp.length);
    }
  }

  void buildFrame(DccDirection dir, int f) {
    final int stride = directionBuffer.cellsW;
    final FrameBuffer frameBuffer = this.frameBuffer[f];
    if (DEBUG) if (DEBUG) if (DEBUG) if (DEBUG) System.out.println("frameBuffer.size: " + frameBuffer.size);
    if (DEBUG) if (DEBUG) if (DEBUG) System.out.println("  " + dir.pixelCodeAndDisplacementBitStream.bitsRead());
    final byte[] pixels = pixelBuffer.pixels;
    for (int c = 0, s = frameBuffer.size; c < s; c++) {
      final short fCx = frameBuffer.x[c];
      final short fCy = frameBuffer.y[c];
      final byte fCw = frameBuffer.w[c];
      final byte fCh = frameBuffer.h[c];
      final int cellId = (fCy >>> 2) * stride + (fCx >>> 2);
      final short dCxLast = directionBuffer.xLast[cellId];
      final short dCyLast = directionBuffer.yLast[cellId];
      final byte dCwLast = directionBuffer.wLast[cellId];
      final byte dChLast = directionBuffer.hLast[cellId];
      if (!pixelBuffer.peek(f, c)) {
        if (DEBUG) if (DEBUG) System.out.println("copy " + c + " " + pixelBuffer.peek());
        if (fCw != dCwLast || fCh != dChLast) {
          bmp.clear(fCx, fCy, fCw, fCh);
        } else {
          bmp.copy(dCxLast, dCyLast, fCx, fCy, fCw, fCh);
        }
      } else {
        final int iter = pixelBuffer.peek();
        final int c0 = iter << 2;
        if (DEBUG) System.out.println("write " + pixelBuffer.peekFrame() + " " + c + " " + iter + " "
            + String.format("0x%08x",
                  (pixels[c0 + 0] & 0xff)
                | (pixels[c0 + 1] & 0xff) << 8
                | (pixels[c0 + 2] & 0xff) << 16
                | (pixels[c0 + 3] & 0xff) << 24
        ));
        if (pixels[c0 + 0] == pixels[c0 + 1]) {
          bmp.fill(fCx, fCy, fCw, fCh, pixels[c0 + 0]);
        } else {
          final int bits = pixels[c0 + 1] == pixels[c0 + 2] ? 1 : 2;
          for (int y = 0; y < fCh; y++) {
            for (int x = 0; x < fCw; x++) {
              final int i = dir.pixelCodeAndDisplacementBitStream.read7u(bits);
              if (DEBUG) System.out.printf("0 0x%01x%n", i);
              bmp.set(fCx, fCy, x, y, pixels[c0 + i]);
            }
          }
        }

        pixelBuffer.dequeue();
      }

      directionBuffer.touch(cellId, fCx, fCy, fCw, fCh);
    }
  }

  /**
   * Buffer used to cache cell size data across multiple frames of a decoding.
   * Direction buffer is sized to encompass all frames and is subdivided into
   * 4x4 pixel macroblocks.
   *
   * There is no requirement for the frame buffer of each frame to have width
   * and height that are multiples of four, as there is no requirement for the
   * frames to be aligned on cell boundaries. If the width of the frame buffer
   * is not a multiple of four then the last cells of each row of cells (we
   * assume left to right ordering of the cells, and top to town in vertical)
   * will have different dimensions. Analogically if the height is not a
   * multiple of four, the cells at the bottom of the frame buffer will have
   * dimensions different from 4x4. However, this is only the way the frame
   * buffer is divided. There are additional conditions that specify the cell
   * sizes for each frame.
   */
  static final class DirectionBuffer {
    /**
     * This is an old note, I think I was mistaken, below are all well within
     * cell range, e.g., 365,213 is 92,64 in cells
     *
     * DCC with dir box wxh > 65K
     * max DCC cells is supposed to be 75x75, below are more than that
     * data\global\missiles\extra\beachballexplode.dcc 0 365,213
     * data\global\monsters\gt\tr\gttrlita1hth.dcc 0 345,324
     * data\global\monsters\gt\tr\gttrlita1hth.dcc 1 345,324
     * data\global\monsters\gt\tr\gttrlita1hth.dcc 2 345,324
     * data\global\monsters\gt\tr\gttrlita1hth.dcc 3 345,324
     * data\global\monsters\gt\tr\gttrlitnuhth.dcc 0 345,324
     * data\global\monsters\gt\tr\gttrlitnuhth.dcc 1 345,324
     * data\global\monsters\gt\tr\gttrlitnuhth.dcc 2 345,324
     * data\global\monsters\gt\tr\gttrlitnuhth.dcc 3 345,324
     * data\global\monsters\os\lh\oslhlita2hth.dcc 0 356,244
     * data\global\monsters\os\lh\oslhlita2hth.dcc 3 343,203
     * data\global\monsters\os\lh\oslhlita2hth.dcc 5 445,178
     */
    static final int MAX_SIZE = 0x10000;
    static final byte CELL_SIZE = 4; // 4x4

    DccDirection direction;
    int cellsW;
    int cellsH;
    int size; // cellsW * cellsH

    final byte[] w = PlatformDependent.allocateUninitializedArray(MAX_SIZE);
    final byte[] h = PlatformDependent.allocateUninitializedArray(MAX_SIZE);
    final short[] xLast = new short[MAX_SIZE];
    final short[] yLast = new short[MAX_SIZE];
    final byte[] wLast = PlatformDependent.allocateUninitializedArray(MAX_SIZE);
    final byte[] hLast = PlatformDependent.allocateUninitializedArray(MAX_SIZE);

    void set(int i, byte w, byte h) {
      this.w[i] = w;
      this.h[i] = h;
      xLast[i] = yLast[i] = -1;
      wLast[i] = hLast[i] = 0;
    }

    void touch(int i, short fCx, short fCy, byte fCw, byte fCh) {
      xLast[i] = fCx;
      yLast[i] = fCy;
      wLast[i] = fCw;
      hLast[i] = fCh;
    }

    /**
     * Clears all cells in this frame buffer and sets their sizes, ranging from
     * {@code [1..4]}
     */
    void clear(DccDirection direction) {
      this.direction = direction;
      BBox d = direction.box;
      clear(d.width, d.height);
    }

    /** @deprecated for use in tests, use {@link #clear(DccDirection)} instead */
    @Deprecated
    void clear(int width, int height) {
      assert CELL_SIZE == 4 : "CELL_SIZE(" + CELL_SIZE + ") != " + 4;

      cellsW = (width + 3) / 4;
      cellsH = (height + 3) / 4;
      size = cellsW * cellsH;

      final int defCellsW = width >>> 2; // 4's
      final byte remCellsW = (byte) (width & 0x3); // <4
      final int defCellsH = height >>> 2; // 4's
      final byte remCellsH = (byte) (height & 0x3); // <4

      int i = 0, y = 0, x = 0;
      for (int cy = 0; cy < defCellsH; cy++, y += CELL_SIZE) {
        i = resetCells(defCellsW, remCellsW, i, x, CELL_SIZE);
      }
      if (remCellsH <= 0) return;
      i = resetCells(defCellsW, remCellsW, i, x, remCellsH);
    }

    private int resetCells(int defCellsW, byte remCellsW, int i, int x, byte cellSize) {
      for (int cx = 0; cx < defCellsW; cx++, x += CELL_SIZE) {
        set(i++, CELL_SIZE, cellSize);
      }
      if (remCellsW > 0) {
        set(i++, remCellsW, cellSize);
        x += CELL_SIZE;
      }
      return i;
    }
  }

  /**
   * Buffer used to cache cell size data for a single frame backed by a
   * {@link DirectionBuffer} and subdivided into 1x1-5x5 macroblocks, but
   * typically the default division is 4x4.
   *
   * If there are no pixels from the cell, which results from the default
   * division of the frame buffer, that are outside the frame the cell has
   * the default size of 4x4. If there are pixels from that cell that are
   * outside the frame the corresponding cell from that frame has such
   * dimensions that it contains pixels that are within the frame only.
   * However, there is one exception: if the last column or row of cells,
   * produced by the default frame buffer division that have common pixels with
   * the frame, have width of one or height of one respectively - the cells that
   * are next to them and within the frame at the same time have width of 5 or
   * height of 5 respectively. In fewer words this means the last cell in the
   * row of the frame can't have width of one - in that case the cell left of it
   * has width of 5. Analogically the cells at the bottom of the frame can't
   * have height of one - instead the cells up of them have height of 5.
   * However, the width of a cell can't be greater than the width of the frame.
   * The same applies for the height of a cell - must be smaller or equal to the
   * height of the frame. So if we have a frame that have dimensions of 2x3, and
   * it is placed in the frame buffer such that its pixel in the bottom-right
   * corner of the frame is a pixel in the top-left corner of a cell - that
   * frame will have only one cell with dimensions of 2x3 instead of four cells
   * with dimensions of 1x2, 1x2, 1x1, 1x1.
   */
  static final class FrameBuffer {
    static final int MAX_SIZE = DirectionBuffer.MAX_SIZE;
    static final byte CELL_SIZE = DirectionBuffer.CELL_SIZE;

    DirectionBuffer directionBuffer;
    DccDirection direction;
    DccFrame frame;
    int cellsW;
    int cellsH;
    int size; // cellsW * cellsH

    final short[] x = new short[MAX_SIZE];
    final short[] y = new short[MAX_SIZE];
    final byte[] w = PlatformDependent.allocateUninitializedArray(MAX_SIZE);
    final byte[] h = PlatformDependent.allocateUninitializedArray(MAX_SIZE);

    void set(int i, short x, short y, byte w, byte h) {
        this.x[i] = x;
        this.y[i] = y;
        this.w[i] = w;
        this.h[i] = h;
    }

    short x(int i) {
      return x[i];
    }

    short y(int i) {
      return y[i];
    }

    byte w(int i) {
      return w[i];
    }

    byte h(int i) {
      return h[i];
    }

    void clear(DirectionBuffer directionBuffer, DccFrame frame) {
      this.directionBuffer = directionBuffer;
      this.direction = directionBuffer.direction;
      this.frame = frame;
      clear(direction.box, frame.box);
    }

    /** @deprecated for use in tests, use {@link #clear(DirectionBuffer, DccFrame)} instead */
    @Deprecated
    void clear(BBox d, BBox f) {
      assert CELL_SIZE == 4 : "CELL_SIZE(" + CELL_SIZE + ") != " + 4;

      byte startCell;

      final int cellsW;
      final byte startCellW;
      final byte endCellW;
      final int defCellsW;
      startCell = (byte) (4 - ((f.xMin - d.xMin) & 0x3)); // <4
      // System.out.println("startCell: " + startCell);
      if (f.width - startCell <= 1) {
        // first w cell is only w cell and is a subset of the cell
        startCellW = (byte) f.width; // <4
        endCellW = 0;
        defCellsW = 0;
        cellsW = 1;
      } else {
        startCellW = startCell;
        int remWidth = f.width - startCellW;
        final byte endCell = (byte) (remWidth & 0x3);
        // System.out.println("endCell: " + endCell);
        if (endCell == 0) { // no remainder
          endCellW = endCell;
          defCellsW = remWidth >>> 2;
          cellsW = 1 + defCellsW;
        } else if (endCell == 1) { // merge with previous cell
          endCellW = 5;
          defCellsW = (remWidth >>> 2) - 1;
          cellsW = 1 + defCellsW + 1;
        } else { // append remainder
          endCellW = endCell;
          defCellsW = remWidth >>> 2;
          cellsW = 1 + defCellsW + 1;
        }
      }
      // System.out.println("cellsW: " + cellsW);
      // System.out.println("startCellW: " + startCellW);
      // System.out.println("endCellW: " + endCellW);
      // System.out.println("defCellsW: " + defCellsW);

      final int cellsH;
      final byte startCellH;
      final byte endCellH;
      final int defCellsH;
      startCell = (byte) (4 - ((f.yMin - d.yMin) & 0x3)); // <4
      // System.out.println("startCell: " + startCell);
      if (f.height - startCell <= 1) {
        // first h cell is only h cell and is a subset of the cell
        startCellH = (byte) f.height; // <4
        endCellH = 0;
        defCellsH = 0;
        cellsH = 1;
      } else {
        startCellH = startCell;
        int remHeight = f.height - startCellH;
        final byte endCell = (byte) (remHeight & 0x3);
        // System.out.println("endCell: " + endCell);
        if (endCell == 0) { // no remainder
          endCellH = endCell;
          defCellsH = remHeight >>> 2;
          cellsH = 1 + defCellsH;
        } else if (endCell == 1) { // merge with previous cell
          endCellH = 5;
          defCellsH = (remHeight >>> 2) - 1;
          cellsH = 1 + defCellsH + 1;
        } else { // append remainder
          endCellH = endCell;
          defCellsH = remHeight >>> 2;
          cellsH = 1 + defCellsH + 1;
        }
      }
      // System.out.println("cellsH: " + cellsH);
      // System.out.println("startCellH: " + startCellH);
      // System.out.println("endCellH: " + endCellH);
      // System.out.println("defCellsH: " + defCellsH);

      this.cellsW = cellsW;
      this.cellsH = cellsH;
      size = cellsW * cellsH;

      int i = 0;
      final short xReset = (short) (f.xMin - d.xMin);
      short y = (short) (f.yMin - d.yMin);
      assert startCellH > 0 : "startCellH(" + startCellH + ") <= " + 0;
      // if (startCellH > 0) {
      resetCells(startCellW, defCellsW, endCellW, i, xReset, y, startCellH);
      y += startCellH;
      i += cellsW;
      // }
      for (int cy = 0; cy < defCellsH; cy++) {
        resetCells(startCellW, defCellsW, endCellW, i, xReset, y, CELL_SIZE);
        y += CELL_SIZE;
        i += cellsW;
      }
      if (endCellH > 0) {
        resetCells(startCellW, defCellsW, endCellW, i, xReset, y, endCellH);
        // y += endCellH;
        // i += cellsW;
      }
    }

    private void resetCells(byte startCellW, int defCellsW, byte endCellW, int i, short x, short y, byte cellSize) {
      assert startCellW > 0 : "startCellW(" + startCellW + ") <= " + 0;
      // if (startCellW > 0) {
      set(i++, x, y, startCellW, cellSize);
      x += startCellW;
      // }
      for (int cx = 0; cx < defCellsW; cx++) {
        set(i++, x, y, CELL_SIZE, cellSize);
        x += CELL_SIZE;
      }
      if (endCellW > 0) {
        set(i/*++*/, x, y, endCellW, cellSize);
        // x += endCellW;
      }
      // y += cellSize;
    }
  }

  static final class PixelBuffer {
    /** cardinality of integers from 0x0..0xf */
    static final int[] PIXEL_TABLE = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4 };
    /** bit mask of 4 8-bit pixels encoded into 32-bit integer */
    static final int[] PIXEL_MASK = {
        0x00000000, 0x000000ff, 0x0000ff00, 0x0000ffff,
        0x00ff0000, 0x00ff00ff, 0x00ffff00, 0x00ffffff,
        0xff000000, 0xff0000ff, 0xff00ff00, 0xff00ffff,
        0xffff0000, 0xffff00ff, 0xffffff00, 0xffffffff,
    };

    static final int DEFAULT_BUFFER_CAPACITY = 5625; // or 120000?
    static final int DEFAULT_QUEUE_CAPACITY = 0x100000;

    final int bufferCapacity;
    final short[] lastFrame; // can be simplified to bool/bitset later to represent it was ever touched
    final int[] lastCell; // cell mapped to this buffer cell (queue index)

    final int queueCapacity;
    final short[] frame; // frame pushing this cell
    final int[] cell; // fCell of this cell
    final byte[] pixels; // pixels of the cell
    int iter;
    int size;

    PixelBuffer() {
      this(DEFAULT_BUFFER_CAPACITY, DEFAULT_QUEUE_CAPACITY);
    }

    PixelBuffer(int bufferCapacity, int queueCapacity) {
      this.bufferCapacity = bufferCapacity;
      this.lastFrame = new short[bufferCapacity];
      this.lastCell = new int[bufferCapacity];

      this.queueCapacity = queueCapacity;
      frame = new short[queueCapacity];
      cell = new int[queueCapacity];
      pixels = PlatformDependent.allocateUninitializedArray(queueCapacity << 2);
    }

    void clear(int bufferSize) {
      Arrays.fill(lastFrame, 0, bufferSize, (short) -1);
      Arrays.fill(lastCell, 0, bufferSize, -1);
      iter = 0;
      size = 0;
    }

    boolean touched(int cell) {
      return lastFrame[cell] >= 0;
    }

    /** frame at iter position */
    int peekFrame() {
      return iter >= size ? -1 : frame[iter];
    }

    /** cell at iter position */
    int peekCell() {
      return iter >= size ? -1 : cell[iter];
    }

    /** checks if iter position matches f,c */
    boolean peek(int f, int c) {
      return iter < size && frame[iter] == f && cell[iter] == c;
    }

    /** queue iter position */
    int peek() {
      return iter;
    }

    /** increments queue iter position */
    int dequeue() {
      return iter++;
    }

    void enqueue(int dCell, int frame, int fCell, int pixelMask, int[] pixelStack, int stackPtr) {
      lastFrame[dCell] = (short) frame; // touch buffer dCell

      final int queuePtr = size++;
      this.frame[queuePtr] = (short) frame;
      this.cell[queuePtr] = fCell;

      // map dCell from buffer dCell to queue dCell
      final int queueCell = lastCell[dCell] < 0 ? queuePtr : lastCell[dCell];
      lastCell[dCell] = queuePtr; // save enqueued dCell to active dCell

      /** if bit is set, copy from pixelStack, otherwise, copy from dCell */
      for (int i = 0, bit = 1; i < 4; bit = 1 << ++i) {
        if ((pixelMask & bit) == bit) {
          pixels[(queuePtr << 2) + i] = stackPtr > 0 ? (byte) pixelStack[--stackPtr] : 0;
        } else {
          pixels[(queuePtr << 2) + i] = pixels[(queueCell << 2) + i];
        }
        // System.out.println(String.format("      %d 0x%02x", i, pixels[queuePtr][i] & 0xff));
      }
    }
  }

  static final class Bitmap {
    static final int MAX_SIZE = 0x40000;
    final byte[] colormap0 = PlatformDependent.allocateUninitializedArray(MAX_SIZE);
    final byte[] colormap1 = PlatformDependent.allocateUninitializedArray(MAX_SIZE);
    byte[] frontBuffer;
    byte[] backBuffer;
    int width, height;
    int stride;
    int length;

    Bitmap() {
      frontBuffer = colormap0;
      backBuffer = colormap1;
    }

    void flip() {
      byte[] tmp = frontBuffer;
      frontBuffer = backBuffer;
      backBuffer = tmp;
      Arrays.fill(frontBuffer, 0, length, (byte) 0);
    }

    void reset(DccDirection dir) {
      reset(dir.box.width, dir.box.height);
    }

    /** @deprecated for use in tests, use {@link #reset(DccDirection)} instead */
    @Deprecated
    void reset(int width, int height) {
      this.width = width;
      this.height = height;
      stride = width;
      length = width * height;
      // TODO: only need to reset the buffer which becomes new front
      // Arrays.fill(colormap0, 0, length, (byte) 0);
      // Arrays.fill(colormap1, 0, length, (byte) 0);
    }

    void clear(short fCx, short fCy, byte fCw, byte fCh) {
      for (int yC = fCy, yS = yC + fCh; yC < yS; yC++) {
        int id = yC * stride;
        for (int xC = fCx, xS = xC + fCw; xC < xS; xC++) {
          frontBuffer[id++] = 0;
        }
      }
    }

    void fill(short fCx, short fCy, byte fCw, byte fCh, byte value) {
      for (int yC = fCy, yS = yC + fCh; yC < yS; yC++) {
        int id = yC * stride;
        for (int xC = fCx, xS = xC + fCw; xC < xS; xC++) {
          frontBuffer[id++] = value;
        }
      }
    }

    void set(short fCx, short fCy, int x, int y, byte value) {
      final int id = ((fCy + y) * stride) + (fCx + x);
      frontBuffer[id] = value;
    }

    void copy(
        int srcX, int srcY,
        int dstX, int dstY,
        int width, int height
    ) {
      int fromIndexSrc = srcY * stride + srcX;
      int fromIndexDst = dstY * stride + dstX;
      for (int r = 0; r < height; r++) {
        System.arraycopy(backBuffer, fromIndexSrc, frontBuffer, fromIndexDst, width);
        fromIndexSrc += stride;
        fromIndexDst += stride;
      }
    }
  }
}
