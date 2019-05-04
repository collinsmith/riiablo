package com.riiablo.codec;

import com.kotcrab.vis.ui.widget.VisTable;

public class DccInfo extends VisTable {
  DCC dcc;

  public DccInfo() {}

  public DccInfo setDCC(DCC dcc) {
    if (this.dcc == dcc) return this;
    this.dcc = dcc;
    clear();

    add("signature: ",         dcc.header.signature);
    add("version: ",           dcc.header.version);
    add("directions: ",        dcc.header.directions);
    add("frames per dir: ",    dcc.header.framesPerDir);
    add("tag: ",               dcc.header.tag);
    add("uncompressed size: ", dcc.header.finalDC6Size);

    return this;
  }

  private void add(String label, int value) {
    add(label).right();
    add(String.valueOf(value)).left();
    row();
  }

  public void update(int d, int f) {
  }
}
