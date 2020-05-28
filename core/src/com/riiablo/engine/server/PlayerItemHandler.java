package com.riiablo.engine.server;

import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.riiablo.Riiablo;
import com.riiablo.codec.COF;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.Weapons;
import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.Player;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.save.CharData;
import com.riiablo.save.ItemData;

import org.apache.commons.lang3.ObjectUtils;

@All({Player.class, CofReference.class})
public class PlayerItemHandler extends BaseEntitySystem implements ItemData.EquipListener, ItemData.AlternateListener {
  private static final String TAG = "PlayerItemHandler";
  private static final boolean DEBUG        = !true;
  private static final boolean DEBUG_WCLASS = DEBUG && true;

  protected ComponentMapper<Player> mPlayer;
  protected ComponentMapper<CofReference> mCofReference;

  protected CofManager cofs;

  private final ObjectIntMap<ItemData> itemDatas = new ObjectIntMap<>();

  @Override
  protected void processSystem() {}

  @Override
  protected void inserted(int entityId) {
    if (Riiablo.game.player != entityId) return; // FIXME: workaround for remote heros
    CharData data = mPlayer.get(entityId).data;
    data.preloadItems();
    ItemData itemData = data.getItems();
    CofReference reference = mCofReference.get(entityId);
    updateWeaponClass(entityId, itemData, reference);
    updateArmorClass(entityId, itemData);
    itemDatas.put(itemData, entityId);
    data.getItems().addEquipListener(this);
    data.getItems().addAlternateListener(this);
  }

  private void updateWeaponClass(int entityId, ItemData itemData, CofReference reference) {
    Item RH = null, LH = null, SH = null;
    Item rArm = itemData.getEquipped(BodyLoc.RARM);
    if (rArm != null) {
      if (rArm.type.is(com.riiablo.item.Type.WEAP)) {
        RH = rArm;
      } else if (rArm.type.is(com.riiablo.item.Type.SHLD)) {
        SH = rArm;
      }
    }

    Item lArm = itemData.getEquipped(BodyLoc.LARM);
    if (lArm != null) {
      if (lArm.type.is(com.riiablo.item.Type.WEAP)) {
        LH = lArm;
      } else if (lArm.type.is(com.riiablo.item.Type.SHLD)) {
        SH = lArm;
      }
    }

    if (DEBUG_WCLASS) {
      Gdx.app.debug(TAG, "RH = " + RH);
      Gdx.app.debug(TAG, "LH = " + LH);
      Gdx.app.debug(TAG, "SH = " + SH);
    }

    if (LH != null && RH != null) {
      Weapons.Entry LHEntry = LH.getBase();
      Weapons.Entry RHEntry = RH.getBase();
      if (       LHEntry.wclass.equals("1hs") && RHEntry.wclass.equals("1hs")) {
        cofs.setWClass(entityId, Engine.WEAPON_1SS); // Left Swing Right Swing
      } else if (LHEntry.wclass.equals("1hs") && RHEntry.wclass.equals("1ht")) {
        cofs.setWClass(entityId, Engine.WEAPON_1ST); // Left Swing Right Thrust
      } else if (LHEntry.wclass.equals("1ht") && RHEntry.wclass.equals("1hs")) {
        cofs.setWClass(entityId, Engine.WEAPON_1JS); // Left Jab Right Swing
      } else if (LHEntry.wclass.equals("1ht") && RHEntry.wclass.equals("1ht")) {
        cofs.setWClass(entityId, Engine.WEAPON_1JT); // Left Jab Right Thrust
      } else if (LH.type.is(com.riiablo.item.Type.MISS) || RH.type.is(com.riiablo.item.Type.MISS)) {
        cofs.setWClass(entityId, (byte) Riiablo.files.WeaponClass.index(LH.type.is(com.riiablo.item.Type.MISS) ? LHEntry.wclass : RHEntry.wclass));
      } else if (LH.type.is(com.riiablo.item.Type.H2H)  || RH.type.is(com.riiablo.item.Type.H2H)) {
        cofs.setWClass(entityId, Engine.WEAPON_HT2); // Two Hand-to-Hand
      } else {
        cofs.setWClass(entityId, Engine.WEAPON_HTH);
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
        cofs.setWClass(entityId, (byte) Riiablo.files.WeaponClass.index(LHEntry.wclass));
      } else if (RH.type.is(com.riiablo.item.Type.WEAP)) { // make sure weap and not e.g. misl, might not be required
        Weapons.Entry RHEntry = RH.getBase();
        cofs.setWClass(entityId, (byte) Riiablo.files.WeaponClass.index(RHEntry.wclass));
      } else {
        cofs.setWClass(entityId, Engine.WEAPON_HTH);
      }
    } else {
      cofs.setWClass(entityId, Engine.WEAPON_HTH);
    }

    cofs.setComponent(entityId, COF.Component.RH, RH != null ? Class.Type.PLR.getComponent(RH.base.alternateGfx) : CofComponents.COMPONENT_NIL);
    cofs.setComponent(entityId, COF.Component.LH, LH != null ? Class.Type.PLR.getComponent(LH.base.alternateGfx) : CofComponents.COMPONENT_NIL);
    cofs.setComponent(entityId, COF.Component.SH, SH != null ? Class.Type.PLR.getComponent(SH.base.alternateGfx) : CofComponents.COMPONENT_NIL);

    int alphaFlags = 0;
    alphaFlags |= cofs.setAlpha(entityId, COF.Component.RH, RH != null && RH.isEthereal() ? Item.ETHEREAL_ALPHA : CofAlphas.ALPHA_NULL);
    alphaFlags |= cofs.setAlpha(entityId, COF.Component.LH, LH != null && LH.isEthereal() ? Item.ETHEREAL_ALPHA : CofAlphas.ALPHA_NULL);
    alphaFlags |= cofs.setAlpha(entityId, COF.Component.SH, SH != null && SH.isEthereal() ? Item.ETHEREAL_ALPHA : CofAlphas.ALPHA_NULL);
    cofs.updateAlpha(entityId, alphaFlags);
  }

