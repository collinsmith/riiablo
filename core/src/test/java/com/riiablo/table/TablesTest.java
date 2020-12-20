package com.riiablo.table;

import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.table.schema.MonStats;
import com.riiablo.table.table.MonStatsTable;

public class TablesTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.table.Tables", Level.TRACE);
  }

  @Test
  public void monstats() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    MonStatsTable table = Tables.loadTsv(TableManifest.monstats, parser);
    MonStats record = table.get(0);
    System.out.println(record.Id);
    System.out.println(record.hcIdx);
    System.out.println(Arrays.toString(record.Level));
  }

  @Test
  public void monstats_random_access() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    TableManifest.monstats.parser = null;
    MonStatsTable table = Tables.loadTsv(TableManifest.monstats, parser);
    MonStats record;
    record = table.get(54);
    System.out.println(record);
    record = table.get(311);
    System.out.println(record);
    record = table.get(411);
    System.out.println(record);
    record = table.get(5);
    System.out.println(record);
    record = table.get(700);
    System.out.println(record);
    record = table.get(578);
    System.out.println(record);
  }
}
