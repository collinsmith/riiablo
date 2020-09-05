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
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.mpq.MPQFileHandleResolver;

public class PropertiesGeneratorTest {
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
    LogManager.setLevel("com.riiablo.attributes", Level.DEBUG);
    LogManager.setLevel("com.riiablo.attributes.PropertiesGenerator", Level.TRACE);
  }

  private void test(String code) {
    PropertiesGenerator properties = new PropertiesGenerator();
    GemGenerator gems = new GemGenerator(properties);
    Attributes attrs = Attributes.gemAttributes();
    gems.set(attrs, code);

    StatListLabeler labeler = new StatListLabeler(new StatFormatter());
    for (int i = 0; i < StatListFlags.NUM_GEM_LISTS; i++) {
      final StatListGetter stats = attrs.list(i);
      System.out.println(labeler.createDebugLabel(stats, null));
      System.out.println(labeler.createLabel(stats, null));
      System.out.println("----------");
    }
  }

  @Test
  public void Flawed_Ruby() {
    test("gfr");
  }

  @Test
  public void Perfect_Diamond() {
    test("gpw");
  }
}