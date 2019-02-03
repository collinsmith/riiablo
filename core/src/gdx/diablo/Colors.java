package gdx.diablo;

import com.badlogic.gdx.graphics.Color;

public class Colors {

  public static final Color WHITE  = new Color(0xFFFFFFFF); //  0 #FFFFFF
  public static final Color SERVER = new Color(0xFF4D4DFF); //  1 #FF4D4D
  public static final Color SET    = new Color(0x00FF00FF); //  2 #00FF00
  public static final Color MAGIC  = new Color(0x6969FFFF); //  3 #6969FF
  public static final Color UNIQUE = new Color(0xC7B377FF); //  4 #C7B377
  public static final Color GREY   = new Color(0x696969FF); //  5 #696969
  public static final Color BLACK  = new Color(0x000000FF); //  6 #000000
  public static final Color C7     = new Color(0xD0C27DFF); //  7 #D0C27D
  public static final Color CRAFT  = new Color(0xFFA800FF); //  8 #FFA800
  public static final Color RARE   = new Color(0xFFFF64FF); //  9 #FFFF64
  public static final Color C10    = new Color(0x008000FF); // 10 #008000
  public static final Color PURPLE = new Color(0xAE00FFFF); // 11 #AE00FF
  public static final Color C12    = new Color(0x00C800FF); // 12 #00C800

  public Color white  = WHITE.cpy();
  public Color server = SERVER.cpy();
  public Color set    = SET.cpy();
  public Color magic  = MAGIC.cpy();
  public Color unique = UNIQUE.cpy();
  public Color grey   = GREY.cpy();
  public Color black  = BLACK.cpy();
  public Color c7     = C7.cpy();
  public Color craft  = CRAFT.cpy();
  public Color rare   = RARE.cpy();
  public Color c10    = C10.cpy();
  public Color purple = PURPLE.cpy();
  public Color c12    = C12.cpy();
  public Color modal  = black.cpy().sub(0, 0, 0, 0.5f);

  public Colors() {}

  public void load() {}
}
