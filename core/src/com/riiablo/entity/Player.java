package com.riiablo.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.riiablo.CharData;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.COF;
import com.riiablo.codec.D2S;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.Weapons;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.map.DT1.Tile;
import com.riiablo.map.Map;
import com.riiablo.server.Connect;

import org.apache.commons.lang3.ObjectUtils;

public class Player extends Entity implements CharData.EquippedListener {
  private static final String TAG = "Player";
  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_STATE = DEBUG && !true;
  private static final boolean DEBUG_INV   = DEBUG && !true;
  private static final boolean DEBUG_EQUIP = DEBUG && !true;

  public static final int MAX_NAME_LENGTH = 15;

  public static final byte MODE_DT =  0;
  public static final byte MODE_NU =  1;
  public static final byte MODE_WL =  2;
  public static final byte MODE_RN =  3;
  public static final byte MODE_GH =  4;
  public static final byte MODE_TN =  5;
  public static final byte MODE_TW =  6;
  public static final byte MODE_A1 =  7;
  public static final byte MODE_A2 =  8;
  public static final byte MODE_BL =  9;
  public static final byte MODE_SC = 10;
  public static final byte MODE_TH = 11;
  public static final byte MODE_KK = 12;
  public static final byte MODE_S1 = 13;
  public static final byte MODE_S2 = 14;
  public static final byte MODE_S3 = 15;
  public static final byte MODE_S4 = 16;
  public static final byte MODE_DD = 17;
  //public static final byte MODE_GH = 18;
  //public static final byte MODE_GH = 19;

  private static final int MOVING_MODES = (1 << MODE_WL) | (1 << MODE_RN) | (1 << MODE_TW);
  private static final int CASTING_MODES = (1 << MODE_SC);
  private static final int CASTABLE_MODES = (1 << MODE_NU) | (1 << MODE_TN) | MOVING_MODES;

  private static final String[] TOKENS = {"AM", "SO", "NE", "PA", "BA", "DZ", "AI"};

  public static String getToken(int type) {
    return TOKENS[type];
  }

  boolean ignoreUpdate;
  public Map map;
  public Map.Zone curZone;
  public D2S.MercData merc;
  public CharData charData;
  public final CharacterClass charClass;

  public Player(String name, CharacterClass characterClass) {
    this(name, characterClass.id);
  }

  public Player(CharData charData) {
    this(charData.getD2S().header.name, charData.getD2S().header.charClass);
    this.charData = charData;
    charData.getD2S().loadRemaining();
    charData.updateD2S();
    loadItems(charData.getD2S().items.items);
    merc = charData.getD2S().header.merc;
    for (Item item : merc.items.items.items) {
      item.load();
    }
    charData.addEquippedListener(this);
  }

  public Player(Connect connect) {
    this(connect.name, connect.classId);

    ignoreUpdate = true;
    setWeapon(WEAPON_1HS);
    setComponents(connect.composites);
    setTransforms(connect.colors);
  }

  Player(String name, int classId) {
    super(Type.PLR, "player", TOKENS[classId]);
    charClass = CharacterClass.get(classId);
    setMode(MODE_TN);
    setWalkSpeed(6);
    setRunSpeed(9);
    setRunning(true);
    angle(-MathUtils.PI / 2);
  }

  private void loadItems(Array<Item> items) {
    for (Item item : items) {
      //item.setOwner(this);
      item.load();
    }
  }

  @Override
  public byte getNeutralMode() {
    return (curZone != null && curZone.isTown()) ? MODE_TN : MODE_NU;
  }

  @Override
  public byte getWalkMode() {
    return (curZone != null && curZone.isTown()) ? MODE_TW : MODE_WL;
  }

  @Override
  public byte getRunMode() {
    return MODE_RN;
  }

  boolean ignoreFootstep = false;

