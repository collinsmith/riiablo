package gdx.diablo.codec.excel;

public class RarePrefix extends Excel<RarePrefix.Entry> {
  @Override
  protected int offset() {
    // TODO: Should really point to RareSuffix.size()
    return 156;
  }

  public static class Entry extends RareAffix {
    @Column public String  itype1;
    @Column public String  itype2;
    @Column public String  itype3;
    @Column public String  itype4;
    @Column public String  itype5;
    @Column public String  itype6;
    @Column public String  itype7;
    @Column public String  etype1;
    @Column public String  etype2;
    @Column public String  etype3;
    @Column public String  etype4;
  }
}
