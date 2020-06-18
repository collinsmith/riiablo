package com.riiablo.server.netty;

import io.netty.buffer.ByteBuf;
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

import com.riiablo.net.packet.netty.Header;
import com.riiablo.net.packet.netty.Netty;

public class ReliableChannelHandler implements ChannelHandler, ChannelInboundHandler, ChannelOutboundHandler {
  private static final String TAG = "ReliableChannelHandler";

  private final TypeParameterMatcher matcher;

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
    try {
      ByteBuffer buffer = in.nioBuffer();
      Packet packet = Packet.obtain(0, buffer);
      processHeader(ctx, packet.data.header());
      processPacket(ctx, packet.data);
    } finally {
//      in.release(); // Automatically released by channelRead() right now
    }
  }

  protected void processHeader(ChannelHandlerContext ctx, Header header) throws Exception {
    Gdx.app.log(TAG, "  " + String.format("SEQ:%d ACK:%d ACK_BITS:%08x", header.sequence(), header.ack(), header.ackBits()));
  }

  protected void processPacket(ChannelHandlerContext ctx, Netty netty) throws Exception {

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
