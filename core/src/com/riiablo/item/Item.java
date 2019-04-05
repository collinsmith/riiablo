package com.riiablo.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.codec.Index;
import com.riiablo.codec.StringTBL;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.Gems;
import com.riiablo.codec.excel.Inventory;
import com.riiablo.codec.excel.ItemEntry;
import com.riiablo.codec.excel.ItemTypes;
import com.riiablo.codec.excel.MagicAffix;
import com.riiablo.codec.excel.Misc;
import com.riiablo.codec.excel.SetItems;
import com.riiablo.codec.excel.Sets;
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

  private static final int MAGIC_PROPS = 0;
  private static final int SET_PROPS   = 1;
  private static final int RUNE_PROPS  = 6;
  private static final int NUM_PROPS   = 7;
  private static final int MAGIC_PROPS_FLAG = 1 << MAGIC_PROPS;
  private static final int SET_2_PROPS_FLAG = 1 << SET_PROPS + 0;
  private static final int SET_3_PROPS_FLAG = 1 << SET_PROPS + 1;
  private static final int SET_4_PROPS_FLAG = 1 << SET_PROPS + 2;
  private static final int SET_5_PROPS_FLAG = 1 << SET_PROPS + 3;
  private static final int SET_6_PROPS_FLAG = 1 << SET_PROPS + 4;
  private static final int RUNE_PROPS_FLAG  = 1 << RUNE_PROPS;

  private static final int WEAPON_PROPS  = 0;
  private static final int ARMOR_PROPS   = 1;
  private static final int SHIELD_PROPS  = 2;
  private static final int NUM_GEM_PROPS = 3;

  private static final PropertyList[] EMPTY_STAT_ARRAY = new PropertyList[NUM_PROPS];

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

  public Array<Item> sockets;

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

  public Attributes   props;
  public PropertyList stats[];

  public ItemEntry       base;
  public ItemTypes.Entry typeEntry;
  public Type            type;

  private String  name;
  private AssetDescriptor<DC6> invFileDescriptor;
  public DC6      invFile;
  public Index    invColormap;
  public int      invColorIndex;
  public Index    charColormap;
  public int      charColorIndex;

  private Details details;

  public static Item loadFromStream(BitStream bitStream) {
    return new Item().read(bitStream);
  }

  Item() {}

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

    sockets = new Array<>(6);

    base = findBase(typeCode);
    typeEntry = Riiablo.files.ItemTypes.get(base.type);
    type = Type.get(typeEntry);

    props = new Attributes();
    props.put(Stat.item_levelreq, base.levelreq);
    if (base instanceof Weapons.Entry) {
      Weapons.Entry weapon = getBase();
      props.put(Stat.mindamage, weapon.mindam);
      props.put(Stat.maxdamage, weapon.maxdam);
      props.put(Stat.secondary_mindamage, weapon._2handmindam);
      props.put(Stat.secondary_maxdamage, weapon._2handmaxdam);
      props.put(Stat.item_throw_mindamage, weapon.minmisdam);
      props.put(Stat.item_throw_maxdamage, weapon.maxmisdam);
      props.put(Stat.reqstr, weapon.reqstr);
      props.put(Stat.reqdex, weapon.reqdex);
    } else if (base instanceof Armor.Entry) {
      Armor.Entry armor = getBase();
      props.put(Stat.reqstr, armor.reqstr);
      props.put(Stat.reqdex, 0);
      props.put(Stat.toblock, Riiablo.charData.getCharacterClass().entry().BlockFactor + armor.block);
      props.put(Stat.mindamage, armor.mindam);
      props.put(Stat.maxdamage, armor.maxdam);
    } else if (base instanceof Misc.Entry) {
      Misc.Entry misc = getBase();
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
      if (type.is(Type.GEM) || type.is(Type.RUNE)) {
        Gems.Entry gem = Riiablo.files.Gems.get(base.code);
        stats = new PropertyList[NUM_GEM_PROPS];
        stats[WEAPON_PROPS] = new PropertyList().add(gem.weaponModCode, gem.weaponModParam, gem.weaponModMin, gem.weaponModMax);
        stats[ARMOR_PROPS ] = new PropertyList().add(gem.helmModCode, gem.helmModParam, gem.helmModMin, gem.helmModMax);
        stats[SHIELD_PROPS] = new PropertyList().add(gem.shieldModCode, gem.shieldModParam, gem.shieldModMin, gem.shieldModMax);
      } else {
        stats = EMPTY_STAT_ARRAY;
      }
    } else {
      id        = bitStream.read32BitsOrLess(Integer.SIZE);
      level     = bitStream.readUnsigned7OrLess(7);
      quality   = Quality.valueOf(bitStream.readUnsigned7OrLess(4));
      pictureId = bitStream.readBoolean() ? bitStream.readUnsigned7OrLess(3)   : -1;
      classOnly = bitStream.readBoolean() ? bitStream.readUnsigned15OrLess(11) : -1;
      int listsFlags = MAGIC_PROPS_FLAG;
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
        listsFlags |= RUNE_PROPS_FLAG;
      }

      if ((flags & INSCRIBED) == INSCRIBED) {
        inscription = bitStream.readString2(Player.MAX_NAME_LENGTH + 1, 7);
      }

      bitStream.skip(1); // TODO: Unknown, this usually is 0, but is 1 on a Tome of Identify.  (It's still 0 on a Tome of Townportal.)

      if (type.is(Type.ARMO)) {
        props.read(Stat.armorclass, bitStream);
      }

      if (type.is(Type.ARMO) || type.is(Type.WEAP)) {
        int maxdurability = props.read(Stat.maxdurability, bitStream);
        if (maxdurability > 0) {
          props.read(Stat.durability, bitStream);
        }
      }

      if ((flags & SOCKETED) == SOCKETED && (type.is(Type.ARMO) || type.is(Type.WEAP))) {
        props.read(Stat.item_numsockets, bitStream);
      }

      if (type.is(Type.BOOK)) {
        bitStream.skip(5); // TODO: Tomes have an extra 5 bits inserted at this point.  I have no idea what purpose they serve.  It looks like the value is 0 on all of my tomes.
      }

      if (base.stackable) {
        int quantity = bitStream.readUnsigned15OrLess(9);
        props.put(Stat.quantity, quantity);
      }

      if (quality == SET) {
        int lists = bitStream.readUnsigned7OrLess(5);
        listsFlags |= (lists << SET_PROPS);
      }

      if (type.is(Type.BOOK)) {
        listsFlags = 0;
      }

      stats = new PropertyList[NUM_PROPS];
      for (int i = 0; i < NUM_PROPS; i++) {
        if (((listsFlags >> i) & 1) == 1) {
          stats[i] = new PropertyList().read(bitStream);
        }
      }

      //System.out.println(getName() + " : " + Arrays.toString(stats) + " : " + Integer.toBinaryString(listsFlags));
    }

    return this;
  }

  public void update() {
    if ((flags & COMPACT) == COMPACT) return;
    System.out.println(getName());
    PropertyList magicProps = stats[MAGIC_PROPS];
    PropertyList runeProps = stats[RUNE_PROPS];
    if (magicProps != null) {
      PropertyList magicPropsAggregate = magicProps.copy();
      for (Item socket : sockets) {
        if (socket.type.is(Type.GEM) || socket.type.is(Type.RUNE)) {
          magicPropsAggregate.addAll(socket.stats[base.gemapplytype]);
        } else {
          magicPropsAggregate.addAll(socket.stats[MAGIC_PROPS]);
        }
      }
      if (runeProps != null) magicPropsAggregate.addAll(runeProps);
      props.apply(magicPropsAggregate);
    }
  }

  public Details details() {
    if (details == null) {
      update();
      details = new Details();
    }
    return details;
  }

  public void load() {
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

    // TODO: load the images of socketed items
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
          .append("sockets", sockets)
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
          builder.append("sockets", sockets);
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
    if (isIdentified()) {
      switch (quality) {
        case SET:
          SetItems.Entry setItem = (SetItems.Entry) qualityData;
          if (!setItem.invfile.isEmpty()) return setItem.invfile;
          break;
        case UNIQUE:
          UniqueItems.Entry uniqueItem = (UniqueItems.Entry) qualityData;
          if (!uniqueItem.invfile.isEmpty()) return uniqueItem.invfile;
          break;
        default:
          // do nothing
      }
    }

    return pictureId >= 0 ? typeEntry.InvGfx[pictureId] : base.invfile;
  }

  public String getInvColor() {
    if (base.InvTrans == 0 || !isIdentified()) return null;
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
    if (base.Transform == 0 || !isIdentified()) return null;
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
    if (isIdentified()) {
      switch (quality) {
        case SET:
          SetItems.Entry setItem = (SetItems.Entry) qualityData;
          if (!setItem.flippyfile.isEmpty()) return setItem.flippyfile;
          break;
        case UNIQUE:
          UniqueItems.Entry uniqueItem = (UniqueItems.Entry) qualityData;
          if (!uniqueItem.flippyfile.isEmpty()) return uniqueItem.flippyfile;
          break;
        default:
          // do nothing
      }
    }

    return base.flippyfile;
  }

  public int getDropFxFrame() {
    if (isIdentified()) {
      switch (quality) {
        case SET:
          SetItems.Entry setItem = (SetItems.Entry) qualityData;
          if (setItem.dropsfxframe > 0) return setItem.dropsfxframe;
          break;
        case UNIQUE:
          UniqueItems.Entry uniqueItem = (UniqueItems.Entry) qualityData;
          if (uniqueItem.dropsfxframe > 0) return uniqueItem.dropsfxframe;
          break;
        default:
          // do nothing
      }
    }

    return base.dropsfxframe;
  }

  public String getDropSound() {
    if (isIdentified()) {
      switch (quality) {
        case SET:
          SetItems.Entry setItem = (SetItems.Entry) qualityData;
          if (!setItem.dropsound.isEmpty()) return setItem.dropsound;
          break;
        case UNIQUE:
          UniqueItems.Entry uniqueItem = (UniqueItems.Entry) qualityData;
          if (!uniqueItem.dropsound.isEmpty()) return uniqueItem.dropsound;
          break;
        default:
          // do nothing
      }
    }

    return base.dropsound;
  }

  public String getUseSound() {
    if (isIdentified()) {
      switch (quality) {
        case SET:
          SetItems.Entry setItem = (SetItems.Entry) qualityData;
          if (!setItem.usesound.isEmpty()) return setItem.usesound;
          break;
        case UNIQUE:
          UniqueItems.Entry uniqueItem = (UniqueItems.Entry) qualityData;
          if (!uniqueItem.usesound.isEmpty()) return uniqueItem.usesound;
          break;
        default:
          // do nothing
      }
    }

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

      if (Item.this.type.is(Type.RUNE))
        name.setColor(Riiablo.colors.orange);

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

      if (sockets.size > 0) {
        String runequote = Riiablo.string.lookup("RuneQuote");
        StringBuilder runewordBuilder = null;
        for (Item socket : sockets) {
          if (socket.type.is(Type.RUNE)) {
            if (runewordBuilder == null) runewordBuilder = new StringBuilder(runequote);
            runewordBuilder.append(Riiablo.string.lookup(socket.base.namestr + "L")); // TODO: Is there a r##L reference somewhere?
          }
        }
        if (runewordBuilder != null) {
          runewordBuilder.append(runequote);
          add(new Label(runewordBuilder.toString(), font, Riiablo.colors.gold)).center().space(SPACING).row();
        }
      }

      if (Item.this.type.is(Type.BOOK)) {
        add(new Label(Riiablo.string.lookup("InsertScrolls"), font, Riiablo.colors.white)).center().space(SPACING).row();
      } else if (Item.this.type.is(Type.CHAR)) {
        add(new Label(Riiablo.string.lookup("ItemExpcharmdesc"), font, Riiablo.colors.white)).center().space(SPACING).row();
      } else if (Item.this.type.is(Type.SOCK)) {
        add(new Label(Riiablo.string.lookup("ExInsertSocketsX"), font, Riiablo.colors.white)).center().space(SPACING).row();
      }

      if (Item.this.type.is(Type.GEM) || Item.this.type.is(Type.RUNE)) {
        assert stats.length == NUM_GEM_PROPS;
        add().height(font.getLineHeight()).space(SPACING).row();
        add(new Label(Riiablo.string.lookup("GemXp3") + " " + stats[WEAPON_PROPS].copy().reduce().get().format(Riiablo.charData), font, Riiablo.colors.white)).center().space(SPACING).row();
        String tmp = stats[ARMOR_PROPS].copy().reduce().get().format(Riiablo.charData);
        add(new Label(Riiablo.string.lookup("GemXp4") + " " + tmp, font, Riiablo.colors.white)).center().space(SPACING).row();
        add(new Label(Riiablo.string.lookup("GemXp1") + " " + tmp, font, Riiablo.colors.white)).center().space(SPACING).row();
        add(new Label(Riiablo.string.lookup("GemXp2") + " " + stats[SHIELD_PROPS].copy().reduce().get().format(Riiablo.charData), font, Riiablo.colors.white)).center().space(SPACING).row();
        add().height(font.getLineHeight()).space(SPACING).row();
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

      //if ((flags & COMPACT) == 0) {
        Stat.Instance prop;
        if ((prop = props.attrs().get(Stat.armorclass)) != null) {
          Table table = new Table();
          table.add(new Label(Riiablo.string.lookup("ItemStats1h") + " ", font));
          table.add(new Label(Integer.toString(prop.value), font, props.isModified(Stat.armorclass) ? Riiablo.colors.blue : Riiablo.colors.white));
          table.pack();
          add(table).space(SPACING).row();
        }
        if (Item.this.type.is(Type.WEAP)) {
          Weapons.Entry weapon = getBase();
          int i;
          if (weapon._1or2handed && Riiablo.charData.getCharacterClass() == CharacterClass.BARBARIAN) {
            i = 3;
          } else if (weapon._2handed) {
            i = 2;
          } else {
            i = 1;
          }
          if ((i & 1) != 0 && (prop = props.attrs().get(Stat.maxdamage)) != null) {
            Table table = new Table();
            table.add(new Label(Riiablo.string.lookup("ItemStats1l") + " ", font));
            table.add(new Label(props.get(Stat.mindamage).value + " to " + prop.value, font, props.isModified(Stat.maxdamage) ? Riiablo.colors.blue : Riiablo.colors.white));
            table.pack();
            add(table).space(SPACING).row();
          }
          if ((i & 2) != 0 && (prop = props.attrs().get(Stat.secondary_maxdamage)) != null) {
            Table table = new Table();
            table.add(new Label(Riiablo.string.lookup("ItemStats1m") + " ", font));
            table.add(new Label(props.get(Stat.secondary_mindamage).value + " to " + prop.value, font, props.isModified(Stat.secondary_maxdamage) ? Riiablo.colors.blue : Riiablo.colors.white));
            table.pack();
            add(table).space(SPACING).row();
          }
          if (typeEntry.Throwable && (prop = props.attrs().get(Stat.item_throw_maxdamage)) != null) {
            Table table = new Table();
            table.add(new Label(Riiablo.string.lookup("ItemStats1n") + " ", font));
            table.add(new Label(props.get(Stat.item_throw_mindamage).value + " to " + prop.value, font, props.isModified(Stat.item_throw_maxdamage) ? Riiablo.colors.blue : Riiablo.colors.white));
            table.pack();
            add(table).space(SPACING).row();
          }
        }
        if (Item.this.type.is(Type.SHLD)) {
          if ((prop = props.attrs().get(Stat.toblock)) != null) {
            Table table = new Table();
            table.add(new Label(Riiablo.string.lookup("ItemStats1r"), font));
            table.add(new Label(prop.value + "%", font, Riiablo.colors.blue));
            table.pack();
            add(table).space(SPACING).row();
          }
          if (Riiablo.charData.getCharacterClass() == CharacterClass.PALADIN && (prop = props.attrs().get(Stat.maxdamage)) != null && prop.value > 0)
            add(new Label(Riiablo.string.lookup("ItemStats1o") + " " + props.attrs().get(Stat.mindamage).value + " to " + prop.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        }
        if (!Item.this.base.nodurability && (prop = props.attrs().get(Stat.durability)) != null)
          add(new Label(Riiablo.string.lookup("ItemStats1d") + " " + prop.value + " " + Riiablo.string.lookup("ItemStats1j") + " " + props.attrs().get(Stat.maxdurability).value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if (Item.this.type.is(Type.CLAS)) {
          add(new Label(Riiablo.string.lookup(CharacterClass.get(Item.this.typeEntry.Class).entry().StrClassOnly), font, Riiablo.colors.white)).center().space(SPACING).row();
        }
        if ((prop = props.attrs().get(Stat.reqdex)) != null && prop.value > 0)
          add(new Label(Riiablo.string.lookup("ItemStats1f") + " " + prop.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if ((prop = props.attrs().get(Stat.reqstr)) != null && prop.value > 0)
          add(new Label(Riiablo.string.lookup("ItemStats1e") + " " + prop.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if ((prop = props.attrs().get(Stat.item_levelreq)) != null && prop.value > 0)
          add(new Label(Riiablo.string.lookup("ItemStats1p") + " " + prop.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if ((prop = props.attrs().get(Stat.quantity)) != null)
          add(new Label(Riiablo.string.lookup("ItemStats1i") + " " + prop.value, font, Riiablo.colors.white)).center().space(SPACING).row();
        if (Item.this.type.is(Type.WEAP)) {
          add(new Label(Riiablo.string.lookup(WEAPON_DESC.get(Item.this.base.type)) + " - " + 0, font, Riiablo.colors.white)).center().space(SPACING).row();
        }
      //}

      // magic props
      if ((flags & COMPACT) == 0) {
        PropertyList magicProps = stats[MAGIC_PROPS];
        PropertyList runeProps = stats[RUNE_PROPS];
        if (magicProps != null) {
          PropertyList magicPropsAggregate = magicProps.copy();
          for (Item socket : sockets) {
            if (socket.type.is(Type.GEM) || socket.type.is(Type.RUNE)) {
              magicPropsAggregate.addAll(socket.stats[base.gemapplytype]);
            } else {
              magicPropsAggregate.addAll(socket.stats[MAGIC_PROPS]);
            }
          }
          if (runeProps != null) magicPropsAggregate.addAll(runeProps);
          magicPropsAggregate.reduce();

          Array<Stat.Instance> aggregate = magicPropsAggregate.toArray();
          aggregate.sort();
          for (Stat.Instance stat : aggregate) {
            String text = stat.format(Riiablo.charData);
            if (text == null) continue;
            add(new Label(text, font, Riiablo.colors.blue)).center().space(SPACING).row();
          }
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

      if (quality == SET) {
        SetItems.Entry setItem = Riiablo.files.SetItems.get(qualityId);
        int setId = Riiablo.files.Sets.index(setItem.set);
        int numEquipped = Riiablo.charData.getSets().get(setId, 0);
        if (numEquipped >= 2) {
          PropertyList setProps = stats[SET_PROPS + numEquipped - 2];
          Array<Stat.Instance> aggregate = setProps.toArray();
          aggregate.sort();
          for (Stat.Instance stat : aggregate) {
            String text = stat.format(Riiablo.charData);
            if (text == null) continue;
            add(new Label(text, font, Riiablo.colors.green)).center().space(SPACING).row();
          }

          Sets.Entry set = setItem.getSet();
          PropertyList setBonus = null;
          if (numEquipped == set.getItems().size) { // full set bonus
            setBonus = new PropertyList().add(set.FCode, set.FParam, set.FMin, set.FMax);
          } else { // partial set bonus
            switch (numEquipped) {
              case 2:
                setBonus = new PropertyList().add(set.PCode2, set.PParam2, set.PMin2, set.PMax2);
                break;
              case 3:
                setBonus = new PropertyList().add(set.PCode3, set.PParam3, set.PMin3, set.PMax3);
                break;
              case 4:
                setBonus = new PropertyList().add(set.PCode4, set.PParam4, set.PMin4, set.PMax4);
                break;
              case 5:
                setBonus = new PropertyList().add(set.PCode5, set.PParam5, set.PMin5, set.PMax5);
                break;
              default:
                // do nothing
            }
          }

          if (setBonus != null && setBonus.size() > 0) {
            add().height(font.getLineHeight()).space(SPACING).row();
            setBonus.reduce().toArray();
            aggregate = setBonus.toArray();
            aggregate.sort();
            for (Stat.Instance stat : aggregate) {
              String text = stat.format(Riiablo.charData);
              if (text == null) continue;
              add(new Label(text, font, Riiablo.colors.gold)).center().space(SPACING).row();
            }
          }
        }

        add().height(font.getLineHeight()).space(SPACING).row();
        Sets.Entry set = Riiablo.files.SetItems.get(qualityId).getSet();
        add(new Label(Riiablo.string.lookup(set.name), font, Riiablo.colors.gold)).space(SPACING).row();
        for (SetItems.Entry item : set.getItems()) {
          int numOwned = Riiablo.charData.getSetItems().get(Riiablo.files.SetItems.index(item.index), 0);
          Label label = new Label(Riiablo.string.lookup(item.index), font,
              numOwned > 0 ? Riiablo.colors.green : Riiablo.colors.red);
          add(label).space(SPACING).row();
        }
      }

      pack();
    }
  }
}
