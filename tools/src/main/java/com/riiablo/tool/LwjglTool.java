package com.riiablo.tool;

import java.lang.reflect.Constructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class LwjglTool extends LwjglApplication {
  LwjglTool(BaseTool tool, LwjglApplicationConfiguration config) {
    super(tool, config);
  }

  public static LwjglToolBuilder create(
      Class<? extends BaseTool> toolClass,
      String cmd,
      String[] args
  ) {
    return create(toolClass, cmd, args, new LwjglApplicationConfiguration()).defaults();
  }

  public static LwjglToolBuilder create(
      Class<? extends BaseTool> toolClass,
      String cmd,
      String[] args,
      LwjglApplicationConfiguration config
  ) {
    return new LwjglToolBuilder(toolClass, cmd, args, config);
  }

  public static final class LwjglToolBuilder {
    final Class<? extends BaseTool> toolClass;
    final String cmd;
    final String[] args;
    final LwjglApplicationConfiguration config;

    LwjglToolBuilder(Class<? extends BaseTool> toolClass, String cmd, String[] args, LwjglApplicationConfiguration config) {
      this.toolClass = toolClass;
      this.cmd = cmd;
      this.args = args;
      this.config = config;
    }

    public LwjglToolBuilder defaults() {
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

    public LwjglToolBuilder config(LwjglToolConfigurator configurator) {
      configurator.config(config);
      return this;
    }

    public LwjglToolBuilder title(String title) {
      config.title = title;
      return this;
    }

    public LwjglToolBuilder size(int width, int height) {
      config.width = width;
      config.height = height;
      return this;
    }

    public LwjglTool start() {
      try {
        Constructor<? extends BaseTool> defaultConstructor = toolClass.getDeclaredConstructor();
        defaultConstructor.setAccessible(true);
        defaultConstructor.newInstance((Object[]) null);
        BaseTool toolInstance = defaultConstructor.newInstance((Object[]) null);

        Options options = new Options();
        toolInstance.createCliOptions(options);
        try {
          CommandLine cli = toolInstance.parseCliOptions(options, args);
          toolInstance.handleCliOptions(cmd, options, cli);
        } catch (Throwable t) {
          toolInstance.handleCliError(cmd, options, t);
          return ExceptionUtils.wrapAndThrow(t);
        }

        return new LwjglTool(toolInstance, config);
      } catch (Throwable t) {
        return ExceptionUtils.wrapAndThrow(t);
      }
    }
  }

  public interface LwjglToolConfigurator {
    void config(LwjglApplicationConfiguration config);
  }
}
