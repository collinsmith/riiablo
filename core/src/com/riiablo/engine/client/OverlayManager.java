package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;

import com.badlogic.gdx.assets.AssetDescriptor;

import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC;
import com.riiablo.codec.excel.Overlay;
import com.riiablo.graphics.BlendMode;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

@All(com.riiablo.engine.client.component.Overlay.class)
public class OverlayManager extends IteratingSystem {
  private static final Logger log = LogManager.getLogger(OverlayManager.class);

  protected ComponentMapper<com.riiablo.engine.client.component.Overlay> mOverlay;

  @Override
  protected void process(int entityId) {
    com.riiablo.engine.client.component.Overlay overlay = mOverlay.get(entityId);
    if (!Riiablo.assets.isLoaded(overlay.assetDescriptor)) return;

    Animation animation = overlay.animation;
    if (!overlay.isLoaded) {
      DC dc = Riiablo.assets.get(overlay.assetDescriptor);
      animation.edit()
          .layer(dc, overlay.entry.Trans == 3 ? BlendMode.LUMINOSITY : BlendMode.ID)
          .build();
      animation.setMode(Animation.Mode.ONCE);
      // FIXME: set frame to elapsed time since creation
      overlay.isLoaded = true;
      log.debug("Loaded {}", overlay.assetDescriptor.fileName);
    }

    if (animation.isFinished()) {
      dispose(overlay);
      mOverlay.remove(entityId);
    }
  }

  public void set(int entityId, String overlayId) {
    if (mOverlay.has(entityId)) {
      dispose(mOverlay.get(entityId));
    }

    Overlay.Entry overlay = Riiablo.files.Overlay.get(overlayId);
    com.riiablo.engine.client.component.Overlay overlayC = mOverlay.create(entityId).set(overlay);
    Riiablo.assets.load(overlayC.assetDescriptor);
  }

  void dispose(com.riiablo.engine.client.component.Overlay overlay) {
    AssetDescriptor assetDescriptor = overlay.assetDescriptor;
    Riiablo.assets.unload(assetDescriptor.fileName);
    log.debug("Unloaded {}", assetDescriptor.fileName);
  }
}
