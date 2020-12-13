package com.riiablo.excel;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.io.ByteOutput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class BinGenerator {
  private static final Logger log = LogManager.getLogger(BinGenerator.class);

  String sourcePackage = "com.riiablo.excel.txt";
  String excelPath = "DATA\\GLOBAL\\EXCEL2".toLowerCase(); // string will be defined elsewhere in caps
  FileHandle binDir;

  BinGenerator configure(FileHandle binDir) {
    this.binDir = binDir;
    return this;
  }

  public void generateBins() {
    log.info("Generating bins for {}...", sourcePackage);

    FileHandle excelDir = binDir.child(excelPath);
    log.trace("excelDir: {}", excelDir);
    excelDir.mkdirs();

    generateBin(excelDir, null);
  }

  public <E extends Excel.Entry, S extends Serializer<E>, T extends Excel<E, S>>
  void generateBin(FileHandle excelDir, T excel) {
    final Class<? extends Excel> excelClass = excel.excelClass();
    log.trace("excel: {}", excelClass.getCanonicalName());

    FileHandle binFile = excelDir.child(excelClass.getSimpleName() + "." + "bin");
    log.trace("binFile: {}", binFile);

    ByteOutput out = ByteOutput.wrap(Unpooled.buffer());
    S serializer = excel.newSerializer();
    for (E entry : excel) {
      // serializer.writeBin(entry, out);
    }

    log.trace("dump of {}:\n{}", binFile, ByteBufUtil.prettyHexDump(out.buffer()));
  }
}
