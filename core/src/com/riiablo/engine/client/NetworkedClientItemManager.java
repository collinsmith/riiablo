package com.riiablo.engine.client;

import com.google.flatbuffers.FlatBufferBuilder;

import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.StoreLoc;
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

import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class NetworkedClientItemManager extends ClientItemManager {
  private static final String TAG = "NetworkedClientItemManager";

  @Wire(name = "client.socket")
  protected Socket socket;

  private void wrapAndSend(FlatBufferBuilder builder, byte data_type, int dataOffset) {
    int root = D2GS.createD2GS(builder, data_type, dataOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, root);

    try {
      OutputStream out = socket.getOutputStream();
      WritableByteChannel channelOut = Channels.newChannel(out);
      channelOut.write(builder.dataBuffer());
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }
  }

  private FlatBufferBuilder obtainBuilder() {
    return new FlatBufferBuilder(0);
  }

  @Override
  public void groundToCursor(Item item) {
    FlatBufferBuilder builder = obtainBuilder();
    int dataOffset = GroundToCursor.createGroundToCursor(builder, 0);
    wrapAndSend(builder, D2GSData.GroundToCursor, dataOffset);
  }

  @Override
  public void cursorToGround() {
    FlatBufferBuilder builder = obtainBuilder();
    CursorToGround.startCursorToGround(builder);
    int dataOffset = CursorToGround.endCursorToGround(builder);
    wrapAndSend(builder, D2GSData.CursorToGround, dataOffset);
  }

  @Override
  public void storeToCursor(int i) {
    FlatBufferBuilder builder = obtainBuilder();
    int dataOffset = StoreToCursor.createStoreToCursor(builder, i);
    wrapAndSend(builder, D2GSData.StoreToCursor, dataOffset);
  }

  @Override
  public void cursorToStore(StoreLoc storeLoc, int x, int y) {
    FlatBufferBuilder builder = obtainBuilder();
    int dataOffset = CursorToStore.createCursorToStore(builder, storeLoc.ordinal(), x, y);
    wrapAndSend(builder, D2GSData.CursorToStore, dataOffset);
  }

  @Override
  public void swapStoreItem(int i, StoreLoc storeLoc, int x, int y) {
    FlatBufferBuilder builder = obtainBuilder();
    int dataOffset = SwapStoreItem.createSwapStoreItem(builder, i, storeLoc.ordinal(), x, y);
    wrapAndSend(builder, D2GSData.SwapStoreItem, dataOffset);
  }

  @Override
  public void bodyToCursor(BodyLoc bodyLoc, boolean merc) {
    FlatBufferBuilder builder = obtainBuilder();
    int dataOffset = BodyToCursor.createBodyToCursor(builder, bodyLoc.ordinal(), merc);
    wrapAndSend(builder, D2GSData.BodyToCursor, dataOffset);
  }

  @Override
  public void cursorToBody(BodyLoc bodyLoc, boolean merc) {
    FlatBufferBuilder builder = obtainBuilder();
    int dataOffset = CursorToBody.createCursorToBody(builder, bodyLoc.ordinal(), merc);
    wrapAndSend(builder, D2GSData.CursorToBody, dataOffset);
  }

  @Override
  public void swapBodyItem(BodyLoc bodyLoc, boolean merc) {
    FlatBufferBuilder builder = obtainBuilder();
    int dataOffset = SwapBodyItem.createSwapBodyItem(builder, bodyLoc.ordinal(), merc);
    wrapAndSend(builder, D2GSData.SwapBodyItem, dataOffset);
  }

  @Override
  public void beltToCursor(int i) {
    FlatBufferBuilder builder = obtainBuilder();
    int dataOffset = BeltToCursor.createBeltToCursor(builder, i);
    wrapAndSend(builder, D2GSData.BeltToCursor, dataOffset);
  }

  @Override
  public void cursorToBelt(int x, int y) {
    FlatBufferBuilder builder = obtainBuilder();
    int dataOffset = CursorToBelt.createCursorToBelt(builder, x, y);
    wrapAndSend(builder, D2GSData.CursorToBelt, dataOffset);
  }

  @Override
  public void swapBeltItem(int i) {
    FlatBufferBuilder builder = obtainBuilder();
    int dataOffset = SwapBeltItem.createSwapBeltItem(builder, i);
    wrapAndSend(builder, D2GSData.SwapBeltItem, dataOffset);
  }
}
