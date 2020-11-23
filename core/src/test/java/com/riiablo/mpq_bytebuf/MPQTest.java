package com.riiablo.mpq_bytebuf;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.Riiablo;
import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class MPQTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf", Level.DEBUG);
    LogManager.setLevel("com.riiablo.mpq_bytebuf.MPQInputStream", Level.INFO);
    LogManager.setLevel("com.riiablo.mpq_bytebuf.util", Level.WARN);
  }

  private static MPQ load(String mpq) {
    return MPQ.load(new FileHandle("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\" + mpq + ".mpq"));
  }

  @Test
  public void load() {
    load("d2char");
  }

  @Test
  public void loadAll() {
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2xmusic.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2xtalk.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2xvideo.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2char.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2sfx.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2music.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2speech.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2video.mpq"));
  }

  // @Test
  public void yloadAll_old() {
    for (int i = 0; i < 1000; i++) {
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2xmusic.mpq"));
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2xtalk.mpq"));
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2xvideo.mpq"));
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2char.mpq"));
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2sfx.mpq"));
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2music.mpq"));
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2speech.mpq"));
    com.riiablo.mpq.MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2video.mpq"));
    }
  }

  @Test
  public void contains() {
    final MPQ mpq = load("d2char");
    final MPQFileHandle handle = new MPQFileHandle(mpq, "data\\global\\CHARS\\BA\\COF\\BAA11HS.COF");
    Assert.assertTrue(handle.exists());
    System.out.println("searches: " + mpq.searches + ", misses: " + mpq.misses);
  }

  @Test
  public void stress_new() {
    final MPQ d2speech = load("d2speech");
    final long start = System.currentTimeMillis();
    new MPQFileHandle(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_needhelp.wav").readByteBuf().release();
    new MPQFileHandle(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_needkey.wav").readByteBuf().release();
    new MPQFileHandle(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_needmana.wav").readByteBuf().release();
    new MPQFileHandle(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_no.wav").readByteBuf().release();
    new MPQFileHandle(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_notenoughmana.wav").readByteBuf().release();
    new MPQFileHandle(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_nothere.wav").readByteBuf().release();
    new MPQFileHandle(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_notintown.wav").readByteBuf().release();
    new MPQFileHandle(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_noway.wav").readByteBuf().release();
    new MPQFileHandle(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_nowdie.wav").readByteBuf().release();
    System.out.println(System.currentTimeMillis() - start);
  }

  @Test
  public void stress_old() {
    final long start = System.currentTimeMillis();
    Riiablo.mpqs.d2speech.readBytes("data\\local\\sfx\\Common\\Amazon\\Ama_needhelp.wav");
    Riiablo.mpqs.d2speech.readBytes("data\\local\\sfx\\Common\\Amazon\\Ama_needkey.wav");
    Riiablo.mpqs.d2speech.readBytes("data\\local\\sfx\\Common\\Amazon\\Ama_needmana.wav");
    Riiablo.mpqs.d2speech.readBytes("data\\local\\sfx\\Common\\Amazon\\Ama_no.wav");
    Riiablo.mpqs.d2speech.readBytes("data\\local\\sfx\\Common\\Amazon\\Ama_notenoughmana.wav");
    Riiablo.mpqs.d2speech.readBytes("data\\local\\sfx\\Common\\Amazon\\Ama_nothere.wav");
    Riiablo.mpqs.d2speech.readBytes("data\\local\\sfx\\Common\\Amazon\\Ama_notintown.wav");
    Riiablo.mpqs.d2speech.readBytes("data\\local\\sfx\\Common\\Amazon\\Ama_noway.wav");
    Riiablo.mpqs.d2speech.readBytes("data\\local\\sfx\\Common\\Amazon\\Ama_nowdie.wav");
    System.out.println(System.currentTimeMillis() - start);
  }

  // @Test
  public void zloadAll_new() {
    for (int i = 0; i < 1000; i++) {
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2xmusic.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2xtalk.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2xvideo.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2char.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2sfx.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2music.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2speech.mpq"));
    MPQ.load(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2video.mpq"));
    }
  }
}
