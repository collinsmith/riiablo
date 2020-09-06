package com.riiablo.attributes;

import com.riiablo.codec.excel.CharStats;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class AttributesUpdater {
  private static final Logger log = LogManager.getLogger(AttributesUpdater.class);

  public Attributes update(Attributes attrs, int listFlags, Attributes opAttrs) {
    log.tracefEntry("update(attrs: %s, listFlags: 0x%x, opAttrs: %s)", attrs, listFlags, opAttrs);
    return update(attrs, listFlags, opAttrs, null);
  }

  public Attributes update(Attributes attrs, int listFlags, Attributes opAttrs, CharStats.Entry charStats) {
    log.tracefEntry("update(attrs: %s, listFlags: 0x%x, opAttrs: %s, charStats: %s)", attrs, listFlags, opAttrs, charStats);
    switch (attrs.type()) {
      case Attributes.AGGREGATE: {
        final int setItemListCount = StatListFlags.countSetItemFlags(listFlags);
        if (setItemListCount > 1) {
          log.warnf("listFlags(0x%x) contains more than 1 set list", listFlags);
        }
        break;
      }
      case Attributes.GEM: {
        final int gemListCount = StatListFlags.countGemFlags(listFlags);
        if (gemListCount > 1) {
          log.warnf("listFlags(0x%x) contains more than 1 gem list", listFlags);
        }
        break;
      }
      default: // no-op
        return attrs;
    }

    final StatList list = attrs.list();
    if (list.isEmpty()) return attrs;
    final StatListGetter base = attrs.base();
    final StatListBuilder agg = attrs.aggregate().builder();
    final StatListBuilder rem = attrs.remaining().builder();
    for (int i = 0, s = list.numLists(); i < s; i++) {
      if (((listFlags >> i) & 1) == 1) {
        update(opAttrs, charStats, list.get(i), base, agg, rem);
      }
    }

    return attrs;
  }

  public Attributes add(Attributes attrs, StatListGetter stats, Attributes opAttrs) {
    return add(attrs, stats, opAttrs, null);
  }

  public Attributes add(Attributes attrs, StatListGetter stats, Attributes opAttrs, CharStats.Entry charStats) {
    if (!(attrs instanceof AggregateAttributes)) return attrs; // no-op
    final StatListGetter base = attrs.base();
    final StatListBuilder agg = attrs.aggregate().builder();
    final StatListBuilder rem = attrs.remaining().builder();
    update(opAttrs, charStats, stats, base, agg, rem);
    return attrs;
  }

  private static void update(
      final Attributes opAttrs,
      final CharStats.Entry charStats,
      final StatListGetter stats,
      final StatListGetter base,
      final StatListBuilder agg,
      final StatListBuilder rem) {
    for (StatGetter stat : stats) {
      final ItemStatCost.Entry entry = stat.entry();
      try {
        MDC.put("updateStat", stat.id());
        if (entry.op > 0) {
          final int ops = op(opAttrs, charStats, agg, stat);
          if (ops == 0) {
            log.trace("Propagating stat({})", stat.debugString());
            rem.add(stat);
          }
        } else if (!base.contains(stat)) {
          log.trace("Propagating stat({})", stat.debugString());
          rem.add(stat);
        } else {
          log.trace("Aggregating stat({})", stat.debugString());
          agg.add(stat);
        }
      } finally {
        MDC.remove("updateStat");
      }
    }
  }

  private static int op(
      final Attributes opAttrs,
      final CharStats.Entry charStats,
      final StatListBuilder agg,
      final StatGetter stat) {
    final ItemStatCost.Entry entry = stat.entry();
    final int op = entry.op;
    final int op_param = entry.op_param;
    final int op_base = op_param > 0
        ? opAttrs.aggregate().get(Stat.index(entry.op_base)).value()
        : 1;
    int ops = 0;
    for (String op_stat : entry.op_stat) {
      if (op_stat.isEmpty()) break;
      final short statId = Stat.index(op_stat);
      final StatGetter opStat = agg.get(statId);
      if (opStat != null) {
        log.trace("Aggregating stat({})", stat.debugString());
        final int opValue = op(charStats, agg, stat, opStat, op, op_base, op_param);
        opStat.add(opValue);
        ops++;
      }
    }

    return ops;
  }

  /** @see StatFormatter#op(StatGetter, Attributes) */
  private static int op(
      final CharStats.Entry charStats,
      final StatListBuilder agg,
      final StatGetter stat,
      final StatGetter opStat,
      final int op,
      final int op_base,
      final int op_param) {
    switch (op) {
      case 1: return (stat.value() * opStat.value()) / 100;
      case 2: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param);
      case 3: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param) * opStat.value() / 100;
      case 4: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param);
      case 5: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param) * opStat.value() / 100;
      case 6: return 0; // by-time
      case 7: return 0; // by-time percent
      case 8:
        if (charStats == null) return 0;
        log.trace("Aggregating stat({})", stat.debugString());
        agg.add(stat);
        //mod.set(stat.id);
        return stat.value() * charStats.ManaPerMagic; // max mana
      case 9:
        if (charStats == null) return 0;
        if (opStat.id() == Stat.maxhp) { // only increment vit on maxhp op
          log.trace("Aggregating stat({})", stat.debugString());
          agg.add(stat);
          //mod.set(stat.id);
        }
        return stat.value() // max hitpoints or stamina
            * (opStat.id() == Stat.maxhp
                ? charStats.LifePerVitality
                : charStats.StaminaPerVitality);
      case 10: return 0; // no-op
      case 11: return (stat.value() * opStat.value()) / 100; // TODO: modify field value? used with item_maxhp_percent and item_maxmana_percent
      case 12: return 0; // no-op
      case 13: return (stat.value() * opStat.value()) / 100;
      default: throw new AssertionError("Unsupported op: " + op + " for " + stat);
    }
  }
}
