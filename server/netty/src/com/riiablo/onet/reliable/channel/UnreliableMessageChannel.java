package com.riiablo.onet.reliable.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import java.net.SocketAddress;

import com.riiablo.onet.reliable.Log;
import com.riiablo.onet.reliable.MessageChannel;
import com.riiablo.onet.reliable.ReliableConfiguration;

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
  public void onAckProcessed(ChannelHandlerContext ctx, SocketAddress from, int sequence) {
    if (DEBUG_RECEIVE) Log.debug(TAG, "onAckProcessed " + sequence);
  }

  @Override
  public void onPacketProcessed(ChannelHandlerContext ctx, SocketAddress from, int sequence, ByteBuf bb) {
    if (DEBUG_RECEIVE) Log.debug(TAG, "onPacketProcessed " + sequence + " " + bb);
    packetTransceiver.receivePacket(ctx, from, bb);
  }

  @Override
  public void reset() {
    packetController.reset();
  }

  @Override
  public void update(float delta, int channelId, DatagramChannel ch) {
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
