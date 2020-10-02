package com.riiablo.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;

import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.PaletteIndexedPixmap;
import com.riiablo.io.BitInput;
import com.riiablo.io.BitUtils;
import com.riiablo.io.ByteInput;
import com.riiablo.mpq.MPQFileHandle;
import com.riiablo.util.BufferUtils;

public class DCC extends com.riiablo.codec.DC {
  private static final String TAG = "DCC";
  private static final boolean DEBUG            = !true;
  private static final boolean DEBUG_DIRECTIONS = DEBUG && true;
  private static final boolean DEBUG_FRAMES     = DEBUG && true;
  private static final boolean DEBUG_SHEETS     = DEBUG && false;
  private static final boolean DEBUG_PB_SIZE    = DEBUG && true;

  public static final String EXT = "dcc";

  Header    header;
  Direction directions[];
  Frame     frames[][];
  BBox      box;

  Texture   textures[][];

  private DCC(Header header, Direction[] directions, Frame[][] frames, BBox box) {
    this.header     = header;
    this.directions = directions;
    this.frames     = frames;
    this.box        = box;
    this.regions    = new TextureRegion[header.directions][];
  }

  @Override
  public void dispose() {
    disposeFrames();
    disposeTextures();
  }

  private void disposeFrames() {
    final int numDirections = header.directions;
    for (int d = 0; d < numDirections; d++) {
      Frame[] frames = this.frames[d];
      if (frames != null) for (Frame frame : frames) {
        if (frame.pixmap == null) continue;
        frame.pixmap.dispose();
        frame.pixmap = null;
      }
    }
  }

  private void disposeTextures() {
    if (textures == null) return;
    final int numDirections = header.directions;
    for (int d = 0; d < numDirections; d++) {
      Texture[] frames = textures[d];
      if (frames != null) for (Texture frame : frames) frame.dispose();
    }

    textures = null;
  }

  @Override
  public Pixmap getPixmap(int d, int f) {
    return frames[d][f].pixmap;
  }

  @Override
  public TextureRegion getTexture(int d, int i) {
    assert regions[d] != null : "loadDirection(d) must be called before getTexture(d,i)";
    return regions[d][i];
  }

  @Override
  public boolean isPreloaded(int d) {
    return true;
  }

  @Override
  public void preloadDirections(boolean combineFrames) {
    assert !combineFrames;
  }

  @Override
  public void preloadDirection(int d, boolean combineFrames) {
    assert !combineFrames;
  }

  @Override
  public boolean isLoaded(int d) {
    return textures != null && textures[d] != null;
  }

  @Override
  public void loadDirections(boolean combineFrames) {
    final int numDirections = header.directions;
    for (int d = 0; d < numDirections; d++) loadDirection(d);
  }

  @Override
  public void loadDirection(int d, boolean combineFrames) {
    if (textures == null) textures = new Texture[header.directions][];
    else if (textures[d] != null) return;
    preloadDirection(d);

    textures[d] = new Texture[header.framesPerDir];
    for (int f = 0; f < header.framesPerDir; f++) {
      Pixmap pixmap = frames[d][f].pixmap;
      Texture texture = new Texture(new PixmapTextureData(pixmap, null, false, false, false));
      //texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
      textures[d][f] = texture;
    }

    regions[d] = new TextureRegion[header.framesPerDir];
    for (int f = 0; f < header.framesPerDir; f++) {
      regions[d][f] = new TextureRegion(textures[d][f]);
    }
  }

  public int getVersion() {
    return header.version;
  }

  @Override
  public int getNumDirections() {
    return header.directions;
  }

  @Override
  public int getNumFramesPerDir() {
    return header.framesPerDir;
  }

  @Override
  public Direction getDirection(int d) {
    return directions[d];
  }

  @Override
  public Frame getFrame(int d, int f) {
    return frames[d][f];
  }

  @Override
  public BBox getBox() {
    return box;
  }

  @Override
  public BBox getBox(int d) {
    return directions[d].box;
  }

  @Override
  public BBox getBox(int d, int f) {
    return getBox(d);
    //return frames[d][f].box;
  }

