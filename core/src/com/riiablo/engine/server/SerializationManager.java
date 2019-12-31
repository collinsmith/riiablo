package com.riiablo.engine.server;

import com.google.flatbuffers.FlatBufferBuilder;

import com.artemis.Component;
import com.artemis.ComponentManager;
import com.artemis.ComponentMapper;
import com.artemis.utils.Bag;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.DS1ObjectWrapper;
import com.riiablo.engine.server.component.Item;
import com.riiablo.engine.server.component.Monster;
import com.riiablo.engine.server.component.Player;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.engine.server.component.Warp;
import com.riiablo.engine.server.component.serializer.AngleSerializer;
import com.riiablo.engine.server.component.serializer.CofAlphasSerializer;
import com.riiablo.engine.server.component.serializer.CofComponentsSerializer;
import com.riiablo.engine.server.component.serializer.CofTransformsSerializer;
import com.riiablo.engine.server.component.serializer.DS1ObjectWrapperSerializer;
import com.riiablo.engine.server.component.serializer.FlatBuffersSerializer;
import com.riiablo.engine.server.component.serializer.ItemSerializer;
import com.riiablo.engine.server.component.serializer.MonsterSerializer;
import com.riiablo.engine.server.component.serializer.PlayerSerializer;
import com.riiablo.engine.server.component.serializer.PositionSerializer;
import com.riiablo.engine.server.component.serializer.VelocitySerializer;
import com.riiablo.engine.server.component.serializer.WarpSerializer;
import com.riiablo.net.packet.d2gs.CofAlphasP;
import com.riiablo.net.packet.d2gs.CofComponentsP;
import com.riiablo.net.packet.d2gs.CofTransformsP;
import com.riiablo.net.packet.d2gs.ComponentP;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.EntitySync;

import net.mostlyoriginal.api.system.core.PassiveSystem;

public class SerializationManager extends PassiveSystem {
  private static final String TAG = "SerializationManager";
  private static final boolean DEBUG             = true;
  private static final boolean DEBUG_DESERIALIZE = DEBUG && true;

  private static final int INITIAL_SIZE = 64;
  public final Bag<Component> components = new Bag<>(INITIAL_SIZE);
  public final ByteArray dataType = new ByteArray(INITIAL_SIZE);
  public final IntArray data = new IntArray(INITIAL_SIZE);

  private ObjectMap<Class<? extends Component>, FlatBuffersSerializer> serializers;
  private Class<? extends Component>[] deserializers;
  private final EntitySync sync = new EntitySync();

  protected ComponentMapper<com.riiablo.engine.server.component.Class> mClass;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofTransforms> mCofTransforms;
  protected ComponentMapper<CofAlphas> mCofAlphas;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<DS1ObjectWrapper> mDS1ObjectWrapper;
  protected ComponentMapper[] cm;

  protected ComponentManager componentManager;
  protected CofManager cofs;

  @Override
  @SuppressWarnings("unchecked")
  protected void initialize() {
    serializers = new ObjectMap<>();
//    serializers.put(com.riiablo.engine.server.component.Class.class, new ClassSerializer());
    serializers.put(CofComponents.class, new CofComponentsSerializer());
    serializers.put(CofTransforms.class, new CofTransformsSerializer());
    serializers.put(CofAlphas.class, new CofAlphasSerializer());
    serializers.put(Position.class, new PositionSerializer());
    serializers.put(Velocity.class, new VelocitySerializer());
    serializers.put(Angle.class, new AngleSerializer());
    serializers.put(Player.class, new PlayerSerializer());
    serializers.put(DS1ObjectWrapper.class, new DS1ObjectWrapperSerializer());
    serializers.put(Warp.class, new WarpSerializer());
    serializers.put(Monster.class, new MonsterSerializer());
    serializers.put(Item.class, new ItemSerializer());

    deserializers = (Class<? extends Component>[]) new Class[ComponentP.names.length];
//    deserializers[SyncData.ClassP] = com.riiablo.engine.server.component.Class.class;
    deserializers[ComponentP.CofComponentsP] = CofComponents.class;
    deserializers[ComponentP.CofTransformsP] = CofTransforms.class;
    deserializers[ComponentP.CofAlphasP] = CofAlphas.class;
    deserializers[ComponentP.PositionP] = Position.class;
    deserializers[ComponentP.VelocityP] = Velocity.class;
    deserializers[ComponentP.AngleP] = Angle.class;
    deserializers[ComponentP.PlayerP] = Player.class;
    deserializers[ComponentP.DS1ObjectWrapperP] = DS1ObjectWrapper.class;
    deserializers[ComponentP.WarpP] = Warp.class;
    deserializers[ComponentP.MonsterP] = Monster.class;
    deserializers[ComponentP.ItemP] = Item.class;

    cm = new ComponentMapper[ComponentP.names.length];
    cm[ComponentP.ClassP] = null; //mClass;
    cm[ComponentP.CofComponentsP] = null;
    cm[ComponentP.CofTransformsP] = null;
    cm[ComponentP.CofAlphasP] = null;
    cm[ComponentP.PositionP] = mPosition;
    cm[ComponentP.VelocityP] = mVelocity;
    cm[ComponentP.AngleP] = mAngle;
    cm[ComponentP.PlayerP] = null;
    cm[ComponentP.DS1ObjectWrapperP] = mDS1ObjectWrapper;
    cm[ComponentP.WarpP] = null;
    cm[ComponentP.MonsterP] = null;
    cm[ComponentP.ItemP] = null;
  }

