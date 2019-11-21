package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.riiablo.engine.EntityAdapter;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.ItemComponent;
import com.riiablo.engine.component.MonsterComponent;
import com.riiablo.engine.component.ObjectComponent;
import com.riiablo.engine.component.SelectableComponent;
import com.riiablo.engine.component.WarpComponent;

public class SelectableSystem extends EntitySystem {
  private final ComponentMapper<ObjectComponent> objectComponent = ComponentMapper.getFor(ObjectComponent.class);
  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final Family objectFamily = Family.all(ObjectComponent.class, CofComponent.class).get();
  private ImmutableArray<Entity> objectEntities;

  private final Family monsterFamily = Family.all(MonsterComponent.class).get();
  private final EntityListener monsterListener = new EntityAdapter() {
    private final ComponentMapper<MonsterComponent> monsterComponent = ComponentMapper.getFor(MonsterComponent.class);

    @Override
    public void entityAdded(Entity entity) {
      MonsterComponent monsterComponent = this.monsterComponent.get(entity);
      setSelectable(entity, monsterComponent.monstats2.isSel);
    }
  };

  private final Family itemFamily = Family.all(ItemComponent.class).get();
  private final EntityListener itemListener = new EntityAdapter() {
    @Override
    public void entityAdded(Entity entity) {
      setSelectable(entity, true);
    }
  };

  private final Family warpFamily = Family.all(WarpComponent.class).get();
  private final EntityListener warpListener = new EntityAdapter() {
    @Override
    public void entityAdded(Entity entity) {
      setSelectable(entity, true);
    }
  };

  private final ComponentMapper<SelectableComponent> selectableComponent = ComponentMapper.getFor(SelectableComponent.class);

  public SelectableSystem() {
    super();
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    objectEntities = engine.getEntitiesFor(objectFamily);
    engine.addEntityListener(monsterFamily, monsterListener);
    engine.addEntityListener(warpFamily, warpListener);
    engine.addEntityListener(itemFamily, itemListener);
  }

  @Override
  public void removedFromEngine(Engine engine) {
    super.removedFromEngine(engine);
    objectEntities = null;
    engine.removeEntityListener(monsterListener);
    engine.removeEntityListener(warpListener);
    engine.removeEntityListener(itemListener);
  }

  @Override
  public void update(float delta) {
    super.update(delta);
    for (int i = 0, size = objectEntities.size(); i < size; i++) {
      Entity entity = objectEntities.get(i);
      ObjectComponent objectComponent = this.objectComponent.get(entity);
      CofComponent cofComponent = this.cofComponent.get(entity);
      setSelectable(entity, objectComponent.base.Selectable[cofComponent.mode]);
    }
  }

  private void setSelectable(Entity entity, boolean b) {
    if (b) {
      if (!selectableComponent.has(entity)) entity.add(getEngine().createComponent(SelectableComponent.class));
    } else {
      entity.remove(SelectableComponent.class);
    }
  }
}
