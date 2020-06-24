package com.riiablo.net.reliable;

import java.util.Arrays;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class SequenceBuffer<T> {
  public static final int INVALID_SEQUENCE = -1; // 0xFFFFFFFF
  
  private int sequence;

  private final int    numEntries;
  private final int    entrySequence[];
  private final Object entryData[];

  public SequenceBuffer(Class<T> dataContainer, int bufferSize) {
    numEntries = bufferSize;
    entrySequence = new int[numEntries];
    Arrays.fill(entrySequence, INVALID_SEQUENCE);
    entryData = new Object[numEntries];
    try {
      for (int i = 0; i < numEntries; i++) entryData[i] = dataContainer.newInstance();
    } catch (Throwable t) {
      ExceptionUtils.wrapAndThrow(t);
    }

    sequence = 0;
  }

  public void reset() {
    sequence = 0;
    Arrays.fill(entrySequence, INVALID_SEQUENCE);
  }

  public void removeEntries(int startSequence, int endSequence) {
    startSequence &= Packet.USHORT_MAX_VALUE;
    endSequence   &= Packet.USHORT_MAX_VALUE;
    if (endSequence < startSequence) {
      Arrays.fill(entrySequence, startSequence, numEntries, INVALID_SEQUENCE);
      Arrays.fill(entrySequence, 0, endSequence, INVALID_SEQUENCE);
    } else {
      Arrays.fill(entrySequence, startSequence, endSequence, INVALID_SEQUENCE);
    }
  }

  public boolean testInsert(int sequence) {
    sequence &= Packet.USHORT_MAX_VALUE;
    return !ReliableUtils.sequenceLessThan(sequence, (this.sequence - numEntries) & Packet.USHORT_MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public T insert(int sequence) {
    sequence &= Packet.USHORT_MAX_VALUE;
    if (ReliableUtils.sequenceLessThan(sequence, (this.sequence - numEntries) & Packet.USHORT_MAX_VALUE)) {
      return null;
    }

    final int nextSequence = (sequence + 1) & Packet.USHORT_MAX_VALUE;
    if (ReliableUtils.sequenceGreaterThan(nextSequence, this.sequence)) {
      removeEntries(this.sequence, sequence);
      this.sequence = nextSequence;
    }

    int index = sequence % numEntries;
    entrySequence[index] = sequence;
    return (T) entryData[index];
  }

  public void remove(int sequence) {
    sequence &= Packet.USHORT_MAX_VALUE;
    entrySequence[sequence % numEntries] = INVALID_SEQUENCE;
  }

  public boolean available(int sequence) {
    sequence &= Packet.USHORT_MAX_VALUE;
    return entrySequence[sequence % numEntries] == INVALID_SEQUENCE;
  }

  public boolean exists(int sequence) {
    sequence &= Packet.USHORT_MAX_VALUE;
    return entrySequence[sequence % numEntries] == sequence;
  }

  @SuppressWarnings("unchecked")
  public T find(int sequence) {
    sequence &= Packet.USHORT_MAX_VALUE;
    int index = sequence % numEntries;
    return entrySequence[index] == sequence ? (T) entryData[index] : null;
  }

  @SuppressWarnings("unchecked")
  public T atIndex(int index) {
    return entrySequence[index] != INVALID_SEQUENCE ? (T) entryData[index] : null;
  }

  public int generateAck() {
    return (sequence - 1) & Packet.USHORT_MAX_VALUE;
  }

  public int generateAckBits(int ack) {
    ack &= Packet.USHORT_MAX_VALUE;
    int ackBits = 0;
    for (int i = 0, mask = 1; i < Integer.SIZE; i++, mask <<= 1) {
      if (exists(ack - i)) ackBits |= mask;
    }
    return ackBits;
  }
}
