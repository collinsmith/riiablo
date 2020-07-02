package com.riiablo.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.badlogic.gdx.Gdx;

public class OutboundChannelHandler implements ChannelOutboundHandler {
  private static final String TAG = "OutboundChannelHandler";

  private static final boolean DEBUG = !true;
  private static final boolean DEBUG_CALLS = DEBUG && true;
  private static final boolean DEBUG_OUTBOUND = DEBUG && true;

  public OutboundChannelHandler() {}

  private Object writeMessage(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
    if (DEBUG_CALLS) {
      InetSocketAddress receiver = (InetSocketAddress) ctx.channel().remoteAddress();
      Gdx.app.log(TAG, "writeMessage sending packet to " + receiver);
    }
    if (DEBUG_OUTBOUND) Gdx.app.debug(TAG, "  " + ByteBufUtil.hexDump(msg));
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
    Gdx.app.error(TAG, cause.getMessage(), cause);
    ctx.close();
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
