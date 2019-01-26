package gdx.diablo.widget;

import com.badlogic.gdx.utils.Disposable;

import gdx.diablo.Diablo;

public class Dialog extends com.badlogic.gdx.scenes.scene2d.ui.Dialog implements Disposable {
  public Dialog() {
    super("", new WindowStyle() {{
      titleFont = Diablo.fonts.font16;
    }});
  }

  @Override
  public void dispose() {}
}
