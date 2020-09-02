package com.riiablo.attributes;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;

import com.riiablo.Files;
import com.riiablo.Riiablo;
import com.riiablo.codec.StringTBLs;
import com.riiablo.io.BitInput;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.mpq.MPQFileHandleResolver;

public class StatListReaderTest {
  @BeforeClass
  public static void setup() {
    Gdx.app = new HeadlessApplication(new ApplicationAdapter() {});
    Riiablo.home = Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II");
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.string = new StringTBLs(Riiablo.mpqs);
    Riiablo.files = new Files();
  }

  @AfterClass
  public static void teardown() {
    Gdx.app.exit();
  }

  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.attributes", Level.TRACE);
  }

  @Test
  public void read_char_stats() {
    final byte[] bytes = {
        (byte) 0x00, (byte) 0xD4, (byte) 0x08, (byte) 0x30, (byte) 0x82,
        (byte) 0x80, (byte) 0x0C, (byte) 0x06, (byte) 0xD8, (byte) 0x65,
        (byte) 0x80, (byte) 0x7B, (byte) 0xDA, (byte) 0x1C, (byte) 0x00,
        (byte) 0xA0, (byte) 0x1C, (byte) 0x08, (byte) 0x2A, (byte) 0xC3,
        (byte) 0x45, (byte) 0x02, (byte) 0x00, (byte) 0x6A, (byte) 0xA0,
        (byte) 0xC0, (byte) 0xFD, (byte) 0x94, (byte) 0x2C, (byte) 0x00,
        (byte) 0x70, (byte) 0x10, (byte) 0x0C, (byte) 0xB4, (byte) 0x0D,
        (byte) 0x98, (byte) 0x73, (byte) 0x78, (byte) 0xCE, (byte) 0x1E,
        (byte) 0xC4, (byte) 0x6C, (byte) 0x25, (byte) 0xF8, (byte) 0x0F
    };

    final BitInput bits = ByteInput.wrap(bytes).unalign();
    final Attributes attrs = Attributes.aggregateAttributes();
    final StatListReader reader = new StatListReader();
    reader.read(attrs.base().buildList(), bits);
  }
}