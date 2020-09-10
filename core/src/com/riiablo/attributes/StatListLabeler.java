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

  private final int[] dgrpCacheSize = new int[com.riiablo.attributes.Stat.NUM_GROUPS];
  private final int[][] dgrpCache = new int[com.riiablo.attributes.Stat.NUM_GROUPS][]; {
    for (int i = 0, s = com.riiablo.attributes.Stat.NUM_GROUPS; i < s; i++) {
      dgrpCache[i] = new int[com.riiablo.attributes.Stat.getNumGrouped(i)];
    }
  }

  private final Tuple[] desc = new Tuple[com.riiablo.attributes.StatList.MAX_SIZE]; {
    for (int i = 0; i < com.riiablo.attributes.StatList.MAX_SIZE; i++) {
      desc[i] = new Tuple();
    }
  }
  private int descCount;

  protected com.riiablo.attributes.StatFormatter formatter;

  public StatListLabeler() {}

  public StatListLabeler(com.riiablo.attributes.StatFormatter formatter) {
    this.formatter = formatter;
  }

  public CharSequence createDebugLabel(StatListRef stats, com.riiablo.attributes.Attributes opAttrs) {
    builder.setLength(0);
    for (com.riiablo.attributes.StatRef stat : stats) {
      CharSequence line = formatter.format(stat, opAttrs);
      builder.append(line).append('\n');
    }

    if (builder.length() > 0) builder.setLength(builder.length() - 1);
    return builder.toString();
  }

  public CharSequence createLabel(StatListRef stats, com.riiablo.attributes.Attributes opAttrs) {
    log.traceEntry("createLabel(stats: {}, opAttrs: {})", stats, opAttrs);
//    assert stats.parent().numLists() == 1 : "Parent StatList contains more than 1 list";

    clear();
    for (com.riiablo.attributes.StatList.IndexIterator it = stats.indexIterator(); it.hasNext();) {
      final int index = it.next();
      final com.riiablo.attributes.StatRef stat = stats.get(index);
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

        final int expectedEncoded = com.riiablo.attributes.Stat.getNumEncoded(statId);
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
              case com.riiablo.attributes.Stat.mindamage: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModMinDamage", "strModMinDamageRange"));
                continue;
              }
              case com.riiablo.attributes.Stat.firemindam: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModFireDamage", "strModFireDamageRange"));
                continue;
              }
              case com.riiablo.attributes.Stat.lightmindam: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModLightningDamage", "strModLightningDamageRange"));
                continue;
              }
              case com.riiablo.attributes.Stat.magicmindam: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModMagicDamage", "strModMagicDamageRange"));
                continue;
              }
              case com.riiablo.attributes.Stat.coldmindam: {
                if (log.debugEnabled()) log.debug(
                    "appending {}, {}, {}",
                    stat.debugString(),
                    stats.get(index + 1).debugString(),
                    stats.get(index + 2).debugString());
                add(entry.descpriority, formatter.formatEncoded(stats, index, encoded,
                    "strModColdDamage", "strModColdDamageRange"));
                continue;
              }
              case com.riiablo.attributes.Stat.poisonmindam: {
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
    for (int i = 1; i < com.riiablo.attributes.Stat.NUM_GROUPS; i++) {
      final int cacheSize = this.dgrpCacheSize[i];
      if (cacheSize == 0) continue;
      final int[] cache = this.dgrpCache[i];
      if (cacheSize < cache.length || !allEqual(stats, cache, cacheSize)) {
        for (int j = 0; j < cacheSize; j++) {
          final com.riiablo.attributes.StatRef cachedStat = stats.get(cache[j]);
          if (log.traceEnabled()) log.trace("pushback dgrp {}[{}]: {}", i, j, cachedStat.debugString());
          if (log.debugEnabled()) log.debug("appending {}", cachedStat.debugString());
          add(cachedStat.entry().descpriority, formatter.format(cachedStat, opAttrs));
        }

        continue;
      }

      log.trace("combining dgrp {}: {{}}", i, StringUtils.join(cache, ',', 0, cacheSize));
      final com.riiablo.attributes.StatRef cachedStat = stats.get(cache[0]);
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

  private static boolean allEqual(StatListRef stats, int[] dgrpCache, int dgrpCacheSize) {
    final com.riiablo.attributes.StatList parent = stats.parent();
    final int index0 = dgrpCache[0];
    final int value0 = parent.encodedValues(index0);
    final int param0 = parent.encodedParams(index0);
    for (int i = 1; i < dgrpCacheSize; i++) {
      final com.riiablo.attributes.StatRef sibling = stats.get(dgrpCache[i]);
      if (sibling.encodedValues() != value0 || sibling.encodedParams() != param0) {
        return false;
      }
    }

    return true;
  }

  private void clear() {
    descCount = 0;
    for (int i = 0; i < com.riiablo.attributes.Stat.NUM_GROUPS; i++) {
      Arrays.fill(dgrpCacheSize, 0);
    }
  }

  private void add(int priority, String str) {
    final Tuple tuple = desc[descCount++];
    tuple.descPriority = priority;
    tuple.descString = str;
  }

  private static final class Tuple implements Comparable<Tuple> {
    int descPriority;
    String descString;

    @Override
    public int compareTo(Tuple other) {
      return NumberUtils.compare(descPriority, other.descPriority);
    }

    @Override
    public String toString() {
      return descString;
    }
  }
}
