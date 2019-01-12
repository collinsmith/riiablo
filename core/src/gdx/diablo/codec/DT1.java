package gdx.diablo.codec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import gdx.diablo.graphics.PaletteIndexedPixmap;
import gdx.diablo.util.BufferUtils;

// TODO: In production, skip unused data (zeros/unknowns)
public class DT1 {
  private static final String TAG = "DT1";
  private static final boolean DEBUG               = true;
  private static final boolean DEBUG_TILE_HEADERS  = DEBUG && false;
  private static final boolean DEBUG_BLOCK_HEADERS = DEBUG && false;

  String fileName;
  Header header;
  Tile   tiles[];

  private DT1(String fileName, Header header, Tile[] tiles) {
    this.fileName = fileName;
    this.header = header;
    this.tiles  = tiles;
  }

  private static final int xjump[] = {14, 12, 10, 8, 6, 4, 2, 0, 2, 4, 6, 8, 10, 12, 14};
  private static final int nbpix[] = {4, 8, 12, 16, 20, 24, 28, 32, 28, 24, 20, 16, 12, 8, 4};

  public Pixmap tile(int i) {
    Tile tile = tiles[i];

    int width  = tile.width;
    int height = -tile.height;

    if (tile.orientation == 10 || tile.orientation == 11) {
      width = 160;
    }

    int y_add = 96;
    if (tile.orientation == 0 || tile.orientation == 15) {
      if (tile.height != 0) {
        tile.height = -80;
        height = 80;
        y_add = 0;
      }
    } else if (tile.orientation < 15) {
      if (tile.height != 0) {
        tile.height += 32;
        height -= 32;
        y_add = height;
      }
    }

    int x0, y0, length, format;
    byte[] data;

    Pixmap pixmap = new PaletteIndexedPixmap(width, height);
    for (Block block : tile.blocks) {
      x0     = block.x;
      y0     = y_add + block.y;
      data   = block.colormap;
      length = block.length;
      format = block.format;
      if (format == 0x0001) {
        drawIsometricBlock(pixmap, x0, y0, data, length);
      } else {
        drawRLEBlock(pixmap, x0, y0, data, length);
      }
    }

    return pixmap;
  }

  /*
  public Pixmap tile(int i) {
    Cell tile = tiles[i];

    int y0;
    final int height = Math.abs(tile.height);
    DC6.Pixmap pixmap = new DC6.Pixmap(tile.width, height);
    for (Block block : tile.blocks) {
      Gdx.app.debug(TAG, block.toString());
      // TODO: negative coordinate system is fucking things up...
      if (block.format == 1) {
        //System.out.println("y0 = " + block.y);
        drawIsometricBlock(pixmap, block.x, block.y, block.colormap, block.length);
      } else {
        //y0 = height + block.y;
        drawRLEBlock(pixmap, block.x, block.y, block.colormap, block.length);
      }
    }

    return pixmap;
  }
  */

  private void drawIsometricBlock(Pixmap pixmap, int x0, int y0, byte[] data, int length) {
    if (length != 256) {
      Gdx.app.error(TAG, "Blocks should be 256 bytes, was " + length);
      return;
    }

    int x, y = 0, n, i = 0;
    while (length > 0) {
      x = xjump[y];
      n = nbpix[y];
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

  private void drawRLEBlock(Pixmap pixmap, int x0, int y0, byte[] data, int length) {
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

  public static DT1 loadFromFile(FileHandle file) {
    return loadFromStream(file.path(), file.read());
  }

  public static DT1 loadFromStream(String fileName, InputStream in) {
    try {
      Header header = new Header(in);
      if (DEBUG) Gdx.app.debug(TAG, header.toString());

      if (header.version1 != 0x7)
        throw new GdxRuntimeException("Cannot read. Unknown version1: " + header.version1);
      if (header.version2 != 0x6)
        throw new GdxRuntimeException("Cannot read. Unknown version2: " + header.version2);

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

  static class Tile {
    static final int SIZE = 96;

    public static final int FLAG_BLOCK_WALK        = 1 << 0;
    public static final int FLAG_BLOCK_LIGHT_LOS   = 1 << 1;
    public static final int FLAG_BLOCK_JUMP        = 1 << 2;
    public static final int FLAG_BLOCK_PLAYER_WALK = 1 << 3;
    public static final int FLAG_BLOCK_UNKNOWN1    = 1 << 4;
    public static final int FLAG_BLOCK_LIGHT       = 1 << 5;
    public static final int FLAG_BLOCK_UNKNOWN2    = 1 << 6;
    public static final int FLAG_BLOCK_UNKNOWN3    = 1 << 7;

    int   direction;
    short roofHeight;
    byte  soundIndex;
    byte  animated;
    int   height;
    int   width;
    byte  zeros1[];
    int   orientation;
    int   mainIndex;
    int   subIndex;
    int   rarity; // frame index if animated
    byte  unknown[];
    byte  tileFlags[];
    byte  zeros2[];
    int   blockHeadersPointer;
    int   blockDataLength;
    int   numBlocks;
    byte  zeros3[];

    Block blocks[];

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
      tileFlags   = BufferUtils.readBytes(buffer, 25);
      zeros2      = BufferUtils.readBytes(buffer, 7);
      blockHeadersPointer = buffer.getInt();
      blockDataLength     = buffer.getInt();
      numBlocks   = buffer.getInt();
      zeros3      = BufferUtils.readBytes(buffer, 12);
      assert !buffer.hasRemaining();
      assert allZeros(zeros1) : "Expected 4 zeros, got: " + Arrays.toString(zeros1);
      assert allZeros(zeros2) : "Expected 7 zeros, got: " + Arrays.toString(zeros2);
      //assert allZeros(zeros3) : "Expected 12 zeros, got: " + Arrays.toString(zeros3);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
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
          .append("tileFlags", Arrays.toString(tileFlags))
          //.append("zeros2", Arrays.toString(zeros2))
          .append("blockHeadersPointer", "0x" + Integer.toHexString(blockHeadersPointer))
          .append("blockDataLength", blockDataLength)
          .append("numBlocks", numBlocks)
          //.append("zeros3", Arrays.toString(zeros3))
          .toString();
    }
  }

  static class Block {
    static final int SIZE = 20;

    short x;
    short y;
    byte  zeros1[];
    byte  gridX;
    byte  gridY;
    short format;
    int   length;
    byte  zeros2[];
    int   fileOffset;

    byte  colormap[];

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
