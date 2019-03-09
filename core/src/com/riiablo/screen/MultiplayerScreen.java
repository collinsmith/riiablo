package com.riiablo.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.DC6Loader;
import com.riiablo.widget.AnimationWrapper;
import com.riiablo.widget.TextButton;

public class MultiplayerScreen extends ScreenAdapter {
  private static final String TAG = "MultiplayerScreen";

  final AssetDescriptor<DC6> TitleScreenDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\TitleScreen.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion TitleScreen;

  final AssetDescriptor<DC6> D2logoBlackLeftDescriptor  = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoBlackLeft.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoFireLeftDescriptor   = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoFireLeft.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoBlackRightDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoBlackRight.DC6", DC6.class);
  final AssetDescriptor<DC6> D2logoFireRightDescriptor  = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\D2logoFireRight.DC6", DC6.class);
  Animation D2logoLeft;
  Animation D2logoRight;

  final AssetDescriptor<DC6>   WideButtonBlankDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\3WideButtonBlank.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  final AssetDescriptor<Sound> buttonDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\button.wav", Sound.class);

  final AssetDescriptor<Sound> selectDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\select.wav", Sound.class);

  private Stage stage;
  private AnimationWrapper D2logo;
  private Button btnOpenBattlenet;
  private Button btnTCPIP;
  private Button btnCancel;

  public MultiplayerScreen(Animation D2logoLeft, Animation D2logoRight) {
    this.D2logoLeft = D2logoLeft;
    this.D2logoRight = D2logoRight;
    Riiablo.assets.load(TitleScreenDescriptor);
    Riiablo.assets.load(D2logoFireLeftDescriptor);
    Riiablo.assets.load(D2logoFireRightDescriptor);
    Riiablo.assets.load(D2logoBlackLeftDescriptor);
    Riiablo.assets.load(D2logoBlackRightDescriptor);
    Riiablo.assets.load(WideButtonBlankDescriptor);
    Riiablo.assets.load(buttonDescriptor);
    Riiablo.assets.load(selectDescriptor);

    stage = new Stage(Riiablo.viewport, Riiablo.batch);

    Riiablo.assets.finishLoadingAsset(TitleScreenDescriptor);
    TitleScreen = Riiablo.assets.get(TitleScreenDescriptor).getTexture();

    if (D2logoLeft == null || D2logoRight == null) {
      Riiablo.music.enqueue("data/global/music/Act1/tristram.wav");
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

    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle() {{
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
        if (actor == btnOpenBattlenet) {
          Riiablo.client.pushScreen(new LoginScreen(MultiplayerScreen.this.D2logoLeft, MultiplayerScreen.this.D2logoRight));
        } else if (actor == btnTCPIP) {
          Riiablo.client.pushScreen(new TCPIPScreen(MultiplayerScreen.this.D2logoLeft, MultiplayerScreen.this.D2logoRight));
        } else if (actor == btnCancel) {
          Riiablo.client.popScreen();
        }
      }
    };
    btnOpenBattlenet = new TextButton(5115, style);
    btnOpenBattlenet.addListener(clickListener);
    btnTCPIP = new TextButton(5116, style);
    btnTCPIP.addListener(clickListener);
    btnCancel = new TextButton(5134, style);
    btnCancel.addListener(clickListener);

    Table panel = new Table() {{
      add(btnOpenBattlenet).space(8).row();
      add(btnTCPIP).space(8).row();
      add(btnCancel).space(8).row();
    }};
    panel.setX(stage.getWidth() / 2);
    panel.setY(stage.getHeight() * 0.40f);
    stage.addActor(panel);
  }

  @Override
  public void show() {

    Riiablo.input.addProcessor(stage);
  }

  @Override
  public void hide() {
    Riiablo.input.removeProcessor(stage);
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(TitleScreenDescriptor.fileName);
    Riiablo.assets.unload(D2logoFireLeftDescriptor.fileName);
    Riiablo.assets.unload(D2logoFireRightDescriptor.fileName);
    Riiablo.assets.unload(D2logoBlackLeftDescriptor.fileName);
    Riiablo.assets.unload(D2logoBlackRightDescriptor.fileName);
    Riiablo.assets.unload(WideButtonBlankDescriptor.fileName);
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
