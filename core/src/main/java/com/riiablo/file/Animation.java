package com.riiablo.file;

import java.util.Arrays;
import org.apache.commons.lang3.Validate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import com.riiablo.Riiablo;
import com.riiablo.codec.Index;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

import static com.riiablo.file.Dc.toRealDir;

public class Animation extends BaseDrawable implements Pool.Poolable {
  private static final Logger log = LogManager.getLogger(Animation.class);
  private static final int DEBUG_MODE = 2; // 0=off, 1=box, 2=layer box

  private static final int   NUM_LAYERS = Cof.Component.NUM_COMPONENTS;
  public  static final float FRAMES_PER_SECOND = 25f;
  public  static final float FRAME_DURATION = 1 / FRAMES_PER_SECOND;

  private static final Color   SHADOW_TINT      = Riiablo.colors.modal75;
  private static final Affine2 SHADOW_TRANSFORM = new Affine2();

  Cof cof;

  int numFrames;
  int numDirections;
  int direction;
  int frame;

  int startIndex;
  int endIndex;

  float frameDuration = FRAME_DURATION;
  float elapsedTime;

  public enum Mode { ONCE, LOOP, CLAMP }
  Mode mode = Mode.LOOP;

  boolean reversed;

  boolean highlighted;

  final Layer layers[] = new Layer[NUM_LAYERS];
  final BBox  box      = new BBox();

  private final IntMap<Array<AnimationListener>> EMPTY_MAP = new IntMap<>(0);
  private IntMap<Array<AnimationListener>> animationListeners = EMPTY_MAP;

  public static Animation newAnimation() {
    return new Animation();
  }

  public static Animation newAnimation(Dc dc) {
    return Animation.builder().layer(dc).build();
  }

  public static Animation newAnimation(Cof cof) {
    Animation animation = newAnimation();
    animation.setCof(cof);
    return animation;
  }

  @Override
  public void reset() {
    cof           = null;
    numFrames     = 0;
    numDirections = 0;
    frame         = 0;
    direction     = 0;
    startIndex    = 0;
    endIndex      = 0;
    mode          = Mode.LOOP;
    reversed      = false;
    frameDuration = FRAME_DURATION;
    highlighted   = false;
    Layer.freeAll(layers);
    box.reset();
    animationListeners = EMPTY_MAP;
  }

  public int getNumDirections() {
    return numDirections;
  }

  public int getNumFramesPerDir() {
    return numFrames;
  }

  public int getDirection() {
    return direction;
  }

  public void setDirection(int d) {
    if (d != direction) {
      Validate.isTrue(0 <= d && d < numDirections, "Invalid direction: " + d);
      direction = d;
    }
  }

  public int getFrame() {
    return frame;
  }

  public void setFrame(int f) {
    if (f != frame) {
      Validate.isTrue(0 <= f && f < numFrames, "Invalid frame: " + f);
      frame = f;
      elapsedTime = frameDuration * frame;
      // if (frame == endIndex - 1) notifyAnimationFinished();
    }
  }

  public float getFrameDuration() {
    return frameDuration;
  }

  public void setFrameDuration(float f) {
    frameDuration = f;
    elapsedTime = frameDuration * frame;
  }

  public int getFrameDelta() {
    return MathUtils.roundPositive(256f / (frameDuration * FRAMES_PER_SECOND));
  }

  public void setFrameDelta(int delta) {
    setFrameDuration(256f / (delta * FRAMES_PER_SECOND));
  }

  public int getFrame(float stateTime) {
    int frameRange = endIndex - startIndex;
    if (frameRange <= 1) return startIndex;
    int frameNumber = (int) (stateTime / frameDuration);
    switch (mode) {
      case ONCE:  return startIndex + Math.min(frameRange, frameNumber);
      case LOOP:  return startIndex + (frameNumber % frameRange);
      case CLAMP: return startIndex + Math.min(frameRange - 1, frameNumber);
      default: throw new AssertionError("Invalid mode set: " + mode);
    }
  }

  public boolean isFinished() {
    return frame == endIndex - 1;
  }

  public boolean isLooping() {
    return mode == Mode.LOOP;
  }

  public boolean isClamped() {
    return mode == Mode.CLAMP;
  }

