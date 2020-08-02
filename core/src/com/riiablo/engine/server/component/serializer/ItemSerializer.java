package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.codec.util.BitStream;
import com.riiablo.engine.server.component.Item;
import com.riiablo.net.packet.d2gs.ComponentP;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.ItemP;
import com.riiablo.util.BufferUtils;

public class ItemSerializer implements FlatBuffersSerializer<Item, ItemP> {
  public static final ItemP table = new ItemP();

  @Override
  public byte getDataType() {
    return ComponentP.ItemP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Item c) {
    int dataOffset = ItemP.createDataVector(builder, c.item.data());
    return ItemP.createItemP(builder, dataOffset);
  }

  @Override
  public ItemP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public Item getData(EntitySync sync, int j, Item c) {
    getTable(sync, j);
    byte[] bytes = BufferUtils.readRemaining(table.dataAsByteBuffer());
    BitStream bitStream = new BitStream(bytes);
//    bitStream.skip(D2S.ItemData.SECTION_HEADER_BITS);
    c.item = com.riiablo.item.Item.loadFromStream(bitStream);
    return c;
  }
}
