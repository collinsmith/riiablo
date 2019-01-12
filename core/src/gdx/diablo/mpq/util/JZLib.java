package gdx.diablo.mpq.util;

import com.jcraft.jzlib.Inflater;

import java.nio.ByteBuffer;

public class JZLib {
  private JZLib() {}

  private static Inflater inflater = new Inflater();

  public static void inflate(ByteBuffer in, ByteBuffer out, int FSize) {
    assert out.remaining() == FSize;
    final int CSize = in.remaining();
    inflater.init();
    inflater.setInput(in.array(), in.arrayOffset(), CSize, false);
    inflater.setOutput(out.array(), out.arrayOffset(), FSize);
    while (inflater.total_out < FSize && inflater.total_in < CSize) {
      inflater.avail_in = inflater.avail_out = 1;
      int err = inflater.inflate(0);
      if (err == 1) {
        break;
      }
    }

    inflater.end();
  }

}
