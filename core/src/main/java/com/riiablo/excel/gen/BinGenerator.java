package com.riiablo.excel.gen;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class BinGenerator {
  private static final Logger log = LogManager.getLogger(BinGenerator.class);

  public String serializerPackage;
  public FileHandle serializerDir;

  public String excelPath = "DATA\\GLOBAL\\EXCEL";

  public FileHandle assetsDir;

  public void generateBins() {
    log.info("Generating bins for {}...", serializerPackage);

  }
}
