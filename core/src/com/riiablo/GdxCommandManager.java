package com.riiablo;

import com.badlogic.gdx.Gdx;

import com.riiablo.command.Command;
import com.riiablo.command.CommandManager;

public class GdxCommandManager extends CommandManager {
  private static final String TAG = "GdxCommandManager";

  public GdxCommandManager() {}

  @Override
  public void onAssigned(Command command, String alias) {
    super.onAssigned(command, alias);
    Gdx.app.debug(TAG, "assigning \"" + alias.toLowerCase() + "\" to " + command);
  }

  @Override
  public void onUnassigned(Command command, String alias) {
    super.onUnassigned(command, alias);
    Gdx.app.debug(TAG, "unassigning " + alias + " from " + command);
  }
}
