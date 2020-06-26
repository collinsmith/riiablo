package com.riiablo.net.reliable;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

import com.badlogic.gdx.math.MathUtils;

import com.riiablo.net.reliable.data.FragmentReassemblyData;
import com.riiablo.net.reliable.data.ReceivedPacketData;
import com.riiablo.net.reliable.data.SentPacketData;

public class ReliablePacketController {
  private static final String TAG = "ReliablePacketController";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_SEND = DEBUG && true;
  private static final boolean DEBUG_RECEIVE = DEBUG && true;

  private static final float TOLERANCE = 0.00001f;

  private final ReliableConfiguration config;
  private final MessageChannel channel;

  private final SequenceBuffer<SentPacketData> sentPackets;
  private final SequenceBuffer<ReceivedPacketData> receivedPackets;
  private final SequenceBuffer<FragmentReassemblyData> fragmentReassembly;

  private float time;
  private float rtt;
  private float packetLoss;
  private float sentBandwidth;
  private float receivedBandwidth;
  private float ackedBandwidth;

  public ReliablePacketController(ReliableConfiguration config, MessageChannel channel) {
    this.config = config;
    this.channel = channel;

    this.sentPackets = new SequenceBuffer<>(SentPacketData.class, config.sentPacketBufferSize);
    this.receivedPackets = new SequenceBuffer<>(ReceivedPacketData.class, config.receivedPacketBufferSize);
    this.fragmentReassembly = new SequenceBuffer<>(FragmentReassemblyData.class, config.fragmentReassemblyBufferSize);
  }

  public float rtt() {
    return rtt;
  }

  public void reset() {
    channel.sequence = 0;
    for (int i = 0, s = config.fragmentReassemblyBufferSize; i < s; i++) {
      FragmentReassemblyData reassemblyData = fragmentReassembly.atIndex(i);
      if (reassemblyData != null) reassemblyData.dataBuffer.clear();
    }

    sentPackets.reset();
    receivedPackets.reset();
    fragmentReassembly.reset();
  }

  public void update(float delta) {
    time += delta;
    updatePacketLoss();
    updateSentBandwidth();
    updateReceivedBandwidth();
    updateAckedBandwidth();
  }

  private void updatePacketLoss() {
    int baseSequence = (sentPackets.getSequence() - config.sentPacketBufferSize + 1 + Packet.USHORT_MAX_VALUE) & Packet.USHORT_MAX_VALUE;

    int numDropped = 0;
    int numSamples = config.sentPacketBufferSize / 2;
    for (int i = 0; i < numSamples; i++) {
      int sequence = (baseSequence + i) & Packet.USHORT_MAX_VALUE;
      SentPacketData sentPacketData = sentPackets.find(sequence);
      if (sentPacketData != null && !sentPacketData.acked) numDropped++;
    }

    float packetLoss = numDropped / (float) numSamples;
    if (MathUtils.isEqual(this.packetLoss, packetLoss, TOLERANCE)) {
      this.packetLoss += (packetLoss - this.packetLoss) * config.packetLossSmoothingFactor;
    } else {
      this.packetLoss = packetLoss;
    }
  }

  private void updateSentBandwidth() {
    int baseSequence = (sentPackets.getSequence() - config.sentPacketBufferSize + 1 + Packet.USHORT_MAX_VALUE) & Packet.USHORT_MAX_VALUE;

    int bytesSent = 0;
    float startTime = Float.MAX_VALUE;
    float finishTime = 0f;
    int numSamples = config.sentPacketBufferSize / 2;
    for (int i = 0; i < numSamples; i++) {
      int sequence = (baseSequence + i) & Packet.USHORT_MAX_VALUE;
      SentPacketData sentPacketData = sentPackets.find(sequence);
      if (sentPacketData == null) continue;
      bytesSent += sentPacketData.packetSize;
      startTime = Math.min(startTime, sentPacketData.time);
      finishTime = Math.max(finishTime, sentPacketData.time);
    }

    if (startTime != Float.MAX_VALUE && finishTime != 0f) {
      float sentBandwidth = bytesSent / (finishTime - startTime) * 8f / 1000f;
      if (MathUtils.isEqual(this.sentBandwidth, sentBandwidth, TOLERANCE)) {
        this.sentBandwidth += (sentBandwidth - this.sentBandwidth) * config.bandwidthSmoothingFactor;
      } else {
        this.sentBandwidth = sentBandwidth;
      }
    }
  }

