// automatically generated by the FlatBuffers compiler, do not modify

package com.riiablo.net.packet.d2gs;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class EntitySync extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_1_12_0(); }
  public static EntitySync getRootAsEntitySync(ByteBuffer _bb) { return getRootAsEntitySync(_bb, new EntitySync()); }
  public static EntitySync getRootAsEntitySync(ByteBuffer _bb, EntitySync obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public EntitySync __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int entityId() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int type() { int o = __offset(6); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0; }
  public int flags() { int o = __offset(8); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0; }
  public byte componentType(int j) { int o = __offset(10); return o != 0 ? bb.get(__vector(o) + j * 1) : 0; }
  public int componentTypeLength() { int o = __offset(10); return o != 0 ? __vector_len(o) : 0; }
  public ByteVector componentTypeVector() { return componentTypeVector(new ByteVector()); }
  public ByteVector componentTypeVector(ByteVector obj) { int o = __offset(10); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer componentTypeAsByteBuffer() { return __vector_as_bytebuffer(10, 1); }
  public ByteBuffer componentTypeInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 10, 1); }
  public Table component(Table obj, int j) { int o = __offset(12); return o != 0 ? __union(obj, __vector(o) + j * 4) : null; }
  public int componentLength() { int o = __offset(12); return o != 0 ? __vector_len(o) : 0; }
  public UnionVector componentVector() { return componentVector(new UnionVector()); }
  public UnionVector componentVector(UnionVector obj) { int o = __offset(12); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }

  public static int createEntitySync(FlatBufferBuilder builder,
      int entityId,
      int type,
      int flags,
      int component_typeOffset,
      int componentOffset) {
    builder.startTable(5);
    EntitySync.addComponent(builder, componentOffset);
    EntitySync.addComponentType(builder, component_typeOffset);
    EntitySync.addEntityId(builder, entityId);
    EntitySync.addFlags(builder, flags);
    EntitySync.addType(builder, type);
    return EntitySync.endEntitySync(builder);
  }

  public static void startEntitySync(FlatBufferBuilder builder) { builder.startTable(5); }
  public static void addEntityId(FlatBufferBuilder builder, int entityId) { builder.addInt(0, entityId, 0); }
  public static void addType(FlatBufferBuilder builder, int type) { builder.addByte(1, (byte)type, (byte)0); }
  public static void addFlags(FlatBufferBuilder builder, int flags) { builder.addByte(2, (byte)flags, (byte)0); }
  public static void addComponentType(FlatBufferBuilder builder, int componentTypeOffset) { builder.addOffset(3, componentTypeOffset, 0); }
  public static int createComponentTypeVector(FlatBufferBuilder builder, byte[] data) { builder.startVector(1, data.length, 1); for (int i = data.length - 1; i >= 0; i--) builder.addByte(data[i]); return builder.endVector(); }
  public static void startComponentTypeVector(FlatBufferBuilder builder, int numElems) { builder.startVector(1, numElems, 1); }
  public static void addComponent(FlatBufferBuilder builder, int componentOffset) { builder.addOffset(4, componentOffset, 0); }
  public static int createComponentVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startComponentVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endEntitySync(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public EntitySync get(int j) { return get(new EntitySync(), j); }
    public EntitySync get(EntitySync obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

