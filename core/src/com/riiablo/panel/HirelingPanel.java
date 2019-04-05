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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
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
import com.riiablo.widget.Label;

import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;

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

    EnumMap<BodyLoc, Item> equipped = new EnumMap<>(BodyLoc.class);
    if (gameScreen.player.merc.items.items != null) {
      for (Item item : gameScreen.player.merc.items.items.items) {
        equipped.put(item.bodyLoc, item);
      }
    }
    for (int i = BodyLocs.HEAD; i < BodyLocs.NUM_LOCS; i++) {
      if (bodyParts[i] == null) continue;
      bodyParts[i].slot = i;
      bodyParts[i].item = equipped.get(BodyLoc.valueOf(i));
      bodyParts[i].setBodyPart(Riiablo.files.bodylocs.get(i).Code);
    }

    Label name = new Label("UNIMPLEMENTED", Riiablo.fonts.ReallyTheLastSucker);
    name.setSize(150, 16);
    name.setPosition(6, 216);
    name.setAlignment(Align.left);
    addActor(name);

    Table health = new Table();
    health.setSize(151, 16);
    health.setPosition(163, 216);
    health.add(Label.i18n("strchrlif", Riiablo.fonts.ReallyTheLastSucker));
    health.add(new Label(Integer.toString(0), Riiablo.fonts.font8, Align.center)).growX().row();
    addActor(health);

    Table exp = new Table();
    exp.setSize(120, 30);
    exp.setPosition(8, 177);
    exp.add(Label.i18n("strchrexp", Riiablo.fonts.ReallyTheLastSucker)).row();
    exp.add(new Label(Integer.toString(0), Riiablo.fonts.font8)).growY().row();
    addActor(exp);

    Table level = new Table();
    level.setSize(45, 30);
    level.setPosition(138, 177);
    level.add(Label.i18n("strchrlvl", Riiablo.fonts.ReallyTheLastSucker)).row();
    level.add(new Label(Integer.toString(0), Riiablo.fonts.font8)).growY().row();
    addActor(level);

    Table nextLevel = new Table();
    nextLevel.setSize(120, 30);
    nextLevel.setPosition(194, 177);
    nextLevel.add(Label.i18n("strchrnxtlvl", Riiablo.fonts.ReallyTheLastSucker)).row();
    nextLevel.add(new Label(Integer.toString(0), Riiablo.fonts.font8)).growY().row();
    addActor(nextLevel);

    Label strLabel = Label.i18n("strchrstr", Riiablo.fonts.ReallyTheLastSucker);
    strLabel.setSize(96, 16);
    strLabel.setPosition(8, 149);
    strLabel.setAlignment(Align.left);
    addActor(strLabel);

    Label str = new Label(Integer.toString(0), Riiablo.fonts.font8);
    str.setSize(46, 16);
    str.setPosition(109, 149);
    str.setAlignment(Align.right);
    addActor(str);

    Label dexLabel = Label.i18n("strchrdex", Riiablo.fonts.ReallyTheLastSucker);
    dexLabel.setSize(96, 16);
    dexLabel.setPosition(8, 125);
    strLabel.setAlignment(Align.left);
    addActor(dexLabel);

    Label dex = new Label(Integer.toString(0), Riiablo.fonts.font8);
    dex.setSize(46, 16);
    dex.setPosition(109, 125);
    dex.setAlignment(Align.right);
    addActor(dex);

    Label damLabel = Label.i18n("strchrskm", Riiablo.fonts.ReallyTheLastSucker);
    damLabel.setSize(96, 16);
    damLabel.setPosition(8, 101);
    strLabel.setAlignment(Align.left);
    addActor(damLabel);

    Label dam = new Label(Integer.toString(0), Riiablo.fonts.font8);
    dam.setSize(46, 16);
    dam.setPosition(109, 101);
    dam.setAlignment(Align.right);
    addActor(dam);

    Label defLabel = Label.i18n("strchrdef", Riiablo.fonts.ReallyTheLastSucker);
    defLabel.setSize(96, 16);
    defLabel.setPosition(8, 77);
    strLabel.setAlignment(Align.left);
    addActor(defLabel);

    Label def = new Label(Integer.toString(0), Riiablo.fonts.font8);
    def.setSize(46, 16);
    def.setPosition(109, 77);
    def.setAlignment(Align.right);
    addActor(def);

    Label fireResLabel = Label.i18n("strchrfir", Riiablo.fonts.ReallyTheLastSucker);
    fireResLabel.setSize(96, 16);
    fireResLabel.setPosition(165, 149);
    fireResLabel.setAlignment(Align.center);
    addActor(fireResLabel);

    Label fireRes = new Label(Integer.toString(0), Riiablo.fonts.font8);
    fireRes.setSize(46, 16);
    fireRes.setPosition(266, 149);
    fireRes.setAlignment(Align.right);
    addActor(fireRes);

    Label coldResLabel = Label.i18n("strchrcol", Riiablo.fonts.ReallyTheLastSucker);
    coldResLabel.setSize(96, 16);
    coldResLabel.setPosition(165, 125);
    coldResLabel.setAlignment(Align.center);
    addActor(coldResLabel);

    Label coldRes = new Label(Integer.toString(0), Riiablo.fonts.font8);
    coldRes.setSize(46, 16);
    coldRes.setPosition(266, 125);
    coldRes.setAlignment(Align.right);
    addActor(coldRes);

    Label lightResLabel = Label.i18n("strchrlit", Riiablo.fonts.ReallyTheLastSucker);
    lightResLabel.setSize(96, 16);
    lightResLabel.setPosition(165, 101);
    lightResLabel.setAlignment(Align.center);
    addActor(lightResLabel);

    Label lightRes = new Label(Integer.toString(0), Riiablo.fonts.font8);
    lightRes.setSize(46, 16);
    lightRes.setPosition(266, 101);
    lightRes.setAlignment(Align.right);
    addActor(lightRes);

    Label poisonResLabel = Label.i18n("strchrpos", Riiablo.fonts.ReallyTheLastSucker);
    poisonResLabel.setSize(96, 16);
    poisonResLabel.setPosition(165, 77);
    poisonResLabel.setAlignment(Align.center);
    addActor(poisonResLabel);

    Label poisonRes = new Label(Integer.toString(0), Riiablo.fonts.font8);
    poisonRes.setSize(46, 16);
    poisonRes.setPosition(266, 77);
    poisonRes.setAlignment(Align.right);
    addActor(poisonRes);

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
        gameScreen.setDetails(item.details(), item, HirelingPanel.this, this);
      }
    }
  }
}
