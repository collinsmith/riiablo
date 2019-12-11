package com.riiablo.engine;

public class Engine {
  private Engine() {}

  public static final int INVALID_ENTITY = -1;

  public static final byte WEAPON_NIL =  0;
  public static final byte WEAPON_HTH =  1;
  public static final byte WEAPON_BOW =  2;
  public static final byte WEAPON_1HS =  3;
  public static final byte WEAPON_1HT =  4;
  public static final byte WEAPON_STF =  5;
  public static final byte WEAPON_2HS =  6;
  public static final byte WEAPON_2HT =  7;
  public static final byte WEAPON_XBW =  8;
  public static final byte WEAPON_1JS =  9;
  public static final byte WEAPON_1JT = 10;
  public static final byte WEAPON_1SS = 11;
  public static final byte WEAPON_1ST = 12;
  public static final byte WEAPON_HT1 = 13;
  public static final byte WEAPON_HT2 = 14;

  private static final String[] WCLASS_NAME = {
      "", "HTH", "BOW", "1HS", "1HT", "STF", "2HS", "2HT", "XBW", "1JS", "1JT", "1SS", "1ST", "HT1", "HT2"
  };

  public static String getWClass(byte wclass) {
    return WCLASS_NAME[wclass];
  }

  private static final String[] COMPOSITE_NAME = {
      "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8",
  };

  public static String getComposite(int component) {
    return COMPOSITE_NAME[component];
  }

  public static final class Object {
    public static final int SUBCLASS_SHRINE    = 1 << 0; // displays "<shrine_type> Shrine"
    public static final int SUBCLASS_OBELISH   = 1 << 1; // does nothing
    public static final int SUBCLASS_PORTAL    = 1 << 2; // portals
    public static final int SUBCLASS_CONTAINER = 1 << 3; // displays the object's name
    public static final int SUBCLASS_ASPORTAL  = 1 << 4; // arcane sanctuary portal
    public static final int SUBCLASS_WELL      = 1 << 5; // does nothing
    public static final int SUBCLASS_WAYPOINT  = 1 << 6; // displays "<area_name> <object_name>"
    public static final int SUBCLASS_SECRET    = 1 << 7; // makes the object unselectable, while keeping it clickable

    public static final byte MODE_NU = 0;
    public static final byte MODE_OP = 1;
    public static final byte MODE_ON = 2;
    public static final byte MODE_S1 = 3;
    public static final byte MODE_S2 = 4;
    public static final byte MODE_S3 = 5;
    public static final byte MODE_S4 = 6;
    public static final byte MODE_S5 = 7;
  }

  public static final class Monster {
    public static final byte MODE_DT =  0;
    public static final byte MODE_NU =  1;
    public static final byte MODE_WL =  2;
    public static final byte MODE_GH =  3;
    public static final byte MODE_A1 =  4;
    public static final byte MODE_A2 =  5;
    public static final byte MODE_BL =  6;
    public static final byte MODE_SC =  7;
    public static final byte MODE_S1 =  8;
    public static final byte MODE_S2 =  9;
    public static final byte MODE_S3 = 10;
    public static final byte MODE_S4 = 11;
    public static final byte MODE_DD = 12;
    //public static final byte MODE_GH = 13;
    public static final byte MODE_XX = 14;
    public static final byte MODE_RN = 15;
  }

  public static final class Player {
    public static final byte TOKEN_AM = 0;
    public static final byte TOKEN_SO = 1;
    public static final byte TOKEN_NE = 2;
    public static final byte TOKEN_PA = 3;
    public static final byte TOKEN_BA = 4;
    public static final byte TOKEN_DZ = 5;
    public static final byte TOKEN_AI = 6;

    private static final String[] TOKENS = {"AM", "SO", "NE", "PA", "BA", "DZ", "AI"};

    public static String getToken(int classId) {
      return TOKENS[classId];
    }

    public static final byte MODE_DT =  0;
    public static final byte MODE_NU =  1;
    public static final byte MODE_WL =  2;
    public static final byte MODE_RN =  3;
    public static final byte MODE_GH =  4;
    public static final byte MODE_TN =  5;
    public static final byte MODE_TW =  6;
    public static final byte MODE_A1 =  7;
    public static final byte MODE_A2 =  8;
    public static final byte MODE_BL =  9;
    public static final byte MODE_SC = 10;
    public static final byte MODE_TH = 11;
    public static final byte MODE_KK = 12;
    public static final byte MODE_S1 = 13;
    public static final byte MODE_S2 = 14;
    public static final byte MODE_S3 = 15;
    public static final byte MODE_S4 = 16;
    public static final byte MODE_DD = 17;
    //public static final byte MODE_GH = 18;
    //public static final byte MODE_GH = 19;
  }

  public static final int MAX_PLAYERS = 8;
  public static final int MAX_NAME_LENGTH = 15;
}
