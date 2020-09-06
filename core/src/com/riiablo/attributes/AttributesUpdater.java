package com.riiablo.attributes;

import com.riiablo.codec.excel.CharStats;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class AttributesUpdater {
  private static final Logger log = LogManager.getLogger(AttributesUpdater.class);

  public Attributes aggregate(
      final Attributes attrs,
      final int listFlags,
      final Attributes opBase) {
    return aggregate(attrs, listFlags, opBase, null);
  }

  public Attributes aggregate(
      final Attributes attrs,
      final int listFlags,
      final Attributes opBase,
      final CharStats.Entry charStats) {
    if (log.traceEnabled()) log.tracefEntry("aggregate(attrs: %s, listFlags: 0x%x, opBase: %s, charStats: %s)", attrs, listFlags, opBase, charStats);
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
        if (gemListCount == 0) {
          log.warnf("listFlags(0x%x) does not have any gem list selected");
        } else if (gemListCount > 1) {
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
        aggregate(base, agg, rem, list.get(i), opBase, charStats);
      }
    }

    return attrs;
  }

  public Attributes add(
      final Attributes attrs,
      final StatListGetter stats,
      final Attributes opBase) {
    return add(attrs, stats, opBase, null);
  }

  public Attributes add(
      final Attributes attrs,
      final StatListGetter stats,
      final Attributes opBase,
      final CharStats.Entry charStats) {
    if (log.traceEnabled()) log.traceEntry("add(attrs: {}, stats: {}, opBase: {}, charStats: {})", attrs, stats, opBase, charStats);
    if (attrs.isSimpleType()) return attrs; // no-op
    final StatListGetter base = attrs.base();
    final StatListBuilder agg = attrs.aggregate().builder();
    final StatListBuilder rem = attrs.remaining().builder();
    aggregate(base, agg, rem, stats, opBase, charStats);
    return attrs;
  }

  private static void aggregate(
      final StatListGetter base,
      final StatListBuilder agg,
      final StatListBuilder rem,
      final StatListGetter stats,
      final Attributes opBase,
      final CharStats.Entry charStats) {
    for (StatGetter stat : stats) {
      final ItemStatCost.Entry entry = stat.entry();
      try {
        MDC.put("updateStat", stat.id());
        if (entry.op > 0) {
          final int ops = op(agg, stat, opBase, charStats);
          if (ops == 0) { /** FIXME: see note in {@link #op(StatListBuilder, StatGetter, Attributes, CharStats.Entry)} */
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
      final StatListBuilder agg,
      final StatGetter stat,
      final Attributes opBase,
      final CharStats.Entry charStats) {
    final ItemStatCost.Entry entry = stat.entry();
    final int op = entry.op;
    final int op_param = entry.op_param;
    assert op_param == 0 || !entry.op_base.isEmpty();
    final StatGetter opBaseStat = op_param > 0 ? opBase.aggregate().get(Stat.index(entry.op_base)) : null;
    assert op_param == 0 || opBaseStat != null :
        "opBase(" + opBase + ") must contain entry.op_base(" + entry.op_base + ")";
    final int op_base = op_param > 0 ? opBaseStat.value() : 1;
    int ops = 0;
    for (String op_stat : entry.op_stat) {
      if (op_stat.isEmpty()) break;
      final short statId = Stat.index(op_stat);
      final StatGetter opStat = agg.get(statId);
      if (opStat != null) {
        log.trace("Aggregating stat({})", stat.debugString());
        final int value = op(agg, stat, statId, opStat.value(), charStats, op, op_base, op_param);
        opStat.add(value);
        ops++;
      } else {
        log.warn("stat({}) modifies op_stat({}) but agg({}) does not contain it", stat, Stat.entry(statId), agg);
        /**
         * FIXME: op_stat didn't exist, needs to be put into remainder so it propagates
         */
      }
    }

    return ops;
  }

  /** @see StatFormatter#op(StatGetter, Attributes) */
  private static int op(
      final StatListBuilder agg,
      final StatGetter stat,
      final int opStatId,
      final int opStatValue,
      final CharStats.Entry charStats,
      final int op,
      final int op_base,
      final int op_param) {
    switch (op) {
      case 1: return (stat.value() * opStatValue) / 100;
      case 2: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param);
      case 3: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param) * opStatValue / 100;
      case 4: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param);
      case 5: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param) * opStatValue / 100;
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
        if (opStatId == Stat.maxhp) { // only increment vit on maxhp op
          log.trace("Aggregating stat({})", stat.debugString());
          agg.add(stat);
          //mod.set(stat.id);
        }
        return stat.value() // max hitpoints or stamina
            * (opStatId == Stat.maxhp
                ? charStats.LifePerVitality
                : charStats.StaminaPerVitality);
      case 10: return 0; // no-op
      case 11: return (stat.value() * opStatValue) / 100; // TODO: modify field value? used with item_maxhp_percent and item_maxmana_percent
      case 12: return 0; // no-op
      case 13: return (stat.value() * opStatValue) / 100;
      default: throw new AssertionError("Unsupported op: " + op + " for " + stat);
    }
  }
}
