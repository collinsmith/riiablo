package com.riiablo.tool;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * must replace all lwjgl tools with this tool
 */
public class Lwjgl3Tool extends AbstractTool<Lwjgl3Application> {
  public static <T extends Tool>
  Lwjgl3ToolBuilder<T> create(
      Class<T> toolClass,
      String cmd,
      String[] args
  ) {
    return create(toolClass, cmd, args, new Lwjgl3ApplicationConfiguration()).defaults();
  }

  public static <T extends Tool>
  Lwjgl3ToolBuilder<T> create(
      Class<T> toolClass,
      String cmd,
      String[] args,
      Lwjgl3ApplicationConfiguration config
  ) {
    return new Lwjgl3ToolBuilder<>(toolClass, cmd, args, config);
  }

  public static final class Lwjgl3ToolBuilder<T extends Tool>
      extends ToolBuilder<
          Lwjgl3Application,
          T,
          Lwjgl3ApplicationConfiguration,
          Lwjgl3ToolBuilder<T>
      >
  {
    Lwjgl3ToolBuilder(
        Class<T> toolClass,
        String cmd,
        String[] args,
        Lwjgl3ApplicationConfiguration config
    ) {
      super(toolClass, cmd, args, config);
    }

    @Override
    public Lwjgl3Application newInstance(
        T toolInstance,
        Lwjgl3ApplicationConfiguration config
    ) {
      return new Lwjgl3Application(toolInstance, config);
    }

    @Override
    public Lwjgl3ToolBuilder<T> defaults() {
      config.setTitle(toolClass.getSimpleName());
      config.setWindowedMode(800, 600);
      config.setResizable(true);
      config.setForegroundFPS(300);
      config.setIdleFPS(300);
      config.useVsync(false);
      config.setWindowIcon(
          "ic_launcher_16.png",
          "ic_launcher_32.png",
          "ic_launcher_128.png"
      );
      return this;
    }

    public Lwjgl3ToolBuilder<T> title(String title) {
      config.setTitle(title);
      return this;
    }

    public Lwjgl3ToolBuilder<T> size(int width, int height) {
      return size(width, height, true);
    }

    public Lwjgl3ToolBuilder<T> size(int width, int height, boolean resizable) {
      config.setWindowedMode(width, height);
      config.setResizable(resizable);
      return this;
    }
  }

  public interface Lwjgl3ToolConfigurator
      extends ToolConfigurator<Lwjgl3ApplicationConfiguration>
  {
    @Override
    void config(Lwjgl3ApplicationConfiguration config);
  }
}
