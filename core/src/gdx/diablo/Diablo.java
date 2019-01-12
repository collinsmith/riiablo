package gdx.diablo;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.Viewport;

import gdx.diablo.audio.MusicController;
import gdx.diablo.codec.StringTBLs;
import gdx.diablo.console.RenderedConsole;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.map2.DT1s;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class Diablo {
  private Diablo() {}

  public static final int VIRTUAL_WIDTH  = (int) (480 * 16 / 9f);
  public static final int VIRTUAL_HEIGHT = 480;

  public static final int VIRTUAL_WIDTH_CENTER  = VIRTUAL_WIDTH  >>> 1;
  public static final int VIRTUAL_HEIGHT_CENTER = VIRTUAL_HEIGHT >>> 1;

  public static Client                client;
  public static Palettes              palettes;
  public static Colormaps             colormaps;
  public static Fonts                 fonts;
  public static Files                 files;
  public static COFs                  cofs;
  public static FileHandle            home;
  public static MPQFileHandleResolver mpqs;
  public static StringTBLs            string;
  public static Client.InputProcessor input;
  public static PaletteIndexedBatch   batch;
  public static ShaderProgram         shader;
  public static ShapeRenderer         shapes;
  public static Viewport              viewport;
  public static RenderedConsole       console;
  public static AssetManager          assets;
  public static MusicController       music;
  public static GdxCommandManager     commands;
  public static GdxCvarManager        cvars;
  public static GdxKeyMapper          keys;
  public static I18NBundle            bundle;
  public static Colors                colors;
  public static DT1s                  dt1s;
  public static Cursor                cursor;
  public static Audio                 audio;
}
