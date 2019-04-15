package com.riiablo.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.riiablo.codec.Palette;
import com.riiablo.graphics.PaletteIndexedPixmap;
import com.riiablo.util.BufferUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

// TODO: In production, skip unused data (zeros/unknowns)
public class DT1 implements Disposable {
  private static final String TAG = "DT1";
  private static final boolean DEBUG               = true;
  private static final boolean DEBUG_TILE_HEADERS  = DEBUG && !true;
  private static final boolean DEBUG_BLOCK_HEADERS = DEBUG && !true;

  private static final int X_JUMP[] = { 14, 12, 10, 8, 6, 4, 2, 0, 2, 4, 6, 8, 10, 12, 14 };
  private static final int PIXEL_WIDTH[] = { 4, 8, 12, 16, 20, 24, 28, 32, 28, 24, 20, 16, 12, 8, 4 };

  String  fileName;
  Header  header;
  Tile    tiles[];
  Texture textures[];

  private DT1(String fileName, Header header, Tile[] tiles) {
    this.fileName = fileName;
    this.header   = header;
    this.tiles    = tiles;
  }

  @Override
  public void dispose() {
    if (textures == null) return;
    for (Texture texture : textures) texture.dispose();
  }

  public void prepareTextures() {
    Validate.validState(textures == null, "textures have already been prepared");
    textures = new Texture[header.numTiles];
    for (int i = 0; i < header.numTiles; i++) {
      Texture texture = new Texture(new PixmapTextureData(tiles[i].pixmap, null, false, false, false));
      //texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
      tiles[i].texture = new TextureRegion(textures[i] = texture);
    }
  }

  public static DT1 loadFromFile(FileHandle handle) {
    return loadFromStream(handle.path(), handle.read());
  }

