package com.riiablo.item;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.codec.excel.Properties;
import com.riiablo.codec.util.BitStream;

public class PropertyList {
  private static final int[] ATTRIBUTES  = {Stat.strength, Stat.energy, Stat.dexterity, Stat.vitality};
  private static final int[] RESISTS     = {Stat.fireresist, Stat.lightresist, Stat.coldresist, Stat.poisonresist};
  private static final int[] FIREDMG     = {Stat.firemindam, Stat.firemaxdam};
  private static final int[] LIGHTDMG    = {Stat.lightmindam, Stat.lightmaxdam};
  private static final int[] MAGICDMG    = {Stat.magicmindam, Stat.magicmaxdam};
  private static final int[] COLDDMG     = {Stat.coldmindam, Stat.coldmaxdam};
  private static final int[] POISONDMG   = {Stat.poisonmindam, Stat.poisonmaxdam, Stat.poisonlength};
  private static final int[] ENHANCEDDMG = {Stat.item_mindamage_percent, Stat.item_maxdamage_percent};
  private static final int[] MINDMG      = {Stat.mindamage, Stat.maxdamage};

  final IntMap<Stat.Instance> props = new IntMap<>();

  PropertyList() {}

  PropertyList(PropertyList src) {
    props.putAll(src.props);
  }

  public PropertyList copy() {
    return new PropertyList(this);
  }

  public void clear() {
    props.clear();
  }

  public void put(int stat, int value) {
    props.put(stat, Stat.create(stat, value));
  }

  Stat.Instance get() {
    assert props.size == 1;
    return props.entries().next().value;
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

  public PropertyList addAll(PropertyList other) {
    for (IntMap.Entry<Stat.Instance> entry : other.props) {
      Stat.Instance existing = props.get(entry.key);
      if (existing != null) {
        existing.add(entry.value);
      } else {
        props.put(entry.key, entry.value);
      }
    }

    return this;
  }

  public PropertyList reduce() {
    if (containsAll(ATTRIBUTES) && allEqual(ATTRIBUTES)) {
      int value = props.get(ATTRIBUTES[0]).value;
      for (int attr : ATTRIBUTES) props.remove(attr);
      put(Stat.all_attributes, value);
    }

    if (containsAll(RESISTS) && allEqual(RESISTS)) {
      int value = props.get(RESISTS[0]).value;
      for (int attr : RESISTS) props.remove(attr);
      put(Stat.all_resistances, value);
    }

    if (containsAll(ENHANCEDDMG) && allEqual(ENHANCEDDMG)) {
      int value = props.get(ENHANCEDDMG[0]).value;
      for (int attr : ENHANCEDDMG) props.remove(attr);
      put(Stat.enhanceddam, value);
    }

    if (containsAll(MINDMG)) {
      Stat.Instance mindamage = get(Stat.mindamage);
      Stat.Instance maxdamage = get(Stat.maxdamage);
      for (int attr : MINDMG) props.remove(attr);
      props.put(Stat.mindam, new Stat.Aggregate(Stat.mindam, "strModMinDamage", "strModMinDamageRange", mindamage, maxdamage));
    }

    if (containsAll(FIREDMG)) {
      Stat.Instance firemindam = get(Stat.firemindam);
      Stat.Instance firemaxdam = get(Stat.firemaxdam);
      for (int attr : FIREDMG) props.remove(attr);
      props.put(Stat.firedam, new Stat.Aggregate(Stat.firedam, "strModFireDamage", "strModFireDamageRange", firemindam, firemaxdam));
    }

    if (containsAll(LIGHTDMG)) {
      Stat.Instance lightmindam = get(Stat.lightmindam);
      Stat.Instance lightmaxdam = get(Stat.lightmaxdam);
      for (int attr : LIGHTDMG) props.remove(attr);
      props.put(Stat.lightdam, new Stat.Aggregate(Stat.lightdam, "strModLightningDamage", "strModLightningDamageRange", lightmindam, lightmaxdam));
    }

    if (containsAll(MAGICDMG)) {
      Stat.Instance magicmindam = get(Stat.magicmindam);
      Stat.Instance magicmaxdam = get(Stat.magicmaxdam);
      for (int attr : MAGICDMG) props.remove(attr);
      props.put(Stat.magicdam, new Stat.Aggregate(Stat.magicdam, "strModMagicDamage", "strModMagicDamageRange", magicmindam, magicmaxdam));
    }

    if (containsAll(COLDDMG)) {
      Stat.Instance coldmindam = get(Stat.coldmindam);
      Stat.Instance coldmaxdam = get(Stat.coldmaxdam);
      for (int attr : COLDDMG) props.remove(attr);
      props.put(Stat.colddam, new Stat.Aggregate(Stat.colddam, "strModColdDamage", "strModColdDamageRange", coldmindam, coldmaxdam));
    }

    if (containsAll(POISONDMG)) {
      Stat.Instance poisonmindam = get(Stat.poisonmindam);
      Stat.Instance poisonmaxdam = get(Stat.poisonmaxdam);
      Stat.Instance poisonlength = get(Stat.poisonlength);
      for (int attr : POISONDMG) props.remove(attr);
      props.put(Stat.poisondam, new Stat.Aggregate(Stat.poisondam, "strModPoisonDamage", "strModPoisonDamageRange", poisonmindam, poisonmaxdam, poisonlength));
    }

    return this;
  }

  private boolean containsAll(int[] keys) {
    boolean result = true;
    for (int i = 0; result && i < keys.length; i++) {
      result = props.containsKey(keys[i]);
    }

    return result;
  }

  private boolean allEqual(int[] keys) {
    int value = props.get(keys[0]).value;
    for (int i = 1; i < keys.length; i++) {
      if (value != props.get(keys[i]).value) return false;
    }

    return true;
  }

  public PropertyList add(String[] code, int[] param, int[] min, int[] max) {
    for (int i = 0; i < code.length; i++) {
      String c = code[i];
      if (c.isEmpty()) break;
      Properties.Entry prop = Riiablo.files.Properties.get(c);
      for (int j = 0; j < prop.stat.length; j++) {
        int[] value = j == 0 ? min : max;
        String stat = prop.stat[j];
        if (stat.isEmpty()) break;
        ItemStatCost.Entry desc = Riiablo.files.ItemStatCost.get(stat);
        Stat.Instance inst = Stat.create(desc.ID, param[i], value[i]);
        props.put(inst.hash, inst);
        //System.out.println(inst);
      }
    }

    return this;
  }
}
