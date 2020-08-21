package com.riiablo.engine.server;

import org.apache.logging.log4j.Logger;

import com.artemis.ComponentMapper;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Skills;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.MovementModes;
import com.riiablo.engine.server.component.Sequence;
import com.riiablo.log.LogManager;

public class Actioneer extends PassiveSystem {
  private static final Logger log = LogManager.getLogger(Actioneer.class);

  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<Sequence> mSequence;
  protected ComponentMapper<MovementModes> mMovementModes;

  public void cast(int entityId, int skillId) {
    if (mSequence.has(entityId)) return;
    final Skills.Entry skill = Riiablo.files.skills.get(skillId);
    log.trace("casting skill: {}", skill);

    byte mode = (byte) mClass.get(entityId).type.getMode(skill.anim);
    log.trace("mode: {}", mode);
    if (mode == -1) { // TODO: replace -1 with some const INVALID_SKILL
      mode = (byte) mClass.get(entityId).type.getMode("SC");
      log.trace("changed mode to {} because it was invalid", mode);
    }

    mSequence.create(entityId).sequence(mode, mMovementModes.get(entityId).NU);

    Riiablo.audio.play(skill.stsound, true);
  }
}
