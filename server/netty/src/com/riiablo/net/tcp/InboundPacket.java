package com.riiablo.net.tcp;

import com.google.flatbuffers.Table;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.IllegalReferenceCountException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.TimeUtils;

public abstract class InboundPacket<T extends Table> implements ByteBufHolder {
  public static final int INVALID_CLIENT = -1;

  private final Class<T> tableType;
  private final ChannelHandlerContext ctx;
  private final InetSocketAddress sender;
  private final ByteBuf data;
  private final long time;

  protected final ByteBuffer buffer;
  protected T table;

  private int id;

  InboundPacket(Class<T> tableType, ChannelHandlerContext ctx, InetSocketAddress sender, ByteBuf message) {
    this.tableType = tableType;
    this.ctx = ctx;
    this.sender = sender;
    this.data = message;
    this.buffer = message.nioBuffer();
    this.id = INVALID_CLIENT;
    this.time = TimeUtils.millis();
  }

  public int id() {
    return id;
  }

  public void setId(int id) {
    assert this.id == INVALID_CLIENT : "id should be effectively final";
    this.id = id;
  }

  public int flag() {
    assert 0 <= id && id < Integer.SIZE : "id must be within the range [0,32) to be encodable as a flag";
    return 1 << id;
  }

  public long time() {
    return time;
  }

  public ChannelHandlerContext ctx() {
    return ctx;
  }

  public InetSocketAddress sender() {
    return sender;
  }

  public ByteBuffer buffer() {
    return buffer;
  }

  public T table() {
    return table;
  }

  public abstract byte dataType();

  public abstract String dataTypeName();

  @Override
  public String toString() {
    return toString("INVALID_CLIENT");
  }

  public String toString(String invalidString) {
    return String.format("%s (0x%02x) from %s (%s)", dataTypeName(), dataType() & 0xFF, sender(), id() == INVALID_CLIENT ? invalidString : Integer.toString(id()));
  }

  @Override
  public ByteBuf content() {
    if (data.refCnt() <= 0) throw new IllegalReferenceCountException(data.refCnt());
    return data;
  }

  @Override
  public InboundPacket<T> copy() {
    return replace(data.copy());
  }

  @Override
  public InboundPacket<T> duplicate() {
    return replace(data.duplicate());
  }

  @Override
  public InboundPacket<T> retainedDuplicate() {
    return replace(data.retainedDuplicate());
  }

  @Override
  public abstract InboundPacket<T> replace(ByteBuf content);

  @Override
  public int refCnt() {
    return data.refCnt();
  }

  @Override
  public InboundPacket<T> retain() {
    data.retain();
    return this;
  }

  @Override
  public InboundPacket<T> retain(int increment) {
    data.retain(increment);
    return this;
  }

  @Override
  public InboundPacket<T> touch() {
    data.touch();
    return this;
  }

  @Override
  public InboundPacket<T> touch(Object hint) {
    data.touch(hint);
    return this;
  }

  @Override
  public boolean release() {
    return data.release();
  }

  @Override
  public boolean release(int decrement) {
    return data.release(decrement);
  }
}
