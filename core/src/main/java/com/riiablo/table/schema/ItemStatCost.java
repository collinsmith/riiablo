package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;
import com.riiablo.table.annotation.Table;
import com.riiablo.table.table.ItemStatCostTable;

@Schema
@Table(ItemStatCostTable.class)
@SuppressWarnings("unused")
public class ItemStatCost {
  @Override
  public String toString() {
    return Stat;
  }

  @PrimaryKey
  public String Stat;

  public int ID;

  @Format(format = "Send Other")
  public boolean Send_Other;

  public boolean Signed;

  @Format(format = "Send Bits")
  public int Send_Bits;

  @Format(format = "Send Param Bits")
  public int Send_Param_Bits;

  public boolean UpdateAnimRate;
  public boolean Saved;
  public boolean CSvSigned;
  public int CSvBits;
  public int CSvParam;
  public boolean fCallback;
  public int fMin;
  public int MinAccr;
  public int Encode;
  public int Add;
  public int Multiply;
  public int Divide;
  public int ValShift;

  @Format(format = "1.09-Save Bits")
  public int Save_Bits_109;

  @Format(format = "1.09-Save Add")
  public int Save_Add_109;

  @Format(format = "Save Bits")
  public int Save_Bits;

  @Format(format = "Save Add")
  public int Save_Add;

  @Format(format = "Save Param Bits")
  public int Save_Param_Bits;

  public boolean keepzero;
  public int op;

  @Format(format = "op param")
  public int op_param;

  @Format(format = "op base")
  public String op_base;

  @Format(format = "op stat%d",
      startIndex = 1,
      endIndex = 4)
  public String op_stat[];

  public boolean direct;
  public String maxstat;
  public boolean itemspecific;
  public String damagerelated;
  public String itemevent1;
  public int itemeventfunc1;
  public String itemevent2;
  public int itemeventfunc2;
  public int descpriority;
  public int descfunc;
  public int descval;
  public String descstrpos;
  public String descstrneg;
  public String descstr2;
  public int dgrp;
  public int dgrpfunc;
  public int dgrpval;
  public String dgrpstrpos;
  public String dgrpstrneg;
  public String dgrpstr2;
  public int stuff;
}
