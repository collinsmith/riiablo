package com.riiablo.file;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import java.io.InputStream;

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

import com.riiablo.RiiabloTest;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetUtils;
import com.riiablo.asset.param.DcParams;
import com.riiablo.asset.param.MpqParams;
import com.riiablo.codec.Palette;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.mpq_bytebuf.MpqFileHandle;
import com.riiablo.mpq_bytebuf.MpqFileResolver;
import com.riiablo.util.InstallationFinder;

public class Dc6DecoderTest {
  @BeforeEach
  public void beforeEach() {
    RiiabloTest.clearGdxContext();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "data\\global\\monsters\\ty\\ra\\tyralitnuhth.dc6",
  })
  void draw_pixmaps(String dc6Name) throws Exception {
    FileHandle testHome = InstallationFinder.getInstance().defaultHomeDir();
    Dc6Decoder decoder = new Dc6Decoder();
    EventExecutor executor = ImmediateEventExecutor.INSTANCE;
    AssetDesc<Dc6> parent = AssetDesc.of(dc6Name, Dc6.class, DcParams.of(-1));
    MpqFileResolver resolver = new MpqFileResolver(testHome);
    MpqFileHandle dc6Handle = resolver.resolve(parent);
    InputStream stream = dc6Handle.bufferStream(executor, dc6Handle.sectorSize()).get();
    Dc6 dc6 = Dc6.read(dc6Handle, stream);
    int offset = dc6.dirOffset(0);
    int nextOffset = dc6.dirOffset(1);
    ByteBuf buffer = dc6Handle.bufferAsync(executor, offset, nextOffset - offset).get();
    dc6.read(buffer, 0);

    final Promise<?> promise = executor.newPromise();
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration() {{
      title = dc6Name;
      forceExit = false;
    }};
    ApplicationListener listener = new ApplicationAdapter() {
      PaletteIndexedBatch batch;
      ShaderProgram shader;
      Texture paletteTexture;
      int frame = 0;
      float updater = 0f;

      @Override
      public void create() {
        try {
          create0();
        } catch (Throwable t) {
          t.printStackTrace(System.err);
          Gdx.app.exit();
        }
      }

      void create0() {
        decoder.decode(dc6, 0);
        dc6.uploadTextures(0, 0);

        String paletteName = "data\\global\\palette\\ACT1\\pal.dat";
        AssetDesc<Palette> paletteDesc = AssetDesc.of(paletteName, Palette.class, MpqParams.of());
        MpqFileHandle paletteHandle = resolver.resolve(paletteDesc);
        Palette palette = Palette.loadFromStream(paletteHandle.stream());
        paletteTexture = palette.render();

        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(
            Gdx.files.internal("shaders/indexpalette3.vert"),
            Gdx.files.internal("shaders/indexpalette3.frag"));
        if (!shader.isCompiled()) {
          throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
        }
        batch = new PaletteIndexedBatch(1024, shader);
        batch.setGamma(1.2f);
      }

      @Override
      public void render() {
        // Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
        Gdx.gl.glClearColor(1f, 1f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setBlendMode(BlendMode.NONE);
        batch.begin(paletteTexture);
        updater += Gdx.graphics.getDeltaTime();
        final float fdelay = 0.10f;
        if (updater > fdelay) {
          updater -= fdelay;
          frame++;
          if (frame >= dc6.numFrames) {
            frame = 0;
          }
        }
        Dc6.Dc6Direction dir = dc6.directions[0];
        BBox box = dir.frames[frame].box;
        batch.draw(dir.texture[frame],
            dir.box.width + box.xMin, -box.yMax,
            box.width, box.height);
        batch.end();
      }

      @Override
      public void dispose() {
        // also releases dccHandle reference
        // release twice, once for header, again for direction
        ReferenceCountUtil.release(dc6);
        ReferenceCountUtil.release(dc6);
        AssetUtils.dispose(paletteTexture);
        AssetUtils.dispose(shader);
        AssetUtils.dispose(batch);
        promise.setSuccess(null);
      }
    };
    new LwjglApplication(listener, config);
    promise.awaitUninterruptibly();
    resolver.dispose();
  }
}
