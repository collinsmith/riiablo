package gdx.diablo.screen;

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

import java.net.InetAddress;
import java.net.UnknownHostException;

import gdx.diablo.BlendMode;
import gdx.diablo.Diablo;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.DC6;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.widget.Label;
import gdx.diablo.widget.TextButton;

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
  private Label  lbHostIP;
  private Button btnHostGame;
  private Button btnJoinGame;
  private Label  lbDescription;
  private Button btnCancel;

  public TCPIPScreen(Animation D2logoLeft, Animation D2logoRight) {
    this.D2logoLeft = D2logoLeft;
    this.D2logoRight = D2logoRight;
    Diablo.assets.load(TitleScreenDescriptor);
    Diablo.assets.load(D2logoFireLeftDescriptor);
    Diablo.assets.load(D2logoFireRightDescriptor);
    Diablo.assets.load(D2logoBlackLeftDescriptor);
    Diablo.assets.load(D2logoBlackRightDescriptor);
    Diablo.assets.load(WideButtonBlankDescriptor);
    Diablo.assets.load(MediumButtonBlankDescriptor);
    Diablo.assets.load(buttonDescriptor);
    Diablo.assets.load(selectDescriptor);

    stage = new Stage(Diablo.viewport, Diablo.batch);
  }

  @Override
  public void show() {
    Diablo.assets.finishLoadingAsset(TitleScreenDescriptor);
    TitleScreen = Diablo.assets.get(TitleScreenDescriptor).getTexture();

    if (D2logoLeft == null || D2logoRight == null) {
      Diablo.music.enqueue("data/global/music/Act1/tristram.wav");
    }

    if (D2logoLeft == null) {
      Diablo.assets.finishLoadingAsset(D2logoBlackLeftDescriptor);
      Diablo.assets.finishLoadingAsset(D2logoFireLeftDescriptor);
      D2logoLeft = Animation.builder()
          .layer(Diablo.assets.get(D2logoBlackLeftDescriptor))
          .layer(Diablo.assets.get(D2logoFireLeftDescriptor), BlendMode.LUMINOSITY)
          .build();
    }

    if (D2logoRight == null) {
      Diablo.assets.finishLoadingAsset(D2logoBlackRightDescriptor);
      Diablo.assets.finishLoadingAsset(D2logoFireRightDescriptor);
      D2logoRight = Animation.builder()
          .layer(Diablo.assets.get(D2logoBlackRightDescriptor))
          .layer(Diablo.assets.get(D2logoFireRightDescriptor), BlendMode.LUMINOSITY)
          .build();
    }

    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle() {{
        Diablo.assets.finishLoadingAsset(WideButtonBlankDescriptor);
        DC6 WideButtonBlank = Diablo.assets.get(WideButtonBlankDescriptor);
        up   = new TextureRegionDrawable(WideButtonBlank.getTexture(0));
        down = new TextureRegionDrawable(WideButtonBlank.getTexture(1));
        font = Diablo.fonts.fontexocet10;
    }};
    ClickListener clickListener = new ClickListener() {
      @Override
      public boolean mouseMoved(InputEvent event, float x, float y) {
        if (btnHostGame.isOver()) {
          lbDescription.setText(5122);
        } else if (btnJoinGame.isOver()) {
          lbDescription.setText(5123);
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
          Diablo.client.popScreen();
        }
      }
    };
    String ip = null;
    try {
      InetAddress address = InetAddress.getLocalHost();
      ip = address.getHostAddress();
    } catch (UnknownHostException e) {}
    lbHostIP = new Label(Diablo.string.lookup(5121) + '\n' + ip, Diablo.fonts.font16);
    lbHostIP.setColor(Diablo.colors.gold);
    lbHostIP.setAlignment(Align.center);
    btnHostGame = new TextButton(5118, style);
    btnHostGame.addListener(clickListener);
    btnJoinGame = new TextButton(5119, style);
    btnJoinGame.addListener(clickListener);
    lbDescription = new Label(null, Diablo.fonts.fontformal12);
    lbDescription.setColor(Diablo.colors.gold);
    lbDescription.setAlignment(Align.center | Align.top);
    lbDescription.setWrap(true);

    // TODO: Sizing could be cleaned up some
    Table panel = new Table() {{
      add(lbHostIP).space(8).row();
      add(btnHostGame).space(8).row();
      add(btnJoinGame).space(8).row();
      add(lbDescription).space(8).minSize(400, 112).row();
    }};
    panel.setX(stage.getWidth() / 2);
    panel.setY(stage.getHeight() * 0.375f);
    stage.addActor(panel);

    TextButton.TextButtonStyle mediumButtonStyle = new TextButton.TextButtonStyle() {{
      Diablo.assets.finishLoadingAsset(MediumButtonBlankDescriptor);
      DC6 pages = Diablo.assets.get(MediumButtonBlankDescriptor);
      up = new TextureRegionDrawable(pages.getTexture(0));
      down = new TextureRegionDrawable(pages.getTexture(1));
      font = Diablo.fonts.fontexocet10;
    }};
    btnCancel = new TextButton(5134, mediumButtonStyle);
    btnCancel.addListener(clickListener);
    btnCancel.setPosition(20, 20);
    stage.addActor(btnCancel);

    Diablo.input.addProcessor(stage);
  }

  @Override
  public void hide() {
    Diablo.input.removeProcessor(stage);
  }

  @Override
  public void dispose() {
    Diablo.assets.unload(TitleScreenDescriptor.fileName);
    Diablo.assets.unload(D2logoFireLeftDescriptor.fileName);
    Diablo.assets.unload(D2logoFireRightDescriptor.fileName);
    Diablo.assets.unload(D2logoBlackLeftDescriptor.fileName);
    Diablo.assets.unload(D2logoBlackRightDescriptor.fileName);
    Diablo.assets.unload(WideButtonBlankDescriptor.fileName);
    Diablo.assets.unload(MediumButtonBlankDescriptor.fileName);
    Diablo.assets.unload(buttonDescriptor.fileName);
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

    b.end();

    stage.act(delta);
    stage.draw();
  }
}
