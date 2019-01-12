package gdx.diablo.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import gdx.diablo.BlendMode;
import gdx.diablo.CharClass;
import gdx.diablo.Diablo;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.DC6;
import gdx.diablo.entity.Player;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.widget.CharButton;
import gdx.diablo.widget.TextButton;

public class CreateCharacterScreen extends ScreenAdapter {
  final AssetDescriptor<DC6> CharacterCreateDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\CharacterCreate.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion CharacterCreate;

  final AssetDescriptor<DC6> fireDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\fire.DC6", DC6.class);
  Animation fire;

  /*
  final AssetDescriptor<Animation> groundLDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\groundL.DC6.anim", Animation.class);
  final AssetDescriptor<Animation> groundRDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\groundR.DC6.anim", Animation.class);
  Animation groundL;
  Animation groundR;

  final AssetDescriptor<Animation> sceneL1Descriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\sceneL1.DC6.anim", Animation.class);
  final AssetDescriptor<Animation> sceneL2Descriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\sceneL2.DC6.anim", Animation.class);
  final AssetDescriptor<Animation> sceneRDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\sceneR.DC6.anim", Animation.class);
  Animation sceneL1;
  Animation sceneL2;
  Animation sceneR;
  */

  final AssetDescriptor<DC6> MediumSelButtonBlankDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\MediumSelButtonBlank.dc6", DC6.class);
  final AssetDescriptor<Sound> buttonDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\button.wav", Sound.class);

  final AssetDescriptor<DC6> textboxDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\textbox.dc6", DC6.class);
  private TextField tfCharName;

  private Stage stage;
  private InputProcessor inputProcessor;
  private Button btnExit;
  private Button btnOK;

  private Label selectedName;
  private Label selectedDescription;
  private CharButton selected;

  private CharButton amazon, assassin, necromancer, barbarian, paladin, sorceress, druid;

  public CreateCharacterScreen() {
    Diablo.assets.load(CharacterCreateDescriptor);
    Diablo.assets.load(fireDescriptor);
    /*
    Diablo.assets.load(groundLDescriptor);
    Diablo.assets.load(groundRDescriptor);
    Diablo.assets.load(sceneL1Descriptor);
    Diablo.assets.load(sceneL2Descriptor);
    Diablo.assets.load(sceneRDescriptor);
    */
    Diablo.assets.load(MediumSelButtonBlankDescriptor);
    Diablo.assets.load(textboxDescriptor);
    Diablo.assets.load(buttonDescriptor);

    stage = new Stage(Diablo.viewport, Diablo.batch);
  }

