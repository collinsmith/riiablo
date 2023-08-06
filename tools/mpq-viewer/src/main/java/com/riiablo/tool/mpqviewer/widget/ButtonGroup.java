package com.riiablo.tool.mpqviewer.widget;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import static com.riiablo.util.ImplUtils.unsupported;

public class ButtonGroup<T extends Button> extends com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup<T> {
  final IntMap<Button> ids = new IntMap<>();
  final ClickListener clickListener = new ClickListener() {
    @Override
    public void clicked(InputEvent event, float x, float y) {
      Button button = (Button) event.getListenerActor();
      if (button.isDisabled()) return;
      notifyButtonSwitched(getButtons().indexOf((T) button, true));
    }
  };

  final Array<ButtonGroupListener> listeners = new Array<>();

  public ButtonGroup() {
    super();
  }

  public void addListener(ButtonGroupListener listener) {
    listeners.add(listener);
  }

  public int addId(T button) {
    int index = getButtons().size;
    ids.put(index, button);
    super.add(button);
    button.addListener(clickListener);
    return index;
  }

  @Override
  public final void add(T button) {
    unsupported("Not supported.");
  }

  public void setChecked(int index) {
    ids.get(index).setChecked(true);
  }

  public void setDisabled(int index, boolean b) {
    ids.get(index).setDisabled(b);
  }

  @Override
  public final void clear() {
    unsupported("Not supported.");
  }

  @Override
  public final void remove(T... buttons) {
    unsupported("Not supported.");
  }

  @Override
  public final void remove(T button) {
    unsupported("Not supported.");
  }

  private void notifyButtonSwitched(int toIndex) {
    for (ButtonGroupListener l : listeners) {
      l.switchedButton(toIndex);
    }
  }

  public interface ButtonGroupListener {
    void switchedButton(int toIndex);
  }
}
