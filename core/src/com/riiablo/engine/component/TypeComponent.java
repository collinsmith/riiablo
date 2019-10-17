package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.Riiablo;
import com.riiablo.codec.COFD2;

public class TypeComponent implements Component, Pool.Poolable {
  public enum Type {
    OBJ("OBJECTS",
        new String[] {"NU", "OP", "ON", "S1", "S2", "S3", "S4", "S5"},
        new String[] {"NIL", "LIT"}),
    MON("MONSTERS",
        new String[] {
            "DT", "NU", "WL", "GH", "A1", "A2", "BL", "SC", "S1", "S2", "S3", "S4", "DD", "GH",
            "XX", "RN"
        },
        new String[] {
            "NIL", "LIT", "MED", "HEV", "HVY", "DES", "BRV", "AXE", "FLA", "HAX", "MAC", "SCM",
            "BUC", "LRG", "KIT", "SML", "LSD", "WND", "SSD", "CLB", "TCH", "BTX", "HAL", "LAX",
            "MAU", "SCY", "WHM", "WHP", "JAV", "OPL", "GPL", "SBW", "LBW", "LBB", "SBB", "PIK",
            "SPR", "TRI", "FLC", "SBR", "GLV", "PAX", "BSD", "FLB", "WAX", "WSC", "WSD", "CLM",
            "SMC", "FIR", "LHT", "CLD", "POS", "RSP", "LSP", "UNH", "RSG", "BLD", "SHR", "LHR",
            "HBD", "TKT", "BAB", "PHA", "FAN", "PON", "HD1", "HD2", "HD3", "HD4", "ZZ1", "ZZ2",
            "ZZ3", "ZZ4", "ZZ5", "ZZ6", "ZZ7", "RED", "TH2", "TH3", "TH4", "TH5", "FBL", "FSP",
            "YNG", "OLD", "BRD", "GOT", "FEZ", "ROL", "BSK", "BUK", "SAK", "BAN", "FSH", "SNK",
            "BRN", "BLK", "SRT", "LNG", "DLN", "BTP", "MTP", "STP", "SVT", "COL", "HOD", "HRN",
            "LNK", "TUR", "MLK", "FHM", "GHM", "BHN", "HED",
        }),
    PLR("CHARS",
        new String[] {
            "DT", "NU", "WL", "RN", "GH", "TN", "TW", "A1", "A2", "BL", "SC", "TH", "KK", "S1",
            "S2", "S3", "S4", "DD", "GH", "GH"
        },
        new String[] {
            "NIL", "LIT", "MED", "HVY", "HAX", "AXE", "LAX", "BTX", "GIX", "WND", "YWN", "BWN",
            "CLB", "MAC", "WHM", "FLA", "MAU", "SSD", "SCM", "FLC", "CRS", "BSD", "LSD", "CLM",
            "GSD", "DGR", "DIR", "JAV", "PIL", "GLV", "SPR", "TRI", "BRN", "PIK", "HAL", "SCY",
            "PAX", "BST", "SST", "CST", "LST", "SBW", "LBW", "CLW", "SKR", "KTR", "AXF", "SBB",
            "LBB", "LXB", "HXB", "OB1", "OB3", "OB4", "AM1", "AM2", "AM3", "CAP", "SKP", "HLM",
            "FHL", "GHM", "CRN", "MSK", "QLT", "LEA", "HLA", "STU", "RNG", "SCL", "CHN", "BRS",
            "SPL", "PLT", "FLD", "GTH", "FUL", "AAR", "LTP", "BUC", "LRG", "KIT", "TOW", "BHM",
            "BSH", "SPK", "DR1", "DR4", "DR3", "BA1", "BA3", "BA5", "PA1", "PA3", "PA5", "NE1",
            "NE2", "NE3", "_62", "_63", "_64", "_65", "_66", "_67", "_68", "_69", "_6A", "_6B",
            "_6C", "_6D", "_6E", "_6F", "_70", "_71", "_72", "_73", "_74", "_75", "_76", "_77",
            "_78", "_79", "_7A", "_7B", "_7C", "GPL", "OPL", "GPS", "OPS",
        }) {
          @Override
          public COFD2 getCOFs() {
            return Riiablo.cofs.chars_cof;
          }
        },
    ITM("ITEMS",
        new String[] {"NU"},
        new String[] {"NIL"}),
    WRP("WARPS",
        new String[] {"NU"},
        new String[] {"NIL"}),
    MIS("MISSILES",
        new String[] {"NU"},
        new String[] {"NIL"});

    public final String PATH;
    public final String MODE[];
    public final String COMP[];

    private ObjectIntMap<String> MODES;
    private ObjectIntMap<String> COMPS;

    Type(String path, String[] modes, String[] comps) {
      PATH = "data\\global\\" + path;
      MODE = modes;
      MODES = new ObjectIntMap<>();
      for (int i = 0; i < modes.length; i++) MODES.put(modes[i].toLowerCase(), i);
      COMP = comps;
      COMPS = new ObjectIntMap<>();
      for (int i = 0; i < comps.length; i++) COMPS.put(comps[i].toLowerCase(), i);
    }

    public COFD2 getCOFs() {
      return Riiablo.cofs.active;
    }

    public byte getMode(String mode) {
      return (byte) MODES.get(mode.toLowerCase(), -1);
    }

    public int getComponent(String comp) {
      return COMPS.get(comp.toLowerCase(), -1);
    }
  }

  public Type type = null;

  @Override
  public void reset() {
    type = null;
  }
}
