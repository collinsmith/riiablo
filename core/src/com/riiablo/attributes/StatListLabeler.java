package com.riiablo.attributes;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class StatListLabeler {
  private static final Logger log = LogManager.getLogger(StatListLabeler.class);

  private final StringBuilder builder = new StringBuilder(256);

  private final int[] dgrpCacheSize = new int[Stat.NUM_GROUPS];
  private final int[][] dgrpCache = new int[Stat.NUM_GROUPS][]; {
    for (int i = 0, s = Stat.NUM_GROUPS; i < s; i++) {
      dgrpCache[i] = new int[Stat.getNumGrouped(i)];
    }
  }

  private final Tuple[] desc = new Tuple[StatList.MAX_STATS]; {
    for (int i = 0; i < StatList.MAX_STATS; i++) {
      desc[i] = new Tuple();
    }
  }
  private int descCount;

  protected StatFormatter formatter = new StatFormatter();

  public CharSequence createDebugLabel(StatListGetter stats, Attributes opAttrs) {
    builder.setLength(0);
    for (StatGetter stat : stats) {
      CharSequence line = formatter.format(stat, opAttrs);
      builder.append(line).append('\n');
    }

    if (builder.length() > 0) builder.setLength(builder.length() - 1);
    return builder.toString();
  }

  public CharSequence createLabel(StatListGetter stats, Attributes opAttrs) {
    log.traceEntry("createLabel(stats: {}, opAttrs: {})", stats, opAttrs);
    assert stats.parent().numLists() == 1 : "Parent StatList contains more than 1 list";

    descCount = 0;
    for (StatList.IndexIterator it = stats.indexIterator(); it.hasNext();) {
      final int index = it.next();
      final StatGetter stat = stats.get(index);
      final short statId = stat.id();
      final ItemStatCost.Entry entry = stat.entry();
      try {
        MDC.put("stat", statId);
        if (log.debugEnabled()) log.debug(stat.debugString());
        final int dgrp = entry.dgrp;
        log.trace("dgrp: {}", dgrp);
        if (dgrp > 0) {
          final int cacheIndex = dgrpCacheSize[dgrp]++;
          dgrpCache[dgrp][cacheIndex] = index;
          if (log.debugEnabled()) log.debug("caching {}", stat.debugString());
          continue;
        }

        final int expectedEncoded = Stat.getNumEncoded(statId);
        if (expectedEncoded > 1) {
          int encoded = 1;
          for (int nextIndex = index + encoded, nextStat = statId + encoded;
              encoded < expectedEncoded && it.hasNext();
              encoded++, nextIndex++, nextStat++) {
            if (stats.parent().id(it.next()) != nextStat) {
              break;
            }
          }

          if (encoded == expectedEncoded) {
            switch (statId) {
              case Stat.mindamage: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModMinDamage", "strModMinDamageRange"));
                continue;
              }
              case Stat.firemindam: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModFireDamage", "strModFireDamageRange"));
                continue;
              }
              case Stat.lightmindam: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModLightningDamage", "strModLightningDamageRange"));
                continue;
              }
              case Stat.magicmindam: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModMagicDamage", "strModMagicDamageRange"));
                continue;
              }
              case Stat.coldmindam: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString(),
                    stats.get(index + 2).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModColdDamage", "strModColdDamageRange"));
                continue;
              }
              case Stat.poisonmindam: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString(),
                    stats.get(index + 2).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModPoisonDamage", "strModPoisonDamageRange"));
                continue;
              }
              default: // fall-through and append normally
                log.warn("stat({}) numEncoded({}) is unsupported", statId, expectedEncoded);
            }
          } else {
            final int pushback = encoded - 1;
            log.trace("cannot combine, pushing back {}", pushback);
            it.pushback(pushback);
          }

          // consume them
          // TODO: only output distinct strings (item_throw_ has none)
          // {Stat.mindamage, Stat.secondary_mindamage, Stat.item_throw_mindamage}
          // {Stat.maxdamage, Stat.secondary_maxdamage, Stat.item_throw_maxdamage}
          continue;
        }

        if (log.debugEnabled()) log.debug("appending {}", stat.debugString());
        add(entry.descpriority, formatter.format(stat, opAttrs));
      } finally {
        MDC.remove("stat");
      }
    }

    log.trace("groups: {}", Arrays.toString(dgrpCacheSize));
    for (int i = 1; i < Stat.NUM_GROUPS; i++) {
      final int cacheSize = this.dgrpCacheSize[i];
      if (cacheSize == 0) continue;
      final int[] cache = this.dgrpCache[i];
      if (cacheSize < cache.length || !allEqual(stats, cache, cacheSize)) {
        for (int j = 0; j < cacheSize; j++) {
          final StatGetter cachedStat = stats.get(cache[j]);
          if (log.traceEnabled()) log.trace("pushback dgrp {}[{}]: {}", i, j, cachedStat.debugString());
          if (log.debugEnabled()) log.debug("appending {}", cachedStat.debugString());
          add(cachedStat.entry().descpriority, formatter.format(cachedStat, opAttrs));
        }

        continue;
      }

      log.trace("combining dgrp {}: {{}}", i, StringUtils.join(cache, ',', 0, cacheSize));
      final StatGetter cachedStat = stats.get(cache[0]);
      final ItemStatCost.Entry entry = cachedStat.entry();
      add(entry.descpriority,
          formatter.format(
              cachedStat,
              opAttrs,
              entry.dgrpfunc,
              entry.dgrpval,
              entry.dgrpstrpos,
              entry.dgrpstrneg,
              entry.dgrpstr2));
    }

    Arrays.sort(desc, 0, descCount);
    builder.setLength(0);
    for (int i = 0, s = descCount; i < s; i++) {
      builder.append(desc[i]).append('\n');
    }
    if (builder.length() > 0) builder.setLength(builder.length() - 1);
    return builder.toString();
  }

  private static boolean allEqual(StatListGetter stats, int[] dgrpCache, int dgrpCacheSize) {
    final StatList parent = stats.parent();
    final int index0 = dgrpCache[0];
    final int value0 = parent.value(index0);
    final int param0 = parent.param(index0);
    for (int i = 1; i < dgrpCacheSize; i++) {
      final StatGetter sibling = stats.get(dgrpCache[i]);
      if (sibling.value() != value0 || sibling.param() != param0) {
        return false;
      }
    }

    return true;
  }

  private void add(int priority, String str) {
    final Tuple tuple = desc[descCount++];
    tuple.descPriotity = priority;
    tuple.descString = str;
  }

  private static final class Tuple implements Comparable<Tuple> {
    int descPriotity;
    String descString;

    @Override
    public int compareTo(Tuple other) {
      return NumberUtils.compare(descPriotity, other.descPriotity);
    }

    @Override
    public String toString() {
      return descString;
    }
  }
}
