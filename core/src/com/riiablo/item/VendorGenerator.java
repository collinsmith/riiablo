package com.riiablo.item;

import java.lang.reflect.Field;

import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import com.riiablo.codec.excel.Excel;
import com.riiablo.codec.excel.ItemEntry;

public class VendorGenerator extends PassiveSystem {
  public ItemGenerator generator;

  public void generate(String vendor, Array<Item> items, Excel<? extends ItemEntry> excel) throws Exception {
    Class<? extends ItemEntry> entryClass = excel.getEntryClass();
    Field field = entryClass.getField(vendor);

    items.clear();
    for (ItemEntry base : excel) {
      int[] vendorData = (int[]) field.get(base);
      if (vendorData[1] > 0) {
        int count = base.PermStoreItem ? 1 : MathUtils.random(vendorData[0], vendorData[1]);
        for (int i = 0; i < count; i++) {
          Item item = generator.generate(base);
          item.load();
          items.add(item);
        }
      }
      if (vendorData[3] > 0) {
        int count = base.PermStoreItem ? 1 : MathUtils.random(vendorData[2], vendorData[3]);
        for (int i = 0; i < count; i++) {
          Item item = generator.generate(base);
          item.load();
          item.quality = Quality.MAGIC;
          items.add(item);
        }
      }
    }
  }
}
