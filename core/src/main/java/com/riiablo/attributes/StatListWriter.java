package com.riiablo.attributes;

import java.util.Iterator;

import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.io.BitOutput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class StatListWriter {
  private static final Logger log = LogManager.getLogger(StatListWriter.class);

  public StatRef write(StatListRef stats, StatRef stat, BitOutput bits, boolean cs) {
    final ItemStatCost.Entry entry = stat.entry();
    if (log.traceEnabled()) log.traceEntry("write(stats: {}, stat: {} ({}), bits: {}, cs: {})", stats, stat, entry, bits, cs);
    if (cs) {
      log.trace("Writing as character stat {}", stat.debugString());
      bits.write63u(stat.encodedParams(), entry.CSvParam);
      bits.write63u(stat.encodedValues(), entry.CSvBits);
    } else {
      log.trace("Writing as standard stat {}", stat.debugString());
      bits.write63u(stat.encodedParams(), entry.Save_Param_Bits);
      bits.write63u(Stat.decode(stat.id(), stat.encodedValues()) + entry.Save_Add, entry.Save_Bits);
    }
    return stat;
  }

  public StatRef write(StatListRef stats, short stat, BitOutput bits, boolean cs) {
    return write(stats, stats.get(stat), bits, cs);
  }

  public void write(StatListRef stats, BitOutput bits, boolean cs) {
    for (Iterator<StatRef> it = stats.iterator(); it.hasNext();) {
      final StatRef stat = it.next();
      final short id = stat.id();
      try {
        MDC.put("stat", id);
        bits.write15u(id, Stat.BITS);
        final byte numEncoded = Stat.getNumEncoded(id);
        try {
          if (numEncoded > 1) {
            MDC.put("numEncoded", numEncoded);
            MDC.put("encodedStat", id);
          }
          write(stats, stat, bits, cs);
          for (short j = 1; j < numEncoded; j++) {
            final StatRef next = it.next();
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

    bits.write15u(Stat.NONE, Stat.BITS);
  }

  public void write(StatList stats, BitOutput bits, int flags) {
    final int numLists = stats.numLists();
    for (int i = 0; i < numLists; i++) {
      if (((flags >> i) & 1) == 1) {
        try {
          MDC.put("propList", StatListFlags.itemToString(i)); // assert only items will be serialized
          write(stats.get(i), bits, false);
        } finally {
          MDC.remove("propList");
        }
      }
    }

    if (flags == StatListFlags.FLAG_NONE) {
      bits.write15u(Stat.NONE, Stat.BITS);
    }
  }

//  public Attributes read(Attributes attrs, BitInput bits, int flags) {
//    final StatList list = attrs.base().stats;
//    read(list, bits, flags);
//    return attrs;
//  }
}
