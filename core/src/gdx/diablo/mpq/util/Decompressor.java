package gdx.diablo.mpq.util;

import com.badlogic.gdx.Gdx;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class Decompressor {
  private Decompressor() {}

  private static final String TAG = "Decompressor";
  private static final boolean DEBUG = false;

  /* Masks for Decompression Type 2 */
  private static final byte FLAG_HUFFMAN = 0x01;
  public  static final byte FLAG_DEFLATE = 0x02;
  // 0x04 is unknown
  private static final byte FLAG_IMPLODE = 0x08;
  private static final byte FLAG_BZIP2   = 0x10;
  private static final byte FLAG_SPARSE  = 0x20;
  private static final byte FLAG_ADPCM1C = 0x40;
  private static final byte FLAG_ADPCM2C = -0x80;
  private static final byte FLAG_LMZA    = 0x12;

  private static final byte ADPCM_MASK   = FLAG_ADPCM1C | FLAG_ADPCM2C;

  private static AtomicReference<Huffman> huffman = new AtomicReference<>();

  public static void decompress(ByteBuffer sector, ByteBuffer buffer, ByteBuffer scratch, int CSize, int FSize) {
    if (CSize == FSize) {
      buffer.put(sector);
      buffer.rewind();
    } else {
      scratch.clear().limit(FSize);
      assert buffer.remaining() == FSize;
      byte compressionFlags = sector.get();
      if (DEBUG) Gdx.app.debug(TAG, "compressionFlags = 0x" + Integer.toHexString(compressionFlags & 0xFF));

      boolean flip = false;
      if ((compressionFlags & FLAG_DEFLATE) == FLAG_DEFLATE) {
        /*JZLib.inflate(sector, buffer, FSize);
        Gdx.app.debug(TAG, "Inflated to " + buffer.position() + " bytes");
        buffer.rewind();
        flip = !flip;*/
        throw new UnsupportedOperationException("FLAG_DEFLATE");
      } else if ((compressionFlags & FLAG_LMZA) == FLAG_LMZA) {
        throw new UnsupportedOperationException("FLAG_LMZA");
      } else if ((compressionFlags & FLAG_BZIP2) == FLAG_BZIP2) {
        throw new UnsupportedOperationException("FLAG_BZIP2");
      } else if ((compressionFlags & FLAG_IMPLODE) == FLAG_IMPLODE) {
        Exploder.pkexplode(sector, buffer);
        sector.rewind().position(1);
        if (DEBUG) Gdx.app.debug(TAG, "Exploded to " + buffer.position() + " bytes");
        buffer.rewind();
        flip = !flip;
      }

      if ((compressionFlags & FLAG_SPARSE) == FLAG_SPARSE) {
        throw new UnsupportedOperationException("FLAG_SPARSE");
      }

      if ((compressionFlags & FLAG_HUFFMAN) == FLAG_HUFFMAN) {
        huffman.compareAndSet(null, new Huffman());
        if (flip) {
          sector.clear();
          huffman.get().decompress(buffer, sector);
        } else {
          buffer.clear();
          huffman.get().decompress(sector, buffer);
        }

        sector.rewind();
        buffer.flip(); // TODO: This doesn't make any sense for flip == true case
        flip = !flip;
      }

      if ((compressionFlags & ADPCM_MASK) != 0) {
        int channels = ((compressionFlags & FLAG_ADPCM1C) == FLAG_ADPCM1C) ? 1 : 2;
        if (flip) {
          ADPCM.decompress(buffer, scratch, channels);
          buffer.rewind();
          scratch.rewind();
        } else {
          ADPCM.decompress(sector, scratch, channels);
          sector.rewind();
          scratch.rewind();
        }

        buffer.limit(FSize);
        buffer.put(scratch).rewind();
      }
    }
  }
}
