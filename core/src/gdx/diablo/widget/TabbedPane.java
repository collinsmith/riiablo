package gdx.diablo.widget;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.concurrent.CopyOnWriteArrayList;

import gdx.diablo.widget.TextButton.TextButtonStyle;

public class TabbedPane extends Table {

  private Table tabs;
  private Container<Table> contentPanel;

  ClickListener clickListener;
  ButtonGroup<TextButton> buttons = new ButtonGroup<>();
  ObjectMap<String, Table> map = new ObjectMap<>();
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

    add(tabs = new Table()).growX().row();
    add(contentPanel = new Container<>()).align(Align.top).row();
  }

  public void addTab(int id, TextButtonStyle style, Table content) {
    TextButton button = new TextButton(id, style) {{
      setName(getText());
      setStyle(new TextButtonStyle(getStyle()) {{
        checked = down;
      }});
      setProgrammaticChangeEvents(false);
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
