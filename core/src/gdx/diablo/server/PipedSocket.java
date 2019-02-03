package gdx.diablo.server;

import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedSocket implements Socket {
  PipedInputStream  in;
  PipedOutputStream out;

  public PipedSocket() {
    try {
      in  = new PipedInputStream();
      out = new PipedOutputStream(in);
    } catch (IOException e) {
      throw new GdxRuntimeException(e);
    }
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public InputStream getInputStream() {
    return in;
  }

  @Override
  public OutputStream getOutputStream() {
    return out;
  }

  @Override
  public String getRemoteAddress() {
    return "localhost";
  }

  @Override
  public void dispose() {
    IOUtils.closeQuietly(in);
    IOUtils.closeQuietly(out);
  }
}
