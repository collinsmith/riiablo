package com.gmail.collinsmith70.unifi.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Source: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/util/Property.java
 *
 * @param <T> The class on which the property is declared.
 * @param <V> The type that this property represents.
 */
public abstract class Property<T, V> {

  public static <T, V> Property<T, V> of(Class<T> hostType, Class<V> valueType, String name) {
    return null;
  }

  private final String NAME;
  private final Class<V> TYPE;

  /**
   * A constructor that takes an identifying name and {@link #getType() type} for the property.
   */
  public Property(Class<V> type, String name) {
    this.TYPE = Preconditions.checkNotNull(type);
    this.NAME = Strings.nullToEmpty(name);
  }

  /**
   * Returns the name for this property.
   */
  public String getName() {
    return NAME;
  }

  /**
   * Returns the type for this property.
   */
  public Class<V> getType() {
    return TYPE;
  }

  /**
   * Returns true if the {@link #set(Object, Object)} method does not set the value on the target
   * object (in which case the {@link #set(Object, Object) set()} method should throw a {@link
   * NoSuchPropertyException} exception). This may happen if the Property wraps functionality that
   * allows querying the underlying value but not setting it. For example, the {@link #of(Class,
   * Class, String)} factory method may return a Property with name "foo" for an object that has
   * only a <code>getFoo()</code> or <code>isFoo()</code> method, but no matching
   * <code>setFoo()</code> method.
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns the current value that this property represents on the given <code>object</code>.
   */
  public abstract V get(T object);

  /**
   * Sets the value on <code>object</code> which this property represents. If the method is unable
   * to set the value on the target object it will throw an {@link UnsupportedOperationException}
   * exception.
   */
  public void set(T object, V value) {
    throw new UnsupportedOperationException("Property " + getName() + " is read-only");
  }

}
