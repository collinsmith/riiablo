package gdx.diablo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;

import gdx.diablo.cvar.Cvar;
import gdx.diablo.cvar.CvarStateAdapter;

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

    /*
    FileHandle home = null;
    try {
      Constructor constructor = ClassReflection.getDeclaredConstructor(AndroidFileHandle.class, AssetManager.class, File.class, Files.FileType.class);
      constructor.setAccessible(true);
      home = (FileHandle) constructor.newInstance(null, getContext().getExternalFilesDir(null), Files.FileType.External);
    } catch (Throwable t) {
      ExceptionUtils.wrapAndThrow(t);
    }
    */


    FileHandle home = new FileHandle(getContext().getExternalFilesDir(null));
    initialize(new Client(home), config);
    //initialize(new TestClient(home), config);

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
  }
}
