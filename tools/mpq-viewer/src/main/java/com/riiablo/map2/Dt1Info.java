package com.riiablo.map2;

import com.kotcrab.vis.ui.widget.VisTable;

import com.riiablo.map.DT1;

public class Dt1Info extends VisTable {
  DT1 dt1;
  VisTable header, tileTable;

  public Dt1Info() {}

  public Dt1Info setDT1(DT1 dt1) {
    if (this.dt1 == dt1) return this;
    this.dt1 = dt1;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "tiles: ", dt1.getNumTiles());

    tileTable = new VisTable();

    VisTable left = new VisTable();
    left.add(header).row();
    left.add().growY().row();
    left.add(tileTable).row();
    add(left).growY().spaceRight(8);

    return this;
  }

  private static VisTable add(VisTable table, String label, int value) {
    return add(table, label, String.valueOf(value));
  }

  private static VisTable add(VisTable table, String label, String format, Object... args) {
    table.add(label).right();
    table.add(String.format(format, args)).left();
    table.row();
    return table;
  }

  public void update(int t) {
    DT1.Tile tile = dt1.getTile(t);

    tileTable.clear();
    tileTable.add("Tile:").left().colspan(2).row();
    add(tileTable, "width,height: ", "%d,%d", tile.width, tile.height);
  }
}
