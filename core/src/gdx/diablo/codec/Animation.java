package gdx.diablo.codec;

import com.google.common.base.Preconditions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Bits;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import gdx.diablo.BlendMode;
import gdx.diablo.Diablo;
import gdx.diablo.codec.util.BBox;
import gdx.diablo.graphics.PaletteIndexedBatch;

public class Animation extends BaseDrawable {
  private static final String TAG = "Animation";

  private static final int   NUM_LAYERS = 16;
  private static final float FRAMES_PER_SECOND = 25f;
  private static final float FRAME_DURATION = 1 / FRAMES_PER_SECOND;

  private static final Color   SHADOW_TINT      = new Color(0, 0, 0, 0.5f);
  private static final Affine2 SHADOW_TRANSFORM = new Affine2();

  private int     numDirections;
  private int     numFrames;
  private int     direction;
  private int     frame;
  private boolean looping;
  private float   frameDuration;
  private float   elapsedTime;
  private Layer   layers[];
  private COF     cof;
  private boolean drawShadow;
  //private Bits  cache[];

  private final Set<AnimationListener> ANIMATION_LISTENERS;

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
    drawShadow    = false;
    frameDuration = FRAME_DURATION;

    ANIMATION_LISTENERS = new CopyOnWriteArraySet<>();
  }

  public static Animation newAnimation(DC dc) {
    return Animation.builder().layer(dc).build();
  }

  public static Animation newAnimation(COF cof) {
    Animation animation = new Animation();
    animation.reset(cof);
    return animation;
  }

  public boolean reset(COF cof) {
    if (this.cof != cof) {
      this.cof = cof;
      numDirections = cof.getNumDirections();
      numFrames = cof.getNumFramesPerDir();
      drawShadow = true;
      setFrameDelta(cof.getAnimRate());

      if (direction >= numDirections) direction = 0;
      if (frame >= numFrames) {
        frame = 0;
        elapsedTime = 0;
      }

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
    layers[component] = dc != null ? new Layer(dc).load(direction) : null;
    return this;
  }

  public Layer getLayer(int component) {
    return layers[component];
  }

  public void setShadow(boolean b) {
    if (b != drawShadow) {
      drawShadow = b;
    }
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
      Preconditions.checkArgument(0 <= d && d < numDirections, "Invalid direction: " + d);
      load(d);
      direction = d;
    }
  }

  public int getFrame() {
    return frame;
  }

  public void setFrame(int f) {
    if (f != frame) {
      Preconditions.checkArgument(0 <= f && f < numFrames, "Invalid frame: " + f);
      frame = f;
      elapsedTime = frameDuration * frame;
      if (frame == numFrames - 1) notifyAnimationFinished();
    }
  }

  public boolean isLooping() {
    return looping;
  }

  public void setLooping(boolean b) {
    looping = b;
  }

  public float getFrameDuration() {
    return frameDuration;
  }

  public void setFrameDuration(float f) {
    frameDuration = f;
    elapsedTime = frameDuration * frame;
  }

  public void setFrameDelta(int delta) {
    setFrameDuration(256f / (delta * FRAMES_PER_SECOND));
  }

  public int getKeyFrameIndex(float stateTime) {
    if (numFrames == 1) return 0;
    int frameNumber = (int) (stateTime / frameDuration);
    return looping
        ? frameNumber % numFrames
        : Math.min(numFrames - 1, frameNumber);
  }

  public void act() {
    act(Gdx.graphics.getDeltaTime());
  }

  public void act(float delta) {
    elapsedTime += delta;
    frame = getKeyFrameIndex(elapsedTime);
    if (frame == numFrames - 1) notifyAnimationFinished();
  }

  public void drawDebug(ShapeRenderer shapes, float x, float y) {
    if (cof == null) {
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

      BBox box = getBox();
      shapes.setColor(layers[0].DEBUG_COLOR);
      shapes.rect(x + box.xMin, y - box.yMax, box.width, box.height);

      if (reset) {
        shapes.end();
      }
    } else {
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
    if (cof == null) {
      if (drawShadow) {
        for (Layer layer : layers) {
          if (layer == null) continue;
          drawShadow(batch, layer, x, y);
        }
        batch.resetBlendMode();
      }

      for (Layer layer : layers) {
        if (layer == null) continue;
        drawLayer(batch, layer, x, y);
      }
      batch.resetBlendMode();
      batch.resetColormap();
    } else {
      int d = DC.Direction.toReadDir(direction, cof.getNumDirections());
      int f = frame;
      if (drawShadow) {
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
        batch.resetBlendMode();
      }

      // TODO: Layer blend modes should correspond with the cof trans levels
      for (int l = 0; l < cof.getNumLayers(); l++) {
        int component = cof.getLayerOrder(d, f, l);
        Layer layer = layers[component];
        if (layer != null) drawLayer(batch, layer, x, y);
      }
      batch.resetBlendMode();
      batch.resetColormap();
    }
  }

  public void drawShadow(PaletteIndexedBatch batch, Layer layer, float x, float y) {
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
    batch.setBlendMode(BlendMode.TINT_BLACKS, SHADOW_TINT);
    batch.draw(region, region.getRegionWidth(), region.getRegionHeight(), SHADOW_TRANSFORM);
  }

  public void drawLayer(PaletteIndexedBatch batch, Layer layer, float x, float y) {
    layer.draw(batch, direction, frame, x, y);
  }

  public BBox getBox() {
    return cof == null
        ? layers[0].dc.getDirection(direction).box
        : cof.box;
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
    for (AnimationListener l : ANIMATION_LISTENERS) {
      l.onFinished(this);
    }
  }

  public boolean addAnimationListener(AnimationListener l) {
    Preconditions.checkArgument(l != null, "l cannot be null");
    return ANIMATION_LISTENERS.add(l);
  }

  public boolean removeAnimationListener(Object o) {
    return o != null && ANIMATION_LISTENERS.remove(o);
  }

  public boolean containsAnimationListener(Object o) {
    return o != null && ANIMATION_LISTENERS.contains(o);
  }

  public interface AnimationListener {
    void onFinished(Animation animation);
  }

  public static class Layer {
    private final Color DEBUG_COLOR = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);

    final DC            dc;
    final TextureRegion regions[][];

    final int numDirections;
    final int numFrames;

    int   blendMode;
    Index transform;
    int   transformColor;

    Layer(DC dc) {
      this(dc, BlendMode.ID);
    }

    Layer(DC dc, int blendMode) {
      this.dc        = dc;
      this.blendMode = blendMode;
      numDirections  = dc.getNumDirections();
      numFrames      = dc.getNumFramesPerDir();
      regions        = new TextureRegion[numDirections][];
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
      regions[d] = new TextureRegion[numFrames];
      for (int f = 0; f < numFrames; f++) {
        regions[d][f] = dc.getTexture(d, f);
      }

      return this;
    }

    public void setBlendMode(int blendMode) {
      this.blendMode = blendMode;
    }

    public void setTransform(Index colormap, int id) {
      //if (colormap != null) System.out.println("----> " + colormap + "; " + id);
      transform = colormap;
      transformColor = colormap == null ? 0 : id;
    }

    public void setTransform(byte packedTransform) {
      if ((packedTransform & 0xFF) == 0xFF) {
        setTransform(null, 0);
      } else {
        setTransform(Diablo.colormaps.get(packedTransform >>> 5), packedTransform & 0x1F);
      }
    }

    protected void draw(Batch batch, int d, int f, float x, float y) {
      BBox box = dc.getBox(d, f);
      x += box.xMin;
      y -= box.yMax;
      if (regions[d] == null) load(d);
      PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
      b.setBlendMode(blendMode);
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

      shapeRenderer.setColor(Color.RED);
      shapeRenderer.line(x, y, x + 40, y);
      shapeRenderer.setColor(Color.GREEN);
      shapeRenderer.line(x, y, x, y + 20);
      shapeRenderer.setColor(Color.BLUE);
      shapeRenderer.line(x, y, x + 20, y - 10);

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
