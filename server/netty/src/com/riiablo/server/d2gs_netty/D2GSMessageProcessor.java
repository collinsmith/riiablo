package com.riiablo.server.d2gs_netty;

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
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.GroundToCursor;
import com.riiablo.net.packet.d2gs.StoreToCursor;
import com.riiablo.net.packet.d2gs.SwapBeltItem;
import com.riiablo.net.packet.d2gs.SwapBodyItem;
import com.riiablo.net.packet.d2gs.SwapStoreItem;
import com.riiablo.net.tcp.D2GSOutboundPacketFactory;
import com.riiablo.net.tcp.InboundPacket;
import com.riiablo.net.tcp.OutboundPacket;

public class D2GSMessageProcessor {
  private static final String TAG = "D2GSMessageProcessor";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_RECEIVED_PACKETS = DEBUG && true;

  @Wire(name = "outPackets")
  protected BlockingQueue<OutboundPacket> outPackets;

  @Wire(name = "player")
  protected IntIntMap player;

  protected ItemManager itemManager;
  protected NetworkSynchronizer sync;

  public void processPacket(InboundPacket<D2GS> packet) {
    if (DEBUG_RECEIVED_PACKETS && !Server.ignoredPackets.get(packet.dataType())) Gdx.app.debug(TAG, "Processing " + packet);
    switch (packet.dataType()) {
      case D2GSData.EntitySync:
        onSynchronize(packet);
        break;
      case D2GSData.GroundToCursor:
        onGroundToCursor(packet);
        break;
      case D2GSData.CursorToGround:
        onCursorToGround(packet);
        break;
      case D2GSData.StoreToCursor:
        onStoreToCursor(packet);
        break;
      case D2GSData.CursorToStore:
        onCursorToStore(packet);
        break;
      case D2GSData.SwapStoreItem:
        onSwapStoreItem(packet);
        break;
      case D2GSData.BodyToCursor:
        onBodyToCursor(packet);
        break;
      case D2GSData.CursorToBody:
        onCursorToBody(packet);
        break;
      case D2GSData.SwapBodyItem:
        onSwapBodyItem(packet);
        break;
      case D2GSData.BeltToCursor:
        onBeltToCursor(packet);
        break;
      case D2GSData.CursorToBelt:
        onCursorToBelt(packet);
        break;
      case D2GSData.SwapBeltItem:
        onSwapBeltItem(packet);
        break;
      default:
        String name = 0 <= packet.dataType() && packet.dataType() < D2GSData.names.length ? packet.dataTypeName() : "unknown";
        Gdx.app.debug(TAG, String.format("Unknown data type: %s (0x%02x)", name, packet.dataType()));
    }
  }

  private int getPlayerEntityId(InboundPacket<D2GS> packet) {
    int entityId = player.get(packet.id(), Engine.INVALID_ENTITY);
    assert entityId != Engine.INVALID_ENTITY;
    return entityId;
  }
  
  private void onSynchronize(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    sync.sync(entityId, packet.table());
  }

  private static ByteBuffer duplicate(ByteBuffer buffer) {
    final int size = buffer.rewind().remaining();
    return (ByteBuffer) ByteBuffer.wrap(new byte[size + 4])
        .order(ByteOrder.LITTLE_ENDIAN)
        .putInt(size)
        .put(buffer)
        .rewind();
  }

  /**
   * TODO: Implement support for efficiently echoing inbound packet by retaining message contents
   *       until the OutboundPacket is sent.
   * @see OutboundPacket#echo(InboundPacket)
   */
  private static OutboundPacket echo(InboundPacket<D2GS> packet) {
    ByteBuffer duplicate = duplicate(packet.buffer());
    return D2GSOutboundPacketFactory.obtain(packet.flag(), packet.dataType(), duplicate);
  }
  
  private void onGroundToCursor(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    GroundToCursor groundToCursor = (GroundToCursor) packet.table().data(new GroundToCursor());
    itemManager.groundToCursor(entityId, groundToCursor.itemId());
    outPackets.offer(echo(packet));
  }

  private void onCursorToGround(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    CursorToGround cursorToGround = (CursorToGround) packet.table().data(new CursorToGround());
    itemManager.cursorToGround(entityId);
    outPackets.offer(echo(packet));
  }

  private void onStoreToCursor(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    StoreToCursor storeToCursor = (StoreToCursor) packet.table().data(new StoreToCursor());
    itemManager.storeToCursor(entityId, storeToCursor.itemId());
    outPackets.offer(echo(packet));
  }

  private void onCursorToStore(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    CursorToStore cursorToStore = (CursorToStore) packet.table().data(new CursorToStore());
    itemManager.cursorToStore(entityId, cursorToStore.storeLoc(), cursorToStore.x(), cursorToStore.y());
    outPackets.offer(echo(packet));
  }

  private void onSwapStoreItem(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    SwapStoreItem swapStoreItem = (SwapStoreItem) packet.table().data(new SwapStoreItem());
    itemManager.swapStoreItem(entityId, swapStoreItem.itemId(), swapStoreItem.storeLoc(), swapStoreItem.x(), swapStoreItem.y());
    outPackets.offer(echo(packet));
  }

  private void onBodyToCursor(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    BodyToCursor bodyToCursor = (BodyToCursor) packet.table().data(new BodyToCursor());
    itemManager.bodyToCursor(entityId, bodyToCursor.bodyLoc(), bodyToCursor.merc());
    outPackets.offer(echo(packet));
  }

  private void onCursorToBody(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    CursorToBody cursorToBody = (CursorToBody) packet.table().data(new CursorToBody());
    itemManager.cursorToBody(entityId, cursorToBody.bodyLoc(), cursorToBody.merc());
    outPackets.offer(echo(packet));
  }

  private void onSwapBodyItem(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    SwapBodyItem swapBodyItem = (SwapBodyItem) packet.table().data(new SwapBodyItem());
    itemManager.swapBodyItem(entityId, swapBodyItem.bodyLoc(), swapBodyItem.merc());
    outPackets.offer(echo(packet));
  }

  private void onBeltToCursor(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    BeltToCursor beltToCursor = (BeltToCursor) packet.table().data(new BeltToCursor());
    itemManager.beltToCursor(entityId, beltToCursor.itemId());
    outPackets.offer(echo(packet));
  }

  private void onCursorToBelt(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    CursorToBelt cursorToBelt = (CursorToBelt) packet.table().data(new CursorToBelt());
    itemManager.cursorToBelt(entityId, cursorToBelt.x(), cursorToBelt.y());
    outPackets.offer(echo(packet));
  }

  private void onSwapBeltItem(InboundPacket<D2GS> packet) {
    int entityId = getPlayerEntityId(packet);
    SwapBeltItem swapBeltItem = (SwapBeltItem) packet.table().data(new SwapBeltItem());
    itemManager.swapBeltItem(entityId, swapBeltItem.itemId());
    outPackets.offer(echo(packet));
  }
}
