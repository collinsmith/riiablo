package com.riiablo.mpq;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.riiablo.mpq.util.Decompressor;
import com.riiablo.mpq.util.Decryptor;
import com.riiablo.mpq.util.Exploder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import com.riiablo.util.BufferUtils;

public class MPQInputStream extends InputStream {
  private static final String TAG = "MPQInputStream";
  private static final boolean DEBUG = false;

  private static final int[] ZERO_ARRAY = new int[] { 0 };

  final FileChannel          fc;
  final MPQ.BlockTable.Block block;
  final ByteBuffer           sector;
  final ByteBuffer           buffer;
  final ByteBuffer           scratch;

  final int sectorSize;
  final int sectorCount;
  final int sectorOffsets[];

  final int key;
  int curSector = 0;
  int finalSize = 0;
  int read = 0;

  public MPQInputStream(MPQ mpq, String fileName, MPQ.BlockTable.Block block) {
    try {
      RandomAccessFile raf = new RandomAccessFile(mpq.file.file(), "r");
      fc = raf.getChannel();
      fc.position(block.filePos);
    } catch (Exception e) {
      throw new GdxRuntimeException("Unable to read block: " + block, e);
    }

    this.block = block;
    sectorSize = mpq.header.sectorSize;
    sectorCount = (block.FSize + sectorSize - 1) / sectorSize;
    sector = ByteBuffer.allocate(sectorSize).order(ByteOrder.LITTLE_ENDIAN);
    buffer = ByteBuffer.allocate(sectorSize).order(ByteOrder.LITTLE_ENDIAN);
    buffer.limit(0);
    scratch = ByteBuffer.allocate(sectorSize).order(ByteOrder.LITTLE_ENDIAN);

    int key = 0;
    if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
      String baseName = FilenameUtils.getName(fileName);
      key = Decryptor.HASH_ENCRYPTION_KEY.hash(baseName);
      if (block.hasFlag(MPQ.BlockTable.Block.FLAG_FIX_KEY)) {
        key = ((key + (int) block.filePos) ^ block.FSize);
      }
    }

    this.key = key;

    // TODO: Figure out if this is correct. How to check if block contains offsets table.
    if (DEBUG) Gdx.app.debug(TAG, "sectorCount = " + sectorCount);
    //if (!block.hasFlag(MPQ.BlockTable.Block.FLAG_SINGLE_UNIT)) {
    if (block.hasFlag(MPQ.BlockTable.Block.FLAG_COMPRESSED)
     || block.hasFlag(MPQ.BlockTable.Block.FLAG_IMPLODE)) {
      if (DEBUG) Gdx.app.debug(TAG, "Populating sector offsets table");
      sectorOffsets = new int[sectorCount + 1];
      ByteBuffer sectors = ByteBuffer.wrap(new byte[(sectorCount + 1) << 2]).order(ByteOrder.LITTLE_ENDIAN);
      try {
        IOUtils.readFully(fc, sectors);
        sectors.flip();
      } catch (IOException e) {
        throw new GdxRuntimeException("Unable to read sector offsets: " + block, e);
      }

      if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
        if (DEBUG) Gdx.app.debug(TAG, "Decrypting sector offsets table");
        Decryptor.decrypt(key - 1, sectors);
        sectors.flip();
      }

      for (int i = 0; i < sectorCount; i++) {
        sectorOffsets[i] = sectors.getInt();
      }

