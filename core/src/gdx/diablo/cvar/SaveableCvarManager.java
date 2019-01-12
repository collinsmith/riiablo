package gdx.diablo.cvar;

import com.google.common.base.Throwables;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gdx.diablo.serializer.BooleanStringSerializer;
import gdx.diablo.serializer.ByteStringSerializer;
import gdx.diablo.serializer.CharacterStringSerializer;
import gdx.diablo.serializer.DoubleStringSerializer;
import gdx.diablo.serializer.FloatStringSerializer;
import gdx.diablo.serializer.IntegerStringSerializer;
import gdx.diablo.serializer.LongStringSerializer;
import gdx.diablo.serializer.SerializeException;
import gdx.diablo.serializer.ShortStringSerializer;
import gdx.diablo.serializer.StringSerializer;
import gdx.diablo.serializer.StringStringSerializer;

public abstract class SaveableCvarManager extends CvarManager {

  private final Map<Class, StringSerializer> SERIALIZERS;

  private boolean autosave;

  public SaveableCvarManager() {
    this(true);
  }

  public SaveableCvarManager(boolean autosave) {
    this.autosave = autosave;
    SERIALIZERS = new ConcurrentHashMap<>();
    SERIALIZERS.put(Character.class, CharacterStringSerializer.INSTANCE);
    SERIALIZERS.put(Boolean.class, BooleanStringSerializer.INSTANCE);
    SERIALIZERS.put(Byte.class, ByteStringSerializer.INSTANCE);
    SERIALIZERS.put(Short.class, ShortStringSerializer.INSTANCE);
    SERIALIZERS.put(Integer.class, IntegerStringSerializer.INSTANCE);
    SERIALIZERS.put(Long.class, LongStringSerializer.INSTANCE);
    SERIALIZERS.put(Float.class, FloatStringSerializer.INSTANCE);
    SERIALIZERS.put(Double.class, DoubleStringSerializer.INSTANCE);
    SERIALIZERS.put(String.class, StringStringSerializer.INSTANCE);
  }

  public boolean isAutosaving() {
    return autosave;
  }

  public void setAutosave(boolean b) {
    if (b != autosave) {
      autosave = b;
      if (b) saveAll();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean add(@NonNull Cvar cvar) {
    try {
      Object value = load(cvar);
      cvar.set(value);
    } catch (Throwable t) {
      Throwables.propagateIfPossible(t, SerializeException.class);
      throw new SerializeException(t);
    } finally {
      return super.add(cvar);
    }
  }

  public abstract <T> T    load(Cvar<T> cvar);
  public abstract <T> void save(Cvar<T> cvar);

  public Collection<Throwable> saveAll() {
    Collection<Throwable> throwables = null;
    for (Cvar cvar : this) {
      try {
        save(cvar);
      } catch (Throwable t) {
        if (throwables == null) throwables = new ArrayList<>();
        throwables.add(t);
      }
    }

    return throwables != null ? throwables : Collections.<Throwable>emptyList();
  }

  @Override
  public void onChanged(Cvar cvar, Object from, Object to) {
    if (autosave) save(cvar);
  }

  @SuppressWarnings("unchecked")
  public <T> StringSerializer<T> getSerializer(Class<T> type) {
    if (type == null) return null;
    return SERIALIZERS.get(type);
  }

  @SuppressWarnings("unchecked")
  public <T> StringSerializer<T> getSerializer(Cvar<T> cvar) {
    if (cvar == null) return null;
    return SERIALIZERS.get(cvar.TYPE);
  }

  public void putSerializer(Class type, StringSerializer serializer) {
    if (serializer == null) {
      SERIALIZERS.remove(type);
      return;
    }

    SERIALIZERS.put(type, serializer);
  }

  public void removeSerializer(Class type) {
    SERIALIZERS.remove(type);
  }
}
