package com.riiablo.onet.reliable.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import java.net.SocketAddress;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Queue;

import com.riiablo.onet.reliable.Log;
import com.riiablo.onet.reliable.MessageChannel;
import com.riiablo.onet.reliable.Packet;
import com.riiablo.onet.reliable.ReliableConfiguration;
import com.riiablo.onet.reliable.ReliableUtils;
import com.riiablo.onet.reliable.SequenceBuffer;

public class ReliableMessageChannel extends MessageChannel {
  private static final String TAG = "ReliableMessageChannel";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_SEND = DEBUG && true;
  private static final boolean DEBUG_RECEIVE = DEBUG && true;

  private final ByteBuf packetBuffer = Unpooled.buffer();
  private final SequenceBuffer<BufferedPacket> sendBuffer;
  private final SequenceBuffer<BufferedPacket> receiveBuffer;
  private final SequenceBuffer<OutgoingPacketSet> ackBuffer;

  private final Queue<ByteBuf> messageQueue = new Queue<>(64, ByteBuf.class);
  private final IntArray outgoingMessageIds = new IntArray(256);

  private float time;
  private float lastBufferFlush;
  private float lastMessageSend;

  private int oldestUnacked;
//  private int sequence; // hides MessageChannel#sequence
  private int nextReceive;

  private boolean congestionControl = false;
  private float congestionDisableTimer;
  private float congestionDisableInterval;
  private float lastCongestionSwitchTime;

  public ReliableMessageChannel(PacketTransceiver packetTransceiver) {
    super(new ReliableConfiguration(), packetTransceiver);

    this.sendBuffer = new SequenceBuffer<>(BufferedPacket.class, 256);
    this.receiveBuffer = new SequenceBuffer<>(BufferedPacket.class, 256);
    this.ackBuffer = new SequenceBuffer<>(OutgoingPacketSet.class, 256);

    time = 0.0f;
    lastBufferFlush = -1.0f;
    lastMessageSend = 0.0f;

    congestionDisableInterval = 5.0f;

    sequence = 0;
    nextReceive = 0;
    oldestUnacked = 0;
  }


  @Override
  public void reset() {
    packetController.reset();

    sendBuffer.reset();
//    receiveBuffer.reset(); // this isn't in the original code? why?
    ackBuffer.reset();

    lastBufferFlush = -1.0f;
    lastMessageSend = 0.0f;

    congestionControl = false;
    lastCongestionSwitchTime = 0.0f;
    congestionDisableTimer = 0.0f;
    congestionDisableInterval = 5.0f;

    sequence = 0;
    nextReceive = 0;
    oldestUnacked = 0;
  }

  @Override
  public void update(float delta, int channelId, DatagramChannel ch) {
    packetController.update(delta);

    time += delta;

    // see if we can pop messages off of the message queue and put them into the send queue
    updateQueue(channelId, ch);
    updateCongestion(delta, channelId, ch);
  }

  private void updateQueue(int channelId, DatagramChannel ch) {
    if (messageQueue.size > 0) {
      int sendBufferSize = 0;
      for (int seq = oldestUnacked; ReliableUtils.sequenceLessThan(seq, sequence); seq = (seq + 1) & Packet.USHORT_MAX_VALUE) {
        if (sendBuffer.exists(seq)) sendBufferSize++;
      }

      if (sendBufferSize < sendBuffer.numEntries) {
        ByteBuf packetData = messageQueue.removeFirst();
        sendMessage(channelId, ch, packetData);
      }
    }
  }

  private void updateCongestion(float delta, int channelId, DatagramChannel ch) {
    boolean conditionsBad = packetController.rtt() >= 250.0f; // 250ms

    // if conditions are bad, immediately enable congestion control and reset the congestion timer
    if (conditionsBad) {
      if (!congestionControl) {
        // if we're within 10 seconds of the last time we switched, double the threshold interval
        if (time - lastCongestionSwitchTime < 10.0) {
          congestionDisableInterval = Math.min(congestionDisableInterval * 2, 60.0f);
        }

        lastCongestionSwitchTime = time;
      }

      congestionControl = true;
      congestionDisableTimer = 0.0f;
    }

    // if we're in bad mode, and conditions are good, update the timer and see if we can disable
    // congestion control
    if (congestionControl && !conditionsBad) {
      congestionDisableTimer += delta;
      if (congestionDisableTimer >= congestionDisableInterval) {
        congestionControl = false;
        lastCongestionSwitchTime = time;
        congestionDisableTimer = 0.0f;
      }
    }

    // as long as conditions are good, halve the threshold interval every 10 seconds
    if (!congestionControl) {
      congestionDisableTimer += delta;
      if (congestionDisableTimer > 10.0f) {
        congestionDisableInterval = Math.max(congestionDisableInterval * 0.5f, 5.0f);
      }
    }

    // if we're in congestion control mode, only send packets 10 times per second. otherwise, send
    // 30 times per second
    float flushInterval = congestionControl ? (1.0f / 10) : (1.0f / 30);
    if (time - lastBufferFlush >= flushInterval) {
      lastBufferFlush = time;
      processSendBuffer(channelId, ch);
    }
  }

