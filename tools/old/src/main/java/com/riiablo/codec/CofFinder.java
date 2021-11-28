package com.riiablo.codec;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.riiablo.Riiablo;
import com.riiablo.mpq.MPQFileHandleResolver;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintWriter;
import java.util.Arrays;

public class CofFinder extends ApplicationAdapter {
  private static final String TAG = "CofFinder";

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new CofFinder(args), config);
  }

  String[] cofs;

  CofFinder(String[] args) {
    Riiablo.home = new FileHandle(args[0]);
    if (args.length > 1) {
      this.cofs = Arrays.copyOfRange(args, 1, args.length);
    }
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    Riiablo.home = Gdx.files.absolute(Riiablo.home.path());
    Riiablo.assets = new AssetManager();
    Riiablo.mpqs = new MPQFileHandleResolver();

    String[] cofd2Names = {
        "chars_cof", "cmncof_a1", "cmncof_a2", "cmncof_a3",
        "cmncof_a4", "cmncof_a5", "cmncof_a6", "cmncof_a7",
    };
    Array<Pair<String, COFD2>> cofd2s = new Array<>();
    for (String cofd2Name : cofd2Names) {
      String path = "data\\global\\" + cofd2Name + ".d2";
      FileHandle handle = Riiablo.mpqs.resolve(path);
      COFD2 cofd2 = COFD2.loadFromFile(handle);
      cofd2s.add(Pair.of(cofd2Name, cofd2));
    }

    String[] cofDirs = { "chars", "objects", "monsters" };

    if (cofs == null) {
      for (Pair<String, COFD2> pair : cofd2s) {
        dump(pair.getValue(), pair.getKey());
      }
    } else {
      for (String cof : cofs) {
        Gdx.app.log(TAG, cof);
        int i = 0;
        for (Pair<String, COFD2> pair : cofd2s) {
          if (lookup(cof, pair.getValue(), pair.getKey()) != null) i++;
        }

        for (String cofDir : cofDirs) {
          if (lookup(cof, "data\\global\\" + cofDir + "\\") != null) i++;
        }
        if (i == 0) Gdx.app.log(TAG, cof + " was not found in any file!");
      }
    }

    Gdx.app.exit();
  }

  private void dump(COFD2 lib, String libName) {
    if (lib == null) return;
    FileHandle handle = Gdx.files.local(libName + ".tmp");
    Gdx.app.log(TAG, "Writing " + lib.getNumEntries() + " to " + handle);
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(handle.write(false));
      for (COFD2.Entry entry : lib.entries) {
        writer.println(entry.cofName + '\t' + entry.cofSize + '\t' + entry.cof.toString());
      }
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  private COF lookup(String cof, COFD2 lib, String libName) {
    if (lib == null) return null;
    COF resolved = lib.lookup(cof);
    if (resolved != null) Gdx.app.log(TAG, "  " + libName + " : " + resolved);
    return resolved;
  }

  private COF lookup(String cof, String path) {
    String fileName = cof.substring(0, 2) + "\\cof\\" + cof + ".cof";
    FileHandle handle = Riiablo.mpqs.resolve(path + fileName);
    COF resolved = handle != null ? COF.loadFromFile(handle) : null;
    if (resolved != null) Gdx.app.log(TAG, "  " + handle + " : " + resolved);
    return resolved;
  }

  @Override
  public void dispose() {
    Riiablo.assets.dispose();
  }
}
