package com.gmail.collinsmith70.util;

/**
 * Abstract representation of a <a href="https://en.wikipedia.org/wiki/Serialization">Serializor</a>
 * which can both serialize and deserialize objects ({@link T1}) in a specified format ({@link T2}).
 *
 * @param <T1> class which this Serializor accepts
 * @param <T2> class this Serializor (de)serializes {@link T1} into.
 */
public interface Serializer<T1, T2> {

  /**
   * Serializes an object into its {@link T2} representation.
   *
   * @param obj object to serialize
   * @return {@link T2} representation of the passed object
   */
  T2 serialize(T1 obj);

  /**
   * Deserializes an object from its {@link T2} representation.
   *
   * @param obj object to deserialize
   * @return {@link T1} representation of the passed object
   */
  T1 deserialize(T2 obj);

}