  public void setClamp(int startIndex, int endIndex) {
    setMode(Mode.CLAMP);
    this.startIndex = startIndex;
    this.endIndex   = endIndex;
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    assert mode != null;
    this.mode = mode;
  }

  public boolean isReversed() {
    return reversed;
  }

  public void setReversed(boolean b) {
    reversed = b;
  }

  public boolean isHighlighted() {
    return highlighted;
  }

  public void setHighlighted(boolean b) {
    if (highlighted != b) {
      highlighted = b;
      if (b) {
        if (cof == null) {
          for (int l = 0; l < NUM_LAYERS; l++) {
            Layer layer = layers[l];
            if (layer == null) break;
            if (layer.blendMode == BlendMode.ID) {
              layer.setBlendMode(BlendMode.BRIGHTEN, Riiablo.colors.highlight);
            }
          }
        } else {
          for (int l = 0; l < cof.numLayers(); l++) {
            Cof.Layer cofLayer = cof.layer(l);
            Layer layer = layers[cofLayer.component];
            if (layer != null && layer.blendMode == BlendMode.ID) { // FIXME: may be unnecessary in production
              layer.setBlendMode(BlendMode.BRIGHTEN, Riiablo.colors.highlight);
            }
          }
        }
      } else {
        if (cof == null) {
          for (int l = 0; l < NUM_LAYERS; l++) {
            Layer layer = layers[l];
            if (layer == null) break;
            if (layer.blendMode == BlendMode.BRIGHTEN) {
              layer.setBlendMode(BlendMode.ID);
            }
          }
        } else {
          for (int l = 0; l < cof.numLayers(); l++) {
            Cof.Layer cofLayer = cof.layer(l);
            Layer layer = layers[cofLayer.component];
            if (layer != null && layer.blendMode == BlendMode.BRIGHTEN) { // FIXME: may be unnecessary in production
              layer.setBlendMode(BlendMode.ID);
            }
          }
        }
      }
    }
  }

  public Layer getLayerRaw(int i) {
    return getLayer(i);
  }

  public Animation setLayerRaw(int i, Dc dc, boolean updateBox) {
    return setLayer(i, dc, updateBox);
  }

  public Layer getLayer(int component) {
    return layers[component];
  }

  public Animation setLayer(int component, Dc dc) {
    return setLayer(component, dc, true);
  }

  public Animation setLayer(int component, Dc dc, boolean updateBox) {
    if (dc == null) {
      Layer.free(layers, component);
    } else {
      if (layers[component] == null) {
        layers[component] = Layer.obtain(dc, Layer.DEFAULT_BLENDMODE);
      } else {
        layers[component].set(dc, Layer.DEFAULT_BLENDMODE);
      }
      if (updateBox) updateBox();
    }

    return this;
  }

  public Layer setLayer(Cof.Layer cofLayer, Dc dc, boolean updateBox) {
    setLayer(cofLayer.component, dc, updateBox);
    Layer layer = layers[cofLayer.component];
    if (layer != null && cofLayer.overrideTransLvl != 0) {
      applyTransform(layer, cofLayer.newTransLvl & 0xFF);
    }

    return layer;
  }

  private void applyTransform(Layer layer, int transform) {
    switch (transform) {
      case 0x00:
        layer.setBlendMode(layer.blendMode, Riiablo.colors.trans75);
        break;
      case 0x01:
        layer.setBlendMode(layer.blendMode, Riiablo.colors.trans50);
        break;
      case 0x02:
        layer.setBlendMode(layer.blendMode, Riiablo.colors.trans25);
        break;
      case 0x03:
        layer.setBlendMode(BlendMode.LUMINOSITY);
        break;
      case 0x04:
        layer.setBlendMode(BlendMode.LUMINOSITY); // not sure
        break;
      case 0x06:
        layer.setBlendMode(BlendMode.LUMINOSITY); // not sure
        break;
      default:
        log.error("Unknown transform: {}", transform);
    }
  }

  public Cof getCof() {
    return cof;
  }

  public boolean setCof(Cof cof) {
    if (this.cof != cof) {
      this.cof = cof;
      numDirections = cof.numDirections();
      numFrames = cof.numFrames();
      setFrameDelta(cof.animRate());

      if (direction >= numDirections) direction = 0; // FIXME: maybe not necessary if done correctly
      frame       = 0;
      elapsedTime = 0;

      startIndex = 0;
      endIndex   = numFrames;

      return true;
    }

    return false;
  }

