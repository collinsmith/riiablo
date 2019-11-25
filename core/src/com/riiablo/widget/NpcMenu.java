package com.riiablo.widget;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.riiablo.Riiablo;
import com.riiablo.graphics.BorderedPaletteIndexedDrawable;

public class NpcMenu extends Table {

  private static final float PADDING = 8;
  private static final float SPACING = 2;

  Entity  owner;
  NpcMenu parent;

  CancellationListener cancellationListener;

  public NpcMenu(Entity owner, String header) {
    parent = null;
    this.owner = owner;
    setBackground(new BorderedPaletteIndexedDrawable());
    pad(PADDING);
    add(new Label(header, Riiablo.fonts.font16) {{
      setColor(Riiablo.colors.gold);
    }}).space(SPACING).row();
  }

  public NpcMenu(int id) {
    setBackground(new BorderedPaletteIndexedDrawable());
    pad(PADDING);
    add(new Label(id, Riiablo.fonts.font16, Riiablo.colors.gold)).space(SPACING).row();
  }

  public boolean hasParent() {
    return parent != null;
  }

  public NpcMenu getParent() {
    return parent;
  }

  public NpcMenu addItem(int id, ClickListener clickListener) {
    LabelButton button = new LabelButton(id, Riiablo.fonts.font16);
    button.addListener(clickListener);
    add(button).space(SPACING).row();
    return this;
  }

  public NpcMenu addItem(int id, final NpcMenu menu) {
    menu.parent = this;
    menu.owner = owner;
    addItem(id, new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Riiablo.game.setMenu(menu, owner);
      }
    });
    return this;
  }

  public NpcMenu addCancel(CancellationListener cancellationListener) {
    this.cancellationListener = cancellationListener;
    addItem(3400, new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        cancel();
        Riiablo.game.setMenu(parent, owner);
      }
    });
    return this;
  }

  public NpcMenu build() {
    pack();
    return this;
  }

  public void cancel() {
    if (cancellationListener != null) cancellationListener.onCancelled();
  }

  public interface CancellationListener {
    void onCancelled();
  }
}
