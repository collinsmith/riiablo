package gdx.diablo.codec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import gdx.diablo.codec.util.BBox;
import gdx.diablo.util.BufferUtils;

public class COF {
  private static final String TAG = "COF";
  private static final boolean DEBUG        = !true;
  private static final boolean DEBUG_LAYERS = DEBUG && true;

  public static final class Component {
    public static final int HD = 0;  // head
    public static final int TR = 1;  // torso
    public static final int LG = 2;  // legs
    public static final int RA = 3;  // right arm
    public static final int LA = 4;  // left arm
    public static final int RH = 5;  // right hand
    public static final int LH = 6;  // left hand
    public static final int SH = 7;  // shield
    public static final int S1 = 8;  // special 1
    public static final int S2 = 9;  // special 2
    public static final int S3 = 10; // special 3
    public static final int S4 = 11; // special 4
    public static final int S5 = 12; // special 5
    public static final int S6 = 13; // special 6
    public static final int S7 = 14; // special 7
    public static final int S8 = 15; // special 8
    public static final int NUM_COMPONENTS = 16;
  }

  Header   header;
  Layer    layers[];
  Keyframe keyframe[];
  byte     layerOrder[];
  BBox     box;

  Layer    components[];

  private COF(Header header, Layer[] layers, Keyframe[] keyframe, byte[] layerOrder) {
    this.header     = header;
    this.layers     = layers;
    this.keyframe   = keyframe;
    this.layerOrder = layerOrder;

    box = new BBox();
    box.xMin = header.xMin;
    box.yMin = header.yMin;
    box.xMax = header.xMax;
    box.yMax = header.yMax;
    box.width  = box.xMax - box.xMin + 1;
    box.height = box.yMax - box.yMin + 1;

    components = new Layer[16];
    for (Layer layer : layers) {
      components[layer.component] = layer;
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append(header)
        .append(layers)
        .append("keyframe", keyframe)
        .append("layerOrder", layerOrder)
        .build();
  }

  public static String[] parse(String cof) {
    return new String[] {
        cof.substring(0, 2),
        cof.substring(2, 4),
        cof.substring(4)
    };
  }

  public int getNumLayers() {
    return header.layers;
  }

  public int getNumDirections() {
    return header.directions;
  }

  public int getNumFramesPerDir() {
    return header.framesPerDir;
  }

  public int getMinX() {
    return header.xMin;
  }

  public int getMaxX() {
    return header.xMax;
  }

  public int getMinY() {
    return header.yMin;
  }

  public int getMaxY() {
    return header.yMax;
  }

  public int getAnimRate() {
    return header.animRate;
  }

  public Layer getLayer(int layer) {
    return layers[layer];
  }

  public Layer getComponent(int component) {
    return components[component];
  }

  public byte getLayerOrder(int d, int f, int l) {
    int dfl = d * header.framesPerDir * header.layers;
    int df  = f * header.layers;
    return layerOrder[dfl + df + l];
  }

  public byte[] getLayerOrder() {
    return layerOrder;
  }

  public static COF loadFromFile(FileHandle handle) {
    return loadFromStream(handle.read());
  }

  public static COF loadFromStream(InputStream in) {
    return loadFromStream(in, -1);
  }

  public static COF loadFromStream(InputStream in, int size) {
    try {
      Header header = new Header(in);
      if (DEBUG) Gdx.app.debug(TAG, header.toString());

      Layer[] layers = new Layer[header.layers];
      for (int l = 0; l < header.layers; l++) {
        layers[l] = new Layer(in);
        if (DEBUG_LAYERS) Gdx.app.debug(TAG, layers[l].toString());
      }

      int keyframesSize;
      if (size == 42 && header.layers == 1 && header.directions == 1 && header.framesPerDir == 1) {
        keyframesSize = 4;
      } else {
        keyframesSize = header.framesPerDir;
      }

      Keyframe[] keyframes = new Keyframe[header.framesPerDir];
      ByteBuffer keyframeBuffer = ByteBuffer.wrap(IOUtils.readFully(in, keyframesSize)).order(ByteOrder.LITTLE_ENDIAN);
      for (int f = 0; f < header.framesPerDir; f++) {
        keyframes[f] = Keyframe.fromInteger(keyframeBuffer.get());
      }
      //assert size != 42 || !keyframeBuffer.hasRemaining() : "size = " + size + "; keyframeBuffer.hasRemaining() = " + keyframeBuffer.hasRemaining();
      if (DEBUG) Gdx.app.debug(TAG, "keyframes = " + Arrays.toString(keyframes));

      final int numLayers = header.directions * header.framesPerDir * header.layers;
      byte[] layerOrder = IOUtils.readFully(in, numLayers);
      if (DEBUG_LAYERS) {
        String[] LAYER = {
            "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"
        };
        StringBuilder builder = new StringBuilder("layerOrder = ").append('\n');
        for (int d = 0, i = 0; d < header.directions; d++) {
          builder.append(String.format("%2d", d)).append(':').append(' ');
          for (int f = 0; f < header.framesPerDir; f++) {
            builder.append('[');
            for (int l = 0; l < header.layers; l++) {
              byte b = layerOrder[i++];
              builder.append(LAYER[b]).append(' ');
            }

            builder.setLength(builder.length() - 1);
            builder.append(']').append(',').append(' ');
          }

          builder.setLength(builder.length() - 2);
          builder.append('\n');
        }

        builder.setLength(builder.length() - 1);
        Gdx.app.debug(TAG, builder.toString());
      }

      assert in.available() == 0;
      return new COF(header, layers, keyframes, layerOrder);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load COF.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  static class Header {
    static final int SIZE = 28;

    short layers;       // unsigned byte
    short framesPerDir; // unsigned byte
    short directions;   // unsigned byte
    short version;      // unsigned byte
    byte  unknown1[];

    int xMin;
    int xMax;
    int yMin;
    int yMax;

    short animRate;
    short zeros;

    Header(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      layers       = (short) BufferUtils.readUnsignedByte(buffer);
      framesPerDir = (short) BufferUtils.readUnsignedByte(buffer);
      directions   = (short) BufferUtils.readUnsignedByte(buffer);
      version      = (short) BufferUtils.readUnsignedByte(buffer);
      unknown1     = BufferUtils.readBytes(buffer, 4);
      xMin         = buffer.getInt();
      xMax         = buffer.getInt();
      yMin         = buffer.getInt();
      yMax         = buffer.getInt();
      animRate     = buffer.getShort();
      zeros        = buffer.getShort();
      assert !buffer.hasRemaining();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("layers", layers)
          .append("framesPerDir", framesPerDir)
          .append("directions", directions)
          .append("version", version)
          .append("unknown1", Arrays.toString(unknown1))
          .append("xMin", xMin)
          .append("xMax", xMax)
          .append("yMin", yMin)
          .append("yMax", yMax)
          .append("animRate", animRate)
          .append("zeros", zeros)
          .build();
    }
  }
  public static class Layer {
    static final int SIZE = 9;

    public static final int HD = 0;
    public static final int TR = 1;
    public static final int LG = 2;
    public static final int RA = 3;
    public static final int LA = 4;
    public static final int RH = 5;
    public static final int LH = 6;
    public static final int SH = 7;
    public static final int S1 = 8;
    public static final int S2 = 9;
    public static final int S3 = 10;
    public static final int S4 = 11;
    public static final int S5 = 12;
    public static final int S6 = 13;
    public static final int S7 = 14;
    public static final int S8 = 15;

    public byte   component;        // unsigned
    public byte   shadow;           // unsigned
    public byte   selectable;       // unsigned
    public byte   overrideTransLvl; // unsigned
    public byte   newTransLvl;      // unsigned
    public String weaponClass;

    Layer(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      component        = buffer.get();
      shadow           = buffer.get();
      selectable       = buffer.get();
      overrideTransLvl = buffer.get();
      newTransLvl      = buffer.get();
      weaponClass      = BufferUtils.readString2(buffer, 4);
      assert !buffer.hasRemaining();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("component", component)
          .append("shadow", shadow)
          .append("selectable", selectable)
          .append("overrideTransLvl", overrideTransLvl)
          .append("newTransLvl", newTransLvl)
          .append("weaponClass", weaponClass)
          .build();
    }
  }
  enum Keyframe {
    NONE,
    ATTACK,
    MISSILE,
    SOUND,
    SKILL;

    public static Keyframe fromInteger(int i) {
      switch (i) {
        case 0: return NONE;
        case 1: return ATTACK;
        case 2: return MISSILE;
        case 3: return SOUND;
        case 4: return SKILL;
        default:
          throw new IllegalArgumentException(i + " does not map to any keyframe constant!");
      }
    }
  }
}
