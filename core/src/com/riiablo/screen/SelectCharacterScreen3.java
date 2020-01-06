package com.riiablo.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.riiablo.Riiablo;
import com.riiablo.save.D2S;
import com.riiablo.codec.DC6;
import com.riiablo.codec.StringTBL;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.DC6Loader;
import com.riiablo.widget.CharacterSelectButton;
import com.riiablo.widget.TextButton;

public class SelectCharacterScreen3 extends ScreenAdapter {
  private static final String TAG = "SelectCharacterScreen2";

  final AssetDescriptor<DC6> characterselectscreenEXPDescriptor = new AssetDescriptor<>("data\\global\\ui\\CharSelect\\charselectbckg.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion characterselectscreenEXP;

  final AssetDescriptor<DC6> MediumButtonBlankDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\MediumButtonBlank.dc6", DC6.class);
  final AssetDescriptor<DC6> TallButtonBlankDescriptor = new AssetDescriptor<>("data\\global\\ui\\CharSelect\\TallButtonBlank.dc6", DC6.class);

  private Stage stage;
  private Button btnExit;
  private Button btnOK;
  private Button btnCreateNewCharacter;
  private Button btnDeleteCharacter;

  private CharacterSelectButton        selected;
  private Array<CharacterSelectButton> characters;

  public SelectCharacterScreen3(final Socket socket) {
    load();

    stage = new Stage(Riiablo.viewport, Riiablo.batch);

    Riiablo.assets.finishLoadingAsset(characterselectscreenEXPDescriptor);
    characterselectscreenEXP = Riiablo.assets.get(characterselectscreenEXPDescriptor).getTexture();

    ChangeListener clickListener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (actor == btnExit) {
          Riiablo.client.popScreen();
        } else if (actor == btnOK) {
          assert selected != null;
          Riiablo.client.pushScreen(new NetworkedGameScreen(Riiablo.charData.setD2S(selected.getD2S()), socket));
        } else if (actor == btnCreateNewCharacter) {
          //Riiablo.client.pushScreen(new CreateCharacterScreen());
        }
      }
    };
    TextButton.TextButtonStyle tallButtonStyle = new TextButton.TextButtonStyle() {{
      Riiablo.assets.finishLoadingAsset(TallButtonBlankDescriptor);
      DC6 pages = Riiablo.assets.get(TallButtonBlankDescriptor);
      up   = disabled = new TextureRegionDrawable(pages.getTexture(0));
      down = new TextureRegionDrawable(pages.getTexture(1));
      disabled = up;
      font = Riiablo.fonts.fontexocet10;
    }};
    btnDeleteCharacter = new TextButton(StringTBL.EXPANSION_OFFSET + 2744, tallButtonStyle);
    btnDeleteCharacter.addListener(clickListener);
    btnDeleteCharacter.setDisabled(true);
    btnCreateNewCharacter = new TextButton(StringTBL.EXPANSION_OFFSET + 2743, tallButtonStyle);
    btnCreateNewCharacter.addListener(clickListener);
    Table panel = new Table() {{
      final float SPACING = 4;
      add(btnDeleteCharacter).space(SPACING);
      add(btnCreateNewCharacter).space(SPACING);
      pack();
    }};
    panel.setPosition(stage.getWidth() / 2, 20, Align.bottom | Align.center);
    stage.addActor(panel);

    TextButton.TextButtonStyle mediumButtonStyle = new TextButton.TextButtonStyle() {{
      Riiablo.assets.finishLoadingAsset(MediumButtonBlankDescriptor);
      DC6 pages = Riiablo.assets.get(MediumButtonBlankDescriptor);
      up   = disabled = new TextureRegionDrawable(pages.getTexture(0));
      down = new TextureRegionDrawable(pages.getTexture(1));
      font = Riiablo.fonts.fontexocet10;
    }};

    btnExit = new TextButton(5101, mediumButtonStyle);
    btnExit.addListener(clickListener);
    btnExit.setPosition(20, 20, Align.bottomLeft);
    stage.addActor(btnExit);

    btnOK = new TextButton(5102, mediumButtonStyle);
    btnOK.addListener(clickListener);
    btnOK.setPosition(stage.getWidth() - 20, 20, Align.bottomRight);
    btnOK.setDisabled(true);
    stage.addActor(btnOK);

    FileHandle savesLocation = Riiablo.home.child("Save");
    FileHandle[] saves = savesLocation.list(D2S.EXT);
    characters = new Array<>();
    for (FileHandle save : saves) {
      Gdx.app.debug(TAG, "Loading " + save.toString());
      D2S d2s = D2S.loadFromFile(save);
      CharacterSelectButton button = new CharacterSelectButton(d2s);
      button.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          if (getTapCount() >= 2) {
            assert selected == event.getListenerActor();
            btnOK.toggle();
            return;
          }

          if (selected != null) selected.deselect();
          selected = (CharacterSelectButton) event.getListenerActor();
          selected.select();
        }
      });
      characters.add(button);
      if (selected == null) {
        selected = button;
        selected.select();
        btnOK.setDisabled(false);
        //btnDeleteCharacter.setDisabled(false); // TODO
      }
    }

    final int offsetX = 32;
    final int offsetY = 32;
    for (int i = 0, x, y = (int) stage.getHeight() - offsetY - CharacterSelectButton.HEIGHT; i < characters.size; i++) {
      x = (i & 1) == 0 ? offsetX : offsetX + CharacterSelectButton.WIDTH;
      CharacterSelectButton character = characters.get(i);
      character.setPosition(x, y);
      stage.addActor(character);
      if ((i & 1) == 1) y -= CharacterSelectButton.HEIGHT;
    }
  }

  @Override
  public void show() {
    load();
    Riiablo.input.addProcessor(stage);
  }

  @Override
  public void hide() {
    Riiablo.input.removeProcessor(stage);
    dispose();
  }

  private void load() {
    CharacterSelectButton.loadBox();
    Riiablo.assets.load(characterselectscreenEXPDescriptor);
    Riiablo.assets.load(MediumButtonBlankDescriptor);
    Riiablo.assets.load(TallButtonBlankDescriptor);
  }

  @Override
  public void dispose() {
    CharacterSelectButton.unloadBox();
    for (CharacterSelectButton selectButton : characters) selectButton.dispose();
    Riiablo.assets.unload(characterselectscreenEXPDescriptor.fileName);
    Riiablo.assets.unload(MediumButtonBlankDescriptor.fileName);
    Riiablo.assets.unload(TallButtonBlankDescriptor.fileName);
  }

  @Override
  public void render(float delta) {
    PaletteIndexedBatch b = Riiablo.batch;
    b.begin(Riiablo.palettes.units);
    b.draw(characterselectscreenEXP, (stage.getWidth() / 2) - (characterselectscreenEXP.getRegionWidth() / 2), stage.getHeight() - characterselectscreenEXP.getRegionHeight());
    b.end();

    stage.act(delta);
    stage.draw();
  }
}