  @Override
  public void act(float delta) {
    super.act(delta);
    if (animation != null && (mode == getWalkMode() || mode == getRunMode())) {
      int frame = animation.getFrame();
      int numFrames = animation.getNumFramesPerDir();
      if (frame == 0 || frame == numFrames >>> 1) {
        if (ignoreFootstep) return;
        ignoreFootstep = true;
        int x = Map.round(position.x);
        int y = Map.round(position.y);
        int tx = x < 0
            ? ((x + 1) / Tile.SUBTILE_SIZE) - 1
            : (x / Tile.SUBTILE_SIZE);
        int ty = y < 0
            ? ((y + 1) / Tile.SUBTILE_SIZE) - 1
            : (y / Tile.SUBTILE_SIZE);
        Map map = Map.instance;
        Map.Zone zone = map.getZone(x, y);
        Tile tile = map.getTile(0, tx, ty);
        String type = DT1Sound.getType(zone.level, tile);
        Riiablo.audio.play("light_run_" + type + "_1", true);
      } else {
        ignoreFootstep = false;
      }
    }
  }

  @Override
  protected void updateCOF() {
    if (ignoreUpdate) {
      super.updateCOF();
      return;
    }

    updateWeaponClass();

    Item head = charData.getEquipped(BodyLoc.HEAD);
    setComponent(COF.Component.HD, head != null ? (byte) Type.PLR.getComponent(head.base.alternateGfx) : (byte) 1);
    setTransform(COF.Component.HD, head != null ? (byte) ((head.base.Transform << 5) | (head.charColorIndex & 0x1F)) : (byte) 0xFF);

    Item body = charData.getEquipped(BodyLoc.TORS);
    if (body != null) {
      Armor.Entry armor = body.getBase();
      setComponent(COF.Component.TR, (byte) (armor.Torso + 1));
      setComponent(COF.Component.LG, (byte) (armor.Legs  + 1));
      setComponent(COF.Component.RA, (byte) (armor.rArm  + 1));
      setComponent(COF.Component.LA, (byte) (armor.lArm  + 1));
      setComponent(COF.Component.S1, (byte) (armor.lSPad + 1));
      setComponent(COF.Component.S2, (byte) (armor.rSPad + 1));

      byte packedTransform = (byte) ((body.base.Transform << 5) | (body.charColorIndex & 0x1F));
      setTransform(COF.Component.TR, packedTransform);
      setTransform(COF.Component.LG, packedTransform);
      setTransform(COF.Component.RA, packedTransform);
      setTransform(COF.Component.LA, packedTransform);
      setTransform(COF.Component.S1, packedTransform);
      setTransform(COF.Component.S2, packedTransform);
    } else {
      setComponent(COF.Component.TR, (byte) 1);
      setComponent(COF.Component.LG, (byte) 1);
      setComponent(COF.Component.RA, (byte) 1);
      setComponent(COF.Component.LA, (byte) 1);
      setComponent(COF.Component.S1, (byte) 1);
      setComponent(COF.Component.S2, (byte) 1);

      setTransform(COF.Component.TR, (byte) 0xFF);
      setTransform(COF.Component.LG, (byte) 0xFF);
      setTransform(COF.Component.RA, (byte) 0xFF);
      setTransform(COF.Component.LA, (byte) 0xFF);
      setTransform(COF.Component.S1, (byte) 0xFF);
      setTransform(COF.Component.S2, (byte) 0xFF);
    }

    super.updateCOF();
  }

  @Override
  public void onChanged(CharData client, BodyLoc bodyLoc, Item oldItem, Item item) {
    if (item != null) item.bodyLoc = bodyLoc;
    invalidate(bodyLoc.components());
    updateWeaponClass();
  }

  @Override
  public void onAlternated(CharData client, int alternate, Item LH, Item RH) {
    updateWeaponClass();
  }

