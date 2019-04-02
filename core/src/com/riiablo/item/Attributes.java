package com.riiablo.item;

import com.badlogic.gdx.utils.Bits;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.ItemStatCost;

public class Attributes extends PropertyList {

  /**   *
   * magic + rune + sockets + set are additive to each other, then applied to base
   *
   * attributes list
   * magic props (magic + rune + socket)
   * set props
   * set attrs (applied only to character attrs)
   */

  final PropertyList attrs = new PropertyList();
  final Bits modified = new Bits(512);

  public Attributes() {}

  public void apply(PropertyList list) {
    modified.clear();
    attrs.clear();
    attrs.addAll(this);

    for (Stat.Instance stat : list) {
      Stat.Instance existing = attrs.get(stat.hash);
      if (existing != null) {
        System.out.println("stat " + existing + " + " + stat);
        existing.add(stat);
        modified.set(stat.stat);
      } else {
        attrs.add(stat);
      }
    }

    for (Stat.Instance stat : list) {
      op(stat, stat.entry);
    }
  }

  private void op(Stat.Instance stat, ItemStatCost.Entry entry) {
    switch (entry.op) {
      case 1:
        // adds opstat.base * statvalue / 100 to the opstat.
        break;
      case 2:
        // adds (statvalue * basevalue) / (2 ^ param) to the opstat, this does not work properly
        // with any stat other then level because of the way this is updated, it is only refreshed
        // when you re-equip the item, your character is saved or you level up, similar to passive
        // skills, just because it looks like it works in the item description does not mean it
        // does, the game just recalculates the information in the description every frame, while
        // the values remain unchanged serverside.
        break;
      case 3:
        // this is a percentage based version of op #2, look at op #2 for information about the
        // formula behind it, just remember the stat is increased by a percentage rather then by
        // adding an integer.
        break;
      case 4:
        // this works the same way op #2 works, however the stat bonus is added to the item and not
        // to the player (so that +defense per level properly adds the defense to the armor and not
        // to the character directly!)
        break;
      case 5:
        // this works like op #4 but is percentage based, it is used for percentage based increase
        // of stats that are found on the item itself, and not stats that are found on the
        // character.
        break;
      case 6:
        // like for op #7, however this adds a plain bonus to the stat, and just like #7 it also
        // doesn't work so I won't bother to explain the arithmetic behind it either.
        break;
      case 7:
        // this is used to increase a stat based on the current daytime of the game world by a
        // percentage, there is no need to explain the arithmetics behind it because frankly enough
        // it just doesn't work serverside, it only updates clientside so this op is essentially
        // useless.
        break;
      case 8:
        // hardcoded to work only with maxmana, this will apply the proper amount of mana to your
        // character based on CharStats.txt for the amount of energy the stat added (doesn't work
        // for non characters)
        break;
      case 9:
        // hardcoded to work only with maxhp and maxstamina, this will apply the proper amount of
        // maxhp and maxstamina to your character based on CharStats.txt for the amount of vitality
        // the stat added (doesn't work for non characters)
        break;
      case 10:
        // doesn't do anything, this has no switch case in the op function.
        break;
      case 11:
        // adds opstat.base * statvalue / 100 similar to 1 and 13, the code just does a few more checks
        break;
      case 12:
        // doesn't do anything, this has no switch case in the op function.
        break;
      case 13:
        // adds opstat.base * statvalue / 100 to the value of opstat, this is useable only on items
        // it will not apply the bonus to other unit types (this is why it is used for
        // +% durability, +% level requirement, +% damage, +% defense [etc]).
        int op_base = entry.op_base.isEmpty() ? 1 : 1;
        for (String op_stat : entry.op_stat) {
          if (op_stat.isEmpty()) break;
          System.out.println("op_stat=");
          int statId = Riiablo.files.ItemStatCost.index(op_stat);
          Stat.Instance mod = attrs.get(statId);
          if (mod == null) continue;
          mod.value = mod.value * (op_base * (stat.value + 100)) / 100;
          modified.set(statId);
        }
        break;
    }
  }
}
