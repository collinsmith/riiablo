package com.riiablo.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.riiablo.CharacterClass;
import com.riiablo.Client;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.DC6Loader;
import com.riiablo.widget.AnimationWrapper;
import com.riiablo.widget.CharacterCreateButton;
import com.riiablo.widget.Label;
import com.riiablo.widget.TextButton;
import com.riiablo.widget.TextField;

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
  private Table charOptions;

  private Stage stage;
  private InputProcessor inputProcessor;
  private Client.ScreenBoundsListener screenBoundsListener;
  private Button btnExit;
  private Button btnOK;

  private Label                 selectedName;
  private Label                 selectedDescription;
  private CharacterCreateButton selected;

  private CharacterCreateButton amazon, assassin, necromancer, barbarian, paladin, sorceress, druid;
  private AnimationWrapper fireWrapper;

  public CreateCharacterScreen() {
    Riiablo.assets.load(CharacterCreateDescriptor);
    Riiablo.assets.load(fireDescriptor);
    /*
    Riiablo.assets.load(groundLDescriptor);
    Riiablo.assets.load(groundRDescriptor);
    Riiablo.assets.load(sceneL1Descriptor);
    Riiablo.assets.load(sceneL2Descriptor);
    Riiablo.assets.load(sceneRDescriptor);
    */
    Riiablo.assets.load(MediumSelButtonBlankDescriptor);
    Riiablo.assets.load(textboxDescriptor);
    Riiablo.assets.load(buttonDescriptor);

    stage = new Stage(Riiablo.viewport, Riiablo.batch);
    //stage.setDebugAll(true);
  }

  @Override
  public void show() {
    Riiablo.assets.finishLoadingAsset(CharacterCreateDescriptor);
    CharacterCreate = Riiablo.assets.get(CharacterCreateDescriptor).getTexture();

    Riiablo.assets.finishLoadingAsset(fireDescriptor);
    fire = Animation.builder()
        .layer(Riiablo.assets.get(fireDescriptor), BlendMode.LUMINOSITY)
        .build();
    fireWrapper = new AnimationWrapper(fire);
    fireWrapper.setPosition(stage.getWidth() / 2 - 22, stage.getHeight() / 2);
    /*
    new Thread(new Runnable() {
      @Override
      public void run() {
        while (!Riiablo.assets.isLoaded(fireDescriptor)
            || !Riiablo.assets.isLoaded(groundLDescriptor)
            || !Riiablo.assets.isLoaded(groundRDescriptor));

        System.out.println("loaded fire");
        //Riiablo.assets.finishLoadingAsset(fireDescriptor);
        //Riiablo.assets.finishLoadingAsset(groundLDescriptor);
        //Riiablo.assets.finishLoadingAsset(groundRDescriptor);
        Gdx.app.postRunnable(new Runnable() {
          @Override
          public void run() {
            fire    = Riiablo.assets.get(fireDescriptor);
            groundL = Riiablo.assets.get(groundLDescriptor);
            groundR = Riiablo.assets.get(groundRDescriptor);
            groundL.setFrameDuration(1 / 16f);
            groundR.setFrameDuration(1 / 16f);
          }
        });
      }
    }).start();

    new Thread(new Runnable() {
      @Override
      public void run() {
        Riiablo.assets.finishLoadingAsset(sceneL1Descriptor);
        Riiablo.assets.finishLoadingAsset(sceneL2Descriptor);
        Riiablo.assets.finishLoadingAsset(sceneRDescriptor);
        sceneL1 = Riiablo.assets.get(sceneL1Descriptor);
        sceneL2 = Riiablo.assets.get(sceneL2Descriptor);
        sceneR  = Riiablo.assets.get(sceneRDescriptor);
        sceneL1.setFrameDuration(1 / 16f);
        sceneL2.setFrameDuration(1 / 16f);
        sceneR.setFrameDuration(1 / 16f);
      }
    }).start();
    */

    tfCharName = new TextField("", new TextField.TextFieldStyle() {{
      Riiablo.assets.finishLoadingAsset(textboxDescriptor);
      DC6 pages = Riiablo.assets.get(textboxDescriptor);
      background = new TextureRegionDrawable(pages.getTexture(0)) {{
        final float padding = 4;
        setLeftWidth(padding);
        setTopHeight(padding);
        setRightWidth(padding);
        setBottomHeight(padding);
      }};
      font = Riiablo.fonts.fontformal12;
      fontColor = Riiablo.colors.white;
      cursor = new TextureRegionDrawable(Riiablo.textures.white);
    }});
    tfCharName.setOnlyFontChars(true);
    tfCharName.setTextFieldListener(new com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener() {
      @Override
      public void keyTyped(com.badlogic.gdx.scenes.scene2d.ui.TextField textField, char c) {
        btnOK.setVisible(!textField.getText().isEmpty());
      }
    });

    charOptions = new Table() {{
      final float SPACING = 2;
      add(new Label(5125, Riiablo.fonts.font16, Riiablo.colors.gold)).space(SPACING).left().row();
      add(tfCharName).space(SPACING).row();
      pack();
    }};
    //charOptions.setPosition(stage.getWidth() / 2, 20, Align.bottom | Align.center);
    charOptions.setVisible(false);

    final TextButton.TextButtonStyle style = new TextButton.TextButtonStyle() {{
        Riiablo.assets.finishLoadingAsset(MediumSelButtonBlankDescriptor);
      DC6 pages = Riiablo.assets.get(MediumSelButtonBlankDescriptor);
        up   = new TextureRegionDrawable(pages.getTexture(0));
        down = new TextureRegionDrawable(pages.getTexture(1));
        font = Riiablo.fonts.fontexocet10;
    }};
    ChangeListener clickListener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        //Actor actor = event.getListenerActor();
        if (actor == btnExit) {
          Riiablo.client.popScreen();
        } else if (actor == btnOK) {
          if (selected == null) return;
          Riiablo.client.clearAndSet(new LoadingScreen(new GameScreen(Riiablo.charData.createD2S(tfCharName.getText(), selected.charClass))));
        }
      }
    };
    btnExit = new TextButton(5101, style);
    btnExit.addListener(clickListener);
    btnExit.setPosition(20, 20, Align.bottomLeft);
    btnOK = new TextButton(5102, style);
    btnOK.addListener(clickListener);
    btnOK.setPosition(stage.getWidth() - 20, 20, Align.bottomRight);
    btnOK.setVisible(false);

    selectedName = new Label(5127, Riiablo.fonts.font42);
    selectedName.setPosition(stage.getWidth() / 2, stage.getHeight() - 10, Align.top | Align.center);
    selectedName.setVisible(true);
    selectedDescription = new Label("", Riiablo.fonts.font16);
    selectedDescription.setVisible(false);
    selected = null;
    ClickListener selectedListener = new ClickListener(Input.Buttons.LEFT) {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        CharacterCreateButton newSelected = (CharacterCreateButton) event.getListenerActor();
        if (newSelected.getState() == CharacterCreateButton.State.BW) {
          selectedName.setVisible(false);
          selectedDescription.setVisible(false);
          btnOK.setVisible(false);
          charOptions.setVisible(false);
          selected = null;
          return;
        }

        if (selected != null && selected.isSelected()) {
          selected.deselect();
        }

        selected = newSelected;
        selectedName.setVisible(true);
        selectedName.setText(Riiablo.string.lookup(selected.charClass.name));
        selectedName.setSize(selectedName.getPrefWidth(), selectedName.getPrefHeight());
        selectedName.setPosition(stage.getWidth() / 2, stage.getHeight() - 10, Align.top | Align.center);
        selectedDescription.setText(Riiablo.string.lookup(selected.charClass.description));
        selectedDescription.setWidth(300);
        selectedDescription.setAlignment(Align.center);
        selectedDescription.setWrap(true);
        selectedDescription.setHeight(selectedDescription.getPrefHeight());
        selectedDescription.setPosition(stage.getWidth() / 2, stage.getHeight() - selectedName.getHeight() - 10, Align.top | Align.center);
        selectedDescription.setVisible(true);
        btnOK.setVisible(!tfCharName.getText().isEmpty());
        charOptions.setVisible(true);
        stage.setKeyboardFocus(tfCharName);
        fixActorOrder();
      }
    };

    int x = (int) stage.getWidth() / 2 - 11;
    int y = (int) (stage.getHeight() / 2) - (CharacterCreate.getRegionHeight() / 2) + 265;//225;
    amazon = new CharacterCreateButton(CharacterClass.AMAZON);
    amazon.setPosition(x - 210, y - 20);
    amazon.addListener(selectedListener);

    assassin = new CharacterCreateButton(CharacterClass.ASSASSIN);
    assassin.setPosition(x - 120, y - 25);
    assassin.addListener(selectedListener);

    necromancer = new CharacterCreateButton(CharacterClass.NECROMANCER);
    necromancer.setPosition(x - 80, y);
    necromancer.addListener(selectedListener);

    barbarian = new CharacterCreateButton(CharacterClass.BARBARIAN);
    barbarian.setPosition(x, y + 5);
    barbarian.addListener(selectedListener);

    paladin = new CharacterCreateButton(CharacterClass.PALADIN);
    paladin.setPosition(x + 195, y - 25);
    paladin.addListener(selectedListener);

    sorceress = new CharacterCreateButton(CharacterClass.SORCERESS);
    sorceress.setPosition(x + 115, y - 5);
    sorceress.addListener(selectedListener);

    druid = new CharacterCreateButton(CharacterClass.DRUID);
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

    stage.addActor(barbarian);
    stage.addActor(necromancer);
    stage.addActor(sorceress);
    stage.addActor(assassin);
    stage.addActor(paladin);
    stage.addActor(amazon);
    stage.addActor(druid);
    stage.addActor(fireWrapper);
    stage.addActor(selectedName);
    stage.addActor(selectedDescription);
    stage.addActor(btnExit);
    stage.addActor(btnOK);
    stage.addActor(charOptions);
    fixActorOrder();

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
    Riiablo.input.addProcessor(inputProcessor);
    Riiablo.client.addScreenBoundsListener(screenBoundsListener = new Client.ScreenBoundsListener() {
      @Override
      public void updateScreenBounds(float x, float y, float width, float height) {
        System.out.println("y = " + y);
        charOptions.setPosition(stage.getWidth() / 2, y + 20, Align.bottom | Align.center);
      }
    });
    screenBoundsListener.updateScreenBounds(0, 0, 0, 0);
  }

  private void fixActorOrder() {
    selectedName.toFront();
    selectedDescription.toFront();
    fireWrapper.toFront();
    btnExit.toFront();
    btnOK.toFront();
    charOptions.toFront();
  }

  @Override
  public void hide() {
    Riiablo.input.removeProcessor(inputProcessor);
    Riiablo.client.removeScreenBoundsListener(screenBoundsListener);
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
    Riiablo.assets.unload(CharacterCreateDescriptor.fileName);
    Riiablo.assets.unload(fireDescriptor.fileName);
    /*
    Riiablo.assets.unload(groundLDescriptor.fileName);
    Riiablo.assets.unload(groundRDescriptor.fileName);
    Riiablo.assets.unload(sceneL1Descriptor.fileName);
    Riiablo.assets.unload(sceneL2Descriptor.fileName);
    Riiablo.assets.unload(sceneRDescriptor.fileName);
    */
    Riiablo.assets.unload(MediumSelButtonBlankDescriptor.fileName);
    Riiablo.assets.unload(buttonDescriptor.fileName);
  }

  @Override
  public void render(float delta) {
    PaletteIndexedBatch b = Riiablo.batch;
    b.begin(Riiablo.palettes.fechar);

    int x = (int) stage.getWidth() / 2;
    int y = (int) stage.getHeight() / 2;
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
      sceneR.draw(b, Riiablo.VIRTUAL_WIDTH - 113, Riiablo.VIRTUAL_HEIGHT - 179);
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

    /*
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
    */
  }
}
