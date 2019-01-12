package gdx.diablo.codec.excel;

public class PlrType extends Excel<PlrType.Entry>{
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Name;
    }

    @Column
    public String Name;

    @Column
    @Key
    public String Token;
  }
}
