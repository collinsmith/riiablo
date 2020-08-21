package com.riiablo.engine.client;

import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Skills;
import com.riiablo.engine.server.event.SkillCastEvent;

public class SkillCastHandler extends PassiveSystem {
  protected OverlayManager overlays;

  @Subscribe
  public void onSkillCast(SkillCastEvent event) {
    final Skills.Entry skill = Riiablo.files.skills.get(event.skillId);

    Riiablo.audio.play(skill.stsound, true);

    if (!skill.castoverlay.isEmpty()) {
      overlays.set(event.entityId, skill.castoverlay);
    }
  }

}
