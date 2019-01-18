package gdx.diablo.util;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.Pools;

public class EventUtils {

  private EventUtils() {}

  public static boolean click(Button button) {
    if (button.isDisabled()) return false;
    InputEvent event = Pools.obtain(InputEvent.class);
    event.setType(InputEvent.Type.touchDown);
    event.setStage(button.getStage());
    event.setStageX(0);
    event.setStageY(0);
    event.setPointer(0);
    event.setButton(Input.Buttons.LEFT);
    event.setListenerActor(button);
    button.fire(event);

    event.setType(InputEvent.Type.touchUp);
    button.fire(event);
    Pools.free(event);
    return true;
  }
}
