package com.riiablo.log;

import java.nio.charset.Charset;
import java.util.Map;
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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
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
  private final OrderedMap.Entry<String, String> tail = new ObjectMap.Entry<>();
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

  @Override
  public void encode(LogEvent event, ByteBufferDestination destination) {
    OrderedMap<String, String> ctx = CTX.map();
    int depth = ctx.size;
    if (this.depth != depth) {
      this.depth = depth;
    }

    if (depth > 0) {
      Array<String> ordered = ctx.orderedKeys();
      String tailKey = ordered.peek();
      String tailValue = ctx.get(tailKey);
      if (tailKey.equals(tail.key)) {
        if (!tailValue.equals(tail.value)) {
          tail.value = tailValue;
          writeEntry(destination, depth, tail);
        }
      } else {
        tail.key = tailKey;
        tail.value = tailValue;
        writeEntry(destination, depth, tail);
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
