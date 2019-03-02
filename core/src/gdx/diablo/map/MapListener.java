package gdx.diablo.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import gdx.diablo.entity.Entity;
import gdx.diablo.screen.GameScreen;

public class MapListener extends InputAdapter {
  private final Vector2    tmpVec2  = new Vector2();
  private final Vector3    tmpVec3  = new Vector3();
  private final GridPoint2 tmpVec2i = new GridPoint2();

  GameScreen gameScreen;
  Map map;
  MapRenderer mapRenderer;
  Entity target;

  public MapListener(GameScreen gameScreen, Map map, MapRenderer mapRenderer) {
    this.gameScreen = gameScreen;
    this.map = map;
    this.mapRenderer = mapRenderer;
  }

  @Override
  public boolean mouseMoved(int x, int y) {
    mapRenderer.unproject(x, y, tmpVec2);
    gameScreen.clearLabels();
    for (Map.Zone zone : map.zones) {
      for (Entity entity : zone.entities) {
        entity.over = entity.contains(tmpVec2);
        if (entity.over) gameScreen.addLabel(entity.getLabel());
      }
    }

    return false;
  }

  @Override
  public boolean touchDown(int x, int y, int pointer, int button) {
    setTarget(null);
    mapRenderer.unproject(x, y, tmpVec2);
    for (Map.Zone zone : new Array.ArrayIterator<>(map.zones)) {
      for (Entity entity : zone.entities) {
        if (entity.over) {
          if (entity.position().dst(gameScreen.player.position()) <= entity.getInteractRange()) {
            setTarget(null);
            entity.interact(gameScreen);
            return true;
          } else {
            setTarget(entity);
            return true;
          }
        }
      }
    }

    return false;
  }

  public void update() {
    if (target != null) {
      if (target.position().dst(gameScreen.player.position()) <= target.getInteractRange()) {
        Entity entity = target;
        setTarget(null);
        entity.interact(gameScreen);
      }

      return;
    }

    if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) return;
    int x = Gdx.input.getX();
    int y = Gdx.input.getY();
    mapRenderer.unproject(x, y, tmpVec2);
    mapRenderer.coords(tmpVec2.x, tmpVec2.y, tmpVec2i);
    tmpVec3.set(tmpVec2i.x, tmpVec2i.y, 0);
    gameScreen.player.setPath(map, tmpVec3);
  }

  private void setTarget(Entity entity) {
    if (target != entity) {
      if (entity == null) {
        target = null;
        gameScreen.player.setPath(map, null);
      } else {
        target = entity;
        gameScreen.player.setPath(map, target.position());
      }
    }
  }
}