  @Override
  public void show() {
    Diablo.assets.finishLoadingAsset(CharacterCreateDescriptor);
    CharacterCreate = Diablo.assets.get(CharacterCreateDescriptor).getTexture();

    Diablo.assets.finishLoadingAsset(fireDescriptor);
    fire = Animation.newAnimation(Diablo.assets.get(fireDescriptor));
    /*
    new Thread(new Runnable() {
      @Override
      public void run() {
        while (!Diablo.assets.isLoaded(fireDescriptor)
            || !Diablo.assets.isLoaded(groundLDescriptor)
            || !Diablo.assets.isLoaded(groundRDescriptor));

        System.out.println("loaded fire");
        //Diablo.assets.finishLoadingAsset(fireDescriptor);
        //Diablo.assets.finishLoadingAsset(groundLDescriptor);
        //Diablo.assets.finishLoadingAsset(groundRDescriptor);
        Gdx.app.postRunnable(new Runnable() {
          @Override
          public void run() {
            fire    = Diablo.assets.get(fireDescriptor);
            groundL = Diablo.assets.get(groundLDescriptor);
            groundR = Diablo.assets.get(groundRDescriptor);
            groundL.setFrameDuration(1 / 16f);
            groundR.setFrameDuration(1 / 16f);
          }
        });
      }
    }).start();

    new Thread(new Runnable() {
      @Override
      public void run() {
        Diablo.assets.finishLoadingAsset(sceneL1Descriptor);
        Diablo.assets.finishLoadingAsset(sceneL2Descriptor);
        Diablo.assets.finishLoadingAsset(sceneRDescriptor);
        sceneL1 = Diablo.assets.get(sceneL1Descriptor);
        sceneL2 = Diablo.assets.get(sceneL2Descriptor);
        sceneR  = Diablo.assets.get(sceneRDescriptor);
        sceneL1.setFrameDuration(1 / 16f);
        sceneL2.setFrameDuration(1 / 16f);
        sceneR.setFrameDuration(1 / 16f);
      }
    }).start();
    */

    tfCharName = new TextField("", new TextField.TextFieldStyle() {{
      Diablo.assets.finishLoadingAsset(textboxDescriptor);
      DC6 pages = Diablo.assets.get(textboxDescriptor);
      background = new TextureRegionDrawable(pages.getTexture(0)) {{
        final float padding = 8;
        setLeftWidth(padding);
        //setTopHeight(padding);
        setRightWidth(padding);
        setBottomHeight(padding);
      }};
      font = Diablo.fonts.fontformal12;
      fontColor = Color.WHITE;

      BitmapFont.Glyph glyph = font.getData().getGlyph('_');
      TextureRegion cursorRegion = new TextureRegion(font.getRegion(glyph.page), glyph.srcX, glyph.srcY, glyph.width, glyph.height);
      cursor = new TextureRegionDrawable(cursorRegion);
    }});
    tfCharName.setPosition(Diablo.VIRTUAL_WIDTH_CENTER - (tfCharName.getWidth() / 2), 20);
    tfCharName.setOnlyFontChars(true);
    tfCharName.setTextFieldListener(new TextField.TextFieldListener() {
      @Override
      public void keyTyped(TextField textField, char c) {
        btnOK.setVisible(!textField.getText().isEmpty());
      }
    });
    tfCharName.setVisible(false);

    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle() {{
        Diablo.assets.finishLoadingAsset(MediumSelButtonBlankDescriptor);
      DC6 pages = Diablo.assets.get(MediumSelButtonBlankDescriptor);
        up   = new TextureRegionDrawable(pages.getTexture(0));
        down = new TextureRegionDrawable(pages.getTexture(1));
        font = Diablo.fonts.fontexocet10;
    }};
    ChangeListener clickListener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        //Actor actor = event.getListenerActor();
        if (actor == btnExit) {
          Diablo.client.popScreen();
        } else if (actor == btnOK) {
          if (selected == null) return;
          //Diablo.client.clearAndSet(new LoadingScreen(new GameScreen(new Char(selected.charClass))));
          Diablo.client.clearAndSet(new LoadingScreen(new GameScreen(new Player(tfCharName.getText(), selected.charClass))));
        }
      }
    };
    btnExit = new TextButton(5101, style);
    btnExit.addListener(clickListener);
    btnExit.setPosition(20, 20);
    btnOK = new TextButton(5102, style);
    btnOK.addListener(clickListener);
    btnOK.setPosition(Diablo.VIRTUAL_WIDTH - 20 - btnOK.getWidth(), 20);
    btnOK.setVisible(false);

    selectedName = new Label(Diablo.string.lookup(5127), new Label.LabelStyle(Diablo.fonts.font42, null));
    selectedName.setPosition(Diablo.VIRTUAL_WIDTH_CENTER - (selectedName.getPrefWidth() / 2), Diablo.VIRTUAL_HEIGHT - Diablo.fonts.font42.getLineHeight() - 10);
    selectedName.setVisible(true);
    selectedDescription = new Label("", new Label.LabelStyle(Diablo.fonts.font16, null));
    selectedDescription.setVisible(false);
    selected = null;
    ClickListener selectedListener = new ClickListener(Input.Buttons.LEFT) {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        CharButton newSelected = (CharButton) event.getListenerActor();
        if (newSelected.getState() == CharButton.State.BW) {
          selectedName.setVisible(false);
          selectedDescription.setVisible(false);
          btnOK.setVisible(false);
          tfCharName.setVisible(false);
          selected = null;
          return;
        }

        if (selected != null && selected.isSelected()) {
          selected.deselect();
        }

        selected = newSelected;
        selectedName.setVisible(true);
        selectedName.setText(Diablo.string.lookup(selected.charClass.name));
        selectedName.setPosition(Diablo.VIRTUAL_WIDTH_CENTER - (selectedName.getPrefWidth() / 2), Diablo.VIRTUAL_HEIGHT - Diablo.fonts.font42.getLineHeight() - 10);
        selectedDescription.setText(Diablo.string.lookup(selected.charClass.description));
        selectedDescription.setWidth(300);
        selectedDescription.setAlignment(Align.center);
        selectedDescription.setWrap(true);
        selectedDescription.setPosition(
            Diablo.VIRTUAL_WIDTH_CENTER - (selectedDescription.getWidth() / 2),
            Diablo.VIRTUAL_HEIGHT - Diablo.fonts.font42.getLineHeight() - 30);
        selectedDescription.setVisible(true);
        btnOK.setVisible(!tfCharName.getText().isEmpty());
        tfCharName.setVisible(true);
        stage.setKeyboardFocus(tfCharName);
      }
    };

    int x = Diablo.VIRTUAL_WIDTH_CENTER - 11;
    int y = 225;
    amazon = new CharButton(CharClass.AMAZON);
    amazon.setPosition(x - 210, y - 20);
    amazon.addListener(selectedListener);

    assassin = new CharButton(CharClass.ASSASSIN);
    assassin.setPosition(x - 120, y - 25);
    assassin.addListener(selectedListener);

    necromancer = new CharButton(CharClass.NECROMANCER);
    necromancer.setPosition(x - 80, y);
    necromancer.addListener(selectedListener);

    barbarian = new CharButton(CharClass.BARBARIAN);
    barbarian.setPosition(x, y + 5);
    barbarian.addListener(selectedListener);

    paladin = new CharButton(CharClass.PALADIN);
    paladin.setPosition(x + 195, y - 25);
    paladin.addListener(selectedListener);

    sorceress = new CharButton(CharClass.SORCERESS);
    sorceress.setPosition(x + 115, y - 5);
    sorceress.addListener(selectedListener);

    druid = new CharButton(CharClass.DRUID);
    druid.setPosition(x + 305, y - 50);
    druid.addListener(selectedListener);

    amazon.loadRemainingAssets();
    assassin.loadRemainingAssets();
    necromancer.loadRemainingAssets();
    barbarian.loadRemainingAssets();
    paladin.loadRemainingAssets();
    sorceress.loadRemainingAssets();
    druid.loadRemainingAssets();

    //stage.setDebugAll(true);

    stage.addActor(btnExit);
    stage.addActor(btnOK);
    stage.addActor(tfCharName);
    stage.addActor(barbarian);
    stage.addActor(necromancer);
    stage.addActor(sorceress);
    stage.addActor(assassin);
    stage.addActor(paladin);
    stage.addActor(amazon);
    stage.addActor(druid);
    stage.addActor(selectedName);
    stage.addActor(selectedDescription);

    inputProcessor = new InputMultiplexer() {{
      addProcessor(new InputAdapter() {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
          if (selected != null && selected.isAnimating()) {
            return true;
          }

          return super.touchDown(screenX, screenY, pointer, button);
        }

        @Override
        public boolean keyDown(int keycode) {
          switch (keycode) {
            case Input.Keys.ENTER:
              if (tfCharName.getText().length() > 2) btnOK.toggle();
              return true;

            case Input.Keys.ESCAPE:
              btnExit.toggle();
              return true;

            default:
              return super.keyDown(keycode);
          }
        }
      });
      addProcessor(stage);
    }};
    Diablo.input.addProcessor(inputProcessor);
  }

  @Override
  public void hide() {
    Diablo.input.removeProcessor(inputProcessor);
  }

  @Override
  public void dispose() {
    amazon.dispose();
    assassin.dispose();
    necromancer.dispose();
    barbarian.dispose();
    paladin.dispose();
    sorceress.dispose();
    druid.dispose();
    Diablo.assets.unload(CharacterCreateDescriptor.fileName);
    Diablo.assets.unload(fireDescriptor.fileName);
    /*
    Diablo.assets.unload(groundLDescriptor.fileName);
    Diablo.assets.unload(groundRDescriptor.fileName);
    Diablo.assets.unload(sceneL1Descriptor.fileName);
    Diablo.assets.unload(sceneL2Descriptor.fileName);
    Diablo.assets.unload(sceneRDescriptor.fileName);
    */
    Diablo.assets.unload(MediumSelButtonBlankDescriptor.fileName);
    Diablo.assets.unload(buttonDescriptor.fileName);
  }

  @Override
  public void render(float delta) {
    PaletteIndexedBatch b = Diablo.batch;
    b.begin(Diablo.palettes.fechar);

    int x = Diablo.VIRTUAL_WIDTH_CENTER;
    int y = Diablo.VIRTUAL_HEIGHT_CENTER;
    int xOff = (CharacterCreate.getRegionWidth()  / 2);
    int yOff = (CharacterCreate.getRegionHeight() / 2);
    b.draw(CharacterCreate, x - xOff, y - yOff);

    /*
    if (sceneL1 != null && sceneL2 != null && sceneR != null) {
      xOff = 100;
      sceneL2.act(delta);
      sceneL2.draw(b, xOff, 600 + (15) - 204);
      sceneL1.act(delta);
      sceneL1.draw(b, xOff, 600 + (15) - 204 - 231);
      sceneR.act(delta);
      sceneR.draw(b, Diablo.VIRTUAL_WIDTH - 113, Diablo.VIRTUAL_HEIGHT - 179);
    }

    if (groundL != null && groundR != null) {
      xOff = -23;
      yOff = -22;
      groundL.act(delta);
      groundL.draw(b, x + xOff, y + yOff);
      groundR.act(delta);
      groundR.draw(b, x + xOff, y + yOff);
    }
    */

    b.end();

    stage.act(delta);
    stage.draw();

    if (fire != null) {
      b.begin();
      b.flush();
      b.getShader().setUniformi("blendMode", BlendMode.LUMINOSITY);
      fire.act(delta);
      fire.draw(b, x - 22, y);
      b.flush();
      b.getShader().setUniformi("blendMode", BlendMode.NONE);
      b.end();
    }
  }
}
