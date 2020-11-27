package com.riiablo.codec;

import com.kotcrab.vis.ui.widget.VisTable;
import java.util.Arrays;

import com.riiablo.util.DebugUtils;

public class CofInfo extends VisTable {
  String[] componentNames = new String[]{
      "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH",
      "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8",
  };

  COF cof;
  VisTable header, box, order, info;

  public CofInfo() {}

  public CofInfo setCOF(COF cof) {
    if (this.cof == cof) return this;
    this.cof = cof;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "layers: ",         cof.header.layers);
    add(header, "frames per dir: ", cof.header.framesPerDir);
    add(header, "directions: ",     cof.header.directions);
    add(header, "version: ",        cof.header.version);
    add(header, "unknown1: ",       DebugUtils.toByteArray(cof.header.unknown1));
    add(header, "animRate: ",       cof.header.animRate);
    add(header, "zeros: ",          cof.header.zeros);

//    body = new VisTable();
//    for (int i = 0; i < cof.getNumLayers(); i++) {
//      //cof.getLayer(l);
//    }


    box = new VisTable();
    box.add("BBox:").left().colspan(2).row();
    add(box, "width,height: ", "%d,%d", cof.box.width, cof.box.height);
    add(box, "bbox: ",         "(%d,%d) -> (%d,%d)", cof.box.xMin, cof.box.yMin, cof.box.xMax, cof.box.yMax);
    add(box, "offset: ",       "(%d,%d)", cof.box.xMin, cof.box.yMax);

    VisTable left = new VisTable();
    left.add(header).row();
    left.add().growY().row();
    left.add(box).row();
    add(left).growY().spaceRight(8);

    info = new VisTable();
    info.top();
    info.add("Info: S C O T W").left().colspan(2).row();
    for (int l = 0; l < cof.getNumLayers(); l++) {
      COF.Layer layer = cof.getLayer(l);
      add(info, componentNames[layer.component] + " ", "%d %d %d %d %d %s",
          layer.component, layer.shadow, layer.selectable, layer.overrideTransLvl, layer.newTransLvl, layer.weaponClass);
    }
    add(info).growY().spaceRight(8);

    order = new VisTable();
    add(order).growY().spaceRight(8);
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

  String[] tmp = new String[COF.Component.NUM_COMPONENTS];

  public void update(int d, int f) {
    Arrays.fill(tmp, null);
    for (int l = 0, numLayers = cof.getNumLayers(); l < numLayers; l++) {
      int c = cof.getLayerOrder(d, f, l);
      tmp[l] = componentNames[c];
    }

    order.clear();
    order.top();
    order.add("Order:").left().row();
    for (String str : tmp) {
      if (str == null) break;
      order.add(str).left().row();
    }
  }
}
