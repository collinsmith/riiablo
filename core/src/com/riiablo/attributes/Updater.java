package com.riiablo.attributes;

import com.riiablo.codec.excel.CharStats;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public final class Updater {
  private static final Logger log = LogManager.getLogger(Updater.class);

  private final UpdateSequence SEQUENCER = new UpdateSequence(this);
  private final StatList cache = StatList.obtainLarge(); // easier than implementing StatList#remove

  public UpdateSequence update(final Attributes attrs, final CharStats.Entry charStats) {
    return update(attrs, StatListFlags.FLAG_NONE, charStats);
  }

  public UpdateSequence update(final Attributes attrs, final int listFlags, final CharStats.Entry charStats) {
    return SEQUENCER.reset(attrs, listFlags, charStats);
  }

  void add(
      final Attributes attrs,
      final StatListGetter stats) {
    add(attrs.base(), attrs.aggregate().builder(), attrs.remaining().builder(), stats);
  }

  void apply(
      final Attributes attrs,
      final CharStats.Entry charStats,
      final Attributes opBase) {
    apply(attrs.base(), attrs.aggregate().builder(), attrs.remaining(), opBase, charStats);
  }

  static void add(
      final StatListGetter base,
      final StatListBuilder agg,
      final StatListBuilder rem,
      final StatListGetter stats) {
    for (StatGetter stat : stats) {
      final ItemStatCost.Entry entry = stat.entry();
      try {
        MDC.put("addStat", stat.id());
        final int op = entry.op;
        if (op > 0) {
          if (log.traceEnabled()) log.trace("Propagating stat({}) op({})", stat.debugString(), op);
          rem.add(stat);
        } else if (base.contains(stat)) {
          if (log.traceEnabled()) log.trace("Aggregating stat({})", stat.debugString());
          agg.add(stat);
        } else {
          if (log.traceEnabled()) log.trace("Propagating stat({})", stat.debugString());
          rem.add(stat);
        }
      } finally {
        MDC.remove("addStat");
      }
    }
  }

  void apply(
      final StatListGetter base,
      final StatListBuilder agg,
      final StatListGetter rem,
      final Attributes opBase,
      final CharStats.Entry charStats) {
    // should pass values from rem to agg if appliable
    final StatListBuilder cache = this.cache.clear().buildList();
    for (StatGetter stat : rem) {
      final ItemStatCost.Entry entry = stat.entry();
      try {
        MDC.put("applyStat", stat.id());
        final int op = entry.op;
        if (op > 0) {
          if (log.traceEnabled()) log.trace("Applying stat({}) op({})", stat.debugString(), op);
          op(agg, cache, opBase, charStats, stat);
        } else if (base.contains(stat)) {
          if (log.traceEnabled()) log.trace("Aggregating stat({})", stat.debugString());
          agg.add(stat);
        } else {
          if (log.traceEnabled()) log.trace("Propagating stat({})", stat.debugString());
          cache.add(stat);
        }
      } finally {
        MDC.remove("applyStat");
      }
    }

    rem.parent().setAll(cache.build());
  }

  static void op(
      final StatListBuilder agg,
      final StatListBuilder rem,
      final Attributes opBase,
      final CharStats.Entry charStats,
      final StatGetter stat) {
    final ItemStatCost.Entry entry = stat.entry();
    final int op = entry.op;
    final int op_param = entry.op_param;
    assert op_param == 0 || !entry.op_base.isEmpty();
    final short opBaseStatId = Stat.index(entry.op_base);
    assert op_param == 0 || opBase.aggregate().contains(opBaseStatId);
    final int op_base = op_param > 0 ? opBase.aggregate().getValue(opBaseStatId, 1) : 1;

    int ops = 0, expectedOps = 0;
    for (String op_stat : entry.op_stat) {
      if (op_stat.isEmpty()) break;
      expectedOps++;

      final short opStatId = Stat.index(op_stat);
      final StatGetter opStat = agg.get(opStatId);
      if (opStat != null) {
        if (log.traceEnabled()) log.trace("Op stat({}) with opStat({})", stat.debugString(), opStat.debugString());
        final int value = op(stat, opStat, charStats, op, op_param, op_base);
        opStat.addShifted(value);
        ops++;
      } else {
        log.warn("stat({}) modifies opStat({}) but agg({}) does not contain it", stat.debugString(), Stat.entry(opStatId), agg);
      }
    }

    if (ops == 0) {
      rem.add(stat);
    } else if (ops < expectedOps) {
      log.warn("{} stats were not op'd", ops);
    }
  }

  /** @see StatFormatter#op(StatGetter, Attributes) */
  static int op(
      final StatGetter stat,
      final StatGetter opStat,
      final CharStats.Entry charStats,
      final int op,
      final int op_param,
      final int op_base) {
    switch (op) {
      case 1: return (stat.value() * opStat.value()) / 100;
      case 2: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param);
      case 3: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param) * opStat.value() / 100;
      case 4: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param);
      case 5: return Fixed.intBitsToFloatFloor(stat.value() * op_base, op_param) * opStat.value() / 100;
      case 6: return 0; // by-time
      case 7: return 0; // by-time percent
      case 8: // energy
        assert charStats != null;
        if (charStats == null) return 0;
        return (stat.value() * charStats.ManaPerMagic) >>> 2; // in quarters
      case 9: // vitality
        assert charStats != null;
        if (charStats == null) return 0;
        return (stat.value() *
            (opStat.id() == Stat.maxhp
                ? charStats.LifePerVitality
                : charStats.StaminaPerVitality)) >>> 2; // in quarters
      case 10: return 0; // no-op
      case 11: return (stat.value() * opStat.value()) / 100; // TODO: modify field value? used with item_maxhp_percent and item_maxmana_percent
      case 12: return 0; // no-op
      case 13: return (stat.value() * opStat.value()) / 100;
      default:
        log.error("Unknown op({}) for stat({})", op, stat.debugString());
        return 0;
    }
  }
}
