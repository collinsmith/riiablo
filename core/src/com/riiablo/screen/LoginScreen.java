package com.riiablo.screen;

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
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.codec.StringTBL;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.DC6Loader;
import com.riiablo.server.Account;
import com.riiablo.widget.AnimationWrapper;
import com.riiablo.widget.Label;
import com.riiablo.widget.TextButton;
import com.riiablo.widget.TextField;

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
  private AnimationWrapper D2logo;
  private Button btnLogIn;
  private Button btnAccountSettings;
  private Button btnCreateNewAccount;
  private Button btnCancel;

  public LoginScreen(Animation D2logoLeft, Animation D2logoRight) {
    this.D2logoLeft = D2logoLeft;
    this.D2logoRight = D2logoRight;
    load();

    stage = new Stage(Riiablo.extendViewport, Riiablo.batch);

    Riiablo.assets.finishLoadingAsset(TitleScreenDescriptor);
    TitleScreen = Riiablo.assets.get(TitleScreenDescriptor).getTexture();

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
        if (actor == btnLogIn) {
          Net.HttpRequest request = new HttpRequestBuilder()
              .newRequest()
              .method(Net.HttpMethods.POST)
              .url("http://" + Riiablo.client.getRealm() + ":6112/login")
              .jsonContent(new Account.Builder() {{ account = "test"; }})
              .build();
          Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
              final Account account = new Json().fromJson(Account.class, httpResponse.getResultAsStream());
              Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                  Riiablo.client.pushScreen(new SelectCharacterScreen2(account));
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
          Riiablo.client.popScreen();
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
      Riiablo.assets.finishLoadingAsset(textbox2Descriptor);
      TextureRegion textbox2 = Riiablo.assets.get(textbox2Descriptor).getTexture();
      background = new TextureRegionDrawable(textbox2) {{
        final float padding = 4;
        setLeftWidth(padding);
        setTopHeight(padding);
        setRightWidth(padding);
        setBottomHeight(padding);
      }};
      font = Riiablo.fonts.fontformal12;
      fontColor = Riiablo.colors.white;
      cursor = new TextureRegionDrawable(Riiablo.textures.white);
    }};

    final Table panel = new Table() {{
      final float SPACING = 8;
      add(new Label(5205, Riiablo.fonts.font16, Riiablo.colors.white) {{
        setWrap(true);
        setAlignment(Align.center);
      }}).width(400).space(SPACING).row();
      add(new Table() {{
        add(new Label(5224, Riiablo.fonts.font16, Riiablo.colors.gold)).align(Align.left).row();
        add(new TextField(textFieldStyle)).row();
        align(Align.left);
      }}).space(SPACING).row();
      add(new Table() {{
        add(new Label(5225, Riiablo.fonts.font16, Riiablo.colors.gold)).align(Align.left).row();
        add(new TextField(textFieldStyle)).row();
      }}).space(SPACING).row();
      add(btnLogIn).space(SPACING).row();
      add(btnAccountSettings).space(SPACING).row();
      add(btnCreateNewAccount).space(SPACING).row();
    }};
    panel.setPosition(stage.getWidth() / 2, stage.getHeight() * 0.325f, Align.center);
    stage.addActor(panel);

    TextButton.TextButtonStyle mediumButtonStyle = new TextButton.TextButtonStyle() {{
      Riiablo.assets.finishLoadingAsset(MediumButtonBlankDescriptor);
      DC6 pages = Riiablo.assets.get(MediumButtonBlankDescriptor);
      up   = new TextureRegionDrawable(pages.getTexture(0));
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
    Riiablo.viewport = Riiablo.extendViewport;
    load();
    Riiablo.input.addProcessor(stage);
  }

  @Override
  public void hide() {
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
    Riiablo.assets.load(textbox2Descriptor);
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
    Riiablo.assets.unload(textbox2Descriptor.fileName);
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
