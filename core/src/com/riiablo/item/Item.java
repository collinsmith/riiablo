package com.riiablo.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectMap;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.codec.Index;
import com.riiablo.codec.StringTBL;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.Inventory;
import com.riiablo.codec.excel.ItemEntry;
import com.riiablo.codec.excel.ItemTypes;
import com.riiablo.codec.excel.MagicAffix;
import com.riiablo.codec.excel.Misc;
import com.riiablo.codec.excel.SetItems;
import com.riiablo.codec.excel.UniqueItems;
import com.riiablo.codec.excel.Weapons;
import com.riiablo.codec.util.BBox;
import com.riiablo.codec.util.BitStream;
import com.riiablo.entity.Player;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.graphics.PaletteIndexedColorDrawable;
import com.riiablo.widget.Label;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;

import static com.riiablo.item.Quality.SET;

public class Item extends Actor implements Disposable {
  private static final String TAG = "Item";
  private static final boolean DEBUG = true;
  private static final boolean DEBUG_VERBOSE = DEBUG && !true;

  public static final float ETHEREAL_ALPHA = 0.667f;

  private static final int MAGIC_AFFIX_SIZE = 11;
  private static final int MAGIC_AFFIX_MASK = 0x7FF;
  private static final int RARE_AFFIX_SIZE = 8;
  private static final int RARE_AFFIX_MASK = 0xFF;

  private static final int QUEST      = 0x00000001;
  private static final int IDENTIFIED = 0x00000010;
  private static final int SWITCHIN   = 0x00000040; // Unconfirmed
  private static final int SWITCHOUT  = 0x00000080; // Unconfirmed
  private static final int BROKEN     = 0x00000100; // Unconfirmed (0 durability?)
  private static final int SOCKETED   = 0x00000800;
  private static final int INSTORE    = 0x00002000; // Unconfirmed (must be bought)
  private static final int EAR        = 0x00010000;
  private static final int STARTER    = 0x00020000;
  private static final int COMPACT    = 0x00200000;
  private static final int ETHEREAL   = 0x00400000;
  private static final int INSCRIBED  = 0x01000000;
  private static final int RUNEWORD   = 0x04000000;

  private static final Array<Stat.Instance>[] EMPTY_STAT_ARRAY = (Array<Stat.Instance>[]) new Array[0];

  private static final ObjectMap<String, String> WEAPON_DESC = new ObjectMap<>();
  static {
    WEAPON_DESC.put("mace", "WeaponDescMace");
    WEAPON_DESC.put("club", "WeaponDescMace");
    WEAPON_DESC.put("hamm", "WeaponDescMace");
    WEAPON_DESC.put("scep", "WeaponDescMace");
    WEAPON_DESC.put("axe", "WeaponDescAxe");
    WEAPON_DESC.put("taxe", "WeaponDescAxe");
    WEAPON_DESC.put("swor", "WeaponDescSword");
    WEAPON_DESC.put("knif", "WeaponDescDagger");
    WEAPON_DESC.put("tkni", "WeaponDescDagger");
    WEAPON_DESC.put("tpot", "WeaponDescThrownPotion");
    WEAPON_DESC.put("jave", "WeaponDescJavelin");
    WEAPON_DESC.put("ajav", "WeaponDescJavelin");
    WEAPON_DESC.put("spea", "WeaponDescSpear");
    WEAPON_DESC.put("aspe", "WeaponDescSpear");
    WEAPON_DESC.put("bow", "WeaponDescBow");
    WEAPON_DESC.put("abow", "WeaponDescBow");
    WEAPON_DESC.put("staf", "WeaponDescStaff");
    WEAPON_DESC.put("wand", "WeaponDescStaff");
    WEAPON_DESC.put("pole", "WeaponDescPoleArm");
    WEAPON_DESC.put("xbow", "WeaponDescCrossBow");
    WEAPON_DESC.put("h2h", "WeaponDescH2H");
    WEAPON_DESC.put("h2h2", "WeaponDescH2H");
    WEAPON_DESC.put("orb", "WeaponDescOrb");
  }

  public int      flags;
  public int      version; // 0 = pre-1.08; 1 = 1.08/1.09 normal; 2 = 1.10 normal; 100 = 1.08/1.09 expansion; 101 = 1.10 expansion
  public Location location;
  public BodyLoc  bodyLoc;
  public StoreLoc storeLoc;
  public byte     gridX;
  public byte     gridY;
  public String   typeCode;
  public int      socketsFilled;

  public Array<Item> socketed;

  // Extended
  public long    id;
  public byte    level;
  public Quality quality;
  public byte    pictureId;
  public short   classOnly;
  public int     qualityId;
  public Object  qualityData;
  public int     runewordData;
  public String  inscription;

