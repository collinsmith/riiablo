package gdx.diablo.tester.fonttbl;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
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
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

import gdx.diablo.Colors;
import gdx.diablo.Diablo;
import gdx.diablo.Fonts;
import gdx.diablo.Palettes;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.FontTBL;
import gdx.diablo.codec.Index;
import gdx.diablo.codec.Palette;
import gdx.diablo.codec.TXT;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.BitmapFontLoader;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.loader.IndexLoader;
import gdx.diablo.loader.PaletteLoader;
import gdx.diablo.loader.TXTLoader;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class Tester404 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester404";
    config.resizable = true;
    config.width = 800;
    config.height = 600;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester404(), config);
  }

  private static final String STRING = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
      "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lacus viverra vitae " +
      "congue eu consequat.\n\nTristique nulla aliquet enim tortor at auctor urna nunc id. Libero id" +
      " faucibus nisl tincidunt eget nullam non.";

  AssetManager assets;
  MPQFileHandleResolver mpqs;

  Stage stage;
  ShapeRenderer shapes;
  ShaderProgram shader;
  PaletteIndexedBatch batch;

  boolean debug = true;
  boolean center = false;
  FontTBL.BitmapFont active;
  BitmapFont.BitmapFontData activeData;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    Diablo.mpqs = mpqs = new MPQFileHandleResolver();
    mpqs.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
    mpqs.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    mpqs.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));

    Diablo.assets = assets = new AssetManager();
    assets.setLoader(DC6.class, new DC6Loader(mpqs));
    assets.setLoader(Palette.class, new PaletteLoader(mpqs));
    assets.setLoader(Index.class, new IndexLoader(mpqs));
    assets.setLoader(FontTBL.BitmapFont.class, new BitmapFontLoader(mpqs));
    assets.setLoader(TXT.class, new TXTLoader(mpqs));
    Texture.setAssetManager(assets);

    Diablo.palettes = new Palettes(assets);
    Diablo.fonts    = new Fonts(assets);
    Diablo.colors   = new Colors();
    active     = Diablo.fonts.fontformal11;
    activeData = active.getData();

    ShaderProgram.pedantic = false;
    Diablo.shader = shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette3.vert"),
        Gdx.files.internal("shaders/indexpalette3.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader");
    }
    Diablo.batch = batch = new PaletteIndexedBatch(2048, shader);
    Diablo.shapes = shapes = new ShapeRenderer();

    VisUI.load();

    VisTable root = new VisTable();
    root.setFillParent(true);
    root.align(Align.right);
    root.setDebug(true, true);

    VisTable table = new VisTable();
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
    table.add(new Spinner("lineHeight", new IntSpinnerModel((int) activeData.lineHeight, 2, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          activeData.lineHeight = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("xHeight", new IntSpinnerModel((int) activeData.xHeight, 2, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          activeData.xHeight = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("capHeight", new IntSpinnerModel((int) activeData.capHeight, 2, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          activeData.capHeight = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("ascent", new IntSpinnerModel((int) activeData.ascent, 2, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          activeData.ascent = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("descent", new IntSpinnerModel((int) activeData.descent, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          activeData.descent = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("down", new IntSpinnerModel((int) activeData.down, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          activeData.down = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("padBottom", new IntSpinnerModel((int) activeData.padBottom, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          activeData.padBottom = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("padLeft", new IntSpinnerModel((int) activeData.padLeft, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          activeData.padLeft = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("padTop", new IntSpinnerModel((int) activeData.padTop, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          activeData.padTop = ((IntSpinnerModel) getModel()).getValue();
        }
      });
    }}).align(Align.right).row();
    table.add(new Spinner("padRight", new IntSpinnerModel((int) activeData.padRight, -128, 128, 1)) {{
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          activeData.padRight = ((IntSpinnerModel) getModel()).getValue();
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
    GlyphLayout consolas16Layout = Diablo.fonts.consolas16.draw(b, STRING, 0, 550, 600, center ? Align.center : Align.left, true);
    b.end();

    /*for (GlyphLayout.GlyphRun run : layout.runs) {
      shapes.setColor(Color.GREEN);
      shapes.line(run.x, 550 + run.y, run.x + run.width, 550 + run.y);
      shapes.setColor(Color.RED);
      shapes.line(
          run.x, 550 + run.y - Diablo.fonts.consolas16.getLineHeight() + 1,
          run.x + run.width, 550 + run.y - Diablo.fonts.consolas16.getLineHeight() + 1);
      shapes.setColor(Color.BLUE);
      shapes.line(
          run.x, 550 + run.y - Diablo.fonts.consolas16.getAscent(),
          run.x + run.width, 550 + run.y - Diablo.fonts.consolas16.getAscent());
      shapes.setColor(Color.MAGENTA);

      //int i = 1;
      //float x = 0;
      //for (BitmapFont.Glyph glyph : run.glyphs) {
      //  shapes.line(x + run.x, 545 + run.y, x + run.x + glyph.width, 545 + run.y);
      //  x += run.xAdvances.get(i++);
      //}
    }*/

    batch.begin(Diablo.palettes.units);
    batch.setBlendMode(active.getBlendMode());
    GlyphLayout otherLayout = active.draw(batch, STRING, 0, 250, 600, center ? Align.center : Align.left, true);
    batch.end();

    if (debug) {
      shapes.begin(ShapeRenderer.ShapeType.Line);
      drawDebug(Diablo.fonts.consolas16, consolas16Layout, 550);
      drawDebug(active, otherLayout, 250);
      shapes.end();
    }
  }

  public void drawDebug(BitmapFont font, GlyphLayout layout, int y) {
    for (GlyphLayout.GlyphRun run : layout.runs) {
      shapes.setColor(Color.GREEN);
      shapes.line(run.x, y + run.y, run.x + run.width, y + run.y);
      shapes.setColor(Color.RED);
      shapes.line(
          run.x, y + run.y - font.getLineHeight(),
          run.x + run.width, y + run.y - font.getLineHeight());
      //shapes.setColor(Color.BLUE);
      //shapes.line(
      //    run.x, y + run.y - font.getAscent(),
      //    run.x + run.width, y + run.y - font.getAscent());
      //shapes.setColor(Color.MAGENTA);
    }
  }

  @Override
  public void dispose() {
    Diablo.palettes.dispose();
    VisUI.dispose();
    assets.dispose();
    stage.dispose();
    shader.dispose();
    batch.dispose();
    shader.dispose();
  }
}
