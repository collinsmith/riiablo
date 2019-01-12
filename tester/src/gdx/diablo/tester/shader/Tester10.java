package gdx.diablo.tester.shader;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;

import gdx.diablo.BlendMode;
import gdx.diablo.Diablo;
import gdx.diablo.Fonts;
import gdx.diablo.Palettes;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.FontTBL;
import gdx.diablo.codec.FontTBL.BitmapFont;
import gdx.diablo.codec.Palette;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.BitmapFontLoader;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.loader.PaletteLoader;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class Tester10 extends ApplicationAdapter {
  private static final String TAG = "Tester10";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester10";
    config.resizable = true;
    config.width = 1920;
    config.height = 1080;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester10(new FileHandle("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II")), config);
  }

  FileHandle home;
  PaletteIndexedBatch batch;
  ShaderProgram shader;
  ShapeRenderer shapes;

  TextureRegion background;
  TextureRegion blend;
  Label label;

  public Tester10(FileHandle home) {
    this.home = home;
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    // This is needed so that home is in a platform-dependent handle
    home = Gdx.files.absolute(home.path());

    MPQFileHandleResolver resolver = Diablo.mpqs = new MPQFileHandleResolver();
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));

    AssetManager assets = Diablo.assets = new AssetManager();
    assets.setLoader(Palette.class, new PaletteLoader(resolver));
    assets.setLoader(DC6.class, new DC6Loader(resolver));
    assets.setLoader(FontTBL.BitmapFont.class, new BitmapFontLoader(resolver));

    Diablo.fonts = new Fonts(assets);
    Diablo.palettes = new Palettes(assets);
    //Diablo.pl2s = new Palettes(assets);

    AssetDescriptor<DC6> backgroundAsset = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\TitleScreen.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
    assets.load(backgroundAsset);
    assets.finishLoadingAsset(backgroundAsset);
    background = assets.get(backgroundAsset).getTexture();

    AssetDescriptor<DC6> blendAsset = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\necromancer\\NENU3s.DC6", DC6.class);
    assets.load(blendAsset);
    assets.finishLoadingAsset(blendAsset);
    blend = assets.get(blendAsset).getTexture();

    label = new Label("Text Color", Diablo.fonts.font16);
    label.setPosition(0, 800);
    label.setColor(new Color(0xA59263FF)); //0x4169E1FF

    ShaderProgram.pedantic = false;
    shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette3.vert"),
        Gdx.files.internal("shaders/indexpalette3.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
    }

    batch = new PaletteIndexedBatch(512, shader);
    shapes = new ShapeRenderer();
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.begin(Diablo.palettes.sky);

    batch.setBlendMode(BlendMode.NONE);
    batch.draw(background, 0, 0);

    batch.setPalette(Diablo.palettes.fechar);
    batch.setBlendMode(BlendMode.NONE);
    batch.draw(blend, 825, blend.getRegionHeight() + 25);

    batch.setBlendMode(BlendMode.ID);
    batch.draw(blend, 850 + blend.getRegionWidth(), blend.getRegionHeight() + 25);

    batch.setBlendMode(BlendMode.LUMINOSITY);
    batch.draw(blend, 875 + blend.getRegionWidth() * 2, blend.getRegionHeight() + 25);

    batch.setBlendMode(BlendMode.SOLID);
    batch.draw(blend, 825 + blend.getRegionWidth() * 0, 0);

    batch.setBlendMode(BlendMode.TINT_BLACKS);
    batch.draw(blend, 850 + blend.getRegionWidth() * 1, 0);

    batch.setBlendMode(BlendMode.TINT_WHITES);
    batch.draw(blend, 875 + blend.getRegionWidth() * 2, 0);

    batch.setBlendMode(BlendMode.TINT_WHITES);
    Diablo.fonts.font16.setColor(new Color(0xA59263FF));
    Diablo.fonts.font16.draw(batch, "Text Color 1", 0, 650);

    batch.setBlendMode(BlendMode.LUMINOSITY);
    Diablo.fonts.font16.draw(batch, "Text Color 2", 0, 675);

    batch.setBlendMode(BlendMode.LUMINOSITY_TINT);
    Diablo.fonts.font16.draw(batch, "Text Color 3", 0, 700);

    batch.end();

    batch.begin(Diablo.palettes.fechar);
    batch.setBlendMode(BlendMode.LUMINOSITY_TINT);
    Diablo.fonts.font16.draw(batch, "Text Color 4", 0, 725);
    batch.end();

    batch.begin(Diablo.palettes.fechar);
    //batch.enableBlending();
    //batch.setColor(Color.RED);
    batch.setBlendMode(BlendMode.TINT_WHITES);
    Diablo.fonts.font16.setColor(new Color(0xA59263FF));
    Diablo.fonts.font16.draw(batch, "Text Color 5", 0, 750);
    Diablo.fonts.font16.setColor(Color.WHITE);

    label.draw(batch, 1);

    batch.end();

    //Palette.loadFrom

    batch.begin(Diablo.palettes.fechar);
    batch.setBlendMode(BlendMode.TINT_BLACKS);
    Diablo.fonts.fontexocet10.setColor(Color.BLACK);
    Diablo.fonts.fontexocet10.draw(batch, "Text Color 6", 0, 775);
    Diablo.fonts.fontexocet10.setColor(Color.WHITE);
    batch.end();

    shapes.begin(ShapeRenderer.ShapeType.Line);
    shapes.rect(0, 0, background.getRegionWidth(), background.getRegionHeight());
    shapes.rect(825, blend.getRegionHeight() + 25, blend.getRegionWidth(), blend.getRegionHeight());
    shapes.rect(850 + blend.getRegionWidth()    , blend.getRegionHeight() + 25, blend.getRegionWidth(), blend.getRegionHeight());
    shapes.rect(875 + blend.getRegionWidth() * 2, blend.getRegionHeight() + 25, blend.getRegionWidth(), blend.getRegionHeight());
    shapes.rect(825 + blend.getRegionWidth() * 0, 0, blend.getRegionWidth(), blend.getRegionHeight());
    shapes.rect(850 + blend.getRegionWidth() * 1, 0, blend.getRegionWidth(), blend.getRegionHeight());
    shapes.rect(875 + blend.getRegionWidth() * 2, 0, blend.getRegionWidth(), blend.getRegionHeight());
    shapes.end();
  }

  @Override
  public void dispose() {
    Diablo.assets.dispose();
    shader.dispose();
    batch.dispose();
    shapes.dispose();
  }

  public class Label extends com.badlogic.gdx.scenes.scene2d.ui.Label {
    public Label(int id, BitmapFont font) {
      this(id == -1 ? "" : Diablo.string.lookup(id), font);
    }

    public Label(String text, BitmapFont font) {
      super(text, new LabelStyle(font, null));
    }

    @Override
    public void draw(Batch batch, float a) {
      if (batch instanceof PaletteIndexedBatch) {
        draw((PaletteIndexedBatch) batch, a);
      } else {
        throw new GdxRuntimeException("Not supported");
      }
    }

    public void draw(PaletteIndexedBatch batch, float a) {
      validate();

      batch.setBlendMode(((BitmapFont) getStyle().font).getBlendMode());
      BitmapFontCache cache = getBitmapFontCache();
      cache.setPosition(getX(), getY());
      cache.tint(getColor());
      cache.draw(batch);
      batch.resetBlendMode();
    }

    @Override
    public boolean setText(int id) {
      setText(Diablo.string.lookup(id));
      return true;
    }
  }
}
