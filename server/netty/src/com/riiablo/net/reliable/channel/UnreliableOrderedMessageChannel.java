package com.riiablo.net.reliable.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

import com.riiablo.net.reliable.Log;
import com.riiablo.net.reliable.MessageChannel;
import com.riiablo.net.reliable.ReliableConfiguration;

public class UnreliableOrderedMessageChannel extends MessageChannel {
  private static final String TAG = "UnreliableOrderedMessageChannel";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_SEND = DEBUG && true;
  private static final boolean DEBUG_RECEIVE = DEBUG && true;

  public UnreliableOrderedMessageChannel(PacketTransceiver packetTransceiver) {
    super(new ReliableConfiguration(), packetTransceiver);
  }

  @Override
  public void reset() {

  }

  @Override
  public void update(long time, DatagramChannel ch) {

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

  }

  @Override
  public void onPacketProcessed(int sequence, ByteBuf bb) {

  }
}
