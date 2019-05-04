package com.riiablo.codec;

import com.kotcrab.vis.ui.widget.VisTable;

import org.apache.commons.lang3.ObjectUtils;

public class DccInfo extends VisTable {
  DCC dcc;
  VisTable header, box, body, frame, streams;

  public DccInfo() {}

  public DccInfo setDCC(DCC dcc) {
    if (this.dcc == dcc) return this;
    this.dcc = dcc;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "signature: ",         dcc.header.signature);
    add(header, "version: ",           dcc.header.version);
    add(header, "directions: ",        dcc.header.directions);
    add(header, "frames per dir: ",    dcc.header.framesPerDir);
    add(header, "tag: ",               dcc.header.tag);
    add(header, "uncompressed size: ", dcc.header.finalDC6Size);

    box = new VisTable();

    VisTable left = new VisTable();
    left.add(header).row();
    left.add().growY().row();
    left.add(box).row();
    add(left).growY().spaceRight(8);

    body = new VisTable();
    add(body).growY().spaceRight(8);

    frame = new VisTable();
    streams = new VisTable();

    VisTable right = new VisTable();
    right.add(frame).row();
    right.add().growY().row();
    right.add(streams).row();
    add(right).growY();

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
    DCC.Frame frm = dcc.getFrame(d, f);

    body.clear();
    body.add("Direction:").left().colspan(2).row();
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
    box.add("BBox:").left().colspan(2).row();
    add(box, "width,height: ", "%d,%d", dir.box.width, dir.box.height);
    add(box, "bbox: ",         "(%d,%d) -> (%d,%d)", dir.box.xMin, dir.box.yMin, dir.box.xMax, dir.box.yMax);
    add(box, "offset: ",       "(%d,%d)", dir.box.xMin, dir.box.yMax);

    streams.clear();
    streams.add("Streams:").left().colspan(2).row();
    add(streams, "equalCellBitStreamSize: ",     (int) dir.equalCellBitStreamSize);
    add(streams, "pixelMaskBitStreamSize: ",     (int) dir.pixelMaskBitStreamSize);
    add(streams, "encodingTypeBitStreamSize: ",  (int) dir.encodingTypeBitStreamSize);
    add(streams, "rawPixelCodesBitStreamSize: ", (int) dir.rawPixelCodesBitStreamSize);

    frame.clear();
    frame.add("Frame:").left().colspan(2).row();
    add(frame, "coded bytes: ",    frm.codedBytes);
    add(frame, "optional bytes: ", frm.optionalBytes);
    add(frame, "optional data: ",  ObjectUtils.toString(frm.optionalBytesData));
    add(frame, "width,height: ",   "%d,%d", frm.box.width, frm.box.height);
    add(frame, "offset: ",         "(%d,%d)", frm.box.xMin, frm.box.yMax);
  }
}
