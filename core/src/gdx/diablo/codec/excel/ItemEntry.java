package gdx.diablo.codec.excel;

public class ItemEntry extends Excel.Entry {
  @Override
  public String toString() {
    return name;
  }

  @Key
  @Column
  public String  code;

  @Column public String  name;
  @Column public int     version;
  @Column public String  alternateGfx;
  @Column public String  type;
  @Column public String  type2;
  @Column public int     component;
  @Column public String  invfile;
  @Column public String  uniqueinvfile;
  @Column public String  setinvfile;
  @Column public int     Transform;
  @Column public int     InvTrans;
  @Column public int     invwidth;
  @Column public int     invheight;
  @Column public String  dropsound;
  @Column public boolean stackable;
  @Column public boolean useable;
  @Column public String  usesound;
}
