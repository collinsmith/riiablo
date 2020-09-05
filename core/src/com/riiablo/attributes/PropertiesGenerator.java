package com.riiablo.attributes;

import com.badlogic.gdx.math.MathUtils;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.codec.excel.Properties;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class PropertiesGenerator {
  private static final Logger log = LogManager.getLogger(PropertiesGenerator.class);

  /**
   * @param code properties file keys (res-all, ac/lvl, str, etc)
   * @param param parameter for the resulting stat (e.g., effect duration)
   * @param min min value
   * @param max max value
   */
  public StatListBuilder add(
      final StatListBuilder stats,
      final String[] code,
      final int[] param,
      final int[] min,
      final int[] max) {
    log.traceEntry("add(stats: {}, code: {}, param: {}, min: {}, max: {})", stats, code, param, min, max);
    for (int i = 0; i < code.length; i++) {
      final String c = code[i];
      if (c.isEmpty()) break;
      try {
        MDC.put("propCode", c);
        log.trace("Adding {}", c);
        final Properties.Entry prop = Riiablo.files.Properties.get(c);
        for (int j = 0; j < prop.func.length; j++) {
          if (prop.func[j] == 0) break;
          add(stats, prop, j, param[i], min[i], max[i]);
        }
      } finally {
        MDC.remove("propCode");
      }
    }

    return stats;
  }

  /**
   * @param propId some properties reference siblings, this is active index
   */
  // TODO: These might need support for assigning ranges if used when generating item stats
  void add(
      final StatListBuilder stats,
      final Properties.Entry prop,
      final int propId,
      final int param,
      final int min,
      final int max) {
    log.traceEntry(
        "add(stats: {}, prop: {}, propId: {}, param: {}, min: {}, max: {})",
        stats, prop, propId, param, min, max);
    // NOTE: some stats have a function without a stat, e.g., dmg-min -- func 5
    ItemStatCost.Entry entry = Stat.entry(prop.stat[propId]);
    final int func = prop.func[propId];
    log.trace("func: {}", func);
    switch (func) {
      case 1: { // vit, str, hp, etc.
        final int value = random(min, max);
        stats.put((short) entry.ID, value);
        return;
      }
      case 2: { // item_armor_percent
        final int value = random(min, max);
        stats.put((short) entry.ID, value);
        return;
      }
      case 3: { // res-all, all-stats, etc -- copy previous
        final StatGetter last = stats.last();
        assert last.id() == Stat.entry(prop.stat[propId - 1]).ID :
            "last.id(" + last.id() + ") != prop.stat(" + prop.stat[propId - 1] + ")";
        stats.put((short) entry.ID, last.param(), last.value());
        return;
      }
      case 5: { // dmg-min
        final int value = random(min, max);
        stats.put(Stat.mindamage, value);
        return;
      }
      case 6: { // dmg-max
        final int value = random(min, max);
        stats.put(Stat.maxdamage, value);
        return;
      }
      case 7: { // dmg%
        final int value = random(min, max);
        stats.put(Stat.item_mindamage_percent, value);
        stats.put(Stat.item_maxdamage_percent, value);
        return;
      }
      case 8: { // fcr, fwr, fbr, fhr, etc
        final int value = random(min, max);
        stats.put((short) entry.ID, value);
        return;
      }
      case 10: { // skilltab
        final int value = random(min, max);
        stats.put((short) entry.ID, param, value);
        return;
      }
      case 11: { // att-skill, hit-skill, gethit-skill, kill-skill, death-skill, levelup-skill
        final int value = min; // skill
        final int _param = Stat.encodeParam(entry.Encode, max, param); // %, level
        stats.put((short) entry.ID, _param, value);
        return;
      }
      case 12: { // skill-rand (Ormus' Robes)
        final int value = param; // skill level
        final int _param = random(min, max); // random skill
        stats.put((short) entry.ID, _param, value);
        return;
      }
      case 13: { // dur%
        final int value = random(min, max);
        stats.put((short) entry.ID, value);
        return;
      }
      case 14: { // sock
        // TODO: set item SOCKETED flag?
        final int value = random(min, max);
        stats.put((short) entry.ID, value);
        return;
      }
      case 15: { // dmg-* (min)
        final int value = min;
        stats.put((short) entry.ID, value);
        return;
      }
      case 16: { // dmg-* (max)
        final int value = max;
        stats.put((short) entry.ID, value);
        return;
      }
      case 17: { // dmg-* (length) and */lvl
        final int value = param;
        stats.put((short) entry.ID, value);
        return;
      }
      case 18: { // */time // TODO: Add support
        log.error("Unsupported property function: {}", func);
        return;
      }
      case 19: { // charged (skill)
        final int value = Stat.encodeValue(3, min, min, 0); // charges
        final int _param = Stat.encodeParam(3, max, param); // level, skill
        stats.put((short) entry.ID, _param, value);
        return;
      }
      case 20: { // indestruct
        // TODO: set item maxdurability to 0?
        stats.put(Stat.item_indesctructible, 1);
        return;
      }
      case 21: { // ama, pal, nec, etc. (item_addclassskills) and fireskill
        final int value = random(min, max);
        final int _param = prop.val[propId];
        stats.put((short) entry.ID, _param, value);
        return;
      }
      case 22: { // skill, aura, oskill
        final int value = random(min, max);
        final int _param = param;
        stats.put((short) entry.ID, _param, value);
        return;
      }
      case 23: { // ethereal
        // TODO: set item ETHEREAL flag?
        return;
      }
      case 24: { // reanimate, att-mon%, dmg-mon%, state
        final int value = random(min, max);
        final int _param = param;
        stats.put((short) entry.ID, _param, value);
        return;
      }
      case 36: { // randclassskill
        final int value = prop.val[propId]; // skill levels
        final int _param = random(min, max); // random class
        stats.put((short) entry.ID, _param, value);
        return;
      }
      case 4: // fall-through
      case 9: // fall-through
      default:
        log.error("Unknown property function: {}", func);
        return;
    }
  }

  static final int random(int min, int max) {
    return MathUtils.random(min, max);
  }
}
