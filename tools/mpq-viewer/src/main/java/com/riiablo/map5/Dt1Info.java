package com.riiablo.map5;

import com.kotcrab.vis.ui.widget.ScrollableTextArea;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import org.apache.commons.lang3.ArrayUtils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.riiablo.util.DebugUtils;

public class Dt1Info extends VisTable {
  Dt1 dt1;
  VisTable header, tileTable, tileTable2, tileFlags, blocks;

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

    blocks = new VisTable();
    add(blocks).growY();

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

  public void updateBlocks(Block[] blockData) {
    ScrollableTextArea blocksDump = new ScrollableTextArea(generateBlocksDump(blockData));
    final VisScrollPane scrollPane = (VisScrollPane) blocksDump.createCompatibleScrollPane();
    scrollPane.addListener(new ClickListener() {
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        getStage().setScrollFocus(scrollPane);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        getStage().setScrollFocus(null);
      }
    });
    blocks.clear();
    blocks.add("Blocks:").left().row();
    blocks.add(scrollPane).minWidth(200f).growY();
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

    add(tileTable2, "red: ", Tile.flagToString(Tile.FLAG_BLOCK_WALK));
    add(tileTable2, "green: ", Tile.flagToString(Tile.FLAG_BLOCK_LIGHT_LOS));
    add(tileTable2, "blue: ", Tile.flagToString(Tile.FLAG_BLOCK_JUMP));
    add(tileTable2, "purple: ", Tile.flagToString(Tile.FLAG_BLOCK_PLAYER_WALK));
    add(tileTable2, "gold: ", Tile.flagToString(Tile.FLAG_BLOCK_UNKNOWN1));
    add(tileTable2, "sky: ", Tile.flagToString(Tile.FLAG_BLOCK_LIGHT));
    add(tileTable2, "white: ", Tile.flagToString(Tile.FLAG_BLOCK_UNKNOWN2));
    add(tileTable2, "black: ", Tile.flagToString(Tile.FLAG_BLOCK_UNKNOWN3));
    tileTable2.add().colspan(2).growY().row();

    // tileFlags.clear();
    tileFlags.add("Flags:").left().growX().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 0, 5))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 5, 10))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 10, 15))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 15, 20))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 20, 25))).center().row();
  }

  String generateBlocksDump(Block[] blocks) {
    StringBuilder builder = new StringBuilder(1024);
    for (int i = 0, s = blocks.length; i < s; i++) {
      Block block = blocks[i];
      builder.append('[').append(i).append(']').append('\n');
      builder.append("x,y: ").append(block.x).append(',').append(block.y).append('\n');
      builder.append("xGrid,yGrid: ").append(block.xGrid).append(',').append(block.yGrid).append('\n');
      builder.append("format: ").append(block.format()).append('\n');
      builder.append("dataOffset: ").append(String.format("+0x%08x", block.dataOffset)).append('\n');
      builder.append("dataLength: ").append(block.dataLength).append('B').append('\n');
      builder.append('\n');
    }
    return builder.toString();
  }
}
