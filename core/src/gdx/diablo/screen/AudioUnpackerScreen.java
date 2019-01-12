package gdx.diablo.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import gdx.diablo.Diablo;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.DC6;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.DC6Loader;

public class AudioUnpackerScreen extends ScreenAdapter {
  private static final String TAG = "AudioUnpackerScreen";

  // TODO: This is a pretty basic way to calculate progress, change to more accurate and count sizes
  AtomicInteger sizeRead = new AtomicInteger(0);
  AtomicInteger totalSize = new AtomicInteger(0);
  AtomicReference<String> currentFile = new AtomicReference<>(null);

  AssetDescriptor<DC6> DownloadPatchBckgDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\DownloadPatchBckg.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  AssetDescriptor<DC6> DownloadPatchBall2Descriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\DownloadPatchBall2.dc6", DC6.class);
  TextureRegion DownloadPatchBckg;
  int DownloadPatchBckgX, DownloadPatchBckgY;

  Animation DownloadPatchBall2;

  GlyphLayout font42Glyphs;
  GlyphLayout font24Glyphs;

  public AudioUnpackerScreen() {
    Diablo.assets.load(DownloadPatchBckgDescriptor);
    Diablo.assets.load(DownloadPatchBall2Descriptor);
  }

  @Override
  public void show() {
    font42Glyphs = new GlyphLayout(Diablo.fonts.font42, Diablo.bundle.get("unpacking_audio_files"));
    font24Glyphs = new GlyphLayout(Diablo.fonts.font24, Diablo.bundle.get("unpacking_description"));

    Diablo.assets.finishLoadingAsset(DownloadPatchBckgDescriptor);
    DownloadPatchBckg = Diablo.assets.get(DownloadPatchBckgDescriptor).getTexture();

    DownloadPatchBckgX = Diablo.VIRTUAL_WIDTH_CENTER  - (DownloadPatchBckg.getRegionWidth() / 2);
    DownloadPatchBckgY = Diablo.VIRTUAL_HEIGHT_CENTER - (DownloadPatchBckg.getRegionHeight() / 2);

    Diablo.assets.finishLoadingAsset(DownloadPatchBall2Descriptor);
    DownloadPatchBall2 = Animation.newAnimation(Diablo.assets.get(DownloadPatchBall2Descriptor));
    DownloadPatchBall2.addAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onFinished(Animation animation) {
        Diablo.client.setScreen(new SplashScreen());
      }
    });

    Diablo.console.out.println("Unpacking audio files...");
    new Thread(new Runnable() {
      private static final String TAG = "Unpacker";

      @Override
      public void run() {
        FileHandle listfile0 = Gdx.files.internal("(listfile).1");
        //FileHandle listfile1 = new FileHandle(Gdx.files.getLocalStoragePath()).child("(listfile).1");
        BufferedReader reader = null;
        Writer writer = null;
        try {
          long size = 0;
          reader = listfile0.reader(4096, "US-ASCII");
          //writer = listfile1.writer(false, "US-ASCII");
          totalSize.set(NumberUtils.toInt(reader.readLine()));
          for (String line; (line = reader.readLine()) != null;) {
            FileHandle mpqHandle = null;
            try {
              currentFile.set(line);
              FileHandle handle = Diablo.home.child(line.replaceAll("\\\\", "/"));
              //if ((mpqHandle = Diablo.mpqs.resolve(line)) != null) {
              //  writer.write(line);
              //  writer.write('\n');
              //  size += mpqHandle.length();
              //}
              mpqHandle = Diablo.mpqs.resolve(line);
              if (handle.exists()) {
                sizeRead.addAndGet((int) mpqHandle.length());
                continue;
              }

              Diablo.console.out.println("Unpacking " + line);
              handle.writeBytes(mpqHandle.readBytes(), false);
              sizeRead.addAndGet((int) mpqHandle.length());
            } catch (Throwable t) {
              Gdx.app.error(TAG, t.getMessage(), t);
              Diablo.console.out.println(t.getMessage());
              if (mpqHandle != null) sizeRead.addAndGet((int) mpqHandle.length());
            }
          }

          //writer.write(Long.toString(size));
        } catch (Throwable t) {
          throw new GdxRuntimeException("unable to read " + listfile0, t);
        } finally {
          StreamUtils.closeQuietly(reader);
          //StreamUtils.closeQuietly(writer);

          // Safety net in case something went wrong to trigger screen change
          sizeRead.set(totalSize.get());
        }
      }
    }).start();
  }

  @Override
  public void hide() {}

  @Override
  public void dispose() {
    Diablo.assets.unload(DownloadPatchBckgDescriptor.fileName);
    Diablo.assets.unload(DownloadPatchBall2Descriptor.fileName);
  }

  @Override
  public void render(float delta) {
    PaletteIndexedBatch b = Diablo.batch;
    b.begin(Diablo.palettes.units);
    b.draw(DownloadPatchBckg, DownloadPatchBckgX, DownloadPatchBckgY);

    DownloadPatchBall2.setFrame((int) ((DownloadPatchBall2.getNumFramesPerDir() - 1) * ((double) sizeRead.get() / totalSize.get())));
    DownloadPatchBall2.draw(b, Diablo.VIRTUAL_WIDTH_CENTER, Diablo.VIRTUAL_HEIGHT_CENTER);

    Diablo.fonts.font42.draw(b, font42Glyphs, Diablo.VIRTUAL_WIDTH_CENTER - ((int) font42Glyphs.width >>> 1), Diablo.VIRTUAL_HEIGHT);
    Diablo.fonts.font24.draw(b, font24Glyphs, Diablo.VIRTUAL_WIDTH_CENTER - ((int) font24Glyphs.width >>> 1), Diablo.VIRTUAL_HEIGHT - (int) Diablo.fonts.font42.getLineHeight());

    GlyphLayout glyphs = new GlyphLayout(Diablo.fonts.fontformal12, Diablo.bundle.format("unpacking_file", currentFile.get()));
    Diablo.fonts.fontformal12.draw(b, glyphs, Diablo.VIRTUAL_WIDTH_CENTER - (glyphs.width / 2), 24 + glyphs.height);

    b.end();
  }
}