  private void updateReceivedBandwidth() {
    synchronized (receivedPackets) {
      int baseSequence = (receivedPackets.getSequence() - config.receivedPacketBufferSize + 1 + Packet.USHORT_MAX_VALUE) & Packet.USHORT_MAX_VALUE;

      int bytesReceived = 0;
      float startTime = Float.MAX_VALUE;
      float finishTime = 0f;
      int numSamples = config.receivedPacketBufferSize / 2;
      for (int i = 0; i < numSamples; i++) {
        int sequence = (baseSequence + i) & Packet.USHORT_MAX_VALUE;
        ReceivedPacketData receivedPacketData = receivedPackets.find(sequence);
        if (receivedPacketData == null) continue;
        bytesReceived += receivedPacketData.packetSize;
        startTime = Math.min(startTime, receivedPacketData.time);
        finishTime = Math.max(finishTime, receivedPacketData.time);
      }

      if (startTime != Float.MAX_VALUE && finishTime != 0f) {
        float receivedBandwidth = bytesReceived / (finishTime - startTime) * 8f / 1000f;
        if (MathUtils.isEqual(this.receivedBandwidth, receivedBandwidth, TOLERANCE)) {
          this.receivedBandwidth += (receivedBandwidth - this.receivedBandwidth) * config.bandwidthSmoothingFactor;
        } else {
          this.receivedBandwidth = receivedBandwidth;
        }
      }
    }
  }

  private void updateAckedBandwidth() {
    int baseSequence = (sentPackets.getSequence() - config.sentPacketBufferSize + 1 + Packet.USHORT_MAX_VALUE) & Packet.USHORT_MAX_VALUE;

    int bytesSent = 0;
    float startTime = Float.MAX_VALUE;
    float finishTime = 0f;
    int numSamples = config.sentPacketBufferSize / 2;
    for (int i = 0; i < numSamples; i++) {
      int sequence = (baseSequence + i) & Packet.USHORT_MAX_VALUE;
      SentPacketData sentPacketData = sentPackets.find(sequence);
      if (sentPacketData == null || !sentPacketData.acked) continue;
      bytesSent += sentPacketData.packetSize;
      startTime = Math.min(startTime, sentPacketData.time);
      finishTime = Math.max(finishTime, sentPacketData.time);
    }

    if (startTime != Float.MAX_VALUE && finishTime != 0f) {
      float ackedBandwidth = bytesSent / (finishTime - startTime) * 8f / 1000f;
      if (MathUtils.isEqual(this.ackedBandwidth, ackedBandwidth, TOLERANCE)) {
        this.ackedBandwidth += (ackedBandwidth - this.ackedBandwidth) * config.bandwidthSmoothingFactor;
      } else {
        this.ackedBandwidth = ackedBandwidth;
      }
    }
  }

  public void sendAck(int channelId, DatagramChannel ch) {
    if (DEBUG_SEND) Log.debug(TAG, "sendAck");

    int ack, ackBits;
    synchronized (receivedPackets) {
      ack = receivedPackets.generateAck();
      ackBits = receivedPackets.generateAckBits(ack);
    }

    ByteBuf packet = ch.alloc().directBuffer(config.packetHeaderSize);
    int headerSize = Packet.writeAck(packet, channelId, ack, ackBits);
    if (headerSize < 0) {
      Log.error(TAG, "failed to write ack");
      ReliableEndpoint.stats.NUM_ACKS_INVALID++;
      return;
    }

    channel.onPacketTransmitted(packet);
    ch.writeAndFlush(packet);
  }

  public int sendPacket(int channelId, DatagramChannel ch, ByteBuf bb) {
    if (DEBUG_SEND) Log.debug(TAG, "sendPacket " + bb);

    final int packetSize = bb.readableBytes();
    if (packetSize > config.maxPacketSize) {
      Log.error(TAG, "packet is too large to send (%d bytes), max packet size is %d bytes", packetSize, config.maxPacketSize);
      ReliableEndpoint.stats.NUM_PACKETS_TOO_LARGE_TO_SEND++;
      return -1;
    }

    final int sequence = channel.incSequence();
    if (DEBUG_SEND) Log.debug(TAG, "packet sequence set to %d", sequence);

    int ack, ackBits;
    synchronized (receivedPackets) {
      ack = receivedPackets.generateAck();
      ackBits = receivedPackets.generateAckBits(ack);
    }

    SentPacketData sentPacketData = sentPackets.insert(sequence);
    sentPacketData.time = this.time;
    sentPacketData.packetSize = packetSize;
    sentPacketData.acked = false;

    if (packetSize <= config.fragmentThreshold) {
      // regular packet

      ByteBuf header = ch.alloc().buffer(config.packetHeaderSize);
      int headerSize = Packet.writePacketHeader(header, channelId, sequence, ack, ackBits);

      ByteBuf composite = ch.alloc().compositeBuffer(2)
          .addComponent(true, header)
          .addComponent(true, bb);

      channel.onPacketTransmitted(composite);
      ch.writeAndFlush(composite);
      return headerSize;
    } else {
      // fragmented packet

      throw new UnsupportedOperationException();
//      return -1;
    }
  }

