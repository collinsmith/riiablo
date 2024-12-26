package com.riiablo.file;

import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import java.util.Arrays;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import com.riiablo.file.Cof.Component;
import com.riiablo.tool.mpqviewer.MpqViewer;
import com.riiablo.util.DebugUtils;

import static com.badlogic.gdx.utils.Align.top;

public class CofInfo extends VisTable {
  Cof cof;
  VisTable header, box, order, info;

  public CofInfo() {}

  public CofInfo setCof(Cof cof) {
    if (this.cof == cof) return this;
    this.cof = cof;
    clear();

    header = new VisTable();
    header.add("Header:").left().colspan(2).row();
    add(header, "directions: ", cof.numDirections);
    add(header, "frames: ", cof.numFrames);
    add(header, "layers: ", cof.numLayers);
    add(header, "version: ", cof.version);
    add(header, "unk: ", DebugUtils.toByteArray(cof.unk));
    add(header, "animRate: ", cof.animRate);

    box = new VisTable();
    box.add("BBox:").left().colspan(2).row();
    add(box, "width,height: ", "%d,%d", cof.box.width, cof.box.height);
    add(box, "bbox: ", "(%d,%d) -> (%d,%d)", cof.box.xMin, cof.box.yMin, cof.box.xMax, cof.box.yMax);
    add(box, "offset: ", "(%d,%d)", cof.box.xMin, cof.box.yMax);

    VisTable left = new VisTable();
    left.add(header).row();
    left.add().growY().row();
    left.add(box).minWidth(256).row();
    add(left).growY().spaceRight(8);

    Label.LabelStyle style = new Label.LabelStyle(VisUI.getSkin().get(
        "default",
        Label.LabelStyle.class)
    ) {{
      this.background = VisUI.getSkin().getDrawable("default-pane");
    }};
    info = new VisTable() {{
      align(top);
      setBackground(VisUI.getSkin().getDrawable("default-pane"));
      VisTable header = new VisTable();
      header.columnDefaults(0).minWidth(48);
      header.columnDefaults(1).minWidth(16);
      header.columnDefaults(2).minWidth(16);
      header.columnDefaults(3).minWidth(16);
      header.columnDefaults(4).minWidth(16);
      header.columnDefaults(5).minWidth(48);
      MpqViewer v = MpqViewer.instance;
      Actor a;
      a = cell(header, v.i18n("cof-name"), style);
      new Tooltip.Builder(v.i18n("cof-component")).target(a).build();
      a = cell(header, "D", style);
      new Tooltip.Builder(v.i18n("cof-shadow")).target(a).build();
      a = cell(header, "E", style);
      new Tooltip.Builder(v.i18n("cof-selectable")).target(a).build();
      a = cell(header, "O", style);
      new Tooltip.Builder(v.i18n("cof-overrideTransLvl")).target(a).build();
      a = cell(header, "N", style);
      new Tooltip.Builder(v.i18n("cof-newTransLvl")).target(a).build();
      a = cell(header, "W", style);
      new Tooltip.Builder(v.i18n("cof-weaponClass")).target(a).build();
      add(header).row();

      VisTable body = new VisTable();
      body.columnDefaults(0).width(48);
      body.columnDefaults(1).width(16);
      body.columnDefaults(2).width(16);
      body.columnDefaults(3).width(16);
      body.columnDefaults(4).width(16);
      body.columnDefaults(5).width(48);
      for (int l = 0; l < cof.numLayers; l++) {
        Cof.Layer layer = cof.layers[l];
        cell(body, Component.toString(layer.component), style);
        cell(body, layer.shadow, style);
        cell(body, layer.selectable, style);
        cell(body, layer.overrideTransLvl, style);
        cell(body, layer.newTransLvl, style);
        cell(body, layer.weaponClass, style);
        body.row();
      }
      add(new VisScrollPane(body) {
        {
          setStyle(new ScrollPaneStyle(getStyle()) {{
            vScroll = null;
            vScrollKnob = MpqViewer.getSkin().getDrawable("vscroll");
          }});
          setScrollingDisabled(true, false);
          setScrollbarsOnTop(true);
          addListener(MpqViewer.SCROLL_ON_HOVER);
        }

        @Override
        protected void drawScrollBars(Batch batch, float r, float g, float b, float a) {
          super.drawScrollBars(batch, r, g, b, a * 0.5f);
        }
      }).growX().fillY();
    }};
    add(info).growY();

    order = new VisTable();
    add(order).growY();
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

  private static Actor cell(Table table, Object obj, Label.LabelStyle style) {
    VisLabel label = new VisLabel(String.valueOf(obj), style);
    label.setAlignment(Align.center);
    table
        .add(label).fill()
        ;
    return label;
  }

  String[] tmp = new String[Component.NUM_COMPONENTS];

  public void update(int d, int f) {
    if (cof == null) return;
    Arrays.fill(tmp, null);
    for (int l = 0, numLayers = cof.numLayers; l < numLayers; l++) {
      byte c = cof.componentAt(d, f, l);
      tmp[l] = Cof.Component.toString(c);
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