      sectorOffsets[sectorCount] = block.CSize;
      if (DEBUG) Gdx.app.debug(TAG, "sector offsets = " + Arrays.toString(sectorOffsets));
    } else {
      sectorOffsets = ZERO_ARRAY;
    }
  }

  @Override
  public int available() throws IOException {
    return block.FSize - read; // block.FSize - buffer.position();
  }

  @Override
  public int read() throws IOException {
    if (available() <= 0) {
      return -1;
    }

    if (!buffer.hasRemaining()) {
      readSector();
    }

    assert buffer.hasRemaining();

    read++;
    if (DEBUG) Gdx.app.debug(TAG, "Read 1 bytes [" + available() + " bytes remaining]");
    return buffer.get() & 0xFF;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (available() <= 0) {
      return -1;
    }

    ByteBuffer dst = ByteBuffer.wrap(b, off, len);

    int read = 0;
    while (read < len && available() > 0) {
      if (!buffer.hasRemaining()) {
        readSector();
      }

      int copyLen = Math.min(dst.remaining(), buffer.remaining());
      if (DEBUG) Gdx.app.debug(TAG, "Copying " + copyLen + " bytes");
      read += copyLen;
      this.read += copyLen;

      int bufferPosition = buffer.position();
      dst.put(buffer.array(), buffer.arrayOffset() + bufferPosition, copyLen);
      buffer.position(bufferPosition + copyLen);
    }

    if (DEBUG) Gdx.app.debug(TAG, "Read " + read + " bytes [" + available() + " bytes remaining]");
    return read;
  }

  private void readSector() throws IOException {
    if (available() <= 0) {
      return;
    }

    if (block.hasFlag(MPQ.BlockTable.Block.FLAG_SINGLE_UNIT)) {
      if (DEBUG) Gdx.app.debug(TAG, "Reading as single unit...");

      assert sector.position() == 0;
      assert sector.limit() == block.CSize;
      IOUtils.readFully(fc, sector);
      sector.flip();

      if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
        if (DEBUG) Gdx.app.debug(TAG, "Decrypting...");
        Decryptor.decrypt(key, sector);
        sector.flip();
      }

      if (block.hasFlag(MPQ.BlockTable.Block.FLAG_COMPRESSED)) {
        if (DEBUG) Gdx.app.debug(TAG, "Decompressing...");
        assert buffer.position() == 0;
        Decompressor.decompress(sector, buffer, scratch, block.CSize, block.FSize);
        assert buffer.position() == block.FSize;
        buffer.flip();
      }
    } else if (block.hasFlag(MPQ.BlockTable.Block.FLAG_COMPRESSED)) {
      assert curSector < sectorCount;
      if (DEBUG) Gdx.app.debug(TAG, "Reading sector " + (curSector + 1) + " / " + sectorCount);
      fc.position(block.filePos + sectorOffsets[curSector]);

      final int start = sectorOffsets[curSector];
      final int end   = sectorOffsets[curSector + 1];
      final int CSize = end - start;
      sector.clear();
      sector.limit(CSize);
      IOUtils.readFully(fc, sector);
      sector.rewind();

      if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
        if (DEBUG) Gdx.app.debug(TAG, "Decrypting sector...");
        Decryptor.decrypt(key + curSector, sector);
        sector.flip();
      }

      if (DEBUG) Gdx.app.debug(TAG, "Decompressing sector...");
      final int FSize = Math.min(block.FSize - finalSize, sectorSize);
      buffer.rewind().limit(FSize);
      Decompressor.decompress(sector, buffer, scratch, CSize, FSize);
      if (DEBUG) Gdx.app.debug(TAG, "Decompressed " + buffer.limit() + " bytes");
      buffer.rewind();

      finalSize += sectorSize;
      curSector++;
    } else if (block.hasFlag(MPQ.BlockTable.Block.FLAG_IMPLODE)) {
      assert curSector < sectorCount;
      if (DEBUG) Gdx.app.debug(TAG, "Reading sector " + (curSector + 1) + " / " + sectorCount + " " + block.getFlags());
      fc.position(block.filePos + sectorOffsets[curSector]);

      final int start = sectorOffsets[curSector];
      final int end = sectorOffsets[curSector + 1];
      final int CSize = end - start;
      sector.clear();
      sector.limit(CSize);
      IOUtils.readFully(fc, sector);
      sector.rewind();

      if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
        if (DEBUG) Gdx.app.debug(TAG, "Decrypting sector...");
        Decryptor.decrypt(key + curSector, sector);
        sector.flip();
      }

      if (DEBUG) Gdx.app.debug(TAG, "Exploding sector...");
      final int FSize = Math.min(block.FSize - finalSize, sectorSize);
      buffer.rewind().limit(FSize);
      Exploder.pkexplode(sector, buffer);
      if (DEBUG) Gdx.app.debug(TAG, "Exploded to " + buffer.position() + " bytes");
      buffer.rewind();

      finalSize += sectorSize;
      curSector++;
    } else if (block.hasFlag(MPQ.BlockTable.Block.FLAG_EXISTS)) {
      assert block.CSize == block.FSize : "Shouldn't be compressed";
      assert curSector < sectorCount;
      if (DEBUG) Gdx.app.debug(TAG, "Reading sector " + (curSector + 1) + " / " + sectorCount + " " + block.getFlags());

      final int FSize = Math.min(block.FSize - finalSize, sectorSize);
      buffer.rewind().limit(FSize);
      IOUtils.readFully(fc, buffer);
      buffer.rewind();

      finalSize += sectorSize;
      curSector++;
    } else {
      throw new UnsupportedOperationException("File has unsupported flags " + block.getFlags());
      /*
      if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
        Gdx.app.log(TAG, "Decrypting sector...");
        Decryptor.decrypt(key, sector);
        sector.flip();
      }

      assert buffer.position() == 0;
      buffer.put(sector);
      buffer.flip();
      */
    }
  }

  public static byte[] readBytes(MPQ mpq, String fileName, MPQ.BlockTable.Block block) {
    RandomAccessFile raf = null;
    try {
      raf = new RandomAccessFile(mpq.file.file(), "r");
      FileChannel fc = raf.getChannel();
      fc.position(block.filePos);

      final int sectorSize = mpq.header.sectorSize;
      final int sectorCount = (block.FSize + sectorSize - 1) / sectorSize;
      final ByteBuffer sector = ByteBuffer.allocate(sectorSize).order(ByteOrder.LITTLE_ENDIAN);
      final ByteBuffer buffer = ByteBuffer.allocate(block.FSize).order(ByteOrder.LITTLE_ENDIAN);
      final ByteBuffer scratch = ByteBuffer.allocate(sectorSize).order(ByteOrder.LITTLE_ENDIAN);

      int tmp = 0;
      if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
        String baseName = FilenameUtils.getName(fileName);
        tmp = Decryptor.HASH_ENCRYPTION_KEY.hash(baseName);
        if (block.hasFlag(MPQ.BlockTable.Block.FLAG_FIX_KEY)) {
          tmp = ((tmp + (int) block.filePos) ^ block.FSize);
        }
      }

      final int key = tmp;
      if (block.hasFlag(MPQ.BlockTable.Block.FLAG_SINGLE_UNIT)) {
        if (DEBUG) Gdx.app.debug(TAG, "Reading as single unit...");
        IOUtils.readFully(fc, sector);
        sector.flip();

        if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
          if (DEBUG) Gdx.app.debug(TAG, "Decrypting...");
          Decryptor.decrypt(key, sector);
          sector.flip();
        }

        if (block.hasFlag(MPQ.BlockTable.Block.FLAG_COMPRESSED)) {
          if (DEBUG) Gdx.app.debug(TAG, "Decompressing...");
          Decompressor.decompress(sector, buffer, scratch, block.CSize, block.FSize);
          assert buffer.position() == block.FSize;
          buffer.flip();
        }

        buffer.flip();
      } else if (block.hasFlag(MPQ.BlockTable.Block.FLAG_COMPRESSED) || block.hasFlag(MPQ.BlockTable.Block.FLAG_IMPLODE)) {
        if (DEBUG) Gdx.app.debug(TAG, "Populating sector offsets table");
        final int[] sectorOffsets = new int[sectorCount + 1];
        ByteBuffer sectors = ByteBuffer.wrap(new byte[(sectorCount + 1) << 2]).order(ByteOrder.LITTLE_ENDIAN);
        try {
          IOUtils.readFully(fc, sectors);
          sectors.flip();
        } catch (IOException e) {
          throw new GdxRuntimeException("Unable to read sector offsets: " + block, e);
        }

        if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
          if (DEBUG) Gdx.app.debug(TAG, "Decrypting sector offsets table");
          Decryptor.decrypt(key - 1, sectors);
          sectors.flip();
        }

        for (int i = 0; i < sectorCount; i++) {
          sectorOffsets[i] = sectors.getInt();
        }

        sectorOffsets[sectorCount] = block.CSize;
        if (DEBUG) Gdx.app.debug(TAG, "sector offsets = " + Arrays.toString(sectorOffsets));

        int finalSize = 0;
        for (int curSector = 0; curSector < sectorCount; curSector++) {
          if (DEBUG) Gdx.app.debug(TAG, "Reading sector " + (curSector + 1) + " / " + sectorCount + " " + block.getFlags());
          fc.position(block.filePos + sectorOffsets[curSector]);

          final int start = sectorOffsets[curSector];
          final int end = sectorOffsets[curSector + 1];
          final int CSize = end - start;
          sector.clear();
          sector.limit(CSize);
          IOUtils.readFully(fc, sector);
          sector.rewind();

          if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
            if (DEBUG) Gdx.app.debug(TAG, "Decrypting sector...");
            Decryptor.decrypt(key + curSector, sector);
            sector.flip();
          }

          if (block.hasFlag(MPQ.BlockTable.Block.FLAG_COMPRESSED)) {
            if (DEBUG) Gdx.app.debug(TAG, "Decompressing sector...");
            final int FSize = Math.min(block.FSize - finalSize, sectorSize);
            ByteBuffer slice = BufferUtils.slice(buffer, FSize);
            Decompressor.decompress(sector, slice, scratch, CSize, FSize);
            if (DEBUG) Gdx.app.debug(TAG, "Decompressed " + slice.limit() + " bytes");
            buffer.position(buffer.position() + slice.limit());
          } else {
            assert block.hasFlag(MPQ.BlockTable.Block.FLAG_IMPLODE);
            if (DEBUG) Gdx.app.debug(TAG, "Exploding sector...");
            final int FSize = Math.min(block.FSize - finalSize, sectorSize);
            ByteBuffer slice = BufferUtils.slice(buffer, FSize);
            Exploder.pkexplode(sector, slice);
            if (DEBUG) Gdx.app.debug(TAG, "Exploded to " + slice.limit() + " bytes");
            buffer.position(buffer.position() + slice.limit());
          }

          finalSize += sectorSize;
        }
      } else {
        throw new UnsupportedOperationException("File has unsupported flags " + block.getFlags());
        /*
        if (DEBUG) Gdx.app.debug(TAG, "Reading as single unit...");
        if (DEBUG) Gdx.app.debug(TAG, "Not single unit or compressed: " + block.FSize + " =? " + block.CSize);
        IOUtils.readFully(fc, sector);
        sector.flip();

        if (block.hasFlag(MPQ.BlockTable.Block.FLAG_ENCRYPTED)) {
          Gdx.app.log(TAG, "Decrypting sector...");
          Decryptor.decrypt(key, sector);
          sector.flip();
        }

        buffer.put(sector);
        buffer.flip();
        */
      }

      assert buffer.limit() == block.FSize : "buffer.limit() = " + buffer.limit() + " not equal to block.FSize " + block.FSize;
      return buffer.array();
    } catch (Exception e) {
      throw new GdxRuntimeException("Unable to read file: " + fileName, e);
    } finally {
      StreamUtils.closeQuietly(raf);
    }
  }
}
