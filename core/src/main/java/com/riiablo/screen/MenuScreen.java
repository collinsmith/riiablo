package com.riiablo.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.controllers.mappings.Xbox;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import com.riiablo.Riiablo;
import com.riiablo.RiiabloVersion;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.DC6Loader;
import com.riiablo.util.EventUtils;
import com.riiablo.widget.AnimationWrapper;
import com.riiablo.widget.Label;
import com.riiablo.widget.TextButton;

public class MenuScreen extends ScreenAdapter {
  final AssetDescriptor<DC6> TitleScreenDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\TitleScreen.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion TitleScreen;

  final AssetDescriptor<DC6> D2logoBlackLeftDescriptor  = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoBlackLeft.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoFireLeftDescriptor   = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoFireLeft.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoBlackRightDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoBlackRight.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoFireRightDescriptor  = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoFireRight.DC6", DC6.class);
  Animation D2logoLeft;
  Animation D2logoRight;

  final AssetDescriptor<DC6>   WideButtonBlankDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\3WideButtonBlank.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  final AssetDescriptor<DC6>   MediumButtonBlankDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\MediumButtonBlank.dc6", DC6.class);
  final AssetDescriptor<Sound> buttonDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\button.wav", Sound.class);

  final AssetDescriptor<Sound> selectDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\select.wav", Sound.class);

  private Stage stage;
  private AnimationWrapper D2logo;
  private Button btnSinglePlayer;
  private Button btnMultiplayer;
  private Button btnExitDiablo;
  private Button btnExit;
  private Label  lbVersion;

  ControllerListener controllerListener;

  public MenuScreen() {
    this(null, null);
  }