  /*
  public Pixmap frame(int d, int f) {
    Direction dir = directions[d];
    Frame frame = frames[d][f];
    return new PaletteIndexedPixmap(dir.box.width, dir.box.height, frame.colormap);
  }

  @Override
  public Pixmap[] frames(int d) {
    Validate.isTrue(0 <= d && d < header.directions, "Invalid direction specified: " + d);
    final int numFrames = header.framesPerDir;
    Pixmap[] pages = new Pixmap[numFrames];
    // TODO: optimize
    for (int f = 0; f < numFrames; f++) {
      pages[f] = frame(d, f);
    }

    return pages;
  }

  @Override
  public Pixmap[] frameSheets(int d) {
    Validate.isTrue(0 <= d && d < header.directions, "Invalid direction specified: " + d);

    Direction dir = directions[d];
    if (DEBUG_SHEETS) Gdx.app.debug(TAG, "dir.box = " + dir);
    final int columnWidth = dir.box.width;
    final int columnHeight = dir.box.height;
    final int numFrames = header.framesPerDir;
    final int columns = 2048 / columnWidth;
    final int width = columns * columnWidth;
    if (DEBUG_SHEETS) Gdx.app.debug(TAG, "framesPerDir = " + numFrames);
    if (DEBUG_SHEETS) Gdx.app.debug(TAG, "columns = " + columns);
    if (DEBUG_SHEETS) Gdx.app.debug(TAG, "width = " + width);

    final int rows = (numFrames + columns - 1) / columns;
    int height = rows * (columnHeight);
    if (DEBUG_SHEETS) Gdx.app.debug(TAG, "rows = " + rows);
    if (DEBUG_SHEETS) Gdx.app.debug(TAG, "height = " + height);

    final int pages = (height + 2048 - 1) / 2048;
    if (DEBUG_SHEETS) Gdx.app.debug(TAG, "pages = " + pages);
    final int rowsPerPage = (Math.min(height, 2048) / columnHeight);
    if (DEBUG_SHEETS) Gdx.app.debug(TAG, "rowsPerPage = " + rowsPerPage);

    Pixmap[] sheets = new Pixmap[pages];
    for (int i = 0; i < pages; i++) {
      int h = height;
      if (h > 2048) {
        h = rowsPerPage * columnHeight;
        height -= 2048;
      } else if (h < columnHeight) {
        h = columnHeight;
      }

      if (DEBUG_SHEETS) Gdx.app.debug(TAG, "h = " + h);
      Pixmap sheet = new PaletteIndexedPixmap(width, h);
      sheets[i] = sheet;
      if (DEBUG_SHEETS) Gdx.app.debug(TAG, i + " size = " + (width * height) + " bytes");
    }

    Frame[] src = new Frame[columns];
    for (int p = 0, r = 0, f = 0; p < pages && f < numFrames; f++) {
      Pixmap sheet = sheets[p];
      ByteBuffer buffer = sheet.getPixels();
      for (int rpp = 0; rpp < rowsPerPage && r < rows && f < numFrames; rpp++, r++) {
        int size = 0;
        while (size < columns && f < numFrames) {
          src[size++] = frames[d][f++];
        }

        for (int h = 0; h < columnHeight; h++) {
          int rowStart = buffer.position();
          for (int c = 0; c < size; c++) {
            Frame frame = src[c];
            int columnStart = buffer.position();
            if (h < frame.height) {
              buffer.put(frame.colormap, h * frame.width, frame.width);
            }

            buffer.position(columnStart + columnWidth);
          }

          buffer.position(rowStart + width);
        }
      }

      buffer.rewind();
    }

    return sheets;
  }

  @Override
  public Animation getAnimation() {
    return Animation.newAnimation(this);
  }

  @Override
  public Animation getAnimation(int d) {
    return Animation.newAnimation(this, d);
  }

  @Override
  public Animation.Layer getLayer() {
    return new Animation.Layer(this);
  }

  @Override
  public Animation.Layer getLayer(int blendMode) {
    return new Animation.Layer(this, blendMode);
  }*/

  public static DCC loadFromArray(byte[] bytes) {
    return loadFromByteBuf(Unpooled.wrappedBuffer(bytes));
  }

  public static DCC loadFromByteBuf(ByteBuf buffer) {
    ByteInput in = ByteInput.wrap(buffer);
    final int fileSize = in.bytesRemaining();

    Header header = Header.obtain(in);
    if (DEBUG) Gdx.app.debug(TAG, header.toString());

    final int numDirections = header.directions;
    final int numFrames = header.framesPerDir;

    int[] dirOffsets = new int[numDirections + 1];
    BitUtils.readSafe32u(in, dirOffsets, 0, numDirections);
    dirOffsets[numDirections] = fileSize;
    if (DEBUG) Gdx.app.debug(TAG, "direction offsets = " + Arrays.toString(dirOffsets));

    BBox box = new BBox();
    box.xMin = box.yMin = Integer.MAX_VALUE;
    box.xMax = box.yMax = Integer.MIN_VALUE;

    Direction[] directions = new Direction[numDirections];
    Frame[][] frames = new Frame[numDirections][numFrames];
    int start = dirOffsets[0], end;
    for (int d = 0; d < numDirections; d++) {
      end = dirOffsets[d + 1];
      final Direction dir = directions[d] = Direction.obtain(in, end - start, frames[d]);
      if (DEBUG_DIRECTIONS) Gdx.app.debug(TAG, dir.toString());
      if (DEBUG_FRAMES) for (Frame frame : frames[d]) Gdx.app.debug(TAG, frame.toString());

      Cache cache = new Cache(header);
      fillPixelBuffer(cache, dir, frames[d]);
      makeFrames(cache, dir, frames[d]);
      if (DEBUG_PB_SIZE) Gdx.app.debug(TAG, "pixelBuffer.size = " + cache.numEntries);

      start = end;

      assert dir.equalCellBitStream.bitsRemaining() == 0;
      assert dir.pixelMaskBitStream.bitsRemaining() == 0;
      assert dir.encodingTypeBitStream.bitsRemaining() == 0;
      assert dir.rawPixelCodesBitStream.bitsRemaining() == 0;
      assert dir.pixelCodeAndDisplacementBitStream.bytesRemaining() == 0;

      if (dir.box.xMin < box.xMin) box.xMin = dir.box.xMin;
      if (dir.box.yMin < box.yMin) box.yMin = dir.box.yMin;
      if (dir.box.xMax > box.xMax) box.xMax = dir.box.xMax;
      if (dir.box.yMax > box.yMax) box.yMax = dir.box.yMax;
    }

    box.width  = box.xMax - box.xMin + 1;
    box.height = box.yMax - box.yMin + 1;

    return new DCC(header, directions, frames, box);
  }

