package com.riiablo.tool;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class LwjglTool extends AbstractTool<LwjglApplication> {
  public static <T extends Tool>
  LwjglToolBuilder<T> create(
      Class<T> toolClass,
      String cmd,
      String[] args
  ) {
    return create(toolClass, cmd, args, new LwjglApplicationConfiguration()).defaults();
  }

  public static <T extends Tool>
  LwjglToolBuilder<T> create(
      Class<T> toolClass,
      String cmd,
      String[] args,
      LwjglApplicationConfiguration config
  ) {
    return new LwjglToolBuilder<>(toolClass, cmd, args, config);
  }

  public static final class LwjglToolBuilder<T extends Tool>
      extends ToolBuilder<
          LwjglApplication,
          T,
          LwjglApplicationConfiguration,
          LwjglToolBuilder<T>
      >
  {
    LwjglToolBuilder(
        Class<T> toolClass,
        String cmd,
        String[] args,
        LwjglApplicationConfiguration config
    ) {
      super(toolClass, cmd, args, config);
    }

    @Override
    public LwjglApplication newInstance(
        T toolInstance,
        LwjglApplicationConfiguration config
    ) {
      return new LwjglApplication(toolInstance, config);
    }

    @Override
    public LwjglToolBuilder<T> defaults() {
      config.title = toolClass.getSimpleName();
      config.width = 800;
      config.height = 600;
      config.resizable = true;
      config.foregroundFPS = config.backgroundFPS = 300;
      config.vSyncEnabled = false;
      config.addIcon("ic_launcher_16.png", Files.FileType.Internal);
      config.addIcon("ic_launcher_32.png", Files.FileType.Internal);
      config.addIcon("ic_launcher_128.png", Files.FileType.Internal);
      return this;
    }

    public LwjglToolBuilder<T> title(String title) {
      config.title = title;
      return this;
    }

    public LwjglToolBuilder<T> size(int width, int height) {
      return size(width, height, true);
    }

    public LwjglToolBuilder<T> size(int width, int height, boolean resizable) {
      config.width = width;
      config.height = height;
      config.resizable = resizable;
      return this;
    }
  }

  public interface LwjglToolConfigurator
      extends ToolConfigurator<LwjglApplicationConfiguration>
  {
    @Override
    void config(LwjglApplicationConfiguration config);
  }
}
