package com.riiablo.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.StringTBL;
import com.riiablo.codec.excel.Misc;
import com.riiablo.codec.excel.SetItems;
import com.riiablo.codec.excel.Sets;
import com.riiablo.codec.excel.Weapons;
import com.riiablo.graphics.PaletteIndexedColorDrawable;
import com.riiablo.widget.Label;

public class ItemLabeler {
  private static final String TAG = "ItemLabeler";

  private static final float SPACING = 2;

  public static final int LABELFLAG_INSTORE = 1 << 0;
  public static final int LABELFLAG_CANSELL = 1 << 1;

  static final Array<Stat> EMPTY_STAT_ARRAY = new Array<Stat>(0) {
    @Override
    public void add(Stat value) {
      throw new UnsupportedOperationException();
    }
  };

  private static final ObjectMap<String, String> WEAPON_DESC = new ObjectMap<>();
  static {
    WEAPON_DESC.put("mace", "WeaponDescMace");
    WEAPON_DESC.put("club", "WeaponDescMace");
    WEAPON_DESC.put("hamm", "WeaponDescMace");
    WEAPON_DESC.put("scep", "WeaponDescMace");
    WEAPON_DESC.put("axe",  "WeaponDescAxe");
    WEAPON_DESC.put("taxe", "WeaponDescAxe");
    WEAPON_DESC.put("swor", "WeaponDescSword");
    WEAPON_DESC.put("knif", "WeaponDescDagger");
    WEAPON_DESC.put("tkni", "WeaponDescDagger");
    WEAPON_DESC.put("tpot", "WeaponDescThrownPotion");
    WEAPON_DESC.put("jave", "WeaponDescJavelin");
    WEAPON_DESC.put("ajav", "WeaponDescJavelin");
    WEAPON_DESC.put("spea", "WeaponDescSpear");
    WEAPON_DESC.put("aspe", "WeaponDescSpear");
    WEAPON_DESC.put("bow",  "WeaponDescBow");
    WEAPON_DESC.put("abow", "WeaponDescBow");
    WEAPON_DESC.put("staf", "WeaponDescStaff");
    WEAPON_DESC.put("wand", "WeaponDescStaff");
    WEAPON_DESC.put("pole", "WeaponDescPoleArm");
    WEAPON_DESC.put("xbow", "WeaponDescCrossBow");
    WEAPON_DESC.put("h2h",  "WeaponDescH2H");
    WEAPON_DESC.put("h2h2", "WeaponDescH2H");
    WEAPON_DESC.put("orb",  "WeaponDescOrb");
  }

  public static void updateHeaderColors(Item item, Label name, Label type) {
    switch (item.quality) {
      case LOW:
      case NORMAL:
      case HIGH:
        if ((item.flags & Item.ITEMFLAG_RUNEWORD) == Item.ITEMFLAG_RUNEWORD || item.base.quest > 0)
          name.setColor(Riiablo.colors.gold);
        if ((item.flags & (Item.ITEMFLAG_ETHEREAL|Item.ITEMFLAG_SOCKETED)) != 0)
          type.setColor(Riiablo.colors.grey);
        break;
      case MAGIC:
        name.setColor(Riiablo.colors.blue);
        type.setColor(Riiablo.colors.blue);
        break;
      case SET:
        name.setColor(Riiablo.colors.green);
        type.setColor(Riiablo.colors.green);
        break;
      case RARE:
        name.setColor(Riiablo.colors.yellow);
        type.setColor(Riiablo.colors.yellow);
        break;
      case UNIQUE:
        name.setColor(Riiablo.colors.gold);
        type.setColor(Riiablo.colors.gold);
        break;
      case CRAFTED:
        name.setColor(Riiablo.colors.orange);
        type.setColor(Riiablo.colors.orange);
        break;
    }

    if (item.type.is(Type.RUNE)) {
      name.setColor(Riiablo.colors.orange);
    }
  }

