package com.riiablo.onet.reliable.data;

import io.netty.buffer.ByteBuf;

import com.artemis.utils.BitVector;

public class FragmentReassemblyData {
  public int     sequence;
  public int     ack;
  public int     ackBits;
  public int     numFragmentsReceived;
  public int     numFragmentsTotal;
  public ByteBuf dataBuffer;
  public int     packetBytes;
  public int     headerOffset;

  public final BitVector fragmentReceived = new BitVector(256);
}
