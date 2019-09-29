package com.riiablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.riiablo.codec.Animation;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC6;
import com.riiablo.codec.DCC;
import com.riiablo.codec.FontTBL;
import com.riiablo.codec.Index;
import com.riiablo.codec.Palette;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.BitmapFontLoader;
import com.riiablo.loader.COFLoader;
import com.riiablo.loader.DC6Loader;
import com.riiablo.loader.DCCLoader;
import com.riiablo.loader.IndexLoader;
import com.riiablo.loader.PaletteLoader;
import com.riiablo.mpq.MPQFileHandleResolver;

public class AnimationTool extends ApplicationAdapter {
  private static final String TAG = "AnimationTool";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = TAG;
    config.resizable = true;
    config.width = 256;
    config.height = 384;
    config.vSyncEnabled = false;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new AnimationTool(args[0]), config);
  }

  FileHandle home;
  Animation anim;
  COF cof;
  float accumulator;
  float lastUpdate;
  Stage stage;
  boolean lockFps;

  AnimationTool(String home) {
    this.home = new FileHandle(home);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    Riiablo.home = home = Gdx.files.absolute(home.path());
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.assets = new AssetManager();
    Riiablo.assets.setLoader(COF.class, new COFLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(DC6.class, new DC6Loader(Riiablo.mpqs));
    Riiablo.assets.setLoader(DCC.class, new DCCLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(Palette.class, new PaletteLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(Index.class, new IndexLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(FontTBL.BitmapFont.class, new BitmapFontLoader(Riiablo.mpqs));
    Texture.setAssetManager(Riiablo.assets);

    Riiablo.palettes = new Palettes(Riiablo.assets);
    Riiablo.fonts    = new Fonts(Riiablo.assets);
    Riiablo.colors   = new Colors();

    ShaderProgram.pedantic = false;
    Riiablo.shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette3.vert"),
        Gdx.files.internal("shaders/indexpalette3.frag"));
    if (!Riiablo.shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + Riiablo.shader.getLog());
    }

    Riiablo.batch = new PaletteIndexedBatch(2048, Riiablo.shader);
    Riiablo.shapes = new ShapeRenderer();

    VisUI.load();

    final VisCheckBox lock = new VisCheckBox("lock fps", false);
    lock.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        lockFps = lock.isChecked();
      }
    });

    final VisSlider slider = new VisSlider(128, 512, 128, false);
    slider.setValue(256);
    slider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        anim.setFrameDelta((int) slider.getValue());
      }
    });

    VisTable root = new VisTable();
    root.setFillParent(true);
    root.align(Align.bottom);
    root.add(lock).row();
    root.add(slider).row();

    stage = new Stage();
    stage.addActor(root);
    Gdx.input.setInputProcessor(stage);

    FileHandle handle = Riiablo.mpqs.resolve("data\\global\\monsters\\FA\\COF\\Faa1hth.cof");
    cof = COF.loadFromFile(handle);
    anim = Animation.newAnimation(cof);
    anim.setLayer(COF.Layer.TR, DCC.loadFromFile(Riiablo.mpqs.resolve("data\\global\\monsters\\FA\\TR\\FATRLITA1HTH.dcc")));
    anim.setLayer(COF.Layer.RH, DCC.loadFromFile(Riiablo.mpqs.resolve("data\\global\\monsters\\FA\\RH\\FARHAXEA1HTH.dcc")));
    anim.setLayer(COF.Layer.SH, DCC.loadFromFile(Riiablo.mpqs.resolve("data\\global\\monsters\\FA\\SH\\FASHBUCA1HTH.dcc")));
    anim.setLayer(COF.Layer.S1, DCC.loadFromFile(Riiablo.mpqs.resolve("data\\global\\monsters\\FA\\S1\\FAS1LITA1HTH.dcc")));
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    stage.act();
    stage.draw();

    float delta = Gdx.graphics.getDeltaTime();
    if (lockFps) {
      accumulator += delta;
      while (accumulator >= Animation.FRAME_DURATION) {
        lastUpdate = accumulator;
        accumulator -= Animation.FRAME_DURATION;
        anim.act(Animation.FRAME_DURATION);
      }
    } else {
      lastUpdate = delta;
      anim.act(delta);
    }

    Batch b = stage.getBatch();
    b.begin();
    Riiablo.fonts.consolas16.draw(b, String.valueOf(Gdx.graphics.getFramesPerSecond()), 0, Gdx.graphics.getHeight());
    Riiablo.fonts.consolas16.draw(b, String.valueOf(MathUtils.roundPositive(1 / lastUpdate)), 0, Gdx.graphics.getHeight() - 16);
    b.end();

    Riiablo.batch.begin(Riiablo.palettes.act1);
    anim.draw(Riiablo.batch, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    Riiablo.batch.end();
  }

  @Override
  public void dispose() {
    for (int l = 0; l < cof.getNumLayers(); l++) {
      COF.Layer layer = cof.getLayer(l);
      Animation.Layer animLayer = anim.getLayer(layer.component);
      if (animLayer != null) animLayer.getDC().dispose();
    }

    Riiablo.palettes.dispose();
    Riiablo.assets.dispose();
    Riiablo.shader.dispose();
    Riiablo.batch.dispose();
    Riiablo.shapes.dispose();
    VisUI.dispose();
    stage.dispose();
  }
}
