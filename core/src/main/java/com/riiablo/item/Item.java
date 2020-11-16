package com.riiablo.item;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;

import com.riiablo.Riiablo;
import com.riiablo.attributes.Attributes;
import com.riiablo.attributes.AttributesUpdater;
import com.riiablo.attributes.Stat;
import com.riiablo.attributes.StatListFlags;
import com.riiablo.attributes.StatListRef;
import com.riiablo.attributes.UpdateSequence;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.CharStats;
import com.riiablo.codec.excel.ItemEntry;
import com.riiablo.codec.excel.ItemTypes;
import com.riiablo.codec.excel.MagicAffix;
import com.riiablo.codec.excel.Misc;
import com.riiablo.codec.excel.SetItems;
import com.riiablo.codec.excel.UniqueItems;
import com.riiablo.codec.excel.Weapons;

public class Item {
  private static final String TAG = "Item";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_VERBOSE = DEBUG && !true;

  private static final boolean SIMPLE_FLAGS = !true;
  private static final boolean ONLY_KNOWN_FLAGS = SIMPLE_FLAGS && true;

  private static final ItemLabeler DEFAULT_LABELER = new ItemLabeler(); // TODO: inject

  public static final float ETHEREAL_ALPHA = 2 / 3f;

  public static final int VERSION_100  = 0;
  public static final int VERSION_108  = 1;
  public static final int VERSION_110  = 2;
  public static final int VERSION_108e = 100;
  public static final int VERSION_110e = 101;
  public static final int VERSION_200e = 200; // Riiablo

  // TODO: Research the unconfirmed flags (prefixed with an extra '_')
  //       Copied from another project -- appears many flags are not in the save files
  //       It's probably safe to re-purpose unused flags for this project during runtime, but some
  //         of these may indicate flags that will be needed down the line.
  public static final int ITEMFLAG__RELOAD     = 0x00000001; // Note: Updates client side stats
  public static final int ITEMFLAG__BOUGHT     = 0x00000002;
  public static final int ITEMFLAG__CURSOR     = 0x00000004;
  public static final int ITEMFLAG__IGNORE     = 0x00000008; // Note: Tells client not to reset the cursor when the update packed is received
  public static final int ITEMFLAG_IDENTIFIED  = 0x00000010;
  public static final int ITEMFLAG__REMOVED    = 0x00000020;
  public static final int ITEMFLAG__ADDED      = 0x00000040;
  public static final int ITEMFLAG__TAKEN      = 0x00000080;
  public static final int ITEMFLAG_BROKEN      = 0x00000100;
  public static final int ITEMFLAG__RESTORED   = 0x00000200;
  public static final int ITEMFLAG__SORTED     = 0x00000400;
  public static final int ITEMFLAG_SOCKETED    = 0x00000800;
  public static final int ITEMFLAG__MONSTER    = 0x00001000;
  public static final int ITEMFLAG__NEW        = 0x00002000;
  public static final int ITEMFLAG__DISABLED   = 0x00004000;
  public static final int ITEMFLAG__HARDCORE   = 0x00008000;
  public static final int ITEMFLAG_BODYPART    = 0x00010000;
  public static final int ITEMFLAG_BEGINNER    = 0x00020000;
  public static final int ITEMFLAG__RESTRICT   = 0x00040000; // Note: Blocks RELOAD, i.e., mutex with RELOAD
  public static final int ITEMFLAG__SERVER     = 0x00080000;
  public static final int ITEMFLAG__1000000    = 0x00100000;
  public static final int ITEMFLAG_COMPACT     = 0x00200000;
  public static final int ITEMFLAG_ETHEREAL    = 0x00400000;
  public static final int ITEMFLAG__SAVED      = 0x00800000;
  public static final int ITEMFLAG_INSCRIBED   = 0x01000000;
  public static final int ITEMFLAG__CRUDE      = 0x02000000;
  public static final int ITEMFLAG_RUNEWORD    = 0x04000000;
  public static final int ITEMFLAG__MAGICAL    = 0x08000000;
  public static final int ITEMFLAG__STAFFMODS  = 0x10000000; // Note: New (Unconfirmed?)
  public static final int ITEMFLAG__CURSED     = 0x20000000; // Note: New (Unconfirmed?)
  public static final int ITEMFLAG__DROW       = 0x40000000; // Note: New (Unconfirmed?)
  public static final int ITEMFLAG__TAGGED     = 0x80000000; // Note: New (Unconfirmed?) Use depends on item type