  private void updateArmorClass(int entityId, ItemData itemData) {
    int transformFlags = 0;
    Item head = itemData.getSlot(BodyLoc.HEAD);
    cofs.setComponent(entityId, COF.Component.HD, head != null ? Class.Type.PLR.getComponent(head.base.alternateGfx) : CofComponents.COMPONENT_LIT);
    transformFlags |= cofs.setTransform(entityId, COF.Component.HD, head != null ? (byte) ((head.base.Transform << 5) | (head.charColorIndex & 0x1F)) : CofTransforms.TRANSFORM_NULL);

    Item body = itemData.getSlot(BodyLoc.TORS);
    if (body != null) {
      Armor.Entry armor = body.getBase();
      cofs.setComponent(entityId, COF.Component.TR, (armor.Torso + 1));
      cofs.setComponent(entityId, COF.Component.LG, (armor.Legs  + 1));
      cofs.setComponent(entityId, COF.Component.RA, (armor.rArm  + 1));
      cofs.setComponent(entityId, COF.Component.LA, (armor.lArm  + 1));
      cofs.setComponent(entityId, COF.Component.S1, (armor.lSPad + 1));
      cofs.setComponent(entityId, COF.Component.S2, (armor.rSPad + 1));

      byte packedTransform = (byte) ((body.base.Transform << 5) | (body.charColorIndex & 0x1F));
      transformFlags |= cofs.setTransform(entityId, COF.Component.TR, packedTransform);
      transformFlags |= cofs.setTransform(entityId, COF.Component.LG, packedTransform);
      transformFlags |= cofs.setTransform(entityId, COF.Component.RA, packedTransform);
      transformFlags |= cofs.setTransform(entityId, COF.Component.LA, packedTransform);
      transformFlags |= cofs.setTransform(entityId, COF.Component.S1, packedTransform);
      transformFlags |= cofs.setTransform(entityId, COF.Component.S2, packedTransform);
    } else {
      cofs.setComponent(entityId, COF.Component.TR, CofComponents.COMPONENT_LIT);
      cofs.setComponent(entityId, COF.Component.LG, CofComponents.COMPONENT_LIT);
      cofs.setComponent(entityId, COF.Component.RA, CofComponents.COMPONENT_LIT);
      cofs.setComponent(entityId, COF.Component.LA, CofComponents.COMPONENT_LIT);
      cofs.setComponent(entityId, COF.Component.S1, CofComponents.COMPONENT_LIT);
      cofs.setComponent(entityId, COF.Component.S2, CofComponents.COMPONENT_LIT);

      transformFlags |= cofs.setTransform(entityId, COF.Component.TR, CofTransforms.TRANSFORM_NULL);
      transformFlags |= cofs.setTransform(entityId, COF.Component.LG, CofTransforms.TRANSFORM_NULL);
      transformFlags |= cofs.setTransform(entityId, COF.Component.RA, CofTransforms.TRANSFORM_NULL);
      transformFlags |= cofs.setTransform(entityId, COF.Component.LA, CofTransforms.TRANSFORM_NULL);
      transformFlags |= cofs.setTransform(entityId, COF.Component.S1, CofTransforms.TRANSFORM_NULL);
      transformFlags |= cofs.setTransform(entityId, COF.Component.S2, CofTransforms.TRANSFORM_NULL);
    }

    cofs.updateTransform(entityId, transformFlags);
  }

  @Override
  public void onUnequip(ItemData items, BodyLoc bodyLoc, Item item) {
    onChanged(items, bodyLoc);
  }

  @Override
  public void onEquip(ItemData items, BodyLoc bodyLoc, Item item) {
    onChanged(items, bodyLoc);
  }

  public void onChanged(ItemData items, BodyLoc bodyLoc) {
    int id = itemDatas.get(items, Engine.INVALID_ENTITY);
    CofReference reference = mCofReference.get(id);
    //cofComponent.dirty |= bodyLoc.components();
    updateWeaponClass(id, items, reference);
    updateArmorClass(id, items);
  }

  @Override
  public void onAlternated(ItemData items, int alternate, Item LH, Item RH) {
    int id = itemDatas.get(items, Engine.INVALID_ENTITY);
    CofReference reference = mCofReference.get(id);
    updateWeaponClass(id, items, reference);
    updateArmorClass(id, items);
  }
}
