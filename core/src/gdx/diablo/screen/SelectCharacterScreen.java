package gdx.diablo.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import gdx.diablo.Diablo;
import gdx.diablo.codec.D2S;
import gdx.diablo.codec.DC6;
import gdx.diablo.entity.Player;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.widget.SelectButton;
import gdx.diablo.widget.TextButton;

public class SelectCharacterScreen extends ScreenAdapter {
  private static final String TAG = "SelectCharacterScreen";

  final AssetDescriptor<DC6> characterselectscreenEXPDescriptor = new AssetDescriptor<>("data\\global\\ui\\CharSelect\\charselectbckg.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion characterselectscreenEXP;

  final AssetDescriptor<DC6> MediumButtonBlankDescriptor = new AssetDescriptor<>("data\\global\\ui\\FrontEnd\\MediumButtonBlank.dc6", DC6.class);
  final AssetDescriptor<DC6> TallButtonBlankDescriptor = new AssetDescriptor<>("data\\global\\ui\\CharSelect\\TallButtonBlank.dc6", DC6.class);

  private Stage stage;
  private Button btnExit;
  private Button btnOK;
  private Button btnCreateNewCharacter;

  private SelectButton selected;
  private Array<SelectButton> characters;

  public SelectCharacterScreen() {
    SelectButton.load();
    Diablo.assets.load(characterselectscreenEXPDescriptor);
    Diablo.assets.load(MediumButtonBlankDescriptor);
    Diablo.assets.load(TallButtonBlankDescriptor);
    stage = new Stage(Diablo.viewport, Diablo.batch);
  }

  @Override
  public void show() {
    Diablo.assets.finishLoadingAsset(characterselectscreenEXPDescriptor);
    characterselectscreenEXP = Diablo.assets.get(characterselectscreenEXPDescriptor).getTexture();

    ChangeListener clickListener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (actor == btnExit) {
          Diablo.client.popScreen();
        } else if (actor == btnOK) {
          assert selected != null;
          GameScreen game = new GameScreen(new Player(selected.getD2S()));
          Diablo.client.clearAndSet(new LoadingScreen(game));
        } else if (actor == btnCreateNewCharacter) {
          Diablo.client.pushScreen(new CreateCharacterScreen());
        }
      }
    };
    TextButton.TextButtonStyle tallButtonStyle = new TextButton.TextButtonStyle() {{
      Diablo.assets.finishLoadingAsset(TallButtonBlankDescriptor);
      DC6 pages = Diablo.assets.get(TallButtonBlankDescriptor);
      up   = new TextureRegionDrawable(pages.getTexture(0));
      down = new TextureRegionDrawable(pages.getTexture(1));
      font = Diablo.fonts.fontexocet10;
    }};
    btnCreateNewCharacter = new TextButton(5273, tallButtonStyle);
    btnCreateNewCharacter.addListener(clickListener);
    btnCreateNewCharacter.setPosition(Diablo.VIRTUAL_WIDTH - btnCreateNewCharacter.getWidth() - 100, 100);

    TextButton.TextButtonStyle mediumButtonStyle = new TextButton.TextButtonStyle() {{
      Diablo.assets.finishLoadingAsset(MediumButtonBlankDescriptor);
      DC6 pages = Diablo.assets.get(MediumButtonBlankDescriptor);
      up   = new TextureRegionDrawable(pages.getTexture(0));
      down = new TextureRegionDrawable(pages.getTexture(1));
      font = Diablo.fonts.fontexocet10;
    }};
    btnExit = new TextButton(5101, mediumButtonStyle);
    btnExit.addListener(clickListener);
    btnExit.setPosition(20, 20);
    btnOK = new TextButton(5102, mediumButtonStyle);
    btnOK.addListener(clickListener);
    btnOK.setPosition(Diablo.VIRTUAL_WIDTH - 20 - btnOK.getWidth(), 20);
    //btnOK.setVisible(false);
    btnOK.setDisabled(true);

    FileHandle savesLocation = Diablo.home.child("Save");
    FileHandle[] saves = savesLocation.list(D2S.EXT);
    characters = new Array<>();
    for (FileHandle save : saves) {
      Gdx.app.debug(TAG, "Loading " + save.toString());
      D2S d2s = D2S.loadFromFile(save);
      SelectButton button = new SelectButton(d2s);
      button.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          if (getTapCount() >= 2) {
            assert selected == event.getListenerActor();
            btnOK.toggle();
            return;
          }

          if (selected != null) selected.deselect();
          selected = (SelectButton) event.getListenerActor();
          selected.select();
        }
      });
      characters.add(button);
      if (selected == null) {
        selected = button;
        selected.select();
        btnOK.setDisabled(false);
      }
    }

    for (int i = 0, x, y = Diablo.VIRTUAL_HEIGHT - 64 - SelectButton.HEIGHT; i < characters.size; i++) {
      x = (i & 1) == 0 ? 64 : 64 + SelectButton.WIDTH;
      SelectButton character = characters.get(i);
      character.setPosition(x, y);
      stage.addActor(character);
      if ((i & 1) == 1) y -= SelectButton.HEIGHT;
    }

    stage.addActor(btnExit);
    stage.addActor(btnOK);
    stage.addActor(btnCreateNewCharacter);
    Diablo.input.addProcessor(stage);
  }

  @Override
  public void hide() {
    Diablo.input.removeProcessor(stage);
  }

  @Override
  public void dispose() {
    SelectButton.unload();
    for (SelectButton selectButton : characters) if (selected != selectButton) selectButton.dispose();
    Diablo.assets.unload(characterselectscreenEXPDescriptor.fileName);
    Diablo.assets.unload(MediumButtonBlankDescriptor.fileName);
    Diablo.assets.unload(TallButtonBlankDescriptor.fileName);
  }

  @Override
  public void render(float delta) {
    PaletteIndexedBatch b = Diablo.batch;
    b.begin(Diablo.palettes.units);
    int x = Diablo.VIRTUAL_WIDTH_CENTER;
    int y = Diablo.VIRTUAL_HEIGHT_CENTER;
    int xOff = (characterselectscreenEXP.getRegionWidth()  / 2);
    int yOff = (characterselectscreenEXP.getRegionHeight() / 2);
    b.draw(characterselectscreenEXP, x - xOff, Diablo.VIRTUAL_HEIGHT - characterselectscreenEXP.getRegionHeight());
    b.end();

    stage.act(delta);
    stage.draw();
  }
}
