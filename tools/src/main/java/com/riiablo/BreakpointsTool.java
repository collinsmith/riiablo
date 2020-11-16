package com.riiablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;

import java.util.Arrays;

public class BreakpointsTool extends ApplicationAdapter {
  private static final String TAG = "BreakpointsTool";

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new BreakpointsTool(), config);
  }

  BreakpointsTool() {}

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    int[] efcrs = new int[25];
    Arrays.fill(efcrs, Integer.MIN_VALUE);

    int[] breakpoints = new int[25];
    Arrays.fill(breakpoints, Integer.MIN_VALUE);

    int numFrames = 14;
    int speed = 256;
    for (int fcr = 0; fcr <= 200; fcr++) {
      int efcr = efcr(fcr);
      int i = frames(numFrames, speed, efcr);
      if (breakpoints[i] == Integer.MIN_VALUE) {
        breakpoints[i] = fcr;
        efcrs[i] = efcr;
      }
    }

    for (int i = breakpoints.length - 1; i >= 0; i--) {
      if (breakpoints[i] == Integer.MIN_VALUE) continue;
      Gdx.app.log(TAG, String.format("%2d=%-3d (%d)", i, breakpoints[i], efcrs[i]));
    }

    Gdx.app.exit();
  }

  private int efcr(int fcr) {
    return fcr * 120 / (fcr + 120);
  }

  private int frames(int numFrames, int speed, int efcr) {
    efcr = Math.min(efcr, 75);
    int i = speed * (100 + efcr) / 100;
    float base = 256f * numFrames / i;
    return MathUtils.ceilPositive(base) - 1;
  }
}
