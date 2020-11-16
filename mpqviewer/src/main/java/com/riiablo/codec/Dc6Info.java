package com.riiablo.codec;

import com.kotcrab.vis.ui.widget.VisTable;

public class Dc6Info extends VisTable {
  DC6 dc6;
  VisTable header, box, frame;

  public Dc6Info() {}

  public Dc6Info setDC6(DC6 dc6) {
    if (this.dc6 == dc6) return this;
    this.dc6 = dc6;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "version: ",        dc6.header.version);
    add(header, "flags: ",          "%08x", dc6.header.flags);
    add(header, "encoding: ",       dc6.header.format);
    add(header, "pad bytes: ",      "%08x", dc6.header.termination);
    add(header, "directions: ",     dc6.header.directions);
    add(header, "frames per dir: ", dc6.header.framesPerDir);

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
    DC6.Direction dir = dc6.getDirection(d);
    DC6.Frame frm = dc6.getFrame(d, f);

    box.clear();
    box.add("BBox:").left().colspan(2).row();
    add(box, "width,height: ", "%d,%d", dir.box.width, dir.box.height);
    add(box, "bbox: ",         "(%d,%d) -> (%d,%d)", dir.box.xMin, dir.box.yMin, dir.box.xMax, dir.box.yMax);
    add(box, "offset: ",       "(%d,%d)", dir.box.xMin, dir.box.yMax);

    frame.clear();
    frame.add("Frame:").left().colspan(2).row();
    add(frame, "direction: ",    d);
    add(frame, "flipped: ",      frm.flip);
    add(frame, "width,height: ", "%d,%d", frm.box.width, frm.box.height);
    add(frame, "offset: ",       "(%d,%d)", frm.box.xMin, frm.box.yMax);
    add(frame, "alloc size: ",   frm.allocSize);
    add(frame, "next block: ",   "%08x", frm.nextBlock);
    add(frame, "len: ",          frm.length);
    frame.add().growY().row();
  }
}
