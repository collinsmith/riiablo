package com.riiablo.screen;

import com.badlogic.gdx.Screen;
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

  private final AssetDescriptor<DC6> loadingscreenDescriptor = new AssetDescriptor<>("data\\local\\ui\\loadingscreen.dc6", DC6.class);

  private Screen screen;
  private Array<AssetDescriptor> assets;

  private Stage stage;
  private AnimationWrapper loadingscreenWrapper;

  private Map map;

  public GameLoadingScreen(Array<AssetDescriptor> assets, GameScreen screen) {
    this.screen = screen;
    this.map = screen.map;

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

    this.assets = assets;
    if (assets != null) {
      for (AssetDescriptor asset : assets) {
        Riiablo.assets.load(asset);
      }
    }

    map.setAct(0);
    map.load();
  }

  public GameLoadingScreen(GameScreen screen) {
    this(screen.getDependencies(), screen);
  }

  @Override
  public void show() {
    Riiablo.viewport = Riiablo.defaultViewport;
    if (assets == null) {
      Riiablo.client.clearAndSet(screen);
    }
  }

  @Override
  public void hide() {
    if (assets != null) {
      Riiablo.assets.unload(loadingscreenDescriptor.fileName);
    }
  }

  @Override
  public void render(float delta) {
    if (Riiablo.assets.update()) {
      map.finishLoading();
      map.generate();
      Riiablo.client.clearAndSet(screen);
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
