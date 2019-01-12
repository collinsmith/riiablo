package gdx.diablo.entity2;

import com.badlogic.gdx.Gdx;

import java.util.EnumMap;

import gdx.diablo.codec.excel.Weapons;
import gdx.diablo.item.BodyLoc;
import gdx.diablo.item.Item;

public class Player extends Entity {
  private static final String TAG = "Player";

  public enum Slot {
    HEAD,
    NECK,
    TORS,
    RARM,
    LARM,
    RRIN,
    LRIN,
    BELT,
    FEET,
    GLOV;
  }

  boolean dirty;
  boolean alternate;
  EnumMap<BodyLoc, Item> equipped = new EnumMap<>(BodyLoc.class);

  public Player() {
    //super(Diablo.files.PlrMode);
  }

  public Item getSlot(Slot slot) {
    switch (slot) {
      case HEAD: return equipped.get(BodyLoc.HEAD);
      case NECK: return equipped.get(BodyLoc.NECK);
      case TORS: return equipped.get(BodyLoc.TORS);
      case RARM: return equipped.get(alternate ? BodyLoc.RARM2 : BodyLoc.RARM);
      case LARM: return equipped.get(alternate ? BodyLoc.LARM2 : BodyLoc.LARM);
      case RRIN: return equipped.get(BodyLoc.RRIN);
      case LRIN: return equipped.get(BodyLoc.LRIN);
      case BELT: return equipped.get(BodyLoc.BELT);
      case FEET: return equipped.get(BodyLoc.FEET);
      case GLOV: return equipped.get(BodyLoc.GLOV);
      default:
        Gdx.app.error(TAG, "Invalid slot: " + slot);
        return null;
    }
  }

  public void update() {
    if (!dirty) {
      return;
    } else {
      dirty = false;
    }

    Item rArm = getSlot(Slot.RARM);
    if (rArm != null && rArm.type.is("weap")) {
      Weapons.Entry entry = rArm.getBase();
      if (entry._2handed) {
        weaponClass = entry._2handedwclass;
      } else {
        weaponClass = entry.wclass;
      }
    } else {
      weaponClass = "HTH";
    }

    Item lArm = getSlot(Slot.LARM);
    if (lArm != null) {
      if (lArm.type.is("weap") && rArm != null && rArm.type.is("weap")) {

      } else if (lArm.type.is("shld")) {

      }
    }

    if (lArm != null && lArm.type.is("weap")) {
      weaponClass = ((Weapons.Entry) lArm.base).wclass;
    }

    // TODO: add support for barb (1js,1jt,1ss,1st) / assassin (ht1,ht2) customs
    Item weapon = null;
    if (rArm != null && rArm.type.is("weap")) {
      weapon = rArm;
    } else if (lArm != null && lArm.type.is("weap")) {
      weapon = lArm;
    }

    String wclass = null;
    if (weapon == null) {
      wclass = "hth";
    } else {
      Weapons.Entry weaponEntry = weapon.getBase();
      wclass = weaponEntry.wclass;
    }

    // TODO: custom code for barbarian _1or2handed
    if (rArm != null && lArm != null) {
      // if rArm and lArm are weapons
      // if only one is weapon
      /*
      if (rArm.type.is("weap")) {
        Weapons.Entry rArmEntry = rArm.getBase();

      }
      */
    } else if (rArm != null) {
    } else if (lArm != null) {
    }
  }
}
