package com.riiablo.excel2.txt;

import com.riiablo.excel2.Entry;
import com.riiablo.excel2.Excel;
import com.riiablo.excel2.PrimaryKey;
import com.riiablo.excel2.SerializedWith;
import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteOutput;

@Entry(MonStats.Entry.class)
@SerializedWith(MonStats.Serializer.class)
public class MonStats extends Excel<MonStats.Entry, MonStats.Serializer> {
  public MonStats() {
    super(Entry.class);
  }

  @Override
  public Entry newEntry() {
    return new Entry();
  }

  @Override
  public Serializer newSerializer() {
    return new Serializer();
  }

  public static class Entry extends Excel.Entry {
    @Column public String Id;
    @Column public int hcIdx;
  }

  public static class Serializer implements com.riiablo.excel2.Serializer<Entry> {
    @Override public void readBin(Entry entry, ByteInput in) {}
    @Override public void writeBin(Entry entry, ByteOutput out) {}
    @Override public boolean equals(Entry e1, Entry e2) { throw new UnsupportedOperationException(); }
    @Override public void logErrors(Entry e1, Entry e2) {}
  }
}
