package com.riiablo.codec;

import com.kotcrab.vis.ui.widget.VisTable;

public class Dc6Info extends VisTable {
  DC6 dc6;

  public Dc6Info() {}

  public Dc6Info setDC6(DC6 dc6) {
    if (this.dc6 == dc6) return this;
    this.dc6 = dc6;
    clear();

    add("version: ",        dc6.header.version);
    add("flags: ",          dc6.header.flags, true);
    add("encoding: ",       dc6.header.format);
    add("pad bytes: ",      dc6.header.termination, true);
    add("directions: ",     dc6.header.directions);
    add("frames per dir: ", dc6.header.framesPerDir);

    return this;
  }

  private void add(String label, int value) {
    add(label, value, false);
  }

  private void add(String label, int value, boolean hex) {
    add(label).right();
    add(hex ? String.format("%08x", value) : String.valueOf(value)).left();
    row();
  }

  public void update(int d, int f) {
  }
}
