package com.riiablo.map2;

import com.kotcrab.vis.ui.widget.ScrollableTextArea;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import org.apache.commons.lang3.ArrayUtils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.riiablo.util.DebugUtils;

public class Dt1Info extends VisTable {
  DT1 dt1;
  VisTable header, tileTable, tileTable2, tileFlags, blocks, boxTable;

  public Dt1Info() {}

  public Dt1Info setDT1(DT1 dt1) {
    if (this.dt1 == dt1) return this;
    this.dt1 = dt1;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "signature: ", DebugUtils.toByteArray(ArrayUtils.subarray(DT1Reader.SIGNATURE, 0, DT1Reader.SIGNATURE.length / 2)));
    add(header, "", DebugUtils.toByteArray(ArrayUtils.subarray(DT1Reader.SIGNATURE, DT1Reader.SIGNATURE.length / 2, DT1Reader.SIGNATURE.length)));
    add(header, "tiles: ", dt1.numTiles);
    add(header, "data offset: ", "+0x%08x", dt1.tileOffset);

    tileFlags = new VisTable();

    VisTable left = new VisTable();
    left.add(header).row();
    left.add().growY().row();
    left.add(tileFlags).growX().row();
    add(left).growY().spaceRight(8);

    tileTable = new VisTable();
    add(tileTable).growY().spaceRight(8);

    tileTable2 = new VisTable();
    boxTable = new VisTable();

    VisTable right = new VisTable();
    right.add(tileTable2).row();
    right.add().growY().row();
    right.add(boxTable).growX().row();
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

  public void update(int t) {
    final DT1.Tile tile = dt1.tiles[t];

    tileTable.clear();
    tileTable.add("Tile:").left().colspan(2).row();
    add(tileTable, "tileIndex: ", "0x%08x", tile.tileIndex);
    add(tileTable, "mainIndex: ", tile.mainIndex);
    add(tileTable, "subIndex: ", tile.subIndex);
    add(tileTable, "orientation: ", "%d (%s)", tile.orientation, Orientation.toString(tile.orientation));
    add(tileTable, "direction: ", "%d (%s)", tile.direction, Orientation.directionToString(tile.direction));
    add(tileTable, "width,height: ", "%d,%d", tile.width, tile.height);
    add(tileTable, "texture: ", "%dx%d", tile.pixmap.getWidth(), tile.pixmap.getHeight());
    // add(tileTable, "soundIndex: ", tile.soundIndex);
    // add(tileTable, "animated: ", tile.animated);
    // add(tileTable, tile.animated != 0 ? "frame: " : "rarity: ", tile.rarity);
    // add(tileTable, "unknown: ", "%1$d (0x%1$08x)", tile.unknown);
    add(tileTable, "tile: ", "green");
    add(tileTable, "texture: ", "red");
    add(tileTable, "bbox: ", "white");
    tileTable.add().colspan(2).growY().row();

    tileTable2.clear();
    tileTable2.add("").left().colspan(2).row();
    add(tileTable2, "soundIndex: ", tile.soundIndex);
    add(tileTable2, "animated: ", tile.animated);
    add(tileTable2, tile.animated != 0 ? "frame: " : "rarity: ", tile.rarity);
    add(tileTable2, "unknown: ", "%1$d (0x%1$08x)", tile.unknown);
    add(tileTable2, "unknown2: ", "%1$d (0x%1$08x)", tile.unknown2);
    add(tileTable2, "roofHeight: ", tile.roofHeight);
    tileTable2.add().colspan(2).growY().row();

    boxTable.clear();
    boxTable.add("BBox:").left().colspan(2).row();
    add(boxTable, "width,height: ", "%d,%d", tile.box.width, tile.box.height);
    add(boxTable, "bbox: ", "(%d,%d) -> (%d,%d)", tile.box.xMin, tile.box.yMin, tile.box.xMax, tile.box.yMax);
    add(boxTable, "offset: ", "(%d,%d)", tile.box.xMin, tile.box.yMin);

    tileFlags.clear();
    tileFlags.add("Flags:").left().growX().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 0, 5))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 5, 10))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 10, 15))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 15, 20))).center().row();
    tileFlags.add(DebugUtils.toByteArray(ArrayUtils.subarray(tile.flags, 20, 25))).center().row();

    ScrollableTextArea blocksDump = new ScrollableTextArea(generateBlocksDump(tile));
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

  String generateBlocksDump(DT1.Tile tile) {
    StringBuilder builder = new StringBuilder(1024);
    for (int i = 0, s = tile.numBlocks; i < s; i++) {
      DT1.Tile.Block block = tile.blocks[i];
      builder.append('[').append(i).append(']').append('\n');
      builder.append("x,y: ").append(block.x).append(',').append(block.y).append('\n');
      builder.append("gridX,gridY: ").append(block.gridX).append(',').append(block.gridY).append('\n');
      builder.append("format: ").append(String.format("0x%04x", block.format)).append('\n');
      builder.append("size: ").append(block.size).append('B').append('\n');
      builder.append("offset: ").append(String.format("+0x%08x", block.dataOffset)).append('\n');
      builder.append('\n');
    }
    return builder.toString();
  }
}
