package gdx.diablo.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import org.apache.commons.lang3.ArrayUtils;

import gdx.diablo.Diablo;
import gdx.diablo.entity.Monster;
import gdx.diablo.map.DS1;
import gdx.diablo.screen.GameScreen;

public class Npc extends AI {
  private static final String TAG = "Npc";

  int targetId = ArrayUtils.INDEX_NOT_FOUND;
  float actionTimer = 0;
  boolean actionPerformed = false;

  public Npc(Monster entity) {
    super(entity);
  }

  @Override
  public void interact(GameScreen gameScreen) {
    String name = entity.getName().toLowerCase();
    String id = name + "_greeting_1";
    int index = Diablo.audio.play(id, false);
    if (index == 0) {
      id = name + "_greeting_inactive_1";
      Diablo.audio.play(id, false);
    }
  }

  public void update(float delta) {
    Vector3 target = entity.target();
    if (target.equals(Vector3.Zero) || (entity.position().epsilonEquals(target) && !entity.targets().hasNext())) {
      DS1.Path path = entity.object.path;
      if (targetId == ArrayUtils.INDEX_NOT_FOUND) {
        targetId = 0;
      } else if (actionTimer > 0) {
        actionTimer -= delta;
        actionPerformed = actionTimer < 0;
        return;
      } else if (actionPerformed) {
        actionPerformed = false;
        targetId = MathUtils.random(path.numPoints - 1);
      } else {
        entity.setMode("NU");
        actionTimer = action(path.points[targetId].action);
        actionPerformed = actionTimer < 0;
        return;
      }

      //entity.setMode("WL");
      DS1.Path.Point dst = path.points[targetId];
      entity.setPath(entity.map, new Vector3(dst.x, dst.y, 0));
    }
  }

  private float action(int action) {
    // path.actions == look at nearest player, chill, hold time, quest?
    // 1 = 4 second hold
    // 2 = 6 second hold
    // 3 = 4 second hold
    // 4 = special action at end of 10 second warriv jamella spell
    // 4 = special action at end of 8  second charsi jamella book
    // 4? fara = 5 seconds
    // 4 == S1
    // 5 == S2
    // etc

    switch (action) {
      case 1:
      case 3:
        return 4;
      case 2:
        return 6;
      // TODO: play anim only once, after timer, ending the action
      case 4:
        entity.setMode("S1");
        return 10;
      case 5:
        entity.setMode("S2");
        return 10;
      default:
        Gdx.app.error(TAG, "Unknown action index: " + action);
        return 4;
    }
  }
}
