package com.riiablo.entity;

import java.util.Arrays;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.StringBuilder;

@Deprecated
public class DirectionTool extends ApplicationAdapter {
  private static final String TAG = "DirectionTool";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = TAG;
    config.resizable = false;
    config.width = 256;
    config.height = 256;
    config.vSyncEnabled = false;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new DirectionTool(), config);
  }

  Batch batch;
  BitmapFont font;
  ShapeRenderer shapes;
  float width, height;
  float x1, y1;
  float x2, y2;
  StringBuilder builder = new StringBuilder();

  DirectionTool() {}

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    font = new BitmapFont();
    batch = new SpriteBatch();
    shapes = new ShapeRenderer();

    width  = Gdx.graphics.getWidth();
    height = Gdx.graphics.getHeight();
    x1 = width  / 2;
    y1 = height / 2;

    Gdx.input.setInputProcessor(new InputAdapter() {
      @Override
      public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        update(screenX, screenY);
        return true;
      }

      @Override
      public boolean touchDragged(int screenX, int screenY, int pointer) {
        update(screenX, screenY);
        return true;
      }

      void update(int screenX, int screenY) {
        x2 = screenX;
        y2 = height - screenY;
      }
    });
  }

  void print(float[] a) {
    Gdx.app.log(TAG, Arrays.toString(a));
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    float radius = 64;
    float angle = MathUtils.atan2(y2 - y1, x2 - x1);
    float x = radius * MathUtils.cos(angle);
    float y = radius * MathUtils.sin(angle);

    builder.setLength(0);
    builder
        .append("x,y: ").append("(" + x2 + "," + y2 + ")").append('\n')
        .append("angle: ").append(angle).append('\n')
        .append("dir4: ").append(Direction.radiansToDirection(angle, 4)).append('\n')
        .append("dir8: ").append(Direction.radiansToDirection(angle, 8)).append('\n')
        .append("dir16: ").append(Direction.radiansToDirection(angle, 16)).append('\n')
        .append("dir32: ").append(Direction.radiansToDirection(angle, 32)).append('\n');

    shapes.begin(ShapeRenderer.ShapeType.Line);
    drawDiamond(shapes, x1 - 80, y1 - 40, 160, 80);
    shapes.line(x1, y1, x1 + x, y1 + y);
    shapes.end();

    batch.begin();
    font.draw(batch, builder.toString(), 0, Gdx.graphics.getHeight());
    batch.end();
  }

  @Override
  public void dispose() {
    font.dispose();
    batch.dispose();
    shapes.dispose();
  }

  static void drawDiamond(ShapeRenderer shapes, float x, float y, int width, int height) {
    int hw = width >>> 1;
    int hh = height >>> 1;
    shapes.line(x, y + hh, x + hw, y + height);
    shapes.line(x + hw, y + height, x + width, y + hh);
    shapes.line(x + width, y + hh, x + hw, y);
    shapes.line(x + hw, y, x, y + hh);
  }
}
