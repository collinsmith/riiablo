package com.riiablo.file;

import io.netty.buffer.ByteBufUtil;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.SwappedDataInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public final class Dcc extends Dc {
  private static final Logger log = LogManager.getLogger(Dcc.class);

// final FileHandle handle; // Dc#handle
  final byte[] signature;
  final byte version;
//final int numDirections; // Dc#numDirections
//final int numFrames; // Dc#numFrames

  public static Dcc read(FileHandle handle, InputStream stream) {
    SwappedDataInputStream in = new SwappedDataInputStream(stream);
    try {
      return read(handle, in);
    } catch (Throwable t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public static Dcc read(FileHandle handle, SwappedDataInputStream in) throws IOException {
    byte[] signature = IOUtils.readFully(in, 1);
    if (log.traceEnabled()) log.trace("signature: {}", ByteBufUtil.hexDump(signature));
    byte version = in.readByte();
    log.trace("version: {}", version);
    int numDirections = in.readByte();
    log.trace("numDirections: {}", numDirections);
    int numFrames = in.readInt();
    log.trace("numFrames: {}", numFrames);
    return new Dcc(handle, signature, version, numDirections, numFrames);
  }

  Dcc(
      FileHandle handle,
      byte[] signature,
      byte version,
      int numDirections,
      int numFrames
  ) {
    super(handle, numDirections, numFrames);
    this.signature = signature;
    this.version = version;
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
