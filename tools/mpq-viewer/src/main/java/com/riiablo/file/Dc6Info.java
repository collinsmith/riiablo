package com.riiablo.file;

import com.kotcrab.vis.ui.widget.VisTable;

import com.riiablo.util.DebugUtils;

public class Dc6Info extends VisTable {
  Dc6 dc6;
  VisTable header, box, frame;

  public Dc6Info() {}

  public Dc6Info setDc6(Dc6 dc6) {
    if (this.dc6 == dc6) return this;
    this.dc6 = dc6;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "signature: ", DebugUtils.toByteArray(dc6.signature));
    add(header, "version: ", "0x%x", dc6.version);
    add(header, "encoding: ", dc6.format);
    add(header, "section: ", DebugUtils.toByteArray(dc6.section));
    add(header, "directions: ", dc6.numDirections);
    add(header, "frames: ", dc6.numFrames);

    box = new VisTable();

    VisTable left = new VisTable();
    left.add(header).row();
    left.add().growY().row();
    left.add(box).row();
    add(left).growY().spaceRight(8);

    frame = new VisTable();
    add(frame).growY();

    return this;
  }

  private static VisTable add(VisTable table, String label, boolean value) {
    return add(table, label, String.valueOf(value));
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

  public void update(int d, int f) {
    Dc6.Dc6Direction dir = dc6.directions[d];
    Dc6.Dc6Frame frm = dir.frames[f];

    box.clear();
    box.add("BBox:").left().colspan(2).row();
    add(box, "width,height: ", "%d,%d", dir.box.width, dir.box.height);
    add(box, "bbox: ", "(%d,%d) -> (%d,%d)", dir.box.xMin, dir.box.yMin, dir.box.xMax, dir.box.yMax);
    add(box, "offset: ", "(%d,%d)", dir.box.xMin, dir.box.yMax);

    frame.clear();
    frame.add("Frame:").left().colspan(2).row();
    add(frame, "direction: ", d);
    add(frame, "flipY: ", frm.flipY);
    add(frame, "width,height: ", "%d,%d", frm.box.width, frm.box.height);
    add(frame, "offset: ", "(%d,%d)", frm.box.xMin, frm.box.yMax);
    add(frame, "unk0: ", "0x%08x", frm.unk0);
    add(frame, "next: ", "0x%08x", frm.nextOffset);
    add(frame, "length: ", "0x%08x", frm.length);
    frame.add().growY().row();
  }
}
