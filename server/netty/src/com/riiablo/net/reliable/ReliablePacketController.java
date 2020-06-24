package com.riiablo.net.reliable;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

import com.riiablo.net.reliable.data.FragmentReassemblyData;
import com.riiablo.net.reliable.data.ReceivedPacketData;
import com.riiablo.net.reliable.data.SentPacketData;

public class ReliablePacketController {
  private static final String TAG = "ReliablePacketController";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_SEND = DEBUG && true;
  private static final boolean DEBUG_RECEIVE = DEBUG && true;

  private final ReliableConfiguration config;
  private final MessageChannel channel;

  private final SequenceBuffer<SentPacketData> sentPackets;
  private final SequenceBuffer<ReceivedPacketData> receivedPackets;
  private final SequenceBuffer<FragmentReassemblyData> fragmentReassembly;

  private long time;

  public ReliablePacketController(ReliableConfiguration config, MessageChannel channel) {
    this.config = config;
    this.channel = channel;

    this.sentPackets = new SequenceBuffer<>(SentPacketData.class, config.sentPacketBufferSize);
    this.receivedPackets = new SequenceBuffer<>(ReceivedPacketData.class, config.receivedPacketBufferSize);
    this.fragmentReassembly = new SequenceBuffer<>(FragmentReassemblyData.class, config.fragmentReassemblyBufferSize);
  }

  public int nextSequence() {
    return channel.sequence;
  }

  private int incSequence() {
    return channel.sequence = (channel.sequence + 1) & Packet.USHORT_MAX_VALUE;
  }

  public void reset() {

  }

  public void update(long time) {
    this.time = time;
  }

  public void sendAck(int channelId, DatagramChannel ch) {

  }

  public int sendPacket(int channelId, DatagramChannel ch, ByteBuf bb) {
    if (DEBUG_SEND) Log.debug(TAG, "sendPacket " + bb);

    final int packetSize = bb.readableBytes();
    if (packetSize > config.maxPacketSize) {
      Log.error(TAG, "packet is too large to send (%d bytes), max packet size is %d bytes", packetSize, config.maxPacketSize);
      ReliableEndpoint.stats.NUM_PACKETS_TOO_LARGE_TO_SEND++;
      return -1;
    }

    final int sequence = incSequence();
    int ack, ackBits;
    synchronized (receivedPackets) {
      ack = receivedPackets.generateAck();
      ackBits = receivedPackets.generateAckBits(ack);
    }

    SentPacketData sentPacketData = sentPackets.insert(sequence);
    sentPacketData.time = this.time;
//    sentPacketData.packetSize =
    sentPacketData.acked = false;

    if (packetSize <= config.fragmentThreshold) {
      // regular packet

      ByteBuf header = ch.alloc().buffer(config.packetHeaderSize);
      int headerSize = Packet.writePacketHeader(header, channelId, sequence, ack, ackBits);

      ByteBuf composite = ch.alloc().compositeBuffer(2)
          .addComponent(true, header)
          .addComponent(true, bb);

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

        final int sequence = headerData.sequence;
        if (!receivedPackets.testInsert(sequence)) {
          Log.error(TAG, "ignoring stale packet %d", sequence);
          ReliableEndpoint.stats.NUM_PACKETS_STALE++;
          return;
        }

        if (DEBUG_RECEIVE) Log.debug(TAG, "processing packet %d", sequence);
        ByteBuf slice = bb.readSlice(bb.readableBytes());
        channel.onPacketProcessed(sequence, slice);
        // TODO...
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
    void onPacketProcessed(int sequence, ByteBuf bb);
  }
}
