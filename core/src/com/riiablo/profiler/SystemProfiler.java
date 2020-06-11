package com.riiablo.profiler;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.utils.ArtemisProfiler;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;

import com.riiablo.util.ClassUtils;

/**
 * {@link ArtemisProfiler} implementation, {@link SystemProfiler#dispose()} should be called to
 * clean static references as needed
 *
 * @author piotr-j
 */
public class SystemProfiler implements ArtemisProfiler {
  /**
   * Samples to store per system, only changes before initialization have effect
   */
  public static int SAMPLES = 60 * 5;
  private static boolean RUNNING = true;

  private static Array<SystemProfiler> profilers = new Array<>(SystemProfiler.class);
  private static ObjectMap<String, SystemProfiler> profilerByName = new ObjectMap<>();

  /**
   * Add manually created profiler
   *
   * @param profiler to add
   *
   * @return added profiler
   */
  public static SystemProfiler add(SystemProfiler profiler) {
    if (profiler.added) return profiler;
    profiler.added = true;
    profilers.add(profiler);
    profilerByName.put(profiler.getName(), profiler);
    return profiler;
  }

  /**
   * @param name of the profiler
   *
   * @return profiler registered with given name or null
   */
  public static SystemProfiler get(String name) {
    return profilerByName.get(name, null);
  }

  /**
   * @param index of profiler to get, no bounds check!
   *
   * @return profiler with given index
   */
  public static SystemProfiler get(int index) {
    return profilers.get(index);
  }

  /**
   * Get profiler for given system
   *
   * @return profiler or null
   */
  public static SystemProfiler getFor(BaseSystem system) {
    Object[] items = profilers.items;
    for (int i = 0; i < profilers.size; i++) {
      SystemProfiler profiler = (SystemProfiler) items[i];
      if (profiler.system == system) {
        return profiler;
      }
    }
    return null;
  }

  /**
   * Create a profiler with given name
   *
   * @param name of the profiler
   */
  public static SystemProfiler create(String name) {
    return SystemProfiler.add(new SystemProfiler(name));
  }

  /**
   * Create a profiler for given system
   *
   * @param system to profiler
   * @param world  to init profiler with
   *
   * @return created profiler
   */
  public static SystemProfiler createFor(BaseSystem system, World world) {
    return SystemProfiler.add(new SystemProfiler(system, world));
  }

  /**
   * @return {@link Array} with all registered profilers
   */
  public static Array<SystemProfiler> get() {
    return profilers;
  }

  /**
   * @return number of registered profilers
   */
  public static int size() {
    return profilers.size;
  }


  /**
   * Pause all profilers
   */
  public static void pause() {
    RUNNING = false;
  }

  /**
   * Resume all profilers
   */
  public static void resume() {
    RUNNING = true;
  }

  /**
   * @return if profilers are running
   */
  public static boolean isRunning() {
    return RUNNING;
  }

  /**
   * Clear registered profilers, should be called when {@link World} is disposed
   */
  public static void dispose() {
    profilers.clear();
    profilerByName.clear();
  }

  /*
      If this profiler was already added via SystemProfiler.add()
   */
  private boolean added;

  protected long[] times = new long[SAMPLES];
  protected int index;
  protected long max;
  protected int lastMaxCounter;
  protected long localMax;
  protected long localMaxIndex;
  protected int samples;

  protected long total;

  protected Color color;
  protected String name;
  protected BaseSystem system;
  protected boolean gpu;

  public SystemProfiler() {}

  /**
   * Create not initialized profiler, must be initialized with {@link
   * SystemProfiler#initialize(BaseSystem, World)}
   *
   * @param name of the profiler
   */
  public SystemProfiler(String name) {
    this.name = name;
  }

  /**
   * Create profiler with default name and initialize it
   *
   * @param system to profiler
   * @param world  to init with
   */
  public SystemProfiler(BaseSystem system, World world) {
    this(null, system, world);
  }

  /**
   * Create profiler with specified name and initialize it
   *
   * @param name   of the profiler
   * @param system to profiler
   * @param world  to init with
   */
  public SystemProfiler(String name, BaseSystem system, World world) {
    this.name = name;
    initialize(system, world);
  }

