package gdx.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectSet;

import gdx.diablo.codec.TXT;
import gdx.diablo.codec.excel.ArmType;
import gdx.diablo.codec.excel.Armor;
import gdx.diablo.codec.excel.BodyLocs;
import gdx.diablo.codec.excel.Colors;
import gdx.diablo.codec.excel.Composit;
import gdx.diablo.codec.excel.Excel;
import gdx.diablo.codec.excel.Inventory;
import gdx.diablo.codec.excel.ItemStatCost;
import gdx.diablo.codec.excel.ItemTypes;
import gdx.diablo.codec.excel.Levels;
import gdx.diablo.codec.excel.LowQualityItems;
import gdx.diablo.codec.excel.LvlPrest;
import gdx.diablo.codec.excel.LvlTypes;
import gdx.diablo.codec.excel.MagicPrefix;
import gdx.diablo.codec.excel.MagicSuffix;
import gdx.diablo.codec.excel.Misc;
import gdx.diablo.codec.excel.MonMode;
import gdx.diablo.codec.excel.MonStats;
import gdx.diablo.codec.excel.MonStats2;
import gdx.diablo.codec.excel.Obj;
import gdx.diablo.codec.excel.ObjMode;
import gdx.diablo.codec.excel.Objects;
import gdx.diablo.codec.excel.PlrMode;
import gdx.diablo.codec.excel.PlrType;
import gdx.diablo.codec.excel.QualityItems;
import gdx.diablo.codec.excel.RarePrefix;
import gdx.diablo.codec.excel.RareSuffix;
import gdx.diablo.codec.excel.Runes;
import gdx.diablo.codec.excel.SetItems;
import gdx.diablo.codec.excel.Sounds;
import gdx.diablo.codec.excel.UniqueItems;
import gdx.diablo.codec.excel.WeaponClass;
import gdx.diablo.codec.excel.Weapons;

public class Files {
  public final Obj obj;

  public final Armor        armor;
  public final ArmType      ArmType;
  public final BodyLocs     bodylocs;
  public final Colors       colors;
  public final Composit     Composit;
  public final Inventory    inventory;
  public final ItemStatCost ItemStatCost;
  public final ItemTypes    ItemTypes;
  public final Levels       Levels;
  public final LowQualityItems LowQualityItems;
  public final LvlPrest     LvlPrest;
  public final LvlTypes     LvlTypes;
  public final Misc         misc;
  public final MagicPrefix  MagicPrefix;
  public final MagicSuffix  MagicSuffix;
  public final MonMode      MonMode;
  public final MonStats     monstats;
  public final MonStats2    monstats2;
  public final Objects      objects;
  public final ObjMode      ObjMode;
  public final PlrMode      PlrMode;
  public final PlrType      PlrType;
  public final QualityItems QualityItems;
  public final RarePrefix   RarePrefix;
  public final RareSuffix   RareSuffix;
  public final Runes        Runes;
  public final SetItems     SetItems;
  public final Sounds       Sounds;
  public final UniqueItems  UniqueItems;
  //public final UniquePrefix UniquePrefix;
  //public final UniqueSuffix UniqueSuffix;
  public final WeaponClass  WeaponClass;
  public final Weapons      weapons;

  public Files(AssetManager assets) {
    obj      = loadObj();

    armor        = load(assets, Armor.class);
    ArmType      = load(assets, ArmType.class);
    bodylocs     = load(assets, BodyLocs.class);
    colors       = load(assets, Colors.class);
    Composit     = load(assets, Composit.class);
    inventory    = load(assets, Inventory.class);
    ItemStatCost = load(assets, ItemStatCost.class);
    ItemTypes    = load(assets, ItemTypes.class);
    Levels       = load(assets, Levels.class);
    LowQualityItems = load(assets, LowQualityItems.class);
    LvlPrest     = load(assets, LvlPrest.class);
    LvlTypes     = load(assets, LvlTypes.class);
    misc         = load(assets, Misc.class);
    MagicPrefix  = load(assets, MagicPrefix.class, Excel.EXPANSION);
    MagicSuffix  = load(assets, MagicSuffix.class, Excel.EXPANSION);
    MonMode      = load(assets, MonMode.class);
    monstats     = load(assets, MonStats.class, Excel.EXPANSION);
    monstats2    = load(assets, MonStats2.class, Excel.EXPANSION);
    RarePrefix   = load(assets, RarePrefix.class, Excel.EXPANSION);
    RareSuffix   = load(assets, RareSuffix.class, Excel.EXPANSION);
    Runes        = load(assets, Runes.class);
    objects      = load(assets, Objects.class);
    ObjMode      = load(assets, ObjMode.class);
    PlrMode      = load(assets, PlrMode.class);
    PlrType      = load(assets, PlrType.class);
    QualityItems = load(assets, QualityItems.class);
    SetItems     = load(assets, SetItems.class, Excel.EXPANSION);
    Sounds       = load(assets, Sounds.class);
    UniqueItems  = load(assets, UniqueItems.class, Excel.EXPANSION);
    //UniquePrefix = load(assets, UniquePrefix.class);
    //UniqueSuffix = load(assets, UniqueSuffix.class);
    WeaponClass  = load(assets, WeaponClass.class);
    weapons      = load(assets, Weapons.class);
  }

  private Obj loadObj() {
    FileHandle handle = Gdx.files.internal("data/obj.txt");
    TXT txt = TXT.loadFromFile(handle);
    return Excel.parse(txt, Obj.class);
  }

  private <T extends Excel> T load(AssetManager assets, Class<T> clazz, ObjectSet<String> ignore) {
    return load(assets, clazz, clazz.getSimpleName(), ignore);
  }

  private <T extends Excel> T load(AssetManager assets, Class<T> clazz) {
    return load(assets, clazz, clazz.getSimpleName(), Excel.<String>emptySet());
  }

  private <T extends Excel> T load(AssetManager assets, Class<T> clazz, String tableName, ObjectSet<String> ignore) {
    FileHandle handle = Diablo.mpqs.resolve("data\\global\\excel\\" + tableName + ".txt");
    TXT txt = TXT.loadFromFile(handle);
    return Excel.parse(txt, clazz, ignore);
  }
}
