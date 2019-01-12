package gdx.diablo.codec;

import com.google.common.base.Preconditions;

import android.support.annotation.Nullable;

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

import java.util.concurrent.CopyOnWriteArraySet;

import gdx.diablo.BlendMode;
import gdx.diablo.Diablo;
import gdx.diablo.codec.util.BBox;
import gdx.diablo.graphics.PaletteIndexedBatch;

public abstract class Animation extends BaseDrawable {
  private static final String TAG = "Animation";

  private static final float FRAMES_PER_SECOND = 25f;
  private static final float FRAME_DURATION = 1 / FRAMES_PER_SECOND;

  final Bits dirs;

  private final int numDirections;
  private final int numFrames;

  private int     direction;
  private int     frame;
  private boolean looping;
  private float   frameDuration;
  private float   elapsedTime;

  private final CopyOnWriteArraySet<AnimationListener> ANIMATION_LISTENERS;

  public Animation(int directions, int framesPerDir) {
    this(directions, framesPerDir, 0, new Bits());
  }
  public Animation(int directions, int framesPerDir, int d) {
    this(directions, framesPerDir, d, new Bits());
  }

  public Animation(int directions, int framesPerDir, int d, Bits dirs) {
    numDirections = directions;
    numFrames     = framesPerDir;
    this.dirs     = dirs;
    looping       = true;
    frameDuration = FRAME_DURATION;

    ANIMATION_LISTENERS = new CopyOnWriteArraySet<>();

    dirs.set(d);
  }

  public static Animation newAnimation(Layer layer) {
    return new AnimationImpl(layer);
  }

  public static Animation newAnimation(DC anim) {
    return new AnimationImpl(anim);
  }

  public static Animation newAnimation(DC anim, int d) {
    return new AnimationImpl(anim, d);
  }

  protected void loadAll() {
    for (int d = dirs.nextSetBit(0); d >= 0; d = dirs.nextSetBit(d + 1)) {
      load(d);
    }
  }

  protected abstract void load(int d);

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
      if (!dirs.get(d)) load(d);
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

  public void draw(Batch batch, float x, float y) {
    draw(batch, x, y, getMinWidth(), getMinHeight());
  }

  public abstract void drawDebug(ShapeRenderer shapes, float x, float y);

  public abstract BBox getBox();

  @Override
  public float getMinWidth() {
    return getBox().width;
  }

  @Override
  public float getMinHeight() {
    return getBox().height;
  }

