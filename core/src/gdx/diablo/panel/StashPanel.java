package gdx.diablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import gdx.diablo.Diablo;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.excel.Inventory;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.screen.GameScreen;
import gdx.diablo.widget.Button;
import gdx.diablo.widget.ItemGrid;

public class StashPanel extends WidgetGroup implements Disposable {
  private static final String TAG = "StashPanel";

  final AssetDescriptor<DC6> TradeStashDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\TradeStash.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion TradeStash;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class);
  Button btnExit;

  final GameScreen gameScreen;
  final Inventory.Entry inventory;

  public StashPanel(GameScreen gameScreen) {
    this.gameScreen = gameScreen;

    Diablo.assets.load(TradeStashDescriptor);
    Diablo.assets.finishLoadingAsset(TradeStashDescriptor);
    TradeStash = Diablo.assets.get(TradeStashDescriptor).getTexture();
    setSize(TradeStash.getRegionWidth(), TradeStash.getRegionHeight());
    setVisible(false);

    btnExit = new Button(new Button.ButtonStyle() {{
      Diablo.assets.load(buysellbtnDescriptor);
      Diablo.assets.finishLoadingAsset(buysellbtnDescriptor);
      up   = new TextureRegionDrawable(Diablo.assets.get(buysellbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Diablo.assets.get(buysellbtnDescriptor).getTexture(11));
    }});
    btnExit.setPosition(272, 15);
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    addActor(btnExit);

    inventory = Diablo.files.inventory.get("Big Bank Page 1");

    ItemGrid grid = new ItemGrid(gameScreen.player, inventory);
    //grid.populate(gameScreen.player.getStash());
    grid.setPosition(
        inventory.gridLeft - inventory.invLeft,
        getHeight() - inventory.gridTop - grid.getHeight());
    addActor(grid);

    setDebug(true, true);
  }

  @Override
  public void dispose() {
    btnExit.dispose();
    Diablo.assets.unload(TradeStashDescriptor.fileName);
    Diablo.assets.unload(buysellbtnDescriptor.fileName);
  }

  @Override
  public void draw(Batch batch, float a) {
    batch.draw(TradeStash, getX(), getY());
    super.draw(batch, a);
  }
}
