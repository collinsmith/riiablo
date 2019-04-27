package com.riiablo.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.codec.excel.Properties;
import com.riiablo.codec.util.BitStream;

import java.util.Iterator;

public class PropertyList implements Iterable<Stat> {
  private static final String TAG = "PropertyList";

  private static final int[] ATTRIBUTES  = {Stat.strength, Stat.energy, Stat.dexterity, Stat.vitality};
  private static final int[] RESISTS     = {Stat.fireresist, Stat.lightresist, Stat.coldresist, Stat.poisonresist};
  private static final int[] FIREDMG     = {Stat.firemindam, Stat.firemaxdam};
  private static final int[] LIGHTDMG    = {Stat.lightmindam, Stat.lightmaxdam};
  private static final int[] MAGICDMG    = {Stat.magicmindam, Stat.magicmaxdam};
  private static final int[] COLDDMG     = {Stat.coldmindam, Stat.coldmaxdam, Stat.coldlength};
  private static final int[] POISONDMG   = {Stat.poisonmindam, Stat.poisonmaxdam, Stat.poisonlength};
  private static final int[] ENHANCEDDMG = {Stat.item_mindamage_percent, Stat.item_maxdamage_percent};
  private static final int[] MINDMG      = {Stat.mindamage, Stat.maxdamage};
  private static final int[] MINDMG2     = {Stat.mindamage, Stat.secondary_mindamage, Stat.item_throw_mindamage};
  private static final int[] MAXDMG2     = {Stat.maxdamage, Stat.secondary_maxdamage, Stat.item_throw_maxdamage};

  final IntMap<Stat> props = new IntMap<>();

  PropertyList() {}

  PropertyList(PropertyList src) {
    props.putAll(src.props);
  }

  public PropertyList copy() {
    return new PropertyList(this);
  }

  public void deepCopy(PropertyList src) {
    for (IntMap.Entry<Stat> entry : src.props.entries()) {
      props.put(entry.key, entry.value.copy());
    }
  }

  @Override
  public Iterator<Stat> iterator() {
    return props.values();
  }

  public void clear() {
    props.clear();
  }

  public int size() {
    return props.size;
  }

  public void put(int stat, int value) {
    props.put(stat, Stat.obtain(stat, value));
  }

  Stat get() {
    //assert props.size == 1;
    return props.entries().next().value;
  }

  public int read(int stat, BitStream bitStream) {
    Stat instance = Stat.obtain(stat, bitStream);
    props.put(instance.hash, instance);
    return instance.val;
  }

  public PropertyList read(BitStream bitStream) {
    for (int prop; (prop = bitStream.readUnsigned15OrLess(Stat.BITS)) != Stat.NONE;) {
      for (int j = prop, size = j + Stat.getNumEncoded(prop); j < size; j++) {
        read(j, bitStream);
      }
    }

    return this;
  }

  public Stat get(int stat) {
    return props.get(stat);
  }

  public Array<Stat> toArray() {
    return props.values().toArray();
  }

  public void add(Stat stat) {
    assert stat.id == 0 || stat.hash != 0;
    Stat existing = props.get(stat.hash);
    if (existing != null) {
      existing.add(stat);
    } else {
      props.put(stat.hash, stat);
    }
  }

  public void addCopy(Stat stat) {
    assert stat.id == 0 || stat.hash != 0;
    Stat existing = props.get(stat.hash);
    if (existing != null) {
      existing.add(stat);
    } else {
      props.put(stat.hash, stat.copy());
    }
  }

  public PropertyList addAll(PropertyList other) {
    for (Stat stat : other.props.values()) {
      add(stat);
    }

    return this;
  }

