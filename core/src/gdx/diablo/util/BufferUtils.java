package gdx.diablo.util;

import com.google.common.base.Preconditions;

import com.badlogic.gdx.utils.GdxRuntimeException;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class BufferUtils {

  private static final long UINT   = 0xFFFF_FFFFL;
  private static final int  USHORT = 0xFFFF;
  private static final int  UBYTE  = 0xFF;

  public static long readUnsignedInt(ByteBuffer buffer) {
    return buffer.getInt() & UINT;
  }

  public static int readUnsignedShort(ByteBuffer buffer) {
    return buffer.getShort() & USHORT;
  }

  public static int readUnsignedByte(ByteBuffer buffer) {
    return buffer.get() & UBYTE;
  }

  public static String readString(InputStream in) {
    try {
      StringBuilder builder = new StringBuilder(64);
      for (int ch = in.read(); ch != 0; ch = in.read()) {
        builder.append((char) ch);
      }

      return builder.toString();
    } catch (IOException t) {
      throw new GdxRuntimeException("Cannot read string.", t);
    }
  }

  public static String readString(InputStream in, int len) {
    try {
      return new String(IOUtils.readFully(in, len));
    } catch (IOException t) {
      throw new GdxRuntimeException("Cannot read string.", t);
    }
  }

  public static String readString(ByteBuffer buffer) {
    StringBuilder builder = new StringBuilder(64);
    for (byte ch = buffer.get(); ch != 0; ch = buffer.get()) {
      builder.append((char) (ch & 0xFF));
    }

    return builder.toString();
  }

  public static String readString2(ByteBuffer buffer, int len) {
    ByteBuffer bytes = ByteBuffer.wrap(readBytes(buffer, len));
    StringBuilder builder = new StringBuilder(len);
    for (byte ch = bytes.get(); ch != 0; ch = bytes.get()) {
      builder.append((char) (ch & 0xFF));
    }

    return builder.toString();
  }

  public static String readString(ByteBuffer buffer, int len) {
    byte[] chars = readBytes(buffer, len);
    return new String(chars);
  }

  public static byte[] readBytes(ByteBuffer buffer, int len) {
    byte[] bytes = new byte[len];
    buffer.get(bytes);
    return bytes;
  }

  public static ByteBuffer slice(ByteBuffer buffer, int size) {
    ByteBuffer slice = buffer.slice().order(buffer.order());
    slice.limit(size);
    return slice;
  }

  public static ByteBuffer skip(ByteBuffer buffer, int len) {
    buffer.position(buffer.position() + len);
    return buffer;
  }

  public static ByteBuffer slice(ByteBuffer buffer, byte[] MARK) {
    return slice(buffer, MARK, false);
  }

  public static ByteBuffer slice(ByteBuffer buffer, byte[] MARK, boolean skipFirst) {
    Preconditions.checkArgument(MARK.length == 2, "Only supports MARK length of 2");
    int pos = buffer.position();
    buffer.mark();
    int read = 0;
    for (int b0, b1 = 0;;) {
      b0 = buffer.get();
      if (b1 == MARK[0] && b0 == MARK[1]) {
        if (skipFirst) {
          skipFirst = false;
        } else {
          read--;
          break;
        }
      }

      b1 = b0;
      read++;
    }

    buffer.reset();
    ByteBuffer slice = slice(buffer, read);
    buffer.position(pos + read);
    return slice;
  }

  public static boolean lookahead(ByteBuffer buffer, byte[] MARK) {
    buffer.mark();
    byte[] bytes = BufferUtils.readBytes(buffer, MARK.length);
    if (!Arrays.equals(MARK, bytes)) {
      buffer.reset();
      return false;
    }

    return true;
  }

  public static byte[] readRemaining(ByteBuffer buffer) {
    byte[] data = new byte[buffer.remaining()];
    buffer.get(data);
    return data;
  }

  public static int[] readInts(ByteBuffer buffer, int len) {
    int[] ints = new int[len];
    for (int i = 0; i < len; i++) ints[i] = buffer.getInt();
    return ints;
  }

  public static short[] readShorts(ByteBuffer buffer, int len) {
    short[] shorts = new short[len];
    for (int i = 0; i < len; i++) shorts[i] = buffer.getShort();
    return shorts;
  }

  private BufferUtils() {}

}
