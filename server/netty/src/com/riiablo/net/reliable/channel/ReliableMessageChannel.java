package com.riiablo.net.reliable.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

import com.riiablo.net.reliable.MessageChannel;
import com.riiablo.net.reliable.ReliableConfiguration;

public class ReliableMessageChannel extends MessageChannel {
  private static final String TAG = "ReliableMessageChannel";

  public ReliableMessageChannel(PacketTransceiver packetTransceiver) {
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

  }

  @Override
  public void onMessageReceived(ChannelHandlerContext ctx, DatagramPacket packet) {

  }

  @Override
  public void onPacketTransmitted(ByteBuf bb) {

  }

  @Override
  public void onPacketProcessed(int sequence, ByteBuf bb) {

  }
}
