package com.riiablo;

import com.badlogic.gdx.graphics.Color;

public class Colors {

  public static final Color WHITE  = new Color(0xFFFFFFFF); //  0 #FFFFFF
  public static final Color RED    = new Color(0xFF4D4DFF); //  1 #FF4D4D
  public static final Color GREEN  = new Color(0x00FF00FF); //  2 #00FF00
  public static final Color BLUE   = new Color(0x6969FFFF); //  3 #6969FF
  public static final Color GOLD   = new Color(0xC7B377FF); //  4 #C7B377
  public static final Color GREY   = new Color(0x696969FF); //  5 #696969
  public static final Color BLACK  = new Color(0x000000FF); //  6 #000000
  public static final Color C7     = new Color(0xD0C27DFF); //  7 #D0C27D
  public static final Color ORANGE = new Color(0xFFA800FF); //  8 #FFA800
  public static final Color YELLOW = new Color(0xFFFF64FF); //  9 #FFFF64
  public static final Color C10    = new Color(0x008000FF); // 10 #008000
  public static final Color PURPLE = new Color(0xAE00FFFF); // 11 #AE00FF
  public static final Color C12    = new Color(0x00C800FF); // 12 #00C800

  public Color white  = WHITE.cpy();
  public Color red    = RED.cpy();
  public Color green  = GREEN.cpy();
  public Color blue   = BLUE.cpy();
  public Color gold   = GOLD.cpy();
  public Color grey   = GREY.cpy();
  public Color black  = BLACK.cpy();
  public Color c7     = C7.cpy();
  public Color orange = ORANGE.cpy();
  public Color yellow = YELLOW.cpy();
  public Color c10    = C10.cpy();
  public Color purple = PURPLE.cpy();
  public Color c12    = C12.cpy();

  public Color highlight = new Color(0.15f, 0.15f, 0.12f, 0);

  public Color invBlue  = new Color(0.1f, 0.1f, 0.5f, 0.3f);
  public Color invGreen = new Color(0.1f, 0.5f, 0.1f, 0.3f);
  public Color invRed   = new Color(0.5f, 0.1f, 0.1f, 0.3f);
  public Color invWhite = new Color(0.5f, 0.5f, 0.5f, 0.3f);

  public Color modal25 = new Color(0, 0, 0, 0.25f);
  public Color modal50 = new Color(0, 0, 0, 0.50f);
  public Color modal75 = new Color(0, 0, 0, 0.75f);

  public Color trans25 = new Color(1, 1, 1, 0.25f);
  public Color trans50 = new Color(1, 1, 1, 0.50f);
  public Color trans75 = new Color(1, 1, 1, 0.75f);

  public Colors() {}

  public void load() {
    // TODO: Add item color cvars
    //Cvars.Client.Colors.unique = gold;
    //Cvars.Client.Colors.unique.addListener(
    //    unique.set(toValue)
    //)
  }
}