  public Table updateHeader(Item item, Table table) {
    BitmapFont font = Riiablo.fonts.font16;
    Label name = new Label(item.getNameString(), font);
    Label type = new Label(Riiablo.string.lookup(item.base.namestr), font);
    updateHeaderColors(item, name, type);

    table.clearChildren();
    table.setBackground(PaletteIndexedColorDrawable.MODAL_FONT16);
    table.add(new Label(name)).center().space(SPACING).row();
    if (item.quality.ordinal() > Quality.MAGIC.ordinal() || (item.flags & Item.ITEMFLAG_RUNEWORD) == Item.ITEMFLAG_RUNEWORD) {
      table.add(new Label(type)).center().space(SPACING).row();
    }
    table.pack();
    return table;
  }

  public Table updateLabel(Item item, Table table, int labelFlags) {
    BitmapFont font = Riiablo.fonts.font16;
    Label name = new Label(item.getNameString(), font);
    Label type = new Label(Riiablo.string.lookup(item.base.namestr), font);
    updateHeaderColors(item, name, type);

    table.clearChildren();
    table.setBackground(PaletteIndexedColorDrawable.MODAL_FONT16);

    if (item.hasFlag2(Item.ITEMFLAG2_INSTORE)) {
      table.add(new Label(Riiablo.string.lookup("cost") + 0, font, name.getColor())).center().space(SPACING).row();
    }
    table.add(name).center().space(SPACING).row();
    if (item.quality.ordinal() > Quality.MAGIC.ordinal() || (item.flags & Item.ITEMFLAG_RUNEWORD) == Item.ITEMFLAG_RUNEWORD) {
      table.add(type).center().space(SPACING).row();
    }

    if (item.sockets.size > 0) {
      String runequote = Riiablo.string.lookup("RuneQuote");
      StringBuilder runewordBuilder = null;
      for (Item socket : item.sockets) {
        if (socket.type.is(Type.RUNE)) {
          if (runewordBuilder == null) runewordBuilder = new StringBuilder(runequote);
          runewordBuilder.append(Riiablo.string.lookup(socket.base.namestr + "L")); // TODO: Is there a r##L reference somewhere?
        }
      }
      if (runewordBuilder != null) {
        runewordBuilder.append(runequote);
        table.add(new Label(runewordBuilder.toString(), font, Riiablo.colors.gold)).center().space(SPACING).row();
      }
    }

    if (item.type.is(Type.BOOK)) {
      table.add(new Label(Riiablo.string.lookup("InsertScrolls"), font, Riiablo.colors.white)).center().space(SPACING).row();
    } else if (item.type.is(Type.CHAR)) {
      table.add(new Label(Riiablo.string.lookup("ItemExpcharmdesc"), font, Riiablo.colors.white)).center().space(SPACING).row();
    } else if (item.type.is(Type.SOCK)) {
      table.add(new Label(Riiablo.string.lookup("ExInsertSocketsX"), font, Riiablo.colors.white)).center().space(SPACING).row();
    }

    if (item.type.is(Type.GEM) || item.type.is(Type.RUNE)) {
      assert item.stats.length == Item.NUM_GEMPROPS;
      table.add().height(font.getLineHeight()).space(SPACING).row();
      table.add(new Label(Riiablo.string.lookup("GemXp3") + " " + item.stats[Item.GEMPROPS_WEAPON].copy().reduce().get().format(Riiablo.charData), font, Riiablo.colors.white)).center().space(SPACING).row();
      String tmp = item.stats[Item.GEMPROPS_ARMOR].copy().reduce().get().format(Riiablo.charData);
      table.add(new Label(Riiablo.string.lookup("GemXp4") + " " + tmp, font, Riiablo.colors.white)).center().space(SPACING).row();
      table.add(new Label(Riiablo.string.lookup("GemXp1") + " " + tmp, font, Riiablo.colors.white)).center().space(SPACING).row();
      table.add(new Label(Riiablo.string.lookup("GemXp2") + " " + item.stats[Item.GEMPROPS_SHIELD].copy().reduce().get().format(Riiablo.charData), font, Riiablo.colors.white)).center().space(SPACING).row();
      table.add().height(font.getLineHeight()).space(SPACING).row();
    }

    // TODO: This seems a bit hacky, check and see if this is located somewhere (doesn't look like it)
    if (item.base.useable) {
      String string;
      if (item.base.code.equalsIgnoreCase("box")) {
        string = Riiablo.string.lookup("RightClicktoOpen");
      } else if (item.base.code.equalsIgnoreCase("bkd")) {
        string = Riiablo.string.lookup("RightClicktoRead");
      } else if (item.base instanceof Misc.Entry) {
        Misc.Entry misc = item.getBase();
        if (misc.spelldesc > 0) {
          string = Riiablo.string.lookup(misc.spelldescstr);
        } else {
          string = Riiablo.string.lookup("RightClicktoUse");
        }
      } else {
        string = Riiablo.string.lookup("RightClicktoUse");
      }
      Label usable = new Label(string, font);
      usable.setColor(name.getColor());
      table.add(usable).center().space(SPACING).row();
    }

    //if ((flags & COMPACT) == 0) {
      Stat prop;
      if ((prop = item.props.agg.get(Stat.armorclass)) != null) {
        Table t = new Table();
        t.add(new Label(Riiablo.string.lookup("ItemStats1h") + " ", font));
        t.add(new Label(Integer.toString(prop.val), font, item.props.get(Stat.armorclass).isModified() ? Riiablo.colors.blue : Riiablo.colors.white));
        t.pack();
        table.add(t).space(SPACING).row();
      }
      if (item.type.is(Type.WEAP)) {
        Weapons.Entry weapon = item.getBase();
        int i;
        if (weapon._1or2handed && Riiablo.charData.classId == CharacterClass.BARBARIAN) {
          i = 3;
        } else if (weapon._2handed) {
          i = 2;
        } else {
          i = 1;
        }
        if ((i & 1) != 0 && (prop = item.props.agg.get(Stat.maxdamage)) != null) {
          Table t = new Table();
          t.add(new Label(Riiablo.string.lookup("ItemStats1l") + " ", font));
          t.add(new Label(item.props.get(Stat.mindamage).val + " to " + prop.val, font, item.props.get(Stat.maxdamage).isModified() ? Riiablo.colors.blue : Riiablo.colors.white));
          t.pack();
          table.add(t).space(SPACING).row();
        }
        if ((i & 2) != 0 && (prop = item.props.agg.get(Stat.secondary_maxdamage)) != null) {
          Table t = new Table();
          t.add(new Label(Riiablo.string.lookup("ItemStats1m") + " ", font));
          t.add(new Label(item.props.get(Stat.secondary_mindamage).val + " to " + prop.val, font, item.props.get(Stat.secondary_maxdamage).isModified() ? Riiablo.colors.blue : Riiablo.colors.white));
          t.pack();
          table.add(t).space(SPACING).row();
        }
        if (item.typeEntry.Throwable && (prop = item.props.agg.get(Stat.item_throw_maxdamage)) != null) {
          Table t = new Table();
          t.add(new Label(Riiablo.string.lookup("ItemStats1n") + " ", font));
          t.add(new Label(item.props.get(Stat.item_throw_mindamage).val + " to " + prop.val, font, item.props.get(Stat.item_throw_maxdamage).isModified() ? Riiablo.colors.blue : Riiablo.colors.white));
          t.pack();
          table.add(t).space(SPACING).row();
        }
      }
      if (item.type.is(Type.SHLD)) {
        if ((prop = item.props.agg.get(Stat.toblock)) != null) {
          Table t = new Table();
          t.add(new Label(Riiablo.string.lookup("ItemStats1r"), font));
          t.add(new Label(prop.val + "%", font, Riiablo.colors.blue));
          t.pack();
          table.add(t).space(SPACING).row();
        }
        if (Riiablo.charData.classId == CharacterClass.PALADIN && (prop = item.props.agg.get(Stat.maxdamage)) != null && prop.val > 0)
          table.add(new Label(Riiablo.string.lookup("ItemStats1o") + " " + item.props.agg.get(Stat.mindamage).val + " to " + prop.val, font, Riiablo.colors.white)).center().space(SPACING).row();
      }
      if (!item.base.nodurability && (prop = item.props.agg.get(Stat.durability)) != null)
        table.add(new Label(Riiablo.string.lookup("ItemStats1d") + " " + prop.val + " " + Riiablo.string.lookup("ItemStats1j") + " " + item.props.agg.get(Stat.maxdurability).val, font, Riiablo.colors.white)).center().space(SPACING).row();
      if (item.type.is(Type.CLAS)) {
        table.add(new Label(Riiablo.string.lookup(CharacterClass.get(item.typeEntry.Class).entry().StrClassOnly), font, Riiablo.colors.white)).center().space(SPACING).row();
      }
      if ((prop = item.props.agg.get(Stat.reqdex)) != null && prop.val > 0)
        table.add(new Label(Riiablo.string.lookup("ItemStats1f") + " " + prop.val, font, Riiablo.colors.white)).center().space(SPACING).row();
      if ((prop = item.props.agg.get(Stat.reqstr)) != null && prop.val > 0)
        table.add(new Label(Riiablo.string.lookup("ItemStats1e") + " " + prop.val, font, Riiablo.colors.white)).center().space(SPACING).row();
      if ((prop = item.props.agg.get(Stat.item_levelreq)) != null && prop.val > 0)
        table.add(new Label(Riiablo.string.lookup("ItemStats1p") + " " + prop.val, font, Riiablo.colors.white)).center().space(SPACING).row();
      if ((prop = item.props.agg.get(Stat.quantity)) != null)
        table.add(new Label(Riiablo.string.lookup("ItemStats1i") + " " + prop.val, font, Riiablo.colors.white)).center().space(SPACING).row();
      if (item.type.is(Type.WEAP)) {
        table.add(new Label(Riiablo.string.lookup(WEAPON_DESC.get(item.base.type)) + " - " + 0, font, Riiablo.colors.white)).center().space(SPACING).row();
      }
    //}

    // magic props
    if ((item.flags & Item.ITEMFLAG_COMPACT) == 0) {
      PropertyList magicProps = item.stats[Item.MAGIC_PROPS];
      PropertyList runeProps = item.stats[Item.RUNE_PROPS];
      if (magicProps != null) {
        PropertyList magicPropsAggregate = magicProps.copy();
        for (Item socket : item.sockets) {
          if (socket.type.is(Type.GEM) || socket.type.is(Type.RUNE)) {
            magicPropsAggregate.addAll(socket.stats[item.base.gemapplytype]);
          } else {
            magicPropsAggregate.addAll(socket.stats[Item.MAGIC_PROPS]);
          }
        }
        if (runeProps != null) magicPropsAggregate.addAll(runeProps);
        magicPropsAggregate.reduce();

        Array<Stat> aggregate = magicPropsAggregate.toArray();
        aggregate.sort();
        for (Stat stat : aggregate) {
          String text = stat.format(Riiablo.charData);
          if (text == null) continue;
          table.add(new Label(text, font, Riiablo.colors.blue)).center().space(SPACING).row();
        }
      }
    }

    StringBuilder itemFlags = null;
    if ((item.flags & Item.ITEMFLAG_ETHEREAL) == Item.ITEMFLAG_ETHEREAL) {
      itemFlags = new StringBuilder(32);
      itemFlags.append(Riiablo.string.lookup(StringTBL.EXPANSION_OFFSET + 2745));
    }
    if ((item.flags & Item.ITEMFLAG_SOCKETED) == Item.ITEMFLAG_SOCKETED) {
      if (itemFlags != null) itemFlags.append(',').append(' ');
      else itemFlags = new StringBuilder(16);
      Stat stat = item.props.get(Stat.item_numsockets);
      if (stat != null) {
        itemFlags.append(Riiablo.string.lookup("Socketable")).append(' ').append('(').append(stat.val).append(')');
      } else {
        if (itemFlags.length() == 0) itemFlags = null;
        Gdx.app.error(TAG, "Item marked socketed, but missing item_numsockets: " + item.getNameString());
      }
    }
    if (itemFlags != null) {
      table.add(new Label(itemFlags.toString(), font, Riiablo.colors.blue)).center().space(SPACING).row();
    }

    if (item.quality == Quality.SET && item.location == Location.EQUIPPED) {
      SetItems.Entry setItem = Riiablo.files.SetItems.get(item.qualityId);
      int setId = Riiablo.files.Sets.index(setItem.set);
      int numEquipped = Riiablo.charData.getItems().getEquippedSets().get(setId, 0); // TODO: use parent itemdata instead
      if (numEquipped >= 2) {
        PropertyList setPropsAggregate = null;
        for (int i = 0; i < numEquipped; i++) {
          PropertyList setProps = item.stats[Item.SET_PROPS + i];
          if (setProps == null) continue; // It might be the case that gaps exist
          if (setPropsAggregate == null) {
            setPropsAggregate = setProps.copy();
          } else {
            setPropsAggregate.addAll(setProps);
          }
        }

        Array<Stat> aggregate = setPropsAggregate != null
            ? setPropsAggregate.reduce().toArray()
            : EMPTY_STAT_ARRAY;
        aggregate.sort();
        for (Stat stat : aggregate) {
          String text = stat.format(Riiablo.charData);
          if (text == null) continue;
          table.add(new Label(text, font, Riiablo.colors.green)).center().space(SPACING).row();
        }

        Sets.Entry set = setItem.getSet();
        PropertyList setBonus = null;
        if (numEquipped == set.getItems().size) { // full set bonus
          setBonus = PropertyList.obtain().add(set.FCode, set.FParam, set.FMin, set.FMax);
        } else { // partial set bonus
          switch (numEquipped) {
            case 2:
              setBonus = PropertyList.obtain().add(set.PCode2, set.PParam2, set.PMin2, set.PMax2);
              break;
            case 3:
              setBonus = PropertyList.obtain().add(set.PCode3, set.PParam3, set.PMin3, set.PMax3);
              break;
            case 4:
              setBonus = PropertyList.obtain().add(set.PCode4, set.PParam4, set.PMin4, set.PMax4);
              break;
            case 5:
              setBonus = PropertyList.obtain().add(set.PCode5, set.PParam5, set.PMin5, set.PMax5);
              break;
            default:
              // do nothing
          }
        }

        if (setBonus != null && setBonus.size() > 0) {
          table.add().height(font.getLineHeight()).space(SPACING).row();
          setBonus.reduce().toArray();
          aggregate = setBonus.toArray();
          aggregate.sort();
          for (Stat stat : aggregate) {
            String text = stat.format(Riiablo.charData);
            if (text == null) continue;
            table.add(new Label(text, font, Riiablo.colors.gold)).center().space(SPACING).row();
          }
        }
      }

      table.add().height(font.getLineHeight()).space(SPACING).row();
      Sets.Entry set = Riiablo.files.SetItems.get(item.qualityId).getSet();
      table.add(new Label(Riiablo.string.lookup(set.name), font, Riiablo.colors.gold)).space(SPACING).row();
      for (SetItems.Entry setItemEntry : set.getItems()) {
        int numOwned = Riiablo.charData.getItems().getOwnedSetCount(Riiablo.files.SetItems.index(setItemEntry.index));
        Label label = new Label(Riiablo.string.lookup(setItemEntry.index), font,
            numOwned > 0 ? Riiablo.colors.green : Riiablo.colors.red);
        table.add(label).space(SPACING).row();
      }
    }

    table.pack();
    return table;
  }
}
