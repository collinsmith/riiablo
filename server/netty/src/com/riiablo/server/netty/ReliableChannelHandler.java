package com.riiablo.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;

import com.riiablo.net.packet.netty.Netty;

public class ReliableChannelHandler implements ChannelHandler, ChannelInboundHandler, ChannelOutboundHandler {
  private static final String TAG = "ReliableChannelHandler";

  private static final boolean DEBUG          = true;
  private static final boolean DEBUG_SEQ      = DEBUG && true;
  private static final boolean DEBUG_OUTBOUND = DEBUG && true;
  private static final boolean DEBUG_INBOUND  = DEBUG && true;

  private final TypeParameterMatcher matcher;

  private static final int PROTOCOL = 0;

  int seq = -1; // 0xFFFFFFFF
  int ack = -1; // 0xFFFFFFFF
  int ack_bits = 0;

  ReliableChannelHandler() {
    matcher = TypeParameterMatcher.get(DatagramPacket.class);
  }

  protected boolean accept(Object msg) throws Exception {
    return matcher.match(msg);
  }

  protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
    InetSocketAddress sender = msg.sender();
    Gdx.app.log(TAG, "channelRead0 Packet from " + sender.getHostName() + ":" + sender.getPort());
    ByteBuf in = msg.content();
    if (DEBUG_INBOUND) Gdx.app.log(TAG, "  " + ByteBufUtil.hexDump(in));
    try {
      boolean valid = processHeader(ctx, in);
      if (!valid) return;
      ByteBuf content = Packet.Single.getContent(in);
      if (DEBUG_INBOUND) Gdx.app.log(TAG, "  " + ByteBufUtil.hexDump(content));
      ByteBuffer buffer = content.nioBuffer();
      PacketTuple packet = PacketTuple.obtain(0, buffer);
      processPacket(ctx, packet.data);
    } finally {
//      in.release(); // Automatically released by channelRead() right now
    }
  }

  protected boolean processHeader(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    int remoteProtocol = Packet.getProtocol(in);
    if (remoteProtocol != PROTOCOL) {
      Gdx.app.log(TAG, "  rejected incoming PROTO:" + remoteProtocol);
      return false;
    }

    int type = Packet.getType(in);
    if (DEBUG_INBOUND) {
      ByteBuf header = null;
      switch (type) {
        case Packet.SINGLE:     header = Packet.Single.getHeader(in); break;
        case Packet.FRAGMENTED: header = Packet.Fragmented.getHeader(in); break;
        case Packet.SLICED:     header = Packet.Sliced.getHeader(in); break;
        case Packet.SLICEDACK:  header = Packet.SlicedAck.getHeader(in); break;
        default:
          Gdx.app.log(TAG, "  rejected incoming TYPE:" + type);
          return false;
      }
      Gdx.app.log(TAG, "  " + ByteBufUtil.hexDump(header));
    }

    switch (type) {
      case Packet.SINGLE: {
        int remoteSeq = Packet.getSEQ(in);

        Gdx.app.log(TAG, "  accepted incoming " + Packet.Single.toString(in));
        if (ack < 0) {
          ack = remoteSeq;
          Gdx.app.log(TAG, "  init ack=" + ack);
        } else if (sequenceGreater(remoteSeq, ack)) {
          int shift = difference(remoteSeq, ack);
          Gdx.app.log(TAG, "  remoteSeq=" + remoteSeq + "; ack=" + ack + "; shift=" + shift);
          ack_bits <<= shift;
          ack_bits |= (1 << (shift - 1));
          ack = remoteSeq;
        } else {
          int diff = difference(ack, remoteSeq);
          Gdx.app.log(TAG, "  diff=" + diff);
          if (diff <= Integer.SIZE) {
            ack_bits |= (1 << (diff - 1));
          }
        }

        Gdx.app.log(TAG, "  " + String.format("ACK:%d ACK_BITS:%08x", ack, ack_bits));
        break;
      }
      default:
        throw new AssertionError();
    }
    return true;
  }

  protected static boolean sequenceGreater(int a, int b) {
    return ((a > b) && (a - b <= Short.MAX_VALUE))
        || ((a < b) && (b - a >  Short.MAX_VALUE));
  }

  protected static int difference(int a, int b) {
    assert sequenceGreater(a, b) : a + "<" +  b;
    if ((a > b) && (a - b <= Short.MAX_VALUE)) {
      return a - b;
    } else {
      return a + 0xFFFF - b + 1;
    }
  }

  protected void processPacket(ChannelHandlerContext ctx, Netty netty) throws Exception {

  }

  protected int nextSequence() {
    return seq = (seq + 1) & 0xFFFF;
  }

  protected Object channelWrite0(ChannelHandlerContext ctx, Object msg) throws Exception {
    InetSocketAddress receiver = (InetSocketAddress) ctx.channel().remoteAddress();
    Gdx.app.log(TAG, "channelWrite0 Packet to " + receiver.getHostName() + ":" + receiver.getPort());

    ByteBuf header = ctx.alloc().buffer(); // TODO: worth sizing this correctly?
    Packet.Single.createHeader(header, PROTOCOL, nextSequence(), ack, ack_bits);
    if (DEBUG_SEQ) Gdx.app.log(TAG, "  " + Packet.Single.toString(header));
    if (DEBUG_OUTBOUND) Gdx.app.log(TAG, "  " + ByteBufUtil.hexDump(header));

    ByteBuf content = (ByteBuf) msg;
    Packet.Single.setContentSize(header, content.readableBytes());

    CompositeByteBuf composite = ctx.alloc().compositeBuffer(2)
        .addComponent(true, header)
        .addComponent(true, content);

    if (DEBUG_OUTBOUND) Gdx.app.log(TAG, "  " + ByteBufUtil.hexDump(content));
    if (DEBUG_OUTBOUND) Gdx.app.log(TAG, "  " + ByteBufUtil.hexDump(composite));

    return composite;
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    Gdx.app.debug(TAG, "channelRegistered");
    ctx.fireChannelRegistered();
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    Gdx.app.debug(TAG, "channelUnregistered");
    ctx.fireChannelUnregistered();
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    Gdx.app.debug(TAG, "channelActive");
    ctx.fireChannelActive();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    Gdx.app.debug(TAG, "channelInactive");
    ctx.fireChannelInactive();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    Gdx.app.debug(TAG, "channelRead");
    boolean release = true;
    try {
      if (accept(msg)) {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        channelRead0(ctx, datagramPacket);
      } else {
        release = false;
        ctx.fireChannelRead(msg);
      }
    } finally {
      if (release) ReferenceCountUtil.release(msg);
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    Gdx.app.debug(TAG, "channelReadComplete");
    ctx.fireChannelReadComplete();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    Gdx.app.debug(TAG, "userEventTriggered");
    ctx.fireUserEventTriggered(evt);
  }

  @Override
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    Gdx.app.debug(TAG, "channelWritabilityChanged");
    ctx.fireChannelWritabilityChanged();
  }

  @Override
  public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    Gdx.app.debug(TAG, "bind");
    ctx.bind(localAddress, promise);
  }

  @Override
  public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    Gdx.app.debug(TAG, "connect");
    ctx.connect(remoteAddress, localAddress, promise);
  }

  @Override
  public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    Gdx.app.debug(TAG, "disconnect");
    ctx.disconnect(promise);
  }

  @Override
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    Gdx.app.debug(TAG, "close");
    ctx.close(promise);
  }

  @Override
  public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    Gdx.app.debug(TAG, "deregister");
    ctx.deregister(promise);
  }

  @Override
  public void read(ChannelHandlerContext ctx) throws Exception {
    Gdx.app.debug(TAG, "read");
    ctx.read();
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    Gdx.app.debug(TAG, "write");
    msg = channelWrite0(ctx, msg);
    ctx.write(msg, promise);
  }

  @Override
  public void flush(ChannelHandlerContext ctx) throws Exception {
    Gdx.app.debug(TAG, "flush");
    ctx.flush();
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    Gdx.app.debug(TAG, "handlerAdded");
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    Gdx.app.debug(TAG, "handlerRemoved");
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    Gdx.app.debug(TAG, "exceptionCaught");
    ctx.fireExceptionCaught(cause);
  }
}
