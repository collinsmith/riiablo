package gdx.diablo.widget;

import com.badlogic.gdx.utils.Disposable;

public class TextField extends com.badlogic.gdx.scenes.scene2d.ui.TextField implements Disposable {

  public TextField(TextFieldStyle style) {
    this("", style);
  }

  public TextField(String text, TextFieldStyle style) {
    super(text, style);
  }

  @Override
  public void dispose() {

  }

  public static class TextFieldStyle extends com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle {
    public TextFieldStyle() {

    }
  }

}
