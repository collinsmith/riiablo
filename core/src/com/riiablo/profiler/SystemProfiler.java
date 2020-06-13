package com.riiablo.profiler;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.utils.ArtemisProfiler;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.TimeUtils;

import com.riiablo.util.ClassUtils;

/**
 * {@link ArtemisProfiler} implementation
 *
 * @author piotr-j
 */
public class SystemProfiler implements ArtemisProfiler {
  /**
   * Samples to store per system, only changes before initialization have effect
   */
  public static int SAMPLES = 60 * 5;
  private boolean RUNNING = true;

  boolean added; // If this profiler was already added via SystemProfiler.add()

  protected long[] times = new long[SAMPLES];
  protected int index;
  protected long max;
  protected int lastMaxCounter;
  protected long localMax;
  protected long localMaxIndex;
  protected int samples;

  protected long total;
  protected long sample;

  protected Color color;
  protected String name;
  protected BaseSystem system;
  protected boolean gpu;

  SystemProfiler() {}

  /**
   * Create not initialized profiler, must be initialized with {@link
   * SystemProfiler#initialize(BaseSystem, World)}
   *
   * @param name of the profiler
   */
  SystemProfiler(String name) {
    this.name = name;
  }

  /**
   * Create profiler with default name and initialize it
   *
   * @param system to profiler
   * @param world  to init with
   */
  SystemProfiler(BaseSystem system, World world) {
    this(null, system, world);
  }

  /**
   * Create profiler with specified name and initialize it
   *
   * @param name   of the profiler
   * @param system to profiler
   * @param world  to init with
   */
  SystemProfiler(String name, BaseSystem system, World world) {
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
    sample = time;
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
