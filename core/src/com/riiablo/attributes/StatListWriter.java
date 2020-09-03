package com.riiablo.attributes;

import java.util.Iterator;

import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.io.BitOutput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class StatListWriter {
  private static final Logger log = LogManager.getLogger(StatListWriter.class);

  public void write(StatListGetter stats, StatGetter stat, BitOutput bits, boolean cs) {
    log.traceEntry("write(stats: {}, stat: {}, bits: {})", stats, stat, bits);
    final ItemStatCost.Entry entry = stat.entry();
    if (cs) {
      if (log.traceEnabled()) log.trace("Writing character save stat {}", stat.debugString());
      assert !entry.CSvSigned : "entry.CSvSigned(" + entry.CSvSigned + ") unsupported";
      bits.write63u(stat.param(), entry.CSvParam);
      bits.write63u(stat.value(), entry.CSvBits);
    } else {
      if (log.traceEnabled()) log.trace("Writing stat {}", stat.debugString());
      bits.write63u(stat.param(), entry.Save_Param_Bits);
      bits.write63u(stat.value() + entry.Save_Add, entry.Save_Bits);
    }
  }

  public void write(StatListGetter stats, BitOutput bits, boolean cs) {
    for (Iterator<StatGetter> it = stats.iterator(); it.hasNext();) {
      final StatGetter stat = it.next();
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
            final StatGetter next = it.next();
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

  public void write(Attributes attrs, BitOutput bits, boolean cs) {
    final StatList stats = attrs.base();
    write(stats.get(0), bits, cs);
  }

  public void write(Attributes attrs, BitOutput bits, int flags, int maxLists) {
    final StatList stats = attrs.list();
    for (int i = 0; i < maxLists; i++) {
      if (((flags >> i) & 1) == 1) {
        try {
            MDC.put("propList", StatListFlags.itemToString(i));
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
}
