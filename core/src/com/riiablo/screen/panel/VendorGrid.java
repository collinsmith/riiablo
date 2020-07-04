package com.riiablo.screen.panel;

import com.riiablo.codec.excel.Inventory;
import com.riiablo.item.Item;

public class VendorGrid extends ItemGrid {
  private static final String TAG = "VendorGrid";

  public VendorGrid(Inventory.Entry inv, GridListener gridListener) {
    super(inv, gridListener);
    showFill = false;
  }

  @Override
  protected boolean accept(Item item) {
    return false;
  }
}
