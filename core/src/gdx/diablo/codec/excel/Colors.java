package gdx.diablo.codec.excel;

public class Colors extends Excel<Colors.Entry>{
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Transform_Color;
    }

    @Column(format = "Transform Color")
    public String Transform_Color;

    @Column
    @Key
    public String Code;
  }
}
