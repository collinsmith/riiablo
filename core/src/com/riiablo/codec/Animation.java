package com.riiablo.codec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.IntMap;
import com.riiablo.Riiablo;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;

import org.apache.commons.lang3.Validate;

public class Animation extends BaseDrawable {
  private static final String TAG = "Animation";
  private static final int DEBUG_MODE = 1; // 0=off, 1=box, 2=layer box

  private static final int   NUM_LAYERS = COF.Component.NUM_COMPONENTS;
  public  static final float FRAMES_PER_SECOND = 25f;
  public  static final float FRAME_DURATION = 1 / FRAMES_PER_SECOND;

  private static final Color   SHADOW_TINT      = Riiablo.colors.modal75;
  private static final Affine2 SHADOW_TRANSFORM = new Affine2();

  private final IntMap<Array<AnimationListener>> EMPTY_MAP = new IntMap<>(0);

  private int     numDirections;
  private int     numFrames;
  private int     direction;
  private int     frame;
  private boolean looping;
  private boolean clamp;
  private float   frameDuration;
  private float   elapsedTime;
  private Layer   layers[];
  private COF     cof;
  private BBox    box;
  private boolean highlighted;

  private IntMap<Array<AnimationListener>> animationListeners;

  Animation() {
    this(0, 0, new Layer[NUM_LAYERS]);
  }

  Animation(int directions, int framesPerDir) {
    this(directions, framesPerDir, new Layer[NUM_LAYERS]);
  }

  Animation(int directions, int framesPerDir, Layer[] layers) {
    numDirections = directions;
    numFrames     = framesPerDir;
    this.layers   = layers;
    looping       = true;
    clamp         = true;
    frameDuration = FRAME_DURATION;
    box           = new BBox();

    animationListeners = EMPTY_MAP;
  }

  public static Animation newAnimation() {
    return new Animation();
  }

  public static Animation newAnimation(DC dc) {
    return Animation.builder().layer(dc).build();
  }

  public static Animation newAnimation(COF cof) {
    Animation animation = new Animation();
    animation.reset(cof);
    return animation;
  }

  public COF getCOF() {
    return cof;
  }

  public boolean reset(COF cof) {
    if (this.cof != cof) {
      this.cof = cof;
      numDirections = cof.getNumDirections();
      numFrames = cof.getNumFramesPerDir();
      setFrameDelta(cof.getAnimRate());

      if (direction >= numDirections) direction = 0;
      //if (frame >= numFrames) {
        frame = 0;
        elapsedTime = 0;
      //}

      return true;
    }

    return false;
  }

  public static Builder builder() {
    return new Builder();
  }

  //protected void loadAll() {
  //  for (int d = dirs.nextSetBit(0); d >= 0; d = dirs.nextSetBit(d + 1)) {
  //    load(d);
  //  }
  //}

  protected void load(int d) {
    for (Layer l : layers) if (l != null) l.load(d);
  }

  public Animation setLayer(int component, DC dc) {
    return setLayer(component, dc, true);
  }

  public Animation setLayer(int component, DC dc, boolean updateBox) {
    layers[component] = dc != null ? new Layer(dc).load(direction) : null;
    if (updateBox) updateBox();
    return this;
  }

