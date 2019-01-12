package gdx.diablo.codec.excel;

public class Obj extends Excel<Obj.Entry> {
  private static final int MAX_ACTS = 5;
  private static final int TYPE_SIZE[] = {60, 150};

  public static final int TYPE1 = 1;
  public static final int TYPE2 = 2;

  private int[][][] lookup = new int[MAX_ACTS + 1][TYPE_SIZE.length + 1][]; {
    for (int i = 1; i <= MAX_ACTS; i++) {
      for (int j = 1; j <= TYPE_SIZE.length; j++) {
        lookup[i][j] = new int[TYPE_SIZE[j - 1]];
      }
    }
  }

  @Override
  protected void put(int id, Entry value) {
    super.put(id, value);
    lookup[value.Act][value.Type][value.Id] = id;
  }

  public Entry get(int act, int type, int id) {
    return get(lookup[act][type][id]);
  }

  public String getType1(int act, int id) {
    return get(act, TYPE1, id).Description;
  }

  public int getType2(int act, int id) {
    return get(act, TYPE2, id).ObjectId;
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Description;
    }

    @Column
    @Key
    public String  Description;
    @Column public int    Act;
    @Column public int    Id;
    @Column public int    Type;
    @Column public int    ObjectId;
  }
}
