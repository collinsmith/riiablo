package gdx.diablo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class Textures implements Disposable {
  public final Texture white;
  public final Texture modal;

  public Textures() {
    white = createTexture(Diablo.colors.white);
    modal = createTexture(new Color(0.0f, 0.0f, 0.0f, 0.5f));
  }

  public Texture createTexture(Color color) {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fill();
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  @Override
  public void dispose() {
    white.dispose();
    modal.dispose();
  }
}
