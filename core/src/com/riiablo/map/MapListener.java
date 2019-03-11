package com.riiablo.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import com.riiablo.entity.Entity;
import com.riiablo.screen.GameScreen;

public class MapListener {
  private final Vector2    tmpVec2  = new Vector2();
  private final Vector3    tmpVec3  = new Vector3();
  private final GridPoint2 tmpVec2i = new GridPoint2();

  GameScreen  gameScreen;
  Map         map;
  MapRenderer mapRenderer;
  Entity      target;

  boolean requireRelease;

  public MapListener(GameScreen gameScreen, Map map, MapRenderer mapRenderer) {
    this.gameScreen = gameScreen;
    this.map = map;
    this.mapRenderer = mapRenderer;
  }

  // TODO: assert only 1 entity can be selected at once, once found, deselect other and set new and return
  private void updateLabel(Vector2 position) {
    gameScreen.clearLabels();
    for (Map.Zone zone : map.zones) {
      for (Entity entity : zone.entities) {
        entity.setOver(entity.contains(position));
        if (entity.isOver()) gameScreen.addLabel(entity.getLabel());
      }
    }
    for (Entity entity : gameScreen.entities.values()) {
      entity.setOver(entity.contains(position));
      if (entity.isOver()) gameScreen.addLabel(entity.getLabel());
    }
  }

  private boolean touchDown() {
    //setTarget(null);
    for (Map.Zone zone : new Array.ArrayIterator<>(map.zones)) {
      for (Entity entity : zone.entities) {
        if (entity.isOver()) {
          if (entity.position().dst(gameScreen.player.position()) <= entity.getInteractRange()) {
            setTarget(null);
            entity.interact(gameScreen);
          } else {
            setTarget(entity);
          }

          return true;
        }
      }
    }
    for (Entity entity : gameScreen.entities.values()) {
      if (entity.isOver()) {
        if (entity.position().dst(gameScreen.player.position()) <= entity.getInteractRange()) {
          setTarget(null);
          entity.interact(gameScreen);
        } else {
          setTarget(entity);
        }

        return true;
      }
    }

    return false;
  }

  public void update() {
    mapRenderer.unproject(tmpVec2.set(Gdx.input.getX(), Gdx.input.getY()));
    updateLabel(tmpVec2);
    boolean pressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    if (pressed && !requireRelease) {
      // exiting dialog should block all input until button is released to prevent menu from closing the following frame
      if (gameScreen.getDialog() != null) {
        gameScreen.setDialog(null);
        requireRelease = true;
        return;
      } else if (gameScreen.getMenu() != null) {
        gameScreen.setMenu(null, null);
      }
      boolean touched = touchDown();
      if (!touched) {
        mapRenderer.coords(tmpVec2.x, tmpVec2.y, tmpVec2i);
        tmpVec2.set(tmpVec2i.x, tmpVec2i.y);
        gameScreen.player.setPath(map, tmpVec2);
      }
    } else if (!pressed) {
      requireRelease = false;
      if (target != null) {
        if (target.position().dst(gameScreen.player.position()) <= target.getInteractRange()) {
          Entity entity = target;
          setTarget(null);
          entity.interact(gameScreen);
        }
      }
    }
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