  @SuppressWarnings("unchecked")
  public int serialize(FlatBufferBuilder builder, int entityId) {
    dataType.clear();
    data.clear();
    components.clear();

    int type = mClass.get(entityId).type.ordinal();
    componentManager.getComponentsFor(entityId, components);
    for (Component c : components) {
      FlatBuffersSerializer serializer = serializers.get(c.getClass());
      if (serializer == null) continue;
      dataType.add(serializer.getDataType());
      data.add(serializer.putData(builder, c));
    }

    assert dataType.size == data.size;

    final int dataTypeSize = dataType.size;
    final byte[] dataType = this.dataType.items;
    EntitySync.startComponentTypeVector(builder, dataTypeSize);
    for (int i = 0; i < dataTypeSize; i++) builder.addByte(dataType[i]);
    int dataTypeOffset = builder.endVector();

    final int dataSize = data.size;
    final int[] data = this.data.items;
    EntitySync.startComponentVector(builder, dataSize);
    for (int i = 0; i < dataSize; i++) builder.addOffset(data[i]);
    int dataOffset = builder.endVector();

    return EntitySync.createEntitySync(builder, entityId, type, dataTypeOffset, dataOffset);
  }

  public void deserialize(int entityId, D2GS packet) {
    packet.data(sync);
    deserialize(entityId, sync);
  }

  @SuppressWarnings("unchecked")
  private void deserialize(int entityId, EntitySync sync) {
    int tFlags = Dirty.NONE;
    int aFlags = Dirty.NONE;

    byte dataType;
    //int entityId = sync.entityId(); // FIXME: use something like this for client-side (resolve id)
    for (int i = 0, len = sync.componentLength(); i < len; i++) {
      switch (dataType = sync.componentType(i)) {
        case ComponentP.CofComponentsP: {
          Class<? extends Component> clazz = deserializers[dataType];
          CofComponentsSerializer serializer = (CofComponentsSerializer) serializers.get(clazz);
          CofComponentsP table = serializer.getTable(sync, i);
          for (int j = 0, s = table.componentLength(); j < s; j++) {
            cofs.setComponent(entityId, j, (byte) table.component(j));
          }
//          if (DEBUG_DESERIALIZE) Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(ArrayUtils.toByteArray(mCofComponents.get(entityId).component)));
          break;
        }
        case ComponentP.CofTransformsP: {
          Class<? extends Component> clazz = deserializers[dataType];
          CofTransformsSerializer serializer = (CofTransformsSerializer) serializers.get(clazz);
          CofTransformsP table = serializer.getTable(sync, i);
          for (int j = 0, s = table.transformLength(); j < s; j++) {
            tFlags |= cofs.setTransform(entityId, j, (byte) table.transform(j));
          }
//          if (DEBUG_DESERIALIZE) Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(mCofTransforms.get(entityId).transform));
          break;
        }
        case ComponentP.CofAlphasP: {
          Class<? extends Component> clazz = deserializers[dataType];
          CofAlphasSerializer serializer = (CofAlphasSerializer) serializers.get(clazz);
          CofAlphasP table = serializer.getTable(sync, i);
          for (int j = 0, s = table.alphaLength(); j < s; j++) {
            aFlags |= cofs.setAlpha(entityId, j, table.alpha(j) / 255f);
          }
//          if (DEBUG_DESERIALIZE) Gdx.app.log(TAG, "  " + Arrays.toString(mCofAlphas.get(entityId).alpha));
          break;
        }
        case ComponentP.ClassP:
        case ComponentP.PlayerP:
        case ComponentP.DS1ObjectWrapperP:
        case ComponentP.WarpP:
        case ComponentP.MonsterP:
        case ComponentP.ItemP:
          break;
        default: {
          Class<? extends Component> clazz = deserializers[dataType];
          FlatBuffersSerializer serializer = serializers.get(clazz);
          serializer.getData(sync, i, cm[dataType].get(entityId));
          break;
        }
      }
    }

    cofs.updateTransform(entityId, tFlags);
    cofs.updateAlpha(entityId, aFlags);
  }
}
