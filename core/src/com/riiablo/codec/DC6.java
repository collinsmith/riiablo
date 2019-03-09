package com.riiablo.codec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.riiablo.codec.DC;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.PaletteIndexedPixmap;
import com.riiablo.util.BufferUtils;

public class DC6 extends com.riiablo.codec.DC {
  private static final String TAG = "DC6";
  private static final boolean DEBUG            = true;
  private static final boolean DEBUG_DIRECTIONS = DEBUG && false;
  private static final boolean DEBUG_FRAMES     = DEBUG && false;
  private static final boolean DEBUG_SHEETS     = DEBUG && false;

  public static final String EXT = "dc6";

  public static final int PAGE_SIZE = 256;

  Header    header;
  Direction directions[];
  Frame     frames[][];
  BBox      box;

  Pixmap    pixmaps[][];
  Texture   textures[][];

  private static DC6 obtain(Header header, Direction[] directions, Frame[][] frames, BBox box) {
    return new DC6().set(header, directions, frames, box);
  }

  private DC6() {}

  private DC6 set(Header header, Direction[] directions, Frame[][] frames, BBox box) {
    this.header     = header;
    this.directions = directions;
    this.frames     = frames;
    this.box        = box;
    return this;
  }

  @Override
  public void dispose() {
    disposeFrames();
    disposePixmaps();
    disposeTextures();
  }

  private void disposeFrames() {
    final int numDirections = header.directions;
    for (int d = 0; d < numDirections; d++) {
      Frame[] frames = this.frames[d];
      if (frames != null) for (Frame frame : frames) {
        frame.pixmap.dispose();
      }
    }
  }