  public BBox getBox() {
    return box;
  }

  public void updateBox() {
    if (cof == null) {
      box.reset();
      for (int l = 0; l < NUM_LAYERS; l++) {
        Layer layer = layers[l];
        if (layer == null) break;
        if (!layer.dc.loaded(direction)) continue;
        box.max(layer.dc.box(direction));
      }
    } else if (frame < numFrames) { // TODO: else assign box to cof.box for dir
      int d = toRealDir(direction, cof.numDirections());
      int f = frame;
      box.reset();
      for (int l = 0; l < cof.numLayers(); l++) {
        int component = cof.componentAt(d, f, l);
        Layer layer = layers[component];
        if (layer != null && layer.dc.loaded(d)) {
          box.max(layer.dc.box(d));
        }
      }
    }
  }

  @Override
  public float getMinWidth() {
    return box.width;
  }

  @Override
  public float getMinHeight() {
    return box.height;
  }

  public void act() {
    update();
  }

  public void act(float delta) {
    update(delta);
  }

  public void update() {
    update(Gdx.graphics.getDeltaTime());
  }

  public void update(float delta) {
    elapsedTime += delta;
    frame = getFrame(elapsedTime);
    if (reversed) frame = endIndex - 1 - frame;
    notifyListeners(frame);
    if (frame == endIndex - 1) notifyAnimationFinished();
  }

  public void drawDebug(ShapeRenderer shapes, float x, float y) {
    if (DEBUG_MODE == 0) {
      return;
    } else if (DEBUG_MODE == 1 || cof == null) {
      boolean reset = !shapes.isDrawing();
      if (reset) {
        shapes.begin(ShapeRenderer.ShapeType.Line);
      } else {
        shapes.set(ShapeRenderer.ShapeType.Line);
      }

      shapes.setColor(Color.RED);
      shapes.line(x, y, x + 50, y);
      shapes.setColor(Color.GREEN);
      shapes.line(x, y, x, y + 50);
      shapes.setColor(Color.BLUE);
      shapes.line(x, y, x + 15, y - 20);
      shapes.setColor(Color.GREEN);
      shapes.rect(x + box.xMin, y - box.yMax, box.width, box.height);
      if (reset) shapes.end();
    } else if (DEBUG_MODE == 2 && frame < numFrames) {
      int d = toRealDir(direction, cof.numDirections());
      int f = frame;
      for (int l = 0; l < cof.numLayers(); l++) {
        int component = cof.componentAt(d, f, l);
        Layer layer = layers[component];
        if (layer != null) layer.drawDebug(shapes, direction, f, x, y);
      }
    }
  }

  public void draw(Batch batch, float x, float y) {
    draw(batch, x, y, getMinWidth(), getMinHeight());
  }

  @Override
  public void draw(Batch batch, float x, float y, float width, float height) {
    draw((PaletteIndexedBatch) batch, x, y);
  }

  public void draw(PaletteIndexedBatch batch, float x, float y) {
    if (frame >= numFrames) return;
    if (cof == null) {
      for (Layer layer : layers) {
        if (layer == null) continue;
        drawLayer(batch, layer, x, y);
      }
    } else {
      int d = toRealDir(direction, cof.numDirections());
      int f = frame;
      // TODO: Layer blend modes should correspond with the cof trans levels
      for (int l = 0, numLayers = cof.numLayers(); l < numLayers; l++) {
        int component = cof.componentAt(d, f, l);
        Layer layer = layers[component];
        if (layer == null) continue;
        drawLayer(batch, layer, x, y);
      }
    }

    batch.resetBlendMode();
    batch.resetColormap();
  }

  public void drawLayer(PaletteIndexedBatch batch, Layer layer, float x, float y) {
    layer.draw(batch, direction, frame, x, y);
  }


  public void drawShadow(PaletteIndexedBatch batch, float x, float y) {
    drawShadow(batch, x, y, true);
  }

