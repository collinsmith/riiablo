package com.riiablo.server;

import com.badlogic.gdx.Gdx;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerUtils {
  private static final String TAG = "ServerUtils";

  private ServerUtils() {}

  public static String getContent(BufferedReader reader) {
    try {
      int length = -1;
      for (String str; (str = reader.readLine()) != null && !str.isEmpty();) {
        if (StringUtils.startsWithIgnoreCase(str, "Content-Length:")) {
          str = StringUtils.replaceIgnoreCase(str, "Content-Length:", "").trim();
          length = NumberUtils.toInt(str, length);
        }
      }

      char[] chars = new char[length];
      reader.read(chars);
      return new String(chars);
    } catch (IOException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
      return null;
    }
  }

}
