package gdx.diablo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;

import gdx.diablo.codec.DC6;
import gdx.diablo.codec.Palette;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.mpq.MPQ;

public class TestClient extends ApplicationAdapter {

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "TestClient";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new TestClient(new FileHandle("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II")), config);
  }

  FileHandle home;
  PaletteIndexedBatch batch;
  ShaderProgram shader;
  ShapeRenderer shapes;
  Texture palette;
  Texture background;

  public TestClient(FileHandle home) {
    this.home = home;
  }

  @Override
  public void create() {
    // This is needed so that home is in a platform-dependent handle
    home = Gdx.files.absolute(home.path());
    MPQ d2data = MPQ.loadFromFile(home.child("d2data.mpq"));
    DC6 dc6 = DC6.loadFromStream(d2data.read("data\\global\\ui\\FrontEnd\\TitleScreen.dc6"));
    background = new Texture(new PixmapTextureData(dc6.getPixmap(0, 0), null, false, false,false));
    background.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    background.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

    //background = new Texture(new PixmapTextureData(dc6.frame(0, 0), null, false, true,false));
    palette = Palette.loadFromStream(d2data.read(Palettes.UNITS)).render();
    palette.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    palette.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

    ShaderProgram.pedantic = false;
    shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette2.vert"),
        Gdx.files.internal("shaders/indexpalette2.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
    }

    batch = new PaletteIndexedBatch(512, shader);
    shapes = new ShapeRenderer();
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.0f, 0.0f, 1.0f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.begin(palette);
    batch.draw(background, 0, 0);
    batch.end();

    shapes.begin(ShapeRenderer.ShapeType.Line);
    shapes.rect(0, 0, background.getWidth(), background.getHeight());
    shapes.end();
  }

  @Override
  public void dispose() {
    background.dispose();
    palette.dispose();
    shader.dispose();
    batch.dispose();
    shapes.dispose();
  }
}