  public CompositeAnimation composite() {
    throw new UnsupportedOperationException();
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

  public boolean removeAnimationListener(@Nullable Object o) {
    return o != null && ANIMATION_LISTENERS.remove(o);
  }

  public boolean containsAnimationListener(@Nullable Object o) {
    return o != null && ANIMATION_LISTENERS.contains(o);
  }

  public interface AnimationListener {
    void onFinished(Animation animation);
  }

  public static class Layer {
    public static Layer from(DC anim) {
      return new Layer(anim);
    }

    public static Layer from(DC anim, int blendMode) {
      return new Layer(anim, blendMode);
    }

    final DC            base;
    final TextureRegion regions[][];

    final int numDirections;
    final int numFrames;

    int blendMode;
    Index transform;
    int transformColor;

    private final Color DEBUG_COLOR = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);

    Layer(DC anim) {
      this(anim, BlendMode.ID);
    }

    Layer(DC anim, int blendMode) {
      this.blendMode = blendMode;
      numDirections  = anim.getNumDirections();
      numFrames      = anim.getNumFramesPerDir();
      base           = anim;
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
      base.loadDirection(d);
      regions[d] = new TextureRegion[numFrames];
      for (int f = 0; f < numFrames; f++) {
        regions[d][f] = base.getTexture(d, f);
      }

      return this;
    }

    public void setBlendMode(int blendMode) {
      this.blendMode = blendMode;
    }

    public void setTransform(Index colormap, int id) {
      //if ( colormap != null) System.out.println("----> " + colormap + "; " + id);
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

    /*
    public void setTransform(byte packedTransform) {
      if ((packedTransform & 0xFF) == 0xFF) {
        transform = 0;
        transformColor = 0;
      } else {
        transform = packedTransform >>> 4;
        transformColor = packedTransform & 0xF;
      }
    }

    public void setTransform(int transform) {
      this.transform = transform;
    }

    public void setTransformColor(int colorId) {
      this.transformColor = colorId;
    }
    */

    protected void draw(Batch batch, int d, int f, float x, float y) {
      BBox box = base.getBox(d, f);
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

      BBox box = base.getDirection(d).box;
      shapeRenderer.setColor(DEBUG_COLOR);
      shapeRenderer.rect(x + box.xMin, y - box.yMax, box.width, box.height);

      if (reset) {
        shapeRenderer.end();
      }
    }
  }

  public static class AnimationImpl extends Animation {
    final Layer layer;

    AnimationImpl(DC base) {
      super(base.getNumDirections(), base.getNumFramesPerDir());
      layer = new Layer(base);
      loadAll();
    }

    AnimationImpl(DC base, int d) {
      super(base.getNumDirections(), base.getNumFramesPerDir(), d);
      layer = new Layer(base);
      loadAll();
    }

    AnimationImpl(Layer layer) {
      super(layer.numDirections, layer.numFrames);
      this.layer = layer;
      loadAll();
    }

    @Override
    public CompositeAnimation composite() {
      return new CompositeAnimation(this);
    }

    @Override
    protected void load(int d) {
      layer.load(d);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
      layer.draw(batch, getDirection(), getFrame(), x, y);
    }

    @Override
    public void drawDebug(ShapeRenderer shapes, float x, float y) {
      layer.drawDebug(shapes, getDirection(), getFrame(), x, y);
    }

    @Override
    public BBox getBox() {
      return layer.base.getDirection(getDirection()).box;
    }
  }

  public static class CompositeAnimation extends Animation {
    private static final Color SHADOW_TINT = new Color(0, 0, 0, 0.5f);
    private static final Affine2 SHADOW_TRANSFORM = new Affine2();

    static final int DEFAULT_LAYERS = 4;

    final Array<Layer> layers;

    public CompositeAnimation(Animation anim) {
      super(anim.numDirections, anim.numFrames, anim.direction, anim.dirs);
      layers = new Array<>(DEFAULT_LAYERS);
      if (anim instanceof AnimationImpl) {
        AnimationImpl impl = (AnimationImpl) anim;
        layers.add(impl.layer);
      } else if (anim instanceof CompositeAnimation) {
        CompositeAnimation impl = (CompositeAnimation) anim;
        layers.addAll(impl.layers);
      } else {
        throw new UnsupportedOperationException();
      }
    }

    public CompositeAnimation(Layer base) {
      super(base.numFrames, base.numDirections);
      layers     = new Array<>(DEFAULT_LAYERS);
      addLayer(base);
    }

    public CompositeAnimation(Layer... layers) {
      super(layers[0].numDirections, layers[0].numFrames);
      this.layers = new Array<>(layers);
    }

    public CompositeAnimation(DC... anims) {
      super(anims[0].getNumDirections(), anims[0].getNumFramesPerDir());
      this.layers = new Array<>(anims.length);
      for (DC anim : anims) addLayer(Layer.from(anim));
    }

    public CompositeAnimation(int directions, int framesPerDir, int numLayers) {
      super(directions, framesPerDir);
      layers     = new Array<>(numLayers);
      for (int i = 0; i < numLayers; i++) {
        layers.add(null);
      }
    }

    @Override
    protected void load(int d) {
      for (Layer l : layers) if (l != null) l.load(d);
    }

    public CompositeAnimation addAll(Layer[] layers) {
      for (Layer layer : layers) addLayer(layer);
      return this;
    }

    public CompositeAnimation addLayer(Layer layer) {
      return addLayer(layer, layer.blendMode);
    }

    public CompositeAnimation addLayer(Layer layer, int blendMode) {
      layer.blendMode = blendMode;
      layers.add(layer.loadAll(dirs));
      return this;
    }

    public CompositeAnimation setLayer(int layer, DC anim) {
      layers.set(layer, new Layer(anim).loadAll(dirs));
      return this;
    }

    public Layer getLayer(int i) {
      return layers.get(i);
    }

    public void drawShadow(Batch batch, Layer layer, float x, float y) {
      int d = getDirection();
      int f = getFrame();

      DC base = layer.base;
      BBox box = base.getBox(d, f);

      SHADOW_TRANSFORM.idt();
      SHADOW_TRANSFORM.preTranslate(box.xMin, -(box.yMax / 2));
      SHADOW_TRANSFORM.preShear(-1.0f, 0);
      SHADOW_TRANSFORM.preTranslate(x, y);
      SHADOW_TRANSFORM.scale(1, 0.5f);

      if (layer.regions[d] == null) layer.load(d);
      TextureRegion region = layer.regions[d][f];
      ((PaletteIndexedBatch) batch).setBlendMode(BlendMode.TINT_BLACKS, SHADOW_TINT);
      batch.draw(region, region.getRegionWidth(), region.getRegionHeight(), SHADOW_TRANSFORM);
    }

    public void drawLayer(Batch batch, Layer layer, float x, float y) {
      layer.draw(batch, getDirection(), getFrame(), x, y);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
      for (int i = 0; i < layers.size; i++) {
        Layer layer = layers.get(i);
        if (layer == null) {
          continue;
        }

        drawLayer(batch, layer, x, y);
      }

      ((PaletteIndexedBatch) batch).resetBlendMode();
      ((PaletteIndexedBatch) batch).resetColormap();
    }

    @Override
    public void drawDebug(ShapeRenderer shapes, float x, float y) {
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
      shapes.setColor(layers.first().DEBUG_COLOR);
      shapes.rect(x + box.xMin, y - box.yMax, box.width, box.height);

      if (reset) {
        shapes.end();
      }
    }

    @Override
    public BBox getBox() {
      return layers.first().base.getDirection(super.direction).box;
    }
  }

  public static class COFAnimation extends CompositeAnimation {

    final COF cof;
    boolean drawShadow = true;

    public COFAnimation(COF cof) {
      // TODO: 15 needs to be a variable
      super(cof.getNumDirections(), cof.getNumFramesPerDir(), 15);
      this.cof = cof;
      setFrameDelta(cof.getAnimRate());
    }

    public COF getCOF() {
      return cof;
    }

    public void setShadow(boolean b) {
      if (b != drawShadow) {
        drawShadow = b;
      }
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
      int d = DC.Direction.toReadDir(getDirection(), cof.getNumDirections());
      int f = getFrame();
      for (int l = 0; l < cof.getNumLayers(); l++) {
        int component = cof.getLayerOrder(d, f, l);
        Layer layer = layers.get(component);
        if (layer != null) {
          COF.Layer cofLayer = cof.getComponent(component);
          if (drawShadow && cofLayer.shadow == 0x1) {
            drawShadow(batch, layer, x, y);
          }
        }
      }
      ((PaletteIndexedBatch) batch).resetBlendMode();

      // TODO: Layer blend modes should correspond with the cof trans levels
      for (int l = 0; l < cof.getNumLayers(); l++) {
        int component = cof.getLayerOrder(d, f, l);
        Layer layer = layers.get(component);
        if (layer != null) drawLayer(batch, layer, x, y);
      }
      ((PaletteIndexedBatch) batch).resetBlendMode();
      ((PaletteIndexedBatch) batch).resetColormap();
    }

    @Override
    public void drawDebug(ShapeRenderer shapes, float x, float y) {
      int d = DC.Direction.toReadDir(getDirection(), cof.getNumDirections());
      int f = getFrame();
      for (int l = 0; l < cof.getNumLayers(); l++) {
        int component = cof.getLayerOrder(d, f, l);
        Layer layer = layers.get(component);
        if (layer != null) layer.drawDebug(shapes, d, f, x, y);
      }
    }

    @Override
    public BBox getBox() {
      return cof.box;
    }
  }
}
