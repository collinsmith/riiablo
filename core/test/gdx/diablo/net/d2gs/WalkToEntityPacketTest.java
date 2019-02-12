package gdx.diablo.net.d2gs;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import gdx.diablo.util.DebugUtils;

public class WalkToLocationPacketTest {
  private static final int X = 0xFF;
  private static final int Y = 0xFFFF;
  private static final byte[] BYTES = new byte[]{(byte) 0xFF, 0x00, (byte) 0xFF,  (byte) 0xFF};

  @Test
  public void encode() {
    WalkToLocationPacket packet = new WalkToLocationPacket() {{
      x = X;
      y = Y;
    }};

    ByteArrayOutputStream out = new ByteArrayOutputStream(WalkToLocationPacket.SIZE);
    try {
      packet.encode(out);
    } catch (Throwable t) {
      System.err.println(t.getMessage());
    }

    byte[] bytes = out.toByteArray();
    System.out.println(packet + " -> " + DebugUtils.toByteArray(bytes));
    Assert.assertArrayEquals(BYTES, bytes);
  }

  @Test
  public void decode() {
    WalkToLocationPacket packet = new WalkToLocationPacket();
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(BYTES);
      packet.decode(in);
    } catch (Throwable t) {
      System.out.println(t.getMessage());
    }

    System.out.println(DebugUtils.toByteArray(BYTES) + " -> " + packet);
    Assert.assertEquals(X, packet.x);
    Assert.assertEquals(Y, packet.y);
  }
}