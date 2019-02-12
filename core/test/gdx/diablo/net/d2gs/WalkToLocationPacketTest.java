package gdx.diablo.net.d2gs;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import gdx.diablo.util.DebugUtils;

public class MoveToLocationPacketTest {

  @Test
  public void encode() {
    MoveToLocationPacket packet = new MoveToLocationPacket() {{
      x = 31;
      y = 63;
    }};

    ByteArrayOutputStream out = new ByteArrayOutputStream(MoveToLocationPacket.SIZE);
    try {
      packet.encode(out);
    } catch (Throwable t) {
      System.err.println(t.getMessage());
    }

    byte[] bytes = out.toByteArray();
    System.out.println(DebugUtils.toByteArray(bytes));
    Assert.assertArrayEquals(new byte[]{0x1F, 0x00, 0x3F, 0x00}, bytes);
  }

  @Test
  public void decode() {
    MoveToLocationPacket packet = new MoveToLocationPacket();
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{0x1F, 0x00, 0x3F, 0x00});
      packet.decode(in);
    } catch (Throwable t) {
      System.out.println(t.getMessage());
    }

    System.out.println(packet);
    Assert.assertEquals(31, packet.x);
    Assert.assertEquals(63, packet.y);
  }
}