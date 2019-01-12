package gdx.diablo.codec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;

import gdx.diablo.graphics.PaletteIndexedPixmap;
import gdx.diablo.util.DebugUtils;

public class Index implements Disposable {
  private static final String TAG = "Index";
  private static final boolean DEBUG = !true;

  public static final int INDEXES = 22;

  private static final byte[] NONE;
  static {
    NONE = new byte[Palette.COLORS];
    for (int i = 0; i < Palette.COLORS; i++) NONE[i] = (byte) i;
  }

  public final String  name;
  public final byte    indexes[][];

  public Texture texture;

  private Index(String name, byte[][] indexes) {
    this.name = name;
    this.indexes = indexes;
  }

  @Override
  public void dispose() {
    if (texture != null) texture.dispose();
  }

  @Override
  public String toString() {
    return name != null ? name : super.toString();
  }

  public byte[] get(int index) {
    return indexes[index];
  }

  public Index render() {
    if (texture != null) return this;
    PaletteIndexedPixmap indexPixmap = new PaletteIndexedPixmap(Palette.COLORS, INDEXES);
    ByteBuffer buffer = indexPixmap.getPixels();
    for (int i = 0, j = 0; i < INDEXES; i++) {
      buffer.put(indexes[i]);
      //for (int k = 0; k < Palette.COLORS; k++, j += 4) {
      //  buffer.put(j, indexes[i][k]);
      //}
    }

    buffer.rewind();
    Texture texture = new Texture(new PixmapTextureData(indexPixmap, null, false, false, false));
    //Texture texture = new Texture(indexPixmap);
    //texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    indexPixmap.dispose();
    this.texture = texture;
    return this;
  }

  public static Index loadFromStream(InputStream in) {
    try {
      byte[] data = new byte[(INDEXES - 1) * Palette.COLORS];
      IOUtils.readFully(in, data);
      return loadFromArray(null, data);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load palette from stream.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  public static Index loadFromFile(FileHandle file) {
    byte[] data = file.readBytes();
    return loadFromArray(file.toString(), data);
  }

  private static Index loadFromArray(String name, byte[] data) {
    byte[][] indexes = new byte[INDEXES][Palette.COLORS];
    indexes[0] = NONE;
    for (int i = 1; i < INDEXES; i++) {
      System.arraycopy(data, (i - 1) * Palette.COLORS, indexes[i], 0, Palette.COLORS);
    }

    if (DEBUG) {
      Gdx.app.debug(TAG, INDEXES + "x" + Palette.COLORS + " colors");
      for (int i = 0; i < INDEXES; i++) {
        Gdx.app.debug(TAG, String.format("indexes[%02d] = %s", i, DebugUtils.toByteArray(indexes[i])));
      }
    }

    return new Index(name, indexes);
  }
}