  public static final int ITEMFLAG_SAVE_MASK   = 0xFFFFFFFF; // TODO: remove flags which should not be saved

  public static final int ITEMFLAG2_INSTORE    = 0x00000001;

  public static final int NO_PICTURE_ID = -1;
  public static final int NO_CLASS_ONLY = -1;

  static final int MAGIC_AFFIX_SIZE = 11;
  static final int MAGIC_AFFIX_MASK = 0x7FF;

  static final int RARE_AFFIX_SIZE  = 8;
  static final int RARE_AFFIX_MASK  = 0xFF;

  static final int SET_ID_SIZE      = 12;
  static final int UNIQUE_ID_SIZE   = 12;

  static final Array<Item> EMPTY_SOCKETS_ARRAY = new Array<Item>(0) {
    @Override
    public void add(Item value) {
      throw new UnsupportedOperationException();
    }
  };

  // Basic fields
  public int         flags;
  public int         flags2; // riiablo-specific
  public int         version;
  public Location    location;
  public BodyLoc     bodyLoc;
  public StoreLoc    storeLoc;
  public byte        gridX;
  public byte        gridY;
  public String      code;
  public int         socketsFilled;
  public Array<Item> sockets; // derived

  public ItemEntry base;
  public ItemTypes.Entry typeEntry;
  public ItemTypes.Entry type2Entry;
  public Type type;

  // Extended fields
  public int     id;
  public byte    ilvl;
  public Quality quality;
  public byte    pictureId;
  public short   classOnly;
  public int     qualityId;
  public Object  qualityData;
  public int     runewordData;
  public String  inscription;

  public Attributes attrs;
  public int aggFlags;

  @Deprecated
  byte data[]; // TODO: refactor to act as a cache for serialized item data

  String name;
  Table details; // TODO: decouple
  Table header; // TODO: decouple
  public ItemWrapper wrapper; // TODO: decouple

  Item() {}

  @Deprecated
  public byte[] data() {
    if (data == null) throw new NullPointerException("Cannot serialize items yet!");
    return data;
  }

  void reset() {
    flags         = 0;
    version       = 0;
    location      = Location.STORED;
    bodyLoc       = BodyLoc.NONE;
    storeLoc      = StoreLoc.NONE;
    gridX         = 0;
    gridY         = 0;
    code          = "";
    socketsFilled = 0;
    sockets       = EMPTY_SOCKETS_ARRAY;

    base          = null;
    typeEntry     = null;
    type2Entry    = null;
    type          = null;

    id            = 0;
    ilvl          = 0;
    quality       = Quality.NONE;
    pictureId     = NO_PICTURE_ID;
    classOnly     = NO_CLASS_ONLY;
    qualityId     = 0;
    qualityData   = null;
    runewordData  = 0;
    inscription   = null;

    attrs = null;
    aggFlags = 0;

    name = null;
    details = null;
    header = null;
    wrapper = new ItemWrapper(this);
  }

  @SuppressWarnings("unchecked")
  public <T extends ItemEntry> T getBase() {
    return (T) base;
  }

  @SuppressWarnings("unchecked")
  public <T extends ItemEntry> T getBase(Class<T> clazz) {
    return (T) base;
  }

  public boolean isBase(Class type) {
    return base.getClass().isAssignableFrom(type);
  }

  public int getBaseIndex() {
    return ItemUtils.getBaseIndex(code);
  }

  void setBase(ItemEntry base) {
    setBase(base.code);
  }

