package gdx.diablo.net.d2gs;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

import java.io.IOException;

import gdx.diablo.net.Codec;

public class WalkToLocationPacket extends Codec {
  static final int SIZE = 4;

  public int x;
  public int y;

  WalkToLocationPacket() {}

  @Override
  public void encode(LittleEndianDataOutputStream out) throws IOException {
    out.writeShort(x);
    out.writeShort(y);
  }

  @Override
  public void decode(LittleEndianDataInputStream in) throws IOException {
    x = in.readUnsignedShort();
    y = in.readUnsignedShort();
  }

  @Override
  public String toString() {
    return x + ", " + y;
  }
}