  public Layer setLayer(COF.Layer cofLayer, DC dc, boolean updateBox) {
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
        Gdx.app.error(TAG, "Unknown transform: " + transform);
    }
  }

  public Layer getLayer(int component) {
    return layers[component];
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
      load(d);
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
      //if (frame == numFrames - 1) notifyAnimationFinished();
    }
  }

  public boolean isFinished() {
    return frame == numFrames - 1;
  }

  public boolean isLooping() {
    return looping;
  }

  public void setLooping(boolean b) {
    looping = b;
  }

  public boolean isClamped() {
    return clamp;
  }

  public void setClamp(boolean b) {
    clamp = b;
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
          for (int l = 0; l < cof.getNumLayers(); l++) {
            COF.Layer cofLayer = cof.getLayer(l);
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
          for (int l = 0; l < cof.getNumLayers(); l++) {
            COF.Layer cofLayer = cof.getLayer(l);
            Layer layer = layers[cofLayer.component];
            if (layer != null && layer.blendMode == BlendMode.BRIGHTEN) { // FIXME: may be unnecessary in production
              layer.setBlendMode(BlendMode.ID);
            }
          }
        }
      }
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

  public int getKeyFrameIndex(float stateTime) {
    if (numFrames <= 1) return 0;
    int frameNumber = (int) (stateTime / frameDuration);
    return looping
        ? frameNumber % numFrames
        : Math.min(clamp ? numFrames - 1 : numFrames, frameNumber);
  }

  public void act() {
    act(Gdx.graphics.getDeltaTime());
  }

  public void act(float delta) {
    elapsedTime += delta;
    frame = getKeyFrameIndex(elapsedTime);
    notifyListeners(frame);
    if (frame == numFrames - 1) notifyAnimationFinished();
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

      //shapes.setColor(Color.RED);
      //shapes.line(x, y, x + 50, y);
      //shapes.setColor(Color.GREEN);
      //shapes.line(x, y, x, y + 50);
      //shapes.setColor(Color.BLUE);
      //shapes.line(x, y, x + 15, y - 20);
      shapes.setColor(Color.GREEN);
      shapes.rect(x + box.xMin, y - box.yMax, box.width, box.height);
      if (reset) shapes.end();
    } else if (DEBUG_MODE == 2 && frame < numFrames) {
      int d = DC.Direction.toReadDir(direction, cof.getNumDirections());
      int f = frame;
      for (int l = 0; l < cof.getNumLayers(); l++) {
        int component = cof.getLayerOrder(d, f, l);
        Layer layer = layers[component];
        if (layer != null) layer.drawDebug(shapes, d, f, x, y);
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
    if (cof == null && frame < numFrames) {
      for (Layer layer : layers) {
        if (layer == null) continue;
        drawLayer(batch, layer, x, y);
      }
      batch.resetBlendMode();
      batch.resetColormap();
    } else if (frame < numFrames) {
      int d = DC.Direction.toReadDir(direction, cof.getNumDirections());
      int f = frame;
      // TODO: Layer blend modes should correspond with the cof trans levels
      for (int l = 0; l < cof.getNumLayers(); l++) {
        int component = cof.getLayerOrder(d, f, l);
        Layer layer = layers[component];
        if (layer != null) {
          drawLayer(batch, layer, x, y);
        }
      }
      batch.resetBlendMode();
      batch.resetColormap();
    }
  }

  public void drawShadow(PaletteIndexedBatch batch, float x, float y) {
    drawShadow(batch, x, y, true);
  }

  public void drawShadow(PaletteIndexedBatch batch, float x, float y, boolean handleBlends) {
    if (handleBlends) batch.setBlendMode(BlendMode.SOLID, SHADOW_TINT);
    if (cof == null) {
      for (Layer layer : layers) {
        if (layer == null) continue;
        drawShadow(batch, layer, x, y);
      }
    } else if (frame < numFrames) {
      int d = DC.Direction.toReadDir(direction, cof.getNumDirections());
      int f = frame;
      for (int l = 0; l < cof.getNumLayers(); l++) {
        int component = cof.getLayerOrder(d, f, l);
        Layer layer = layers[component];
        if (layer != null) {
          COF.Layer cofLayer = cof.getComponent(component);
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

    DC dc    = layer.dc;
    BBox box = dc.getBox(d, f);

    SHADOW_TRANSFORM.idt();
    SHADOW_TRANSFORM.preTranslate(box.xMin, -(box.yMax / 2));
    SHADOW_TRANSFORM.preShear(-1.0f, 0);
    SHADOW_TRANSFORM.preTranslate(x, y);
    SHADOW_TRANSFORM.scale(1, 0.5f);

    if (layer.regions[d] == null) layer.load(d);
    TextureRegion region = layer.regions[d][f];
    batch.draw(region, region.getRegionWidth(), region.getRegionHeight(), SHADOW_TRANSFORM);
  }

  public void drawLayer(PaletteIndexedBatch batch, Layer layer, float x, float y) {
    layer.draw(batch, direction, frame, x, y);
  }

  public void updateBox() {
    if (cof == null) {
      box.reset();
      for (int l = 0; l < NUM_LAYERS; l++) {
        Layer layer = layers[l];
        if (layer == null) break;
        box.max(layer.dc.getBox(direction));
      }
    } else if (frame < numFrames) {
      int d = DC.Direction.toReadDir(direction, cof.getNumDirections());
      int f = frame;
      box.reset();
      for (int l = 0; l < cof.getNumLayers(); l++) {
        int component = cof.getLayerOrder(d, f, l);
        Layer layer = layers[component];
        if (layer != null) {
          box.max(layer.dc.getBox());
        }
      }
    }
  }

  public BBox getBox() {
    //return cof == null
    //    ? layers[0].dc.getDirection(direction).box
    //    : cof.box;
    return cof == null ? layers[0].dc.getBox(direction) : box;
  }

  @Override
  public float getMinWidth() {
    return getBox().width;
  }

  @Override
  public float getMinHeight() {
    return getBox().height;
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

  public static class Layer {
    private final Color DEBUG_COLOR = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);

    final DC            dc;
    final TextureRegion regions[][];

    final int numDirections;
    final int numFrames;

    int   blendMode;
    Color tint;
    Index transform;
    int   transformColor;

    Layer(DC dc) {
      this(dc, BlendMode.ID);
    }

    Layer(DC dc, int blendMode) {
      this.dc        = dc;
      this.blendMode = blendMode;
      tint           = Color.WHITE;
      numDirections  = dc.getNumDirections();
      numFrames      = dc.getNumFramesPerDir();
      regions        = dc.getRegions();
      transform      = null;
      transformColor = 0;
    }

    protected Layer loadAll(Bits dirs) {
      for (int d = dirs.nextSetBit(0); d >= 0; d = dirs.nextSetBit(d + 1)) {
        load(d);
      }

      return this;
    }

    protected Layer load(int d) {
      if (regions[d] != null) return this;
      dc.loadDirection(d);
      return this;
    }

    public DC getDC() {
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
      BBox box = dc.getBox(d, f);
      x += box.xMin;
      y -= box.yMax;
      if (regions[d] == null) load(d);
      PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
      b.setBlendMode(blendMode, tint, true);
      b.setColormap(transform, transformColor);
      b.draw(regions[d][f], x, y);
    }

    protected void drawDebug(ShapeRenderer shapeRenderer, int d, int f, float x, float y) {
      boolean reset = !shapeRenderer.isDrawing();
      if (reset) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
      } else {
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
      }

      //shapeRenderer.setColor(Color.RED);
      //shapeRenderer.line(x, y, x + 40, y);
      //shapeRenderer.setColor(Color.GREEN);
      //shapeRenderer.line(x, y, x, y + 20);
      //shapeRenderer.setColor(Color.BLUE);
      //shapeRenderer.line(x, y, x + 20, y - 10);

      BBox box = dc.getDirection(d).box;
      shapeRenderer.setColor(DEBUG_COLOR);
      shapeRenderer.rect(x + box.xMin, y - box.yMax, box.width, box.height);

      if (reset) {
        shapeRenderer.end();
      }
    }
  }

  public static class Builder {
    int size = 0;
    Layer layers[] = new Layer[NUM_LAYERS];

    public Builder layer(DC dc) {
      return layer(new Layer(dc));
    }

    public Builder layer(DC dc, int blendMode) {
      return layer(new Layer(dc, blendMode));
    }

    public Builder layer(DC dc, int blendMode, byte packedTransform) {
      Layer layer = new Layer(dc, blendMode);
      layer.setTransform(packedTransform);
      return layer(layer);
    }

    public Builder layer(Layer layer) {
      layers[size++] = layer;
      return this;
    }

    public Animation build() {
      Layer first = layers[0];
      return new Animation(first.numDirections, first.numFrames, layers);
    }
  }
}
