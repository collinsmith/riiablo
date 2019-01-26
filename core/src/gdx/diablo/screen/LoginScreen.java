package gdx.diablo.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;

import gdx.diablo.BlendMode;
import gdx.diablo.Diablo;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.StringTBL;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.server.Account;
import gdx.diablo.widget.Label;
import gdx.diablo.widget.TextButton;
import gdx.diablo.widget.TextField;

public class LoginScreen extends ScreenAdapter {
  private static final String TAG = "LoginScreen";

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

  final AssetDescriptor<DC6> textbox2Descriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\textbox2.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);

  private Stage stage;
  private Button btnLogIn;
  private Button btnAccountSettings;
  private Button btnCreateNewAccount;
  private Button btnCancel;

  public LoginScreen(Animation D2logoLeft, Animation D2logoRight) {
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
    Diablo.assets.load(textbox2Descriptor);

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
      public void clicked(InputEvent event, float x, float y) {
        Actor actor = event.getListenerActor();
        if (actor == btnLogIn) {
          Net.HttpRequest request = new HttpRequestBuilder()
              .newRequest()
              .method(Net.HttpMethods.POST)
              .url("http://hydra:6112/login")
              .jsonContent(new Account.Builder() {{ account = "test"; }})
              .build();
          Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
              final Account account = new Json().fromJson(Account.class, httpResponse.getResultAsStream());
              Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                  Diablo.client.pushScreen(new LobbyScreen(account));
                }
              });
            }

            @Override
            public void failed(Throwable t) {
              Gdx.app.error(TAG, t.getMessage());
            }

            @Override
            public void cancelled() {
            }
          });
        } else if (actor == btnAccountSettings) {
        } else if (actor == btnCreateNewAccount) {
        } else if (actor == btnCancel) {
          Diablo.client.popScreen();
        }
      }
    };
    btnLogIn = new TextButton(5288, style);
    btnLogIn.addListener(clickListener);
    btnAccountSettings = new TextButton(StringTBL.PATCH_OFFSET + 1108, style);
    btnAccountSettings.addListener(clickListener);
    btnCreateNewAccount = new TextButton(5221, style);
    btnCreateNewAccount.addListener(clickListener);

    final TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle() {{
      Diablo.assets.finishLoadingAsset(textbox2Descriptor);
      TextureRegion textbox2 = Diablo.assets.get(textbox2Descriptor).getTexture();
      background = new TextureRegionDrawable(textbox2);
      font = Diablo.fonts.fontformal12;
      fontColor = Diablo.colors.white;
      cursor = new TextureRegionDrawable(Diablo.textures.white);
    }};

    Table panel = new Table() {{
      add(new Label(5205, Diablo.fonts.font16, Diablo.colors.white) {{
        setWrap(true);
        setAlignment(Align.center);
      }}).width(400).space(8).row();
      add(new Table() {{
        add(new Label(5224, Diablo.fonts.font16, Diablo.colors.unique)).align(Align.left).row();
        add(new TextField(textFieldStyle)).row();
        align(Align.left);
      }}).space(8).row();
      add(new Table() {{
        add(new Label(5225, Diablo.fonts.font16, Diablo.colors.unique)).align(Align.left).row();
        add(new TextField(textFieldStyle)).row();
      }}).space(8).row();
      add(btnLogIn).space(8).row();
      add(btnAccountSettings).space(8).row();
      add(btnCreateNewAccount).space(8).row();
    }};
    panel.setX(stage.getWidth() / 2);
    panel.setY(stage.getHeight() * 0.325f);
    stage.addActor(panel);

    TextButton.TextButtonStyle mediumButtonStyle = new TextButton.TextButtonStyle() {{
      Diablo.assets.finishLoadingAsset(MediumButtonBlankDescriptor);
      DC6 pages = Diablo.assets.get(MediumButtonBlankDescriptor);
      up   = new TextureRegionDrawable(pages.getTexture(0));
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
    Diablo.assets.unload(textbox2Descriptor.fileName);
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