  public static DCC loadFromFile(FileHandle handle) {
    if (handle instanceof MPQFileHandle) {
      return loadFromArray(handle.readBytes());
    } else if (handle instanceof com.riiablo.mpq_bytebuf.MPQFileHandle) {
      return loadFromByteBuf(((com.riiablo.mpq_bytebuf.MPQFileHandle) handle).readByteBuf());
    }

    return loadFromArray(handle.readBytes());
  }

//  public static DCC loadFromStream(InputStream in) {
//    try {
//      final int fileSize = in.available();
//
//      Header header = Header.obtain(in);
//      if (DEBUG) Gdx.app.debug(TAG, header.toString());
//
//      final int numDirections = header.directions;
//      final int numFrames = header.framesPerDir;
//
//      int[] dirOffsets = new int[numDirections + 1];
//      ByteBuffer.wrap(IOUtils.readFully(in, numDirections << 2))
//          .order(ByteOrder.LITTLE_ENDIAN)
//          .asIntBuffer()
//          .get(dirOffsets, 0, numDirections);
//      dirOffsets[numDirections] = fileSize;
//      if (DEBUG) Gdx.app.debug(TAG, "direction offsets = " + Arrays.toString(dirOffsets));
//
//      BBox box = new BBox();
//      box.xMin = box.yMin = Integer.MAX_VALUE;
//      box.xMax = box.yMax = Integer.MIN_VALUE;
//
//      Direction[] directions = new Direction[numDirections];
//      Frame[][]   frames     = new Frame    [numDirections][numFrames];
//      int start = dirOffsets[0], end;
//      for (int d = 0; d < numDirections; d++) {
//        end = dirOffsets[d + 1];
//        Direction dir = directions[d] = Direction.obtain(in, end - start, frames[d]);
//        if (DEBUG_DIRECTIONS) Gdx.app.debug(TAG, dir.toString());
//        if (DEBUG_FRAMES) for (Frame frame : frames[d]) Gdx.app.debug(TAG, frame.toString());
//
//        Cache cache = new Cache(header);
//        fillPixelBuffer(cache, dir, frames[d]);
//        makeFrames(cache, dir, frames[d]);
//        if (DEBUG_PB_SIZE) Gdx.app.debug(TAG, "pixelBuffer.size = " + cache.numEntries);
//
//        start = end;
//
//        assert dir.equalCellBitStream.tell() == dir.equalCellBitStream.sizeInBits();
//        assert dir.pixelMaskBitStream.tell() == dir.pixelMaskBitStream.sizeInBits();
//        assert dir.encodingTypeBitStream.tell() == dir.encodingTypeBitStream.sizeInBits();
//        assert dir.rawPixelCodesBitStream.tell() == dir.rawPixelCodesBitStream.sizeInBits();
//        assert dir.pixelCodeAndDisplacementBitStream.tell() + 7 >= dir.pixelCodeAndDisplacementBitStream.sizeInBits();
//
//        if (dir.box.xMin < box.xMin) box.xMin = dir.box.xMin;
//        if (dir.box.yMin < box.yMin) box.yMin = dir.box.yMin;
//        if (dir.box.xMax > box.xMax) box.xMax = dir.box.xMax;
//        if (dir.box.yMax > box.yMax) box.yMax = dir.box.yMax;
//      }
//
//      box.width  = box.xMax - box.xMin + 1;
//      box.height = box.yMax - box.yMin + 1;
//
//      return new DCC(header, directions, frames, box);
//    } catch (Throwable t) {
//      throw new GdxRuntimeException("Couldn't load DCC from stream.", t);
//    } finally {
//      StreamUtils.closeQuietly(in);
//    }
//  }

