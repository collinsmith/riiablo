package com.riiablo.codec;

import com.kotcrab.vis.ui.widget.VisTable;

public class DccInfo extends VisTable {
  DCC dcc;
  VisTable header, box, body;

  public DccInfo() {}

  public DccInfo setDCC(DCC dcc) {
    if (this.dcc == dcc) return this;
    this.dcc = dcc;
    clear();

    header = new VisTable();
    add(header, "signature: ",         dcc.header.signature);
    add(header, "version: ",           dcc.header.version);
    add(header, "directions: ",        dcc.header.directions);
    add(header, "frames per dir: ",    dcc.header.framesPerDir);
    add(header, "tag: ",               dcc.header.tag);
    add(header, "uncompressed size: ", dcc.header.finalDC6Size);
    add(header);

    box = new VisTable();

    VisTable left = new VisTable();
    left.add(header).row();
    left.add().growY().row();
    left.add(box).row();
    add(left).growY().spaceRight(8);

    body = new VisTable();
    add(body);

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
    DCC.Direction dir = dcc.getDirection(d);
    DCC.Frame frame = dcc.getFrame(d, f);

    body.clear();
    add(body, "direction: ",           d);
    add(body, "outsize coded: ",       dir.outsizeCoded);
    add(body, "compression flags: ",   dir.compressionFlags);
    body.add(dir.getFlags()).colspan(2).row();
    add(body, "variable0 bits: ",      dir.variable0Bits);
    add(body, "width bits: ",          dir.widthBits);
    add(body, "height bits: ",         dir.heightBits);
    add(body, "xoffset bits: ",        dir.xOffsetBits);
    add(body, "yoffset bits: ",        dir.yOffsetBits);
    add(body, "optional bytes bits: ", dir.optionalBytesBits);
    add(body, "coded data bits: ",     dir.codedBytesBits);

    box.clear();
    add(box, "width,height: ", "%d,%d", dir.box.width, dir.box.height);
    add(box, "bbox: ",         "(%d,%d) -> (%d,%d)", dir.box.xMin, dir.box.yMin, dir.box.xMax, dir.box.yMax);
    add(box, "offset: ",       "(%d,%d)", dir.box.xMin, dir.box.yMax);
  }
}
