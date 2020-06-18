package com.riiablo.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import java.net.SocketAddress;

import com.badlogic.gdx.Gdx;

public class ReliableOutboundHandler implements ChannelOutboundHandler {
  private static final String TAG = "ReliableOutboundHandler";

  ReliableOutboundHandler() {}

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
    Gdx.app.debug(TAG, "channelRegistered");
    ctx.fireExceptionCaught(cause);
  }
}