  private static void fillPixelBuffer(Cache cache, Direction dir, Frame[] frames) {
    cache.pixelBuffer = new PixelBuffer[PixelBuffer.MAX_VALUE];
    cache.frameBuffer = Bitmap.create(dir.box.width, dir.box.height);
    prepareBufferCells(cache, dir);

    final int frameBufferCellsW = cache.frameBufferCellsW;
    final int frameBufferCellsH = cache.frameBufferCellsH;
    final int numCells = frameBufferCellsW * frameBufferCellsH;
    PixelBuffer[] cellBuffer = new PixelBuffer[numCells];

    int cellsW, cellsH;
    int cellX, cellY;

    int tmp, pixelMask = 0;
    int lastPixel, pixels, decodedPixels;
    int[] readPixel = new int[4];
    int encodingType, pixelDisplacement;

    PixelBuffer oldEntry, newEntry;
    int curId, pixelBufferId = 0;

    int curCX, curCY, curCell;
    for (int f = 0; f < frames.length; f++) {
      Frame frame = frames[f];
      Cache.FrameCache frameCache = cache.frame[f] = new Cache.FrameCache();
      prepareFrameCells(cache, frameCache, dir, frame);

      cellsW = frameCache.cellsW;
      cellsH = frameCache.cellsH;
      cellX  = (frame.box.xMin - dir.box.xMin) / 4;
      cellY  = (frame.box.yMin - dir.box.yMin) / 4;

      for (int cy = 0; cy < cellsH; cy++) {
        curCY = cellY + cy;
        for (int cx = 0; cx < cellsW; cx++) {
          curCX = cellX + cx;
          curCell = curCY * frameBufferCellsW + curCX;
          assert curCell < numCells;

          boolean nextCell = false;
          if (cellBuffer[curCell] != null) {
            if (dir.equalCellBitStreamSize > 0) {
              tmp = dir.equalCellBitStream.read1();
            } else {
              tmp = 0;
            }

            if (tmp == 0) {
              pixelMask = dir.pixelMaskBitStream.read7u(4);
              assert pixelMask >= 0;
            } else {
              nextCell = true;
            }
          } else {
            pixelMask = 0xF;
          }

          if (!nextCell) {
            Arrays.fill(readPixel, 0);
            pixels = PixelBuffer.PIXEL_TABLE[pixelMask];
            if (pixels > 0 && dir.encodingTypeBitStreamSize > 0) {
              encodingType = dir.encodingTypeBitStream.read1();
              assert encodingType >= 0;
            } else {
              encodingType = 0;
            }

            lastPixel = 0;
            decodedPixels = 0;
            for (int i = 0; i < pixels; i++) {
              if (encodingType > 0) {
                readPixel[i] = dir.rawPixelCodesBitStream.read15u(8);
              } else {
                readPixel[i] = lastPixel;
                do {
                  pixelDisplacement = dir.pixelCodeAndDisplacementBitStream.read7u(4);
                  readPixel[i] += pixelDisplacement;
                } while (pixelDisplacement == 0xF);
              }

              if (readPixel[i] == lastPixel) {
                readPixel[i] = 0;
                break;
              } else {
                lastPixel = readPixel[i];
                decodedPixels++;
              }
            }

            oldEntry = cellBuffer[curCell];
            if (pixelBufferId >= PixelBuffer.MAX_VALUE) {
              throw new IllegalStateException("Pixel buffer full, cannot add more entries");
            }

            newEntry = cache.pixelBuffer[pixelBufferId++] = new PixelBuffer();
            curId = decodedPixels - 1;
            for (int i = 0; i < 4; i++) {
              if ((pixelMask & (1 << i)) != 0) {
                if (curId >= 0) {
                  newEntry.val[i] = (byte) readPixel[curId--];
                } else {
                  newEntry.val[i] = 0;
                }
              } else {
                newEntry.val[i] = oldEntry.val[i];
              }
            }

            cellBuffer[curCell] = newEntry;
            newEntry.frame = f; // TODO: I'm not sure how this will behave with f as df
            newEntry.frameCellIndex = cy * cellsW + cx;
          }
        }
      }
    }

    PixelBuffer pbe;
    for (int i = 0; i < pixelBufferId; i++) {
      for (int x = 0; x < 4; x++) {
        pbe = cache.pixelBuffer[i];
        int y = pbe.val[x] & 0xFF;
        pbe.val[x] = dir.pixelValues[y];
      }
    }

    cache.numEntries = pixelBufferId;
  }

  private static void prepareBufferCells(Cache cache, Direction dir) {
    final int bufferW = dir.box.width;
    final int bufferH = dir.box.height;

    final int cellsW = cache.frameBufferCellsW = 1 + ((bufferW - 1) / 4);
    final int[] cellW = new int[cellsW];
    if (cellsW == 1) {
      cellW[0] = bufferW;
    } else {
      int cellMax = cellsW - 1;
      Arrays.fill(cellW, 0, cellMax, 4);
      cellW[cellMax] = bufferW - (4 * cellMax);
    }

    final int cellsH = cache.frameBufferCellsH = 1 + ((bufferH - 1) / 4);
    final int[] cellH = new int[cellsH];
    if (cellsH == 1) {
      cellH[0] = bufferH;
    } else {
      int cellMax = cellsH - 1;
      Arrays.fill(cellH, 0, cellMax, 4);
      cellH[cellMax] = bufferH - (4 * cellMax);
    }

    final int numCells = cellsW * cellsH;
    cache.frameBufferCells = new Cell[numCells];

    //int id = 0;
    int y = 0, x = 0;
    for (int cy = 0; cy < cellsH; cy++, y += 4, x = 0) {
      for (int cx = 0; cx < cellsW; cx++, x += 4) {
        //assert id == cy * cellsW + cx : "Making sure this optimization doesn't bite me in the ass: " + id + " =? " + (y * cellsH + x);
        // TODO: Cell implements Poolable
        Cell cell = cache.frameBufferCells[cy * cellsW + cx] = new Cell();
        cell.w = cellW[cx];
        cell.h = cellH[cy];
        cell.bmp = cache.frameBuffer.getSubimage(x, y, cell.w, cell.h);
      }
    }

    //assert id == numCells;
  }

