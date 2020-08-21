package com.riiablo.skill;

public class SkillCodes {

  private SkillCodes() {}

  public static final int attack               = 0;
  public static final int kick                 = 1;
  public static final int throw_               = 2;
  public static final int unsummon             = 3;
  public static final int left_hand_throw      = 4;
  public static final int left_hand_swing      = 5;
  public static final int scroll_of_identify   = 217;
  public static final int book_of_identify     = 218;
  public static final int scroll_of_townportal = 219;
  public static final int book_of_townportal   = 220;


  public static String getCode(int code) {
    switch (code) {
      case attack: return "attack";
      case kick: return "kick";
      case throw_: return "throw_";
      case unsummon: return "unsummon";
      case left_hand_throw: return "left_hand_throw";
      case left_hand_swing: return "left_hand_swing";
      case scroll_of_identify: return "scroll_of_identify";
      case book_of_identify: return "book_of_identify";
      case scroll_of_townportal: return "scroll_of_townportal";
      case book_of_townportal: return "book_of_townportal";

      default: return null;
    }
  }
}
