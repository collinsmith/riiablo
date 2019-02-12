package gdx.diablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import gdx.diablo.engine.component.WeaponClassComponent;

public class WeaponSystem extends IteratingSystem {
  private ComponentMapper<WeaponClassComponent> wc = ComponentMapper.getFor(WeaponClassComponent.class);

  public WeaponSystem() {
    super(Family.all(WeaponClassComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {

  }
}