  void setBase(String code) {
    assert base == null : "setBase called on unrecycled Item?";
    this.code = code;
    base = ItemUtils.getBase(code);
    type = Type.get(
        typeEntry  = Riiablo.files.ItemTypes.get(base.type),
        type2Entry = Riiablo.files.ItemTypes.get(base.type2));

    attrs = (flags & ITEMFLAG_COMPACT) == ITEMFLAG_COMPACT
        ? Attributes.obtainCompact()
        : Attributes.obtainStandard();
    StatListRef baseProps = attrs.base();
    baseProps.put(Stat.item_levelreq, base.levelreq);
    switch (getBaseType()) {
      case WEAPON: {
        Weapons.Entry weapon = getBase();
        baseProps.put(Stat.mindamage, weapon.mindam);
        baseProps.put(Stat.maxdamage, weapon.maxdam);
        baseProps.put(Stat.secondary_mindamage, weapon._2handmindam);
        baseProps.put(Stat.secondary_maxdamage, weapon._2handmaxdam);
        baseProps.put(Stat.item_throw_mindamage, weapon.minmisdam);
        baseProps.put(Stat.item_throw_maxdamage, weapon.maxmisdam);
        baseProps.put(Stat.reqstr, weapon.reqstr);
        baseProps.put(Stat.reqdex, weapon.reqdex);
        break;
      }
      case ARMOR: {
        Armor.Entry armor = getBase();
        baseProps.put(Stat.reqstr, armor.reqstr);
        baseProps.put(Stat.reqdex, 0);
        baseProps.put(Stat.toblock, armor.block); // FIXME: apply Riiablo.charData.getCharacterClass().entry().BlockFactor for view stats
        baseProps.put(Stat.mindamage, armor.mindam);
        baseProps.put(Stat.maxdamage, armor.maxdam);
        break;
      }
      case MISC: {
        Misc.Entry misc = getBase();
        break;
      }
      default: throw new AssertionError();
    }
    // TODO: copy base item stats
  }

  void setEar(int charClass, int charLevel, String charName) {
    setBase("ear");
    flags |= ITEMFLAG_BODYPART;
    qualityId   = charClass;
    qualityData = charLevel;
    inscription = charName;
  }

  public boolean hasFlag(int flag) {
    return (flags & flag) == flag;
  }

  public boolean isCompact() {
    return hasFlag(ITEMFLAG_COMPACT);
  }

  public boolean isIdentified() {
    return hasFlag(ITEMFLAG_IDENTIFIED);
  }

  public boolean isEthereal() {
    return hasFlag(ITEMFLAG_ETHEREAL);
  }

  public boolean hasFlag2(int flag) {
    return (flags2 & flag) == flag;
  }

  enum ItemEntryType { WEAPON, ARMOR, MISC }
  public ItemEntryType getBaseType() {
    if (base instanceof Weapons.Entry) {
      return ItemEntryType.WEAPON;
    } else if (base instanceof Armor.Entry) {
      return ItemEntryType.ARMOR;
    } else {
      assert base instanceof Misc.Entry;
      return ItemEntryType.MISC;
    }
  }

