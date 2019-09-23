package com.riiablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectSet;
import com.riiablo.codec.TXT;
import com.riiablo.codec.excel.ArmType;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.BodyLocs;
import com.riiablo.codec.excel.CharStats;
import com.riiablo.codec.excel.Colors;
import com.riiablo.codec.excel.CompCode;
import com.riiablo.codec.excel.Composit;
import com.riiablo.codec.excel.DifficultyLevels;
import com.riiablo.codec.excel.Excel;
import com.riiablo.codec.excel.Gems;
import com.riiablo.codec.excel.Inventory;
import com.riiablo.codec.excel.ItemStatCost;
import com.riiablo.codec.excel.ItemTypes;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LowQualityItems;
import com.riiablo.codec.excel.LvlPrest;
import com.riiablo.codec.excel.LvlTypes;
import com.riiablo.codec.excel.LvlWarp;
import com.riiablo.codec.excel.MagicPrefix;
import com.riiablo.codec.excel.MagicSuffix;
import com.riiablo.codec.excel.Misc;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.codec.excel.MonAI;
import com.riiablo.codec.excel.MonMode;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.MonStats2;
import com.riiablo.codec.excel.Obj;
import com.riiablo.codec.excel.ObjMode;
import com.riiablo.codec.excel.Objects;
import com.riiablo.codec.excel.Overlay;
import com.riiablo.codec.excel.PlrMode;
import com.riiablo.codec.excel.PlrType;
import com.riiablo.codec.excel.Properties;
import com.riiablo.codec.excel.QualityItems;
import com.riiablo.codec.excel.Quests;
import com.riiablo.codec.excel.RarePrefix;
import com.riiablo.codec.excel.RareSuffix;
import com.riiablo.codec.excel.Runes;
import com.riiablo.codec.excel.SetItems;
import com.riiablo.codec.excel.Sets;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.codec.excel.Sounds;
import com.riiablo.codec.excel.Speech;
import com.riiablo.codec.excel.UniqueItems;
import com.riiablo.codec.excel.WeaponClass;
import com.riiablo.codec.excel.Weapons;

public class Files {
  private static final String TAG = "Files";
  private static final String EXCEL_PATH = "data\\global\\excel\\";

  public final Obj    obj;
  public final Speech speech;
  public final Quests quests;

  public final Armor            armor;
  public final ArmType          ArmType;
  public final BodyLocs         bodylocs;
  public final CharStats        CharStats;
  public final Colors           colors;
  public final Composit         Composit;
  public final CompCode         compcode;
  public final DifficultyLevels DifficultyLevels;
  public final Gems             Gems;
  public final Inventory        inventory;
  public final ItemStatCost     ItemStatCost;
  public final ItemTypes        ItemTypes;
  public final Levels           Levels;
  public final LowQualityItems  LowQualityItems;
  public final LvlPrest         LvlPrest;
  public final LvlTypes         LvlTypes;
  public final LvlWarp          LvlWarp;
  public final Misc             misc;
  public final Missiles         Missiles;
  public final MagicPrefix      MagicPrefix;
  public final MagicSuffix      MagicSuffix;
  public final MonAI            MonAI;
  public final MonMode          MonMode;
  public final MonStats         monstats;
  public final MonStats2        monstats2;
  public final Objects          objects;
  public final ObjMode          ObjMode;
  public final Overlay          Overlay;
  public final PlrMode          PlrMode;
  public final PlrType          PlrType;
  public final Properties       Properties;
  public final QualityItems     QualityItems;
  public final RarePrefix       RarePrefix;
  public final RareSuffix       RareSuffix;
  public final Runes            Runes;
  public final Sets             Sets;
  public final SetItems         SetItems;
  public final Skills           skills;
  public final SkillDesc        skilldesc;
  public final Sounds           Sounds;
  public final UniqueItems      UniqueItems;
  //public final UniquePrefix   UniquePrefix;
  //public final UniqueSuffix   UniqueSuffix;
  public final WeaponClass      WeaponClass;
  public final Weapons          weapons;

  public Files() {
    this(null);
  }

