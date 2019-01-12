package gdx.diablo;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;

public class AndroidLauncher extends AndroidApplication {
  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

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
  }
}
