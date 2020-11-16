package com.riiablo.camera;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.codec.Animation;
import com.riiablo.map.DT1.Tile;
import com.riiablo.util.DebugUtils;

public class CameraTool extends ApplicationAdapter {
  private static final String TAG = "CameraTool";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = TAG;
    config.resizable = false;
    config.width = 1280;
    config.height = 720;
    config.vSyncEnabled = false;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new CameraTool(), config);
  }

  Batch batch;
  BitmapFont font;
  ShapeRenderer shapes;
  IsometricCamera iso;
  Matrix4 center = new Matrix4();
  Matrix4 idt = new Matrix4();
  float accumulator;

  @Override
  public void create() {
    batch = new SpriteBatch();
    font = new BitmapFont();
    iso = new IsometricCamera();
    shapes = new ShapeRenderer();
    shapes.setAutoShapeType(true);
    center.set(shapes.getTransformMatrix()).translate(
        Gdx.graphics.getWidth()  / 2,
        Gdx.graphics.getHeight() / 2,
        0);
    idt.set(shapes.getProjectionMatrix());

    iso.setToOrtho(false);
    iso.offset(0, -Tile.SUBTILE_HEIGHT50);
    iso.set(0, 0);

    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);

    Gdx.input.setInputProcessor(new InputAdapter() {
      @Override
      public boolean scrolled(int amount) {
        iso.zoom += (amount * 0.05);
        iso.zoom = MathUtils.clamp(iso.zoom, 0.05f, 2f);
        return super.scrolled(amount);
      }

      @Override
      public boolean keyDown(int keycode) {
        final float AMOUNT = 0.25f;
        switch (keycode) {
          case Keys.SPACE:
            iso.set(0, 0);
            break;

          case Keys.SHIFT_LEFT:
            if (Gdx.input.isTouched()) break;
            moveToCursor(AMOUNT);
            break;

          case Keys.W:
          case Keys.UP:
            iso.translate(0, -AMOUNT);
            break;

          case Keys.S:
          case Keys.DOWN:
            iso.translate(0,  AMOUNT);
            break;

          case Keys.A:
          case Keys.LEFT:
            iso.translate(-AMOUNT, 0);
            break;

          case Keys.D:
          case Keys.RIGHT:
            iso.translate( AMOUNT, 0);
            break;
        }
        return super.keyDown(keycode);
      }
    });
  }

  private void moveToCursor(float amount) {
    vec2.set(Gdx.input.getX(), Gdx.input.getY());
    iso.unproject(vec2);
    iso.toWorld(vec2);
    vec2.sub(iso.position);
    float angle = vec2.angleRad();
    vec2.x = amount * MathUtils.cos(angle);
    vec2.y = amount * MathUtils.sin(angle);
    iso.translate(vec2);
  }

  final Vector2 vec2 = new Vector2();
  final Vector2 loc  = new Vector2();
  final Vector2 wrld = new Vector2();
  final Vector2 pos  = new Vector2();
  final Vector2 tmp  = new Vector2();
  final StringBuilder builder = new StringBuilder();

  @Override
  public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    final float delta = Gdx.graphics.getDeltaTime();
    final boolean touched = Gdx.input.isTouched();
    accumulator += delta;
    while (accumulator >= Animation.FRAME_DURATION) {
      accumulator -= Animation.FRAME_DURATION;
      if (touched) moveToCursor(0.25f);
    }

    iso.update();

    shapes.setTransformMatrix(center);
    shapes.setProjectionMatrix(idt);
    shapes.begin(ShapeRenderer.ShapeType.Line); {
      shapes.setColor(Color.GREEN);
      shapes.line(-Gdx.graphics.getWidth() / 2, 0, Gdx.graphics.getWidth() / 2, 0);
      shapes.line(0, -Gdx.graphics.getHeight() / 2, 0, Gdx.graphics.getHeight() / 2);

//    shapes.setColor(Color.BLUE);
//    shapes.rect(-80, -40, 160, 80);
    } shapes.end();

    shapes.identity();
    shapes.setProjectionMatrix(iso.combined);
    shapes.begin(ShapeRenderer.ShapeType.Line); {
      shapes.setColor(Color.RED);
      shapes.line(0, 0, 8, 0);
      shapes.line(0, 0, 0, 8);

      shapes.setColor(Color.WHITE);
      for (int x = -320; x < 320; x += 32) {
        for (int y = -160; y <= 160; y += 16) {
          shapes.line(x, y, x + 32, y + 16);
          shapes.line(x, y + 16, x + 32, y);
        }
      }

      shapes.setColor(Color.BLUE);
      shapes.line(-320, 160, 320, -160);
      shapes.line(-320, -160, 320, 160);
    } shapes.end();

    shapes.begin(ShapeRenderer.ShapeType.Line); {
      vec2.set(Gdx.input.getX(), Gdx.input.getY());
      iso.unproject(vec2);
      iso.toWorld(vec2);
      wrld.set(vec2);
      iso.toTile(vec2);
      loc.set(vec2);
      iso.toScreen(vec2);

      shapes.set(ShapeRenderer.ShapeType.Filled);
      shapes.setColor(Color.SALMON);
      DebugUtils.drawDiamond(shapes, vec2.x, vec2.y, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);

      shapes.set(ShapeRenderer.ShapeType.Filled);
      shapes.setColor(Color.GREEN);
      shapes.rect(vec2.x - 0.5f, vec2.y - 0.5f, 1, 1);
    } shapes.end();

    pos.set(iso.position);
    iso.toTile(pos);

    shapes.begin(ShapeRenderer.ShapeType.Filled); {
      vec2.set(pos);
      iso.toScreen(vec2);

      shapes.setColor(Color.WHITE);
      DebugUtils.drawDiamond(shapes, vec2.x, vec2.y, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
    } shapes.end();

    shapes.begin(ShapeRenderer.ShapeType.Line); {
      tmp.set(iso.position);
      iso.toScreen(tmp);
      shapes.rect(
          tmp.x - iso.viewportWidth  / 2 - 1,
          tmp.y - iso.viewportHeight / 2 - 1,
          iso.viewportWidth  + 2,
          iso.viewportHeight + 2
      );
    } shapes.end();

    float width;
    batch.begin();
    builder.setLength(0);
    builder
        .append("iso:").append('\n')
        .append("pos:").append('\n')
        .append("cursor:").append('\n')
        .append("world:").append('\n')
        .append("px:").append('\n')
        ;
    GlyphLayout layout = font.draw(batch, builder.toString(), 0, Gdx.graphics.getHeight());
    width = layout.width;
    builder.setLength(0);
    builder
        .append(iso.position).append('\n')
        .append(pos).append('\n')
        .append(loc).append('\n')
        .append(wrld).append('\n')
        .append(vec2).append('\n')
        ;
    font.draw(batch, builder.toString(), width, Gdx.graphics.getHeight());
    batch.end();
  }

  @Override
  public void dispose() {
    font.dispose();
    batch.dispose();
    shapes.dispose();
  }
}
