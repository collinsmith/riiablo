package com.riiablo.server.d2gs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;

import com.artemis.annotations.Wire;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntIntMap;

import com.riiablo.engine.Engine;
import com.riiablo.engine.server.ItemManager;
import com.riiablo.net.packet.d2gs.BeltToCursor;
import com.riiablo.net.packet.d2gs.BodyToCursor;
import com.riiablo.net.packet.d2gs.CursorToBelt;
import com.riiablo.net.packet.d2gs.CursorToBody;
import com.riiablo.net.packet.d2gs.CursorToGround;
import com.riiablo.net.packet.d2gs.CursorToStore;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.GroundToCursor;
import com.riiablo.net.packet.d2gs.StoreToCursor;
import com.riiablo.net.packet.d2gs.SwapBeltItem;
import com.riiablo.net.packet.d2gs.SwapBodyItem;
import com.riiablo.net.packet.d2gs.SwapStoreItem;
import com.riiablo.nnet.PacketProcessor;

public class D2GSPacketProcessor implements PacketProcessor {
  private static final String TAG = "D2GSPacketProcessor";

  @Wire(name = "outPackets")
  protected BlockingQueue<D2GSPacket> outPackets;

  @Wire(name = "player")
  protected IntIntMap player;

  protected ItemManager itemManager;
  protected NetworkSynchronizer sync;

  @Override
  public void processPacket(ChannelHandlerContext ctx, SocketAddress from, ByteBuf bb) {
    throw new UnsupportedOperationException();
  }

  public void processPacket(D2GSPacket packet) {
    if (!Server.ignoredPackets.get(packet.data.dataType())) Gdx.app.debug(TAG, "  " + "dataType=" + D2GSData.name(packet.data.dataType()));
    switch (packet.data.dataType()) {
      case D2GSData.EntitySync:
        Synchronize(packet);
        break;
      case D2GSData.GroundToCursor:
        GroundToCursor(packet);
        break;
      case D2GSData.CursorToGround:
        CursorToGround(packet);
        break;
      case D2GSData.StoreToCursor:
        StoreToCursor(packet);
        break;
      case D2GSData.CursorToStore:
        CursorToStore(packet);
        break;
      case D2GSData.SwapStoreItem:
        SwapStoreItem(packet);
        break;
      case D2GSData.BodyToCursor:
        BodyToCursor(packet);
        break;
      case D2GSData.CursorToBody:
        CursorToBody(packet);
        break;
      case D2GSData.SwapBodyItem:
        SwapBodyItem(packet);
        break;
      case D2GSData.BeltToCursor:
        BeltToCursor(packet);
        break;
      case D2GSData.CursorToBelt:
        CursorToBelt(packet);
        break;
      case D2GSData.SwapBeltItem:
        SwapBeltItem(packet);
        break;
      default:
        byte dataType = packet.data.dataType();
        String name = dataType < D2GSData.names.length ? D2GSData.name(dataType) : "null";
        Gdx.app.debug(TAG, String.format("unknown data type: %s (0x%02x)", name, dataType));
    }
  }

  private void Synchronize(D2GSPacket packet) {
    int entityId = player.get(packet.id, Engine.INVALID_ENTITY);
    assert entityId != Engine.INVALID_ENTITY;
    sync.sync(entityId, packet.data);
  }

  private int getPlayerEntityId(D2GSPacket packet) {
    int entityId = player.get(packet.id, Engine.INVALID_ENTITY);
    assert entityId != Engine.INVALID_ENTITY;
    return entityId;
  }

  private static ByteBuffer duplicate(ByteBuffer buffer) {
    final int size = buffer.rewind().remaining();
    byte[] bytes = new byte[size + 4];
    return (ByteBuffer) ByteBuffer.wrap(bytes)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putInt(size)
        .put(buffer)
        .rewind();
  }

  private void GroundToCursor(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    GroundToCursor groundToCursor = (GroundToCursor) packet.data.data(new GroundToCursor());
    itemManager.groundToCursor(entityId, groundToCursor.itemId());

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }

  private void CursorToGround(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    CursorToGround cursorToGround = (CursorToGround) packet.data.data(new CursorToGround());
    itemManager.cursorToGround(entityId);

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }

  private void StoreToCursor(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    StoreToCursor storeToCursor = (StoreToCursor) packet.data.data(new StoreToCursor());
    itemManager.storeToCursor(entityId, storeToCursor.itemId());

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }

  private void CursorToStore(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    CursorToStore cursorToStore = (CursorToStore) packet.data.data(new CursorToStore());
    itemManager.cursorToStore(entityId, cursorToStore.storeLoc(), cursorToStore.x(), cursorToStore.y());

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }

  private void SwapStoreItem(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    SwapStoreItem swapStoreItem = (SwapStoreItem) packet.data.data(new SwapStoreItem());
    itemManager.swapStoreItem(entityId, swapStoreItem.itemId(), swapStoreItem.storeLoc(), swapStoreItem.x(), swapStoreItem.y());

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }

  private void BodyToCursor(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    BodyToCursor bodyToCursor = (BodyToCursor) packet.data.data(new BodyToCursor());
    itemManager.bodyToCursor(entityId, bodyToCursor.bodyLoc(), bodyToCursor.merc());

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }

  private void CursorToBody(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    CursorToBody cursorToBody = (CursorToBody) packet.data.data(new CursorToBody());
    itemManager.cursorToBody(entityId, cursorToBody.bodyLoc(), cursorToBody.merc());

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }

  private void SwapBodyItem(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    SwapBodyItem swapBodyItem = (SwapBodyItem) packet.data.data(new SwapBodyItem());
    itemManager.swapBodyItem(entityId, swapBodyItem.bodyLoc(), swapBodyItem.merc());

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }

  private void BeltToCursor(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    BeltToCursor beltToCursor = (BeltToCursor) packet.data.data(new BeltToCursor());
    itemManager.beltToCursor(entityId, beltToCursor.itemId());

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }

  private void CursorToBelt(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    CursorToBelt cursorToBelt = (CursorToBelt) packet.data.data(new CursorToBelt());
    itemManager.cursorToBelt(entityId, cursorToBelt.x(), cursorToBelt.y());

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }

  private void SwapBeltItem(D2GSPacket packet) {
    int entityId = getPlayerEntityId(packet);
    SwapBeltItem swapBeltItem = (SwapBeltItem) packet.data.data(new SwapBeltItem());
    itemManager.swapBeltItem(entityId, swapBeltItem.itemId());

    packet = D2GSPacket.obtain(packet.id, packet.dataType, duplicate(packet.buffer));
    packet.id = (1 << packet.id);
    outPackets.offer(packet);
  }
}
