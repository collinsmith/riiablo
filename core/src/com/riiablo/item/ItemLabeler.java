package com.riiablo.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.attributes.Attributes;
import com.riiablo.attributes.PropertiesGenerator;
import com.riiablo.attributes.Stat;
import com.riiablo.attributes.StatFormatter;
import com.riiablo.attributes.StatList;
import com.riiablo.attributes.StatListFlags;
import com.riiablo.attributes.StatListLabeler;
import com.riiablo.attributes.StatListRef;
import com.riiablo.attributes.StatRef;
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

  protected StatFormatter statFormatter = new StatFormatter(); // TODO: inject
  protected StatListLabeler labelFormatter = new StatListLabeler(statFormatter); // TODO: inject
  protected PropertiesGenerator propertiesGenerator = new PropertiesGenerator(); // TODO: inject

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
      table.add().height(font.getLineHeight()).space(SPACING).row();
      table.add(new Label(Riiablo.string.lookup("GemXp3") + " " + labelFormatter.createLabel(item.attrs.list(StatListFlags.GEM_WEAPON_LIST), null), font, Riiablo.colors.white)).center().space(SPACING).row();
      CharSequence tmp = labelFormatter.createLabel(item.attrs.list(StatListFlags.GEM_ARMOR_LIST), null);
      table.add(new Label(Riiablo.string.lookup("GemXp4") + " " + tmp, font, Riiablo.colors.white)).center().space(SPACING).row();
      table.add(new Label(Riiablo.string.lookup("GemXp1") + " " + tmp, font, Riiablo.colors.white)).center().space(SPACING).row();
      table.add(new Label(Riiablo.string.lookup("GemXp2") + " " + labelFormatter.createLabel(item.attrs.list(StatListFlags.GEM_SHIELD_LIST), null), font, Riiablo.colors.white)).center().space(SPACING).row();
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

    final Attributes attrs = item.attrs;
    //if ((flags & COMPACT) == 0) {
      StatRef prop;
      if ((prop = attrs.get(Stat.armorclass)) != null) {
        Table t = new Table();
        t.add(new Label(Riiablo.string.lookup("ItemStats1h") + " ", font));
        t.add(new Label(prop.asString(), font, prop.modified() ? Riiablo.colors.blue : Riiablo.colors.white));
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
        if ((i & 1) != 0 && (prop = attrs.get(Stat.maxdamage)) != null) {
          Table t = new Table();
          t.add(new Label(Riiablo.string.lookup("ItemStats1l") + " ", font));
          t.add(new Label(attrs.get(Stat.mindamage).asString() + " to " + prop.asString(), font, prop.modified() ? Riiablo.colors.blue : Riiablo.colors.white));
          t.pack();
          table.add(t).space(SPACING).row();
        }
        if ((i & 2) != 0 && (prop = attrs.get(Stat.secondary_maxdamage)) != null) {
          Table t = new Table();
          t.add(new Label(Riiablo.string.lookup("ItemStats1m") + " ", font));
          t.add(new Label(attrs.get(Stat.secondary_mindamage).asString() + " to " + prop.asString(), font, prop.modified() ? Riiablo.colors.blue : Riiablo.colors.white));
          t.pack();
          table.add(t).space(SPACING).row();
        }
        if (item.typeEntry.Throwable && (prop = attrs.get(Stat.item_throw_maxdamage)) != null) {
          Table t = new Table();
          t.add(new Label(Riiablo.string.lookup("ItemStats1n") + " ", font));
          t.add(new Label(attrs.get(Stat.item_throw_mindamage).asString() + " to " + prop.asString(), font, prop.modified() ? Riiablo.colors.blue : Riiablo.colors.white));
          t.pack();
          table.add(t).space(SPACING).row();
        }
      }
      if (item.type.is(Type.SHLD)) {
        if ((prop = attrs.get(Stat.toblock)) != null) {
          Table t = new Table();
          t.add(new Label(Riiablo.string.lookup("ItemStats1r"), font));
          t.add(new Label(prop.asString() + "%", font, Riiablo.colors.blue));
          t.pack();
          table.add(t).space(SPACING).row();
        }
        if (Riiablo.charData.classId == CharacterClass.PALADIN && (prop = attrs.get(Stat.maxdamage)) != null && prop.asInt() > 0)
          table.add(new Label(Riiablo.string.lookup("ItemStats1o") + " " + attrs.get(Stat.mindamage).asString() + " to " + prop.asString(), font, Riiablo.colors.white)).center().space(SPACING).row();
      }
      if (!item.base.nodurability && (prop = attrs.get(Stat.durability)) != null)
        table.add(new Label(Riiablo.string.lookup("ItemStats1d") + " " + prop.asString() + " " + Riiablo.string.lookup("ItemStats1j") + " " + attrs.get(Stat.maxdurability).asString(), font, Riiablo.colors.white)).center().space(SPACING).row();
      if (item.type.is(Type.CLAS)) {
        table.add(new Label(Riiablo.string.lookup(CharacterClass.get(item.typeEntry.Class).entry().StrClassOnly), font, Riiablo.colors.white)).center().space(SPACING).row();
      }
      if ((prop = attrs.get(Stat.reqdex)) != null && prop.asInt() > 0)
        table.add(new Label(Riiablo.string.lookup("ItemStats1f") + " " + prop.asString(), font, Riiablo.colors.white)).center().space(SPACING).row();
      if ((prop = attrs.get(Stat.reqstr)) != null && prop.asInt() > 0)
        table.add(new Label(Riiablo.string.lookup("ItemStats1e") + " " + prop.asString(), font, Riiablo.colors.white)).center().space(SPACING).row();
      if ((prop = attrs.get(Stat.item_levelreq)) != null && prop.asInt() > 0)
        table.add(new Label(Riiablo.string.lookup("ItemStats1p") + " " + prop.asString(), font, Riiablo.colors.white)).center().space(SPACING).row();
      if ((prop = attrs.get(Stat.quantity)) != null)
        table.add(new Label(Riiablo.string.lookup("ItemStats1i") + " " + prop.asString(), font, Riiablo.colors.white)).center().space(SPACING).row();
      if (item.type.is(Type.WEAP)) {
        table.add(new Label(Riiablo.string.lookup(WEAPON_DESC.get(item.base.type)) + " - " + 0, font, Riiablo.colors.white)).center().space(SPACING).row();
      }
    //}

    if ((item.flags & Item.ITEMFLAG_COMPACT) == 0) {
      StatListRef temp = StatList.obtain();
      final StatList lists = attrs.list();
      if (lists.contains(StatListFlags.ITEM_MAGIC_LIST)) temp.addAll(lists.get(StatListFlags.ITEM_MAGIC_LIST));
      if (lists.contains(StatListFlags.ITEM_RUNE_LIST)) temp.addAll(lists.get(StatListFlags.ITEM_RUNE_LIST));
      for (Item socket : item.sockets) {
        temp.addAll(socket.attrs.remaining());
      }

      Iterable<String> labels = labelFormatter.createLabels(temp, Riiablo.charData.getStats());
      for (String label : labels) {
        table.add(new Label(label, font, Riiablo.colors.blue)).center().space(SPACING).row();
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
      StatRef stat = attrs.get(Stat.item_numsockets);
      if (stat != null) {
        itemFlags.append(Riiablo.string.lookup("Socketable")).append(' ').append('(').append(stat.asInt()).append(')');
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
        Iterable<String> labels = labelFormatter.createLabels(attrs.list(numEquipped), Riiablo.charData.getStats());
        for (String label : labels) {
          table.add(new Label(label, font, Riiablo.colors.green)).center().space(SPACING).row();
        }

        Sets.Entry set = setItem.getSet();
        StatListRef setBonus = null;
        if (numEquipped == set.getItems().size) { // full set bonus
          setBonus = propertiesGenerator.add(StatList.obtain(), set.FCode, set.FParam, set.FMin, set.FMax);
        } else { // partial set bonus
          switch (numEquipped) {
            case 2:
              setBonus = propertiesGenerator.add(StatList.obtain(), set.PCode2, set.PParam2, set.PMin2, set.PMax2);
              break;
            case 3:
              setBonus = propertiesGenerator.add(StatList.obtain(), set.PCode3, set.PParam3, set.PMin3, set.PMax3);
              break;
            case 4:
              setBonus = propertiesGenerator.add(StatList.obtain(), set.PCode4, set.PParam4, set.PMin4, set.PMax4);
              break;
            case 5:
              setBonus = propertiesGenerator.add(StatList.obtain(), set.PCode5, set.PParam5, set.PMin5, set.PMax5);
              break;
            default:
              // do nothing
          }
        }

        if (setBonus != null && setBonus.size() > 0) {
          table.add().height(font.getLineHeight()).space(SPACING).row();
          labels = labelFormatter.createLabels(setBonus, Riiablo.charData.getStats());
          for (String label : labels) {
            table.add(new Label(label, font, Riiablo.colors.gold)).center().space(SPACING).row();
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