  public EnumMap<Stat, Stat.Instance> props;
  public Array<Stat.Instance> stats[];

  public ItemEntry       base;
  public ItemTypes.Entry type;

  private String  name;
  private AssetDescriptor<DC6> invFileDescriptor;
  public DC6      invFile;
  public Index    invColormap;
  public int      invColorIndex;
  public Index    charColormap;
  public int      charColorIndex;

  public Details  details;

  public static Item loadFromStream(BitStream bitStream) {
    return new Item().read(bitStream);
  }

  private Item() {
    addListener(new ClickListener() {
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        super.enter(event, x, y, pointer, fromActor);
        if (isOver()) System.out.println("OVER");
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        super.exit(event, x, y, pointer, toActor);
        if (!isOver()) System.out.println("!OVER");
      }
    });
  }

  private Item read(BitStream bitStream) {
    flags    = bitStream.read32BitsOrLess(Integer.SIZE);
    version  = bitStream.readUnsigned8OrLess(8);
    bitStream.skip(2); // TODO: Unknown, likely included with location, should log at some point to check
    location = Location.valueOf(bitStream.readUnsigned7OrLess(3));
    bodyLoc  = BodyLoc.valueOf(bitStream.readUnsigned7OrLess(4));
    gridX    = bitStream.readUnsigned7OrLess(4);
    gridY    = bitStream.readUnsigned7OrLess(4);
    storeLoc = StoreLoc.valueOf(bitStream.readUnsigned7OrLess(3));

    if ((flags & EAR) == EAR) {
      typeCode      = "play"; // Player Body Part
      socketsFilled = 0;
      qualityId     = bitStream.readUnsigned7OrLess(3); // class
      qualityData   = bitStream.readUnsigned7OrLess(7); // level
      inscription   = bitStream.readString2(Player.MAX_NAME_LENGTH + 1, 7); // name
    } else {
      typeCode      = bitStream.readString(4).trim();
      socketsFilled = bitStream.readUnsigned7OrLess(3);
    }

    socketed = new Array<>(6);

    base = findBase(typeCode);
    type = Riiablo.files.ItemTypes.get(base.type);

    props = new EnumMap<>(Stat.class);
    props.put(Stat.item_levelreq, new Stat.Instance(Stat.item_levelreq, base.levelreq, 0));
    if (base instanceof Weapons.Entry) {
      Weapons.Entry weapon = getBase();
      props.put(Stat.mindamage, new Stat.Instance(Stat.mindamage, weapon.mindam, 0));
      props.put(Stat.maxdamage, new Stat.Instance(Stat.maxdamage, weapon.maxdam, 0));
      props.put(Stat.secondary_mindamage, new Stat.Instance(Stat.secondary_mindamage, weapon._2handmindam, 0));
      props.put(Stat.secondary_maxdamage, new Stat.Instance(Stat.secondary_maxdamage, weapon._2handmaxdam, 0));
      props.put(Stat.item_throw_mindamage, new Stat.Instance(Stat.item_throw_mindamage, weapon.minmisdam, 0));
      props.put(Stat.item_throw_maxdamage, new Stat.Instance(Stat.item_throw_maxdamage, weapon.maxmisdam, 0));
      props.put(Stat.strength, new Stat.Instance(Stat.strength, weapon.reqstr, 0));
      props.put(Stat.dexterity, new Stat.Instance(Stat.dexterity, weapon.reqdex, 0));
    } else if (base instanceof Armor.Entry) {
      Armor.Entry armor = getBase();
      props.put(Stat.strength, new Stat.Instance(Stat.strength, armor.reqstr, 0));
      props.put(Stat.toblock, new Stat.Instance(Stat.toblock, armor.block, 0));
      props.put(Stat.mindamage, new Stat.Instance(Stat.mindamage, armor.mindam, 0));
      props.put(Stat.maxdamage, new Stat.Instance(Stat.maxdamage, armor.maxdam, 0));
    }
    // TODO: copy base items stats

    if ((flags & COMPACT) == COMPACT) {
      id           = 0;
      level        = 0;
      quality      = Quality.NONE;
      pictureId    = -1;
      classOnly    = -1;
      qualityId    = 0;
      qualityData  = null;
      runewordData = 0;
      inscription  = null;
      stats = EMPTY_STAT_ARRAY;
    } else {
      id        = bitStream.read32BitsOrLess(Integer.SIZE);
      level     = bitStream.readUnsigned7OrLess(7);
      quality   = Quality.valueOf(bitStream.readUnsigned7OrLess(4));
      pictureId = bitStream.readBoolean() ? bitStream.readUnsigned7OrLess(3)   : -1;
      classOnly = bitStream.readBoolean() ? bitStream.readUnsigned15OrLess(11) : -1;
      int listsFlags = 1 << 0;
      switch (quality) {
        case LOW:
        case HIGH:
          qualityId = bitStream.readUnsigned31OrLess(3);
          break;

        case NORMAL:
          qualityId = 0;
          break;

        case SET:
        case UNIQUE:
          qualityId = bitStream.readUnsigned31OrLess(12);
          qualityData = quality == SET
              ? Riiablo.files.SetItems.get(qualityId)
              : Riiablo.files.UniqueItems.get(qualityId);
          break;

        case MAGIC:
          qualityId = bitStream.readUnsigned31OrLess(2 * MAGIC_AFFIX_SIZE); // 11 for prefix, 11 for suffix
          break;

        case RARE:
        case CRAFTED:
          qualityId = bitStream.readUnsigned31OrLess(2 * RARE_AFFIX_SIZE); // 8 for prefix, 8 for suffix
          qualityData = new RareQualityData(bitStream);
          break;

        default:
          qualityId = 0;
      }

      if ((flags & RUNEWORD) == RUNEWORD) {
        runewordData = bitStream.read16BitsOrLess(Short.SIZE);
        listsFlags |= (1 << 6);
      }

      if ((flags & INSCRIBED) == INSCRIBED) {
        inscription = bitStream.readString2(Player.MAX_NAME_LENGTH + 1, 7);
      }

      bitStream.skip(1); // TODO: Unknown, this usually is 0, but is 1 on a Tome of Identify.  (It's still 0 on a Tome of Townportal.)

      if (type.is("armo")) {
        props.put(Stat.armorclass, Stat.armorclass.read(bitStream));
      }

      if (type.is("armo") || type.is("weap")) {
        Stat.Instance maxdurability = Stat.maxdurability.read(bitStream);
        props.put(Stat.maxdurability, maxdurability);
        if (maxdurability.value > 0) {
          props.put(Stat.durability, Stat.durability.read(bitStream));
        }
      }

      if ((flags & SOCKETED) == SOCKETED && (type.is("armo") || type.is("weap"))) {
        props.put(Stat.item_numsockets, Stat.item_numsockets.read(bitStream));
      }

      if (type.is("book")) {
        bitStream.skip(5); // TODO: Tomes have an extra 5 bits inserted at this point.  I have no idea what purpose they serve.  It looks like the value is 0 on all of my tomes.
      }

      if (base.stackable) {
        int quantity = bitStream.readUnsigned15OrLess(9);
        props.put(Stat.quantity, new Stat.Instance(Stat.quantity, quantity, 0));
      }

      if (quality == SET) {
        int lists = bitStream.readUnsigned7OrLess(5);
        listsFlags |= (lists << 1);
      }

      if (type.is("book")) {
        listsFlags = 0;
      }

      stats = (Array<Stat.Instance>[]) new Array[7];
      for (int i = 0; i < 7; i++) {
        if (((listsFlags >> i) & 1) == 1) {
          stats[i] = new Array<>(Stat.Instance.class);
          Array<Stat.Instance> stats = this.stats[i];
          for (;;) {
            int prop = bitStream.readUnsigned15OrLess(9);
            if (prop == 0x1ff) break;
            for (int j = 0, size = Stat.getStatCount(prop); j < size; j++) {
              Stat stat = Stat.valueOf(prop + j);
              stats.add(stat.read(bitStream));
            }
          }
        }
      }

      //System.out.println(getName() + " : " + Arrays.toString(stats) + " : " + Integer.toBinaryString(listsFlags));
    }

    return this;
  }

  public void load() {
    details = new Details();

    if (invFileDescriptor != null) return;
    invFileDescriptor = new AssetDescriptor<>("data\\global\\items\\" + getInvFileName() + '.' + DC6.EXT, DC6.class);
    Riiablo.assets.load(invFileDescriptor);
    Riiablo.assets.finishLoadingAsset(invFileDescriptor);
    invFile = Riiablo.assets.get(invFileDescriptor);
    resize();

    invColormap     = Riiablo.colormaps.get(base.InvTrans);
    String invColor = getInvColor();
    invColorIndex   = invColor != null ? Riiablo.files.colors.index(invColor) + 1 : 0;

    charColormap    = Riiablo.colormaps.get(base.Transform);
    String charColor = getCharColor();
    charColorIndex  = charColor != null ? Riiablo.files.colors.index(charColor) + 1 : 0;
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(invFileDescriptor.fileName);
  }

  @SuppressWarnings("unchecked")
  public static <T extends ItemEntry> T findBase(String code) {
    ItemEntry entry;
    if ((entry = Riiablo.files.armor  .get(code)) != null) return (T) entry;
    if ((entry = Riiablo.files.weapons.get(code)) != null) return (T) entry;
    if ((entry = Riiablo.files.misc   .get(code)) != null) return (T) entry;
    throw new GdxRuntimeException("Unable to locate entry for code: " + code);
  }

  @SuppressWarnings("unchecked")
  public <T extends ItemEntry> T getBase() {
    return (T) base;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder
        .append("name", getName())
        .append("type", typeCode)
        .append("flags", getFlagsString())
        .append("version", version);
    if (DEBUG_VERBOSE) {
      builder
          .append("location", location)
          .append("bodyLoc", bodyLoc)
          .append("storeLoc", storeLoc)
          .append("gridX", gridX)
          .append("gridY", gridY)
          .append("socketsFilled", socketsFilled)
          .append("id", String.format("0x%08X", (int) id))
          .append("level", level)
          .append("quality", quality)
          .append("pictureId", pictureId)
          .append("classOnly", String.format("0x%04X", classOnly))
          .append("qualityId", String.format("0x%08X", qualityId))
          .append("qualityData", qualityData)
          .append("runewordData", String.format("0x%04X", runewordData))
          .append("inscription", inscription)
          .append("socketed", socketed)
          .append("attrs", Arrays.toString(stats));
    } else {
      builder.append("location", location);
      switch (location) {
        case EQUIPPED:
          builder.append("bodyLoc", bodyLoc);
          break;

        case STORED:
          builder
              .append("storeLoc", storeLoc)
              .append("gridX", gridX)
              .append("gridY", gridY);
          break;

        case BELT:
          builder.append("gridX", gridX);
          break;

        default:
          // ignored
      }

      if ((flags & COMPACT) == 0) {
        builder
            .append("id", String.format("0x%08X", (int) id))
            .append("level", level)
            .append("quality", quality);
        if (pictureId >= 0) builder.append("pictureId", pictureId);
        if (classOnly >= 0) builder.append("classOnly", String.format("0x%04X", classOnly));
        switch (quality) {
          case LOW:
            builder.append("qualityId", LowQuality.valueOf((int) qualityId));
            break;

          case NORMAL:
            break;

          case HIGH:
            builder.append("qualityId", Riiablo.files.QualityItems.get((int) qualityId));
            break;

          case MAGIC:
            builder.append("qualityId", String.format("0x%06X", qualityId));
            break;

          case RARE:
          case CRAFTED:
            builder
                .append("qualityId", String.format("0x%02X", qualityId))
                .append("affixes", qualityData);
            break;

          case SET:
          case UNIQUE:
          default:
            builder.append("qualityId", qualityId);
        }

        if ((flags & RUNEWORD) == RUNEWORD) {
          builder.append("runewordData", String.format("[id=%d, extra=%d]",
              RunewordData.id(runewordData), RunewordData.extra(runewordData)));
        }

        if ((flags & INSCRIBED) == INSCRIBED) {
          builder.append("inscription", inscription);
        }

        if ((flags & SOCKETED) == SOCKETED && socketsFilled > 0) {
          builder.append("socketed", socketed);
        }

        builder.append("attrs", Arrays.toString(stats));
      }
    }

    return builder.toString();
  }

  private String getFlagsString() {
    StringBuilder builder = new StringBuilder();
    if ((flags & QUEST     ) == QUEST     ) builder.append("QUEST"     ).append('|');
    if ((flags & IDENTIFIED) == IDENTIFIED) builder.append("IDENTIFIED").append('|');
    if ((flags & SOCKETED  ) == SOCKETED  ) builder.append("SOCKETED"  ).append('|');
    if ((flags & EAR       ) == EAR       ) builder.append("EAR"       ).append('|');
    if ((flags & STARTER   ) == STARTER   ) builder.append("STARTER"   ).append('|');
    if ((flags & COMPACT   ) == COMPACT   ) builder.append("COMPACT"   ).append('|');
    if ((flags & ETHEREAL  ) == ETHEREAL  ) builder.append("ETHEREAL"  ).append('|');
    if ((flags & INSCRIBED ) == INSCRIBED ) builder.append("INSCRIBED" ).append('|');
    if ((flags & RUNEWORD  ) == RUNEWORD  ) builder.append("RUNEWORD"  ).append('|');
    if (builder.length() > 0) builder.setLength(builder.length() - 1);
    return builder.toString();
  }

  public String getName() {
    if (name == null) updateName();
    return name;
  }

  public void updateName() {
    StringBuilder name = new StringBuilder();
    int prefix, suffix;
    MagicAffix affix;
    switch (quality) {
      case LOW:
      case NORMAL:
      case HIGH:
        if ((flags & RUNEWORD) == RUNEWORD) {
          int runeword = RunewordData.id(runewordData);
          name.append(Riiablo.string.lookup(Riiablo.files.Runes.get(runeword).Name));
          break;
        } else if (socketsFilled > 0) {
          name.append(Riiablo.string.lookup(1728)) // Gemmed
              .append(' ')
              .append(Riiablo.string.lookup(base.namestr));
          break;
        }

        switch (quality) {
          case LOW:
            name.append(Riiablo.string.lookup(LowQuality.valueOf(qualityId).stringId))
                .append(' ')
                .append(Riiablo.string.lookup(base.namestr));
            break;

          case HIGH:
            name.append(Riiablo.string.lookup(1727)) // Superior
                .append(' ')
                .append(Riiablo.string.lookup(base.namestr));
            break;

          default:
            name.append(Riiablo.string.lookup(base.namestr));
        }
        break;

      case MAGIC:
        prefix = qualityId &   MAGIC_AFFIX_MASK;
        suffix = qualityId >>> MAGIC_AFFIX_SIZE;
        if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null) name.append(Riiablo.string.lookup(affix.name)).append(' ');
        name.append(Riiablo.string.lookup(base.namestr));
        if ((affix = Riiablo.files.MagicSuffix.get(suffix)) != null) name.append(' ').append(Riiablo.string.lookup(affix.name));
        break;

      case RARE:
      case CRAFTED:
        prefix = qualityId &   RARE_AFFIX_MASK;
        suffix = qualityId >>> RARE_AFFIX_SIZE;
        name.append(Riiablo.string.lookup(Riiablo.files.RarePrefix.get(prefix).name))
            .append(' ')
            .append(Riiablo.string.lookup(Riiablo.files.RareSuffix.get(suffix).name));
        break;

      case SET:
        name.append(Riiablo.string.lookup(Riiablo.files.SetItems.get(qualityId).index));
        break;

      case UNIQUE:
        name.append(Riiablo.string.lookup(Riiablo.files.UniqueItems.get(qualityId).index));
        break;

      default:
        name.append(Riiablo.string.lookup(base.namestr));
    }

    this.name = name.toString();
  }

  private String getInvFileName() {
    if (pictureId >= 0) {
      if (isIdentified() && quality == Quality.UNIQUE) {
        if (qualityId == 381) { // Annihilus
          return "invmss";
        } else if (qualityId == 400) { // Hellfire Torch
          return "invtrch";
        }
      }
      return type.InvGfx[pictureId];
    }
    switch (quality) {
      case SET:
        return !base.setinvfile.isEmpty()
            ? base.setinvfile
            : base.invfile;
      case UNIQUE:
        return !base.uniqueinvfile.isEmpty()
            ? base.uniqueinvfile
            : base.invfile;
      default:
        return base.invfile;
    }
  }

  public String getInvColor() {
    if (base.InvTrans == 0) return null;
    switch (quality) {
      case MAGIC: {
        MagicAffix affix;
        int prefix = qualityId & MAGIC_AFFIX_MASK;
        if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null && affix.transform)
          return affix.transformcolor;
        int suffix = qualityId >>> MAGIC_AFFIX_SIZE;
        if ((affix = Riiablo.files.MagicSuffix.get(suffix)) != null && affix.transform)
          return affix.transformcolor;
        return null;
      }

      case RARE:
      case CRAFTED: {
        MagicAffix affix;
        RareQualityData rareQualityData = (RareQualityData) qualityData;
        for (int i = 0; i < RareQualityData.NUM_AFFIXES; i++) {
          int prefix = rareQualityData.prefixes[i];
          if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null && affix.transform)
            return affix.transformcolor;
          int suffix = rareQualityData.suffixes[i];
          if ((affix = Riiablo.files.MagicSuffix.get(suffix)) != null && affix.transform)
            return affix.transformcolor;
        }
        return null;
      }

      case SET:
        return ((SetItems.Entry) qualityData).invtransform;

      case UNIQUE:
        return ((UniqueItems.Entry) qualityData).invtransform;

      default:
        return null;
    }
  }

  public String getCharColor() {
    if (base.Transform == 0) return null;
    switch (quality) {
      case MAGIC: {
        MagicAffix affix;
        int prefix = qualityId & MAGIC_AFFIX_MASK;
        if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null && affix.transform)
          return affix.transformcolor;
        int suffix = qualityId >>> MAGIC_AFFIX_SIZE;
        if ((affix = Riiablo.files.MagicSuffix.get(suffix)) != null && affix.transform)
          return affix.transformcolor;
        return null;
      }

      case RARE:
      case CRAFTED: {
        MagicAffix affix;
        RareQualityData rareQualityData = (RareQualityData) qualityData;
        for (int i = 0; i < RareQualityData.NUM_AFFIXES; i++) {
          int prefix = rareQualityData.prefixes[i];
          if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null && affix.transform)
            return affix.transformcolor;
          int suffix = rareQualityData.suffixes[i];
          if ((affix = Riiablo.files.MagicSuffix.get(suffix)) != null && affix.transform)
            return affix.transformcolor;
        }
        return null;
      }

      case SET:
        return ((SetItems.Entry) qualityData).chrtransform;

      case UNIQUE:
        return ((UniqueItems.Entry) qualityData).chrtransform;

      default:
        return null;
    }
  }

  public String getFlippyFile() {
    if (isIdentified() && quality == Quality.UNIQUE) {
      if (qualityId == 381) { // Annihilus
        return findBase("mss").flippyfile;
      } else if (qualityId == 400) { // Hellfire Torch
        return findBase("tch").flippyfile;
      }
    }

    return base.flippyfile;
  }

  public int getDropFxFrame() {
    if (isIdentified() && quality == Quality.UNIQUE) {
      if (qualityId == 381) { // Annihilus
        return findBase("mss").dropsfxframe;
      } else if (qualityId == 400) { // Hellfire Torch
        return findBase("tch").dropsfxframe;
      }
    }

    return base.dropsfxframe;
  }

  public String getDropSound() {
    if (isIdentified() && quality == Quality.UNIQUE) {
      if (qualityId == 381) { // Annihilus
        return "item_gem";
      } else if (qualityId == 400) { // Hellfire Torch
        return "item_gem";
      }
    }

    return base.dropsound;
  }

  public String getUseSound() {
    /*
    // Neither are usable
    if (isIdentified() && quality == Quality.UNIQUE) {
      if (qualityId == 381) { // Annihilus
        return "item_gem";
      } else if (qualityId == 400) { // Hellfire Torch
        return "item_gem";
      }
    }
    */

    return base.usesound;
  }

  public boolean isIdentified() {
    return (flags & IDENTIFIED) == IDENTIFIED;
  }

  public boolean isEthereal() {
    return (flags & ETHEREAL) == ETHEREAL;
  }

  public void resize() {
    BBox box = invFile.getBox();
    setSize(box.width, box.height);
  }

  public void resize(Inventory.Entry inv) {
    setSize(base.invwidth * inv.gridBoxWidth, base.invheight * inv.gridBoxHeight);
  }

  @Override
  public void draw(Batch batch, float a) {
    PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
    boolean ethereal = (flags & ETHEREAL) == ETHEREAL;
    if (ethereal) b.setAlpha(ETHEREAL_ALPHA);
    if (invColormap != null) b.setColormap(invColormap, invColorIndex);
    invFile.draw(b, getX(), getY());
    if (invColormap != null) b.resetColormap();
    if (ethereal) b.resetColor();
  }

  private static class RareQualityData {
    static final int NUM_AFFIXES = 3;
    int[] prefixes, suffixes;
    RareQualityData(BitStream bitStream) {
      prefixes = new int[NUM_AFFIXES];
      suffixes = new int[NUM_AFFIXES];
      for (int i = 0; i < NUM_AFFIXES; i++) {
        prefixes[i] = bitStream.readBoolean() ? bitStream.readUnsigned15OrLess(MAGIC_AFFIX_SIZE) : 0;
        suffixes[i] = bitStream.readBoolean() ? bitStream.readUnsigned15OrLess(MAGIC_AFFIX_SIZE) : 0;
      }
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("prefixes", prefixes)
          .append("suffixes", suffixes)
          .build();
    }
  }

  private static class RunewordData {
    static final int RUNEWORD_ID_SHIFT    = 0;
    static final int RUNEWORD_ID_MASK     = 0xFFF << RUNEWORD_ID_SHIFT;
    static final int RUNEWORD_EXTRA_SHIFT = 12;
    static final int RUNEWORD_EXTRA_MASK  = 0xF << RUNEWORD_EXTRA_SHIFT;

    static int id(int pack) {
      return (pack & RUNEWORD_ID_MASK) >>> RUNEWORD_ID_SHIFT;
    }

    static int extra(int pack) {
      return (pack & RUNEWORD_EXTRA_MASK) >>> RUNEWORD_EXTRA_SHIFT;
    }
  }

  public class Details extends Table {
    private static final float SPACING = 2;

    public final Table header;

    Label name;
    Label type;
    Label usable;

    Details() {
      setBackground(PaletteIndexedColorDrawable.MODAL_FONT16);
      BitmapFont font = Riiablo.fonts.font16;
      name = new Label(Item.this.getName(), font);
      type = new Label(Riiablo.string.lookup(base.namestr), font);
      switch (quality) {
        case LOW:
        case NORMAL:
        case HIGH:
          if ((flags & RUNEWORD) == RUNEWORD || base.quest > 0)
            name.setColor(Riiablo.colors.gold);
          if ((flags & (ETHEREAL|SOCKETED)) != 0)
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

      add(name).center().space(SPACING).row();
      if (quality.ordinal() > Quality.MAGIC.ordinal() || (flags & RUNEWORD) == RUNEWORD)
        add(type).center().space(SPACING).row();

      header = new Table() {{
        setBackground(PaletteIndexedColorDrawable.MODAL_FONT16);
        add(new Label(name)).center().space(SPACING).row();
        if (quality.ordinal() > Quality.MAGIC.ordinal() || (flags & RUNEWORD) == RUNEWORD)
          add(new Label(type)).center().space(SPACING).row();
        pack();
      }};

      if (socketed.size > 0) {
        String runequote = Riiablo.string.lookup("RuneQuote");
        StringBuilder runewordBuilder = null;
        for (Item socket : socketed) {
          if (socket.type.is("rune")) {
            if (runewordBuilder == null) runewordBuilder = new StringBuilder(runequote);
            runewordBuilder.append(Riiablo.string.lookup(socket.base.namestr + "L")); // TODO: Is there a r##L reference somewhere?
          }
        }
        if (runewordBuilder != null) {
          runewordBuilder.append(runequote);
          add(new Label(runewordBuilder.toString(), font, Riiablo.colors.gold)).center().space(SPACING).row();
        }
      }

      if (Item.this.type.is("book")) {
        add(new Label(Riiablo.string.lookup("InsertScrolls"), font, Riiablo.colors.white)).center().space(SPACING).row();
      } else if (Item.this.type.is("char")) {
        add(new Label(Riiablo.string.lookup("ItemExpcharmdesc"), font, Riiablo.colors.white)).center().space(SPACING).row();
      }

      // TODO: This seems a bit hacky, check and see if this is located somewhere (doesn't look like it)
      if (base.useable) {
        String string;
        if (base.code.equalsIgnoreCase("box")) {
          string = Riiablo.string.lookup("RightClicktoOpen");
        } else if (base.code.equalsIgnoreCase("bkd")) {
          string = Riiablo.string.lookup("RightClicktoRead");
        } else if (base instanceof Misc.Entry) {
          Misc.Entry misc = (Misc.Entry) base;
          if (misc.spelldesc > 0) {
            string = Riiablo.string.lookup(misc.spelldescstr);
          } else {
            string = Riiablo.string.lookup("RightClicktoUse");
          }
        } else {
          string = Riiablo.string.lookup("RightClicktoUse");
        }
        usable = new Label(string, font);
        usable.setColor(name.getColor());
        add(usable).center().space(SPACING).row();
      }

      if ((flags & COMPACT) == 0) {
        Stat.Instance stat;
        EnumMap<Stat, Stat.Instance> stats = Item.this.props;
        if ((stat = stats.get(Stat.armorclass)) != null)
          add(new Label(Riiablo.string.lookup("ItemStats1h") + " " + stat.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if (Item.this.type.is("weap")) {
          if ((stat = stats.get(Stat.maxdamage)) != null) // TODO: Conditional 2 handed if barbarian, etc
            add(new Label(Riiablo.string.lookup("ItemStats1l") + " " + stats.get(Stat.mindamage).value + " to " + stat.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        }
        if (Item.this.type.is("shld")) {
          if ((stat = stats.get(Stat.toblock)) != null)
            add(new Label(Riiablo.string.lookup("ItemStats1r") + stat.value + "%", font, Riiablo.colors.white)).center().space(SPACING).row();
          // TODO: if paladin, show smite damage -- ItemStats1o %d to %d
          if ((stat = stats.get(Stat.maxdamage)) != null && stat.value > 0)
            add(new Label(Riiablo.string.lookup("ItemStats1o") + " " + stats.get(Stat.mindamage).value + " to " + stat.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        }
        if (!Item.this.base.nodurability && (stat = stats.get(Stat.durability)) != null)
          add(new Label(Riiablo.string.lookup("ItemStats1d") + " " + stat.value + " " + Riiablo.string.lookup("ItemStats1j") + " " + stats.get(Stat.maxdurability).value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if ((stat = stats.get(Stat.dexterity)) != null && stat.value > 0)
          add(new Label(Riiablo.string.lookup("ItemStats1f") + " " + stat.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if ((stat = stats.get(Stat.strength)) != null && stat.value > 0)
          add(new Label(Riiablo.string.lookup("ItemStats1e") + " " + stat.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if ((stat = stats.get(Stat.item_levelreq)) != null && stat.value > 0)
          add(new Label(Riiablo.string.lookup("ItemStats1p") + " " + stat.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if ((stat = stats.get(Stat.quantity)) != null)
          add(new Label(Riiablo.string.lookup("ItemStats1i") + " " + stat.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if (Item.this.type.is("weap")) {
          add(new Label(Riiablo.string.lookup(WEAPON_DESC.get(Item.this.base.type)) + " - " + 0, font, Riiablo.colors.white)).center().space(SPACING).row();
        }

        //for (Stat.Instance stat : stats.values()) {
        //  add(new Label(stat.stat.toString(), font, Riiablo.colors.white)).center().space(SPACING).row();
        //}
      }

      // TODO: Detect stats with encoded groupings and auto join them into a grouped stat

      for (int i = 0; i < stats.length; i++) {
        Array<Stat.Instance> stats = Item.this.stats[i];
        if (stats == null) continue;

        // TODO: This can be cleaned up later
        IntMap<Array<Stat.Instance>> groups = new IntMap<>();
        for (Stat.Instance stat : stats) {
          int dgrp = stat.stat.entry().dgrp;
          if (dgrp > 0) {
            Array<Stat.Instance> group = groups.get(dgrp);
            if (group == null) groups.put(dgrp, group = new Array<>());
            group.add(stat);
          }
        }

        IntSet groupReplaced = new IntSet();
        IntMap<Stat.Instance> groupReplacements = new IntMap<>();
        for (IntMap.Entry<Array<Stat.Instance>> group : groups) {
          switch (group.key) {
            case 1:
            case 2:
              if (group.value.size == 4) {
                boolean allEqual = true;
                Stat.Instance first = group.value.first();
                for (int j = 1; allEqual && j < group.value.size; j++) {
                  Stat.Instance stat = group.value.get(j);
                  allEqual = (stat.value == first.value) && (stat.param == first.param);
                }

                if (allEqual) {
                  groupReplacements.put(group.key, first);
                }
              }
              break;
            default:
          }
        }

        stats.sort(new Comparator<Stat.Instance>() {
          @Override
          public int compare(Stat.Instance o1, Stat.Instance o2) {
            return o2.stat.entry().descpriority - o1.stat.entry().descpriority;
          }
        });

        for (Stat.Instance stat : stats) {
          Label label;
          int dgrp = stat.stat.entry().dgrp;
          boolean group = false;
          if (dgrp > 0) {
            if (groupReplaced.contains(dgrp)) continue;
            Stat.Instance grp = groupReplacements.get(dgrp);
            if (grp != null) {
              stat = grp;
              group = true;
              groupReplaced.add(dgrp);
            }
          }

          String text = stat.format(group);
          if (text == null) continue;
          label = new Label(text, font, Riiablo.colors.blue); // Conditionally, set should be green
          add(label).center().space(SPACING).row();
        }
      }

      StringBuilder itemFlags = null;
      if ((Item.this.flags & ETHEREAL) == ETHEREAL) {
        itemFlags = new StringBuilder(32);
        itemFlags.append(Riiablo.string.lookup(StringTBL.EXPANSION_OFFSET + 2745));
      }
      if ((Item.this.flags & SOCKETED) == SOCKETED) {
        if (itemFlags != null) itemFlags.append(',').append(' ');
        else itemFlags = new StringBuilder(16);
        Stat.Instance stat = props.get(Stat.item_numsockets);
        if (stat != null) {
          itemFlags.append(Riiablo.string.lookup("Socketable")).append(' ').append('(').append(stat.value).append(')');
        } else {
          if (itemFlags.length() == 0) itemFlags = null;
          Gdx.app.error(TAG, "Item marked socketed, but missing item_numsockets: " + Item.this.getName());
        }
      }
      if (itemFlags != null) {
        add(new Label(itemFlags.toString(), font, Riiablo.colors.blue)).center().space(SPACING).row();
      }

      // TODO: This can be cleaned up later -- add support for set detection
      if (quality == SET) {
        add().height(font.getLineHeight()).space(SPACING).row();
        final SetItems.Entry set = Riiablo.files.SetItems.get(qualityId);
        add(new Label(Riiablo.string.lookup(set.set), font, Riiablo.colors.gold)).space(SPACING).row();
        for (SetItems.Entry item : Riiablo.files.SetItems.getItems(set)) {
          add(new Label(Riiablo.string.lookup(item.index), font, Riiablo.colors.red)).space(SPACING).row();
        }
      }

      pack();
    }
  }
}