  public void drawShadow(PaletteIndexedBatch batch, float x, float y, boolean handleBlends) {
    if (handleBlends) batch.setBlendMode(BlendMode.SOLID, SHADOW_TINT);
    if (cof == null) {
      for (Layer layer : layers) {
        if (layer == null || !layer.shadow) continue;
        drawShadow(batch, layer, x, y);
      }
    } else if (frame < numFrames) {
      int d = toRealDir(direction, cof.numDirections());
      int f = frame;
      for (int l = 0; l < cof.numLayers(); l++) {
        int component = cof.componentAt(d, f, l);
        Layer layer = layers[component];
        if (layer != null) {
          Cof.Layer cofLayer = cof.layerAt(component);
          if (cofLayer.shadow == 0x1) {
            drawShadow(batch, layer, x, y);
          }
        }
      }
    }
    if (handleBlends) batch.resetBlendMode();
  }

  public void drawShadow(PaletteIndexedBatch batch, Layer layer, float x, float y) {
    if (frame >= numFrames) {
      return;
    }

    int d = direction;
    int f = frame;

    Dc dc    = layer.dc;
    BBox box = dc.box(d, f);

    SHADOW_TRANSFORM.idt();
    SHADOW_TRANSFORM.preTranslate(box.xMin, -(box.yMax / 2));
    SHADOW_TRANSFORM.preShear(-1.0f, 0);
    SHADOW_TRANSFORM.preTranslate(x, y);
    SHADOW_TRANSFORM.scale(1, 0.5f);

    if (f >= layer.numFrames) return; // FIXME: see #113
    TextureRegion region = layer.dc.direction(d).frame(f).texture();
    if (region.getTexture().getTextureObjectHandle() == 0) return;
    batch.draw(region, region.getRegionWidth(), region.getRegionHeight(), SHADOW_TRANSFORM);
  }

  private void notifyAnimationFinished() {
    if (animationListeners == EMPTY_MAP) return;
    Array<AnimationListener> listeners = animationListeners.get(-1);
    if (listeners == null) return;
    for (AnimationListener l : listeners) l.onTrigger(this, -1);
  }

  private void notifyListeners(int frame) {
    if (animationListeners == EMPTY_MAP) return;
    Array<AnimationListener> listeners = animationListeners.get(frame);
    if (listeners == null) return;
    for (AnimationListener l : listeners) l.onTrigger(this, frame);
  }

  public boolean addAnimationListener(int frame, AnimationListener l) {
    Validate.isTrue(l != null, "l cannot be null");
    if (animationListeners == EMPTY_MAP) animationListeners = new IntMap<>(1);
    Array<AnimationListener> listeners = animationListeners.get(frame);
    if (listeners == null) animationListeners.put(frame, listeners = new Array<>(1));
    listeners.add(l);
    return true;
  }

  public boolean removeAnimationListener(int frame, AnimationListener l) {
    if (l == null || animationListeners == EMPTY_MAP) return false;
    Array<AnimationListener> listeners = animationListeners.get(frame);
    if (listeners == null) return false;
    return listeners.removeValue(l, true);
  }

  public boolean containsAnimationListener(int frame, AnimationListener l) {
    if (l == null || animationListeners == EMPTY_MAP) return false;
    Array<AnimationListener> listeners = animationListeners.get(frame);
    if (listeners == null) return false;
    return listeners.contains(l, true);
  }

  public interface AnimationListener {
    void onTrigger(Animation animation, int frame);
  }

  public static class Layer implements Pool.Poolable {
    private static final Pool<Layer> pool = Pools.get(Layer.class, 1024);

    private final Color DEBUG_COLOR = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);

    static final int DEFAULT_BLENDMODE = BlendMode.ID;

    Dc    dc;
    int   numDirections;
    int   numFrames;
    int   blendMode;
    Color tint;
    Index transform;
    int   transformColor;
    boolean shadow;

    static Layer obtain(Dc dc, int blendMode) {
      return pool.obtain().set(dc, blendMode);
    }

    static void freeAll(Layer[] layers) {
      for (int i = 0; i < layers.length; i++) {
        free(layers, i);
      }
    }

    static void free(Layer[] layers, int i) {
      Layer layer = layers[i];
      if (layer != null) {
        pool.free(layer);
        layers[i] = null;
      }
    }

    Layer set(Dc dc, int blendMode) {
      this.dc        = dc;
      this.blendMode = blendMode;
      tint           = Color.WHITE;
      numDirections  = dc.numDirections();
      numFrames      = dc.numFrames();
      transform      = null;
      transformColor = 0;
      shadow         = (blendMode != BlendMode.LUMINOSITY && blendMode != BlendMode.LUMINOSITY_TINT);
      return this;
    }

