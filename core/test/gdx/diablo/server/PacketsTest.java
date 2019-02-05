package gdx.diablo.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.utils.Json;

import org.junit.Assert;
import org.junit.Test;

import gdx.diablo.Diablo;
import gdx.diablo.codec.StringTBLs;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class PacketsTest {

  @Test
  public void getType() {
    int type = Packets.getType("{ \"type\": 1, \"version\": 1, \"data\": { \"name\": \"Tirant\", \"text\": \"Hello World!\" } }");
    Assert.assertEquals(Packets.MESSAGE, type);
  }

  @Test
  public void parsePacketType() {
    Packet packet = Packets.parse("{ \"type\": 1, \"version\": 1, \"data\": { \"name\": \"Tirant\", \"text\": \"Hello World!\" } }");
    Assert.assertEquals(Packets.MESSAGE, packet.type);
  }

  @Test
  public void parsePacketVersion() {
    Packet packet = Packets.parse("{ \"type\": 1, \"version\": 1, \"data\": { \"name\": \"Tirant\", \"text\": \"Hello World!\" } }");
    Assert.assertEquals(1, packet.version);
  }

  @Test
  public void parsePacketData() {
    Packet packet = Packets.parse("{ \"type\": 1, \"version\": 1, \"data\": { \"name\": \"Tirant\", \"text\": \"Hello World!\" } }");
    Message message = new Json().readValue(Message.class, packet.data);
    System.out.println("message " + message);
  }

  @Test
  public void parsePacketData_Event() {
    new HeadlessApplication(new ApplicationAdapter() {}, null);
    MPQFileHandleResolver resolver = new MPQFileHandleResolver();
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));
    Diablo.string = new StringTBLs(resolver);
    Packet packet = Packets.parse("{ \"type\": 2, \"version\": 1, \"data\": { \"id\": 3643, \"args\": [ \"Tirant\", \"email\" ] } }");
    Event event = packet.readValue(Event.class);
    System.out.println("event " + event);
    Gdx.app.exit();
  }

  @Test
  public void buildPacket() {
    Message message = new Message("Tirant", "Hello World!");
    System.out.println("msg:" + Packets.build(message));
  }
}