package com.riiablo.screen.panel;

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

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Inventory;
import com.riiablo.codec.excel.ItemEntry;
import com.riiablo.codec.excel.Misc;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.item.Item;
import com.riiablo.save.ItemData;

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

  final ObjectSet<Actor> hits = new ObjectSet<>(16, 0.99f); // 8, 1.0f no longer supported

  final ItemData itemData = Riiablo.charData.getItems();
  final GridListener gridListener;

  boolean showFill = true;
  boolean blocked = true;
  StoredItem swap = null;
  Vector2 coords = new Vector2();
  Vector2 grid = new Vector2();
  Vector2 itemSize = new Vector2();

  public ItemGrid(Inventory.Entry inv) {
    this(inv, null);
  }

  public ItemGrid(Inventory.Entry inv, GridListener gridListener) {
    this(inv.gridX, inv.gridY, inv.gridBoxWidth, inv.gridBoxHeight, gridListener);
  }

  public ItemGrid(int width, int height, int boxWidth, int boxHeight) {
    this(width, height, boxWidth, boxHeight, null);
  }

  public ItemGrid(int width, int height, int boxWidth, int boxHeight, GridListener gridListener) {
    this.width = width;
    this.height = height;
    this.boxWidth = boxWidth;
    this.boxHeight = boxHeight;
    this.gridListener = gridListener;

    Pixmap solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(0.0f, 0.0f, 0.0f, 1.0f);
    solidColorPixmap.fill();
    fill = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();

    backgroundColorR = Riiablo.colors.invRed;
    backgroundColorG = Riiablo.colors.invGreen;
    backgroundColorB = Riiablo.colors.invBlue;
    backgroundColorW = Riiablo.colors.invWhite;

    setTouchable(Touchable.enabled);
    setSize(width * boxWidth, height * boxHeight);
    addListener(clickListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (event.isHandled()) return;
        ItemGrid.this.mouseMoved();
        Item cursor = itemData.getCursor();
        if (blocked) {
          if (cursor != null) Riiablo.audio.play("sorceress_impossible_1", false);
          event.handle();
          return;
        }

        if (cursor != null) {
          Riiablo.audio.play(cursor.getDropSound(), true);
          if (swap != null) {
            onSwap(swap.item, (int) grid.x, (int) grid.y);
            removeActor(swap);
            swap = null;
          } else {
            onDrop((int) grid.x, (int) grid.y);
          }

          cursor.gridX = (byte) grid.x; // TODO: delete ? why was this note added
          cursor.gridY = (byte) grid.y; // TODO: delete ? why was this note added
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

  protected boolean accept(Item item) {
    return true;
  }

  void onDrop(int x, int y) {
    if (gridListener == null) return;
    gridListener.onDrop(x, y);
  }

  void onPickup(Item item) {
    if (gridListener == null) return;
    int i = itemData.indexOf(item);
    gridListener.onPickup(i);
  }

  void onSwap(Item item, int x, int y) {
    if (gridListener == null) return;
    int i = itemData.indexOf(item);
    gridListener.onSwap(i, x, y);
  }

  private void mouseMoved() {
    swap = null;
    blocked = true;
    hits.clear();
    Item cursor = itemData.getCursor();
    if (cursor != null && clickListener.isOver()) {
      coords = Riiablo.cursor.getCoords();
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
        blocked = !accept(cursor);
        swap = null;
        break;

      case 1:
        blocked = !accept(cursor);
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
    Item cursor = itemData.getCursor();
    if (cursor != null && clickListener.isOver() && showFill) {
      PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
      if (!accept(cursor)) {
        b.setBlendMode(BlendMode.SOLID, backgroundColorR);
        b.draw(fill, coords.x, coords.y, itemSize.x, itemSize.y);
      } else {
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
      }
      b.resetBlendMode();
    }
  }

  public interface GridListener {
    void onDrop(int x, int y);
    void onPickup(int i);
    void onSwap(int i, int x, int y);
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
          if (itemData.getCursor() == null) {
            onPickup(StoredItem.this.item);
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
            Riiablo.audio.play(StoredItem.this.item.getUseSound(), true);
            if (entry instanceof Misc.Entry) {
              Misc.Entry misc = StoredItem.this.item.getBase();
              switch (misc.pSpell) {
                case 7:
                  /**
                   * FIXME: custom logic is needed -- should display OVER stash panel, but also
                   *        close out when escaped (close stash and cube at once)
                   */
                  Riiablo.game.setLeftPanel(Riiablo.game.cubePanel);
                  break;
              }
            }
          }/* else if (Riiablo.cursor.getItem() == null) {
            String[] BodyLocs = item.type.BodyLoc;
            for (String BodyLoc : BodyLocs) {
              if (BodyLoc.isEmpty()) break;
              int loc = Riiablo.files.bodylocs.index(BodyLoc);
              System.out.println("loc = " + loc);
              if (player.getSlot(loc) == null) {
                Riiablo.cursor.setItem(item);
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
      b.setBlendMode(BlendMode.SOLID, clickListener.isOver() && itemData.getCursor() == null ? backgroundColorG : backgroundColorB);
      b.draw(fill, getX(), getY(), getWidth(), getHeight());
      b.resetBlendMode();
      item.draw(b, 1);
      if (clickListener.isOver() && itemData.getCursor() == null) {
        Riiablo.game.setDetails(item.details(), item, ItemGrid.this, item);
      }
    }
  }
}
