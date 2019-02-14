package gdx.diablo.codec.excel;

public class MonStats2 extends Excel<MonStats2.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Id;
    }

    @Key
    @Column public String  Id;
    @Column public int     Height;
    @Column public int     OverlayHeight;
    @Column public int     pixHeight;
    @Column public int     SizeX;
    @Column public int     SizeY;
    @Column public int     spawnCol;
    @Column public int     MeleeRng;
    @Column public String  BaseW;
    @Column public int     HitClass;
    @Column(format = "%sv", endIndex = 16, values = {
        "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"
    })
    public String  ComponentV[];
    @Column(values = {
        "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"
    })
    public boolean Components[];
    @Column public int     TotalPieces;
    @Column(format = "m%s", endIndex = 16, values = {
        "DT", "NU", "WL", "GH", "A1", "A2", "BL", "SC", "S1", "S2", "S3", "S4", "DD", "KB", "SQ", "RN"
    })
    public boolean mMode[];
    @Column(format = "d%s", endIndex = 16, values = {
        "DT", "NU", "WL", "GH", "A1", "A2", "BL", "SC", "S1", "S2", "S3", "S4", "DD", "KB", "SQ", "RN"
    })
    public int     dMode[];
    @Column(format = "%smv", endIndex = 16, values = {
        "DT", "NU", "WL", "GH", "A1", "A2", "BL", "SC", "S1", "S2", "S3", "S4", "DD", "KB", "SQ", "RN"
    })
    public boolean Modemv[];
    //@Column public int     A1mv;
    //@Column public int     A2mv;
    //@Column public int     SCmv;
    //@Column public int     S1mv;
    //@Column public int     S2mv;
    //@Column public int     S3mv;
    //@Column public int     S4mv;
    @Column public boolean noGfxHitTest;
    @Column public int     htTop;
    @Column public int     htLeft;
    @Column public int     htWidth;
    @Column public int     htHeight;
    @Column public int     restore;
    @Column public int     automapCel;
    @Column public boolean noMap;
    @Column public boolean noOvly;
    @Column public boolean isSel;
    @Column public boolean alSel;
    @Column public boolean noSel;
    @Column public boolean shiftSel;
    @Column public boolean corpseSel;
    @Column public boolean isAtt;
    @Column public boolean revive;
    @Column public boolean critter;
    @Column public boolean small;
    @Column public boolean large;
    @Column public boolean soft;
    @Column public boolean inert;
    @Column public boolean objCol;
    @Column public boolean deadCol;
    @Column public boolean unflatDead;
    @Column public boolean Shadow;
    @Column public boolean noUniqueShift;
    @Column public boolean compositeDeath;
    @Column public int     localBlood;
    @Column public int     Bleed;
    @Column public int     Light;
    @Column(format = "light-%s", values = {"r", "g", "b"}, endIndex = 3)
    public int     light[];
    @Column(format = "Utrans%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     Utrans[];
    @Column public String  Heart;
    @Column public String  BodyPart;
    @Column public int     InfernoLen;
    @Column public int     InfernoAnim;
    @Column public int     InfernoRollback;
    @Column public String  ResurrectMode;
    @Column public String  ResurrectSkill;
  }
}