  public void onPacketReceived(ChannelHandlerContext ctx, DatagramPacket packet) {
    if (DEBUG_RECEIVE) Log.debug(TAG, "onPacketReceived " + packet);

    final ByteBuf bb = packet.content();
    final int packetSize = bb.readableBytes();
    if (packetSize > config.maxPacketSize) {
      Log.error(TAG, "packet is too large to receive (%d bytes), max packet size is %d bytes", packetSize, config.maxPacketSize);
      ReliableEndpoint.stats.NUM_PACKETS_TOO_LARGE_TO_RECEIVE++;
      return;
    }

    final byte flags = Packet.getFlags(bb);
    if (!Packet.isFragmented(flags)) {
      // regular packet

      ReliableEndpoint.stats.NUM_PACKETS_RECEIVED++;

      Packet.HeaderData headerData = null;
      try {
        headerData = Packet.obtainData();
        int headerSize = Packet.readPacketHeader(config, bb, headerData);
        if (headerSize == -1) {
          Log.error(TAG, "ignoring invalid packet. could not read packet header");
          ReliableEndpoint.stats.NUM_PACKETS_INVALID++;
          return;
        }

        final boolean isStale;
        final int sequence = headerData.sequence;
        synchronized (receivedPackets) {
          isStale = !receivedPackets.testInsert(sequence);
        }

        if (DEBUG_RECEIVE) Log.debug(TAG, "packet reported sequence as %d", sequence);
        final boolean isAck = Packet.isAck(flags);
        if (!isStale && !isAck) {
          if (DEBUG_RECEIVE) Log.debug(TAG, "processing packet %d", sequence);
          ByteBuf slice = bb.readSlice(bb.readableBytes());
          channel.onPacketProcessed(ctx, sequence, slice);
          synchronized (receivedPackets) {
            ReceivedPacketData receivedPacketData = receivedPackets.insert(sequence);
            receivedPacketData.time = time;
            receivedPacketData.packetSize =  packetSize;
          }
        }

        if (!isStale || isAck) {
          final int ack = headerData.ack;
          for (int i = 0, ackBits = headerData.ackBits; i < Integer.SIZE && ackBits != 0; i++, ackBits >>>= 1) {
            if ((ackBits & 1) != 0) {
              int ackSequence = (ack - i) & Packet.USHORT_MAX_VALUE;
              SentPacketData sentPacketData = sentPackets.find(ackSequence);
              if (sentPacketData != null && !sentPacketData.acked) {
                if (DEBUG_RECEIVE) Log.debug(TAG, "acked packet %d", ackSequence);
                ReliableEndpoint.stats.NUM_PACKETS_ACKED++;
                sentPacketData.acked = true;
                channel.onAckProcessed(ctx, ackSequence);

                float rtt = (time - sentPacketData.time) * 1000f;
                if ((this.rtt == 0.0f && rtt > 0.0f) || MathUtils.isEqual(this.rtt, rtt, TOLERANCE)) {
                  this.rtt = rtt;
                } else {
                  this.rtt += (rtt - this.rtt) * config.rttSmoothingFactor;
                }
              }
            }
          }
        }

        if (isStale) {
          Log.error(TAG, "ignoring stale packet %d", sequence);
          ReliableEndpoint.stats.NUM_PACKETS_STALE++;
          return;
        }
      } finally {
        if (headerData != null) headerData.free();
      }
    } else {
      // fragmented packet

      throw new UnsupportedOperationException();
    }
  }

  public interface PacketListener {
    void onPacketTransmitted(ByteBuf bb);
    void onAckProcessed(ChannelHandlerContext ctx, int sequence);
    void onPacketProcessed(ChannelHandlerContext ctx, int sequence, ByteBuf bb);
  }
}
