package com.riiablo.codec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.text.StringEscapeUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.PaletteIndexedPixmap;
import com.riiablo.util.BufferUtils;

public class FontTBL {
  private static final String TAG = "FontTBL";
  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_CHARS = DEBUG && false;

  public static final int CHARS = 256;
  private static final int CHAR_SHEET_PADDING = 2;

  final Header   header;
  final CharData cData[];

  private FontTBL(Header header, CharData[] cData) {
    this.header = header;
    this.cData  = cData;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("header", header)
        .append("cData", cData)
        .toString();
  }

  public BitmapFontData data(com.riiablo.codec.DC6 dc6) {
    return new BitmapFontData(dc6);
  }

  public class BitmapFontData extends com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData {
    final  com.riiablo.codec.DC6 dc6;
    final  Pixmap               fontSheet;
    public int                  blendMode;
    BitmapFontData(DC6 dc6) {
      this.dc6 = dc6;
      fontSheet = createFontSheet();
      BBox box = dc6.directions[0].box;
      final int charWidth = box.width;
      final int charHeight = box.height;
      padLeft = padTop = padRight = padBottom = 0;
      lineHeight = xHeight = capHeight = charHeight;
      descent = 0;
      for (char c = 0; c < CHARS; c++) {
        BitmapFont.Glyph glyph = new BitmapFont.Glyph();
        setGlyph(c, glyph);
        if (missingGlyph == null) missingGlyph = glyph;

        glyph.id = c;

        glyph.srcX = (c % 16) * (charWidth  + CHAR_SHEET_PADDING);
        glyph.srcY = (c / 16) * (charHeight + CHAR_SHEET_PADDING);

        CharData cData = FontTBL.this.cData[c];
        glyph.width = Math.min(cData.width + 1, charWidth);
        glyph.height = charHeight; // this was  {@code charHeight - 1} before, maybe because of no glyph padding in the backing texture
        glyph.yoffset = -(2 * glyph.height);
        glyph.xadvance = cData.width;

        // This was messing with
        //if (glyph.width > 0 && glyph.height > 0 && glyph.yoffset < descent) descent = glyph.yoffset;
      }
      descent += padBottom;
      ascent = capHeight;
      down = -lineHeight;
    }

    Pixmap createFontSheet() {
      DC.Direction dir = dc6.getDirection(0);
      final int columnWidth = dir.box.width + CHAR_SHEET_PADDING;
      final int columnHeight = dir.box.height + CHAR_SHEET_PADDING;

      final int columns = 16;
      final int rows = 16;
      final int width = columnWidth * columns;
      final int height = columnHeight * rows;

      Pixmap sheet = new PaletteIndexedPixmap(width, height);

      int f = 0, x = 0, y = 0;
      for (int r = 0; r < rows; r++) {
        for (int c = 0; c < columns; c++) {
          Pixmap frame = dc6.getPixmap(0, f++);
          sheet.drawPixmap(frame, x, y);
          x += columnWidth;
        }

        x = 0;
        y += columnHeight;
      }

      return sheet;
    }
  }

  public static class BitmapFont extends com.badlogic.gdx.graphics.g2d.BitmapFont {
    private int blendMode;

    public BitmapFont(FontTBL.BitmapFontData data) {
      super(data, new TextureRegion(new Texture(new PixmapTextureData(data.fontSheet, null, false, true, false))), true);
      blendMode = data.blendMode;
      setOwnsTexture(true);
    }

    public int getBlendMode() {
      return blendMode;
    }

    public void setBlendMode(int blendMode) {
      this.blendMode = blendMode;
    }
  }

  public static FontTBL loadFromFile(FileHandle file) {
    return loadFromArray(file.readBytes());
  }

  public static FontTBL loadFromArray(byte[] data) {
    return loadFromStream(new ByteArrayInputStream(data));
  }

  public static FontTBL loadFromStream(InputStream in) {
    try {
      Header header = new Header(in);
      if (DEBUG) Gdx.app.debug(TAG, header.toString());

      if (!header.id.equals("Woo!"))
        throw new GdxRuntimeException("Not a valid TBL file header: " + StringEscapeUtils.escapeJava(header.id));
      if (header.one != 0x01)
        throw new GdxRuntimeException("Not a valid TBL file version: " + header.one);

      CharData[] cData = new CharData[CHARS];
      for (int i = 0; i < CHARS; i++) {
        CharData c = cData[i] = new CharData(in);
        if (DEBUG_CHARS) Gdx.app.debug(TAG, c.toString());
        assert c.wChar == i;
        assert c.bTrue == 1;
      }

      assert in.available() == 0;
      return new FontTBL(header, cData);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load font TBL from stream.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  static class Header {
    static final int SIZE = 12;

    String id;     // 4
    short  one;    // 2
    int    locale; // 4
    int    height; // 1
    int    width;  // 1

    Header(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      id     = BufferUtils.readString(buffer, 4);
      one    = buffer.getShort();
      locale = buffer.get() << 24 | buffer.get() << 16 | buffer.get() << 8 | buffer.get();
      height = BufferUtils.readUnsignedByte(buffer);
      width  = BufferUtils.readUnsignedByte(buffer);
      assert !buffer.hasRemaining();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("id", StringEscapeUtils.escapeJava(id))
          .append("one", Integer.toHexString(one))
          .append("locale", Integer.toHexString(locale))
          .append("height", height)
          .append("width", width)
          .toString();
    }
  }
  static class CharData {
    static final int SIZE = 14;

    char  wChar;      // 2
    byte  nUnk;       // 1
    int   width;      // 1
    int   height;     // 1
    byte  bTrue;      // 1
    short wUnkEx;     // 2
    int   imageIndex; // 1
    int   nChar;      // 1
    int   dwUnk;      // 4

    CharData(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      wChar      = (char) BufferUtils.readUnsignedShort(buffer);
      nUnk       = buffer.get();
      width      = BufferUtils.readUnsignedByte(buffer);
      height     = BufferUtils.readUnsignedByte(buffer);
      bTrue      = buffer.get();
      wUnkEx     = buffer.getShort();
      imageIndex = BufferUtils.readUnsignedByte(buffer);
      nChar      = BufferUtils.readUnsignedByte(buffer);
      dwUnk      = buffer.getInt();
      assert !buffer.hasRemaining();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("wChar", StringEscapeUtils.escapeJava(Character.toString(wChar)))
          //.append("nUnk", Integer.toHexString(nUnk))
          .append("width", width)
          .append("height", height)
          //.append("bTrue", bTrue)
          //.append("wUnkEx", Integer.toHexString(wUnkEx))
          .append("imageIndex", imageIndex)
          .append("nChar", nChar)
          //.append("dwUnk", Integer.toHexString(dwUnk))
          .toString();
    }
  }
}
