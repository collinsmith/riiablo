package com.gmail.collinsmith70.util.serializer;

import com.gmail.collinsmith70.util.StringSerializer;

/**
 * Serializer which (de)serializes a {@link Integer} object into its {@link String} representation.
 */
public enum IntegerStringSerializer implements StringSerializer<Integer> {
  /**
   * @see IntegerStringSerializer
   */
  INSTANCE;

  /**
   * {@inheritDoc}
   */
  @Override
  public String serialize(Integer obj) {
    return obj.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer deserialize(String obj) {
    return Integer.parseInt(obj);
  }

}
