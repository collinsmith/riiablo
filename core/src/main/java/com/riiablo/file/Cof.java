package com.riiablo.file;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.ArrayUtils;

import com.riiablo.codec.util.BBox;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;
import com.riiablo.util.DebugUtils;

public final class Cof {
  private static final Logger log = LogManager.getLogger(Cof.class);

  public static final class Component {
    public static final byte HD = 0x0; // head
    public static final byte TR = 0x1; // torso
    public static final byte LG = 0x2; // legs
    public static final byte RA = 0x3; // right arm
    public static final byte LA = 0x4; // left arm
    public static final byte RH = 0x5; // right hand
    public static final byte LH = 0x6; // left hand
    public static final byte SH = 0x7; // shield
    public static final byte S1 = 0x8; // special 1
    public static final byte S2 = 0x9; // special 2
    public static final byte S3 = 0xA; // special 3
    public static final byte S4 = 0xB; // special 4
    public static final byte S5 = 0xC; // special 5
    public static final byte S6 = 0xD; // special 6
    public static final byte S7 = 0xE; // special 7
    public static final byte S8 = 0xF; // special 8
    public static final int NUM_COMPONENTS = 16;

    private static final String[] NAME = {
        "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"
    };

    public static String toString(byte value) {
      return NAME[value];
    }

    public static String[] values() {
      return NAME;
    }
  }

  public static Cof read(ByteBuf buffer) {
    return read(ByteInput.wrap(buffer));
  }

  public static Cof read(ByteInput in) {
    final int size = in.bytesRemaining();
    short numLayers = in.read8u();
    short numFrames = in.read8u(); // frames before dirs
    short numDirections = in.read8u();
    short version = in.read8u();
    byte[] unk = in.readBytes(4);
    int xMin = in.read32();
    int xMax = in.read32();
    int yMin = in.read32();
    int yMax = in.read32();
    int animRate = in.readSafe32u();

    BBox box = new BBox(xMin, yMin, xMax, yMax);

    log.trace("version: {}", version);
    log.trace("numLayers: {}", numLayers);
    log.trace("numDirections: {}", numDirections);
    log.trace("numFrames: {}", numFrames);
    log.trace("unk: {}", DebugUtils.toByteArray(unk));
    log.trace("box: {}", box);
    log.trace("animRate: {}", animRate);

    Layer[] layers = new Layer[numLayers];
    for (int l = 0; l < numLayers; l++) {
      try {
        MDC.put("layer", l);
        layers[l] = new Layer(in);
      } finally {
        MDC.remove("layer");
      }
    }

    final int keyframesSize;
    if (size == 42 && numLayers == 1 && numDirections == 1 && numFrames == 1) {
      // not sure if this is a special case or not, min kf #?
      keyframesSize = 4;
    } else {
      keyframesSize = numFrames;
    }

    log.trace("keyframesSize: {}", keyframesSize);
    byte[] keyframes = in.readBytes(keyframesSize);
    log.trace("keyframes: {}", keyframes);

    byte[] layerOrder = in.readBytes(numDirections * numFrames * numLayers);
    if (log.traceEnabled()) {
      StringBuilder builder = new StringBuilder(16384).append('\n');
      for (int d = 0, i = 0; d < numDirections; d++) {
        builder.append(String.format("%2d", d)).append(':').append(' ');
        for (int f = 0; f < numFrames; f++) {
          builder.append('[');
          for (int l = 0; l < numLayers; l++) {
            byte b = layerOrder[i++];
            builder.append(Component.toString(b)).append(' ');
          }

          builder.setLength(builder.length() - 1);
          builder.append(']').append(',').append(' ');
        }

        builder.setLength(builder.length() - 2);
        builder.append('\n');
      }

      builder.setLength(builder.length() - 1);
      log.trace("layerOrder: {}", builder.toString());
    }

    assert in.bytesRemaining() == 0;

    return new Cof(version, numDirections, numFrames, numLayers, unk, box, animRate, layers, keyframes, layerOrder);
  }

  final short numDirections; // ubyte
  final short numFrames; // ubyte
  final short numLayers; // ubyte
  final short version; // ubyte
  final byte[] unk;
  final BBox box;
  final int animRate; // uint
  final Layer[] layers;
  final byte[] keyframes;
  final byte[] layerOrder;
  final byte[] components; // derived

  Cof(
      short version,
      short numDirections,
      short numFrames,
      short numLayers,
      byte[] unk,
      BBox box,
      int animRate,
      Layer[] layers,
      byte[] keyframes,
      byte[] layerOrder
  ) {
    this.version = version;
    this.numLayers = numLayers;
    this.numDirections = numDirections;
    this.numFrames = numFrames;
    this.unk = unk;
    this.box = box;
    this.animRate = animRate;
    this.layers = layers;
    this.keyframes = keyframes;
    this.layerOrder = layerOrder;

    components = new byte[16];
    for (byte i = 0; i < layers.length; i++) {
      components[layers[i].component] = i;
    }
  }

  public int numDirections() {
    return numDirections;
  }

  public int numFrames() {
    return numFrames;
  }

  public int numLayers() {
    return numLayers;
  }

  public int animRate() {
    return animRate;
  }

  public Layer layer(int l) {
    return layers[l];
  }

  public Layer layerAt(int component) {
    return layers[components[component]];
  }

  public int findKeyframe(Keyframe keyframe) {
    return ArrayUtils.indexOf(keyframes, keyframe.asInt());
  }

  public byte componentAt(int d, int f, int l) {
    final int dfl = d * numFrames * numLayers;
    final int df  = f * numLayers;
    return layerOrder[dfl + df + l];
  }

  public static final class Layer {
    public byte component; // ubyte
    public byte shadow;  // ubyte
    public byte selectable; // ubyte
    public byte overrideTransLvl; // ubyte
    public byte newTransLvl; // ubyte
    public String weaponClass;

    Layer(ByteInput in) {
      component = in.readSafe8u();
      shadow = in.readSafe8u();
      selectable = in.readSafe8u();
      overrideTransLvl = in.readSafe8u();
      newTransLvl = in.readSafe8u();
      weaponClass = in.readString(4);

      log.trace("component: {}", component);
      log.trace("shadow: {}", shadow);
      log.trace("selectable: {}", selectable);
      log.trace("overrideTransLvl: {}", overrideTransLvl);
      log.trace("newTransLvl: {}", newTransLvl);
      log.trace("weaponClass: {}", weaponClass);
    }
  }

  public enum Keyframe {
    NONE((byte) 0),
    ATTACK((byte) 1),
    MISSILE((byte) 2),
    SOUND((byte) 3),
    SKILL((byte) 4),
    ;

    public static Keyframe fromInt(byte i) {
      switch (i) {
        case 0: return NONE;
        case 1: return ATTACK;
        case 2: return MISSILE;
        case 3: return SOUND;
        case 4: return SKILL;
        default: throw new IllegalArgumentException(i + " does not map to any known keyframe constant!");
      }
    }

    final byte value;

    Keyframe(byte value) {
      this.value = value;
    }

    public byte asInt() {
      return value;
    }
  }
}
