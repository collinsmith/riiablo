package com.riiablo.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.map.Map;
import com.riiablo.widget.AnimationWrapper;

public class GameLoadingScreen extends ScreenAdapter {
  private static final String TAG = "GameLoadingScreen";

  private final AssetDescriptor<DC6> loadingscreenDescriptor = new AssetDescriptor<>("data\\local\\ui\\loadingscreen.dc6", DC6.class);

  private Array<AssetDescriptor> dependencies;

  private Stage stage;
  private AnimationWrapper loadingscreenWrapper;

  private Map map;
  private int act;

  public GameLoadingScreen(Map map, Array<AssetDescriptor> dependencies) {
    this.map = map;
    stage = new Stage(Riiablo.defaultViewport, Riiablo.batch);

    Riiablo.assets.load(loadingscreenDescriptor);
    Riiablo.assets.finishLoadingAsset(loadingscreenDescriptor);
    final Animation loadingscreen = Animation.newAnimation(Riiablo.assets.get(loadingscreenDescriptor));
    loadingscreen.setFrameDuration(Float.MAX_VALUE);
    loadingscreenWrapper = new AnimationWrapper(loadingscreen) {
      @Override
      public void act(float delta) {
        super.act(delta);
        for (Animation animation : animations) {
          animation.setFrame((int) (Riiablo.assets.getProgress() * (animation.getNumFramesPerDir() - 1)));
        }
      }
    };
    loadingscreenWrapper.setPosition(
        (stage.getWidth()  / 2) - (loadingscreen.getMinWidth()  / 2),
        (stage.getHeight() / 2) - (loadingscreen.getMinHeight() / 2));
    stage.addActor(loadingscreenWrapper);

    this.dependencies = new Array<>(dependencies);
  }

  public void loadAct(int act) {
    this.act = act;
  }

  @Override
  public void show() {
    Gdx.app.log(TAG, "Loading act " + (act + 1));
    Riiablo.viewport = Riiablo.defaultViewport;

    Riiablo.assets.load(loadingscreenDescriptor);
    if (dependencies != null) {
      for (AssetDescriptor asset : dependencies) {
        Riiablo.assets.load(asset);
      }
      dependencies = null;
    }

    map.setAct(act);
    map.load();
  }

  @Override
  public void hide() {
    Riiablo.assets.unload(loadingscreenDescriptor.fileName);
  }

  @Override
  public void dispose() {}

  @Override
  public void render(float delta) {
    if (Riiablo.assets.update()) {
      map.finishLoading();
      map.generate();
      Riiablo.client.popScreen();
      return;
    }

    Riiablo.batch.setPalette(Riiablo.palettes.loading);
    stage.act();
    stage.draw();
  }

  public interface Loadable {
    Array<AssetDescriptor> getDependencies();
  }
}
