package com.riiablo.server;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class Packet {

  private static final Json JSON = new Json();

  public int type;
  public int version;
  public JsonValue data;

  Packet(int type, int version, JsonValue data) {
    this.type = type;
    this.version = version;
    this.data = data;
  }

  public <T> T readValue(Class<T> type) {
    return JSON.readValue(type, data);
  }

  @Override
  public String toString() {
    return JSON.toJson(this);
  }
}
