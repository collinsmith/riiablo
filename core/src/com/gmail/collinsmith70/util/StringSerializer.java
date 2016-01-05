package com.gmail.collinsmith70.util;

/**
 * Implementation of a {@link Serializer} which (de)serializes objects of the specified type
 * {@link T} to and from {@link String} representations.
 *
 * @param <T> type of object which this StringSerializer accepts
 */
public interface StringSerializer<T> extends Serializer<T, String> {
}
