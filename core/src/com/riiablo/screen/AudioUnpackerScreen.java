package com.riiablo.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.StreamUtils;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.loader.DC6Loader;
import com.riiablo.widget.AnimationWrapper;
import com.riiablo.widget.Label;

import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AudioUnpackerScreen extends ScreenAdapter {
  private static final String TAG = "AudioUnpackerScreen";

  // TODO: This is a pretty basic way to calculate progress, change to more accurate and count sizes
  AtomicInteger sizeRead = new AtomicInteger(0);
  AtomicInteger totalSize = new AtomicInteger(0);
  AtomicReference<String> currentFile = new AtomicReference<>(null);

  AssetDescriptor<DC6> DownloadPatchBckgDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\DownloadPatchBckg.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  AssetDescriptor<DC6> DownloadPatchBall2Descriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\DownloadPatchBall2.dc6", DC6.class);

  Stage stage;

  public AudioUnpackerScreen() {
    Riiablo.assets.load(DownloadPatchBckgDescriptor);
    Riiablo.assets.load(DownloadPatchBall2Descriptor);

    stage = new Stage(Riiablo.extendViewport, Riiablo.batch);
    final float stageWidth    = stage.getWidth();
    final float stageHeight   = stage.getHeight();
    final float stageWidth50  = stageWidth  / 2;
    final float stageHeight50 = stageHeight / 2;

    Riiablo.assets.finishLoadingAsset(DownloadPatchBckgDescriptor);
    TextureRegion DownloadPatchBckg = Riiablo.assets.get(DownloadPatchBckgDescriptor).getTexture();
    Image background = new Image(DownloadPatchBckg);
    background.setScaling(Scaling.none);
    background.setPosition(stageWidth50, stageHeight50, Align.center);
    stage.addActor(background);

    Riiablo.assets.finishLoadingAsset(DownloadPatchBall2Descriptor);
    Animation DownloadPatchBall2 = Animation.newAnimation(Riiablo.assets.get(DownloadPatchBall2Descriptor));
    DownloadPatchBall2.setFrameDuration(Float.MAX_VALUE);
    DownloadPatchBall2.addAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onFinished(Animation animation) {
        Riiablo.client.setScreen(new SplashScreen());
      }
    });
    AnimationWrapper progress = new AnimationWrapper(DownloadPatchBall2) {
      @Override
      public void act(float delta) {
        super.act(delta);
        for (Animation animation : animations) {
          animation.setFrame((int) ((animation.getNumFramesPerDir() - 1) * ((double) sizeRead.get() / totalSize.get())));
        }
      }
    };
    progress.setPosition(stageWidth50, stageHeight50, Align.center);
    stage.addActor(progress);

    Table panel = new Table() {{
      final float SPACING = 0;
      add(new Label(Riiablo.bundle.get("unpacking_audio_files"), Riiablo.fonts.font42)).space(SPACING).row();
      add(new Label(Riiablo.bundle.get("unpacking_description"), Riiablo.fonts.font24)).space(SPACING).row();
      pack();
    }};
    panel.setPosition(stageWidth50, stageHeight - 20, Align.top | Align.center);
    stage.addActor(panel);

    Label lbFileName = new Label("", Riiablo.fonts.fontformal12) {
      @Override
      public void act(float delta) {
        super.act(delta);
        setText(Riiablo.bundle.format("unpacking_file", currentFile.get()));
        setPosition(stageWidth50, 20, Align.bottom | Align.center);
      }
    };
    stage.addActor(lbFileName);

    Riiablo.console.out.println("Unpacking audio files...");
    Thread unpacker = new Thread(new Runnable() {
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
              FileHandle handle = Riiablo.home.child(line.replaceAll("\\\\", "/"));
              //if ((mpqHandle = Riiablo.mpqs.resolve(line)) != null) {
              //  writer.write(line);
              //  writer.write('\n');
              //  size += mpqHandle.length();
              //}
              mpqHandle = Riiablo.mpqs.resolve(line);
              if (handle.exists()) {
                sizeRead.addAndGet((int) mpqHandle.length());
                continue;
              }

              Riiablo.console.out.println("Unpacking " + line);
              handle.writeBytes(mpqHandle.readBytes(), false);
              sizeRead.addAndGet((int) mpqHandle.length());
            } catch (Throwable t) {
              Gdx.app.error(TAG, t.getMessage(), t);
              Riiablo.console.out.println(t.getMessage());
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
    });
    unpacker.setPriority(Thread.MAX_PRIORITY);
    unpacker.start();
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(DownloadPatchBckgDescriptor.fileName);
    Riiablo.assets.unload(DownloadPatchBall2Descriptor.fileName);
  }

  @Override
  public void render(float delta) {
    Riiablo.batch.setPalette(Riiablo.palettes.units);
    stage.act(delta);
    stage.draw();
  }
}
