package gdx.diablo.map;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

import gdx.diablo.entity.Entity;
import gdx.diablo.entity.StaticEntity;
import gdx.diablo.screen.GameScreen;

public class MapListener extends InputAdapter {
  private final Vector2 tmpVec2 = new Vector2();

  GameScreen gameScreen;
  Map map;
  MapRenderer mapRenderer;

  public MapListener(GameScreen gameScreen, Map map, MapRenderer mapRenderer) {
    this.gameScreen = gameScreen;
    this.map = map;
    this.mapRenderer = mapRenderer;
  }

  @Override
  public boolean mouseMoved(int x, int y) {
    mapRenderer.unproject(x, y, tmpVec2);
    for (Map.Zone zone : map.zones) {
      for (Entity entity : zone.entities) {
        entity.over = entity.contains(tmpVec2);
      }
    }

    return false;
  }

  @Override
  public boolean touchDown(int x, int y, int pointer, int button) {
    mapRenderer.unproject(x, y, tmpVec2);
    for (Map.Zone zone : map.zones) {
      for (Entity entity : zone.entities) {
        if (entity.over && entity instanceof StaticEntity) {
          StaticEntity object = (StaticEntity) entity;
          if (object.position().dst(gameScreen.player.position()) <= object.base.OperateRange) {
            object.operate(gameScreen);
          }
        }
      }
    }

    return false;
  }
}
