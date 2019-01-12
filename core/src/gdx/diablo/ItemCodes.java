package gdx.diablo;

public class ItemCodes {

  private ItemCodes() {}

  public static final int NIL = 0xFF;

  public static final int LIT = 0x01;
  public static final int MED = 0x02;
  public static final int HVY = 0x03;

  public static final int HAX = 0x04;
  public static final int AXE = 0x05;
  public static final int LAX = 0x06;
  public static final int BTX = 0x07;
  public static final int GIX = 0x08;
  public static final int WND = 0x09;
  public static final int YWN = 0x0A;
  public static final int BWN = 0x0B;
  public static final int CLB = 0x0C;
  public static final int MAC = 0x0D;
  public static final int WHM = 0x0E;
  public static final int FLA = 0x0F;
  public static final int MAU = 0x10;
  public static final int SSD = 0x11;
  public static final int SCM = 0x12;
  public static final int FLC = 0x13;
  public static final int CRS = 0x14;
  public static final int BSD = 0x15;
  public static final int LSD = 0x16;
  public static final int CLM = 0x17;
  public static final int GSD = 0x18;
  public static final int DGR = 0x19;
  public static final int DIR = 0x1A;
  public static final int JAV = 0x1B;
  public static final int PIL = 0x1C;
  public static final int GLV = 0x1D;
  public static final int SPR = 0x1E;
  public static final int TRI = 0x1F;
  public static final int BRN = 0x20;
  public static final int PIK = 0x21;
  public static final int HAL = 0x22;
  public static final int SCY = 0x23;
  public static final int PAX = 0x24;
  public static final int BST = 0x25;
  public static final int SST = 0x26;
  public static final int CST = 0x27;
  public static final int LST = 0x28;
  public static final int SBW = 0x29;
  public static final int LBW = 0x2A;
  public static final int CLW = 0x2B;
  public static final int SKR = 0x2C;
  public static final int KTR = 0x2D;
  public static final int AXF = 0x2E;
  public static final int SBB = 0x2F;
  public static final int LBB = 0x30;
  public static final int LXB = 0x31;
  public static final int HXB = 0x32;
  public static final int OB1 = 0x33;
  public static final int OB3 = 0x34;
  public static final int OB4 = 0x35;
  public static final int AM1 = 0x36;
  public static final int AM2 = 0x37;
  public static final int AM3 = 0x38;
  public static final int CAP = 0x39;
  public static final int SKP = 0x3A;
  public static final int HLM = 0x3B;
  public static final int FHL = 0x3C;
  public static final int GHM = 0x3D;
  public static final int CRN = 0x3E;
  public static final int MSK = 0x3F;

  // Armors (not directly used)
  public static final int QLT = 0x40;
  public static final int LEA = 0x41;
  public static final int HLA = 0x42;
  public static final int STU = 0x43;
  public static final int RNG = 0x44;
  public static final int SCL = 0x45;
  public static final int CHN = 0x46;
  public static final int BRS = 0x47;
  public static final int SPL = 0x48;
  public static final int PLT = 0x49;
  public static final int FLD = 0x4A;
  public static final int GTH = 0x4B;
  public static final int FUL = 0x4C;
  public static final int AAR = 0x4D;
  public static final int LTP = 0x4E;

  // Shields (not directly used)
  public static final int BUC = 0x4F;
  public static final int LRG = 0x50;
  public static final int KIT = 0x51;
  public static final int TOW = 0x52;

  // Gloves?
  public static final int LGL = 0x53;

  public static final int BSH = 0x54;
  public static final int SPK = 0x55;

  public static final int DR1 = 0x56;
  public static final int DR4 = 0x57;
  public static final int DR3 = 0x58;

  public static final int BA1 = 0x59;
  public static final int BA3 = 0x5A;
  public static final int BA5 = 0x5B;

  public static final int PA1 = 0x5C;
  public static final int PA3 = 0x5D;
  public static final int PA5 = 0x5E;

  public static final int NE1 = 0x5F;
  public static final int NE2 = 0x60;
  public static final int NE3 = 0x61;

