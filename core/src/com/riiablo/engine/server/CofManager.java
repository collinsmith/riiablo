package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.client.component.CofDirtyComponents;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.event.AlphaChangeEvent;
import com.riiablo.engine.server.event.ModeChangeEvent;
import com.riiablo.engine.server.event.TransformChangeEvent;
import com.riiablo.engine.server.event.WClassChangeEvent;

import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.system.core.PassiveSystem;

public class CofManager extends PassiveSystem {
  protected ComponentMapper<CofReference> mCofReference;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofDirtyComponents> mCofDirtyComponents;
  protected ComponentMapper<CofAlphas> mCofAlphas;
  protected ComponentMapper<CofTransforms> mCofTransforms;

  protected EventSystem event;

  public void setMode(int id, byte mode) {
    setMode(id, mode, false);
  }

  public void setMode(int id, byte mode, boolean force) {
    CofReference reference = mCofReference.get(id);
    if (reference.mode == mode && !force) return;
    reference.mode = mode;
    event.dispatch(ModeChangeEvent.obtain(id, mode));
  }

  public void setWClass(int id, byte wclass) {
    setWClass(id, wclass, false);
  }

  public void setWClass(int id, byte wclass, boolean force) {
    CofReference reference = mCofReference.get(id);
    if (reference.wclass == wclass && !force) return;
    reference.wclass = wclass;
    event.dispatch(WClassChangeEvent.obtain(id, wclass));
  }

  public int setComponent(int id, int c, int code) {
    int[] component = mCofComponents.get(id).component;
    if (component[c] == code) return Dirty.NONE;
    component[c] = code;
    return mCofDirtyComponents.create(id).flags |= (1 << c);
  }

  public int setAlpha(int id, int c, float a) {
    float[] alpha = mCofAlphas.get(id).alpha;
    if (alpha[c] == a) return Dirty.NONE;
    alpha[c] = a;
    return 1 << c;
  }

  public void updateAlpha(int id, int flags) {
    if (flags == Dirty.NONE) return;
    event.dispatch(AlphaChangeEvent.obtain(id, flags));
  }

  public int setTransform(int id, int c, byte packedTransform) {
    byte[] transform = mCofTransforms.get(id).transform;
    if (transform[c] == packedTransform) return Dirty.NONE;
    transform[c] = packedTransform;
    return 1 << c;
  }

  public void updateTransform(int id, int flags) {
    if (flags == Dirty.NONE) return;
    event.dispatch(TransformChangeEvent.obtain(id, flags));
  }
}