  public String getInvFileName() {
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
        int prefix = qualityId & Item.MAGIC_AFFIX_MASK;
        if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null && affix.transform)
          return affix.transformcolor;
        int suffix = qualityId >>> Item.MAGIC_AFFIX_SIZE;
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
        int prefix = qualityId & Item.MAGIC_AFFIX_MASK;
        if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null && affix.transform)
          return affix.transformcolor;
        int suffix = qualityId >>> Item.MAGIC_AFFIX_SIZE;
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

  String getFlagsString() {
    StringBuilder builder = new StringBuilder();
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__RELOAD   ) == ITEMFLAG__RELOAD   ) builder.append("ITEMFLAG__RELOAD"   ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__BOUGHT   ) == ITEMFLAG__BOUGHT   ) builder.append("ITEMFLAG__BOUGHT"   ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__CURSOR   ) == ITEMFLAG__CURSOR   ) builder.append("ITEMFLAG__CURSOR"   ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__IGNORE   ) == ITEMFLAG__IGNORE   ) builder.append("ITEMFLAG__IGNORE"   ).append('|');
    if ((flags & ITEMFLAG_IDENTIFIED) == ITEMFLAG_IDENTIFIED) builder.append("ITEMFLAG_IDENTIFIED").append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__REMOVED  ) == ITEMFLAG__REMOVED  ) builder.append("ITEMFLAG__REMOVED"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__ADDED    ) == ITEMFLAG__ADDED    ) builder.append("ITEMFLAG__ADDED"    ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__TAKEN    ) == ITEMFLAG__TAKEN    ) builder.append("ITEMFLAG__TAKEN"    ).append('|');
    if ((flags & ITEMFLAG_BROKEN    ) == ITEMFLAG_BROKEN    ) builder.append("ITEMFLAG_BROKEN"    ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__RESTORED ) == ITEMFLAG__RESTORED ) builder.append("ITEMFLAG__RESTORED" ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__SORTED   ) == ITEMFLAG__SORTED   ) builder.append("ITEMFLAG__SORTED"   ).append('|');
    if ((flags & ITEMFLAG_SOCKETED  ) == ITEMFLAG_SOCKETED  ) builder.append("ITEMFLAG_SOCKETED"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__MONSTER  ) == ITEMFLAG__MONSTER  ) builder.append("ITEMFLAG__MONSTER"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__NEW      ) == ITEMFLAG__NEW      ) builder.append("ITEMFLAG__NEW"      ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__DISABLED ) == ITEMFLAG__DISABLED ) builder.append("ITEMFLAG__DISABLED" ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__HARDCORE ) == ITEMFLAG__HARDCORE ) builder.append("ITEMFLAG__HARDCORE" ).append('|');
    if ((flags & ITEMFLAG_BODYPART  ) == ITEMFLAG_BODYPART  ) builder.append("ITEMFLAG_BODYPART"  ).append('|');
    if ((flags & ITEMFLAG_BEGINNER  ) == ITEMFLAG_BEGINNER  ) builder.append("ITEMFLAG_BEGINNER"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__RESTRICT ) == ITEMFLAG__RESTRICT ) builder.append("ITEMFLAG__RESTRICT" ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__SERVER   ) == ITEMFLAG__SERVER   ) builder.append("ITEMFLAG__SERVER"   ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__1000000  ) == ITEMFLAG__1000000  ) builder.append("ITEMFLAG__1000000"  ).append('|');
    if ((flags & ITEMFLAG_COMPACT   ) == ITEMFLAG_COMPACT   ) builder.append("ITEMFLAG_COMPACT"   ).append('|');
    if ((flags & ITEMFLAG_ETHEREAL  ) == ITEMFLAG_ETHEREAL  ) builder.append("ITEMFLAG_ETHEREAL"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__SAVED    ) == ITEMFLAG__SAVED    ) builder.append("ITEMFLAG__SAVED"    ).append('|');
    if ((flags & ITEMFLAG_INSCRIBED ) == ITEMFLAG_INSCRIBED ) builder.append("ITEMFLAG_INSCRIBED" ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__CRUDE    ) == ITEMFLAG__CRUDE    ) builder.append("ITEMFLAG__CRUDE"    ).append('|');
    if ((flags & ITEMFLAG_RUNEWORD  ) == ITEMFLAG_RUNEWORD  ) builder.append("ITEMFLAG_RUNEWORD"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__MAGICAL  ) == ITEMFLAG__MAGICAL  ) builder.append("ITEMFLAG__MAGICAL"  ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__STAFFMODS) == ITEMFLAG__STAFFMODS) builder.append("ITEMFLAG__STAFFMODS").append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__CURSED   ) == ITEMFLAG__CURSED   ) builder.append("ITEMFLAG__CURSED"   ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__DROW     ) == ITEMFLAG__DROW     ) builder.append("ITEMFLAG__DROW"     ).append('|');
    if (ONLY_KNOWN_FLAGS && (flags & ITEMFLAG__TAGGED   ) == ITEMFLAG__TAGGED   ) builder.append("ITEMFLAG__TAGGED"   ).append('|');
    if (builder.length() > 0) builder.setLength(builder.length() - 1);
    return builder.toString();
  }

  public String getNameString() {
    if (name == null) updateName();
    return name;
  }

  private void updateName() {
    StringBuilder name = new StringBuilder();
    int prefix, suffix;
    MagicAffix affix;
    switch (quality) {
      case LOW:
      case NORMAL:
      case HIGH:
        if ((flags & ITEMFLAG_RUNEWORD) == ITEMFLAG_RUNEWORD) {
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
        if (qualityId != (1 << SET_ID_SIZE) - 1) {
          name.append(Riiablo.string.lookup(Riiablo.files.SetItems.get(qualityId).index));
        } else {
          name.append(Riiablo.string.lookup(base.namestr));
        }
        break;

      case UNIQUE:
        if (qualityId != (1 << UNIQUE_ID_SIZE) - 1) {
          name.append(Riiablo.string.lookup(Riiablo.files.UniqueItems.get(qualityId).index));
        } else {
          name.append(Riiablo.string.lookup(base.namestr));
        }
        break;

      default:
        name.append(Riiablo.string.lookup(base.namestr));
    }

    this.name = name.toString();
  }

  // TODO: support width/height also to check full collision rect
  public boolean contains(int x, int y) {
    x -= gridX;
    y -= gridY;
    return 0 <= x && x < base.invwidth
        && 0 <= y && y < base.invheight;
  }

  public void update(AttributesUpdater updater, Attributes opBase, CharStats.Entry charStats, IntIntMap equippedSets) {
    if (type.is(Type.GEM) || type.is(Type.RUNE)) {
      updater.update(attrs, aggFlags, opBase, charStats).apply();
      return;
    } else if ((flags & ITEMFLAG_COMPACT) == ITEMFLAG_COMPACT) {
      return;
    }

    final int numEquippedSets;
    if (quality == Quality.SET && location == Location.EQUIPPED) {
      SetItems.Entry setItem = (SetItems.Entry) qualityData;
      int setId = Riiablo.files.Sets.index(setItem.set);
      numEquippedSets = equippedSets.get(setId, 0);
    } else {
      numEquippedSets = 0;
    }

    aggFlags = StatListFlags.getAggItemFlags(aggFlags, numEquippedSets);
    final UpdateSequence update = updater.update(attrs, aggFlags, opBase, charStats);

    for (Item socket : sockets) {
      if (socket.type.is(Type.GEM) || socket.type.is(Type.RUNE)) {
        socket.aggFlags = (1 << base.gemapplytype); // TODO: set this when loaded or (un)socketed and leave below assertion
      }

      assert !(socket.type.is(Type.GEM) || socket.type.is(Type.RUNE)) || socket.aggFlags == (1 << base.gemapplytype)
          : "socket.aggFlags(" + socket.aggFlags + ") does not match base.gemApplyType(" + base.gemapplytype + ")";
      socket.update(updater, attrs, charStats, equippedSets);
      update.add(socket.attrs.remaining());
    }

    update.apply();
  }

  // loads client-side resources
  public void load() {
    wrapper.load();
  }

  public float getX() {
    return wrapper.getX();
  }

  public float getY() {
    return wrapper.getY();
  }

  public float getWidth() {
    return wrapper.getWidth();
  }

  public float getHeight() {
    return wrapper.getHeight();
  }

  public void draw(Batch b, float a) {
    wrapper.draw(b, a);
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this)
        .append("name", getNameString())
        .append("code", code)
        .append("flags", SIMPLE_FLAGS ? getFlagsString() : String.format("0x%08x", flags))
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
          .append("ilvl", ilvl)
          .append("quality", quality)
          .append("pictureId", pictureId)
          .append("classOnly", String.format("0x%04X", classOnly))
          .append("qualityId", String.format("0x%08X", qualityId))
          .append("qualityData", qualityData)
          .append("runewordData", String.format("0x%04X", runewordData))
          .append("inscription", inscription)
          .append("sockets", sockets)
          .append("attrs", attrs)
          ;
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

      if ((flags & ITEMFLAG_COMPACT) == 0) {
        builder
            .append("id", String.format("0x%08X", (int) id))
            .append("ilvl", ilvl)
            .append("quality", quality);
        if (pictureId >= 0) builder.append("pictureId", pictureId);
        if (classOnly >= 0) builder.append("classOnly", String.format("0x%04X", classOnly));
        switch (quality) {
          case LOW:
            builder.append("qualityId", LowQuality.valueOf(qualityId));
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

        if ((flags & ITEMFLAG_RUNEWORD) == ITEMFLAG_RUNEWORD) {
          builder.append("runewordData", String.format("[id=%d, extra=%d]",
              RunewordData.id(runewordData), RunewordData.extra(runewordData)));
        }

        if ((flags & ITEMFLAG_INSCRIBED) == ITEMFLAG_INSCRIBED) {
          builder.append("inscription", inscription);
        }

        if ((flags & ITEMFLAG_SOCKETED) == ITEMFLAG_SOCKETED && socketsFilled > 0) {
          builder.append("sockets", sockets);
        }

        builder.append("attrs", attrs);
      }
    }
    return builder.build();
  }

  public Table details(AttributesUpdater updater) {
    if (details == null) {
      // TODO: use item parent itemdata for equipped set counter
      update(updater, Riiablo.charData.getStats(), Riiablo.charData.classId.entry(), Riiablo.charData.getItems().getEquippedSets());
      details = DEFAULT_LABELER.updateLabel(this, new Table(), 0);
    }

    return details;
  }

  public Table header() {
    if (header == null) {
      header = DEFAULT_LABELER.updateHeader(this, new Table());
    }

    return header;
  }
}
