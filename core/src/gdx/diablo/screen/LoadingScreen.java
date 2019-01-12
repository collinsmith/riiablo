package gdx.diablo.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.utils.Array;

import gdx.diablo.Diablo;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.DC6;
import gdx.diablo.graphics.PaletteIndexedBatch;

public class LoadingScreen extends ScreenAdapter {

  private final AssetDescriptor<DC6> loadingscreenDescriptor = new AssetDescriptor<>("data\\local\\ui\\loadingscreen.dc6", DC6.class);
  private Animation loadingscreen;

  private Screen screen;
  private Array<AssetDescriptor> assets;

  public LoadingScreen(Array<AssetDescriptor> assets, Screen screen) {
    this.screen = screen;

    Diablo.assets.load(loadingscreenDescriptor);
    Diablo.assets.finishLoadingAsset(loadingscreenDescriptor);
    loadingscreen = Animation.newAnimation(Diablo.assets.get(loadingscreenDescriptor));

    this.assets = assets;
    if (assets != null) {
      for (AssetDescriptor asset : assets) {
        Diablo.assets.load(asset);
      }
    }
  }

  public <T extends Screen & Loadable> LoadingScreen(T screen) {
    this(screen.getDependencies(), screen);
  }

  @Override
  public void show() {
    if (assets == null) {
      Diablo.client.setScreen(screen);
    }
  }

  @Override
  public void hide() {
    if (assets != null) {
      Diablo.assets.unload(loadingscreenDescriptor.fileName);
    }
  }

  @Override
  public void render(float delta) {
    if (Diablo.assets.update()) {
      Diablo.client.clearAndSet(screen);
      return;
    }

    PaletteIndexedBatch b = Diablo.batch;
    b.begin(Diablo.palettes.loading);
    loadingscreen.setFrame((int) (Diablo.assets.getProgress() * (loadingscreen.getNumFramesPerDir() - 1)));
    loadingscreen.draw(b,
        Diablo.VIRTUAL_WIDTH_CENTER  - (int) (loadingscreen.getMinWidth()  / 2),
        Diablo.VIRTUAL_HEIGHT_CENTER - (int) (loadingscreen.getMinHeight() / 2));
    b.end();
  }

  public interface Loadable {
    Array<AssetDescriptor> getDependencies();
  }
}
