package com.riiablo.item;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.riiablo.codec.util.BitStream;

public class PropertyList {
  final IntMap<Stat.Instance> props = new IntMap<>();

  PropertyList() {}

  public void put(int stat, int value) {
    props.put(stat, Stat.create(stat, value));
  }

  public int read(int stat, BitStream bitStream) {
    Stat.Instance instance = Stat.read(stat, bitStream);
    props.put(instance.hash, instance);
    return instance.value;
  }

  public PropertyList read(BitStream bitStream) {
    for (int prop; (prop = bitStream.readUnsigned15OrLess(Stat.BITS)) != Stat.NONE;) {
      for (int j = prop, size = j + Stat.getNumEncoded(prop); j < size; j++) {
        read(j, bitStream);
      }
    }

    return this;
  }

  public Stat.Instance get(int stat) {
    return props.get(stat);
  }

  public Array<Stat.Instance> toArray() {
    return props.values().toArray();
  }
}
