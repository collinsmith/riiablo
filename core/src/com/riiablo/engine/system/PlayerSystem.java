package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.riiablo.CharData;
import com.riiablo.Riiablo;
import com.riiablo.codec.COF;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.Weapons;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.PlayerComponent;
import com.riiablo.engine.component.TypeComponent;
import com.riiablo.engine.component.cof.AlphaUpdate;
import com.riiablo.engine.component.cof.TransformUpdate;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;

import org.apache.commons.lang3.ObjectUtils;

public class PlayerSystem extends EntitySystem implements EntityListener, CharData.EquippedListener {
  private static final String TAG = "PlayerSystem";
  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_STATE = DEBUG && !true;

  private static final String[] TOKENS = {"AM", "SO", "NE", "PA", "BA", "DZ", "AI"};

  private final ComponentMapper<PlayerComponent> charDataComponent = ComponentMapper.getFor(PlayerComponent.class);
  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final Family family = Family.all(PlayerComponent.class, CofComponent.class).get();

  private final ComponentMapper<TransformUpdate> transformUpdate = ComponentMapper.getFor(TransformUpdate.class);
  private final ComponentMapper<AlphaUpdate> alphaUpdate = ComponentMapper.getFor(AlphaUpdate.class);

  private final ObjectMap<CharData, Entity> charDatas = new ObjectMap<>();

  public PlayerSystem() {
    setProcessing(false);
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    engine.addEntityListener(family, this);
  }

  @Override
  public void removedFromEngine(Engine engine) {
    super.removedFromEngine(engine);
    engine.removeEntityListener(this);
  }

  @Override
  public void entityAdded(Entity entity) {
    PlayerComponent playerComponent = this.charDataComponent.get(entity);
    CharData charData = playerComponent.charData;
    loadItems(charData.getD2S().items.items);
    CofComponent cofComponent = this.cofComponent.get(entity);
    cofComponent.token = TOKENS[charData.getD2S().header.charClass];
    updateWeaponClass(entity, charData, cofComponent);
    updateArmorClass(entity, charData, cofComponent);
    charDatas.put(charData, entity);
    charData.addEquippedListener(this);
  }

  @Override
  public void entityRemoved(Entity entity) {

  }

  private void loadItems(Array<Item> items) {
    for (Item item : items) {
      //item.setOwner(this);
      item.load();
    }
  }

