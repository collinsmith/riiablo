package com.riiablo.profiler;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import com.riiablo.Riiablo;

/**
 * Example profiling system.
 *
 * @author piotr-j
 * @author Daan van Yperen
 * @see ProfilerPlugin
 */
@Wire
public class ProfilerSystem extends BaseSystem {

  public static final int DEFAULT_PROFILER_KEY = Input.Keys.P;

  OrthographicCamera camera;
  ShapeRenderer renderer;
  Stage stage;
  Skin skin;

  private int key = DEFAULT_PROFILER_KEY;

  ProfilerManager profilers;
  SystemProfilerGUI gui;
  private boolean f3ButtonDown;
  private boolean initialized;
  private final Vector2 tmpVec2 = new Vector2();

  public boolean hit() {
    if (!gui.isVisible()) return false;
    stage.screenToStageCoordinates(tmpVec2.set(Gdx.input.getX(), Gdx.input.getY()));
    return stage.hit(tmpVec2.x, tmpVec2.y, true) != null;
  }

  @Override
  protected void initialize() {
    camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    camera.setToOrtho(false);
    camera.update();
    renderer = new ShapeRenderer();
    stage = new Stage();
    stage.getBatch().setProjectionMatrix(camera.combined);
    skin = new Skin(Gdx.files.internal("profiler/uiskin.json"));

    // setup some static config like colors etc
    SystemProfilerGUI.GRAPH_H_LINE.set(Color.ORANGE);
    gui = new SystemProfilerGUI(skin, "default");
    gui.setResizeBorder(8);
    gui.show(stage);
    world.inject(gui, true);
    gui.initialize();
  }

  @Override
  protected void processSystem() {
    if (!isEnabled() || !isConfigured()) {
      return;
    }

    if (!Riiablo.console.isVisible()) {
      checkActivationButton();
    }

    if (gui.isVisible()) {
      processInput();
      render();
    }
  }

  private boolean isConfigured() {
    return gui.getParent() != null;
  }

  private void render() {
    stage.act(world.delta);
    stage.draw();
    renderer.setProjectionMatrix(camera.combined);
    renderer.begin(ShapeRenderer.ShapeType.Line);
    if (!initialized) {
      gui.setWidth(stage.getWidth()); // initial width not applying properly otherwise
      initialized = true;
    }
    gui.updateAndRender(world.delta, renderer);
    renderer.end();
  }

  private void checkActivationButton() {
    if (Gdx.input.isKeyPressed(key)) {
      if (!f3ButtonDown) {
        if (!gui.isVisible()) {
          gui.setHeight(stage.getHeight() / 2);
          gui.setY(stage.getHeight(), Align.topLeft);
          gui.setVisible(true);
          profilers.getFor(this).gpu = true;
        } else if (gui.getHeight() != stage.getHeight()) {
          gui.setHeight(stage.getHeight());
        } else {
          gui.setVisible(false);
          profilers.getFor(this).gpu = false;
        }
      }
      f3ButtonDown = true;
    } else f3ButtonDown = false;
  }

  private boolean leftMouseDown;

  /**
   * Emulate stage input to maintain pre-existing input processor.
   */
  private void processInput() {
    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
      if (!leftMouseDown) {
        leftMouseDown = true;
        stage.touchDown(Gdx.input.getX(), Gdx.input.getY(), 0, Input.Buttons.LEFT);
      } else {
        stage.touchDragged(Gdx.input.getX(), Gdx.input.getY(), 0);
      }
    } else if (leftMouseDown) {
      leftMouseDown = false;
      stage.touchUp(Gdx.input.getX(), Gdx.input.getY(), 0, Input.Buttons.LEFT);
    }
  }

  public int getKey() {
    return key;
  }

  public void setKey(int key) {
    this.key = key;
  }
}
