package com.riiablo.log;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

import com.badlogic.gdx.utils.OrderedMap;

@Plugin(
    name = "MDCLayout",
    category = Node.CATEGORY,
    elementType = Layout.ELEMENT_TYPE,
    printObject = true
)
public class MDCLayout extends AbstractStringLayout {
  private final PatternLayout parent;

  private static final int MAX_DEPTH = 256;
  private static final int DEPTH_STEP = 2;

  private int depth = 0;
  private ReadOnlyStringMap ctx;
  private final byte[] spaces = StringUtils.repeat(' ', MAX_DEPTH * DEPTH_STEP).getBytes(super.getCharset());
  private final byte[] endl = System.getProperty("line.separator").getBytes(super.getCharset());

  public MDCLayout(Configuration config, Charset charset, PatternLayout parent) {
    super(config, charset, null, null);
    this.parent = parent;
  }

  @Override
  public String toSerializable(LogEvent event) {
    return parent.toSerializable(event);
  }

  @Override
  public boolean requiresLocation() {
    return parent.requiresLocation();
  }

  @Override
  public Map<String, String> getContentFormat() {
    return parent.getContentFormat();
  }

  @Deprecated
  private void writeEntry(
      ByteBufferDestination destination,
      int depth,
      OrderedMap.Entry<String, String> entry
  ) {
    byte[] b = entry.toString().getBytes(getCharset());
    destination.writeBytes(spaces, 0, (depth - 1) * DEPTH_STEP + 1);
    destination.writeBytes(b, 0, b.length);
    destination.writeBytes(endl, 0, endl.length);
  }

  private void writeEntry(
      ByteBufferDestination destination,
      int depth,
      Object obj
  ) {
    byte[] b = String.valueOf(obj).getBytes(getCharset());
    destination.writeBytes(spaces, 0, (depth - 1) * DEPTH_STEP + 1);
    destination.writeBytes(b, 0, b.length);
    destination.writeBytes(endl, 0, endl.length);
  }

  // Deprecated. May be needed when implementing different output
  @Deprecated
  private void writeMapDifference(
      ByteBufferDestination destination,
      int depth,
      final ReadOnlyStringMap parent,
      final ReadOnlyStringMap child
  ) {
    final StringBuilder sb = new StringBuilder();
    sb.append('{');
    child.forEach(new BiConsumer<String, Object>() {
      @Override
      public void accept(String s, Object o) {
        if (parent != null && Objects.equals(parent.getValue(s), o)) return;
        sb.append(s);
        sb.append(':');
        sb.append(o);
        sb.append(',');
      }
    });
    if (sb.length() > 1) sb.setLength(sb.length() - 1);
    sb.append('}');
    writeEntry(destination, depth, sb);
  }

  @Override
  public void encode(LogEvent event, ByteBufferDestination destination) {
    ReadOnlyStringMap ctx = event.getContextData();
    depth = ctx.size();
    if (depth > 0) {
      if (!ctx.equals(this.ctx)) {
        writeEntry(destination, depth, ctx);
        this.ctx = ctx;
      }

      destination.writeBytes(spaces, 0, depth * DEPTH_STEP);
    }

    parent.encode(event, destination);
  }

  @Override
  public String toString() {
    return parent.toString();
  }

  @PluginBuilderFactory
  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder implements org.apache.logging.log4j.core.util.Builder<MDCLayout> {
    @PluginElement("PatternLayout")
    @Required
    private PatternLayout patternLayout;

    // LOG4J2-783 use platform default by default
    @PluginBuilderAttribute
    private Charset charset = Charset.defaultCharset();

    @PluginConfiguration
    private Configuration configuration;

    private Builder() {}

    public Builder withPatternLayout(final PatternLayout patternLayout) {
      this.patternLayout = patternLayout;
      return this;
    }

    public Builder withConfiguration(final Configuration configuration) {
      this.configuration = configuration;
      return this;
    }

    public Builder withCharset(final Charset charset) {
      // LOG4J2-783 if null, use platform default by default
      if (charset != null) {
        this.charset = charset;
      }
      return this;
    }

    @Override
    public MDCLayout build() {
      // fall back to DefaultConfiguration
      if (configuration == null) {
        configuration = new DefaultConfiguration();
      }
      return new MDCLayout(configuration, charset, patternLayout);
    }
  }
}
