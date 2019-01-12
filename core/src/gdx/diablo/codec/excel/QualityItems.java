package gdx.diablo.codec.excel;

import java.util.Arrays;

public class QualityItems extends Excel<QualityItems.Entry> {
  @Excel.Index
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      switch (nummods) {
        case 1:  return effect[0];
        default: return Arrays.toString(effect);
      }
    }

    @Column public int     nummods;
    @Column public String  mod1code;
    @Column public int     mod1param;
    @Column public int     mod1min;
    @Column public int     mod1max;
    @Column public String  mod2code;
    @Column public int     mod2param;
    @Column public int     mod2min;
    @Column public int     mod2max;
    @Column public int     ToHitMin;
    @Column public int     ToHitMax;
    @Column(format = "Dam%Min")
    public int     DamMin;
    @Column(format = "Dam%Max")
    public int     DamMax;
    @Column(format = "AC%Min")
    public int     ACMin;
    @Column(format = "AC%Max")
    public int     ACMax;
    @Column(format = "Dur%Min")
    public int     DurMin;
    @Column(format = "Dur%Max")
    public int     DurMax;
    @Column(startIndex = 1, endIndex = 3)
    public String  effect[];
    @Column public boolean armor;
    @Column public boolean weapon;
    @Column public boolean shield;
    @Column public boolean thrown;
    @Column public boolean scepter;
    @Column public boolean wand;
    @Column public boolean staff;
    @Column public boolean bow;
    @Column public boolean boots;
    @Column public boolean gloves;
    @Column public boolean belt;
    @Column public int     level;
    @Column public int     multiply;
    @Column public int     add;
  }
}