  private void updateWeaponClass() {
    Item RH = null, LH = null, SH = null;
    Item rArm = charData.getEquipped2(BodyLoc.RARM);
    if (rArm != null) {
      if (rArm.type.is(com.riiablo.item.Type.WEAP)) {
        RH = rArm;
      } else if (rArm.type.is(com.riiablo.item.Type.SHLD)) {
        SH = rArm;
      }
    }

    Item lArm = charData.getEquipped2(BodyLoc.LARM);
    if (lArm != null) {
      if (lArm.type.is(com.riiablo.item.Type.WEAP)) {
        LH = lArm;
      } else if (lArm.type.is(com.riiablo.item.Type.SHLD)) {
        SH = lArm;
      }
    }

    if (DEBUG_STATE) {
      Gdx.app.debug(TAG, "RH = " + RH);
      Gdx.app.debug(TAG, "LH = " + LH);
      Gdx.app.debug(TAG, "SH = " + SH);
    }

    if (LH != null && RH != null) {
      Weapons.Entry LHEntry = LH.getBase();
      Weapons.Entry RHEntry = RH.getBase();
      if (       LHEntry.wclass.equals("1hs") && RHEntry.wclass.equals("1hs")) {
        setWeapon(WEAPON_1SS); // Left Swing Right Swing
      } else if (LHEntry.wclass.equals("1hs") && RHEntry.wclass.equals("1ht")) {
        setWeapon(WEAPON_1ST); // Left Swing Right Thrust
      } else if (LHEntry.wclass.equals("1ht") && RHEntry.wclass.equals("1hs")) {
        setWeapon(WEAPON_1JS); // Left Jab Right Swing
      } else if (LHEntry.wclass.equals("1ht") && RHEntry.wclass.equals("1ht")) {
        setWeapon(WEAPON_1JT); // Left Jab Right Thrust
      } else if (LH.type.is(com.riiablo.item.Type.MISS) || RH.type.is(com.riiablo.item.Type.MISS)) {
        setWeapon((byte) Riiablo.files.WeaponClass.index(LH.type.is(com.riiablo.item.Type.MISS) ? LHEntry.wclass : RHEntry.wclass));
      } else if (LH.type.is(com.riiablo.item.Type.H2H)  || RH.type.is(com.riiablo.item.Type.H2H)) {
        setWeapon(WEAPON_HT2); // Two Hand-to-Hand
      } else {
        setWeapon(WEAPON_HTH);
        Gdx.app.error(TAG, String.format(
            "Unknown weapon combination: LH=%s RH=%s", LHEntry.wclass, RHEntry.wclass));
      }
    } else if (LH != null || RH != null) {
      RH = ObjectUtils.firstNonNull(RH, LH);
      LH = null;
      if (RH.type.is(com.riiablo.item.Type.BOW)) {
        LH = RH;
        RH = null;
        Weapons.Entry LHEntry = LH.getBase();
        setWeapon((byte) Riiablo.files.WeaponClass.index(LHEntry.wclass));
      } else if (RH.type.is(com.riiablo.item.Type.WEAP)) { // make sure weap and not e.g. misl, might not be required
        Weapons.Entry RHEntry = RH.getBase();
        setWeapon((byte) Riiablo.files.WeaponClass.index(RHEntry.wclass));
      } else {
        setWeapon(WEAPON_HTH);
      }
    } else {
      setWeapon(WEAPON_HTH);
    }

    setComponent(COF.Component.RH, RH != null ? (byte) Type.PLR.getComponent(RH.base.alternateGfx) : 0);
    setComponent(COF.Component.LH, LH != null ? (byte) Type.PLR.getComponent(LH.base.alternateGfx) : 0);
    setComponent(COF.Component.SH, SH != null ? (byte) Type.PLR.getComponent(SH.base.alternateGfx) : 0);

    setAlpha(COF.Component.RH, RH != null && RH.isEthereal() ? Item.ETHEREAL_ALPHA : 1.0f);
    setAlpha(COF.Component.LH, LH != null && LH.isEthereal() ? Item.ETHEREAL_ALPHA : 1.0f);
    setAlpha(COF.Component.SH, SH != null && SH.isEthereal() ? Item.ETHEREAL_ALPHA : 1.0f);
  }

  @Override
  public boolean isCasting(byte mode) {
    return (CASTING_MODES & (1 << mode)) != 0;
  }

  @Override
  public boolean isCastable(byte mode) {
    return (CASTABLE_MODES & (1 << mode)) != 0;
  }

  @Override
  public boolean isMoving(byte mode) {
    return (MOVING_MODES & (1 << mode)) != 0;
  }
}
