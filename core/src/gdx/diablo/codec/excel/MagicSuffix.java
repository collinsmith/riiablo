package gdx.diablo.codec.excel;

public class MagicSuffix extends Excel<MagicSuffix.Entry> {
  @Override
  protected int offset() {
    return 1;
  }

  public static class Entry extends MagicAffix {
    @Column public String  mod1code;
    @Column public int     mod1param;
    @Column public int     mod1min;
    @Column public int     mod1max;
    @Column public String  mod2code;
    @Column public int     mod2param;
    @Column public int     mod2min;
    @Column public int     mod2max;
    @Column public String  mod3code;
    @Column public int     mod3param;
    @Column public int     mod3min;
    @Column public int     mod3max;
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
  }
}