  private void disposePixmaps() {
    if (pixmaps == null) return;
    final int numDirections = header.directions;
    for (int d = 0; d < numDirections; d++) {
      Frame[] frames = this.frames[d];
      Pixmap[] pixmaps = this.pixmaps[d];
      if (pixmaps != null && (frames == null || frames.length != pixmaps.length)) {
        for (Pixmap pixmap : pixmaps) pixmap.dispose();
      }
    }

    pixmaps = null;
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
  public int getNumPages(int d) {
    return textures[d].length;
  }

  @Override
  public Pixmap getPixmap(int d, int f) {
    return frames[d][f].pixmap;
  }

  // TODO: This is a workaround until texture regions are stored properly
  TextureRegion regions[][];

  @Override
  public TextureRegion getTexture(int d, int i) {
    if (regions == null) regions = new TextureRegion[header.directions][pixmaps[d].length];
    TextureRegion region = regions[d][i];
    if (region == null) region = regions[d][i] = new TextureRegion(textures[d][i]);
    return region;
  }

  @Override
  public boolean isPreloaded(int d) {
    return pixmaps != null && pixmaps[d] != null;
  }

  @Override
  public void preloadDirections(boolean combineFrames) {
    final int numDirections = header.directions;
    for (int d = 0; d < numDirections; d++) preloadDirection(d, combineFrames);
  }

  @Override
  public void preloadDirection(int d, boolean combineFrames) {
    if (pixmaps == null) pixmaps = new Pixmap[header.directions][];
    else if (pixmaps[d] != null) return;

    if (!combineFrames) {
      pixmaps[d] = new Pixmap[header.framesPerDir];
      for (int f = 0; f < header.framesPerDir; f++) pixmaps[d][f] = frames[d][f].pixmap;
      return;
    }

    final Frame[] frames = this.frames[d];
    final int numFrames = header.framesPerDir;

    int columns = 0, rows = 0;

    int width = 0;
    for (int w = 0, tmp; w < numFrames; w++) {
      columns++;
      tmp = frames[w].width;
      width += tmp;
      if (tmp < PAGE_SIZE) {
        break;
      }
    }

    int height = 0;
    for (int h = 0, tmp; h < numFrames; h += columns) {
      rows++;
      tmp = frames[h].height;
      height += tmp;
      if (tmp < PAGE_SIZE) {
        break;
      }
    }

    final int numPages = numFrames / (rows * columns);
    Pixmap[] pixmaps = this.pixmaps[d] = new Pixmap[numPages];
    if (numPages == numFrames) {
      for (int f = 0; f < numFrames; f++) pixmaps[f] = frames[f].pixmap;
      return;
    }

    for (int p = 0, f = 0; p < numPages; p++) {
      int y = 0, x = 0;
      Frame frame;
      Pixmap pixmap = pixmaps[p] = new PaletteIndexedPixmap(width, height);
      for (int r = 0; r < rows; r++, x = 0) {
        for (int c = 0; c < columns; c++) {
          frame = frames[f++];
          pixmap.drawPixmap(frame.pixmap, x, y);
          x += frame.width;
        }

        y += PAGE_SIZE;
      }
    }
  }

  @Override
  public boolean isLoaded(int d) {
    return textures != null && textures[d] != null;
  }

  @Override
  public void loadDirections(boolean combineFrames) {
    final int numDirections = header.directions;
    for (int d = 0; d < numDirections; d++) loadDirection(d, combineFrames);
  }

  @Override
  public void loadDirection(int d, boolean combineFrames) {
    if (textures == null) textures = new Texture[header.directions][];
    else if (textures[d] != null) return;
    preloadDirection(d, combineFrames);

    Pixmap[] pixmaps = this.pixmaps[d];
    textures[d] = new Texture[pixmaps.length];
    for (int p = 0; p < pixmaps.length; p++) {
      Pixmap pixmap = pixmaps[p];
      Texture texture = new Texture(new PixmapTextureData(pixmap, null, false, false, false));
      //texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
      textures[d][p] = texture;
    }
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
  public int getNumDirections() {
    return header.directions;
  }

  @Override
  public int getNumFramesPerDir() {
    return header.framesPerDir;
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
    return frames[d][f].box;
  }

  public static DC6 loadFromFile(FileHandle handle) {
    return loadFromStream(handle.read());
  }

  public static DC6 loadFromStream(InputStream in) {
    try {
      final int fileSize = in.available();

      Header header = Header.obtain(in);
      if (DEBUG) Gdx.app.debug(TAG, header.toString());

      int numDirections = header.directions;
      int numFrames = header.framesPerDir;

      int totalFrames = numDirections * numFrames;
      int[] frameOffsets = new int[totalFrames + 1];
      ByteBuffer.wrap(IOUtils.readFully(in, totalFrames << 2))
          .order(ByteOrder.LITTLE_ENDIAN)
          .asIntBuffer()
          .get(frameOffsets, 0, totalFrames);
      frameOffsets[totalFrames] = fileSize;
      if (DEBUG) Gdx.app.debug(TAG, "frame offsets = " + Arrays.toString(frameOffsets));

      Frame[][] frames = new Frame[numDirections][numFrames];
      int start = frameOffsets[0], end;
      for (int d = 0, df = 0; d < numDirections; d++) {
        for (int f = 0; f < numFrames; f++, df++) {
          end = frameOffsets[df + 1];
          frames[d][f] = Frame.obtain(in, end - start);
          if (DEBUG_FRAMES) Gdx.app.debug(TAG, frames[d][f].toString());
          start = end;
        }
      }

      BBox box = new BBox();
      box.xMin = box.yMin = Integer.MAX_VALUE;
      box.xMax = box.yMax = Integer.MIN_VALUE;

      Direction[] directions = new Direction[header.directions];
      for (int d = 0; d < header.directions; d++) {
        Direction dir = directions[d] = Direction.obtain(frames[d]);
        if (DEBUG_DIRECTIONS) Gdx.app.debug(TAG, dir.toString());
        if (dir.box.xMin < box.xMin) box.xMin = dir.box.xMin;
        if (dir.box.yMin < box.yMin) box.yMin = dir.box.yMin;
        if (dir.box.xMax > box.xMax) box.xMax = dir.box.xMax;
        if (dir.box.yMax > box.yMax) box.yMax = dir.box.yMax;
      }

      box.width  = box.xMax - box.xMin + 1;
      box.height = box.yMax - box.yMin + 1;
      return DC6.obtain(header, directions, frames, box);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load DC6 from stream.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  static class Header {
    static final int SIZE = 24;

    static final int MAGIC_NUMBER_1 = 0xEEEEEEEE;
    static final int MAGIC_NUMBER_2 = 0xCDCDCDCD;

    int version;
    int flags;
    int format;
    int termination;
    int directions;
    int framesPerDir;

    static Header obtain(InputStream in) throws IOException {
      return new Header().read(in);
    }

    Header read(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      version      = buffer.getInt();
      flags        = buffer.getInt();
      format       = buffer.getInt();
      termination  = buffer.getInt();
      directions   = buffer.getInt();
      framesPerDir = buffer.getInt();
      assert !buffer.hasRemaining();
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("version", version)
          .append("flags", flags)
          .append("format", format)
          .append("termination", "0x" + Integer.toHexString(termination))
          .append("directions", directions)
          .append("framesPerDir", framesPerDir)
          .toString();
    }
  }
  static class Direction extends com.riiablo.codec.DC.Direction {
    //BBox box;

    static Direction obtain(Frame[] frames) {
      return new Direction().read(frames);
    }

    Direction read(Frame[] frames) {
      box = new BBox();
      box.xMin = box.yMin = Integer.MAX_VALUE;
      box.xMax = box.yMax = Integer.MIN_VALUE;
      for (int f = 0; f < frames.length; f++) {
        Frame frame = frames[f];
        if (frame.box.xMin < box.xMin) box.xMin = frame.box.xMin;
        if (frame.box.yMin < box.yMin) box.yMin = frame.box.yMin;
        if (frame.box.xMax > box.xMax) box.xMax = frame.box.xMax;
        if (frame.box.yMax > box.yMax) box.yMax = frame.box.yMax;
      }

      box.width = box.xMax - box.xMin + 1;
      box.height = box.yMax - box.yMin + 1;
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("box", box)
          .toString();
    }
  }
  static class Frame extends DC.Frame {
    static final int SIZE = 32;

    //int flip;
    //int width;
    //int height;
    //int xOffset;
    //int yOffset;
    int allocSize;
    int nextBlock;
    int length;

    //BBox box;
    //byte colormap[];

    Pixmap pixmap;

    static Frame obtain(InputStream in, int size) throws IOException {
      return new Frame().read(in, size);
    }

    Frame read(InputStream in, int size) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, size)).order(ByteOrder.LITTLE_ENDIAN);
      flip      = buffer.getInt();
      width     = buffer.getInt();
      height    = buffer.getInt();
      xOffset   = buffer.getInt();
      yOffset   = buffer.getInt();
      allocSize = buffer.getInt();
      nextBlock = buffer.getInt();
      length    = buffer.getInt();

      box = new BBox();
      box.xMin = xOffset;
      box.xMax = box.xMin + width - 1;
      if (flip > 0) { // bottom-up
        box.yMin = yOffset;
        box.yMax = box.yMin + height - 1;
      } else { // top-down
        box.yMax = yOffset;
        box.yMin = box.yMax - height + 1;
      }

      box.width = box.xMax - box.xMin + 1;
      box.height = box.yMax - box.yMin + 1;

      colormap = new byte[width * height];
      decompress(buffer);
      pixmap = new PaletteIndexedPixmap(width, height, colormap);
      return this;
    }

    void decompress(ByteBuffer in) throws IOException {
      assert width > 0 && height > 0;
      // TODO: Support flipping?

      int x = 0, y = height - 1;

      int rawIndex = 0;
      while (rawIndex < length) {
        int chunkSize = BufferUtils.readUnsignedByte(in);
        rawIndex++;
        if (chunkSize == 0x80) { // eol
          x = 0;
          y--;
        } else if ((chunkSize & 0x80) != 0) { // number of transparent pixels
          x += (chunkSize & 0x7F);
        } else { // number of colors to read
          assert chunkSize + x <= width;
          in.get(colormap, x + width * y, chunkSize);
          rawIndex += chunkSize;
          x += chunkSize;
        }
      }

      assert length == rawIndex;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("flip", flip)
          .append("width", width)
          .append("height", height)
          .append("xOffset", xOffset)
          .append("yOffset", yOffset)
          .append("allocSize", allocSize)
          .append("nextBlock", nextBlock)
          .append("length", length)
          .append("box", box)
          .toString();
    }
  }
}
