package com.riiablo.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectIntMap;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class TsvParserTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.table.TsvParser", Level.TRACE);
  }

  @Test
  public void monstats_field_names() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    System.out.println(parser.fieldNames());
  }

  @Test
  public void monstats_field_indexes() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    List<ObjectIntMap.Entry<String>> fieldIds = new ArrayList<>();
    for (final ObjectIntMap.Entry<String> entry : parser.fieldIds) {
      // ObjectIntMap iterator reuses entry -- create new instances and copy data
      fieldIds.add(new ObjectIntMap.Entry<String>() {{
        key = entry.key;
        value = entry.value;
      }});
    }
    Collections.sort(fieldIds, new Comparator<ObjectIntMap.Entry<String>>() {
      @Override
      public int compare(ObjectIntMap.Entry<String> o1, ObjectIntMap.Entry<String> o2) {
        return Integer.compare(o1.value, o2.value);
      }
    });
    System.out.println(fieldIds);
  }

  @Test
  public void monstats_record_indexes() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    System.out.println(parser.recordNames());
  }

  @Test
  public void monstats_record_names() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    parser.primaryKey("ID");
    System.out.println(parser.recordNames());
  }

  @Test
  public void monstats_skeleton1_tokens() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    System.out.println(parser.tokens(0));
  }
}
