package com.riiablo.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.DC6Loader;
import com.riiablo.widget.AnimationWrapper;
import com.riiablo.widget.Label;
import com.riiablo.widget.TextButton;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TCPIPScreen extends ScreenAdapter {
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
  final AssetDescriptor<DC6>   MediumButtonBlankDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\MediumButtonBlank.dc6", DC6.class);
  final AssetDescriptor<Sound> buttonDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\button.wav", Sound.class);

  final AssetDescriptor<Sound> selectDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\select.wav", Sound.class);

  private Stage stage;
  private AnimationWrapper D2logo;
  private Label  lbHostIP;
  private Button btnHostGame;
  private Button btnJoinGame;
  private Label  lbDescription;
  private Button btnCancel;

  public TCPIPScreen(Animation D2logoLeft, Animation D2logoRight) {
    this.D2logoLeft = D2logoLeft;
    this.D2logoRight = D2logoRight;
    load();

    stage = new Stage(Riiablo.defaultViewport, Riiablo.batch);

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
      public boolean mouseMoved(InputEvent event, float x, float y) {
        if (btnHostGame.isOver()) {
          lbDescription.setText(5122);
          lbDescription.layout();
        } else if (btnJoinGame.isOver()) {
          lbDescription.setText(5123);
          lbDescription.layout();
        }

        return super.mouseMoved(event, x, y);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        lbDescription.setText(null);
        super.exit(event, x, y, pointer, toActor);
      }

      @Override
      public void clicked(InputEvent event, float x, float y) {
        Actor actor = event.getListenerActor();
        if (actor == btnHostGame) {
        } else if (actor == btnJoinGame) {
          try {
            Gdx.input.getTextInput(new Input.TextInputListener() {
              @Override
              public void input(String text) {
                Socket socket = Gdx.net.newClientSocket(Net.Protocol.TCP, text, 6112, new SocketHints());
                socket.dispose();
              }

              @Override
              public void canceled() {}
            }, "Enter IP", "127.0.0.1", "127.0.0.1");
          } catch (GdxRuntimeException e) {
            Gdx.app.log(TAG, e.getMessage(), e);
          }
        } else if (actor == btnCancel) {
          Riiablo.client.popScreen();
        }
      }
    };
    String ip = null;
    try {
      InetAddress address = InetAddress.getLocalHost();
      ip = address.getHostAddress();
    } catch (UnknownHostException e) {}
    lbHostIP = new Label(Riiablo.string.lookup(5121) + '\n' + ip, Riiablo.fonts.font16);
    lbHostIP.setColor(Riiablo.colors.gold);
    lbHostIP.setAlignment(Align.center);
    btnHostGame = new TextButton(5118, style);
    btnHostGame.addListener(clickListener);
    btnJoinGame = new TextButton(5119, style);
    btnJoinGame.addListener(clickListener);
    lbDescription = new Label(null, Riiablo.fonts.fontformal12);
    lbDescription.setColor(Riiablo.colors.gold);
    lbDescription.setAlignment(Align.center | Align.top);
    lbDescription.setWrap(true);

    // TODO: Sizing could be cleaned up some
    final Table panel = new Table() {{
      final float SPACING = 8;
      add(lbHostIP).space(SPACING).row();
      add(btnHostGame).space(SPACING).row();
      add(btnJoinGame).space(SPACING).row();
      add(lbDescription).space(SPACING).minSize(400, 112).row();
      pack();
    }};
    panel.setPosition(stage.getWidth() / 2, D2logo.getY() / 2, Align.center);
    stage.addActor(panel);

    TextButton.TextButtonStyle mediumButtonStyle = new TextButton.TextButtonStyle() {{
      Riiablo.assets.finishLoadingAsset(MediumButtonBlankDescriptor);
      DC6 pages = Riiablo.assets.get(MediumButtonBlankDescriptor);
      up = new TextureRegionDrawable(pages.getTexture(0));
      down = new TextureRegionDrawable(pages.getTexture(1));
      font = Riiablo.fonts.fontexocet10;
    }};
    btnCancel = new TextButton(5134, mediumButtonStyle);
    btnCancel.addListener(clickListener);
    btnCancel.setPosition(20, 20, Align.bottomLeft);
    stage.addActor(btnCancel);
  }

  @Override
  public void show() {
    Riiablo.input.addProcessor(stage);
    load();
  }

  @Override
  public void hide() {
    Riiablo.viewport = Riiablo.defaultViewport;
    dispose();
    Riiablo.input.removeProcessor(stage);
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
