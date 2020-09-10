package com.riiablo.attributes;

import com.riiablo.codec.excel.CharStats;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;
import com.riiablo.math.Fixed;

public final class AttributesUpdater {
  private static final Logger log = LogManager.getLogger(AttributesUpdater.class);

  public com.riiablo.attributes.UpdateSequence update(final Attributes attrs, final CharStats.Entry charStats) {
    return update(attrs, attrs, charStats);
  }

  public com.riiablo.attributes.UpdateSequence update(
      final Attributes attrs,
      final Attributes opBase,
      final CharStats.Entry charStats) {
    return update(attrs, StatListFlags.FLAG_NONE, opBase, charStats);
  }

  public com.riiablo.attributes.UpdateSequence update(
      final Attributes attrs,
      final int listFlags,
      final Attributes opBase,
      final CharStats.Entry charStats) {
    return com.riiablo.attributes.UpdateSequence.obtain().reset(this, attrs, listFlags, opBase, charStats);
  }

  void add(
      final Attributes attrs,
      final StatListRef stats) {
    add(attrs.base(), attrs.aggregate(), attrs.remaining(), stats);
  }

  void apply(
      final Attributes attrs,
      final CharStats.Entry charStats,
      final Attributes opBase) {
    apply(attrs.base(), attrs.aggregate(), attrs.remaining(), opBase, charStats);
  }

  static void add(
      final StatListRef base,
      final StatListRef agg,
      final StatListRef rem,
      final StatListRef stats) {
    for (StatRef stat : stats) {
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
      final StatListRef base,
      final StatListRef agg,
      final StatListRef rem,
      final Attributes opBase,
      final CharStats.Entry charStats) {
    for (final com.riiablo.attributes.StatList.StatIterator it = rem.statIterator(); it.hasNext();) {
      final StatRef stat = it.next();
      final ItemStatCost.Entry entry = stat.entry();
      try {
        MDC.put("applyStat", stat.id());
        final int op = entry.op;
        if (op > 0) {
          if (log.traceEnabled()) log.trace("Applying stat({}) op({})", stat.debugString(), op);
          final int ops = op(agg, opBase, charStats, stat);
          if (ops == 0) {
            if (log.traceEnabled()) log.trace("Propagating stat({})", stat.debugString());
            it.pushback();
          } else if (ops > 0) {
            if (log.traceEnabled()) log.trace("Aggregating stat({})", stat.debugString());
            agg.add(stat);
          }
        } else if (base.contains(stat)) {
          if (log.traceEnabled()) log.trace("Aggregating stat({})", stat.debugString());
          agg.add(stat);
        } else {
          if (log.traceEnabled()) log.trace("Propagating stat({})", stat.debugString());
          it.pushback();
        }
      } finally {
        MDC.remove("applyStat");
      }
    }
  }

  static int op(
      final StatListRef agg,
      final Attributes opBase,
      final CharStats.Entry charStats,
      final StatRef stat) {
    final ItemStatCost.Entry entry = stat.entry();
    final int op = entry.op;
    final int op_param = entry.op_param;
    assert op_param == 0 || !entry.op_base.isEmpty();
    final short opBaseStatId = com.riiablo.attributes.Stat.index(entry.op_base);
    assert op_param == 0 || opBase.aggregate().contains(opBaseStatId) : "entry.op_base " + entry.op_base;
    final int op_base = op_param > 0 ? opBase.aggregate().getValue(opBaseStatId, 1) : 1;

    int ops = 0, expectedOps = 0;
    for (String op_stat : entry.op_stat) {
      if (op_stat.isEmpty()) break;
      expectedOps++;

      final short opStatId = com.riiablo.attributes.Stat.index(op_stat);
      final StatRef opStat = agg.get(opStatId);
      if (opStat != null) {
        if (log.traceEnabled()) log.trace("Op stat({}) with opStat({})", stat.debugString(), opStat.debugString());
        final int value = op(stat, opStat, charStats, op, op_param, op_base);
        opStat.addEncoded(value);
        ops++;
      } else {
        log.warn("stat({}) modifies opStat({}) but agg({}) does not contain it", stat.debugString(), com.riiablo.attributes.Stat.entry(opStatId), agg);
      }
    }

    if (ops < expectedOps) {
      log.warn("{} stats were modified by stat({}) op({})", ops, stat.debugString(), op);
    }

    return ops;
  }

  /** @see com.riiablo.attributes.StatFormatter#op(StatRef, Attributes) */
  static int op(
      final StatRef stat,
      final StatRef opStat,
      final CharStats.Entry charStats,
      final int op,
      final int op_param,
      final int op_base) {
    switch (op) {
      case 1: return (stat.encodedValues() * opStat.encodedValues()) / 100;
      case 2: return Fixed.intBitsToFloatFloor(stat.encodedValues() * op_base, op_param);
      case 3: return Fixed.intBitsToFloatFloor(stat.encodedValues() * op_base, op_param) * opStat.encodedValues() / 100;
      case 4: return Fixed.intBitsToFloatFloor(stat.encodedValues() * op_base, op_param);
      case 5: return Fixed.intBitsToFloatFloor(stat.encodedValues() * op_base, op_param) * opStat.encodedValues() / 100;
      case 6: return 0; // by-time
      case 7: return 0; // by-time percent
      case 8: // energy
        assert charStats != null;
        if (charStats == null) return 0;
        return (stat.encodedValues() * charStats.ManaPerMagic) >>> 2; // in quarters
      case 9: // vitality
        assert charStats != null;
        if (charStats == null) return 0;
        return (stat.encodedValues() *
            (opStat.id() == com.riiablo.attributes.Stat.maxhp
                ? charStats.LifePerVitality
                : charStats.StaminaPerVitality)) >>> 2; // in quarters
      case 10: return 0; // no-op
      case 11: return (stat.encodedValues() * opStat.encodedValues()) / 100; // TODO: modify field value? used with item_maxhp_percent and item_maxmana_percent
      case 12: return 0; // no-op
      case 13: return (stat.encodedValues() * opStat.encodedValues()) / 100;
      default:
        log.error("Unknown op({}) for stat({})", op, stat.debugString());
        return 0;
    }
  }
}
