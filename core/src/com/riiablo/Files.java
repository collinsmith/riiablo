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
  public final MagicPrefix      MagicPrefix;
  public final MagicSuffix      MagicSuffix;
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

  public Files(AssetManager assets) {
    obj    = loadInternal(Obj.class);
    speech = loadInternal(Speech.class);
    quests = loadInternal(Quests.class);

    armor            = load(assets, Armor.class, Excel.EXPANSION);
    ArmType          = load(assets, ArmType.class);
    bodylocs         = load(assets, BodyLocs.class);
    CharStats        = load(assets, CharStats.class, Excel.EXPANSION);
    colors           = load(assets, Colors.class);
    Composit         = load(assets, Composit.class);
    compcode         = load(assets, CompCode.class);
    DifficultyLevels = load(assets, DifficultyLevels.class);
    Gems             = load(assets, Gems.class, Excel.EXPANSION);
    inventory        = load(assets, Inventory.class);
    ItemStatCost     = load(assets, ItemStatCost.class);
    ItemTypes        = load(assets, ItemTypes.class);
    Levels           = load(assets, Levels.class, Excel.EXPANSION);
    LowQualityItems  = load(assets, LowQualityItems.class);
    LvlPrest         = load(assets, LvlPrest.class);
    LvlTypes         = load(assets, LvlTypes.class);
    LvlWarp          = load(assets, LvlWarp.class, Excel.EXPANSION);
    misc             = load(assets, Misc.class, Excel.EXPANSION);
    MagicPrefix      = load(assets, MagicPrefix.class, Excel.EXPANSION);
    MagicSuffix      = load(assets, MagicSuffix.class, Excel.EXPANSION);
    MonMode          = load(assets, MonMode.class);
    monstats         = load(assets, MonStats.class, Excel.EXPANSION);
    monstats2        = load(assets, MonStats2.class, Excel.EXPANSION);
    RarePrefix       = load(assets, RarePrefix.class, Excel.EXPANSION);
    RareSuffix       = load(assets, RareSuffix.class, Excel.EXPANSION);
    Runes            = load(assets, Runes.class);
    objects          = load(assets, Objects.class);
    ObjMode          = load(assets, ObjMode.class);
    Overlay          = load(assets, Overlay.class, Excel.EXPANSION);
    PlrMode          = load(assets, PlrMode.class);
    PlrType          = load(assets, PlrType.class);
    Properties       = load(assets, Properties.class, Excel.EXPANSION);
    QualityItems     = load(assets, QualityItems.class);
    Sets             = load(assets, Sets.class, Excel.EXPANSION);
    SetItems         = load(assets, SetItems.class, Excel.EXPANSION);
    skills           = load(assets, Skills.class);
    skilldesc        = load(assets, SkillDesc.class);
    Sounds           = load(assets, Sounds.class);
    UniqueItems      = load(assets, UniqueItems.class, Excel.EXPANSION);
    //UniquePrefix   = load(assets, UniquePrefix.class);
    //UniqueSuffix   = load(assets, UniqueSuffix.class);
    WeaponClass      = load(assets, WeaponClass.class);
    weapons          = load(assets, Weapons.class, Excel.EXPANSION);

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

  private <T extends Excel> T load(AssetManager assets, Class<T> clazz, ObjectSet<String> ignore) {
    return load(assets, clazz, clazz.getSimpleName(), ignore);
  }

  private <T extends Excel> T load(AssetManager assets, Class<T> clazz) {
    return load(assets, clazz, clazz.getSimpleName(), Excel.<String>emptySet());
  }

  private <T extends Excel> T load(AssetManager assets, Class<T> clazz, String tableName, ObjectSet<String> ignore) {
    FileHandle handle = Riiablo.mpqs.resolve("data\\global\\excel\\" + tableName + ".txt");
    TXT txt = TXT.loadFromFile(handle);
    return Excel.parse(txt, clazz, ignore);
  }
}