  //public static final int _79 = 0x79; // See _7C
  //public static final int _7A = 0x7A; // See _7C
  //public static final int _7B = 0x7B; // See _7C
  //public static final int _7C = 0x7C; // RHHXBTNXBW without LHHXBTNXBW (HXB without string)
  public static final int GPL = 0x7D;
  public static final int OPL = 0x7E; // same gfx as OPS
  public static final int GPS = 0x7F;
  public static final int OPS = 0x80; // same gfx as OPS

  public static String getCode(int code) {
    switch (code) {
      case LIT: return "LIT";
      case MED: return "MED";
      case HVY: return "HVY";

      case HAX: return "HAX";
      case AXE: return "AXE";
      case LAX: return "LAX";
      case BTX: return "BTX";
      case GIX: return "GIX";
      case WND: return "WND";
      case YWN: return "YWN";
      case BWN: return "BWN";
      case CLB: return "CLB";
      case MAC: return "MAC";
      case WHM: return "WHM";
      case FLA: return "FLA";
      case MAU: return "MAU";
      case SSD: return "SSD";
      case SCM: return "SCM";
      case FLC: return "FLC";
      case CRS: return "CRS";
      case BSD: return "BSD";
      case LSD: return "LSD";
      case CLM: return "CLM";
      case GSD: return "GSD";
      case DGR: return "DGR";
      case DIR: return "DIR";
      case JAV: return "JAV";
      case PIL: return "PIL";
      case GLV: return "GLV";
      case SPR: return "SPR";
      case TRI: return "TRI";
      case BRN: return "BRN";
      case PIK: return "PIK";
      case HAL: return "HAL";
      case SCY: return "SCY";
      case PAX: return "PAX";
      case BST: return "BST";
      case SST: return "SST";
      case CST: return "CST";
      case LST: return "LST";
      case SBW: return "SBW";
      case LBW: return "LBW";
      case CLW: return "CLW";
      case SKR: return "SKR";
      case KTR: return "KTR";
      case AXF: return "AXF";
      case SBB: return "SBB";
      case LBB: return "LBB";
      case LXB: return "LXB";
      case HXB: return "HXB";
      case OB1: return "OB1";
      case OB3: return "OB3";
      case OB4: return "OB4";
      case AM1: return "AM1";
      case AM2: return "AM2";
      case AM3: return "AM3";
      case CAP: return "CAP";
      case SKP: return "SKP";
      case HLM: return "HLM";
      case FHL: return "FHL";
      case GHM: return "GHM";
      case CRN: return "CRN";
      case MSK: return "MSK";

      // Armors (not directly used)
      case QLT: return "QLT";
      case LEA: return "LEA";
      case HLA: return "HLA";
      case STU: return "STU";
      case RNG: return "RNG";
      case SCL: return "SCL";
      case CHN: return "CHN";
      case BRS: return "BRS";
      case SPL: return "SPL";
      case PLT: return "PLT";
      case FLD: return "FLD";
      case GTH: return "GTH";
      case FUL: return "FUL";
      case AAR: return "AAR";
      case LTP: return "LTP";

      // Shields (not directly used)
      case BUC: return "BUC";
      case LRG: return "LRG";
      case KIT: return "KIT";
      case TOW: return "TOW";

      // Gloves?
      //case LGL: return "LGL";

      case BSH: return "BSH";
      case SPK: return "SPK";

      case DR1: return "DR1";
      case DR4: return "DR4";
      case DR3: return "DR3";

      case BA1: return "BA1";
      case BA3: return "BA3";
      case BA5: return "BA5";

      case PA1: return "PA1";
      case PA3: return "PA3";
      case PA5: return "PA5";

      case NE1: return "NE1";
      case NE2: return "NE2";
      case NE3: return "NE3";

      case GPL: return "GPL";
      case OPL: return "OPL"; // same gfx as OPS
      case GPS: return "GPS";
      case OPS: return "OPS"; // same gfx as OPS

      case NIL:
      default:  return null;
    }
  }
}
