package com.riiablo.asset.loader;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import org.apache.commons.lang3.math.NumberUtils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.Riiablo;
import com.riiablo.RiiabloTest;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetManager;
import com.riiablo.asset.AssetUtils;
import com.riiablo.asset.adapter.GdxFileHandleAdapter;
import com.riiablo.asset.adapter.MpqFileHandleAdapter;
import com.riiablo.asset.param.DcParams;
import com.riiablo.asset.param.MpqParams;
import com.riiablo.asset.resolver.GdxFileHandleResolver;
import com.riiablo.codec.util.BBox;
import com.riiablo.file.Dc;
import com.riiablo.file.Dc6;
import com.riiablo.file.Palette;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.mpq_bytebuf.MpqFileHandle;
import com.riiablo.mpq_bytebuf.MpqFileResolver;
import com.riiablo.util.InstallationFinder;
import com.riiablo.util.InstallationFinder.DefaultNotFound;

import static com.riiablo.graphics.BlendMode.NONE;
import static com.riiablo.graphics.PaletteIndexedPixmap.INDEXED;

public class Dc6LoaderTest {
  AssetManager assets;

  @BeforeEach
  public void beforeEach() throws DefaultNotFound {
    RiiabloTest.clearGdxContext();
    final InstallationFinder finder = InstallationFinder.getInstance();
    Riiablo.home = finder.defaultHomeDir();
    assets = new AssetManager()
        .resolver(GdxFileHandleResolver.Internal, 0)
        .resolver(new MpqFileResolver(), 1)
        .paramResolver(Dc.class, DcParams.class)
        .adapter(FileHandle.class, new GdxFileHandleAdapter())
        .adapter(MpqFileHandle.class, new MpqFileHandleAdapter())
        .loader(Dc6.class, new Dc6Loader())
        .loader(Palette.class, new PaletteLoader())
    ;
  }

  @AfterEach
  public void afterEach() {
    AssetUtils.dispose(assets);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "data\\global\\monsters\\ty\\ra\\tyralitnuhth.dc6,0,false",
      "data\\global\\ui\\panel\\invchar6.dc6,0,true",
  }, delimiter = ',')
  void load(String dccName, String szDirection, String szCombineFrames) throws Throwable {
    int direction = NumberUtils.toInt(szDirection);
    boolean combineFrames = Boolean.parseBoolean(szCombineFrames);
    AssetDesc<Dc6> asset = AssetDesc.of(dccName, Dc6.class, DcParams.of(direction, combineFrames));
    EventExecutor executor = ImmediateEventExecutor.INSTANCE;
    final Promise<Throwable> promise = executor.newPromise();
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration() {{
      title = dccName;
      forceExit = false;
    }};
    ApplicationListener listener = new ApplicationAdapter() {
      Throwable throwable;
      PaletteIndexedBatch batch;
      ShaderProgram shader;
      int frame = 0;
      float updater = 0f;
      Dc6 dc6;
      Palette palette;
      AssetDesc<Palette> paletteAsset;

      @Override
      public void create() {
        try {
          create0();
        } catch (Throwable t) {
          t.printStackTrace(System.err);
          throwable = t;
          Gdx.app.exit();
        }
      }

      public void create0() throws InterruptedException {
        Dc.MISSING_TEXTURE = new Texture(0, 0, INDEXED);

        assets.load(asset)
            .addListener((FutureListener<Dc6>) future -> dc6 = future.getNow());

        String paletteName = "data\\global\\palette\\ACT1\\pal.dat";
        paletteAsset = AssetDesc.of(paletteName, Palette.class, MpqParams.of());
        assets.load(paletteAsset)
            .addListener((FutureListener<Palette>) future -> palette = future.getNow());

        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(
            Gdx.files.internal("shaders/indexpalette3.vert"),
            Gdx.files.internal("shaders/indexpalette3.frag"));
        if (!shader.isCompiled()) {
          throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
        }
        batch = new PaletteIndexedBatch(1024, shader);
        batch.setGamma(1.2f);

        assets.awaitAll(asset, paletteAsset);
      }

      @Override
      public void render() {
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setBlendMode(NONE);
        batch.begin(palette.texture());
        if (combineFrames) {
          batch.draw(dc6.page(0), 0, 0);
          batch.end();
          return;
        }
        updater += Gdx.graphics.getDeltaTime();
        if (updater > 0.25f) {
          updater -= 0.25f;
          frame++;
          if (frame >= dc6.numFrames()) {
            frame = 0;
          }
        }
        Dc6.Dc6Direction dir = dc6.direction(0);
        BBox box = dir.frame(frame).box();
        batch.draw(dc6.direction(0).frame(frame).texture(),
            dir.box().width + box.xMin, -box.yMax,
            box.width, box.height);
        batch.end();
      }

      @Override
      public void dispose() {
        assets.unload(paletteAsset);
        assets.unload(asset);
        AssetUtils.dispose(Dc.MISSING_TEXTURE);
        AssetUtils.dispose(shader);
        AssetUtils.dispose(batch);
        promise.setSuccess(throwable);
      }
    };
    new LwjglApplication(listener, config);
    Throwable throwable = promise.awaitUninterruptibly().getNow();
    if (throwable != null) throw throwable;
  }
}
