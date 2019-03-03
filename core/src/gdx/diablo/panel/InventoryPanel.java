package gdx.diablo.panel;

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
import com.badlogic.gdx.utils.Disposable;

import org.apache.commons.lang3.ArrayUtils;

import gdx.diablo.BlendMode;
import gdx.diablo.Diablo;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.excel.BodyLocs;
import gdx.diablo.codec.excel.Inventory;
import gdx.diablo.codec.util.BBox;
import gdx.diablo.entity.Player;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.item.BodyLoc;
import gdx.diablo.item.Item;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.screen.GameScreen;
import gdx.diablo.widget.Button;
import gdx.diablo.widget.ItemGrid;

public class InventoryPanel extends WidgetGroup implements Disposable {
  private static final String TAG = "InventoryPanel";

  final AssetDescriptor<DC6> invcharDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\invchar6.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion invchar;

  final AssetDescriptor<DC6> invcharTabDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\invchar6Tab.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion invcharTabR;
  TextureRegion invcharTabL;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class);
  Button btnExit;

  final AssetDescriptor<DC6> inv_armorDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_armor.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_beltDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_belt.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_bootsDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_boots.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_helm_gloveDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_helm_glove.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_ring_amuletDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_ring_amulet.DC6", DC6.class);
  final AssetDescriptor<DC6> inv_weaponsDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\inv_weapons.DC6", DC6.class);
  DC6 inv_armor, inv_belt, inv_boots, inv_helm_glove, inv_ring_amulet, inv_weapons;

  final GameScreen gameScreen;
  final Inventory.Entry inventory;

  final Texture fill;
  final Color backgroundColorG;
  final Color backgroundColorR;

  public InventoryPanel(final GameScreen gameScreen) {
    this.gameScreen = gameScreen;

    Diablo.assets.load(invcharDescriptor);
    Diablo.assets.finishLoadingAsset(invcharDescriptor);
    invchar = Diablo.assets.get(invcharDescriptor).getTexture(1);
    setSize(invchar.getRegionWidth(), invchar.getRegionHeight());
    setTouchable(Touchable.enabled);
    setVisible(false);

    Diablo.assets.load(invcharTabDescriptor);
    Diablo.assets.finishLoadingAsset(invcharTabDescriptor);
    invcharTabR = Diablo.assets.get(invcharTabDescriptor).getTexture(0);
    invcharTabL = Diablo.assets.get(invcharTabDescriptor).getTexture(1);

    Pixmap solidColorPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    solidColorPixmap.setColor(0.0f, 0.0f, 0.0f, 1.0f);
    solidColorPixmap.fill();
    fill = new Texture(solidColorPixmap);
    solidColorPixmap.dispose();

    backgroundColorG = Color.GREEN.cpy();
    backgroundColorG.a = 0.25f;
    backgroundColorR = Color.RED.cpy();
    backgroundColorR.a = 0.25f;

    btnExit = new Button(new Button.ButtonStyle() {{
      Diablo.assets.load(buysellbtnDescriptor);
      Diablo.assets.finishLoadingAsset(buysellbtnDescriptor);
      up   = new TextureRegionDrawable(Diablo.assets.get(buysellbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Diablo.assets.get(buysellbtnDescriptor).getTexture(11));
    }});
    btnExit.setPosition(18, 15);
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    addActor(btnExit);

    inventory = Diablo.files.inventory.getClass(gameScreen.player.stats.getClassId());

    Diablo.assets.load(inv_armorDescriptor);
    Diablo.assets.load(inv_beltDescriptor);
    Diablo.assets.load(inv_bootsDescriptor);
    Diablo.assets.load(inv_helm_gloveDescriptor);
    Diablo.assets.load(inv_ring_amuletDescriptor);
    Diablo.assets.load(inv_weaponsDescriptor);
    Diablo.assets.finishLoadingAsset(inv_armorDescriptor);
    Diablo.assets.finishLoadingAsset(inv_beltDescriptor);
    Diablo.assets.finishLoadingAsset(inv_bootsDescriptor);
    Diablo.assets.finishLoadingAsset(inv_helm_gloveDescriptor);
    Diablo.assets.finishLoadingAsset(inv_ring_amuletDescriptor);
    Diablo.assets.finishLoadingAsset(inv_weaponsDescriptor);

    inv_armor = Diablo.assets.get(inv_armorDescriptor);
    inv_belt = Diablo.assets.get(inv_beltDescriptor);
    inv_boots = Diablo.assets.get(inv_bootsDescriptor);
    inv_helm_glove = Diablo.assets.get(inv_helm_gloveDescriptor);
    inv_ring_amulet = Diablo.assets.get(inv_ring_amuletDescriptor);
    inv_weapons = Diablo.assets.get(inv_weaponsDescriptor);

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
    rArm.yOffsAlt = 5;
    addActor(rArm);

    BodyPart lArm = bodyParts[BodyLocs.LARM] = new BodyPart(BodyLoc.LARM, inv_weapons.getTexture());
    lArm.setSize(inventory.lArmWidth, inventory.lArmHeight);
    lArm.setPosition(
        inventory.lArmLeft - inventory.invLeft,
        getHeight() - inventory.lArmBottom);
    lArm.yOffs = 2;
    lArm.xOffsAlt = 4;
    lArm.yOffsAlt = 5;
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
    addActor(neck);

    BodyPart rHand = bodyParts[BodyLocs.RRIN] = new BodyPart(BodyLoc.RRIN, inv_ring_amulet.getTexture(1));
    rHand.setSize(inventory.rHandWidth, inventory.rHandHeight);
    rHand.setPosition(
        inventory.rHandLeft - inventory.invLeft,
        getHeight() - inventory.rHandBottom);
    addActor(rHand);

    BodyPart lHand = bodyParts[BodyLocs.LRIN] = new BodyPart(BodyLoc.LRIN, inv_ring_amulet.getTexture(1));
    lHand.setSize(inventory.lHandWidth, inventory.lHandHeight);
    lHand.setPosition(
        inventory.lHandLeft - inventory.invLeft,
        getHeight() - inventory.lHandBottom);
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

    for (int i = BodyLocs.HEAD; i < BodyLocs.NUM_LOCS; i++) {
      if (bodyParts[i] == null) continue;
      bodyParts[i].slot = i;
      bodyParts[i].item = gameScreen.player.getSlot(BodyLoc.valueOf(i));
      bodyParts[i].setBodyPart(Diablo.files.bodylocs.get(i).Code);
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
        boolean alternate = !gameScreen.player.isAlternate();
        gameScreen.player.setAlternate(alternate);
      }
    };
    alternateWeaponsL.addListener(swapListener);
    alternateWeaponsR.addListener(swapListener);

    gameScreen.player.addSlotListener(new Player.SlotListener() {
      @Override
      public void onChanged(Player player, BodyLoc bodyLoc, Item oldItem, Item item) {
        //System.out.println("slot = " + slot);
        //bodyParts[bodyLoc.ordinal()].item = item;
        //if (item != null) Diablo.audio.play(item.base.dropsound, true);
      }

      @Override
      public void onAlternate(Player player, Item LH, Item RH) {
        bodyParts[BodyLocs.RARM].bodyLoc = player.isAlternate() ? BodyLoc.RARM2 : BodyLoc.RARM;
        bodyParts[BodyLocs.LARM].bodyLoc = player.isAlternate() ? BodyLoc.LARM2 : BodyLoc.LARM;
        bodyParts[BodyLocs.RARM].item = RH;
        bodyParts[BodyLocs.LARM].item = LH;
      }
    });

    ItemGrid grid = new ItemGrid(gameScreen.player, inventory);
    grid.populate(gameScreen.player.getInventory());
    grid.setPosition(
        inventory.gridLeft - inventory.invLeft,
        getHeight() - inventory.gridTop - grid.getHeight());
    addActor(grid);

    //setDebug(true, true);
  }

  @Override
  public void dispose() {
    btnExit.dispose();
    Diablo.assets.unload(invcharDescriptor.fileName);
    Diablo.assets.unload(buysellbtnDescriptor.fileName);
    Diablo.assets.unload(inv_armorDescriptor.fileName);
    Diablo.assets.unload(inv_beltDescriptor.fileName);
    Diablo.assets.unload(inv_bootsDescriptor.fileName);
    Diablo.assets.unload(inv_helm_gloveDescriptor.fileName);
    Diablo.assets.unload(inv_ring_amuletDescriptor.fileName);
    Diablo.assets.unload(inv_weaponsDescriptor.fileName);
  }

  @Override
  public void draw(Batch batch, float a) {
    batch.draw(invchar, getX(), getY());
    if (gameScreen.player.isAlternate()) {
      batch.draw(invcharTabR,
          getX() + inventory.rArmLeft - inventory.invLeft - 5,
          getY() + getHeight() - inventory.rArmBottom - 4);
      batch.draw(invcharTabL,
          getX() + inventory.lArmLeft - inventory.invLeft - 5,
          getY() + getHeight() - inventory.lArmBottom - 4);
    }
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
          Item cursor = Diablo.cursor.getItem();
          if (cursor != null) {
            if (!ArrayUtils.contains(cursor.type.BodyLoc, bodyPart)) {
              Diablo.audio.play(gameScreen.player.stats.getCharClass().name().toLowerCase() + "_impossible_1", false);
              return;
            }

            Diablo.cursor.setItem(item);
            item = cursor;
            gameScreen.player.setSlot(BodyPart.this.bodyLoc, item);
          } else {
            Diablo.cursor.setItem(item);
            item = null;
            gameScreen.player.setSlot(BodyPart.this.bodyLoc, null);
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
      if (gameScreen.player.isAlternate()) {
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
      Item cursorItem = Diablo.cursor.getItem();
      if (cursorItem != null) {
        blocked = !ArrayUtils.contains(cursorItem.type.BodyLoc, bodyPart);
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
        //Item.Details label = item.label;
        //label.setPosition(getX() + getWidth() / 2 - label.getWidth() / 2, getY() - label.getHeight());
        //label.draw(b, a);
      }
    }
  }
}