  public PropertyList reduce() {
    if (containsAll(ATTRIBUTES) && allEqual(ATTRIBUTES)) {
      int value = props.get(ATTRIBUTES[0]).val;
      for (int attr : ATTRIBUTES) props.remove(attr);
      put(Stat.all_attributes, value);
    }

    if (containsAll(RESISTS) && allEqual(RESISTS)) {
      int value = props.get(RESISTS[0]).val;
      for (int attr : RESISTS) props.remove(attr);
      put(Stat.all_resistances, value);
    }

    if (containsAll(ENHANCEDDMG) && allEqual(ENHANCEDDMG)) {
      int value = props.get(ENHANCEDDMG[0]).val;
      for (int attr : ENHANCEDDMG) props.remove(attr);
      put(Stat.enhanceddam, value);
    }

    if (containsAll(MINDMG)) {
      Stat mindamage = get(Stat.mindamage);
      Stat maxdamage = get(Stat.maxdamage);
      for (int attr : MINDMG) props.remove(attr);
      props.put(Stat.mindam, new Stat.Aggregate(Stat.mindam, "strModMinDamage", "strModMinDamageRange", mindamage, maxdamage));
    }

    if (containsAll(FIREDMG)) {
      Stat firemindam = get(Stat.firemindam);
      Stat firemaxdam = get(Stat.firemaxdam);
      for (int attr : FIREDMG) props.remove(attr);
      props.put(Stat.firedam, new Stat.Aggregate(Stat.firedam, "strModFireDamage", "strModFireDamageRange", firemindam, firemaxdam));
    }

    if (containsAll(LIGHTDMG)) {
      Stat lightmindam = get(Stat.lightmindam);
      Stat lightmaxdam = get(Stat.lightmaxdam);
      for (int attr : LIGHTDMG) props.remove(attr);
      props.put(Stat.lightdam, new Stat.Aggregate(Stat.lightdam, "strModLightningDamage", "strModLightningDamageRange", lightmindam, lightmaxdam));
    }

    if (containsAll(MAGICDMG)) {
      Stat magicmindam = get(Stat.magicmindam);
      Stat magicmaxdam = get(Stat.magicmaxdam);
      for (int attr : MAGICDMG) props.remove(attr);
      props.put(Stat.magicdam, new Stat.Aggregate(Stat.magicdam, "strModMagicDamage", "strModMagicDamageRange", magicmindam, magicmaxdam));
    }

    if (containsAll(COLDDMG)) {
      Stat coldmindam = get(Stat.coldmindam);
      Stat coldmaxdam = get(Stat.coldmaxdam);
      for (int attr : COLDDMG) props.remove(attr);
      props.put(Stat.colddam, new Stat.Aggregate(Stat.colddam, "strModColdDamage", "strModColdDamageRange", coldmindam, coldmaxdam));
    }

    if (containsAll(POISONDMG)) {
      Stat poisonmindam = get(Stat.poisonmindam);
      Stat poisonmaxdam = get(Stat.poisonmaxdam);
      Stat poisonlength = get(Stat.poisonlength);
      for (int attr : POISONDMG) props.remove(attr);
      props.put(Stat.poisondam, new Stat.Aggregate(Stat.poisondam, "strModPoisonDamage", "strModPoisonDamageRange", poisonmindam, poisonmaxdam, poisonlength));
    }

    if (containsAll(MINDMG2) && allEqual(MINDMG2)) {
      for (int i = 1; i < MINDMG2.length; i++) props.remove(MINDMG2[i]);
    }

    if (containsAll(MAXDMG2) && allEqual(MAXDMG2)) {
      for (int i = 1; i < MAXDMG2.length; i++) props.remove(MAXDMG2[i]);
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
    int value = props.get(keys[0]).val;
    for (int i = 1; i < keys.length; i++) {
      if (value != props.get(keys[i]).val) return false;
    }

    return true;
  }

  public PropertyList add(String[] code, int[] param, int[] min, int[] max) {
    for (int i = 0; i < code.length; i++) {
      String c = code[i];
      if (c.isEmpty()) break;
      Properties.Entry prop = Riiablo.files.Properties.get(c);
      int value = Integer.MIN_VALUE;
      for (int j = 0; j < prop.func.length; j++) {
        if (prop.func[j] == 0) break;
        value = add(prop, i, j, value, code, param, min, max);
      }
    }

    return this;
  }

  // TODO: These might need support for assigning ranges if used when generating item stats
  private int add(Properties.Entry prop, int i, int j, int value, String[] code, int[] params, int[] min, int[] max) {
    // NOTE: some stats have a function without a stat, e.g., dmg-min -- func 5
    ItemStatCost.Entry desc = Riiablo.files.ItemStatCost.get(prop.stat[j]);
    Stat inst;
    int param;
    switch (prop.func[j]) {
      case 1: // vit, str, hp, etc.
        value = MathUtils.random(min[i], max[i]);
        inst = Stat.obtain(desc.ID, value);
        props.put(inst.hash, inst);
        return value;
      case 2: // item_armor_percent
        value = MathUtils.random(min[i], max[i]);
        inst = Stat.obtain(desc.ID, value);
        props.put(inst.hash, inst);
        return value;
      case 3: // res-all, all-stats, etc -- reference previous index for values
        assert value != Integer.MIN_VALUE;
        inst = Stat.obtain(desc.ID, value);
        props.put(inst.hash, inst);
        return value;
      case 5: // dmg-min
        value = MathUtils.random(min[i], max[i]);
        put(Stat.mindamage, value);
        return value;
      case 6: // dmg-max
        value = MathUtils.random(min[i], max[i]);
        put(Stat.maxdamage, value);
        return value;
      case 7: // dmg%
        value = MathUtils.random(min[i], max[i]);
        put(Stat.item_mindamage_percent, value);
        put(Stat.item_maxdamage_percent, value);
        return value;
      case 8: // fcr, fwr, fbr, fhr, etc
        value = MathUtils.random(min[i], max[i]);
        inst = Stat.obtain(desc.ID, value);
        props.put(inst.hash, inst);
        return value;
      case 10: // skilltab
        value = MathUtils.random(min[i], max[i]);
        inst = Stat.obtain(desc.ID, params[i], value);
        props.put(inst.hash, inst);
        return value;
      case 11: // att-skill, hit-skill, gethit-skill, kill-skill, death-skill, levelup-skill
        value = min[i]; // skill
        param = Stat.encodeParam(desc.Encode, max[i], params[i]); // %, level
        inst = Stat.obtain(desc.ID, param, value);
        props.put(inst.hash, inst);
        return value;
      case 12: // skill-rand (Ormus' Robes)
        value = params[i]; // skill level
        param = MathUtils.random(min[i], max[i]); // random skill
        inst = Stat.obtain(desc.ID, param, value);
        props.put(inst.hash, inst);
        return value;
      case 13: // dur%
        value = MathUtils.random(min[i], max[i]);
        put(desc.ID, value);
        return value;
      case 14: // sock
        // TODO: set item SOCKETED flag?
        value = MathUtils.random(min[i], max[i]);
        inst = Stat.obtain(desc.ID, value);
        props.put(inst.hash, inst);
        return value;
      case 15: // dmg-* (min)
        value = min[i];
        inst = Stat.obtain(desc.ID, value);
        props.put(inst.hash, inst);
        return value;
      case 16: // dmg-* (max)
        value = max[i];
        inst = Stat.obtain(desc.ID, value);
        props.put(inst.hash, inst);
        return value;
      case 17: // dmg-* (length) and */lvl
        value = params[i];
        inst = Stat.obtain(desc.ID, value);
        props.put(inst.hash, inst);
        return value;
      case 18: // */time // TODO: Add support
        Gdx.app.error(TAG, "Unsupported property function: " + prop.func[i]);
        return Integer.MIN_VALUE;
      case 19: // charged (skill)
        value = Stat.encodeValue(3, min[i], min[i]); // charges
        param = Stat.encodeParam(3, max[i], params[i]); // level, skill
        inst = Stat.obtain(desc.ID, param, value);
        props.put(inst.hash, inst);
        return value;
      case 20: // indestruct
        // TODO: set item maxdurability to 0?
        value = 1;
        put(Stat.item_indesctructible, value);
        return value;
      case 21: // ama, pal, nec, etc. (item_addclassskills) and fireskill
        value = MathUtils.random(min[i], max[i]);
        param = prop.val[j];
        inst = Stat.obtain(desc.ID, param, value);
        props.put(inst.hash, inst);
        return value;
      case 22: // skill, aura, oskill
        value = MathUtils.random(min[i], max[i]);
        param = params[i];
        inst = Stat.obtain(desc.ID, param, value);
        props.put(inst.hash, inst);
        return value;
      case 23: // ethereal
        // TODO: set item ETHEREAL flag?
        return Integer.MIN_VALUE;
      case 24: // reanimate, att-mon%, dmg-mon%, state
        value = MathUtils.random(min[i], max[i]);
        param = params[i];
        inst = Stat.obtain(desc.ID, param, value);
        props.put(inst.hash, inst);
        return value;
      case 36: // randclassskill
        value = prop.val[j]; // skill levels
        param = MathUtils.random(min[i], max[i]); // random class
        inst = Stat.obtain(desc.ID, param, value);
        props.put(inst.hash, inst);
        return value;
      case 4:
      case 9:
      default:
        Gdx.app.error(TAG, "Unsupported property function: " + prop.func[i]);
        return Integer.MIN_VALUE;
    }
  }
}
