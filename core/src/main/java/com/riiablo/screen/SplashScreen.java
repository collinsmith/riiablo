package com.riiablo.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.DC6Loader;
import com.riiablo.widget.AnimationWrapper;
import com.riiablo.widget.Label;

public class SplashScreen extends ScreenAdapter {

  final AssetDescriptor<DC6> TitleScreenDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\TitleScreen.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion TitleScreen;

  final AssetDescriptor<DC6> D2logoBlackLeftDescriptor  = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoBlackLeft.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoFireLeftDescriptor   = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoFireLeft.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoBlackRightDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoBlackRight.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoFireRightDescriptor  = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoFireRight.DC6", DC6.class);
  Animation D2logoLeft;
  Animation D2logoRight;

  final AssetDescriptor<Sound> selectDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\select.wav", Sound.class);
  Sound select;

  InputProcessor inputProcessor;
  ControllerListener controllerListener;

  boolean pressedContinue;

  private Stage stage;
  private AnimationWrapper D2logo;
  private Label pressContinue;

  public SplashScreen() {
    Riiablo.assets.load(TitleScreenDescriptor);
    Riiablo.assets.load(D2logoBlackLeftDescriptor);
    Riiablo.assets.load(D2logoFireLeftDescriptor);
    Riiablo.assets.load(D2logoBlackRightDescriptor);
    Riiablo.assets.load(D2logoFireRightDescriptor);
    Riiablo.assets.load(selectDescriptor);

    stage = new Stage(Riiablo.viewport, Riiablo.batch);

    Riiablo.music.enqueue("data/global/music/Act4/diablo.wav");

    Riiablo.assets.finishLoadingAsset(TitleScreenDescriptor);
    TitleScreen = Riiablo.assets.get(TitleScreenDescriptor).getTexture();

    Riiablo.assets.finishLoadingAsset(D2logoBlackLeftDescriptor);
    Riiablo.assets.finishLoadingAsset(D2logoFireLeftDescriptor);
    D2logoLeft = Animation.builder()
        .layer(Riiablo.assets.get(D2logoBlackLeftDescriptor))
        .layer(Riiablo.assets.get(D2logoFireLeftDescriptor), BlendMode.LUMINOSITY)
        .build();

    Riiablo.assets.finishLoadingAsset(D2logoBlackRightDescriptor);
    Riiablo.assets.finishLoadingAsset(D2logoFireRightDescriptor);
    D2logoRight = Animation.builder()
        .layer(Riiablo.assets.get(D2logoBlackRightDescriptor))
        .layer(Riiablo.assets.get(D2logoFireRightDescriptor), BlendMode.LUMINOSITY)
        .build();

    D2logo = new AnimationWrapper(D2logoLeft, D2logoRight);
    D2logo.setPosition(stage.getWidth() * 0.50f, stage.getHeight() * 0.75f);
    stage.addActor(D2logo);

    String press_to_continue;
    // TODO: Update message for controllers press_any_button
    if (Gdx.app.getType() == Application.ApplicationType.Android) {
      press_to_continue = Riiablo.bundle.get("touch_the_screen");
    } else {
      press_to_continue = Riiablo.bundle.get("press_any_key");
    }
    pressContinue = new Label(press_to_continue, Riiablo.fonts.fontformal12);
    pressContinue.setPosition(stage.getWidth() * 0.50f, stage.getHeight() * 0.25f, Align.center);
    pressContinue.addAction(Actions.forever(Actions.sequence(
        Actions.alpha(0.50f, 2.0f, Interpolation.pow2In),
        Actions.alpha(1.00f, 2.0f, Interpolation.pow2Out))));
    stage.addActor(pressContinue);
  }

  @Override
  public void show() {
    Controllers.addListener(controllerListener = new ControllerAdapter() {
      @Override
      public boolean buttonDown(Controller controller, int buttonIndex) {
        return pressToContinue();
      }
    });
    Riiablo.input.addProcessor(inputProcessor = new InputAdapter() {
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
    Riiablo.input.addProcessor(stage);
  }

  @Override
  public void hide() {
    Controllers.removeListener(controllerListener);
    Riiablo.input.removeProcessor(inputProcessor);
    Riiablo.input.removeProcessor(stage);
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(TitleScreenDescriptor.fileName);
    Riiablo.assets.unload(D2logoBlackLeftDescriptor.fileName);
    Riiablo.assets.unload(D2logoFireLeftDescriptor.fileName);
    Riiablo.assets.unload(D2logoBlackRightDescriptor.fileName);
    Riiablo.assets.unload(D2logoFireRightDescriptor.fileName);
    Riiablo.assets.unload(selectDescriptor.fileName);
  }

  @Override
  public void render(float delta) {
    PaletteIndexedBatch b = Riiablo.batch;
    b.begin(Riiablo.palettes.units);
    b.draw(TitleScreen, (stage.getWidth() / 2) - (TitleScreen.getRegionWidth() / 2), 0);
    b.end();

    stage.act(delta);
    stage.draw();
  }

  private boolean pressToContinue() {
    if (pressedContinue) {
      return false;
    }

    Riiablo.assets.finishLoadingAsset(selectDescriptor);
    select = Riiablo.assets.get(selectDescriptor);
    select.play();
    Riiablo.input.vibrate(25);

    pressedContinue = true;
    Riiablo.client.setScreen(new MenuScreen(D2logoLeft, D2logoRight));
    return true;
  }
}