  private static void prepareFrameCells(Cache cache, Cache.FrameCache frameCache, Direction dir, Frame frame) {
    int tmp, tmpSize;
    final int frameW = frame.box.width;
    final int frameH = frame.box.height;

    final int cellsW;
    final int w = 4 - ((frame.box.xMin - dir.box.xMin) % 4); // TODO: & 0x3
    if (frameW - w <= 1) {
      cellsW = 1;
    } else {
      tmp = frameW - w - 1;
      tmpSize = 2 + (tmp / 4);
      if (tmp % 4 == 0) tmpSize--;
      cellsW = tmpSize;
    }

    final int cellsH;
    final int h = 4 - ((frame.box.yMin - dir.box.yMin) % 4); // TODO: & 0x3
    if (frameH - h <= 1) {
      cellsH = 1;
    } else {
      tmp = frameH - h - 1;
      tmpSize = 2 + (tmp / 4);
      if (tmp % 4 == 0) tmpSize--;
      cellsH = tmpSize;
    }

    final int[] cellW = new int[cellsW];
    if (cellsW == 1) {
      cellW[0] = frameW;
    } else {
      int cellMax = cellsW - 1;
      cellW[0] = w;
      Arrays.fill(cellW, 1, cellMax, 4);
      cellW[cellMax] = frameW - w - (4 * (cellMax - 1));
    }

    final int[] cellH = new int[cellsH];
    if (cellsH == 1) {
      cellH[0] = frameH;
    } else {
      int cellMax = cellsH - 1;
      cellH[0] = h;
      Arrays.fill(cellH, 1, cellMax, 4);
      cellH[cellMax] = frameH - h - (4 * (cellMax - 1));
    }

    frameCache.cellsW = cellsW;
    frameCache.cellsH = cellsH;

    final int numCells = cellsW * cellsH;
    frameCache.cells = new Cell[numCells];


    int id = 0;
    Cell cell = null;
    final int xReset = frame.box.xMin - dir.box.xMin;
    int y = frame.box.yMin - dir.box.yMin, x = xReset;
    for (int cy = 0; cy < cellsH; cy++, y += cell.h, x = xReset) {
      for (int cx = 0; cx < cellsW; cx++, x += cell.w) {
        assert id == cy * cellsW + cx : "Making sure this optimization doesn't bite me in the ass";
        // TODO: Cell implements Poolable
        cell = frameCache.cells[id++] = new Cell();
        cell.x = x;
        cell.y = y;
        cell.w = cellW[cx];
        cell.h = cellH[cy];
        cell.bmp = cache.frameBuffer.getSubimage(cell.x, cell.y, cell.w, cell.h);
      }
    }

    assert id == numCells;
  }

  private static void makeFrames(Cache cache, Direction dir, Frame[] frames) {
    int size = cache.frameBufferCellsW * cache.frameBufferCellsH;
    for (int c = 0; c < size; c++) {
      cache.frameBufferCells[c].lastW = -1;
      cache.frameBufferCells[c].lastH = -1;
    }

    int pbId = 0;
    PixelBuffer pbe;

    int numCells, cellX, cellY, cellId;

    Frame frame;
    Cache.FrameCache frameCache;
    Bitmap frameBmp = Bitmap.create(dir.box.width, dir.box.height);
    for (int f = 0; f < frames.length; f++, frameBmp.clear()) {
      frame = frames[f];
      frameCache = cache.frame[f];
      numCells = frameCache.cellsW * frameCache.cellsH;
      for (int c = 0; c < numCells; c++) {
        pbe = cache.pixelBuffer[pbId];
        Cell cell = frameCache.cells[c];
        cellX = cell.x / 4;
        cellY = cell.y / 4;
        cellId = cellY * cache.frameBufferCellsW + cellX;
        Cell bufferCell = cache.frameBufferCells[cellId];
        if (pbe == null || pbe.frame != f || pbe.frameCellIndex != c) {
          if (cell.w != bufferCell.lastW || cell.h != bufferCell.lastH) {
            cell.bmp.clear();
          } else {
            Bitmap.copy(cache.frameBuffer, cache.frameBuffer,
                bufferCell.lastX, bufferCell.lastY,
                cell.x, cell.y,
                cell.w, cell.h);

            Bitmap.copy(cell.bmp, frameBmp,
                0, 0,
                cell.x, cell.y,
                cell.w, cell.h);
          }
        } else {
          if (pbe.val[0] == pbe.val[1]) {
            cell.bmp.fill(pbe.val[0]);
          } else {
            int bits;
            if (pbe.val[1] == pbe.val[2]) {
              bits = 1;
            } else {
              bits = 2;
            }

            for (int y = 0; y < cell.h; y++) {
              for (int x = 0; x < cell.w; x++) {
                int pix = dir.pixelCodeAndDisplacementBitStream.read7u(bits);
                cell.bmp.setPixel(x, y, pbe.val[pix]);
              }
            }
          }

          Bitmap.copy(cell.bmp, frameBmp,
              0, 0,
              cell.x, cell.y,
              cell.w, cell.h);
          pbId++;
        }

        bufferCell.lastX = cell.x;
        bufferCell.lastY = cell.y;
        bufferCell.lastW = cell.w;
        bufferCell.lastH = cell.h;
      }

      saveFrame(frame, frameBmp);
    }
  }

