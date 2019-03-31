package com.riiablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.BodyLocs;
import com.riiablo.codec.excel.Inventory;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.loader.DC6Loader;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.Button;

import org.apache.commons.lang3.ArrayUtils;

public class HirelingPanel extends WidgetGroup implements Disposable {
  private static final String TAG = "HirelingPanel";

  final AssetDescriptor<DC6> NpcInvDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\NpcInv.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion NpcInv;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class);
  Button btnExit;

  final AssetDescriptor<DC6> inv_armorDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_armor.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_helm_gloveDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_helm_glove.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_weaponsDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_weapons.DC6", DC6.class);
  DC6 inv_armor, inv_helm_glove, inv_weapons;

  final GameScreen gameScreen;
  final Inventory.Entry inventory;

  final Texture fill;
  final Color backgroundColorG;
  final Color backgroundColorR;

  public HirelingPanel(final GameScreen gameScreen) {
    this.gameScreen = gameScreen;

    Riiablo.assets.load(NpcInvDescriptor);
    Riiablo.assets.finishLoadingAsset(NpcInvDescriptor);
    NpcInv = Riiablo.assets.get(NpcInvDescriptor).getTexture();
    setSize(NpcInv.getRegionWidth(), NpcInv.getRegionHeight());
    setTouchable(Touchable.enabled);
    setVisible(false);

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
    btnExit.setPosition(272, 15);
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    addActor(btnExit);

    inventory = Riiablo.files.inventory.get("Hireling");

    Riiablo.assets.load(inv_armorDescriptor);
    Riiablo.assets.load(inv_helm_gloveDescriptor);
    Riiablo.assets.load(inv_weaponsDescriptor);
    Riiablo.assets.finishLoadingAsset(inv_armorDescriptor);
    Riiablo.assets.finishLoadingAsset(inv_helm_gloveDescriptor);
    Riiablo.assets.finishLoadingAsset(inv_weaponsDescriptor);

    inv_armor = Riiablo.assets.get(inv_armorDescriptor);
    inv_helm_glove = Riiablo.assets.get(inv_helm_gloveDescriptor);
    inv_weapons = Riiablo.assets.get(inv_weaponsDescriptor);

    final BodyPart[] bodyParts = new BodyPart[BodyLocs.NUM_LOCS];

    BodyPart torso = bodyParts[BodyLocs.TORS] = new BodyPart(BodyLoc.TORS, inv_armor.getTexture());
    torso.setSize(inventory.torsoWidth, inventory.torsoHeight);
    torso.setPosition(
        inventory.torsoLeft - inventory.invLeft,
        getHeight() - inventory.torsoBottom);
    torso.yOffs = 2;
    addActor(torso);

    BodyPart rArm = bodyParts[BodyLocs.RARM] = new BodyPart(BodyLoc.RARM, inv_weapons.getTexture());
    rArm.setSize(inventory.rArmWidth, inventory.rArmHeight);
    rArm.setPosition(
        inventory.rArmLeft - inventory.invLeft,
        getHeight() - inventory.rArmBottom);
    rArm.xOffs = -2;
    rArm.yOffs = 2;
    addActor(rArm);

    BodyPart lArm = bodyParts[BodyLocs.LARM] = new BodyPart(BodyLoc.LARM, inv_weapons.getTexture());
    lArm.setSize(inventory.lArmWidth, inventory.lArmHeight);
    lArm.setPosition(
        inventory.lArmLeft - inventory.invLeft,
        getHeight() - inventory.lArmBottom);
    lArm.xOffs = -2;
    lArm.yOffs = 2;
    addActor(lArm);

    BodyPart head = bodyParts[BodyLocs.HEAD] = new BodyPart(BodyLoc.HEAD, inv_helm_glove.getTexture(1));
    head.setSize(inventory.headWidth, inventory.headHeight);
    head.setPosition(
        inventory.headLeft - inventory.invLeft,
        getHeight() - inventory.headBottom);
    head.xOffs = -2;
    head.yOffs = 1;
    addActor(head);

    for (int i = BodyLocs.HEAD; i < BodyLocs.NUM_LOCS; i++) {
      if (bodyParts[i] == null) continue;
      bodyParts[i].slot = i;
      //bodyParts[i].item = gameScreen.player.getSlot(BodyLoc.valueOf(i));
      bodyParts[i].setBodyPart(Riiablo.files.bodylocs.get(i).Code);
    }

    //setDebug(true, true);
  }

  @Override
  public void dispose() {
    btnExit.dispose();
    Riiablo.assets.unload(NpcInvDescriptor.fileName);
    Riiablo.assets.unload(buysellbtnDescriptor.fileName);
    Riiablo.assets.unload(inv_armorDescriptor.fileName);
    Riiablo.assets.unload(inv_helm_gloveDescriptor.fileName);
    Riiablo.assets.unload(inv_weaponsDescriptor.fileName);
  }

  @Override
  public void draw(Batch batch, float a) {
    batch.draw(NpcInv, getX(), getY());
    super.draw(batch, a);
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
              //Riiablo.audio.play(gameScreen.player.stats.getCharClass().name().toLowerCase() + "_impossible_1", false);
              return;
            }

            Riiablo.audio.play(cursor.getDropSound(), true);
            Riiablo.cursor.setItem(item);
            item = cursor;
            //gameScreen.player.setSlot(HirelingPanel.BodyPart.this.bodyLoc, item);
          } else {
            Riiablo.cursor.setItem(item);
            item = null;
            //gameScreen.player.setSlot(HirelingPanel.BodyPart.this.bodyLoc, null);
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
      x = xOffs;
      y = yOffs;

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
      if (item != null) {
        BBox box = item.invFile.getBox();
        item.setPosition(
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
        gameScreen.setDetails(item.details, item, HirelingPanel.this, this);
      }
    }
  }
}
