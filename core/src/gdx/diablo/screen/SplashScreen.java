package gdx.diablo.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import gdx.diablo.BlendMode;
import gdx.diablo.Diablo;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.DC6;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.DC6Loader;

public class SplashScreen extends ScreenAdapter {

  final AssetDescriptor<DC6> TitleScreenDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\TitleScreen.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion TitleScreen;

  final AssetDescriptor<DC6> D2logoBlackLeftDescriptor  = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoBlackLeft.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoFireLeftDescriptor   = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoFireLeft.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoBlackRightDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoBlackRight.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoFireRightDescriptor  = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoFireRight.DC6", DC6.class);
  Animation D2logoLeft;
  Animation D2logoRight;

  GlyphLayout pressContinueGlyphs;

  final AssetDescriptor<Sound> selectDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\select.wav", Sound.class);
  Sound select;

  InputProcessor inputProcessor;

  boolean pressedContinue;

  public SplashScreen() {
    Diablo.assets.load(TitleScreenDescriptor);
    Diablo.assets.load(D2logoBlackLeftDescriptor);
    Diablo.assets.load(D2logoFireLeftDescriptor);
    Diablo.assets.load(D2logoBlackRightDescriptor);
    Diablo.assets.load(D2logoFireRightDescriptor);
    Diablo.assets.load(selectDescriptor);
  }

  @Override
  public void show() {
    Diablo.music.enqueue("data/global/music/Act1/tristram.wav");

    Diablo.assets.finishLoadingAsset(TitleScreenDescriptor);
    TitleScreen = Diablo.assets.get(TitleScreenDescriptor).getTexture();

    Diablo.assets.finishLoadingAsset(D2logoBlackLeftDescriptor);
    Diablo.assets.finishLoadingAsset(D2logoFireLeftDescriptor);
    D2logoLeft = Animation.builder()
        .layer(Diablo.assets.get(D2logoBlackLeftDescriptor))
        .layer(Diablo.assets.get(D2logoFireLeftDescriptor), BlendMode.LUMINOSITY)
        .build();

    Diablo.assets.finishLoadingAsset(D2logoBlackRightDescriptor);
    Diablo.assets.finishLoadingAsset(D2logoFireRightDescriptor);
    D2logoRight = Animation.builder()
        .layer(Diablo.assets.get(D2logoBlackRightDescriptor))
        .layer(Diablo.assets.get(D2logoFireRightDescriptor), BlendMode.LUMINOSITY)
        .build();

    String press_to_continue;
    // TODO: Update message for controllers press_any_button
    if (Gdx.app.getType() == Application.ApplicationType.Android) {
      press_to_continue = Diablo.bundle.get("touch_the_screen");
    } else {
      press_to_continue = Diablo.bundle.get("press_any_key");
    }
    pressContinueGlyphs = new GlyphLayout(Diablo.fonts.fontformal12, press_to_continue);

    Diablo.input.addProcessor(inputProcessor = new InputAdapter() {
      @Override
      public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE
            || keycode == Input.Keys.VOLUME_DOWN || keycode == Input.Keys.VOLUME_UP) {
          return true;
        }

        return pressToContinue();
      }

      @Override
      public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return pressToContinue();
      }
    });
  }

  @Override
  public void hide() {}

  @Override
  public void dispose() {
    Diablo.input.removeProcessor(inputProcessor);
    Diablo.assets.unload(TitleScreenDescriptor.fileName);
    Diablo.assets.unload(D2logoBlackLeftDescriptor.fileName);
    Diablo.assets.unload(D2logoFireLeftDescriptor.fileName);
    Diablo.assets.unload(D2logoBlackRightDescriptor.fileName);
    Diablo.assets.unload(D2logoFireRightDescriptor.fileName);
    Diablo.assets.unload(selectDescriptor.fileName);
  }

  @Override
  public void render(float delta) {
    PaletteIndexedBatch b = Diablo.batch;
    b.begin(Diablo.palettes.units);
    b.draw(TitleScreen, Diablo.VIRTUAL_WIDTH_CENTER - (TitleScreen.getRegionWidth() / 2), 0);

    int x = Diablo.VIRTUAL_WIDTH_CENTER;
    float y = (Diablo.VIRTUAL_HEIGHT * 0.75f);
    D2logoLeft.act(delta);
    D2logoLeft.draw(b, x, y);
    D2logoRight.act(delta);
    D2logoRight.draw(b, x, y);

    Diablo.fonts.fontformal12.draw(b, pressContinueGlyphs, x - ((int) pressContinueGlyphs.width >>> 1), (int) (Diablo.VIRTUAL_HEIGHT * 0.25));

    b.end();
  }

  private boolean pressToContinue() {
    if (pressedContinue) {
      return false;
    }

    Diablo.assets.finishLoadingAsset(selectDescriptor);
    select = Diablo.assets.get(selectDescriptor);
    select.play();
    Diablo.input.vibrate(25);

    pressedContinue = true;
    Diablo.client.setScreen(new MenuScreen(D2logoLeft, D2logoRight));
    return true;
  }
}
