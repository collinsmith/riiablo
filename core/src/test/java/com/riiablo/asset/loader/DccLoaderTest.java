package com.riiablo.asset.loader;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;

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
import com.riiablo.file.Dc;
import com.riiablo.file.Dcc;
import com.riiablo.file.Palette;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.mpq_bytebuf.MpqFileHandle;
import com.riiablo.mpq_bytebuf.MpqFileResolver;
import com.riiablo.util.InstallationFinder;
import com.riiablo.util.InstallationFinder.DefaultNotFound;

import static com.riiablo.graphics.BlendMode.NONE;
import static com.riiablo.graphics.PaletteIndexedPixmap.INDEXED;

public class DccLoaderTest {
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
        .loader(Dcc.class, new DccLoader())
        .loader(Palette.class, new PaletteLoader())
    ;
  }

  @AfterEach
  public void afterEach() {
    AssetUtils.dispose(assets);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "data\\global\\chars\\ba\\hd\\bahdbhma11hs.dcc",
      "data\\global\\chars\\ba\\lg\\balglittnhth.dcc",
      "data\\global\\chars\\ba\\hd\\bahdlittnhth.dcc",
      "data\\global\\chars\\ba\\tr\\batrlittnhth.dcc",
  })
  void load(String dccName) throws Throwable {
    AssetDesc<Dcc> asset = AssetDesc.of(dccName, Dcc.class, DcParams.of(0));
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
      Dcc dcc;
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
        Dcc.MISSING_TEXTURE = new Texture(0, 0, INDEXED);

        assets.load(asset)
            .addListener((FutureListener<Dcc>) future -> dcc = future.getNow());

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
        updater += Gdx.graphics.getDeltaTime();
        if (updater > 0.25f) {
          updater -= 0.25f;
          frame++;
          if (frame >= dcc.numFrames()) {
            frame = 0;
          }
        }
        batch.draw(dcc.direction(0).frame(frame).texture(),
            0, 0,
            dcc.direction(0).box().width * 4, dcc.direction(0).box().height * 4);
        batch.end();
      }

      @Override
      public void dispose() {
        assets.unload(paletteAsset);
        assets.unload(asset);
        AssetUtils.dispose(Dcc.MISSING_TEXTURE);
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
