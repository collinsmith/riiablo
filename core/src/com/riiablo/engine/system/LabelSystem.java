package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.riiablo.Riiablo;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.engine.component.HoveredComponent;
import com.riiablo.engine.component.LabelComponent;
import com.riiablo.engine.component.MonsterComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.graphics.PaletteIndexedColorDrawable;
import com.riiablo.widget.Label;

public class LabelSystem extends EntitySystem {
  private final ComponentMapper<LabelComponent> labelComponent = ComponentMapper.getFor(LabelComponent.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final Family labelFamily = Family.all(PositionComponent.class, LabelComponent.class, HoveredComponent.class).get();
  private ImmutableArray<Entity> labelledEntities;

  private final Family monsterFamily = Family.all(PositionComponent.class, HoveredComponent.class, MonsterComponent.class).exclude(LabelComponent.class).get();
  private ImmutableArray<Entity> monsterEntities;

  private final Vector2 tmpVec2 = new Vector2();
  private final IsometricCamera iso;

  private final Array<Actor> labels = new Array<>();
  private final MonsterLabel monsterLabel = new MonsterLabel();

  public LabelSystem(IsometricCamera iso) {
    super();
    this.iso = iso;
    setProcessing(false);
  }

  public Array<Actor> getLabels() {
    return labels;
  }

  public Actor getMonsterLabel() {
    return monsterLabel;
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    labelledEntities = engine.getEntitiesFor(labelFamily);
    monsterEntities = engine.getEntitiesFor(monsterFamily);
  }

  @Override
  public void removedFromEngine(Engine engine) {
    super.removedFromEngine(engine);
    labelledEntities = null;
    monsterEntities = null;
  }

  @Override
  public void update(float delta) {
    labels.clear();
    monsterLabel.setVisible(false);
    for (Entity entity : labelledEntities) updateLabel(entity);
    if (monsterEntities.size() > 0) updateMonster(monsterEntities.first());
  }

  private void updateLabel(Entity entity) {
    PositionComponent positionComponent = this.positionComponent.get(entity);
    iso.toScreen(tmpVec2.set(positionComponent.position));

    LabelComponent labelComponent = this.labelComponent.get(entity);
    tmpVec2.add(labelComponent.offset);

    Actor actor = labelComponent.actor;
    actor.setPosition(tmpVec2.x, tmpVec2.y, Align.center | Align.bottom);
    labels.add(actor);
  }

  private void updateMonster(Entity entity) {
    setMonsterLabel(entity);
  }

  public void setMonsterLabel(Entity entity) {
    monsterLabel.set(entity);
    monsterLabel.setVisible(true);
  }

  private static class MonsterLabel extends Table {
    static final float HORIZONTAL_PADDING = 16;
    static final float VERTICAL_PADDING = 2;

    final ComponentMapper<MonsterComponent> monsterComponent = ComponentMapper.getFor(MonsterComponent.class);

    Table label;
    Label name;
    Label type;
    StringBuilder typeBuilder = new StringBuilder(32);

    MonsterLabel() {
      label = new Table();
      label.setBackground(new PaletteIndexedColorDrawable(Riiablo.colors.darkRed) {{
        setTopHeight(VERTICAL_PADDING);
        setBottomHeight(VERTICAL_PADDING);
        setLeftWidth(HORIZONTAL_PADDING);
        setRightWidth(HORIZONTAL_PADDING);
      }});
      label.add(name = new Label(Riiablo.fonts.font16));
      label.pack();

      add(label).space(4).center().row();
      add(type = new Label(Riiablo.fonts.ReallyTheLastSucker)).row();
      pack();
    }

    void set(Entity entity) {
      MonStats.Entry monstats = monsterComponent.get(entity).monstats;
      name.setText(Riiablo.string.lookup(monstats.NameStr));
      typeBuilder.setLength(0);
      if (monstats.lUndead || monstats.hUndead) {
        typeBuilder.append(Riiablo.string.lookup("UndeadDescriptX")).append(' ');
      } else if (monstats.demon) {
        typeBuilder.append(Riiablo.string.lookup("DemonID")).append(' ');
      }

      if (!monstats.DescStr.isEmpty()) {
        typeBuilder.append(Riiablo.string.lookup(monstats.DescStr)).append(' ');
      }

      if (typeBuilder.length() > 0) typeBuilder.setLength(typeBuilder.length() - 1);
      type.setText(typeBuilder);
      //pack();
    }
  }
}
