package com.gmail.collinsmith70.diablo.scene;

import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.diablo.Client;

public class Hud extends WidgetGroup implements Disposable {

  private final Client CLIENT;

  public Hud(Client client) {
    this.CLIENT = client;
  }

  public Client getClient() {
    return CLIENT;
  }

  @Override
  public void dispose() {

  }

}
