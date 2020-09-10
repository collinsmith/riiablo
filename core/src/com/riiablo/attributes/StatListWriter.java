package com.riiablo.attributes;

import java.util.Iterator;

import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.io.BitOutput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class StatListWriter {
  private static final Logger log = LogManager.getLogger(StatListWriter.class);

  public void write(com.riiablo.attributes.StatListRef stats, com.riiablo.attributes.StatRef stat, BitOutput bits, boolean cs) {
    final ItemStatCost.Entry entry = stat.entry();
    if (log.traceEnabled()) log.traceEntry("write(stats: {}, stat: {} ({}), bits: {}, cs: {})", stats, stat, entry, bits, cs);
    if (cs) {
      log.trace("Writing as character stat {}", stat.debugString());
      bits.write63u(stat.encodedParams(), entry.CSvParam);
      bits.write63u(stat.encodedValues(), entry.CSvBits);
    } else {
      log.trace("Writing as standard stat {}", stat.debugString());
      bits.write63u(stat.encodedParams(), entry.Save_Param_Bits);
      bits.write63u((stat.encodedValues() >> entry.ValShift) + entry.Save_Add, entry.Save_Bits);
    }
  }

  public void write(com.riiablo.attributes.StatListRef stats, BitOutput bits, boolean cs) {
    for (Iterator<com.riiablo.attributes.StatRef> it = stats.iterator(); it.hasNext();) {
      final com.riiablo.attributes.StatRef stat = it.next();
      final short id = stat.id();
      try {
        MDC.put("stat", id);
        bits.write15u(id, com.riiablo.attributes.Stat.BITS);
        final byte numEncoded = com.riiablo.attributes.Stat.getNumEncoded(id);
        try {
          if (numEncoded > 1) {
            MDC.put("numEncoded", numEncoded);
            MDC.put("encodedStat", id);
          }
          write(stats, stat, bits, cs);
          for (short j = 1; j < numEncoded; j++) {
            final com.riiablo.attributes.StatRef next = it.next();
            assert next.id() == id + j : String.format(
                "it.next(%s) != %d : getNumEncoded(%s)[%d..%d]", next, id + j, id + j, id, id + numEncoded - 1);
            MDC.put("encodedStat", next.id());
            write(stats, stat, bits, cs);
          }
        } finally {
          MDC.remove("encodedStat");
          MDC.remove("numEncoded");
        }
      } finally {
        MDC.remove("stat");
      }
    }

    bits.write15u(com.riiablo.attributes.Stat.NONE, com.riiablo.attributes.Stat.BITS);
  }

  public void write(com.riiablo.attributes.StatList stats, BitOutput bits, int flags) {
    final int numLists = stats.numLists();
    for (int i = 0; i < numLists; i++) {
      if (((flags >> i) & 1) == 1) {
        try {
          MDC.put("propList", com.riiablo.attributes.StatListFlags.itemToString(i)); // assert only items will be serialized
          write(stats.get(i), bits, false);
        } finally {
          MDC.remove("propList");
        }
      }
    }

    if (flags == com.riiablo.attributes.StatListFlags.FLAG_NONE) {
      bits.write15u(com.riiablo.attributes.Stat.NONE, com.riiablo.attributes.Stat.BITS);
    }
  }

//  public Attributes read(Attributes attrs, BitInput bits, int flags) {
//    final StatList list = attrs.base().stats;
//    read(list, bits, flags);
//    return attrs;
//  }
}
