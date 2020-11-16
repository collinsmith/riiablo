package com.riiablo.profiler;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Sort;

/**
 * Gui for SystemProfilers implemented in Scene2d
 *
 * Graph must be renderer separately with shape renderer, after stage.draw()
 *
 * Certain static values can be changed before creation to modify behaviour
 *
 * Dynamic modification of profilers is not supported
 *
 * See example implementation of how this class can be used
 *
 * @author piotr-j
 */
public class SystemProfilerGUI extends Window {
  public static final Color GRAPH_V_LINE = new Color(0.6f, 0.6f, 0.6f, 1);
  public static final Color GRAPH_H_LINE = new Color(0.25f, 0.25f, 0.25f, 1);
  public static float FADE_TIME = 0.3f;
  public static float PRECISION = 0.01f;
  public static String FORMAT = "%.2f";
  public static String STYLE_SMALL = "default";
  /**
   * Min width of label with values
   */
  public static float MIN_LABEL_WIDTH = 75;
  public static float GRAPH_MIN_WIDTH = 300;
  public static float GRAPH_MIN_HEIGHT = 200;
  /**
   * How many systems to graph at most
   */
  public static int DRAW_MAX_COUNT = 15;
  /**
   * How often should text update
   */
  public static float REFRESH_RATE = 0.25f;

  protected Skin skin;
  protected Table profilerLabels;
  protected Graph graph;
  protected Table profilersTable;
  protected Array<ProfilerRow> rows = new Array<>();

  protected ProfilerManager profilers;

