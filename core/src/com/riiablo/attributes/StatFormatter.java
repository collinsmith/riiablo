package com.riiablo.attributes;

import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.CharStats;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.math.Fixed;

public class StatFormatter {
  private static final Logger log = LogManager.getLogger(StatFormatter.class);

  private static final StringBuilder builder = new StringBuilder(32);

  private static final CharSequence SPACE;
  private static final CharSequence DASH;
  private static final CharSequence PERCENT;
  private static final CharSequence PLUS;
  private static final CharSequence TO;
  static {
    if (Riiablo.string == null) {
      SPACE = " ";
      DASH = "-";
      PERCENT = "%";
      PLUS = "+";
      TO = "to";
    } else {
      SPACE = Riiablo.string.lookup("space");
      DASH = Riiablo.string.lookup("dash");
      PERCENT = Riiablo.string.lookup("percent");
      PLUS = Riiablo.string.lookup("plus");
      TO = Riiablo.string.lookup("ItemStast1k");
    }
  }

  private static final String[] BY_TIME = {
      "ModStre9e", "ModStre9g", "ModStre9d", "ModStre9f",
  };

  public String format(StatGetter stat, Attributes opAttrs) {
    final ItemStatCost.Entry entry = stat.entry();
    return format(stat, opAttrs, entry.descfunc, entry.descval, entry.descstrpos, entry.descstrneg, entry.descstr2);
  }

