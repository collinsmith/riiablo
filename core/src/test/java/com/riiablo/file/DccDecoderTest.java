package com.riiablo.file;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import java.io.InputStream;
import org.apache.commons.lang3.StringUtils;
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

import com.riiablo.RiiabloTest;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetUtils;
import com.riiablo.asset.param.DcParams;
import com.riiablo.asset.param.MpqParams;
import com.riiablo.codec.DCC;
import com.riiablo.codec.Palette;
import com.riiablo.codec.util.BBox;
import com.riiablo.file.DccDecoder.DirectionBuffer;
import com.riiablo.file.DccDecoder.FrameBuffer;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.mpq_bytebuf.MpqFileHandle;
import com.riiablo.mpq_bytebuf.MpqFileResolver;
import com.riiablo.util.InstallationFinder;

public class DccDecoderTest {
  private int[] toInts(String str) {
    str = StringUtils.substring(str, 1, str.length() - 1); // remove []
    String[] strs = StringUtils.split(str, ',');
    int[] ints = new int[strs.length];
    for (int i = 0; i < ints.length; i++) {
      ints[i] = NumberUtils.toInt(strs[i]);
    }
    return ints;
  }

  private BBox toBBox(String str) {
    String[] strs = StringUtils.split(str, ',');
    int xMin = NumberUtils.toInt(strs[0]);
    int yMin = NumberUtils.toInt(strs[1]);
    int xMax = NumberUtils.toInt(strs[2]);
    int yMax = NumberUtils.toInt(strs[3]);
    return new BBox(xMin, yMin, xMax, yMax);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "1;3;3;[3];[3]",
      "1;4;3;[4];[3]",
      "2;5;3;[4,1];[3]",
      "1;3;4;[3];[4]",
      "1;4;4;[4];[4]",
      "2;5;4;[4,1];[4]",
      "1;3;3;[3];[3]",
      "1;4;4;[4];[4]",
      "4;5;5;[4,1];[4,1]",
  }, delimiter = ';')
  @DisplayName("DirectionBuffer#clear() cell sizes")
  void direction_buffer(String size, String width, String height, String expectedWs, String expectedHs) {
    int s = NumberUtils.toInt(size);
    int w = NumberUtils.toInt(width);
    int h = NumberUtils.toInt(height);
    int[] eWs = toInts(expectedWs);
    int[] eHs = toInts(expectedHs);
    DirectionBuffer directionBuffer = new DirectionBuffer();
    directionBuffer.clear(w, h);
    assertEquals(s, directionBuffer.size);
    for (int y = 0, i = 0; y < eHs.length; y++) {
      for (int x = 0; x < eWs.length; x++, i++) {
        assertEquals(eWs[x], directionBuffer.w[i]);
        assertEquals(eHs[y], directionBuffer.h[i]);
      }
    }
  }

  @ParameterizedTest
  @CsvSource(value = {
      // data\global\chars\ba\hd\bahdbhma11hs.dcc
      "42,180,78,228;53,185,72,200;24;6;4;[1,4,4,4,4,2];[3,4,4,4]", // d=0,f=0
      "42,180,78,228;54,183,73,197;20;5;4;[4,4,4,4,3];[1,4,4,5]", // d=0,f=1
      "42,180,78,228;56,181,75,195;20;5;4;[2,4,4,4,5];[3,4,4,3]", // d=0,f=2
      "42,180,78,228;59,180,77,193;15;5;3;[3,4,4,4,3];[4,4,5]", // d=0,f=3
      "42,180,78,228;59,180,78,193;15;5;3;[3,4,4,4,4];[4,4,5]", // d=0,f=4
      "42,180,78,228;52,185,72,199;24;6;4;[2,4,4,4,4,2];[3,4,4,3]", // d=0,f=5
      "42,180,78,228;43,196,66,213;24;6;4;[3,4,4,4,4,4];[4,4,4,5]", // d=0,f=6
      "42,180,78,228;42,207,66,224;30;6;5;[4,4,4,4,4,4];[1,4,4,4,4]", // d=0,f=7
      "42,180,78,228;44,211,67,228;30;6;5;[2,4,4,4,4,5];[1,4,4,4,4]", // d=0,f=8
      "42,180,78,228;45,210,68,227;35;7;5;[1,4,4,4,4,4,2];[2,4,4,4,3]", // d=0,f=9
      "42,180,78,228;46,207,69,224;30;6;5;[4,4,4,4,4,3];[1,4,4,4,4]", // d=0,f=10
      "42,180,78,228;47,203,69,220;30;6;5;[3,4,4,4,4,3];[1,4,4,4,4]", // d=0,f=11
      "42,180,78,228;49,198,70,215;30;6;5;[1,4,4,4,4,4];[2,4,4,4,3]", // d=0,f=12
      "42,180,78,228;50,194,71,210;25;5;5;[4,4,4,4,5];[2,4,4,4,2]", // d=0,f=13
      "42,180,78,228;51,190,72,206;30;6;5;[3,4,4,4,4,2];[2,4,4,4,2]", // d=0,f=14
      "42,180,78,228;52,187,72,202;30;6;5;[2,4,4,4,4,2];[1,4,4,4,2]", // d=0,f=15

      // data\global\chars\ba\hd\bahdbhma11hs.dcc
      "-2,-16,62,21;-2,1,14,11;12;4;3;[4,4,4,4];[3,4,3]", // d=0,f=0
  }, delimiter = ';')
  @DisplayName("FrameBuffer#clear() cell sizes")
  void frame_buffer(String dirBox, String frameBox, String size, String width, String height, String expectedWs, String expectedHs) {
    BBox d = toBBox(dirBox);
    BBox f = toBBox(frameBox);
    int s = NumberUtils.toInt(size);
    int w = NumberUtils.toInt(width);
    int h = NumberUtils.toInt(height);
    int[] eWs = toInts(expectedWs);
    assertEquals(w, eWs.length);
    int[] eHs = toInts(expectedHs);
    assertEquals(h, eHs.length);
    FrameBuffer frameBuffer = new FrameBuffer();
    frameBuffer.clear(d, f);
    assertEquals(w, frameBuffer.cellsW);
    assertEquals(h, frameBuffer.cellsH);
    assertEquals(s, frameBuffer.size);
    for (int y = 0, i = 0; y < eHs.length; y++) {
      for (int x = 0; x < eWs.length; x++, i++) {
        assertEquals(eWs[x], frameBuffer.w[i]);
        assertEquals(eHs[y], frameBuffer.h[i]);
      }
    }
  }

  @BeforeEach
  public void beforeEach() {
    RiiabloTest.clearGdxContext();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "data\\global\\missiles\\DopplezonExplode.dcc",
      "data\\global\\chars\\ba\\hd\\bahdbhma11hs.dcc",
      "data\\global\\chars\\ba\\lg\\balglittnhth.dcc",
      "data\\global\\chars\\ba\\hd\\bahdlittnhth.dcc",
      "data\\global\\chars\\ba\\s2\\bas2littnhth.dcc",
      "data\\global\\chars\\so\\s2\\sos2medtnhth.dcc",
      "data\\global\\chars\\ba\\tr\\batrlittnhth.dcc",
  })
  void draw_pixmaps(String dccName) throws Exception {
    FileHandle testHome = InstallationFinder.getInstance().defaultHomeDir();
    DccDecoder decoder = new DccDecoder();
    EventExecutor executor = ImmediateEventExecutor.INSTANCE;
    AssetDesc<Dcc> parent = AssetDesc.of(dccName, Dcc.class, DcParams.of(-1));
    MpqFileResolver resolver = new MpqFileResolver(testHome);
    MpqFileHandle dccHandle = resolver.resolve(parent);
    InputStream stream = dccHandle.bufferStream(executor, dccHandle.sectorSize()).get();
    Dcc dcc = Dcc.read(dccHandle, stream);
    int offset = dcc.dirOffset(0);
    int nextOffset = dcc.dirOffset(1);
    ByteBuf buffer = dccHandle.bufferAsync(executor, offset, nextOffset - offset).get();
    dcc.read(buffer, 0);

    final Promise<?> promise = executor.newPromise();
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration() {{
      title = dccName;
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
        decoder.decode(dcc, 0);
        dcc.uploadTextures(0, 0);

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
        if (updater > 0.25f) {
          updater -= 0.25f;
          frame++;
          if (frame >= dcc.numFrames) {
            frame = 0;
          }
        }
        batch.draw(dcc.directions[0].texture[frame],
            0, 0,
            dcc.directions[0].box.width * 4, dcc.directions[0].box.height * 4);
        batch.end();
      }

      @Override
      public void dispose() {
        // also releases dccHandle reference
        // release twice, once for header, again for direction
        ReferenceCountUtil.release(dcc);
        ReferenceCountUtil.release(dcc);
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

  @ParameterizedTest
  @ValueSource(strings = {
      "data\\global\\missiles\\DopplezonExplode.dcc",
      "data\\global\\chars\\ba\\hd\\bahdbhma11hs.dcc",
      "data\\global\\chars\\ba\\lg\\balglittnhth.dcc",
      "data\\global\\chars\\ba\\hd\\bahdlittnhth.dcc",
      "data\\global\\chars\\ba\\s2\\bas2littnhth.dcc",
      "data\\global\\chars\\so\\s2\\sos2medtnhth.dcc",
      "data\\global\\chars\\ba\\tr\\batrlittnhth.dcc",
  })
  void draw_pixmaps2(String dccName) throws Exception {
    FileHandle testHome = InstallationFinder.getInstance().defaultHomeDir();
    EventExecutor executor = ImmediateEventExecutor.INSTANCE;
    AssetDesc<Dcc> parent = AssetDesc.of(dccName, Dcc.class, DcParams.of(-1));
    MpqFileResolver resolver = new MpqFileResolver(testHome);
    MpqFileHandle dccHandle = resolver.resolve(parent);

    final Promise<?> promise = executor.newPromise();
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration() {{
      title = dccName;
      forceExit = false;
    }};
    ApplicationListener listener = new ApplicationAdapter() {
      @Override
      public void create() {
        DCC.loadFromArray(ByteBufUtil.getBytes(dccHandle.buffer()));
        Gdx.app.exit();
      }

      @Override
      public void render() {
      }

      @Override
      public void dispose() {
        promise.setSuccess(null);
      }
    };
    new LwjglApplication(listener, config);
    promise.awaitUninterruptibly();
    ReferenceCountUtil.release(dccHandle);
    resolver.dispose();
  }
}
