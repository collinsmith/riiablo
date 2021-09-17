package com.riiablo.mpq_bytebuf;

import org.junit.jupiter.api.*;

import com.badlogic.gdx.utils.IntArray;

import com.riiablo.RiiabloTest;

public class MpqDiagnostics extends RiiabloTest {
  @Test
  void run() {
    IntArray cSizes = new IntArray();
    IntArray fSizes = new IntArray();
    MpqFileResolver mpqs = new MpqFileResolver();
    for (Mpq mpq : mpqs.mpqs) {
      for (Mpq.Block block : mpq.blockTable) {
        if (block == null) continue;
        cSizes.add(block.CSize);
        fSizes.add(block.FSize);
        if (block.FSize > 2_000_000) {
          System.out.println(block.offset);
        }
      }
    }

    // for (int i = 0, s = fSizes.size; i < s; i++) {
    //   System.out.println(fSizes.get(i));
    // }
  }
}
