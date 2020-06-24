package com.riiablo.net.reliable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

import com.riiablo.net.reliable.channel.ReliableMessageChannel;
import com.riiablo.net.reliable.channel.UnreliableMessageChannel;
import com.riiablo.net.reliable.channel.UnreliableOrderedMessageChannel;
import com.riiablo.util.EnumIntMap;

public class ReliableEndpoint implements MessageChannel.PacketTransceiver {
  private static final String TAG = "ReliableEndpoint";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_QOS = DEBUG && true;
  private static final boolean DEBUG_CHANNEL = DEBUG && true;
  private static final boolean DEBUG_SEND = DEBUG && true;
  private static final boolean DEBUG_RECEIVE = DEBUG && true;

  private final DatagramChannel channel;
  private final PacketProcessor packetProcessor;

  private final EnumIntMap<QoS> defaultChannels;
  private final MessageChannel[] channels;

  public ReliableEndpoint(DatagramChannel channel, PacketProcessor packetProcessor) {
    this.channel = channel;
    this.packetProcessor = packetProcessor;

    // for my purposes 3 works, channelId can be up to 255 though
    channels = new MessageChannel[3];
    channels[QoS.Reliable.ordinal()] = new ReliableMessageChannel(this);
    channels[QoS.Unreliable.ordinal()] = new UnreliableMessageChannel(this);
    channels[QoS.UnreliableOrdered.ordinal()] = new UnreliableOrderedMessageChannel(this);

    defaultChannels = new EnumIntMap<>(QoS.class, -1);
    for (QoS qos : QoS.values()) {
      defaultChannels.put(qos, qos.ordinal());
    }
  }

  public InetSocketAddress remoteAddress() {
    return channel.remoteAddress();
  }

  public void reset() {
    for (MessageChannel mc : channels) if (mc != null) mc.reset();
  }

  public void update(float delta) {
    for (MessageChannel mc : channels) if (mc != null) mc.update(delta, channel);
  }

  public void sendMessage(QoS qos, ByteBuffer bb) {
    if (DEBUG_SEND) Log.debug(TAG, "sendMessage");
    if (DEBUG_QOS) Log.debug(TAG, "sending message with %s QoS (0x%02x)", qos, qos.ordinal());
    int channelId = defaultChannels.get(qos);
    sendMessage(channelId, bb);
  }

  public void sendMessage(int channelId, ByteBuffer bb) {
    if (DEBUG_SEND) Log.debug(TAG, "sendMessage");
    Validate.inclusiveBetween(0x00, 0xFF, channelId, "channelId must fit within a ubyte");
    if (DEBUG_CHANNEL) Log.debug(TAG, "sending message with on channel %d", channelId);
    MessageChannel mc = channels[channelId];
    mc.sendMessage(channelId, channel, Unpooled.wrappedBuffer(bb)); // automatically released
  }

  public void messageReceived(ChannelHandlerContext ctx, DatagramPacket packet) {
    if (DEBUG_RECEIVE) Log.debug(TAG, "onMessageReceived");
    int channelId = Packet.getChannelId(packet.content());
    if (DEBUG_QOS) {
      QoS qos = QoS.valueOf(channelId);
      if (qos != null) {
        Log.debug(TAG, "received message with %s QoS (0x%02x)", qos, channelId);
      } else {
        Log.debug(TAG, "received message with channel %d", channelId);
      }
    } else if (DEBUG_CHANNEL) {
      Log.debug(TAG, "received message with channel %d", channelId);
    }

    MessageChannel mc = channels[channelId];
    mc.onMessageReceived(ctx, packet);
  }

  @Override
  public void sendPacket(ByteBuf bb) {

  }

  @Override
  public void receivePacket(ByteBuf bb) {
    packetProcessor.processPacket(bb);
  }

  public interface PacketProcessor {
    void processPacket(ByteBuf bb);
  }

  public static final Stats stats = new Stats();

  public static class Stats {
    public int NUM_PACKETS_SENT;
    public int NUM_PACKETS_RECEIVED;
    public int NUM_PACKETS_ACKED;
    public int NUM_PACKETS_STALE;
    public int NUM_PACKETS_INVALID;
    public int NUM_PACKETS_TOO_LARGE_TO_SEND;
    public int NUM_PACKETS_TOO_LARGE_TO_RECEIVE;
    public int NUM_FRAGMENTS_SENT;
    public int NUM_FRAGMENTS_RECEIVED;
    public int NUM_FRAGMENTS_INVALID;
    public int NUM_ACKS_SENT;
    public int NUM_ACKS_RECEIVED;
    public int NUM_ACKS_INVALID;
  }
}
