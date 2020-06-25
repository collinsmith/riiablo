package com.riiablo.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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

import com.badlogic.gdx.Gdx;

public class EndpointedChannelHandler<T> implements ChannelHandler, ChannelInboundHandler, ChannelOutboundHandler {
  private static final String TAG = "EndpointedChannelHandler";

  private static final boolean DEBUG          = true;
  private static final boolean DEBUG_CALLS    = DEBUG && true;
  private static final boolean DEBUG_INBOUND  = DEBUG && true;
  private static final boolean DEBUG_OUTBOUND = DEBUG && true;

  private final TypeParameterMatcher matcher;
  private final Endpoint<T> endpoint;

  public EndpointedChannelHandler(Class<T> packetType, Endpoint<T> endpoint) {
    this.endpoint = endpoint;
    matcher = TypeParameterMatcher.get(packetType);
  }

  protected boolean accept(Object msg) throws Exception {
    return matcher.match(msg);
  }

  protected void messageReceived(ChannelHandlerContext ctx, T msg) throws Exception {
    if (DEBUG_CALLS) {
      InetSocketAddress sender = msg instanceof DatagramPacket
          ? ((DatagramPacket) msg).sender()
          : (InetSocketAddress) ctx.channel().remoteAddress();
      Gdx.app.log(TAG, "messageReceived received packet from " + sender.getHostName() + ":" + sender.getPort());
    }
    endpoint.messageReceived(ctx, msg);
  }

  protected Object writeMessage(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
    if (DEBUG_CALLS) {
      InetSocketAddress receiver = (InetSocketAddress) ctx.channel().remoteAddress();
      Gdx.app.log(TAG, "writeMessage sending packet to " + receiver.getHostName() + ":" + receiver.getPort());
    }
    ByteBuf out = msg;
    if (DEBUG_OUTBOUND) Gdx.app.debug(TAG, "  " + ByteBufUtil.hexDump(out));
    return msg;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "handlerAdded");
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "handlerRemoved");
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "exceptionCaught");
    ctx.fireExceptionCaught(cause);
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "channelRegistered");
    ctx.fireChannelRegistered();
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "channelUnregistered");
    ctx.fireChannelUnregistered();
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "channelActive");
    ctx.fireChannelActive();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "channelInactive");
    ctx.fireChannelInactive();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "channelRead");
    boolean release = true;
    try {
      if (accept(msg)) {
        @SuppressWarnings("unchecked")
        T castedMsg = (T) msg;
        messageReceived(ctx, castedMsg);
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
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "channelReadComplete");
    ctx.fireChannelReadComplete();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "userEventTriggered");
    ctx.fireUserEventTriggered(evt);
  }

  @Override
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "channelWritabilityChanged");
    ctx.fireChannelWritabilityChanged();
  }

  @Override
  public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "bind");
    ctx.bind(localAddress, promise);
  }

  @Override
  public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "connect");
    ctx.connect(remoteAddress, localAddress, promise);
  }

  @Override
  public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "disconnect");
    ctx.disconnect(promise);
  }

  @Override
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "close");
    ctx.close(promise);
  }

  @Override
  public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "deregister");
    ctx.deregister(promise);
  }

  @Override
  public void read(ChannelHandlerContext ctx) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "read");
    ctx.read();
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "write");
    msg = writeMessage(ctx, (ByteBuf) msg);
    ctx.write(msg, promise);
  }

  @Override
  public void flush(ChannelHandlerContext ctx) throws Exception {
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "flush");
    ctx.flush();
  }
}
