package com.riiablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import com.riiablo.camera.OrthographicCamera;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.map2.Zone;
import com.riiablo.map2.util.DebugMode;
import com.riiablo.map2.util.ZoneGraph;
import com.riiablo.tool.LwjglTool;
import com.riiablo.tool.Tool;

import static com.badlogic.gdx.Input.Keys.GRAVE;
import static com.riiablo.map2.DT1.Tile.SUBTILE_SIZE;
import static com.riiablo.map2.util.DebugMode.TILE;

public class MapDebugger extends Tool {
  private static final Logger log = LogManager.getLogger(MapDebugger.class);

  public static void main(String[] args) {
    LwjglTool.create(MapDebugger.class, "map-debugger", args)
        .title("Map Debugger")
        .size(800, 600)
        .start();
  }

  OrthographicCamera camera;
  Batch batch;
  ZoneGraph tree;
  DebugMode debugMode = TILE;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    LogManager.setLevel(MapDebugger.class.getName(), Level.DEBUG);

    camera = new OrthographicCamera();
    batch = new SpriteBatch();
    Gdx.input.setInputProcessor(new InputAdapter() {
      Vector2 start = new Vector2();
      Vector2 end = new Vector2();
      Vector2 tmp = new Vector2();

      @Override
      public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        start.set(screenX, screenY);
        camera.unproject(start);
        return true;
      }

      @Override
      public boolean touchDragged(int screenX, int screenY, int pointer) {
        end.set(screenX, screenY);
        camera.unproject(end);

        tmp.set(start).sub(end);
        camera.position.add(tmp.x, tmp.y, 0f);
        camera.update();
        return true;
      }

      @Override
      public boolean scrolled(float amountX, float amountY) {
        camera.zoom += (amountY * 0.1f);
        if (camera.zoom <= 0.1f) camera.zoom = 0.1f;
        else if (camera.zoom >= 10f) camera.zoom = 10f;
        camera.update();
        return true;
      }

      @Override
      public boolean keyDown(int keycode) {
        switch (keycode) {
          case GRAVE:
            debugMode = debugMode.next;
            return true;
          default:
            return false;
        }
      }
    });

    // coords above are in tiles, need to be in subtiles
    tree = new ZoneGraph();
    claim1(tree, -56, 0, 56, 40);
    claim2(tree, 0, -40, 80, 80, 8, 8);
    // tree.claim(-25, 0, 25, 25);
    // tree.claim(0, 0, 50, 50);
    // tree.claim(0, -75, 75, 75);
    // tree.claim(-100, -100, 100, 100);
  }

  /** pseudo-ds1 claim /w grid size = zone size */
  Zone claim1(ZoneGraph tree, int tileX, int tileY, int width, int height) {
    return tree.claim(
        tileX * SUBTILE_SIZE,
        tileY * SUBTILE_SIZE,
        width * SUBTILE_SIZE,
        height * SUBTILE_SIZE,
        width,
        height);
  }

  Zone claim2(ZoneGraph tree, int tileX, int tileY, int width, int height, int chunkWidth, int chunkHeight) {
    return tree.claim(
        tileX * SUBTILE_SIZE,
        tileY * SUBTILE_SIZE,
        width * SUBTILE_SIZE,
        height * SUBTILE_SIZE,
        chunkWidth,
        chunkHeight);
  }

  @Override
  public void resize(int width, int height) {
    camera.setToOrtho(false, width, height);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    tree.drawDebug(batch, debugMode);
    batch.end();
  }

  @Override
  public void dispose() {
    batch.dispose();
    tree.dispose();
  }
}
