package com.riiablo.table.schema;

import com.riiablo.table.annotation.Schema;
import com.riiablo.table.annotation.Table;
import com.riiablo.table.table.ObjTable;

@Schema(
    indexed = true
)
@Table(ObjTable.class)
@SuppressWarnings("unused")
public class Obj {
  @Override
  public String toString() {
    return Description;
  }

  public int Act;
  public int Id;
  public String Description;
  public int ObjectId;
}
