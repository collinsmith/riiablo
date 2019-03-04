package gdx.diablo.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

import gdx.diablo.BlendMode;
import gdx.diablo.Diablo;
import gdx.diablo.codec.excel.Inventory;
import gdx.diablo.codec.excel.ItemEntry;
import gdx.diablo.entity.Player;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.item.Item;
import gdx.diablo.screen.GameScreen;

public class ItemGrid extends Group {
  private static final String TAG = "ItemGrid";
  final int width;
  final int height;
  final int boxWidth;
  final int boxHeight;

  final Texture fill;
  final Color backgroundColorR;
  final Color backgroundColorG;
  final Color backgroundColorB;
  final Color backgroundColorW;

  final ClickListener clickListener;

  final ObjectSet<Actor> hits = new ObjectSet<>(8, 1);

  final GameScreen gameScreen;
  final Player player;

  boolean blocked = true;
  StoredItem swap = null;
  Vector2 coords = new Vector2();
  Vector2 grid = new Vector2();
  Vector2 itemSize = new Vector2();

  public ItemGrid(GameScreen gameScreen, Inventory.Entry inv) {
    this(gameScreen, inv.gridX, inv.gridY, inv.gridBoxWidth, inv.gridBoxHeight);
  }

  public ItemGrid(GameScreen gameScreen, int width, int height, int boxWidth, int boxHeight) {
    this.gameScreen = gameScreen;
    this.player = gameScreen.player;
    this.width = width;
    this.height = height;
    this.boxWidth = boxWidth;
    this.boxHeight = boxHeight;

    Pixmap solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(0.0f, 0.0f, 0.0f, 1.0f);
    solidColorPixmap.fill();
    fill = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();

    backgroundColorR = Color.RED.cpy();
    backgroundColorR.a = 0.20f;
    backgroundColorG = Color.GREEN.cpy();
    backgroundColorG.a = 0.20f;
    backgroundColorB = Color.BLUE.cpy();
    backgroundColorB.a = 0.20f;
    backgroundColorW = Color.WHITE.cpy();
    backgroundColorW.a = 0.20f;

    setTouchable(Touchable.enabled);
    setSize(width * boxWidth, height * boxHeight);
    addListener(clickListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (event.isHandled()) return;
        ItemGrid.this.mouseMoved();
        if (blocked) {
          Diablo.audio.play("sorceress_impossible_1", false);
          event.handle();
          return;
        }

        Item cursor = Diablo.cursor.getItem();
        if (cursor != null) {
          if (swap != null) {
            Diablo.cursor.setItem(swap.item);
            removeActor(swap);
            swap = null;
          } else {
            Diablo.cursor.setItem(null);
          }

          cursor.gridX = (byte) grid.x;
          cursor.gridY = (byte) grid.y;
          swap = addItem(cursor);
          event.handle();
        }
      }
    });
  }

  public void populate(Array<Item> items) {
    for (Item item : items) {
      addItem(item);
    }
  }

  private StoredItem addItem(Item item) {
    StoredItem store = new StoredItem(item);
    store.setPosition(item.gridX * boxWidth, getHeight() - item.gridY * boxHeight - store.getHeight());
    addActor(store);
    return store;
  }

  private void mouseMoved() {
    swap = null;
    blocked = true;
    hits.clear();
    Item cursor = Diablo.cursor.getItem();
    if (cursor != null && clickListener.isOver()) {
      coords = Diablo.cursor.getCoords();
      screenToLocalCoordinates(coords);
      float x = coords.x - (coords.x % boxWidth);
      float y = coords.y - (coords.y % boxHeight);
      float itemWidth = cursor.base.invwidth * boxWidth;
      float itemHeight = cursor.base.invheight * boxHeight;
      coords.x = coords.x % boxWidth;
      coords.y = coords.y % boxHeight;

      switch (cursor.base.invwidth) {
        case 1:  break;
        case 2:  if (coords.x < boxWidth / 2) x -= boxWidth; break;
        default: Gdx.app.error(TAG, "Unsupported invWidth for " + cursor.getName() + ": " + cursor.base.invwidth);
      }

      switch (cursor.base.invheight) {
        case 1:
          break;

        case 2:
          if (coords.y < boxHeight / 2) y -= boxHeight;
          break;

        case 3:
          y -= boxHeight;
          break;

        case 4:
          if (coords.y < boxHeight / 2)
            y -= (boxHeight * 2);
          else
            y -= boxHeight;
          break;

        default: Gdx.app.error(TAG, "Unsupported invHeight for " + cursor.getName() + ": " + cursor.base.invheight);
      }

      x = MathUtils.clamp(x, 0, getWidth()  - itemWidth);
      y = MathUtils.clamp(y, 0, getHeight() - itemHeight);
      coords.set(x, y);
      itemSize.set(itemWidth, itemHeight);

      grid.x = (int) x / boxWidth;
      grid.y = (int) (getHeight() - y - itemHeight) / boxHeight;

      hits.clear();
      float boxWidth50  = boxWidth  / 2f;
      float boxHeight50 = boxHeight / 2f;
      for (int ix = 0; ix < cursor.base.invwidth; ix++) {
        for (int iy = 0; iy < cursor.base.invheight; iy++) {
          float px = x + ix * boxWidth;
          float py = y + iy * boxHeight;
          Actor hit = hit(px + boxWidth50, py + boxHeight50, true);
          if (hit instanceof StoredItem) {
            hits.add(hit);
          }
        }
      }
    }

    switch (hits.size) {
      case 0:
        blocked = false;
        swap = null;
        break;

      case 1:
        blocked = false;
        swap = (StoredItem) hits.first();
        break;

      default:
        blocked = true;
        swap = null;
    }
  }

  @Override
  protected void drawChildren(Batch batch, float parentAlpha) {
    super.drawChildren(batch, parentAlpha);
    mouseMoved();
    if (Diablo.cursor.getItem() != null && clickListener.isOver()) {
      PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
      switch (hits.size) {
        case 0:
          b.setBlendMode(BlendMode.SOLID, backgroundColorG);
          b.draw(fill, coords.x, coords.y, itemSize.x, itemSize.y);
          break;

        case 1:
          b.setBlendMode(BlendMode.SOLID, backgroundColorW);
          b.draw(fill, swap.getX(), swap.getY(), swap.getWidth(), swap.getHeight());
          break;

        default:
          b.flush();
          clipBegin(coords.x, coords.y, itemSize.x, itemSize.y);
          for (Actor hit : hits) {
            b.setBlendMode(BlendMode.SOLID, backgroundColorR);
            b.draw(fill, hit.getX(), hit.getY(), hit.getWidth(), hit.getHeight());
          }
          b.flush();
          clipEnd();
      }
      b.resetBlendMode();
    }
  }

  class StoredItem extends Actor {
    final Item item;
    final ClickListener clickListener;

    StoredItem(final Item item) {
      this.item = item;

      ItemEntry entry = item.base;
      setSize(entry.invwidth * boxWidth, entry.invheight * boxHeight);
      addListener(clickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          if (Diablo.cursor.getItem() == null) {
            Diablo.cursor.setItem(StoredItem.this.item);
            removeActor(StoredItem.this);
            event.handle();
          } else {
            ItemGrid.this.clickListener.clicked(event, x, y);
          }
        }
      });
      addListener(new ClickListener(Input.Buttons.RIGHT) {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          ItemEntry entry = StoredItem.this.item.base;
          if (entry.useable) {
            Diablo.audio.play(entry.usesound, true);
          }/* else if (Diablo.cursor.getItem() == null) {
            String[] BodyLocs = item.type.BodyLoc;
            for (String BodyLoc : BodyLocs) {
              if (BodyLoc.isEmpty()) break;
              int loc = Diablo.files.bodylocs.index(BodyLoc);
              System.out.println("loc = " + loc);
              if (player.getSlot(loc) == null) {
                Diablo.cursor.setItem(item);
                removeActor(StoredItem.this);
                player.setSlot(loc, item);
                break;
              }
            }
          }*/
          event.handle();
        }
      });
    }

    @Override
    public void setPosition(float x, float y) {
      super.setPosition(x, y);
      item.setPosition(x, y);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
      b.setBlendMode(BlendMode.SOLID, clickListener.isOver() && Diablo.cursor.getItem() == null ? backgroundColorG : backgroundColorB);
      b.draw(fill, getX(), getY(), getWidth(), getHeight());
      b.resetBlendMode();
      item.draw(b, 1);
      if (clickListener.isOver() && Diablo.cursor.getItem() == null) {
        gameScreen.setDetails(item.details, item, ItemGrid.this, item);
      }
    }
  }
}
