package com.riiablo.net.reliable.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

import com.riiablo.net.reliable.Log;
import com.riiablo.net.reliable.MessageChannel;
import com.riiablo.net.reliable.ReliableConfiguration;

public class UnreliableMessageChannel extends MessageChannel {
  private static final String TAG = "UnreliableMessageChannel";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_SEND = DEBUG && true;
  private static final boolean DEBUG_RECEIVE = DEBUG && true;

  public UnreliableMessageChannel(PacketTransceiver packetTransceiver) {
    super(new ReliableConfiguration(), packetTransceiver);
  }

  @Override
  public void onPacketTransmitted(ByteBuf bb) {
  }

  @Override
  public void onPacketProcessed(int sequence, ByteBuf bb) {
    if (DEBUG_RECEIVE) Log.debug(TAG, "onPacketProcessed " + bb);
    packetTransceiver.receivePacket(bb);
  }

  @Override
  public void reset() {
    packetController.reset();
  }

  @Override
  public void update(float delta, DatagramChannel ch) {
    packetController.update(delta);
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
}