  // TODO: refactor the removal of this constructor throughout project
  public Files(AssetManager assets) {
    long start = System.currentTimeMillis();
    obj    = loadInternal(Obj.class);
    speech = loadInternal(Speech.class);
    quests = loadInternal(Quests.class);

    armor            = load(Armor.class, Excel.EXPANSION);
    ArmType          = load(ArmType.class);
    bodylocs         = load(BodyLocs.class);
    CharStats        = load(CharStats.class, Excel.EXPANSION);
    colors           = load(Colors.class);
    Composit         = load(Composit.class);
    compcode         = load(CompCode.class);
    DifficultyLevels = load(DifficultyLevels.class);
    Gems             = load(Gems.class, Excel.EXPANSION);
    inventory        = load(Inventory.class);
    ItemStatCost     = load(ItemStatCost.class);
    ItemTypes        = load(ItemTypes.class);
    Levels           = load(Levels.class, Excel.EXPANSION);
    LowQualityItems  = load(LowQualityItems.class);
    LvlPrest         = load(LvlPrest.class);
    LvlTypes         = load(LvlTypes.class);
    LvlWarp          = load(LvlWarp.class, Excel.EXPANSION);
    misc             = load(Misc.class, Excel.EXPANSION);
    Missiles         = load(Missiles.class);
    MagicPrefix      = load(MagicPrefix.class, Excel.EXPANSION);
    MagicSuffix      = load(MagicSuffix.class, Excel.EXPANSION);
    MonAI            = load(MonAI.class);
    MonMode          = load(MonMode.class);
    monstats         = load2(MonStats.class, Excel.EXPANSION);
    monstats2        = load(MonStats2.class, Excel.EXPANSION);
    RarePrefix       = load(RarePrefix.class, Excel.EXPANSION);
    RareSuffix       = load(RareSuffix.class, Excel.EXPANSION);
    Runes            = load(Runes.class);
    objects          = load(Objects.class);
    ObjMode          = load(ObjMode.class);
    Overlay          = load(Overlay.class, Excel.EXPANSION);
    PlrMode          = load(PlrMode.class);
    PlrType          = load(PlrType.class);
    Properties       = load(Properties.class, Excel.EXPANSION);
    QualityItems     = load(QualityItems.class);
    Sets             = load(Sets.class, Excel.EXPANSION);
    SetItems         = load(SetItems.class, Excel.EXPANSION);
    skills           = load(Skills.class);
    skilldesc        = load(SkillDesc.class);
    Sounds           = load(Sounds.class);
    UniqueItems      = load(UniqueItems.class, Excel.EXPANSION);
    //UniquePrefix   = load(UniquePrefix.class);
    //UniqueSuffix   = load(UniqueSuffix.class);
    WeaponClass      = load(WeaponClass.class);
    weapons          = load(Weapons.class, Excel.EXPANSION);

    long end = System.currentTimeMillis();
    Gdx.app.debug(TAG, "Loaded files in " + (end - start) + "ms");

    Sets.index(SetItems);
  }

  private <T extends Excel> T loadInternal(Class<T> clazz) {
    return loadInternal(clazz, clazz.getSimpleName().toLowerCase());
  }

  private <T extends Excel> T loadInternal(Class<T> clazz, String filename) {
    FileHandle handle = Gdx.files.internal("data/" + filename + ".txt");
    TXT txt = TXT.loadFromFile(handle);
    return Excel.parse(txt, clazz);
  }

  private <T extends Excel> T load(Class<T> clazz, ObjectSet<String> ignore) {
    return load(clazz, clazz.getSimpleName(), ignore);
  }

  private <T extends Excel> T load(Class<T> clazz) {
    return load(clazz, clazz.getSimpleName(), Excel.<String>emptySet());
  }

  private <T extends Excel> T load(Class<T> clazz, String tableName, ObjectSet<String> ignore) {
    FileHandle handle = Riiablo.mpqs.resolve(EXCEL_PATH + tableName + ".txt");
    TXT txt = TXT.loadFromFile(handle);
    return Excel.parse(txt, clazz, ignore);
  }
}
