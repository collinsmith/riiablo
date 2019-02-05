package gdx.diablo.server;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectIntMap;

public class Packets {

  private static final Json JSON = new Json();

  private Packets() {}

  public static final int MESSAGE = 1;
  public static final int CONNECT = 2;
  public static final int DISCONNECT = 3;
  public static final int MOVETO = 4;

  private static final ObjectIntMap<Class> MAP;
  static {
    MAP = new ObjectIntMap<>();
    MAP.put(Message.class,    1);
    MAP.put(Connect.class,    2);
    MAP.put(Disconnect.class, 3);
    MAP.put(MoveTo.class,     4);
  }

  public static <T> T parse(Class<T> type, String json) {
    return parse(json).readValue(type);
  }

  public static Packet parse(String json) {
    // TODO: Replace with indexes when well-formed later on
    JsonValue value = new JsonReader().parse(json);
    int type = value.getInt("type", 0);
    int version = value.getInt("version", 0);
    JsonValue data = value.get("data");
    return new Packet(type, version, data);
  }

  public static int getType(String json) {
    return new JsonReader().parse(json).getInt("type", 0);
  }

  public static String build(Object instance) {
    int type = MAP.get(instance.getClass(), 0);
    if (type == 0) return null;
    //JsonValue data = new JsonReader().parse(JSON.toJson(instance));
    EncodedJsonPacket obj = new EncodedJsonPacket(type, 0, instance);
    return JSON.toJson(obj);
  }

  private static class EncodedJsonPacket {
    int type;
    int version;
    Object data;

    EncodedJsonPacket() {}
    EncodedJsonPacket(int type, int version, Object data) {
      this.type = type;
      this.version = version;
      this.data = data;
    }
  }
}
