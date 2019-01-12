package gdx.diablo.command;

public interface Action {
  Action DO_NOTHING = new Action() {
    @Override
    public void onExecuted(Command.Instance instance) {}
  };

  void onExecuted(Command.Instance instance);
}
