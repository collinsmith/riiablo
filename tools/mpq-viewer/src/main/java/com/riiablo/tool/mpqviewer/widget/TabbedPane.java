package com.riiablo.tool.mpqviewer.widget;

import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import java.util.concurrent.CopyOnWriteArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.IntMap;

import com.riiablo.tool.mpqviewer.widget.ButtonGroup.ButtonGroupListener;

public class TabbedPane extends VisTable {

  VisTable tabs;
  Container<VisTable> contentPanel;
  ButtonGroup<VisTextButton> buttons = new ButtonGroup<>();
  IntMap<VisTable> map = new IntMap<>();
  CopyOnWriteArrayList<TabListener> listeners = new CopyOnWriteArrayList<>();

  final ButtonGroupListener groupListener = toIndex -> {
    contentPanel.setActor(map.get(toIndex));
    notifyTabSwitched(toIndex);
  };

  public TabbedPane() {
    setBackground(VisUI.getSkin().getDrawable("default-pane"));

    buttons.addListener(groupListener);
    add(tabs = new VisTable()).growX().row();
    add(new Image(VisUI.getSkin().getDrawable("list-selection"))).minHeight(1).growX().row();
    add(contentPanel = new Container<>())
        .pad(4)
        .row();
  }

  public int addTab(String text, VisTable content) {
    VisTextButton button = new VisTextButton(text) {{
      setName(getText().toString());
      setStyle(new TextButtonStyle(getStyle()) {{
        checked = down;
      }});
      setProgrammaticChangeEvents(false);
      setFocusBorderEnabled(false);
    }};
    int index = buttons.addId(button);
    tabs.add(button).growX();
    map.put(index, content);
    if (buttons.getCheckedIndex() == -1) {
      contentPanel.setActor(content);
      notifyTabSwitched(index);
    }
    return index;
  }

  public boolean switchTo(int tabIndex) {
    if (getTabIndex() == tabIndex) return false;
    buttons.setChecked(tabIndex); // Doesn't actually fire button
    groupListener.switchedButton(tabIndex);
    return true;
  }

  public void update() {
    contentPanel.setActor(map.get(getTabIndex()));
  }

  public String getTab() {
    return buttons.getChecked().getName();
  }

  public int getTabIndex() {
    return buttons.getCheckedIndex();
  }

  public void setDisabled(int tabIndex, boolean b) {
    buttons.setDisabled(tabIndex, b);
    if (b && getTabIndex() == tabIndex) {
      switchTo(tabIndex + 1 >= buttons.getButtons().size ? 0 : tabIndex + 1);
    }
  }

  public void addListener(TabListener l) {
    listeners.add(l);
  }

  public boolean removeListener(TabListener l) {
    return listeners.remove(l);
  }

  private void notifyTabSwitched(int tabIndex) {
    for (TabListener l : listeners) {
      l.switchedTab(tabIndex);
    }
  }

  public interface TabListener {
    void switchedTab(int tabIndex);
  }
}
