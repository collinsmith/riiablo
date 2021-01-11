package com.riiablo.table;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.io.ByteOutput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class BinGenerator {
  private static final Logger log = LogManager.getLogger(BinGenerator.class);
  String excelPath = "DATA\\GLOBAL\\EXCEL3".toLowerCase(); // string will be defined elsewhere in caps

  void generate(FileHandle dst) {
    if (!dst.exists()) throw new IllegalStateException("dst(" + dst + ") does not exist!");
    dst = dst.child(excelPath);
    dst.mkdirs();
    log.trace("dst: {}", dst);

    for (Table table : TableManifest.TABLES) {
      generateBin(table, dst.child(table.getClass().getSimpleName() + ".bin"));
    }
  }

  void generateBin(Table table, FileHandle dst) {
    log.trace("generating {}", dst);

    Serializer serializer = table.newSerializer();
    ByteOutput out = ByteOutput.wrap(Unpooled.buffer());
    for (Object record : table) {
      // TODO: ByteOutput needs to implement DataOutput
      // serializer.writeRecord(record, out);
    }

    log.trace("dump of {}:\n{}", dst, ByteBufUtil.prettyHexDump(out.buffer()));
  }
}
