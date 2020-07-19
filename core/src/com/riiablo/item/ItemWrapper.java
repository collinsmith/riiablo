package com.riiablo.item;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.codec.Index;
import com.riiablo.codec.excel.Inventory;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.PaletteIndexedBatch;

public class ItemWrapper extends Actor implements Disposable {
  final Item item;

  AssetDescriptor<DC6> invFileDescriptor;
  public DC6   invFile;

  public Index invColormap;
  public int   invColorIndex;

  public Index charColormap;
  public int   charColorIndex;

  ItemWrapper(Item item) {
    this.item = item;
  }

  @Override
  public String getName() {
    return item.getNameString();
  }

  public void resize() {
    BBox box = invFile.getBox();
    setSize(box.width, box.height);
  }

  public void resize(Inventory.Entry inv) {
    setSize(item.base.invwidth * inv.gridBoxWidth, item.base.invheight * inv.gridBoxHeight);
  }

  public void load() {
    if (invFileDescriptor != null) return;
    invFileDescriptor = new AssetDescriptor<>("data\\global\\items\\" + item.getInvFileName() + '.' + DC6.EXT, DC6.class);
    Riiablo.assets.load(invFileDescriptor);
    checkLoaded();

    invColormap      = Riiablo.colormaps.get(item.base.InvTrans);
    String invColor  = item.getInvColor();
    invColorIndex    = invColor != null ? Riiablo.files.colors.index(invColor) + 1 : 0;

    charColormap     = Riiablo.colormaps.get(item.base.Transform);
    String charColor = item.getCharColor();
    charColorIndex   = charColor != null ? Riiablo.files.colors.index(charColor) + 1 : 0;

    // TODO: load images of socketed items
  }

  public boolean checkLoaded() {
    boolean b = Riiablo.assets.isLoaded(invFileDescriptor);
    if (b && invFile == null) {
      invFile = Riiablo.assets.get(invFileDescriptor);
      resize();
    }

    return b;
  }

  @Override
  public void draw(Batch batch, float a) {
    if (invFile == null && !checkLoaded()) return;
    PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
    boolean ethereal = item.isEthereal();
    if (ethereal) b.setAlpha(Item.ETHEREAL_ALPHA);
    if (invColormap != null) b.setColormap(invColormap, invColorIndex);
    invFile.draw(b, getX(), getY());
    if (invColormap != null) b.resetColormap();
    if (ethereal) b.resetColor();
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(invFileDescriptor.fileName);
  }
}
