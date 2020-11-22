// automatically generated by the FlatBuffers compiler, do not modify

package com.riiablo.net.packet.msi;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class MSI extends Table {
  public static MSI getRootAsMSI(ByteBuffer _bb) { return getRootAsMSI(_bb, new MSI()); }
  public static MSI getRootAsMSI(ByteBuffer _bb, MSI obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; vtable_start = bb_pos - bb.getInt(bb_pos); vtable_size = bb.getShort(vtable_start); }
  public MSI __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public byte dataType() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public Table data(Table obj) { int o = __offset(6); return o != 0 ? __union(obj, o) : null; }

  public static int createMSI(FlatBufferBuilder builder,
      byte data_type,
      int dataOffset) {
    builder.startObject(2);
    MSI.addData(builder, dataOffset);
    MSI.addDataType(builder, data_type);
    return MSI.endMSI(builder);
  }

  public static void startMSI(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addDataType(FlatBufferBuilder builder, byte dataType) { builder.addByte(0, dataType, 0); }
  public static void addData(FlatBufferBuilder builder, int dataOffset) { builder.addOffset(1, dataOffset, 0); }
  public static int endMSI(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishMSIBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
  public static void finishSizePrefixedMSIBuffer(FlatBufferBuilder builder, int offset) { builder.finishSizePrefixed(offset); }
}
