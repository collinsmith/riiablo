package com.riiablo.log;

import java.util.Map;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.UnmodifiableOrderedMap;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.TriConsumer;

@Deprecated
public class OrderedThreadContextMap implements ThreadContextMap, ReadOnlyStringMap {
  /**
  * Property name ({@value} ) for selecting {@code InheritableThreadLocal} (value "true") or plain
  * {@code ThreadLocal} (value is not "true") in the implementation.
  */
  public static final String INHERITABLE_MAP = "isThreadContextMapInheritable";

  private final boolean useMap;
  private final ThreadLocal<OrderedMap<String, String>> localMap;

  private static boolean inheritableMap;

  static {
    init();
  }

  static <K, V> OrderedMap<K, V> unmodifiableOrderedMap(OrderedMap<K, V> map) {
    return UnmodifiableOrderedMap.unmodifiableOrderedMap(map);
  }

  // LOG4J2-479: by default, use a plain ThreadLocal, only use InheritableThreadLocal if configured.
  // (This method is package protected for JUnit tests.)
  static ThreadLocal<OrderedMap<String, String>> createThreadLocalMap(final boolean isMapEnabled) {
    if (inheritableMap) {
      return new InheritableThreadLocal<OrderedMap<String, String>>() {
      @Override
      protected OrderedMap<String, String> childValue(final OrderedMap<String, String> parentValue) {
        return parentValue != null && isMapEnabled
            ? unmodifiableOrderedMap(new LinkedMap<>(parentValue))
            : null;
        }
      };
    }
    // if not inheritable, return plain ThreadLocal with null as initial value
    return new ThreadLocal<>();
  }

  static void init() {
    inheritableMap = PropertiesUtil.getProperties().getBooleanProperty(INHERITABLE_MAP);
  }

  public OrderedThreadContextMap() {
    this(true);
  }

  public OrderedThreadContextMap(final boolean useMap) {
    this.useMap = useMap;
    this.localMap = createThreadLocalMap(useMap);
  }

  @Override
  public void put(final String key, final String value) {
    if (!useMap) {
      return;
    }
    OrderedMap<String, String> map = localMap.get();
    map = map == null ? new LinkedMap<String, String>(1) : new LinkedMap<>(map);
    map.put(key, value);
    localMap.set(unmodifiableOrderedMap(map));
  }

  public void putAll(final Map<String, String> m) {
    if (!useMap) {
      return;
    }
    OrderedMap<String, String> map = localMap.get();
    map = map == null ? new LinkedMap<String, String>(m.size()) : new LinkedMap<>(map);
    for (final Map.Entry<String, String> e : m.entrySet()) {
      map.put(e.getKey(), e.getValue());
    }
    localMap.set(unmodifiableOrderedMap(map));
  }

  @Override
  public String get(final String key) {
    final Map<String, String> map = localMap.get();
    return map == null ? null : map.get(key);
  }

  @Override
  public void remove(final String key) {
    final Map<String, String> map = localMap.get();
    if (map != null) {
      final OrderedMap<String, String> copy = new LinkedMap<>(map);
      copy.remove(key);
      localMap.set(unmodifiableOrderedMap(copy));
    }
  }

  public void removeAll(final Iterable<String> keys) {
    final Map<String, String> map = localMap.get();
    if (map != null) {
      final OrderedMap<String, String> copy = new LinkedMap<>(map);
      for (final String key : keys) {
        copy.remove(key);
      }
      localMap.set(unmodifiableOrderedMap(copy));
    }
  }

  @Override
  public void clear() {
    localMap.remove();
  }

  @Override
  public Map<String, String> toMap() {
    return getCopy();
  }

  @Override
  public boolean containsKey(final String key) {
    final Map<String, String> map = localMap.get();
    return map != null && map.containsKey(key);
  }

  @Override
  public <V> void forEach(final BiConsumer<String, ? super V> action) {
    final Map<String, String> map = localMap.get();
    if (map == null) {
      return;
    }
    for (final Map.Entry<String, String> entry : map.entrySet()) {
      //BiConsumer should be able to handle values of any type V. In our case the values are of type String.
      @SuppressWarnings("unchecked")
      final
      V value = (V) entry.getValue();
      action.accept(entry.getKey(), value);
    }
  }

  @Override
  public <V, S> void forEach(final TriConsumer<String, ? super V, S> action, final S state) {
    final Map<String, String> map = localMap.get();
    if (map == null) {
      return;
    }
    for (final Map.Entry<String, String> entry : map.entrySet()) {
      //TriConsumer should be able to handle values of any type V. In our case the values are of type String.
      @SuppressWarnings("unchecked")
      final
      V value = (V) entry.getValue();
      action.accept(entry.getKey(), value, state);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final String key) {
    final Map<String, String> map = localMap.get();
    return (V) (map == null ? null : map.get(key));
  }

  @Override
  public Map<String, String> getCopy() {
    final Map<String, String> map = localMap.get();
    return map == null ? new LinkedMap<String, String>() : new LinkedMap<>(map);
  }

  @Override
  public Map<String, String> getImmutableMapOrNull() {
    return localMap.get();
  }

  @Override
  public boolean isEmpty() {
    final Map<String, String> map = localMap.get();
    return map == null || map.size() == 0;
  }

  @Override
  public int size() {
    final Map<String, String> map = localMap.get();
    return map == null ? 0 : map.size();
  }

  @Override
  public String toString() {
    final Map<String, String> map = localMap.get();
    return map == null ? "{}" : map.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    final Map<String, String> map = this.localMap.get();
    result = prime * result + ((map == null) ? 0 : map.hashCode());
    result = prime * result + Boolean.valueOf(this.useMap).hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (obj instanceof OrderedThreadContextMap) {
      final OrderedThreadContextMap other = (OrderedThreadContextMap) obj;
      if (this.useMap != other.useMap) {
        return false;
      }
    }
    if (!(obj instanceof ThreadContextMap)) {
      return false;
    }
    final ThreadContextMap other = (ThreadContextMap) obj;
    final Map<String, String> map = this.localMap.get();
    final Map<String, String> otherMap = other.getImmutableMapOrNull();
    if (map == null) {
      if (otherMap != null) {
        return false;
      }
    } else if (!map.equals(otherMap)) {
      return false;
    }
    return true;
  }
}
