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
import com.riiablo.util.DebugUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintWriter;
import java.util.Arrays;

public class D2Finder extends ApplicationAdapter {
  private static final String TAG = "D2Viewer";

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new D2Finder(args), config);
  }

  String[] cofs;

  D2Finder(String[] args) {
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

    String[] d2Names = { "animdata", "eanimdata" };
    Array<Pair<String, D2>> d2s = new Array<>();
    for (String d2Name : d2Names) {
      String path = "data\\global\\" + d2Name + ".d2";
      FileHandle handle = Riiablo.mpqs.resolve(path);
      D2 d2 = D2.loadFromFile(handle);
      d2s.add(Pair.of(d2Name, d2));
    }

    if (cofs == null) {
      for (Pair<String, D2> pair : d2s) {
        dump(pair.getValue(), pair.getKey());
      }
    } else {
      for (String cof : cofs) {
        Gdx.app.log(TAG, cof);
        int i = 0;
        for (Pair<String, D2> pair : d2s) {
          if (lookup(cof, pair.getValue(), pair.getKey()) != null) i++;
        }
        if (i == 0) Gdx.app.log(TAG, cof + " was not found in any file!");
      }
    }

    Gdx.app.exit();
  }

  private void dump(D2 lib, String libName) {
    if (lib == null) return;
    FileHandle handle = Gdx.files.local(libName + ".tmp");
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(handle.write(false));
      writer.println("cof"
          + '\t' + "framesPerDir"
          + '\t' + "speed"
          + '\t' + "unk1"
          + '\t' + "data"
      );
      for (D2.Block block : lib.blocks) {
        Gdx.app.log(TAG, "Writing " + block.numEntries + " to " + handle);
          for (D2.Entry entry : block.entries) {
            writer.println(entry.cof
                + '\t' + entry.framesPerDir
                + '\t' + (entry.speed & 0xFF)
                + '\t' + (entry.unk1 & 0xFF)
                + '\t' + DebugUtils.toByteArray(entry.data)
            );
          }
      }
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  private D2.Entry lookup(String cof, D2 lib, String libName) {
    if (lib == null) return null;
    D2.Entry resolved = lib.getEntry(cof);
    if (resolved != null) Gdx.app.log(TAG, "  " + libName + " : " + resolved);
    return resolved;
  }

  @Override
  public void dispose() {
    Riiablo.assets.dispose();
  }
}
