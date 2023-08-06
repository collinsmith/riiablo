package com.riiablo.tool.mpqviewer.widget;

import com.kotcrab.vis.ui.widget.VisTable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Value;

public class CollapsibleVisTable extends VisTable {

  private boolean collapsed = false;
  private float currentWidth = -1;

  public CollapsibleVisTable() {
    this(false);
  }

  public CollapsibleVisTable(boolean setVisDefaults) {
    super(setVisDefaults);
  }

  public boolean collapsed() {
    return collapsed;
  }

  public void setCollapsed(boolean collapsed) {
    if (this.collapsed == collapsed) {
      return;
    }

    this.collapsed = collapsed;
    if (collapsed) {
      setTouchable(Touchable.disabled);
      currentWidth = 0;
    } else {
      setTouchable(Touchable.childrenOnly);
      currentWidth = super.getPrefWidth();
    }

    invalidateHierarchy();
  }

  public boolean isCollapsed() {
    return collapsed;
  }

  @Override
  public void draw(Batch batch, float a) {
    if (currentWidth > 0) super.draw(batch, a);
  }

  @Override
  public float getMinWidth() {
    return collapsed ? 0 : super.getMinWidth();
  }

  @Override
  public float getPrefWidth() {
    if (currentWidth < 0) currentWidth = super.getPrefWidth();
    return currentWidth;
  }
}
