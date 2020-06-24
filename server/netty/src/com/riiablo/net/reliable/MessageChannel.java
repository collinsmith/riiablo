package com.riiablo.net.reliable;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

public abstract class MessageChannel implements ReliablePacketController.PacketListener {
  protected final PacketTransceiver packetTransceiver;
  protected final ReliableConfiguration config;
  protected final ReliablePacketController packetController;

  protected int sequence;

  public MessageChannel(ReliableConfiguration config, PacketTransceiver packetTransceiver) {
    this.packetTransceiver = packetTransceiver;
    this.config = config;
    this.packetController = new ReliablePacketController(config, this);
  }

  public abstract void reset();
  public abstract void update(float delta, DatagramChannel ch);
  public abstract void sendMessage(int channelId, DatagramChannel ch, ByteBuf bb);
  public abstract void onMessageReceived(ChannelHandlerContext ctx, DatagramPacket packet);

  public interface PacketTransceiver {
    void sendPacket(ByteBuf bb);
    void receivePacket(ByteBuf bb);
  }
}
