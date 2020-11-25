package com.riiablo.tool;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

public class HeadlessTool extends AbstractTool<HeadlessApplication> {
  public static <T extends Tool>
  HeadlessToolBuilder<T> create(
      Class<T> toolClass,
      String cmd,
      String[] args
  ) {
    return create(toolClass, cmd, args, new HeadlessApplicationConfiguration());
  }

  public static <T extends Tool>
  HeadlessToolBuilder<T> create(
      Class<T> toolClass,
      String cmd,
      String[] args,
      HeadlessApplicationConfiguration config
  ) {
    return new HeadlessToolBuilder<>(toolClass, cmd, args, config);
  }

  public static final class HeadlessToolBuilder<T extends Tool>
      extends ToolBuilder<
          HeadlessApplication,
          T,
          HeadlessApplicationConfiguration,
          HeadlessToolBuilder<T>
      >
  {
    HeadlessToolBuilder(
        Class<T> toolClass,
        String cmd,
        String[] args,
        HeadlessApplicationConfiguration config
    ) {
      super(toolClass, cmd, args, config);
    }

    @Override
    public HeadlessApplication newInstance(
        T toolInstance,
        HeadlessApplicationConfiguration config
    ) {
      return new HeadlessApplication(toolInstance, config);
    }
  }
}
