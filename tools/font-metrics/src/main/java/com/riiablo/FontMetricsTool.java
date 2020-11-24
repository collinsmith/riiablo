package com.riiablo;

import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import com.riiablo.codec.DC6;
import com.riiablo.codec.FontTBL;
import com.riiablo.codec.Index;
import com.riiablo.codec.Palette;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.BitmapFontLoader;
import com.riiablo.loader.DC6Loader;
import com.riiablo.loader.IndexLoader;
import com.riiablo.loader.PaletteLoader;
import com.riiablo.mpq.MPQFileHandleResolver;
import com.riiablo.util.InstallationFinder;

public class FontMetricsTool extends ApplicationAdapter {
  private static final String TAG = "FontMetricsTool";

  public static void main(String[] args) {
    InstallationFinder finder = InstallationFinder.getInstance();
    Array<FileHandle> homeDirs = finder.getHomeDirs();
    FileHandle d2Home = homeDirs.first();

    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.addIcon("ic_launcher_16.png", FileType.Internal);
    config.addIcon("ic_launcher_32.png", FileType.Internal);
    config.addIcon("ic_launcher_128.png", FileType.Internal);
    config.title = TAG;
    config.resizable = true;
    config.width = 800;
    config.height = 600;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new FontMetricsTool(d2Home, args[0]), config);
  }

  private static final String STRING = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
      "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lacus viverra vitae " +
      "congue eu consequat.\n\nTristique nulla aliquet enim tortor at auctor urna nunc uuid. Libero uuid" +
      " faucibus nisl tincidunt eget nullam non.";

  FileHandle home;
  String font;
  Stage stage;

  boolean debug = true;
  boolean center = false;
  FontTBL.BitmapFont active;
  BitmapFont.BitmapFontData data;

  @Deprecated
  FontMetricsTool(String home, String font) {
    this(new FileHandle(home), font);
  }

  FontMetricsTool(FileHandle home, String font) {
    this.home = home;
    this.font = font;
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    Riiablo.home = home = Gdx.files.absolute(home.path());
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.assets = new AssetManager();
    Riiablo.assets.setLoader(DC6.class, new DC6Loader(Riiablo.mpqs));
    Riiablo.assets.setLoader(Palette.class, new PaletteLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(Index.class, new IndexLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(FontTBL.BitmapFont.class, new BitmapFontLoader(Riiablo.mpqs));
    Texture.setAssetManager(Riiablo.assets);

    Riiablo.palettes = new Palettes(Riiablo.assets);
    Riiablo.fonts    = new Fonts(Riiablo.assets);
    Riiablo.colors   = new Colors();

    try {
      active = (FontTBL.BitmapFont) ClassReflection.getField(Fonts.class, font).get(Riiablo.fonts);
    } catch (ReflectionException e) {
      active = Riiablo.fonts.font16;
      Gdx.app.error(TAG, e.getMessage(), e);
    }
    data = active.getData();

    ShaderProgram.pedantic = false;
    Riiablo.shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette3.vert"),
        Gdx.files.internal("shaders/indexpalette3.frag"));
    if (!Riiablo.shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + Riiablo.shader.getLog());
    }

    Riiablo.batch = new PaletteIndexedBatch(2048, Riiablo.shader);
    Riiablo.shapes = new ShapeRenderer();

    /*
    // TODO: add support for changing font at run-time
    final ObjectMap<String, FontTBL.BitmapFont> fonts = new ObjectMap<>();
    Field[] fields = Riiablo.fonts.getClass().getDeclaredFields();
    for (Field field : fields) {
      if (field.getType() == FontTBL.BitmapFont.class) {
        try {
          fonts.put(field.getName(), (FontTBL.BitmapFont) field.get(Riiablo.fonts));
        } catch (Throwable t) {
          Gdx.app.error(TAG, t.getMessage(), t);
        }
      }
    }
    */

    VisUI.load();

    VisTable root = new VisTable();
    root.setFillParent(true);
    root.align(Align.right);
    //root.setDebug(true, true);

    VisTable table = new VisTable();
    /*table.add(new VisSelectBox() {{
      Array<String> items = fonts.keys().toArray();
      items.sort();
      setItems(items);
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {

        }
      });
    }}).row();*/
    table.add(new Table() {{
      add(new VisCheckBox("debug", debug) {{
        addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            debug = isChecked();
          }
        });
      }});
      add(new VisCheckBox("center", center) {{
        addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            center = isChecked();
          }
        });
      }});
    }}).align(Align.right).row();
    table.add(new Spinner("lineHeight", new IntSpinnerModel((int) data.lineHeight, 2, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          data.lineHeight = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("xHeight", new IntSpinnerModel((int) data.xHeight, 2, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          data.xHeight = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("capHeight", new IntSpinnerModel((int) data.capHeight, 2, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          data.capHeight = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("ascent", new IntSpinnerModel((int) data.ascent, 2, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          data.ascent = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("descent", new IntSpinnerModel((int) data.descent, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          data.descent = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("down", new IntSpinnerModel((int) data.down, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          data.down = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("padBottom", new IntSpinnerModel((int) data.padBottom, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          data.padBottom = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("padLeft", new IntSpinnerModel((int) data.padLeft, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          data.padLeft = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("padTop", new IntSpinnerModel((int) data.padTop, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          data.padTop = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("padRight", new IntSpinnerModel((int) data.padRight, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          data.padRight = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    root.add(table).growY();

    stage = new Stage();
    stage.addActor(root);

    Gdx.input.setInputProcessor(stage);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    stage.act();
    stage.draw();

    Batch b = stage.getBatch();
    b.begin();
    GlyphLayout consolas16Layout = Riiablo.fonts.consolas16.draw(b, STRING, 0, 550, 600, center ? Align.center : Align.left, true);
    b.end();

    PaletteIndexedBatch batch = Riiablo.batch;
    batch.begin(Riiablo.palettes.units);
    batch.setBlendMode(active.getBlendMode());
    GlyphLayout otherLayout = active.draw(batch, STRING, 0, 250, 600, center ? Align.center : Align.left, true);
    batch.end();

    if (debug) {
      ShapeRenderer shapes = Riiablo.shapes;
      shapes.begin(ShapeRenderer.ShapeType.Line);
      drawDebug(Riiablo.fonts.consolas16, consolas16Layout, 550);
      drawDebug(active, otherLayout, 250);
      shapes.end();
    }
  }

  public void drawDebug(BitmapFont font, GlyphLayout layout, int y) {
    ShapeRenderer shapes = Riiablo.shapes;
    for (GlyphLayout.GlyphRun run : layout.runs) {
      shapes.setColor(Color.GREEN);
      shapes.line(run.x, y + run.y, run.x + run.width, y + run.y);
      shapes.setColor(Color.RED);
      shapes.line(
          run.x, y + run.y - font.getLineHeight(),
          run.x + run.width, y + run.y - font.getLineHeight());
    }
  }

  @Override
  public void dispose() {
    Riiablo.palettes.dispose();
    VisUI.dispose();
    Riiablo.assets.dispose();
    stage.dispose();
    Riiablo.shader.dispose();
    Riiablo.batch.dispose();
    Riiablo.shader.dispose();
  }
}
