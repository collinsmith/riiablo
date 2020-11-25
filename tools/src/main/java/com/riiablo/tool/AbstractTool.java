package com.riiablo.tool;

import java.lang.reflect.Constructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.Application;

public abstract class AbstractTool<A extends Application> {
  public abstract static class ToolBuilder<
      A extends Application,
      T extends Tool,
      C,
      B extends ToolBuilder<A, T, C, B>
      > {
    protected final Class<T> toolClass;
    protected final String cmd;
    protected final String[] args;
    protected final C config;

    protected ToolBuilder(Class<T> toolClass, String cmd, String[] args, C config) {
      this.toolClass = toolClass;
      this.cmd = cmd;
      this.args = args;
      this.config = config;
    }

    public B defaults() {
      return (B) this;
    }

    public B config(ToolConfigurator<C> configurator) {
      configurator.config(config);
      return (B) this;
    }

    public abstract A newInstance(T toolInstance, C config);

    public A start() {
      try {
        Constructor<T> defaultConstructor = toolClass.getDeclaredConstructor();
        defaultConstructor.setAccessible(true);
        defaultConstructor.newInstance((Object[]) null);
        T toolInstance = defaultConstructor.newInstance((Object[]) null);

        Options options = new Options();
        toolInstance.createCliOptions(options);
        try {
          CommandLine cli = toolInstance.parseCliOptions(options, args);
          toolInstance.handleCliOptions(cmd, options, cli);
        } catch (Throwable t) {
          toolInstance.handleCliError(cmd, options, t);
          return ExceptionUtils.wrapAndThrow(t);
        }

        return newInstance(toolInstance, config);
      } catch (Throwable t) {
        return ExceptionUtils.wrapAndThrow(t);
      }
    }
  }

  public interface ToolConfigurator<C> {
    void config(C config);
  }
}
