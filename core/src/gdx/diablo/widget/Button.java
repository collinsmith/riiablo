package gdx.diablo.widget;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;

import gdx.diablo.Diablo;

public class Button extends com.badlogic.gdx.scenes.scene2d.ui.Button implements Disposable {

  private static final AssetDescriptor<Sound> buttonDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\button.wav", Sound.class);

  @Override
  public void setStyle(com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle style) {
    super.setStyle(style);
  }

  public Button(ButtonStyle style) {
    super(style);
    addListener(new ClickListener() {
      Sound button = null;

      {
        Diablo.assets.load(buttonDescriptor);
      }

      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int b) {
        if (event.getButton() != Input.Buttons.LEFT) {
          return super.touchDown(event, x, y, pointer, b);
        } else if (button == null) {
          Diablo.assets.finishLoadingAsset(buttonDescriptor);
          button = Diablo.assets.get(buttonDescriptor);
        }

        button.play();
        Diablo.input.vibrate(10);
        return super.touchDown(event, x, y, pointer, b);
      }
    });
  }

  @Override
  public void dispose() {
    Diablo.assets.unload(buttonDescriptor.fileName);
  }

  public static class ButtonStyle extends com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle {
    public ButtonStyle() {}
    public ButtonStyle(Drawable up, Drawable down) {
      this(up, down, null);
    }
    public ButtonStyle(Drawable up, Drawable down, Drawable checked) {
      super(up, down, checked);
    }
    public ButtonStyle(com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle style) {
      super(style);
    }
  }
}
