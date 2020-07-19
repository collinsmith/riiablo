package com.riiablo.screen.panel;

import org.apache.commons.lang3.ArrayUtils;

import com.artemis.annotations.Wire;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;

import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.BodyLocs;
import com.riiablo.codec.excel.Inventory;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.Stat;
import com.riiablo.item.StoreLoc;
import com.riiablo.loader.DC6Loader;
import com.riiablo.save.ItemController;
import com.riiablo.save.ItemData;
import com.riiablo.widget.Button;
import com.riiablo.widget.Label;

public class InventoryPanel extends WidgetGroup implements Disposable, ItemGrid.GridListener {
  private static final String TAG = "InventoryPanel";

  final AssetDescriptor<DC6> invcharDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\invchar6.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion invchar;

  final AssetDescriptor<DC6> invcharTabDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\invchar6Tab.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion invcharTabR;
  TextureRegion invcharTabL;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class);
  Button btnExit;

  final AssetDescriptor<DC6> goldcoinbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\goldcoinbtn.dc6", DC6.class);
  Button btnDropGold;

  final AssetDescriptor<DC6> inv_armorDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_armor.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_beltDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_belt.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_bootsDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_boots.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_helm_gloveDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_helm_glove.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_ring_amuletDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_ring_amulet.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_weaponsDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_weapons.DC6", DC6.class);
  DC6 inv_armor, inv_belt, inv_boots, inv_helm_glove, inv_ring_amulet, inv_weapons;

  final Inventory.Entry inventory;

  final Texture fill;
  final Color backgroundColorG;
  final Color backgroundColorR;

  @Wire(name = "itemController")
  protected ItemController itemController;

  public InventoryPanel() {
    Riiablo.assets.load(invcharDescriptor);
    Riiablo.assets.finishLoadingAsset(invcharDescriptor);
    invchar = Riiablo.assets.get(invcharDescriptor).getTexture(1);
    setSize(invchar.getRegionWidth(), invchar.getRegionHeight());
    setTouchable(Touchable.enabled);
    setVisible(false);

    Riiablo.assets.load(invcharTabDescriptor);
    Riiablo.assets.finishLoadingAsset(invcharTabDescriptor);
    invcharTabR = Riiablo.assets.get(invcharTabDescriptor).getTexture(0);
    invcharTabL = Riiablo.assets.get(invcharTabDescriptor).getTexture(1);

    Pixmap solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(0.0f, 0.0f, 0.0f, 1.0f);
    solidColorPixmap.fill();
    fill = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();

    backgroundColorR = Riiablo.colors.invRed;
    backgroundColorG = Riiablo.colors.invGreen;

    btnExit = new Button(new Button.ButtonStyle() {{
      Riiablo.assets.load(buysellbtnDescriptor);
      Riiablo.assets.finishLoadingAsset(buysellbtnDescriptor);
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(11));
    }});
    btnExit.setPosition(18, 15);
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    addActor(btnExit);

    inventory = Riiablo.files.inventory.getClass(Riiablo.charData.charClass);

    Riiablo.assets.load(inv_armorDescriptor);
    Riiablo.assets.load(inv_beltDescriptor);
    Riiablo.assets.load(inv_bootsDescriptor);
    Riiablo.assets.load(inv_helm_gloveDescriptor);
    Riiablo.assets.load(inv_ring_amuletDescriptor);
    Riiablo.assets.load(inv_weaponsDescriptor);
    Riiablo.assets.finishLoadingAsset(inv_armorDescriptor);
    Riiablo.assets.finishLoadingAsset(inv_beltDescriptor);
    Riiablo.assets.finishLoadingAsset(inv_bootsDescriptor);
    Riiablo.assets.finishLoadingAsset(inv_helm_gloveDescriptor);
    Riiablo.assets.finishLoadingAsset(inv_ring_amuletDescriptor);
    Riiablo.assets.finishLoadingAsset(inv_weaponsDescriptor);

    inv_armor = Riiablo.assets.get(inv_armorDescriptor);
    inv_belt = Riiablo.assets.get(inv_beltDescriptor);
    inv_boots = Riiablo.assets.get(inv_bootsDescriptor);
    inv_helm_glove = Riiablo.assets.get(inv_helm_gloveDescriptor);
    inv_ring_amulet = Riiablo.assets.get(inv_ring_amuletDescriptor);
    inv_weapons = Riiablo.assets.get(inv_weaponsDescriptor);

    final BodyPart[] bodyParts = new BodyPart[BodyLocs.NUM_LOCS];

    BodyPart torso = bodyParts[BodyLocs.TORS] = new BodyPart(BodyLoc.TORS, inv_armor.getTexture());
    torso.setSize(inventory.torsoWidth, inventory.torsoHeight);
    torso.setPosition(
        inventory.torsoLeft - inventory.invLeft,
        getHeight() - inventory.torsoBottom);
    torso.xOffs = torso.yOffs = 2;
    addActor(torso);

    BodyPart rArm = bodyParts[BodyLocs.RARM] = new BodyPart(BodyLoc.RARM, inv_weapons.getTexture());
    rArm.setSize(inventory.rArmWidth, inventory.rArmHeight);
    rArm.setPosition(
        inventory.rArmLeft - inventory.invLeft,
        getHeight() - inventory.rArmBottom);
    rArm.yOffs = 2;
    rArm.xOffsAlt = 4;
    rArm.yOffsAlt = 4;
    addActor(rArm);

    BodyPart lArm = bodyParts[BodyLocs.LARM] = new BodyPart(BodyLoc.LARM, inv_weapons.getTexture());
    lArm.setSize(inventory.lArmWidth, inventory.lArmHeight);
    lArm.setPosition(
        inventory.lArmLeft - inventory.invLeft,
        getHeight() - inventory.lArmBottom);
    lArm.yOffs = 2;
    lArm.xOffsAlt = 4;
    lArm.yOffsAlt = 4;
    addActor(lArm);

    BodyPart head = bodyParts[BodyLocs.HEAD] = new BodyPart(BodyLoc.HEAD, inv_helm_glove.getTexture(1));
    head.setSize(inventory.headWidth, inventory.headHeight);
    head.setPosition(
        inventory.headLeft - inventory.invLeft,
        getHeight() - inventory.headBottom);
    head.yOffs = 1;
    addActor(head);

    BodyPart neck = bodyParts[BodyLocs.NECK] = new BodyPart(BodyLoc.NECK, inv_ring_amulet.getTexture(0));
    neck.setSize(inventory.neckWidth, inventory.neckHeight);
    neck.setPosition(
        inventory.neckLeft - inventory.invLeft,
        getHeight() - inventory.neckBottom);
    neck.xOffs = -1;
    neck.yOffs = 1;
    addActor(neck);

    BodyPart rHand = bodyParts[BodyLocs.RRIN] = new BodyPart(BodyLoc.RRIN, inv_ring_amulet.getTexture(1));
    rHand.setSize(inventory.rHandWidth, inventory.rHandHeight);
    rHand.setPosition(
        inventory.rHandLeft - inventory.invLeft,
        getHeight() - inventory.rHandBottom);
    rHand.xOffs = -1;
    rHand.yOffs = 1;
    addActor(rHand);

    BodyPart lHand = bodyParts[BodyLocs.LRIN] = new BodyPart(BodyLoc.LRIN, inv_ring_amulet.getTexture(1));
    lHand.setSize(inventory.lHandWidth, inventory.lHandHeight);
    lHand.setPosition(
        inventory.lHandLeft - inventory.invLeft,
        getHeight() - inventory.lHandBottom);
    lHand.xOffs = -1;
    lHand.yOffs = 1;
    addActor(lHand);

    BodyPart belt = bodyParts[BodyLocs.BELT] = new BodyPart(BodyLoc.BELT, inv_belt.getTexture());
    belt.setSize(inventory.beltWidth, inventory.beltHeight);
    belt.setPosition(
        inventory.beltLeft - inventory.invLeft,
        getHeight() - inventory.beltBottom);
    belt.yOffs = 2;
    addActor(belt);

    BodyPart feet = bodyParts[BodyLocs.FEET] = new BodyPart(BodyLoc.FEET, inv_boots.getTexture());
    feet.setSize(inventory.feetWidth, inventory.feetHeight);
    feet.setPosition(
        inventory.feetLeft - inventory.invLeft,
        getHeight() - inventory.feetBottom);
    feet.xOffs = -1;
    feet.yOffs = 2;
    addActor(feet);

    BodyPart gloves = bodyParts[BodyLocs.GLOV] = new BodyPart(BodyLoc.GLOV, inv_helm_glove.getTexture(0));
    gloves.setSize(inventory.glovesWidth, inventory.glovesHeight);
    gloves.setPosition(
        inventory.glovesLeft - inventory.invLeft,
        getHeight() - inventory.glovesBottom);
    gloves.xOffs = -1;
    gloves.yOffs = 2;
    addActor(gloves);

    final ItemData itemData = Riiablo.charData.getItems();
    for (int i = BodyLocs.HEAD; i < BodyLocs.NUM_LOCS; i++) {
      if (bodyParts[i] == null) continue;
      bodyParts[i].slot = i;
      bodyParts[i].item = itemData.getSlot(BodyLoc.valueOf(i));
      bodyParts[i].setBodyPart(Riiablo.files.bodylocs.get(i).Code);
    }

    Actor alternateWeaponsL = new Actor();
    alternateWeaponsL.setSize(invcharTabR.getRegionWidth(), 20); // width in file seems to be incorrectly stored as 135
    alternateWeaponsL.setPosition(
        inventory.lArmLeft - inventory.invLeft - 5,
        getHeight() - inventory.lArmBottom - 4 + invcharTabL.getRegionHeight() - alternateWeaponsL.getHeight());
    addActor(alternateWeaponsL);

    Actor alternateWeaponsR = new Actor();
    alternateWeaponsR.setSize(invcharTabR.getRegionWidth(), 20);
    alternateWeaponsR.setPosition(
        inventory.rArmLeft - inventory.invLeft - 5,
        getHeight() - inventory.rArmBottom - 4 + invcharTabR.getRegionHeight() - alternateWeaponsR.getHeight());
    addActor(alternateWeaponsR);

    EventListener swapListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        itemData.alternate();
      }
    };
    alternateWeaponsL.addListener(swapListener);
    alternateWeaponsR.addListener(swapListener);

    itemData.addAlternateListener(new ItemData.AlternateListener() {
//      @Override
//      public void onChanged(CharData client, BodyLoc bodyLoc, Item oldItem, Item item) {
//        System.out.println("slot = " + slot);
//        bodyParts[bodyLoc.ordinal()].item = item;
//        if (item != null) Riiablo.audio.play(item.base.dropsound, true);
//      }

      @Override
      public void onAlternated(ItemData items, int alternate, Item LH, Item RH) {
        bodyParts[BodyLocs.RARM].bodyLoc = BodyLoc.getAlternate(BodyLoc.RARM, alternate);
        bodyParts[BodyLocs.LARM].bodyLoc = BodyLoc.getAlternate(BodyLoc.LARM, alternate);
        bodyParts[BodyLocs.RARM].item = RH;
        bodyParts[BodyLocs.LARM].item = LH;
      }
    });

    ItemGrid grid = new ItemGrid(inventory, this);
    IntArray inventoryItems = itemData.getStore(StoreLoc.INVENTORY);
    Array<Item> items = itemData.toItemArray(inventoryItems);
    grid.populate(items);
    grid.setPosition(
        inventory.gridLeft - inventory.invLeft,
        getHeight() - inventory.gridTop - grid.getHeight());
    addActor(grid);

    Label invgold = new Label(Integer.toString(Riiablo.charData.getStats().get(Stat.gold).value()), Riiablo.fonts.font16);
    invgold.setSize(88, 16);
    invgold.setPosition(109, 24);
    addActor(invgold);

    btnDropGold = new Button(new Button.ButtonStyle() {{
      Riiablo.assets.load(goldcoinbtnDescriptor);
      Riiablo.assets.finishLoadingAsset(goldcoinbtnDescriptor);
      DC6 goldcoinbtn = Riiablo.assets.get(goldcoinbtnDescriptor);
      up   = new TextureRegionDrawable(goldcoinbtn.getTexture(0));
      down = new TextureRegionDrawable(goldcoinbtn.getTexture(1));
    }});
    btnDropGold.setPosition(84, 23);
    addActor(btnDropGold);

    //setDebug(true, true);
  }

  @Override
  public void dispose() {
    btnExit.dispose();
    Riiablo.assets.unload(invcharDescriptor.fileName);
    Riiablo.assets.unload(buysellbtnDescriptor.fileName);
    Riiablo.assets.unload(goldcoinbtnDescriptor.fileName);
    Riiablo.assets.unload(inv_armorDescriptor.fileName);
    Riiablo.assets.unload(inv_beltDescriptor.fileName);
    Riiablo.assets.unload(inv_bootsDescriptor.fileName);
    Riiablo.assets.unload(inv_helm_gloveDescriptor.fileName);
    Riiablo.assets.unload(inv_ring_amuletDescriptor.fileName);
    Riiablo.assets.unload(inv_weaponsDescriptor.fileName);
  }

  @Override
  public void draw(Batch batch, float a) {
    batch.draw(invchar, getX(), getY());
    if (Riiablo.charData.getItems().getAlternate() > 0) {
      batch.draw(invcharTabR,
          getX() + inventory.rArmLeft - inventory.invLeft - 5,
          getY() + getHeight() - inventory.rArmBottom - 4);
      batch.draw(invcharTabL,
          getX() + inventory.lArmLeft - inventory.invLeft - 5,
          getY() + getHeight() - inventory.lArmBottom - 4);
    }
    super.draw(batch, a);
  }

  @Override
  public void onDrop(int x, int y) {
    itemController.cursorToStore(StoreLoc.INVENTORY, x, y);
  }

  @Override
  public void onPickup(int i) {
    itemController.storeToCursor(i);
  }

  @Override
  public void onSwap(int i, int x, int y) {
    itemController.swapStoreItem(i, StoreLoc.INVENTORY, x, y);
  }

  private class BodyPart extends Actor {
    TextureRegion background;
    Item item;
    BodyLoc bodyLoc;
    String bodyPart;
    int slot;
    final ClickListener clickListener;
    int xOffs, yOffs;
    int xOffsAlt, yOffsAlt;

    BodyPart(BodyLoc bodyLoc, TextureRegion background) {
      this.bodyLoc = bodyLoc;
      this.background = background;
      addListener(clickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          Item cursor = Riiablo.cursor.getItem();
          if (cursor != null) {
            if (!ArrayUtils.contains(cursor.typeEntry.BodyLoc, bodyPart)) {
              Riiablo.audio.play(Riiablo.charData.classId.name().toLowerCase() + "_impossible_1", false);
              return;
            }

            Riiablo.audio.play(cursor.getDropSound(), true);
            if (item != null) {
              itemController.swapBodyItem(BodyPart.this.bodyLoc, false);
            } else {
              itemController.cursorToBody(BodyPart.this.bodyLoc, false);
            }
            item = cursor;
          } else {
            item = null;
            itemController.bodyToCursor(BodyPart.this.bodyLoc, false);
          }
        }
      });
      xOffs = yOffs = 0;
      xOffsAlt = Integer.MIN_VALUE;
      yOffsAlt = Integer.MIN_VALUE;
    }

    public void setBodyPart(String code) {
      bodyPart = code;
    }

    @Override
    public void draw(Batch batch, float a) {
      int x, y;
      if (Riiablo.charData.getItems().getAlternate() > 0) {
        x = xOffsAlt == Integer.MIN_VALUE ? xOffs : xOffsAlt;
        y = yOffsAlt == Integer.MIN_VALUE ? yOffs : yOffsAlt;
      } else {
        x = xOffs;
        y = yOffs;
      }

      if (item == null) {
        batch.draw(background, getX() + x, getY() + y);
      }

      boolean blocked = false;
      Item cursorItem = Riiablo.cursor.getItem();
      if (cursorItem != null) {
        blocked = !ArrayUtils.contains(cursorItem.typeEntry.BodyLoc, bodyPart);
      }

      // TODO: red if does not meet item requirements
      boolean isOver = clickListener.isOver();
      PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
      if (isOver && !blocked && (cursorItem != null || item != null)) {
        b.setBlendMode(BlendMode.SOLID, backgroundColorG);
        b.draw(fill, getX(), getY(), getWidth(), getHeight());
        b.resetBlendMode();
      }

      // FIXME: Alt images on weapons are slightly off by maybe a pixel or so (rounding?) -- backgrounds fine
      if (item != null && item.wrapper.checkLoaded()) {
        BBox box = item.wrapper.invFile.getBox();
        item.wrapper.setPosition(
            getX() + getWidth()  / 2 - box.width  / 2f + x,
            getY() + getHeight() / 2 - box.height / 2f + y);
        item.draw(b, 1);
      }

      if (isOver && blocked) {
        b.setBlendMode(BlendMode.SOLID, backgroundColorR);
        b.draw(fill, getX(), getY(), getWidth(), getHeight());
        b.resetBlendMode();
      }

      if (isOver && item != null && cursorItem == null) {
        Riiablo.game.setDetails(item.details(), item, InventoryPanel.this, this);
      }
    }
  }
}
