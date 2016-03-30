package com.gmail.collinsmith70.diablo.serializer;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.gmail.collinsmith70.util.StringSerializer;

public class AssetDescriptorStringSerializer<T> implements StringSerializer<AssetDescriptor<T>> {

  private final Class<T> TYPE;

  public AssetDescriptorStringSerializer(Class<T> type) {
    super();
    this.TYPE = type;
  }

  public Class<T> getType() {
    return TYPE;
  }

  @Override
  public String serialize(AssetDescriptor<T> obj) {
    return obj.fileName;
  }

  @Override
  public AssetDescriptor<T> deserialize(String obj) {
    return new AssetDescriptor<T>(obj, TYPE);
  }

}
