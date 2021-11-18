package com.riiablo.file;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.SwappedDataInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.riiablo.codec.util.BBox;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;
import com.riiablo.util.DebugUtils;

public class Dc6 extends Dc<Dc6.Dc6Direction> {
  private static final Logger log = LogManager.getLogger(Dc6.class);

  @SuppressWarnings("GDXJavaStaticResource")
  public static Texture MISSING_TEXTURE;

//final FileHandle handle; // Dc#handle
  final byte[] signature;
  final int version;
  final int format;
  final byte[] section;
//final int numDirections; // Dc#numDirections
//final int numFrames; // Dc#numFrames
  final int[] frameOffsets;

  public static Dc6 read(FileHandle handle, InputStream stream) {
    SwappedDataInputStream in = new SwappedDataInputStream(stream);
    try {
      return read(handle, in);
    } catch (Throwable t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public static Dc6 read(FileHandle handle, SwappedDataInputStream in) throws IOException {
    byte[] signature = IOUtils.readFully(in, 4);
    int version = in.readInt();
    int format = in.readInt();
    byte[] section = IOUtils.readFully(in, 4);
    int numDirections = in.readInt();
    int numFrames = in.readInt();

    log.trace("signature: {}", DebugUtils.toByteArray(signature));
    log.trace("version: {}", version);
    log.trace("format: {}", format);
    log.trace("section: {}", DebugUtils.toByteArray(section));
    log.trace("numDirections: {}", numDirections);
    log.trace("numFrames: {}", numFrames);

    final int totalFrames = numDirections * numFrames;
    final int[] frameOffsets = new int[totalFrames + 1];
    for (int i = 0, s = totalFrames; i < s; i++) frameOffsets[i] = in.readInt();
    frameOffsets[totalFrames] = (int) handle.length();
    log.trace("frameOffsets: {}", frameOffsets);

    return new Dc6(handle, signature, version, format, section, numDirections, numFrames, frameOffsets);
  }

  Dc6(
      FileHandle handle,
      byte[] signature,
      int version,
      int format,
      byte[] section,
      int numDirections,
      int numFrames,
      int[] frameOffsets
  ) {
    super(handle, numDirections, numFrames, Dc6Direction.class);
    this.signature = signature;
    this.version = version;
    this.format = format;
    this.section = section;
    this.frameOffsets = frameOffsets;
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  @Override
  public int dirOffset(int d) {
    return frameOffsets[d * numFrames];
  }

  public int frameOffset(int d, int f) {
    return frameOffsets[d * numFrames + f];
  }

  @Override
  public Dc6 read(ByteBuf buffer, int direction) {
    super.read(buffer, direction);
    ByteInput in = ByteInput.wrap(buffer);
    directions[direction] = new Dc6Direction(this, direction, in);
    return this;
  }

  public void uploadTextures(int d) {
    final Dc6Direction direction = directions[d];
    final Dc6Frame[] frame = direction.frames;
    final Pixmap[] pixmap = direction.pixmap;
    final Texture[] texture = direction.texture;
    for (int f = 0; f < numFrames; f++) {
      Texture t = texture[f] = new Texture(pixmap[f]);
      frame[f].texture.setRegion(t);
      pixmap[f].dispose();
      pixmap[f] = null;
    }
  }

  public static final class Dc6Direction extends Dc.Direction<Dc6Frame> {
    // Dc
    final Dc6Frame[] frames;
    final BBox box;
    final Pixmap[] pixmap;
    final Texture[] texture;

    Dc6Direction(Dc6 dc6, int d, ByteInput in) {
      final int numFrames = dc6.numFrames;
      box = new BBox().prepare();
      pixmap = new Pixmap[numFrames];
      texture = new Texture[numFrames];
      Dc6Frame[] frames = this.frames = new Dc6Frame[numFrames];

      for (int frame = 0; frame < numFrames; frame++) {
        try {
          MDC.put("frame", frame);
          int offset = dc6.frameOffset(d, frame);
          int nextOffset = dc6.frameOffset(d, frame + 1);
          log.tracef("nextOffset - offset: 0x%x", nextOffset - offset);
          Dc6Frame f = frames[frame] = new Dc6Frame(in.readSlice(nextOffset - offset));
          box.max(f.box);
        } finally {
          MDC.remove("frame");
        }
      }
    }

    @Override
    public void dispose() {
      log.trace("disposing dcc pixmaps");
      for (int i = 0, s = pixmap.length; i < s; i++) {
        if (pixmap[i] == null) continue;
        pixmap[i].dispose();
        pixmap[i] = null;
      }

      log.trace("disposing dcc textures");
      for (int i = 0, s = texture.length; i < s; i++) {
        if (texture[i] == null) continue;
        texture[i].dispose();
        texture[i] = null;
      }
    }

    @Override
    public Dc6Frame[] frames() {
      return frames;
    }

    @Override
    public Dc6Frame frame(int f) {
      return frames[f];
    }

    @Override
    public BBox box() {
      return box;
    }
  }

  public static final class Dc6Frame extends Dc.Frame {
    // Dc
    final boolean flipY;
    final int width;
    final int height;
    final int xOffset;
    final int yOffset;

    final BBox box;
    final TextureRegion texture;

    // Dc6
    final int unk0; // unused
    final int nextOffset; // file offset of next frame, unused (header value used in preference)
    final int length; // unused
    final ByteInput in;

    Dc6Frame(ByteInput in) {
      this.in = in;
      flipY = in.read32() != 0;
      width = in.readSafe32u();
      height = in.readSafe32u();
      xOffset = in.read32();
      yOffset = in.read32();
      unk0 = in.readSafe32u();
      nextOffset = in.readSafe32u();
      length = in.readSafe32u();
      box = new BBox().asBox(xOffset, flipY ? yOffset : yOffset - height, width, height);
      texture = MISSING_TEXTURE == null ? new TextureRegion() : new TextureRegion(MISSING_TEXTURE);

      log.trace("flipY: {}", flipY);
      log.trace("width: {}", width);
      log.trace("height: {}", height);
      log.trace("xOffset: {}", xOffset);
      log.trace("yOffset: {}", yOffset);
      log.tracef("unk0: 0x%x", unk0);
      log.tracef("nextOffset: 0x%x", nextOffset);
      log.tracef("length: 0x%x", length);
    }

    @Override
    public boolean flipY() {
      return flipY;
    }

    @Override
    public int width() {
      return width;
    }

    @Override
    public int height() {
      return height;
    }

    @Override
    public int xOffset() {
      return xOffset;
    }

    @Override
    public int yOffset() {
      return yOffset;
    }

    @Override
    public BBox box() {
      return box;
    }

    @Override
    public TextureRegion texture() {
      return texture;
    }
  }
}
