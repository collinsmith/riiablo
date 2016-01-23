package com.gmail.collinsmith70.diablo.scene;

import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.gmail.collinsmith70.diablo.Client;
import com.google.common.base.Preconditions;

public class HudedScene extends WidgetGroup {

private final Client CLIENT;
private final Hud HUD;

public HudedScene(Client client) {
    this.CLIENT = Preconditions.checkNotNull(client, "Client cannot be null");
    this.HUD = new Hud();
    addActor(HUD);
}

public Client getClient() { return CLIENT; }
public Hud getHud() { return HUD; }

}
