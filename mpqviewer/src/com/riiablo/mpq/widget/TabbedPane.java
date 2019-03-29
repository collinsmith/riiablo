package com.riiablo.mpq.widget;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

import java.util.concurrent.CopyOnWriteArrayList;

public class TabbedPane extends VisTable {

  private VisTable tabs;
  private Container<VisTable> contentPanel;

  ClickListener clickListener;
  ButtonGroup<VisTextButton> buttons = new ButtonGroup<>();
  ObjectMap<String, VisTable> map = new ObjectMap<>();
  CopyOnWriteArrayList<TabListener> listeners = new CopyOnWriteArrayList<>();

  public TabbedPane() {
    clickListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Button button = (Button) event.getListenerActor();
        contentPanel.setActor(map.get(button.getName()));
        notifyTabSwitched(buttons.getChecked().getName(), button.getName());
      }
    };

    add(tabs = new VisTable()).growX().row();
    add(new Image(VisUI.getSkin().getDrawable("list-selection"))).growX().row();
    add(contentPanel = new Container<>()).align(Align.top).row();
  }

  public void addTab(String text, VisTable content) {
    VisTextButton button = new VisTextButton(text) {{
      setName(getText().toString());
      setStyle(new TextButtonStyle(getStyle()) {{
        checked = down;
      }});
      setProgrammaticChangeEvents(false);
      setFocusBorderEnabled(false);
      addListener(clickListener);
    }};
    if (buttons.getCheckedIndex() == -1) {
      contentPanel.setActor(content);
      notifyTabSwitched(null, button.getName());
    }
    buttons.add(button);
    tabs.add(button).growX();
    map.put(button.getName(), content);
  }

  public void switchTo(String tab) {
    buttons.setChecked(tab); // Doesn't actually fire button

    // TODO: This could be cleaned up, but it works fine for now
    InputEvent event = new InputEvent();
    event.setListenerActor(buttons.getChecked());
    clickListener.clicked(event, 0, 0);
  }

  public String getTab() {
    return buttons.getChecked().getName();
  }

  public void addListener(TabListener l) {
    listeners.add(l);
  }

  public boolean removeListener(TabListener l) {
    return listeners.remove(l);
  }

  private void notifyTabSwitched(String fromTab, String toTab) {
    for (TabListener l : listeners) {
      l.switchedTab(fromTab, toTab);
    }
  }

  public interface TabListener {
    void switchedTab(String fromTab, String toTab);
  }
}
