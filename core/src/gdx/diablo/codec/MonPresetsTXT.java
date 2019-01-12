package gdx.diablo.codec;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.math.NumberUtils;

public class MonPresetsTXT extends TXT {

  private static final int MAX_ACTS = 5;

  public final int ActColumnId;
  public final int PlaceColumnId;

  private final int[] offsets;
  private final int[] sizes;

  public MonPresetsTXT(TXT txt) {
    super(txt);
    ActColumnId = getColumnId("Act");
    PlaceColumnId = getColumnId("Place");
    offsets = new int[MAX_ACTS];
    sizes   = new int[MAX_ACTS];

    int act = 0, tmp;
    int rows = getRows();
    for (int i = 0; i < rows; i++) {
      tmp = NumberUtils.toInt(super.getString(i, ActColumnId));
      assert tmp > 0 : "Couldn't parse tmp into a valid act ID";
      if (tmp > act) {
        act = tmp;
      }

      sizes[act - 1]++;
    }

    int total = 0;
    for (int i = 0; i < MAX_ACTS; i++) {
      offsets[i] = total;
      total += sizes[i];
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("offsets", offsets)
        .append("sizes", sizes)
        .build();
  }

  public String get(int act, int row) {
    return getString(act, row);
  }

  @Override
  public String getString(int act, int row) {
    act -= 1;
    if (row < sizes[act]) {
      row += offsets[act];
      return super.getString(row, PlaceColumnId);
    }

    return null;
  }
}