  public static DT1 loadFromStream(String fileName, InputStream in) {
    try {
      Header header = new Header(in);
      if (DEBUG) Gdx.app.debug(TAG, header.toString());

      if (header.version1 != 0x7)
        throw new GdxRuntimeException("Unknown version1: " + header.version1);
      if (header.version2 != 0x6)
        throw new GdxRuntimeException("Unknown version2: " + header.version2);

      Tile[] tiles = new Tile[header.numTiles];
      for (int i = 0; i < header.numTiles; i++) {
        Tile tile = tiles[i] = new Tile(in);
        if (DEBUG_TILE_HEADERS) Gdx.app.debug(TAG, tile.toString());
      }

      for (Tile tile : tiles) {
        Block[] blockHeaders = tile.blocks = new Block[tile.numBlocks];
        for (int i = 0; i < tile.numBlocks; i++) {
          blockHeaders[i] = new Block(in);
          if (DEBUG_BLOCK_HEADERS) Gdx.app.debug(TAG, blockHeaders[i].toString());
        }

        for (Block blockHeader : blockHeaders) {
          blockHeader.colormap = IOUtils.readFully(in, blockHeader.length);
        }

        tile.createPixmap();
      }

      assert in.available() == 0;
      return new DT1(fileName, header, tiles);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't read DT1", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  private static boolean allZeros(byte[] zeros) {
    for (byte b : zeros) {
      if (b != 0) return false;
    }

    return true;
  }

  static class Header {
    static final int SIZE = 276;

    int  version1;
    int  version2;
    byte zeros[];
    int  numTiles;
    int  tileOffset;

    Header(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      version1   = buffer.getInt();
      version2   = buffer.getInt();
      zeros      = BufferUtils.readBytes(buffer, 260);
      numTiles   = buffer.getInt();
      tileOffset = buffer.getInt();
      assert !buffer.hasRemaining();
      assert allZeros(zeros) : "Expected 260 zeros, got: " + Arrays.toString(zeros);
      assert tileOffset == SIZE : "Expected first tile header to be located at 276: " + tileOffset;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("version1", version1)
          .append("version2", version2)
          //.append("zeros", Arrays.toString(zeros))
          .append("numTiles", numTiles)
          .append("tileOffset", "0x" + Integer.toHexString(tileOffset))
          .toString();
    }
  }

  public static class Tile {
    static final int SIZE = 96;

    public static final int WIDTH    = 160;
    public static final int HEIGHT   = 80;
    public static final int WIDTH50  = WIDTH  / 2;
    public static final int HEIGHT50 = HEIGHT / 2;

    public static final int SUBTILE_SIZE = 5;
    public static final int NUM_SUBTILES = SUBTILE_SIZE * SUBTILE_SIZE;

    public static final int SUBTILE_WIDTH    = WIDTH  / SUBTILE_SIZE;
    public static final int SUBTILE_HEIGHT   = HEIGHT / SUBTILE_SIZE;
    public static final int SUBTILE_WIDTH50  = SUBTILE_WIDTH  / 2;
    public static final int SUBTILE_HEIGHT50 = SUBTILE_HEIGHT / 2;

    public static final GridPoint2 SUBTILE_CENTER = new GridPoint2(SUBTILE_SIZE / 2, SUBTILE_SIZE / 2);

    public static final int[][] SUBTILE_INDEX/* = {
        {0, 5, 10, 15, 20},
        {1, 6, 11, 16, 21},
        {2, 7, 12, 17, 22},
        {3, 8, 13, 18, 23},
        {4, 9, 14, 19, 24},
    }*/;
    static {
      SUBTILE_INDEX = new int[SUBTILE_SIZE][SUBTILE_SIZE];
      for (int y = 0, i = 0; y < SUBTILE_SIZE; y++) {
        for (int x = 0; x < SUBTILE_SIZE; x++, i++) {
          SUBTILE_INDEX[x][y] = i;
        }
      }
    }

    public static final int[][] SUBTILE_OFFSET/* = {
        {64, 64}, {80, 56}, {96, 48}, {112, 40}, {128, 32},
        {48, 56}, {64, 48}, {80, 40}, { 96, 32}, {112, 24},
        {32, 48}, {48, 40}, {64, 32}, { 80, 24}, { 96, 16},
        {16, 40}, {32, 32}, {48, 24}, { 64, 16}, { 80,  8},
        { 0, 32}, {16, 24}, {32, 16}, { 48,  8}, { 64,  0},
    }*/;
    static {
      SUBTILE_OFFSET = new int[NUM_SUBTILES][2];
      for (int y = 0, i = 0; y < SUBTILE_SIZE; y++) {
        int px = (WIDTH / 2) - SUBTILE_WIDTH50 - (y * SUBTILE_WIDTH50);
        int py = HEIGHT      - SUBTILE_HEIGHT  - (y * SUBTILE_HEIGHT50);
        for (int x = 0; x < SUBTILE_SIZE; x++, i++) {
          SUBTILE_OFFSET[i][0] = px;
          SUBTILE_OFFSET[i][1] = py;
          px += SUBTILE_WIDTH50;
          py -= SUBTILE_HEIGHT50;
        }
      }
    }

    public static final int FLAG_BLOCK_WALK        = 1 << 0;
    public static final int FLAG_BLOCK_LIGHT_LOS   = 1 << 1;
    public static final int FLAG_BLOCK_JUMP        = 1 << 2;
    public static final int FLAG_BLOCK_PLAYER_WALK = 1 << 3;
    public static final int FLAG_BLOCK_UNKNOWN1    = 1 << 4;
    public static final int FLAG_BLOCK_LIGHT       = 1 << 5;
    public static final int FLAG_BLOCK_UNKNOWN2    = 1 << 6;
    public static final int FLAG_BLOCK_UNKNOWN3    = 1 << 7;

    public int   direction;
    public short roofHeight;
    public byte  soundIndex;
    public byte  animated;
    public int   height;
    public int   width;
    public byte  zeros1[];
    public int   orientation;
    public int   mainIndex;
    public int   subIndex;
    public int   rarity; // frame index if animated
    public byte  unknown[];
    public byte  flags[];
    public byte  zeros2[];
    public int   blockHeadersPointer;
    public int   blockDataLength;
    public int   numBlocks;
    public byte  zeros3[];

    int           id;
    Block         blocks[];
    Pixmap        pixmap;
    TextureRegion texture;

    /**
     * zeros3 is not all zeros all of the time. This might correlate with unknown. I need to figure
     * out what these variables mean, I'm fairly certain zeros3 is 3 ints, or at least the second
     * 4 bytes are used somewhere.
     *
     * data\global\tiles\expansion\Town\shrine.dt1
     * data\global\tiles\expansion\Town\trees.dt1
     *
     * unknown in above is same F6 62 FF 00  (16737014)
     * but zeros3 is different:
     * 00 00 00 00 00 00 00 00 00 00 00 00
     * 00 00 00 00 6C B0 08 0B 00 00 00 00
     *
     * data\global\tiles\expansion\Town\tent.dt1
     * F3 65 FF 00  (16737779)
     * 00 00 00 00 4C 00 37 0B 00 00 00 00
     */
    Tile(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      direction   = buffer.getInt();
      roofHeight  = buffer.getShort();
      soundIndex  = buffer.get();
      animated    = buffer.get();
      height      = buffer.getInt();
      width       = buffer.getInt();
      zeros1      = BufferUtils.readBytes(buffer, 4);
      orientation = buffer.getInt();
      mainIndex   = buffer.getInt();
      subIndex    = buffer.getInt();
      rarity      = buffer.getInt();
      unknown     = BufferUtils.readBytes(buffer, 4);
      flags       = BufferUtils.readBytes(buffer, NUM_SUBTILES);
      zeros2      = BufferUtils.readBytes(buffer, 7);
      blockHeadersPointer = buffer.getInt();
      blockDataLength     = buffer.getInt();
      numBlocks   = buffer.getInt();
      zeros3      = BufferUtils.readBytes(buffer, 12);
      id          = Index.create(orientation, mainIndex, subIndex);
      assert !buffer.hasRemaining();
      assert allZeros(zeros1) : "Expected 4 zeros, got: " + Arrays.toString(zeros1);
      assert allZeros(zeros2) : "Expected 7 zeros, got: " + Arrays.toString(zeros2);
      //assert allZeros(zeros3) : "Expected 12 zeros, got: " + Arrays.toString(zeros3);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("id", String.format("%08x", id))
          .append("direction", direction)
          .append("roofHeight", roofHeight)
          .append("soundIndex", soundIndex)
          .append("animated", animated)
          .append("height", height)
          .append("width", width)
          //.append("zeros1", Arrays.toString(zeros1))
          .append("orientation", orientation)
          .append("mainIndex", mainIndex)
          .append("subIndex", subIndex)
          .append("rarity", rarity)
          .append("unknown", Arrays.toString(unknown))
          .append("flags", Arrays.toString(flags))
          //.append("zeros2", Arrays.toString(zeros2))
          .append("blockHeadersPointer", "0x" + Integer.toHexString(blockHeadersPointer))
          .append("blockDataLength", blockDataLength)
          .append("numBlocks", numBlocks)
          //.append("zeros3", Arrays.toString(zeros3))
          .toString();
    }

    public boolean isFloor()   { return Orientation.isFloor(orientation); }
    public boolean isWall()    { return Orientation.isWall(orientation); }
    public boolean isRoof()    { return Orientation.isRoof(orientation); }
    public boolean isSpecial() { return Orientation.isSpecial(orientation); }

    public void createPixmap() {
      Validate.validState(pixmap == null, "pixmap should be null");
      int absWidth  =  width;
      int absHeight = -height;

      if (isSpecial()) {
        absWidth = WIDTH;
      }

      int y_add = 96;
      if (orientation == Orientation.FLOOR || orientation == Orientation.ROOF) {
        if (height != 0) {
          height    = -80;
          absHeight =  80;
          y_add = 0;
        }
      } else if (orientation < Orientation.ROOF) {
        if (height != 0) {
          height    += 32;
          absHeight -= 32;
          y_add = absHeight;
        }
      }

      int x0, y0, length, format;
      byte[] data;

      pixmap = new PaletteIndexedPixmap(absWidth, absHeight);
      for (Block block : blocks) {
        x0     = block.x;
        y0     = y_add + block.y;
        data   = block.colormap;
        length = block.length;
        format = block.format;
        if (format == 0x0001) {
          drawIsometricBlock(x0, y0, data, length);
        } else {
          drawRLEBlock(x0, y0, data, length);
        }
      }
    }

    private void drawIsometricBlock(int x0, int y0, byte[] data, int length) {
      if (length != 256) {
        Gdx.app.error(TAG, "Blocks should be 256 bytes, was " + length);
        return;
      }

      int x, y = 0, n, i = 0;
      while (length > 0) {
        x = X_JUMP[y];
        n = PIXEL_WIDTH[y];
        length -= n;
        while (n > 0) {
          pixmap.drawPixel(x0 + x, y0 + y, Palette.a8(data[i]));
          i++;
          x++;
          n--;
        }
        y++;
      }
    }

    private void drawRLEBlock(int x0, int y0, byte[] data, int length) {
      int i = 0, b1, b2;
      int x = 0, y = 0;
      while (length > 0) {
        b1 = data[i] & 0xFF;
        b2 = data[i + 1] & 0xFF;
        i += 2;
        length -= 2;
        if (b1 > 0 || b2 > 0) {
          x += b1;
          length -= b2;
          while (b2 > 0) {
            pixmap.drawPixel(x0 + x, y0 + y, Palette.a8(data[i]));
            i++;
            x++;
            b2--;
          }
        } else {
          x = 0;
          y++;
        }
      }
    }

    public static class Index {
      private static final int MAIN_INDEX_OFFSET  = 16;
      private static final int MAIN_INDEX_BITS    = 0xFF;

      private static final int SUB_INDEX_OFFSET   = 8;
      private static final int SUB_INDEX_BITS     = 0xFF;

      private static final int ORIENTATION_OFFSET = 0;
      private static final int ORIENTATION_BITS   = 0xFF;

      public static int create(int orientation, int mainIndex, int subIndex) {
        return (mainIndex   & MAIN_INDEX_BITS)  << MAIN_INDEX_OFFSET
             | (subIndex    & SUB_INDEX_BITS)   << SUB_INDEX_OFFSET
             | (orientation & ORIENTATION_BITS) << ORIENTATION_OFFSET;
      }

      public static int mainIndex(int index)   { return (index >>> MAIN_INDEX_OFFSET)  & MAIN_INDEX_BITS; }
      public static int subIndex(int index)    { return (index >>> SUB_INDEX_OFFSET)   & SUB_INDEX_BITS; }
      public static int orientation(int index) { return (index >>> ORIENTATION_OFFSET) & ORIENTATION_BITS; }
    }
  }

  public static class Block {
    static final int SIZE = 20;

    public short x;
    public short y;
    public byte  zeros1[];
    public byte  gridX;
    public byte  gridY;
    public short format;
    public int   length;
    public byte  zeros2[];
    public int   fileOffset;

    public byte  colormap[];

    Block(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      x          = buffer.getShort();
      y          = buffer.getShort();
      zeros1     = BufferUtils.readBytes(buffer, 2);
      gridX      = buffer.get();
      gridY      = buffer.get();
      format     = buffer.getShort();
      length     = buffer.getInt();
      zeros2     = BufferUtils.readBytes(buffer, 2);
      fileOffset = buffer.getInt();
      assert !buffer.hasRemaining();
      assert allZeros(zeros1) : "Expected 2 zeros, got: " + Arrays.toString(zeros1);
      assert allZeros(zeros2) : "Expected 2 zeros, got: " + Arrays.toString(zeros2);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("x", x)
          .append("y", y)
          //.append("zeros1", Arrays.toString(zeros1))
          .append("gridX", gridX)
          .append("gridY", gridY)
          .append("format", format)
          .append("length", length)
          //.append("zeros2", Arrays.toString(zeros2))
          .append("fileOffset", "0x" + Integer.toHexString(fileOffset))
          .toString();
    }
  }
}
