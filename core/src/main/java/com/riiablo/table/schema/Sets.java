package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Sets {
  // TODO:
  // private final IntMap<Array<SetItems>> ITEMS = new IntMap<>();
  //
  // public void index(Table<SetItems> items) {
  //   assert ITEMS.size == 0 : "Illegal state -- ITEM has already been indexed";
  //   for (SetItems item : items) {
  //     int id = index(item.set);
  //     Sets entry = item.parentSet = get(id);
  //     Array<SetItems.Entry> setItems = ITEMS.get(id);
  //     if (setItems == null) ITEMS.put(id, setItems = entry.items = new Array<>(6));
  //     setItems.add(item);
  //   }
  // }
  //
  // public Array<SetItems> getItems() {
  //   return items;
  // }
  //
  // Array<SetItems> items;

  @Override
  public String toString() {
    return index;
  }

  @PrimaryKey
  public String index;

  public String name;
  public int version;
  public int level;

  @Format(
      format = "PCode2%s",
      endIndex = 2,
      values = {"a", "b"})
  public String PCode2[];

  @Format(
      format = "PParam2%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PParam2[];

  @Format(
      format = "PMin2%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PMin2[];

  @Format(
      format = "PMax2%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PMax2[];

  @Format(
      format = "PCode3%s",
      endIndex = 2,
      values = {"a", "b"})
  public String PCode3[];

  @Format(
      format = "PParam3%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PParam3[];

  @Format(
      format = "PMin3%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PMin3[];

  @Format(
      format = "PMax3%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PMax3[];

  @Format(
      format = "PCode4%s",
      endIndex = 2,
      values = {"a", "b"})
  public String PCode4[];

  @Format(
      format = "PParam4%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PParam4[];

  @Format(
      format = "PMin4%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PMin4[];

  @Format(
      format = "PMax4%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PMax4[];

  @Format(
      format = "PCode5%s",
      endIndex = 2,
      values = {"a", "b"})
  public String PCode5[];

  @Format(
      format = "PParam5%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PParam5[];

  @Format(
      format = "PMin5%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PMin5[];

  @Format(
      format = "PMax5%s",
      endIndex = 2,
      values = {"a", "b"})
  public int PMax5[];

  @Format(
      startIndex = 1,
      endIndex = 9)
  public String FCode[];

  @Format(
      startIndex = 1,
      endIndex = 9)
  public int FParam[];

  @Format(
      startIndex = 1,
      endIndex = 9)
  public int FMin[];

  @Format(
      startIndex = 1,
      endIndex = 9)
  public int FMax[];
}
