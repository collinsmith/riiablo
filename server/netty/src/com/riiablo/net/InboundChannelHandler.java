package com.riiablo.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;
import java.net.SocketAddress;

import com.badlogic.gdx.Gdx;

public class InboundChannelHandler<T> implements ChannelInboundHandler {
  private static final String TAG = "InboundChannelHandler";

  private static final boolean DEBUG = !true;
  private static final boolean DEBUG_CALLS = DEBUG && true;
  private static final boolean DEBUG_INBOUND = DEBUG && true;

  private final boolean autoRelease;
  private final TypeParameterMatcher matcher;
  private final Endpoint<T> endpoint;

  public InboundChannelHandler(Class<T> inboundMessageType, Endpoint<T> endpoint) {
    this(true, inboundMessageType, endpoint);
  }

  public InboundChannelHandler(boolean autoRelease, Class<T> inboundMessageType, Endpoint<T> endpoint) {
    this.autoRelease = autoRelease;
    this.endpoint = endpoint;
    this.matcher = TypeParameterMatcher.get(inboundMessageType);
  }

  private boolean accept(Object msg) throws Exception {
    return matcher.match(msg);
  }

  private void messageReceived(ChannelHandlerContext ctx, T msg) throws Exception {
    SocketAddress sender = endpoint.getSender(ctx, msg);
    if (DEBUG_CALLS) Gdx.app.debug(TAG, "messageReceived received packet from " + sender);
    if (DEBUG_INBOUND) {
      if (msg instanceof ByteBuf) {
        Gdx.app.debug(TAG, "  " + ByteBufUtil.hexDump((ByteBuf) msg));
      } else {
        Gdx.app.debug(TAG, "  " + msg);
      }
    }
    endpoint.processMessage(ctx, sender, msg);
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
      if (autoRelease && release) ReferenceCountUtil.release(msg);
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
}