  private static void saveFrame(Frame frame, Bitmap frameBmp) {
    frame.colormap = frameBmp.copy();
    frame.pixmap   = new PaletteIndexedPixmap(frameBmp.width, frameBmp.height, frame.colormap);
  }

  static class Header {
    static final int SIZE = 15;

    int signature;
    int version;
    int directions;
    int framesPerDir;
    int tag;
    int finalDC6Size;

    static Header obtain(InputStream in) throws IOException {
      return new Header().read(in);
    }

    static Header obtain(ByteInput in) {
      return new Header().read(in);
    }

    Header read(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      signature    = BufferUtils.readUnsignedByte(buffer);
      version      = BufferUtils.readUnsignedByte(buffer);
      directions   = BufferUtils.readUnsignedByte(buffer);
      framesPerDir = buffer.getInt();
      tag          = buffer.getInt();
      finalDC6Size = buffer.getInt();
      assert !buffer.hasRemaining();
      return this;
    }

    Header read(ByteInput in) {
      in = in.readSlice(SIZE);
      signature = in.read8();
      version = in.readSafe8u();
      directions = in.readSafe8u();
      framesPerDir = in.readSafe32u();
      tag = in.readSafe32u();
      finalDC6Size = in.readSafe32u();
      assert in.bytesRemaining() == 0 : "in.bytesRemaining(" + in.bytesRemaining() + ") > " + 0;
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("signature", "0x" + Integer.toHexString(signature))
          .append("version", version)
          .append("directions", directions)
          .append("framesPerDir", framesPerDir)
          .append("tag", "0x" + Integer.toHexString(tag))
          .append("finalDC6Size", finalDC6Size)
          .build();
    }
  }
  static class Direction extends com.riiablo.codec.DC.Direction {
    static final int MAX_VALUE = 32;

    static final int HasRawPixelEncoding = 0x1;
    static final int CompressEqualCells  = 0x2;

    long outsizeCoded;
    byte compressionFlags;  // 2 bits
    byte variable0Bits;     // 4 bits
    byte widthBits;         // 4 bits
    byte heightBits;        // 4 bits
    byte xOffsetBits;       // 4 bits
    byte yOffsetBits;       // 4 bits
    byte optionalBytesBits; // 4 bits
    byte codedBytesBits;    // 4 bits

    long equalCellBitStreamSize;
    long pixelMaskBitStreamSize;
    long encodingTypeBitStreamSize;
    long rawPixelCodesBitStreamSize;

    BitInput equalCellBitStream;
    BitInput pixelMaskBitStream;
    BitInput encodingTypeBitStream;
    BitInput rawPixelCodesBitStream;
    BitInput pixelCodeAndDisplacementBitStream;

    byte pixelValues[]; // unsigned

    //BBox  box; // inherited

    static Direction obtain(ByteInput in, int size, Frame[] frames) {
      return new Direction().read(in, size, frames);
    }

    Direction read(ByteInput in, int size, Frame[] frames) {
//      BitStream bitStream = new BitStream(IOUtils.readFully(in, size), size * Byte.SIZE);
      in = in.readSlice(size);
      outsizeCoded      =        in.readSafe32u();
      BitInput bitStream = in.unalign();
      compressionFlags  = (byte) bitStream.readRaw(2);
      variable0Bits     =        bitStream.read7u(4);
      widthBits         =        bitStream.read7u(4);
      heightBits        =        bitStream.read7u(4);
      xOffsetBits       =        bitStream.read7u(4);
      yOffsetBits       =        bitStream.read7u(4);
      optionalBytesBits =        bitStream.read7u(4);
      codedBytesBits    =        bitStream.read7u(4);

      box = new BBox();
      box.xMin = box.yMin = Integer.MAX_VALUE;
      box.xMax = box.yMax = Integer.MIN_VALUE;

      long optionalBytes = 0;
      for (int f = 0; f < frames.length; f++) {
        Frame frame = frames[f] = Frame.obtain(bitStream, this);
        if (frame.box.xMin < box.xMin) box.xMin = frame.box.xMin;
        if (frame.box.yMin < box.yMin) box.yMin = frame.box.yMin;
        if (frame.box.xMax > box.xMax) box.xMax = frame.box.xMax;
        if (frame.box.yMax > box.yMax) box.yMax = frame.box.yMax;
        optionalBytes += frame.optionalBytes;
      }

      box.width  = box.xMax - box.xMin + 1;
      box.height = box.yMax - box.yMin + 1;

      if (optionalBytes > 0) readOptionalBytes(bitStream, frames);
      readBitStreamSizes(bitStream);
      readPixelValues(bitStream);
      initDirBitStreams(bitStream);
      return this;
    }