    @Override
    public void reset() {} // Does nothing -- call Layer#set(Dc,int) when obtained

    public Dc getDc() {
      return dc;
    }

    public Layer setBlendMode(int blendMode) {
      return setBlendMode(blendMode, Color.WHITE);
    }

    public Layer setBlendMode(int blendMode, Color tint) {
      this.blendMode = blendMode;
      this.tint      = tint;
      return this;
    }

    public Layer setAlpha(float a) {
      if (tint == Color.WHITE) tint = tint.cpy();
      tint.a = a;
      return this;
    }

    public Layer setTransform(Index colormap, int id) {
      transform      = colormap;
      transformColor = colormap == null ? 0 : id;
      return this;
    }

    public Layer setTransform(byte packedTransform) {
      int transform = packedTransform & 0xFF;
      if (transform == 0xFF) {
        return setTransform(null, 0);
      } else {
        return setTransform(Riiablo.colormaps.get(transform >>> 5), transform & 0x1F);
      }
    }

    protected void draw(Batch batch, int d, int f, float x, float y) {
      if (!dc.loaded(d)) return;
      BBox box = dc.box(d, f);
      x += box.xMin;
      y -= box.yMax;
      if (f >= numFrames) return; // FIXME: see #113
      TextureRegion region = dc.direction(d).frame(f).texture();
      if (region.getTexture().getTextureObjectHandle() == 0) return;
      PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
      b.setBlendMode(blendMode, tint, true);
      b.setColormap(transform, transformColor);
      b.draw(region, x, y);
    }

    protected void drawDebug(ShapeRenderer shapeRenderer, int d, int f, float x, float y) {
      if (!dc.loaded(d)) return;
      boolean reset = !shapeRenderer.isDrawing();
      if (reset) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
      } else {
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
      }

      shapeRenderer.setColor(Color.RED);
      shapeRenderer.line(x, y, x + 40, y);
      shapeRenderer.setColor(Color.GREEN);
      shapeRenderer.line(x, y, x, y + 20);
      shapeRenderer.setColor(Color.BLUE);
      shapeRenderer.line(x, y, x + 20, y - 10);

      BBox box = dc.box(d, f);
      shapeRenderer.setColor(DEBUG_COLOR);
      shapeRenderer.rect(x + box.xMin, y - box.yMax, box.width, box.height);
      if (reset) shapeRenderer.end();
    }
  }

  public Builder edit() {
    return Builder.obtain(this);
  }

  public static Builder builder() {
    return Builder.obtain(null);
  }

  public static class Builder implements Pool.Poolable {
    private static final Pool<Builder> pool = Pools.get(Builder.class, 32);

    Animation animation;
    final Layer layers[] = new Layer[NUM_LAYERS];
    int size = 0;

    public static Builder obtain(Animation animation) {
      Builder builder = pool.obtain();
      builder.animation = animation;
      return builder;
    }

    @Override
    public void reset() {
      Arrays.fill(layers, 0, size, null);
      size = 0;
    }

    public Builder layer(Dc dc) {
      return layer(Layer.obtain(dc, Layer.DEFAULT_BLENDMODE));
    }

    public Builder layer(Dc dc, int blendMode) {
      return layer(Layer.obtain(dc, blendMode));
    }

    public Builder layer(Dc dc, int blendMode, byte packedTransform) {
      Layer layer = Layer.obtain(dc, blendMode);
      layer.setTransform(packedTransform);
      return layer(layer);
    }

    public Builder layer(Layer layer) {
      layers[size++] = layer;
      return this;
    }

    public Animation build() {
      Layer first = layers[0];
      if (animation == null) {
        animation = Animation.newAnimation();
      } else {
        animation.reset();
      }
      animation.numDirections = first.numDirections;
      animation.numFrames     = first.numFrames;
      animation.startIndex    = 0;
      animation.endIndex      = animation.numFrames;
      animation.frame         = animation.startIndex;
      animation.elapsedTime   = 0;
      System.arraycopy(layers, 0, animation.layers, 0, size);
      animation.updateBox();
      pool.free(this);
      return animation;
    }
  }
}
