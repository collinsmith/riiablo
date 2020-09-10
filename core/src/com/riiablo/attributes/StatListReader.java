package com.riiablo.attributes;

import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.io.BitInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class StatListReader {
  private static final Logger log = LogManager.getLogger(StatListReader.class);

  public com.riiablo.attributes.StatRef read(com.riiablo.attributes.StatListRef stats, short stat, BitInput bits, boolean cs) {
    final ItemStatCost.Entry entry = com.riiablo.attributes.Stat.entry(stat);
    log.traceEntry("read(stats: {}, stat: {} ({}), bits: {}, cs: {})", stats, stat, entry, bits, cs);
    final int encodedParams, encodedValues;
    if (cs) {
      log.trace("Reading as character stat...");
      encodedParams = (int) bits.read63u(entry.CSvParam);
      encodedValues = (int) bits.read63u(entry.CSvBits);
    } else {
      log.trace("Reading as standard stat...");
      encodedParams = (int) bits.read63u(entry.Save_Param_Bits);
      encodedValues = (int) (bits.read63u(entry.Save_Bits) - entry.Save_Add) << entry.ValShift;
    }
    return stats.putEncoded(stat, encodedParams, encodedValues);
  }

  public com.riiablo.attributes.StatListRef read(com.riiablo.attributes.StatListRef stats, BitInput bits, boolean cs) {
    log.traceEntry("read(stats: {}, bits: {}, cs: {})", stats, bits, cs);
    for (short stat; (stat = bits.read15u(com.riiablo.attributes.Stat.BITS)) != com.riiablo.attributes.Stat.NONE;) {
      try {
        MDC.put("stat", stat);
        final byte numEncoded = com.riiablo.attributes.Stat.getNumEncoded(stat);
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

    return stats;
  }

  public com.riiablo.attributes.StatList read(com.riiablo.attributes.StatList stats, BitInput bits, int flags) {
    final int maxLists = stats.maxLists();
    for (int i = 0; i < maxLists; i++) {
      final com.riiablo.attributes.StatListRef list = stats.buildList(); // must be called to init list (even empty)
      if (((flags >> i) & 1) == 1) {
        try {
          MDC.put("propList", StatListFlags.itemToString(i)); // assert only items will be serialized
          read(list, bits, false);
        } finally {
          MDC.remove("propList");
        }
      }
    }

    return stats.freeze();
  }
}