    private void readOptionalBytes(BitInput bitStream, Frame[] frames) {
      ByteInput in = bitStream.align();
      for (Frame frame : frames) {
        if (frame.optionalBytes > 0) {
          frame.optionalBytesData = in.readBytes(frame.optionalBytes);
        }
      }
    }

    private void readBitStreamSizes(BitInput bitStream) {
      equalCellBitStreamSize = 0;
      pixelMaskBitStreamSize = 0;
      encodingTypeBitStreamSize = 0;
      rawPixelCodesBitStreamSize = 0;

      if ((compressionFlags & CompressEqualCells) == CompressEqualCells) {
        equalCellBitStreamSize = bitStream.read31u(20);
      }

      pixelMaskBitStreamSize = bitStream.read31u(20);

      if ((compressionFlags & HasRawPixelEncoding) == HasRawPixelEncoding) {
        encodingTypeBitStreamSize = bitStream.read31u(20);
        rawPixelCodesBitStreamSize = bitStream.read31u(20);
      }
    }

    private void readPixelValues(BitInput bitStream) {
      int index = 0;
      pixelValues = new byte[Palette.COLORS];
      for (int i = 0; i < Palette.COLORS; i++) {
        if (bitStream.readBoolean()) pixelValues[index++] = (byte) i;
      }
    }

    private void initDirBitStreams(BitInput bitStream) {
      assert (compressionFlags & CompressEqualCells) != CompressEqualCells || equalCellBitStreamSize > 0;
      equalCellBitStream = bitStream.readSlice(equalCellBitStreamSize);
      pixelMaskBitStream = bitStream.readSlice(pixelMaskBitStreamSize);
      assert (compressionFlags & HasRawPixelEncoding) != HasRawPixelEncoding
          || (encodingTypeBitStreamSize > 0 && rawPixelCodesBitStreamSize > 0);
      encodingTypeBitStream = bitStream.readSlice(encodingTypeBitStreamSize);
      rawPixelCodesBitStream = bitStream.readSlice(rawPixelCodesBitStreamSize);
      pixelCodeAndDisplacementBitStream = bitStream.readSlice(bitStream.bitsRemaining());
    }

    public String getFlags() {
      StringBuilder builder = new StringBuilder();
      if ((compressionFlags & HasRawPixelEncoding) == HasRawPixelEncoding) {
        builder.append("HasRawPixelEncoding|");
      }

      if ((compressionFlags & CompressEqualCells) == CompressEqualCells) {
        builder.append("CompressEqualCells|");
      }

      int length = builder.length();
      if (length > 0) {
        builder.setLength(length - 1);
      }

      return builder.toString();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("outsizeCoded", outsizeCoded)
          .append("compressionFlags", getFlags())
          .append("variable0Bits", variable0Bits)
          .append("widthBits", widthBits)
          .append("heightBits", heightBits)
          .append("xOffsetBits", xOffsetBits)
          .append("yOffsetBits", yOffsetBits)
          .append("optionalBytesBits", optionalBytesBits)
          .append("codedBytesBits", codedBytesBits)
          //.append("pixelBufferCellsX", pixelBufferCellsX)
          //.append("pixelBufferCellsY", pixelBufferCellsY)
          .append("box", box)
          .build();
    }
  }
  static class Frame extends DC.Frame {
    static final int MAX_VALUE = 256;

    static final int BITS_WIDTH_TABLE[] = {
        0, 1, 2, 4, 6, 8, 10, 12, 14, 16, 20, 24, 26, 28, 30, 32
    };

    int variable0;
    //int width; // inherited
    //int height; // inherited
    //int xOffset; // inherited
    //int yOffset; // inherited
    int optionalBytes;
    int codedBytes;
    //int flip; // inherited

    byte optionalBytesData[];

    //BBox   box; // inherited
    Pixmap pixmap;

    static Frame obtain(BitInput bitStream, Direction direction) {
      return new Frame().read(bitStream, direction);
    }