  private void processSendBuffer(int channelId, DatagramChannel ch) {
//    int numUnacked = 0;
//    for (int seq = oldestUnacked; ReliableUtils.sequenceLessThan(seq, sequence);  seq = (seq + 1) & Packet.USHORT_MAX_VALUE) {
//      numUnacked++;
//    }

    for (int seq = oldestUnacked; ReliableUtils.sequenceLessThan(seq, sequence); seq = (seq + 1) & Packet.USHORT_MAX_VALUE) {
      // never send message ID >= (oldestUnacked + bufferSize)
      if (seq >= (oldestUnacked + 256)) break;

      // for any message that hasn't been sent in the last 0.1 seconds and fits in the available
      // space of our message packer, add it
      BufferedPacket packet = sendBuffer.find(seq);
      if (packet != null && !packet.writeLock) {
        if (MathUtils.isEqual(time, packet.time, 0.1f)) continue;
        boolean packetFits = false;
        int packetSize = packetBuffer.readableBytes() + packet.bb.readableBytes();
        if (packet.bb.readableBytes() < config.fragmentThreshold) {
          packetFits = packetSize <= (config.fragmentThreshold - Packet.MAX_PACKET_HEADER_SIZE);
        } else {
          packetFits = packetSize <= (config.maxPacketSize - Packet.FRAGMENT_HEADER_SIZE - Packet.MAX_PACKET_HEADER_SIZE);
        }

        // if the packet won't fit, flush the message packet
        if (!packetFits) {
          flushPacketBuffer(channelId, ch);
        }

        packet.time = time;
        packetBuffer.writeBytes(packet.bb);
        outgoingMessageIds.add(seq);
        lastMessageSend = time;
      }
    }

    // if it has been 0.1 seconds since the last time we sent a message, send an empty message
    if (time - lastMessageSend >= 0.1f) {
      packetController.sendAck(channelId, ch);
      lastMessageSend = time;
    }

    // flush and remaining messages in the packet buffer
    flushPacketBuffer(channelId, ch);
  }

  private void flushPacketBuffer(int channelId, DatagramChannel ch) {
    if (packetBuffer.readableBytes() > 0) {
      int outgoingSeq = packetController.sendPacket(channelId, ch, packetBuffer);
      OutgoingPacketSet outgoingPacket = ackBuffer.insert(outgoingSeq);

      // store message IDs so we can map packet-level acks to message ID acks
      outgoingPacket.messageIds.clear();
      outgoingPacket.messageIds.addAll(outgoingMessageIds);

      packetBuffer.clear();
      outgoingMessageIds.clear();
    }
  }

  @Override
  public void sendMessage(int channelId, DatagramChannel ch, ByteBuf bb) {
    if (DEBUG_SEND) Log.debug(TAG, "sendMessage " + bb);

    int sendBufferSize = 0;
    for (int seq = oldestUnacked; ReliableUtils.sequenceLessThan(seq, sequence); seq = (seq + 1) & Packet.USHORT_MAX_VALUE) {
      if (sendBuffer.exists(seq)) sendBufferSize++;
    }

    // TODO: make sure this doesn't leak
    if (sendBufferSize == sendBuffer.numEntries) {
      messageQueue.addLast(bb);
      return;
    }

    final int sequence = incSequence();
    BufferedPacket packet = sendBuffer.insert(sequence);
    packet.time = -1.0f;

    // ensure size for header
    // TODO: prepend sequence and variable length field for size of packet.
    //       variable length is 1 or 2 bytes depending on value and 2 byte sequence id ushort
    // https://github.com/KillaMaaki/ReliableNetcode.NET/blob/c5a7339e2de70f52bfda2078f1bbdab2ec9a85c1/ReliableNetcode/MessageChannel.cs#L331-L393

    packet.bb = bb;
    packet.writeLock = false;
  }

  @Override
  public void onMessageReceived(ChannelHandlerContext ctx, DatagramPacket packet) {
    if (DEBUG_SEND) Log.debug(TAG, "onMessageReceived " + packet);
    packetController.onPacketReceived(ctx, packet);
  }

  @Override
  public void onPacketTransmitted(ByteBuf bb) {

  }

  @Override
  public void onAckProcessed(ChannelHandlerContext ctx, SocketAddress from, int sequence) {
    if (DEBUG_RECEIVE) Log.debug(TAG, "onAckProcessed " + sequence);
    // first, map sequence to message IDs and ack them
    OutgoingPacketSet outgoingPacket = ackBuffer.find(sequence);
    if (outgoingPacket == null) return;

    // process messages
    final int[] messageIds = outgoingPacket.messageIds.items;
    for (int i = 0, s = outgoingPacket.messageIds.size; i < s; i++) {
      // remove acked message from send buffer
      int messageId = messageIds[i];
      if (sendBuffer.exists(messageId)) {
        sendBuffer.find(messageId).writeLock = true;
        sendBuffer.remove(messageId);
      }
    }

    // update oldest unacked message
    boolean allAcked = true;
    for (int seq = oldestUnacked;
         seq == this.sequence || ReliableUtils.sequenceLessThan(seq, this.sequence);
         seq = (seq + 1) & Packet.USHORT_MAX_VALUE) {
      // if it's still in the send buffer, it hasn't been acked
      if (sendBuffer.exists(seq)) {
        oldestUnacked = seq;
        allAcked = false;
        break;
      }
    }

    if (allAcked) oldestUnacked = this.sequence;
  }

  @Override
  public void onPacketProcessed(ChannelHandlerContext ctx, SocketAddress from, int sequence, ByteBuf bb) {
    if (DEBUG_RECEIVE) Log.debug(TAG, "onPacketProcessed " + sequence + " " + bb);
    packetTransceiver.receivePacket(ctx, from, bb);
    // TODO: this is different from original function, see above note within #sendMessage
  }

  public static class BufferedPacket {
    boolean writeLock = true;
    float   time;
    ByteBuf bb;
  }

  public static class OutgoingPacketSet {
    final IntArray messageIds = new IntArray();
  }
}