  public SystemProfilerGUI(Skin skin, String style) {
    super("Profiler", skin, style);
    this.skin = skin;

    setVisible(false);
    setResizable(true);
    setResizeBorder(12);
    TextButton closeButton = new TextButton("X", skin);
    getTitleTable().add(closeButton).padRight(3);
    closeButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        hide();
      }
    });
  }

  public void initialize() {
    Table graphTable = new Table();
    Table graphLabels = new Table();
    for (int i = 32; i >= 0; i /= 2) {
      graphLabels.add(label(Integer.toString(i), skin)).expandY().center().row();
      if (i == 0) break;
    }
    graphTable.add(graphLabels).expandY().fillY();

    graphTable.add(graph = new Graph()).expand().fill();

    profilerLabels = new Table();
    profilerLabels.add().expandX().fillX();
    profilerLabels.add(label("max", skin, Align.right)).minWidth(MIN_LABEL_WIDTH);
    profilerLabels.add(label("lmax", skin, Align.right)).minWidth(MIN_LABEL_WIDTH);
    profilerLabels.add(label("avg", skin, Align.right)).minWidth(MIN_LABEL_WIDTH);

    for (SystemProfiler profiler : profilers.get()) {
      rows.add(new ProfilerRow(profiler, skin));
    }
    profilersTable = new Table();
    // basic once so we can get all profilers and can pack nicely
    act(0);

    ScrollPane pane = new ScrollPane(profilersTable);
    pane.setScrollingDisabled(true, false);
    add(graphTable).expand().fill();
    add(pane).fillX().pad(0, 10, 10, 10).top()
        .prefWidth(MIN_LABEL_WIDTH * 7).minWidth(0);
    pack();
  }

  private static Label label(String text, Skin skin) {
    return label(text, skin, Align.left);
  }

  private static Label label(String text, Skin skin, int align) {
    Label label = new Label(text, skin, STYLE_SMALL);
    label.setAlignment(align);
    return label;
  }


  public void updateAndRender(float delta, ShapeRenderer renderer) {
    update(delta);
    renderGraph(renderer);
  }

  float refreshTimer = REFRESH_RATE;
  Comparator<ProfilerRow> byAvg = new Comparator<ProfilerRow>() {
    @Override
    public int compare(ProfilerRow o1, ProfilerRow o2) {
      return (int) (o2.getAverage() - o1.getAverage());
    }
  };

  /**
   * Call to update, rate limited by {@link SystemProfilerGUI#REFRESH_RATE}
   *
   * This is not in {@link Window#act(float)} to avoid polluting results of actual system with stage
   * if one exists
   *
   * @param delta duration of last frame
   */
  public void update(float delta) {
    refreshTimer += delta;
    if (refreshTimer < REFRESH_RATE) return;
    refreshTimer -= REFRESH_RATE;

    if (rows.size != profilers.size()) {
      rebuildRows();
    }

    Sort.instance().sort(rows, byAvg);

    profilersTable.clear();
    profilersTable.add(profilerLabels).expandX().fillX().right();
    profilersTable.row();

    for (ProfilerRow row : rows) {
      row.update();
      profilersTable.add(row).expandX().fillX().left();
      profilersTable.row();
    }
  }

  private void rebuildRows() {
    int target = profilers.size();
    if (target > rows.size) {
      for (int i = rows.size; i < target; i++) {
        rows.add(new ProfilerRow(skin));
      }
    } else if (target < rows.size) {
      rows.removeRange(rows.size - target + 1, rows.size - 1);
    }
    for (int i = 0; i < target; i++) {
      SystemProfiler profiler = profilers.get(i);
      rows.get(i).init(profiler);
    }
  }

  private Vector2 temp = new Vector2();

  /**
   * Render graph for profilers, should be called after {@link Stage#draw()} so it is on top of the
   * gui
   *
   * @param renderer {@link ShapeRenderer} to use, must be ready and set to Line type
   */
  public void renderGraph(ShapeRenderer renderer) {
    graph.localToStageCoordinates(temp.setZero());
    drawGraph(renderer, temp.x, temp.y, graph.getWidth(), graph.getHeight(), getColor().a);
  }

  /**
   * Render graph for profilers in a given bounds
   *
   * @param renderer {@link ShapeRenderer} to use, must be ready and set to Line type
   */
  public void drawGraph(ShapeRenderer renderer, float x, float y, float width, float height, float alpha) {
    Gdx.gl.glEnable(GL20.GL_BLEND);
    // we do this so the logical 0 and top are in the middle of the labels
    drawGraphAxis(renderer, x, y, width, height, alpha);
    float sep = height / 7;
    y += sep / 2;
    height -= sep;
    graphProfileTimes(renderer, x, y, width, height, alpha);
  }

  private void drawGraphAxis(ShapeRenderer renderer, float x, float y, float width, float height, float alpha) {
    float sep = height / 7;
    y += sep / 2;
    renderer.setColor(GRAPH_V_LINE.r, GRAPH_V_LINE.g, GRAPH_V_LINE.b, alpha);
    renderer.line(x, y, x, y + height - sep);
    renderer.line(x + width, y, x + width, y + height - sep);

    renderer.setColor(GRAPH_H_LINE.r, GRAPH_H_LINE.g, GRAPH_H_LINE.b, alpha);
    for (int i = 0; i < 7; i++) {
      renderer.line(x, y + i * sep, x + width, y + i * sep);
    }
  }

  private static final float NANO_MULTI = 1 / 1000000f;

  static Comparator<SystemProfiler> byLocalMax = new Comparator<SystemProfiler>() {
    @Override
    public int compare(SystemProfiler o1, SystemProfiler o2) {
      return (int) (o2.getLocalMax() - o1.getLocalMax());
    }
  };

  private void graphProfileTimes(ShapeRenderer renderer, float x, float y, float width, float height, float alpha) {
    Sort.instance().sort(profilers.get(), byLocalMax);
    int drawn = 0;
    for (SystemProfiler profiler : profilers.get()) {
      if (!profiler.getDrawGraph())
        continue;
      if (drawn++ > DRAW_MAX_COUNT)
        break;

      renderer.setColor(profiler.getColor());
      renderer.getColor().a = alpha;

      // distance between 2 point
      float sampleLen = width / profiler.times.length;

      int current = profiler.getCurrentSampleIndex();
      int skip = current;
      long[] times = profiler.getSampleData();
      float currentPoint = getPoint(times[current] * NANO_MULTI);

      for (int i = times.length - 1; i >= 1; i--) {
        int prev = current == 0 ? times.length - 1 : current - 1;
        float prevPoint = getPoint(times[prev] * NANO_MULTI);
        // we want do skip line between actaul first and last points, as that may result in ugly line at the edge
        if (current != skip && currentPoint > 0)
          renderer.line(x + (i - 1) * sampleLen, y + prevPoint * height / 6, x + i * sampleLen, y + currentPoint * height / 6);
        current = prev;
        currentPoint = prevPoint;
      }
    }
  }

  private static float getPoint(float sampleValue) {
    return sampleValue < 1 ? sampleValue : (MathUtils.log2(sampleValue) + 1);
  }

  /**
   * Single row for profiler list
   */
  private static class ProfilerRow extends Table {
    SystemProfiler profiler;
    Label name, max, localMax, avg;
    CheckBox draw;
    float lastMax, lastLocalMax, lastAvg;
    ChangeListener listener;

    public ProfilerRow(Skin skin) {
      this(null, skin);
    }

    public ProfilerRow(SystemProfiler profiler, Skin skin) {
      super();
      draw = new CheckBox("", skin);
      name = new Label("", skin, STYLE_SMALL);
      name.setEllipsis(true);
      max = label("", skin, Align.right);
      localMax = label("", skin, Align.right);
      avg = label("", skin, Align.right);
      add(draw);
      add(name).expandX().fillX();
      ;
      add(max).minWidth(MIN_LABEL_WIDTH);
      add(localMax).minWidth(MIN_LABEL_WIDTH);
      add(avg).minWidth(MIN_LABEL_WIDTH);

      if (profiler != null) init(profiler);
    }

    public void init(final SystemProfiler profiler) {
      this.profiler = profiler;
      if (listener != null) draw.removeListener(listener);
      draw.setChecked(profiler.getDrawGraph());
      draw.addListener(listener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          profiler.setDrawGraph(!profiler.getDrawGraph());
          if (profiler.getDrawGraph()) {
            setChildColor(profiler.getColor());
          } else {
            setChildColor(Color.LIGHT_GRAY);
          }
        }
      });
      name.setText(profiler.getName());
      setChildColor(profiler.getColor());
      lastMax = lastLocalMax = lastAvg = -1;
      invalidateHierarchy();
      layout();
    }

    private void setChildColor(Color color) {
      name.setColor(color);
      max.setColor(color);
      localMax.setColor(color);
      avg.setColor(color);
    }

    public void update() {
      // we don't want to update if the change wont affect the representation
      if (!MathUtils.isEqual(lastMax, profiler.getMax(), PRECISION)) {
        lastMax = profiler.getMax();
        max.setText(timingToString(lastMax));
      }
      if (!MathUtils.isEqual(lastLocalMax, profiler.getLocalMax(), PRECISION)) {
        lastLocalMax = profiler.getLocalMax();
        localMax.setText(timingToString(lastLocalMax));
      }
      if (!MathUtils.isEqual(lastAvg, profiler.getMovingAvg(), PRECISION)) {
        lastAvg = profiler.getMovingAvg();
        avg.setText(timingToString(lastAvg));
      }
    }

    private String timingToString(float var) {
      int decimals = (int) (var * 100) % 100;
      return Integer.toString((int) (var)) + (decimals < 10 ? ".0" : ".") + Integer.toString(decimals);
    }

    public float getAverage() {
      return profiler.getAverage();
    }

    public float getLocalMax() {
      return profiler.getLocalMax();
    }

    public SystemProfiler getProfiler() {
      return profiler;
    }

    public float getMax() {
      return profiler.getMax();
    }
  }

  /**
   * Simple placeholder for actual graph
   */
  private class Graph extends Table {
    public Graph() {}

    @Override
    public float getMinWidth() {
      return GRAPH_MIN_WIDTH;
    }

    @Override
    public float getMinHeight() {
      return GRAPH_MIN_HEIGHT;
    }
  }

  /**
   * Show the profiler window
   *
   * @param stage stage to add to
   */
  public void show(Stage stage) {
    stage.addActor(this);
    setColor(1, 1, 1, 0);
    addAction(Actions.fadeIn(FADE_TIME, Interpolation.fade));
  }

  /**
   * Hide the window and remove from the stage
   */
  public void hide() {
    addAction(Actions.sequence(Actions.fadeOut(FADE_TIME, Interpolation.fade), Actions.removeActor()));
  }
}
