package com.riiablo.codec.excel;

@Excel.Binned
public class ItemStatCost extends Excel<ItemStatCost.Entry> {
  private static final boolean FIX_BYTIME_ENCODING = true; // changes bit counts
  private static final boolean FIX_NULL_OP_STAT    = true; // sets op_stat to non-null array if null

  @Override
  protected void put(int id, Entry value) {
    super.put(value.ID, value);
    if (FIX_BYTIME_ENCODING && value.Encode == 4 && value.Save_Bits == 22) {
      value.Save_Bits       = 20; // encoded value
      value.Save_Param_Bits = 2;  // time of day
    }
    if (FIX_NULL_OP_STAT && value.op_stat == null) {
      value.op_stat = new String[3];
    }
  }

  @Override
  protected void init() {
    put(new Entry() {{
      Stat = "reqstr";
      ID = com.riiablo.attributes.Stat.reqstr;
    }});
    put(new Entry() {{
      Stat = "reqdex";
      ID = com.riiablo.attributes.Stat.reqdex;
    }});
  }

  protected void put(Entry entry) {
    put(entry.ID, entry);
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Stat;
    }

    @Key
    @Column
    public String  Stat;
    @Column public int     ID;
    @Column(format = "Send Other")
    public boolean Send_Other;
    @Column public boolean Signed;
    @Column(format = "Send Bits")
    public int     Send_Bits;
    @Column(format = "Send Param Bits")
    public int     Send_Param_Bits;
    @Column public boolean UpdateAnimRate;
    @Column public boolean Saved;
    @Column public boolean CSvSigned;
    @Column public int     CSvBits;
    @Column public int     CSvParam;
    @Column public boolean fCallback;
    @Column public int     fMin;
    @Column public int     MinAccr;
    @Column public int     Encode;
    @Column public int     Add;
    @Column public int     Multiply;
    @Column public int     Divide;
    @Column public int     ValShift;
    @Column(format = "1.09-Save Bits")
    public int     Save_Bits_109;
    @Column(format = "1.09-Save Add")
    public int     Save_Add_109;
    @Column(format = "Save Bits")
    public int     Save_Bits;
    @Column(format = "Save Add")
    public int     Save_Add;
    @Column(format = "Save Param Bits")
    public int     Save_Param_Bits;
    @Column public boolean keepzero;
    @Column public int     op;
    @Column(format = "op param")
    public int     op_param;
    @Column(format = "op base")
    public String  op_base;
    @Column(format = "op stat%d", startIndex = 1, endIndex = 4)
    public String  op_stat[];
    @Column public boolean direct;
    @Column public String  maxstat;
    @Column public boolean itemspecific;
    @Column public String  damagerelated;
    @Column public String  itemevent1;
    @Column public int     itemeventfunc1;
    @Column public String  itemevent2;
    @Column public int     itemeventfunc2;
    @Column public int     descpriority;
    @Column public int     descfunc;
    @Column public int     descval;
    @Column public String  descstrpos;
    @Column public String  descstrneg;
    @Column public String  descstr2;
    @Column public int     dgrp;
    @Column public int     dgrpfunc;
    @Column public int     dgrpval;
    @Column public String  dgrpstrpos;
    @Column public String  dgrpstrneg;
    @Column public String  dgrpstr2;
    @Column public int     stuff;
  }
}
