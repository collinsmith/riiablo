package com.riiablo.net.reliable.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

import com.riiablo.net.reliable.Log;
import com.riiablo.net.reliable.MessageChannel;
import com.riiablo.net.reliable.Packet;
import com.riiablo.net.reliable.ReliableConfiguration;
import com.riiablo.net.reliable.ReliableUtils;

public class UnreliableOrderedMessageChannel extends MessageChannel {
  private static final String TAG = "UnreliableOrderedMessageChannel";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_SEND = DEBUG && true;
  private static final boolean DEBUG_RECEIVE = DEBUG && true;

  private int nextSequence = 0;

  public UnreliableOrderedMessageChannel(PacketTransceiver packetTransceiver) {
    super(new ReliableConfiguration(), packetTransceiver);
  }

  @Override
  public void reset() {
    nextSequence = 0;
    packetController.reset();
  }

  @Override
  public void update(long time, DatagramChannel ch) {
    packetController.update(time);
  }

  @Override
  public void sendMessage(int channelId, DatagramChannel ch, ByteBuf bb) {
    if (DEBUG_SEND) Log.debug(TAG, "sendMessage " + bb);
    packetController.sendPacket(channelId, ch, bb);
  }

  @Override
  public void onMessageReceived(ChannelHandlerContext ctx, DatagramPacket packet) {
    if (DEBUG_RECEIVE) Log.debug(TAG, "onMessageReceived " + packet);
    packetController.onPacketReceived(ctx, packet);
  }

  @Override
  public void onPacketTransmitted(ByteBuf bb) {
    packetTransceiver.sendPacket(bb);
  }

  @Override
  public void onPacketProcessed(int sequence, ByteBuf bb) {
    if (sequence == nextSequence || ReliableUtils.sequenceGreaterThan(sequence, nextSequence)) {
      nextSequence = (sequence + 1) & Packet.USHORT_MAX_VALUE;
      packetTransceiver.receivePacket(bb);
    }
  }
}
