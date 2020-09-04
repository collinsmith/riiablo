package com.riiablo.attributes;

import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.io.BitInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class StatListReader {
  private static final Logger log = LogManager.getLogger(StatListReader.class);

  public StatGetter read(StatListBuilder stats, short stat, BitInput bits, boolean cs) {
    log.traceEntry("read(stats: {}, stat: {}, bits: {})", stats, stat, bits);
    final ItemStatCost.Entry entry = Stat.entry(stat);
    final int param, value;
    if (cs) {
      log.trace("Reading character save stat...");
      assert !entry.CSvSigned : "entry.CSvSigned(" + entry.CSvSigned + ") unsupported";
      param = (int) bits.read63u(entry.CSvParam);
      value = (int) bits.read63u(entry.CSvBits);
    } else {
      log.trace("Reading stat...");
      param = (int) bits.read63u(entry.Save_Param_Bits);
      value = (int) bits.read63u(entry.Save_Bits) - entry.Save_Add;
    }
    return stats.put(stat, param, value).last();
  }

  public StatListGetter read(StatListBuilder stats, BitInput bits, boolean cs) {
    log.traceEntry("read(stats: {}, bits: {})", stats, bits);
    for (short stat; (stat = bits.read15u(Stat.BITS)) != Stat.NONE;) {
      try {
        MDC.put("stat", stat);
        final byte numEncoded = Stat.getNumEncoded(stat);
        try {
          if (numEncoded > 1) {
            MDC.put("numEncoded", numEncoded);
            MDC.put("encodedStat", stat);
          }
          for (short j = stat, s = (short) (stat + numEncoded); j < s; j++) {
            if (j > stat) MDC.put("encodedStat", j);
            read(stats, j, bits, cs);
          }
        } finally {
          MDC.remove("encodedStat");
          MDC.remove("numEncoded");
        }
      } finally {
        MDC.remove("stat");
      }
    }

    return stats.get();
  }

  /**
   * Reads a single property list into {@link Attributes#base()}
   */
  public StatListGetter read(Attributes attrs, BitInput bits, boolean cs) {
    final StatListBuilder builder = attrs.base().clear().builder();
    return read(builder, bits, cs);
  }

  /**
   * Reads property lists into {@link Attributes#list()}
   */
  public StatList read(Attributes attrs, BitInput bits, int flags, int maxLists) {
    final StatList stats = attrs.list().clear();
    for (int i = 0; i < maxLists; i++) {
      final StatListBuilder builder = stats.buildList();
      if (((flags >> i) & 1) == 1) {
        try {
          MDC.put("propList", StatListFlags.itemToString(i));
          read(builder, bits, false);
        } finally {
          MDC.remove("propList");
        }
      }
    }

    return stats.freeze();
  }
}
