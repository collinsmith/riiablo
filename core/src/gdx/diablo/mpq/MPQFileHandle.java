package gdx.diablo.mpq;

import com.badlogic.gdx.files.FileHandle;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.InputStream;

public class MPQFileHandle extends FileHandle {
  private static final String TAG = "MPQFileHandle";

  public final MPQ mpq;
  public final String fileName;

  public MPQFileHandle(MPQ mpq, String fileName) {
    this.mpq = mpq;
    this.fileName = fileName;
  }

  @Override
  public InputStream read() {
    return mpq.read(this);
  }

  @Override
  public byte[] readBytes() {
    return mpq.readBytes(this);
  }

  @Override
  public boolean exists() {
    return mpq.contains(fileName);
  }

  @Override
  public String toString() {
    return fileName;
  }

  @Override
  public long length() {
    return mpq.length(fileName);
  }

  @Override
  public String extension() {
    return FilenameUtils.getExtension(fileName);
  }

  @Override
  public String path() {
    return FilenameUtils.getFullPath(fileName);
  }

  @Override
  public String name() {
    return FilenameUtils.getName(fileName);
  }

  @Override
  public File file() {
    throw new UnsupportedOperationException("Not supported by MPQFileHandle.");
  }

  @Override
  public String pathWithoutExtension() {
    int index = FilenameUtils.indexOfExtension(fileName);
    if (index == -1) {
      return fileName;
    }

    return fileName.substring(0, index);
  }
}
