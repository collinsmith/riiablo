package com.riiablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import com.riiablo.codec.Animation;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.Index;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.item.Item;
import com.riiablo.item.Location;
import com.riiablo.save.ItemData;

/**
 * FIXME: Drawing cursor is proving troublesome. Either draw over in-game cursor or set hardware
 *        cursor to a pixmap. Pixmap is doable, but will require rasterizing an ARGB8888 version
 *        of the pixmap with all the needed transformations to color and some work to make the
 *        animations work in the default case with the hand. Other solution is to simply draw the
 *        cursor over the game and hide the hardware cursor entirely. This doesn't work very well
 *        when v-sync is enabled and is workable once v-sync is disabled and the foreground fps
 *        increased, outside of that, there is a noticeable lag to the hardware cursor position.
 *        --
 *        Long term I'd like to implement support for the pixmap method since it's more correct, but
 *        I think I'll stick with the other in the meantime because of its simplicity to implement,
 *        and because it will rely on existing code for changing item colors, etc.
 */
public class Cursor implements ItemData.LocationListener {
  private static final String TAG = "Cursor";
  private static final boolean DEBUG               = true;
  private static final boolean DEBUG_ITEM_BOUNDS   = DEBUG && !true;
  private static final boolean DEBUG_CURSOR_BOUNDS = DEBUG && !true;
  private static final boolean DEBUG_LISTENER      = DEBUG && !true;
  private static final boolean DEBUG_MOBILE        = DEBUG && !true;

  private com.badlogic.gdx.graphics.Cursor cursor;
  private Item item;
  private DC dc;
  private Index transform;
  private int transformColor;
  private Vector2 coords = new Vector2();

  private final AssetDescriptor<DC6> protateDescriptor = new AssetDescriptor<>("data\\global\\ui\\CURSOR\\protate.dc6", DC6.class);
  private DC6 protate;
  private Animation cursorAnim;

  public Cursor(AssetManager assets) {
    if (DEBUG_MOBILE || Gdx.app.getType() != Application.ApplicationType.Desktop) {
      cursorAnim = Animation.newAnimation();
    } else {
      assets.load(protateDescriptor);
      assets.finishLoadingAsset(protateDescriptor);
      protate = assets.get(protateDescriptor);

      cursorAnim = Animation.newAnimation().edit().layer(protate).build();
      cursorAnim.setFrameDuration(1 / 5f);

      Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
      Gdx.graphics.setCursor(cursor);
    }
  }

  public com.badlogic.gdx.graphics.Cursor getCursor() {
    return cursor;
  }

  public void setCursor(DC dc) {
    setCursor(dc, null, 0);
  }

  public void setCursor(DC dc, Index colormap, int id) {
    this.dc = dc;
    if (colormap != null) {
      transform = colormap;
      transformColor = id;
    } else {
      transform = null;
      transformColor = 0;
    }
  }

  public void resetCursor() {
    dc = null;
  }

  public Item getItem() {
    return Riiablo.charData.getItems().getCursor();
  }

  public void setItem(Item item) {
    if (this.item == null && item != null) {
      Riiablo.audio.play("item_pickup", true);
    }

    this.item = item;
    if (item != null) {
      setCursor(item.invFile, item.invColormap, item.invColorIndex);
    } else {
      resetCursor();
    }
  }

  public void act(float delta) {
    if (dc == null) cursorAnim.update(delta);
    if (dc == null && item != null && item.checkLoaded()) {
      setCursor(item.invFile, item.invColormap, item.invColorIndex);
    }
  }

  public void render(PaletteIndexedBatch batch) {
    if (dc == null) {
      coords.set(Gdx.input.getX(), Gdx.input.getY());
      Riiablo.extendViewport.unproject(coords);

      batch.begin();
      cursorAnim.draw(batch, coords.x, coords.y);
      batch.end();

      if (DEBUG_CURSOR_BOUNDS) {
        ShapeRenderer shapes = Riiablo.shapes;
        shapes.setProjectionMatrix(Riiablo.extendViewport.getCamera().combined);
        cursorAnim.drawDebug(shapes, coords.x, coords.y);
        shapes.end();
      }
    } else {
      BBox box = dc.getBox();
      coords.set(Gdx.input.getX(), Gdx.input.getY());
      Riiablo.extendViewport.unproject(coords);
      coords.sub(box.width / 2f, box.height / 2f);

      batch.begin();
      batch.setColormap(transform, transformColor);
      if (item.isEthereal()) batch.setAlpha(Item.ETHEREAL_ALPHA);
      batch.draw(dc.getTexture(), coords.x, coords.y);
      if (item.isEthereal()) batch.resetColor();
      batch.resetColormap();
      batch.end();

      if (DEBUG_ITEM_BOUNDS) {
        ShapeRenderer shapes = Riiablo.shapes;
        shapes.setProjectionMatrix(Riiablo.extendViewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.GREEN);
        shapes.rect(coords.x, coords.y, box.width, box.height);
        shapes.end();
      }
    }
  }

  public int getX() {
    return Gdx.input.getX();
  }

  public int getY() {
    return Gdx.input.getY();
  }

  public Vector2 getCoords() {
    return coords.set(getX(), getY());
  }

  @Override
  public void onChanged(ItemData items, Location oldLocation, Location location, Item item) {
    if (DEBUG_LISTENER) Gdx.app.log(TAG, "onChange " + oldLocation + "->" + location + " " + item);
    if (oldLocation == Location.CURSOR && location != Location.CURSOR) {
      setItem(null);
    } else if (location == Location.CURSOR) {
      setItem(item);
    }
  }
}
