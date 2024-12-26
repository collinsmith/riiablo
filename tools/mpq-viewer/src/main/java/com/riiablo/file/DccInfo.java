package com.riiablo.file;

import com.kotcrab.vis.ui.widget.VisTable;
import io.netty.buffer.ByteBufUtil;

import com.riiablo.util.DebugUtils;

public class DccInfo extends VisTable {
  Dcc dcc;
  VisTable header, box, body, frame, streams;

  public DccInfo() {}

  public DccInfo setDcc(Dcc dcc) {
    if (this.dcc == dcc) return this;
    this.dcc = dcc;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "signature: ", DebugUtils.toByteArray(dcc.signature));
    add(header, "version: ", "0x%x", dcc.version);
    add(header, "directions: ", dcc.numDirections);
    add(header, "frames: ", dcc.numFrames);
    add(header, "tag: ", "0x%08x", dcc.tag);
    add(header, "uncompressed size: ", "0x%x", dcc.uncompressedSize);

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
    if (!dcc.loaded(d)) return;
    Dcc.DccDirection dir = dcc.directions[d];
    Dcc.DccFrame frm = dir.frames[f];

    body.clear();
    body.add("Direction:").left().colspan(2).row();
    add(body, "direction: ", d);
    add(body, "uncompressed size: ", dir.uncompressedSize);
    add(body, "compression flags: ", "0x%02x", dir.compressionFlags);
    body.add(Dcc.DccDirection.getFlagsString(dir.compressionFlags)).colspan(2).row();
    add(body, "variable0 bits: ", dir.variable0Bits);
    add(body, "width bits: ", dir.widthBits);
    add(body, "height bits: ", dir.heightBits);
    add(body, "xOffset bits: ", dir.xOffsetBits);
    add(body, "yOffset bits: ", dir.yOffsetBits);
    add(body, "extra bits: ", dir.extraBytesBits);
    add(body, "uncompressed bits: ", dir.compressedBytesBits);

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
    frame.add("Frame:").left().colspan(2).growX().row();
    add(frame, "flipY: ", frm.flipY);
    add(frame, "compressed bytes: ", frm.compressedBytes);
    add(frame, "extra bytes: ", frm.extraBytes);
    add(frame, "extra data: ", ByteBufUtil.hexDump(frm.extraData.buffer()));
    add(frame, "width,height: ", "%d,%d", frm.box.width, frm.box.height);
    add(frame, "offset: ", "(%d,%d)", frm.box.xMin, frm.box.yMax);
  }
}
