package com.riiablo.file;

import com.kotcrab.vis.ui.widget.VisTable;
import org.apache.commons.lang3.ArrayUtils;

import com.riiablo.map5.Dt1;
import com.riiablo.map5.Orientation;
import com.riiablo.map5.Tile;
import com.riiablo.util.DebugUtils;

public class Dt1Info extends VisTable {
  Dt1 dt1;
  VisTable header, tileTable, tileTable2, tileFlags;

  public Dt1Info() {}

  public Dt1Info setDt1(Dt1 dt1) {
    if (this.dt1 == dt1) return this;
    this.dt1 = dt1;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "version: ", dt1.version());
    add(header, "flags", "0x%08x", dt1.flags());
    add(header, "tiles: ", dt1.numTiles());
    add(header, "data offset: ", "+0x%08x", dt1.tileOffset());

    tileFlags = new VisTable();

    VisTable left = new VisTable();
    left.add(header).row();
    left.add().growY().row();
    left.add(tileFlags).growX().row();
    add(left).growY().spaceRight(8);

    tileTable = new VisTable();
    add(tileTable).growY().spaceRight(8);

    tileTable2 = new VisTable();

    VisTable right = new VisTable();
    right.add(tileTable2).row();
    right.add().growY().row();
    add(right).growY().spaceRight(8);

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
    if (dt1 == null) return;
    tileTable.clear();
    tileTable2.clear();
    tileFlags.clear();
    final Tile tile = dt1.get(t);
    if (tile == null) return;

    // tileTable.clear();
    tileTable.add("Tile:").left().colspan(2).row();
    add(tileTable, "tileIndex: ", "0x%08x", tile.tileIndex());
    add(tileTable, "mainIndex: ", tile.mainIndex);
    add(tileTable, "subIndex: ", tile.subIndex);
    add(tileTable, "orientation: ", "%d (%s)", tile.orientation, Orientation.toString(tile.orientation));
    add(tileTable, "light direction: ", "%d (%s)", tile.lightDirection, Orientation.directionToString(tile.lightDirection));
    add(tileTable, "width,height: ", "%d,%d", tile.width, tile.height);
    add(tileTable, "texture: ", "%dx%d", tile.textureWidth(), tile.textureHeight());
    add(tileTable, "tile: ", "green");
    add(tileTable, "texture: ", "red");
    add(tileTable, "bbox: ", "white");
    tileTable.add().colspan(2).growY().row();

    // tileTable2.clear();
    tileTable2.add("").left().colspan(2).row();
    add(tileTable2, "materialFlags: ", "0x04d", tile.materialFlags);
    add(tileTable2, "materials: ", "");
    if ((tile.materialFlags & Tile.MATERIAL_OTHER) != 0)    add(tileTable2, "", "MATERIAL_OTHER");
    if ((tile.materialFlags & Tile.MATERIAL_WATER) != 0)    add(tileTable2, "", "MATERIAL_WATER");
    if ((tile.materialFlags & Tile.MATERIAL_WOOD_OBJ) != 0) add(tileTable2, "", "MATERIAL_WOOD_OBJ");
    if ((tile.materialFlags & Tile.MATERIAL_ISTONE) != 0)   add(tileTable2, "", "MATERIAL_ISTONE");
    if ((tile.materialFlags & Tile.MATERIAL_OSTONE) != 0)   add(tileTable2, "", "MATERIAL_OSTONE");
    if ((tile.materialFlags & Tile.MATERIAL_DIRT) != 0)     add(tileTable2, "", "MATERIAL_DIRT");
    if ((tile.materialFlags & Tile.MATERIAL_SAND) != 0)     add(tileTable2, "", "MATERIAL_SAND");
    if ((tile.materialFlags & Tile.MATERIAL_WOOD) != 0)     add(tileTable2, "", "MATERIAL_WOOD");
    if ((tile.materialFlags & Tile.MATERIAL_LAVA) != 0)     add(tileTable2, "", "MATERIAL_LAVA");
    if ((tile.materialFlags & Tile.MATERIAL_SNOW) != 0)     add(tileTable2, "", "MATERIAL_SNOW");
    add(tileTable2, tile.animated() ? "frame: " : "rarity: ", tile.rarityFrame);
    // add(tileTable2, "unk0x58: ", "%1$d (0x%1$08x)", tile.unk0x58);
    // add(tileTable2, "unk0x5c: ", "%1$d (0x%1$08x)", tile.unk0x5c);
    add(tileTable2, "roofHeight: ", tile.roofHeight);
    tileTable2.add().colspan(2).growY().row();

    // tileFlags.clear();
    tileFlags.add("Flags:").left().growX().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 0, 5))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 5, 10))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 10, 15))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 15, 20))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 20, 25))).center().row();
  }
}