  public MenuScreen(Animation D2logoLeft, Animation D2logoRight) {
    this.D2logoLeft = D2logoLeft;
    this.D2logoRight = D2logoRight;
    load();

    stage = new Stage(Riiablo.defaultViewport, Riiablo.batch);

    Riiablo.assets.finishLoadingAsset(TitleScreenDescriptor);
    TitleScreen = Riiablo.assets.get(TitleScreenDescriptor).getTexture();

    if (D2logoLeft == null || D2logoRight == null) {
      Riiablo.music.enqueue("data/global/music/Act4/diablo.wav");
    }

    if (D2logoLeft == null) {
      Riiablo.assets.finishLoadingAsset(D2logoBlackLeftDescriptor);
      Riiablo.assets.finishLoadingAsset(D2logoFireLeftDescriptor);
      this.D2logoLeft = D2logoLeft = Animation.builder()
          .layer(Riiablo.assets.get(D2logoBlackLeftDescriptor))
          .layer(Riiablo.assets.get(D2logoFireLeftDescriptor), BlendMode.LUMINOSITY)
          .build();
    }

    if (D2logoRight == null) {
      Riiablo.assets.finishLoadingAsset(D2logoBlackRightDescriptor);
      Riiablo.assets.finishLoadingAsset(D2logoFireRightDescriptor);
      this.D2logoRight = D2logoRight = Animation.builder()
          .layer(Riiablo.assets.get(D2logoBlackRightDescriptor))
          .layer(Riiablo.assets.get(D2logoFireRightDescriptor), BlendMode.LUMINOSITY)
          .build();
    }
    D2logo = new AnimationWrapper(D2logoLeft, D2logoRight);
    D2logo.setPosition(stage.getWidth() * 0.50f, stage.getHeight() * 0.75f);
    stage.addActor(D2logo);

    TextButton.TextButtonStyle wideButtonStyle = new TextButton.TextButtonStyle() {{
      Riiablo.assets.finishLoadingAsset(WideButtonBlankDescriptor);
        DC6 WideButtonBlank = Riiablo.assets.get(WideButtonBlankDescriptor);
        up   = new TextureRegionDrawable(WideButtonBlank.getTexture(0));
        down = new TextureRegionDrawable(WideButtonBlank.getTexture(1));
        font = Riiablo.fonts.fontexocet10;
    }};
    ClickListener clickListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Actor actor = event.getListenerActor();
        if (actor == btnSinglePlayer) {
          Riiablo.client.pushScreen(new SelectCharacterScreen());
        } else if (actor == btnMultiplayer) {
          Riiablo.client.pushScreen(new MultiplayerScreen(MenuScreen.this.D2logoLeft, MenuScreen.this.D2logoRight));
        } else if (actor == btnExitDiablo || actor == btnExit) {
          Gdx.app.exit();
        }
      }
    };
    btnSinglePlayer = new TextButton(5106, wideButtonStyle);
    btnSinglePlayer.addListener(clickListener);
    btnMultiplayer = new TextButton(5107, wideButtonStyle);
    btnMultiplayer.addListener(clickListener);
    btnExitDiablo = new TextButton(5109, wideButtonStyle);
    btnExitDiablo.addListener(clickListener);

    final Table panel = new Table() {{
      final float SPACING = 8;
      add(btnSinglePlayer).space(SPACING).row();
      add(btnMultiplayer).space(SPACING).row();
      add(btnExitDiablo).space(SPACING).row();
      pack();
    }};
    panel.setPosition(stage.getWidth() / 2, D2logo.getY() / 2, Align.center);
    stage.addActor(panel);

    TextButton.TextButtonStyle mediumButtonStyle = new TextButton.TextButtonStyle() {{
      Riiablo.assets.finishLoadingAsset(MediumButtonBlankDescriptor);
        DC6 MediumButtonBlank = Riiablo.assets.get(MediumButtonBlankDescriptor);
        up   = new TextureRegionDrawable(MediumButtonBlank.getTexture(0));
        down = new TextureRegionDrawable(MediumButtonBlank.getTexture(1));
        font = Riiablo.fonts.fontexocet10;
    }};
    btnExit = new TextButton(5101, mediumButtonStyle);
    btnExit.addListener(clickListener);
    btnExit.setPosition(20, 20, Align.bottomLeft);
    //btnExit.setVisible(Gdx.app.getType() == Application.ApplicationType.Android);
    stage.addActor(btnExit);

    lbVersion = new Label(Riiablo.bundle.format("version", RiiabloVersion.VERSION), Riiablo.fonts.font16);
    lbVersion.setPosition(stage.getWidth() - 20, 20, Align.bottomRight);
    stage.addActor(lbVersion);
  }

  @Override
  public void show() {
    Riiablo.viewport = Riiablo.defaultViewport;
    load();
    Riiablo.input.addProcessor(stage);
    Controllers.addListener(controllerListener = new ControllerAdapter() {
      Vector2 tmp = new Vector2();
      Actor focus;

      {
        //focus(btnSinglePlayer);
      }

      boolean focus(Actor actor) {
        focus = actor;
        focus.localToScreenCoordinates(tmp.set(focus.getWidth() / 2, focus.getHeight() / 2));
        Gdx.input.setCursorPosition((int) tmp.x, (int) tmp.y);
        stage.mouseMoved((int) tmp.x, (int) tmp.y);
        return true;
      }

      @Override
      public boolean buttonDown(Controller controller, int buttonIndex) {
        System.out.println(buttonIndex);
        if (buttonIndex == Xbox.A) {
          EventUtils.click((Button) focus);
          return true;
        } else if (buttonIndex == Xbox.B) {
          EventUtils.click(btnExitDiablo);
          return true;
        }

        return false;
      }

      @Override
      public boolean povMoved(Controller controller, int povIndex, PovDirection value) {
        System.out.println(povIndex + " " + value);
        if (focus == null) {
          return focus(btnSinglePlayer);
        } else if (focus == btnSinglePlayer) {
          switch (value) {
            case north: return focus(btnExitDiablo);
            case south: return focus(btnMultiplayer);
          }
        } else if (focus == btnMultiplayer) {
          switch (value) {
            case north: return focus(btnSinglePlayer);
            case south: return focus(btnExitDiablo);
          }
        } else if (focus == btnExitDiablo) {
          switch (value) {
            case north: return focus(btnMultiplayer);
            case south: return focus(btnSinglePlayer);
          }
        }

        return false;
      }

      @Override
      public boolean axisMoved(Controller controller, int axisIndex, float value) {
        System.out.println(axisIndex + " " + value);
        return super.axisMoved(controller, axisIndex, value);
      }
    });
  }

  @Override
  public void hide() {
    Controllers.removeListener(controllerListener);
    Riiablo.input.removeProcessor(stage);
    dispose();
  }

  private void load() {
    Riiablo.assets.load(TitleScreenDescriptor);
    Riiablo.assets.load(D2logoFireLeftDescriptor);
    Riiablo.assets.load(D2logoFireRightDescriptor);
    Riiablo.assets.load(D2logoBlackLeftDescriptor);
    Riiablo.assets.load(D2logoBlackRightDescriptor);
    Riiablo.assets.load(WideButtonBlankDescriptor);
    Riiablo.assets.load(MediumButtonBlankDescriptor);
    Riiablo.assets.load(buttonDescriptor);
    Riiablo.assets.load(selectDescriptor);
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(TitleScreenDescriptor.fileName);
    Riiablo.assets.unload(D2logoFireLeftDescriptor.fileName);
    Riiablo.assets.unload(D2logoFireRightDescriptor.fileName);
    Riiablo.assets.unload(D2logoBlackLeftDescriptor.fileName);
    Riiablo.assets.unload(D2logoBlackRightDescriptor.fileName);
    Riiablo.assets.unload(WideButtonBlankDescriptor.fileName);
    Riiablo.assets.unload(MediumButtonBlankDescriptor.fileName);
    Riiablo.assets.unload(buttonDescriptor.fileName);
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
}
