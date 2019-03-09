package com.riiablo;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.riiablo.cvar.Cvar;
import com.riiablo.cvar.CvarStateAdapter;

public class AndroidLauncher extends AndroidApplication {
  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
    config.useAccelerometer        = false;
    config.useGyroscope            = false;
    config.useCompass              = false;
    config.useRotationVectorSensor = false;
    config.maxSimultaneousSounds   = 16;

    FileHandle home = new FileHandle(getContext().getExternalFilesDir(null));
    final Client client = new Client(home, 360);
    initialize(client, config);

    Cvars.Client.Display.StatusBar.addStateListener(new CvarStateAdapter<Boolean>() {
      @Override
      public void onChanged(Cvar<Boolean> cvar, Boolean from, final Boolean to) {
        getHandler().post(new Runnable() {
          @Override
          public void run() {
            if (to.booleanValue()) {
              getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
              getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
          }
        });
      }
    });

    final ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        Rect visibleDisplayFrame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleDisplayFrame);
        int height = /*content.getRootView().getHeight() -*/ Gdx.graphics.getHeight() - visibleDisplayFrame.height();
        client.updateScreenBounds(0, height, 0, 0); // TODO: other params are unused for now
      }
    };
    final View content = getWindow().getDecorView().findViewById(android.R.id.content);
    content.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    Gdx.app.addLifecycleListener(new LifecycleListener() {
      @Override
      public void pause() {}

      @Override
      public void resume() {}

      @Override
      public void dispose() {
        content.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
      }
    });
  }
}
