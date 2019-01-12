package gdx.diablo.map;

public class Orientation {
  /** Floors */
  public static final int FLOOR                   = 0;
  /** Left Wall */
  public static final int LEFT_WALL               = 1;
  /** Right Wall */
  public static final int RIGHT_WALL              = 2;
  /** Right part of north corner wall */
  public static final int RIGHT_NORTH_CORNER_WALL = 3;
  /** Left part of north corner wall */
  public static final int LEFT_NORTH_CORNER_WALL  = 4;
  /** Left end wall */
  public static final int LEFT_END_WALL           = 5;
  /** Right end wall */
  public static final int RIGHT_END_WALL          = 6;
  /** South corner wall */
  public static final int SOUTH_CORNER_WALL       = 7;
  /** Left wall with door */
  public static final int LEFT_WALL_DOOR          = 8;
  /** Right wall with door */
  public static final int RIGHT_WALL_DOOR         = 9;
  /** Special Cell */
  public static final int SPECIAL_10              = 10;
  /** Special Cell */
  public static final int SPECIAL_11              = 11;
  /** Pillars, columns and standalone objects */
  public static final int PILLAR                  = 12;
  /** Shadows */
  public static final int SHADOW                  = 13;
  /** Trees */
  public static final int TREE                    = 14;
  /** Roofs */
  public static final int ROOF                    = 15;
  /** Lower walls equivalent to Orientation 1
   *  @see #LEFT_WALL */
  public static final int LOWER_LEFT_WALL         = 16;
  /** Lower walls equivalent to Orientation 2
   *  @see #RIGHT_WALL */
  public static final int LOWER_RIGHT_WALL        = 17;
  /** Lower walls equivalent to Orientation 3 and 4
   *  @see #RIGHT_NORTH_CORNER_WALL
   *  @see #LEFT_NORTH_CORNER_WALL */
  public static final int LOWER_NORTH_CORNER_WALL = 18;
  /** Lower walls equivalent to Orientation 7
   *  @see #SOUTH_CORNER_WALL */
  public static final int LOWER_SOUTH_CORNER_WALL = 19;

  public static String toString(int orientation) {
    switch (orientation) {
      case FLOOR:                   return "FLOOR";
      case LEFT_WALL:               return "LEFT_WALL";
      case RIGHT_WALL:              return "RIGHT_WALL";
      case RIGHT_NORTH_CORNER_WALL: return "RIGHT_NORTH_CORNER_WALL";
      case LEFT_NORTH_CORNER_WALL:  return "LEFT_NORTH_CORNER_WALL";
      case LEFT_END_WALL:           return "LEFT_END_WALL";
      case RIGHT_END_WALL:          return "RIGHT_END_WALL";
      case SOUTH_CORNER_WALL:       return "SOUTH_CORNER_WALL";
      case LEFT_WALL_DOOR:          return "LEFT_WALL_DOOR";
      case RIGHT_WALL_DOOR:         return "RIGHT_WALL_DOOR";
      case SPECIAL_10:              return "SPECIAL_10";
      case SPECIAL_11:              return "SPECIAL_11";
      case PILLAR:                  return "PILLAR";
      case SHADOW:                  return "SHADOW";
      case TREE:                    return "TREE";
      case ROOF:                    return "ROOF";
      case LOWER_LEFT_WALL:         return "LOWER_LEFT_WALL";
      case LOWER_RIGHT_WALL:        return "LOWER_RIGHT_WALL";
      case LOWER_NORTH_CORNER_WALL: return "LOWER_NORTH_CORNER_WALL";
      case LOWER_SOUTH_CORNER_WALL: return "LOWER_SOUTH_CORNER_WALL";
      default:                      return "null";
    }
  }

  public static int toDirection(int orientation) {
    switch (orientation) {
      case LEFT_WALL: case LEFT_END_WALL: case LEFT_WALL_DOOR:
        return 1;
      case RIGHT_WALL: case RIGHT_END_WALL: case RIGHT_WALL_DOOR:
        return 2;
      case FLOOR: case RIGHT_NORTH_CORNER_WALL: case LEFT_NORTH_CORNER_WALL: case PILLAR: case TREE:
        return 3;
      case SOUTH_CORNER_WALL:
        return 4;
      case ROOF:
        return 5;
      case LOWER_LEFT_WALL:
        return 6;
      case LOWER_RIGHT_WALL:
        return 7;
      case LOWER_NORTH_CORNER_WALL:
        return 8;
      case LOWER_SOUTH_CORNER_WALL:
        return 9;
      default: throw new AssertionError();
    }
  }

  public static boolean isFloor(int orientation) {
    switch (orientation) {
      case FLOOR:
        return true;
      default:
        return false;
    }
  }

  public static boolean isRoof(int orientation) {
    switch (orientation) {
      case ROOF:
        return true;
      default:
        return false;
    }
  }

  public static boolean isWall(int orientation) {
    switch (orientation) {
      case LEFT_WALL:
      case RIGHT_WALL:
      case RIGHT_NORTH_CORNER_WALL:
      case LEFT_NORTH_CORNER_WALL:
      case LEFT_END_WALL:
      case RIGHT_END_WALL:
      case SOUTH_CORNER_WALL:
      case LEFT_WALL_DOOR:
      case RIGHT_WALL_DOOR:
      case PILLAR:
      case TREE:
      case LOWER_LEFT_WALL:
      case LOWER_RIGHT_WALL:
      case LOWER_NORTH_CORNER_WALL:
      case LOWER_SOUTH_CORNER_WALL:
        return true;
      default:
        return false;
    }
  }

  public static boolean isSpecial(int orientation) {
    switch (orientation) {
      case SPECIAL_10:
      case SPECIAL_11:
        return true;
      default:
        return false;
    }
  }
}