  private void updateWeaponClass(Entity entity, CharData charData, CofComponent cof) {
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
        cof.wclass = CofComponent.WEAPON_1SS; // Left Swing Right Swing
      } else if (LHEntry.wclass.equals("1hs") && RHEntry.wclass.equals("1ht")) {
        cof.wclass = CofComponent.WEAPON_1ST; // Left Swing Right Thrust
      } else if (LHEntry.wclass.equals("1ht") && RHEntry.wclass.equals("1hs")) {
        cof.wclass = CofComponent.WEAPON_1JS; // Left Jab Right Swing
      } else if (LHEntry.wclass.equals("1ht") && RHEntry.wclass.equals("1ht")) {
        cof.wclass = CofComponent.WEAPON_1JT; // Left Jab Right Thrust
      } else if (LH.type.is(com.riiablo.item.Type.MISS) || RH.type.is(com.riiablo.item.Type.MISS)) {
        cof.wclass = Riiablo.files.WeaponClass.index(LH.type.is(com.riiablo.item.Type.MISS) ? LHEntry.wclass : RHEntry.wclass);
      } else if (LH.type.is(com.riiablo.item.Type.H2H)  || RH.type.is(com.riiablo.item.Type.H2H)) {
        cof.wclass = CofComponent.WEAPON_HT2; // Two Hand-to-Hand
      } else {
        cof.wclass = CofComponent.WEAPON_HTH;
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
        cof.wclass = Riiablo.files.WeaponClass.index(LHEntry.wclass);
      } else if (RH.type.is(com.riiablo.item.Type.WEAP)) { // make sure weap and not e.g. misl, might not be required
        Weapons.Entry RHEntry = RH.getBase();
        cof.wclass = Riiablo.files.WeaponClass.index(RHEntry.wclass);
      } else {
        cof.wclass = CofComponent.WEAPON_HTH;
      }
    } else {
      cof.wclass = CofComponent.WEAPON_HTH;
    }

    cof.component[COF.Component.RH] = RH != null ? TypeComponent.Type.PLR.getComponent(RH.base.alternateGfx) : CofComponent.COMPONENT_NIL;
    cof.component[COF.Component.LH] = LH != null ? TypeComponent.Type.PLR.getComponent(LH.base.alternateGfx) : CofComponent.COMPONENT_NIL;
    cof.component[COF.Component.SH] = SH != null ? TypeComponent.Type.PLR.getComponent(SH.base.alternateGfx) : CofComponent.COMPONENT_NIL;

    cof.alpha[COF.Component.RH] = RH != null && RH.isEthereal() ? Item.ETHEREAL_ALPHA : CofComponent.ALPHA_NULL;
    cof.alpha[COF.Component.LH] = LH != null && LH.isEthereal() ? Item.ETHEREAL_ALPHA : CofComponent.ALPHA_NULL;
    cof.alpha[COF.Component.SH] = SH != null && SH.isEthereal() ? Item.ETHEREAL_ALPHA : CofComponent.ALPHA_NULL;
    com.riiablo.engine.Engine.getOrCreateComponent(entity, getEngine(), AlphaUpdate.class, this.alphaUpdate).flags |= BodyLoc.RARM.components();
  }

  private void updateArmorClass(Entity entity, CharData charData, CofComponent cof) {
    Item head = charData.getEquipped(BodyLoc.HEAD);
    cof.component[COF.Component.HD] = head != null ? TypeComponent.Type.PLR.getComponent(head.base.alternateGfx) : CofComponent.COMPONENT_LIT;
    cof.transform[COF.Component.HD] = head != null ? (byte) ((head.base.Transform << 5) | (head.charColorIndex & 0x1F)) : CofComponent.TRANSFORM_NULL;
    TransformUpdate transformUpdate = com.riiablo.engine.Engine.getOrCreateComponent(entity, getEngine(), TransformUpdate.class, this.transformUpdate);
    transformUpdate.flags |= BodyLoc.HEAD.components();

    Item body = charData.getEquipped(BodyLoc.TORS);
    if (body != null) {
      Armor.Entry armor = body.getBase();
      cof.component[COF.Component.TR] = (armor.Torso + 1);
      cof.component[COF.Component.LG] = (armor.Legs  + 1);
      cof.component[COF.Component.RA] = (armor.rArm  + 1);
      cof.component[COF.Component.LA] = (armor.lArm  + 1);
      cof.component[COF.Component.S1] = (armor.lSPad + 1);
      cof.component[COF.Component.S2] = (armor.rSPad + 1);

      byte packedTransform = (byte) ((body.base.Transform << 5) | (body.charColorIndex & 0x1F));
      cof.transform[COF.Component.TR] = packedTransform;
      cof.transform[COF.Component.LG] = packedTransform;
      cof.transform[COF.Component.RA] = packedTransform;
      cof.transform[COF.Component.LA] = packedTransform;
      cof.transform[COF.Component.S1] = packedTransform;
      cof.transform[COF.Component.S2] = packedTransform;
      transformUpdate.flags |= BodyLoc.TORS.components();
    } else {
      cof.component[COF.Component.TR] = CofComponent.COMPONENT_LIT;
      cof.component[COF.Component.LG] = CofComponent.COMPONENT_LIT;
      cof.component[COF.Component.RA] = CofComponent.COMPONENT_LIT;
      cof.component[COF.Component.LA] = CofComponent.COMPONENT_LIT;
      cof.component[COF.Component.S1] = CofComponent.COMPONENT_LIT;
      cof.component[COF.Component.S2] = CofComponent.COMPONENT_LIT;

      cof.transform[COF.Component.TR] = CofComponent.TRANSFORM_NULL;
      cof.transform[COF.Component.LG] = CofComponent.TRANSFORM_NULL;
      cof.transform[COF.Component.RA] = CofComponent.TRANSFORM_NULL;
      cof.transform[COF.Component.LA] = CofComponent.TRANSFORM_NULL;
      cof.transform[COF.Component.S1] = CofComponent.TRANSFORM_NULL;
      cof.transform[COF.Component.S2] = CofComponent.TRANSFORM_NULL;
      transformUpdate.flags |= BodyLoc.TORS.components();
    }
  }

  @Override
  public void onChanged(CharData client, BodyLoc bodyLoc, Item oldItem, Item item) {
    Entity entity = charDatas.get(client);
    CofComponent cofComponent = this.cofComponent.get(entity);
    cofComponent.dirty |= bodyLoc.components();
    updateWeaponClass(entity, client, cofComponent);
    updateArmorClass(entity, client, cofComponent);
  }

  @Override
  public void onAlternated(CharData client, int alternate, Item LH, Item RH) {
    Entity entity = charDatas.get(client);
    CofComponent cofComponent = this.cofComponent.get(entity);
    updateWeaponClass(entity, client, cofComponent);
    updateArmorClass(entity, client, cofComponent);
  }
}