    Frame read(BitInput bitStream, Direction d) {
      variable0     = (int) bitStream.read63u(BITS_WIDTH_TABLE[d.variable0Bits]);
      width         = (int) bitStream.read63u(BITS_WIDTH_TABLE[d.widthBits]);
      height        = (int) bitStream.read63u(BITS_WIDTH_TABLE[d.heightBits]);
      xOffset       = (int) bitStream.read32 (BITS_WIDTH_TABLE[d.xOffsetBits]);
      yOffset       = (int) bitStream.read32 (BITS_WIDTH_TABLE[d.yOffsetBits]);
      optionalBytes = (int) bitStream.read63u(BITS_WIDTH_TABLE[d.optionalBytesBits]);
      codedBytes    = (int) bitStream.read63u(BITS_WIDTH_TABLE[d.codedBytesBits]);
      flip          =       bitStream.read1();

      optionalBytesData = ArrayUtils.EMPTY_BYTE_ARRAY;

      box = new BBox();
      box.xMin = xOffset;
      box.xMax = box.xMin + width - 1;
      if (flip != 0) { // bottom-up
        box.yMin = yOffset;
        box.yMax = box.yMin + height - 1;
      } else {        // top-down
        box.yMax = yOffset;
        box.yMin = box.yMax - height + 1;
      }

      box.width  = box.xMax - box.xMin + 1;
      box.height = box.yMax - box.yMin + 1;
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("variable0", variable0)
          .append("width", width)
          .append("height", height)
          .append("xOffset", xOffset)
          .append("yOffset", yOffset)
          .append("optionalBytes", optionalBytes)
          .append("codedBytes", codedBytes)
          .append("flip", flip)
          .append("box", box)
          .build();
    }
  }
  static class Cache {
    int  frameBufferCellsW, frameBufferCellsH;
    Cell frameBufferCells[];

    PixelBuffer pixelBuffer[];
    int numEntries;

    Bitmap frameBuffer;

    FrameCache frame[];

    Cache(Header header) {
      frame = new FrameCache[header.framesPerDir];
    }

    static class FrameCache {
      int  cellsW, cellsH;
      Cell cells[];
    }
  }
  static class Cell {
    int x, y;
    int w, h;

    int lastX, lastY;
    int lastW, lastH;

    Bitmap bmp;

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("x", x)
          .append("y", y)
          .append("w", w)
          .append("h", h)
          //.append("lastX", lastX)
          //.append("lastY", lastY)
          //.append("lastW", lastW)
          //.append("lastH", lastH)
          .build();
    }
  }
  static class PixelBuffer {
    // TODO: Find more accurate value, 5625 per direction?
    //static final int MAX_VALUE = 5625;
    static final int MAX_VALUE = 65586;
    static final int[] PIXEL_TABLE = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4 };

    byte val[] = new byte[4];
    int  frame = -1;
    int  frameCellIndex = -1;
  }
  static class Bitmap {
    byte colormap[];
    int  x, y;
    int  width, height;
    int  stride;

    static Bitmap create(int width, int height) {
      return new Bitmap(new byte[width * height], width, height);
    }

    Bitmap(byte[] colormap, int w, int h) {
      this.colormap = colormap;
      x = y = 0;
      width = stride = w;
      height = h;
    }

    Bitmap(Bitmap bmp, int x, int y, int w, int h) {
      colormap = bmp.colormap;
      this.x = bmp.x + x;
      this.y = bmp.y + y;
      width = w;
      height = h;
      stride = bmp.stride;

      assert x + w <= bmp.width && y + h <= bmp.height;
    }

    Bitmap getSubimage(int x, int y, int width, int height) {
      return new Bitmap(this, x, y, width, height);
    }

    void clear() {
      fill((byte) 0);
    }

    void fill(byte id) {
      fillBytes(0, 0, width, height, id);
    }

    void fillBytes(int x, int y, int w, int h, byte id) {
      x += this.x;
      y += this.y;
      int start = y * stride + x, end;
      for (int r = 0; r < h; r++) {
        end = start + w;
        Arrays.fill(colormap, start, end, id);
        start += stride;
      }
    }

    void setPixel(int x, int y, byte i) {
      x += this.x;
      y += this.y;
      colormap[y * stride + x] = i;
    }

    static void copy(Bitmap src, Bitmap dst,
                     int srcX, int srcY,
                     int dstX, int dstY,
                     int width, int height) {
      assert srcX + width <= src.width && dstX + width <= dst.width;
      srcX += src.x;
      srcY += src.y;
      int fromIndexSrc = srcY * src.stride + srcX;
      dstX += dst.x;
      dstY += dst.y;
      int fromIndexDst = dstY * dst.stride + dstX;
      for (int r = 0; r < height; r++) {
        System.arraycopy(
            src.colormap, fromIndexSrc,
            dst.colormap, fromIndexDst,
            width);
        fromIndexSrc += src.stride;
        fromIndexDst += dst.stride;
      }
    }

    void copy(Bitmap dst) {
      copy(this, dst, 0, 0, 0, 0, width, height);
    }

    byte[] copy() {
      return Arrays.copyOf(colormap, colormap.length);
    }
  }
}
