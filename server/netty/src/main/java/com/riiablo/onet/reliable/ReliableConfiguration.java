package com.riiablo.onet.reliable;

public class ReliableConfiguration {
  public int maxPacketSize = 16384;
  public int fragmentThreshold = 1024;
  public int maxFragments = 16;
  public int fragmentSize = 1024;
  public int sentPacketBufferSize = 256;
  public int receivedPacketBufferSize = 256;
  public int fragmentReassemblyBufferSize = 64;
  public float rttSmoothingFactor = 0.25f;
  public float packetLossSmoothingFactor = 0.1f;
  public float bandwidthSmoothingFactor = 0.1f;
  public int packetHeaderSize = 28;
}