  @Override
  public void initialize(BaseSystem baseSystem, World world) {
    system = baseSystem;
    gpu = ClassUtils.hasAnnotation(baseSystem.getClass(), GpuSystem.class);
    if (name == null) {
      name = toString();
    }
    if (color == null) {
      calculateColor(toString().hashCode(), color = new Color());
    }
    SystemProfiler.add(this);
  }

  private long startTime;

  @Override
  public void start() {
    startTime = TimeUtils.nanoTime();
  }

  @Override
  public void stop() {
    long time = TimeUtils.nanoTime() - startTime;
    if (RUNNING) sample(time);
  }

  public long getAverage() {
    return samples == 0 ? 0 : total / Math.min(times.length, samples);
  }

  public float getMax() {
    return max / 1000000f;
  }

  public float getLocalMax() {
    return localMax / 1000000f;
  }

  public float getMovingAvg() {
    return getAverage() / 1000000f;
  }

  /**
   * Create a sample with specified time
   *
   * @param time in nanoseconds
   */
  public void sample(long time) {
    lastMaxCounter++;
    if (time > max || lastMaxCounter > 2000) {
      max = time;
      lastMaxCounter = 0;
    }

    if (time > localMax || index == localMaxIndex) {
      localMax = time;
      localMaxIndex = index;
    }
    total -= times[index];
    samples++;
    times[index] = time;
    total += time;
    if (++index == times.length) {
      index = 0;
    }
  }

  /**
   * Add time to current sample
   *
   * @param time in nanoseconds
   */
  public void add(long time) {
    times[index] += time;
    total += time;
  }

  public int getCurrentSampleIndex() {
    return index;
  }

  public int getTotalSamples() {
    return samples;
  }

  public long[] getSampleData() {
    return times;
  }

  @Override
  public String toString() {
    return name != null ? name :
        system != null ? system.getClass().getSimpleName() : "<dummy>";
  }

  public String getName() {
    return name;
  }

  /**
   * @return color assigned to this profiler, maybe null if it is not initialized
   */
  public Color getColor() {
    return color;
  }

  public void setColor(float r, float g, float b, float a) {
    if (color == null) {
      color = new Color();
    }
    color.set(r, g, b, a);
  }

  boolean drawGraph = true;

  public boolean getDrawGraph() {
    return drawGraph;
  }

  public void setDrawGraph(boolean drawGraph) {
    this.drawGraph = drawGraph;
  }

  /**
   * Calculates semi unique color from given hash
   */
  public static Color calculateColor(int hash, Color color) {
    float hue = (hash % 333) / 333f;
    float saturation = ((hash % 271) / 271f) * 0.2f + 0.8f;
    float brightness = ((hash % 577) / 577f) * 0.1f + 0.9f;

    int r = 0, g = 0, b = 0;
    if (saturation == 0) {
      r = g = b = (int) (brightness * 255.0f + 0.5f);
    } else {
      float h = (hue - (float) Math.floor(hue)) * 6.0f;
      float f = h - (float) Math.floor(h);
      float p = brightness * (1.0f - saturation);
      float q = brightness * (1.0f - saturation * f);
      float t = brightness * (1.0f - (saturation * (1.0f - f)));
      switch ((int) h) {
        case 0:
          r = (int) (brightness * 255.0f + 0.5f);
          g = (int) (t * 255.0f + 0.5f);
          b = (int) (p * 255.0f + 0.5f);
          break;
        case 1:
          r = (int) (q * 255.0f + 0.5f);
          g = (int) (brightness * 255.0f + 0.5f);
          b = (int) (p * 255.0f + 0.5f);
          break;
        case 2:
          r = (int) (p * 255.0f + 0.5f);
          g = (int) (brightness * 255.0f + 0.5f);
          b = (int) (t * 255.0f + 0.5f);
          break;
        case 3:
          r = (int) (p * 255.0f + 0.5f);
          g = (int) (q * 255.0f + 0.5f);
          b = (int) (brightness * 255.0f + 0.5f);
          break;
        case 4:
          r = (int) (t * 255.0f + 0.5f);
          g = (int) (p * 255.0f + 0.5f);
          b = (int) (brightness * 255.0f + 0.5f);
          break;
        case 5:
          r = (int) (brightness * 255.0f + 0.5f);
          g = (int) (p * 255.0f + 0.5f);
          b = (int) (q * 255.0f + 0.5f);
          break;
      }
    }

    return color.set(r / 255f, g / 255f, b / 255f, 1);
  }
}
