package com.riiablo.item;

import java.lang.reflect.Field;

import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Excel;
import com.riiablo.codec.excel.ItemEntry;

public class VendorGenerator extends PassiveSystem {
  protected ItemGenerator generator;

  public Array<Item> generate(String vendor) throws Exception {
    Array<Item> items = new Array<>(true, 64, Item.class);
    generate(vendor, items, Riiablo.files.armor);
    generate(vendor, items, Riiablo.files.weapons);
    generate(vendor, items, Riiablo.files.misc);
    return items;
  }

  public void generate(String vendor, Array<Item> items, Excel<? extends ItemEntry> excel) throws Exception {
    Class<? extends ItemEntry> entryClass = excel.getEntryClass();
    Field field = entryClass.getField(vendor);
    for (ItemEntry base : excel) {
      int[] vendorData = (int[]) field.get(base);
      if (vendorData[1] > 0) {
        int count = base.PermStoreItem ? 1 : MathUtils.random(vendorData[0], vendorData[1]);
        for (int i = 0; i < count; i++) {
          Item item = generator.generate(base);
          item.flags |= Item.INSTORE;
          item.load();
          items.add(item);
        }
      }
      if (vendorData[3] > 0 && vendorData[4] != 0xFF) {
        int count = base.PermStoreItem ? 1 : MathUtils.random(vendorData[2], vendorData[3]);
        for (int i = 0; i < count; i++) {
          Item item = generator.generate(base);
          item.flags |= Item.INSTORE;
          item.load();
          item.quality = Quality.MAGIC;
          items.add(item);
        }
      }
    }
  }
}
