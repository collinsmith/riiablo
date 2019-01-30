package gdx.diablo.widget;

import com.badlogic.gdx.utils.Disposable;

public class TextArea extends com.badlogic.gdx.scenes.scene2d.ui.TextArea implements Disposable {

  public TextArea(TextFieldStyle style) {
    this("", style);
  }

  public TextArea(String text, TextFieldStyle style) {
    super(text, style);
  }

  @Override
  public void dispose() {

  }

  public static class TextFieldStyle extends com.badlogic.gdx.scenes.scene2d.ui.TextArea.TextFieldStyle {
    public TextFieldStyle() {

    }
  }

}
