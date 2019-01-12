package gdx.diablo.util;

import android.support.annotation.NonNull;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class ActorUtils {
  private ActorUtils() {}

  public static void centerAt(@NonNull Actor a, float x, float y) {
    a.setPosition(x - a.getWidth() / 2, y - a.getHeight() / 2);
  }
}