  public String format(
      final StatGetter stat,
      final Attributes opAttrs,
      final int func,
      final int valmode,
      final String strpos,
      final String strneg,
      final String str2) {
    builder.setLength(0);
    switch (func) {
      case 1: { // +%d %s1
        final int value = stat.value1();
        if (valmode == 1) builder.append(PLUS).append(value).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value);
        return builder.toString();
      }
      case 2: { // %d%% %s1
        final int value = stat.value1();
        if (valmode == 1) builder.append(value).append(PERCENT).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(value).append(PERCENT);
        return builder.toString();
      }
      case 3: { // %d %s1
        final int value = stat.value1();
        if (valmode == 1) builder.append(value).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(value);
        return builder.toString();
      }
      case 4: { // +%d%% %s1
        final int value = stat.value1();
        if (valmode == 1) builder.append(PLUS).append(value).append(PERCENT).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value).append(PERCENT);
        return builder.toString();
      }
      case 5: { // %d%% %s1
        final int value = com.riiablo.math.Fixed.intBitsToFloatFloor(stat.value1() * 100, 7);
        if (valmode == 1) builder.append(value).append(PERCENT).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(value).append(PERCENT);
        return builder.toString();
      }
      case 6: { // +%d %s1 %s2
        final int value = op(stat, opAttrs);
        if (valmode == 1) builder.append(PLUS).append(value).append(SPACE);
        builder
            .append(Riiablo.string.lookup(value < 0 ? strneg : strpos))
            .append(SPACE)
            .append(Riiablo.string.lookup(str2));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value);
        return builder.toString();
      }
      case 7: { // %d%% %s1 %s2
        final int value = op(stat, opAttrs);
        if (valmode == 1) builder.append(value).append(PERCENT).append(SPACE);
        builder
            .append(Riiablo.string.lookup(value < 0 ? strneg : strpos))
            .append(SPACE)
            .append(Riiablo.string.lookup(str2));
        if (valmode == 2) builder.append(SPACE).append(value).append(PERCENT);
        return builder.toString();
      }
      case 8: { // +%d%% %s1 %s2
        final int value = op(stat, opAttrs);
        if (valmode == 1) builder.append(PLUS).append(value).append(PERCENT).append(SPACE);
        builder
            .append(Riiablo.string.lookup(value < 0 ? strneg : strpos))
            .append(SPACE)
            .append(Riiablo.string.lookup(str2));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value).append(PERCENT);
        return builder.toString();
      }
      case 9: { // %d %s1 %s2
        final int value = op(stat, opAttrs);
        if (valmode == 1) builder.append(value).append(SPACE);
        builder
            .append(Riiablo.string.lookup(value < 0 ? strneg : strpos))
            .append(SPACE)
            .append(Riiablo.string.lookup(str2));
        if (valmode == 2) builder.append(SPACE).append(value);
        return builder.toString();
      }
      case 10: { // %d%% %s1 %s2
        final int value = com.riiablo.math.Fixed.intBitsToFloatFloor(stat.value1() * 100, 7);
        if (valmode == 1) builder.append(value).append(PERCENT).append(SPACE);
        builder
            .append(Riiablo.string.lookup(value < 0 ? strneg : strpos))
            .append(SPACE)
            .append(Riiablo.string.lookup(str2));
        if (valmode == 2) builder.append(SPACE).append(value).append(PERCENT);
        return builder.toString();
      }
      case 11: { // Repairs 1 Durability in %d Seconds
        final int value = 100 / stat.value1();
        return Riiablo.string.format("ModStre9u", 1, value);
      }
      case 12: { // +%d %s1
        final int value = stat.value1();
        if (valmode == 1) builder.append(PLUS).append(value).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value);
        return builder.toString();
      }
      case 13: { // +%d %s | +1 to Paladin Skills
        final int value = stat.value1();
        final int param = stat.param1();
        builder
            .append(PLUS).append(value)
            .append(SPACE)
            .append(Riiablo.string.lookup(CharacterClass.get(param).entry().StrAllSkills));
        return builder.toString();
      }
      case 14: { // %s %s | +1 to Fire Skills (Sorceress Only)
        final int value = stat.value1();
        final int param = stat.param1();
        final CharStats.Entry entry = CharacterClass.get((param >>> 3) & 0x3).entry();
        builder
            .append(Riiablo.string.format(entry.StrSkillTab[param & 0x7], value))
            .append(SPACE)
            .append(Riiablo.string.lookup(entry.StrClassOnly));
        return builder.toString();
      }
      case 15: { // 15% chance to cast Level 5 Life Tap on Striking
        final int value = stat.value1();
        final int param = stat.param1();
        final Skills.Entry skill = Riiablo.files.skills.get(stat.param2());
        final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        return Riiablo.string.format(strpos, value, param, Riiablo.string.lookup(desc.str_name));
      }
      case 16: { // Level 16 Defiance Aura When Equipped
        final int value = stat.value1();
        final int param = stat.param1();
        final Skills.Entry skill = Riiablo.files.skills.get(param);
        final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        return Riiablo.string.format(strpos, value, Riiablo.string.lookup(desc.str_name));
      }
      case 17: { // +10 to Dexterity (Increases Near Dawn) // TODO: untested
        if (log.warnEnabled()) log.warn("stat({}) uses untested bytime func({})", stat.debugString(), func);
        // value needs to update based on time of day
        final int value1 = stat.value1();
        final int value2 = stat.value2();
        final int value3 = stat.value3();
        if (valmode == 1) builder.append(PLUS).append(value3).append(SPACE);
        builder.append(Riiablo.string.lookup(strpos));
        if (valmode == 2) builder.append(SPACE).append(PLUS).append(value3);
        builder.append(SPACE).append(Riiablo.string.lookup(BY_TIME[value1]));
        return builder.toString();
      }
      case 18: { // 50% Enhanced Defense (Increases Near Dawn) // TODO: untested
        if (log.warnEnabled()) log.warn("stat({}) uses untested bytime func({})", stat.debugString(), func);
        // value needs to update based on time of day
        final int value1 = stat.value1();
        final int value2 = stat.value2();
        final int value3 = stat.value3();
        if (valmode == 1) builder.append(value3).append(PERCENT).append(SPACE);
        builder.append(Riiablo.string.lookup(strpos));
        if (valmode == 2) builder.append(SPACE).append(value3).append(PERCENT);
        builder.append(SPACE).append(Riiablo.string.lookup(BY_TIME[value1]));
        return builder.toString();
      }
      case 19: { // Formats strpos/strneg with value
        final int value = stat.value1();
        return Riiablo.string.format(value < 0 ? strneg : strpos, value);
      }
      case 20: { // -%d%% %s1
        final int value = -stat.value1();
        if (valmode == 1) builder.append(value).append(PERCENT).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(value).append(PERCENT);
        return builder.toString();
      }
      case 21: { // -%d %s1
        final int value = -stat.value1();
        if (valmode == 1) builder.append(value).append(SPACE);
        builder.append(Riiablo.string.lookup(value < 0 ? strneg : strpos));
        if (valmode == 2) builder.append(SPACE).append(value);
        return builder.toString();
      }
      case 22: { // +%d%% %s1 %s | +3% Attack Rating Versus: %s // TODO: unsupported for now
        if (log.warnEnabled()) log.warn("stat({}) uses unsupported func({})", stat.debugString(), func);
        return "ERROR(22)";
      }
      case 23: { // %d%% %s1 %s | 3% ReanimateAs: %s // TODO: unsupported for now
        if (log.warnEnabled()) log.warn("stat({}) uses unsupported func({})", stat.debugString(), func);
        return "ERROR(23)";
      }
      case 24: {
        final Skills.Entry skill = Riiablo.files.skills.get(stat.param2());
        final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        builder
            .append(Riiablo.string.lookup("ModStre10b")).append(SPACE)
            .append(stat.param1()).append(SPACE)
            .append(Riiablo.string.lookup(desc.str_name)).append(SPACE)
            .append(Riiablo.string.format(strpos, stat.value1(), stat.value2()));
        return builder.toString();
      }
      case 25: { // TODO: unsupported
        if (log.warnEnabled()) log.warn("stat({}) uses unsupported func({})", stat.debugString(), func);
        return "ERROR(25)";
      }
      case 26: { // TODO: unsupported
        if (log.warnEnabled()) log.warn("stat({}) uses unsupported func({})", stat.debugString(), func);
        return "ERROR(26)";
      }
      case 27: { // +1 to Lightning (Sorceress Only)
        final int value = stat.value1();
        final int param = stat.param1();
        final Skills.Entry skill = Riiablo.files.skills.get(param);
        final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        final CharStats.Entry entry = Riiablo.files.skills.getClass(skill.charclass).entry();
        builder
            .append(PLUS).append(value).append(SPACE)
            .append(TO).append(SPACE)
            .append(Riiablo.string.lookup(desc.str_name)).append(SPACE)
            .append(Riiablo.string.lookup(entry.StrClassOnly));
        return builder.toString();
      }
      case 28: { // +1 to Teleport
        final int value = stat.value1();
        final int param = stat.param1();
        final Skills.Entry skill = Riiablo.files.skills.get(param);
        final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        builder
            .append(PLUS).append(value).append(SPACE)
            .append(TO).append(SPACE)
            .append(Riiablo.string.lookup(desc.str_name));
        return builder.toString();
      }
      default:
        if (log.warnEnabled()) log.warn("stat({}) uses unknown func({})", stat.debugString(), func);
        return "ERROR";
    }
  }

  public String formatEncoded(
      final StatListGetter stats,
      final int index,
      final int encoded,
      final String equalsStr,
      final String rangeStr) {
    switch (encoded) {
      case 2: {
        final int value1 = stats.get(index).asInt();
        final int value2 = stats.get(index + 1).asInt();
        if (value1 == value2) {
          return Riiablo.string.format(equalsStr, value2);
        } else {
          return Riiablo.string.format(rangeStr, value1, value2);
        }
      }
      case 3: {
        final int value1 = stats.get(index).asInt();
        final int value2 = stats.get(index + 1).asInt();
        final int value3 = stats.get(index + 2).asInt();
        if (value1 == value2) {
          return Riiablo.string.format(equalsStr, value2, value3);
        } else {
          return Riiablo.string.format(rangeStr, value1, value2, value3);
        }
      }
      default:
        log.warn("Unknown encoded({}): expected 2 or 3", encoded);
        return "INVALID_ENCODED(" + encoded + ")";
    }
  }

  /** @see AttributesUpdater#op(CharStats.Entry, StatListBuilder, StatGetter, StatGetter, int, int, int) */
  private static int op(StatGetter stat, Attributes opAttrs) {
    final ItemStatCost.Entry entry = stat.entry();
    final int op_param = entry.op_param;
    final int op_base = op_param > 0
        ? opAttrs.aggregate().get(Stat.index(entry.op_base)).value1()
        : 1;
    final int value = stat.value1();
    switch (entry.op) {
      default: log.warn("entry.op({}) unknown for stat({})", entry.op, stat.debugString()); // fall-through
      case 1: return value;
      case 2: return com.riiablo.math.Fixed.intBitsToFloatFloor(value * op_base, op_param);
      case 3: return value;
      case 4: return com.riiablo.math.Fixed.intBitsToFloatFloor(value * op_base, op_param);
      case 5: return Fixed.intBitsToFloatFloor(value * op_base, op_param);
      case 6: return value; // Unsupported -- time of day
      case 7: return value; // Unsupported -- time of day %
      case 8: return value;
      case 9: return value;
      case 10: return value;
      case 11: return value;
      case 12: return value;
      case 13: return value;
    }
  }
}
