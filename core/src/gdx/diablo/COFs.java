package gdx.diablo;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;

import gdx.diablo.codec.COFD2;

public class COFs {
  private static final String COFD2_DIR = "data\\global\\";

  public final COFD2 chars_cof;
  public COFD2 active;
  public COFD2 cmncof_a1;
  public COFD2 cmncof_a2;
  public COFD2 cmncof_a3;
  public COFD2 cmncof_a4;
  public COFD2 cmncof_a5;
  public COFD2 cmncof_a6;
  public COFD2 cmncof_a7;

  public COFs(AssetManager assets) {
    chars_cof = load(assets, "chars_cof");
    cmncof_a1 = load(assets, "cmncof_a1");
//  cmncof_a2 = load(assets, "cmncof_a2");
//  cmncof_a3 = load(assets, "cmncof_a3");
//  cmncof_a4 = load(assets, "cmncof_a4");
//  cmncof_a5 = load(assets, "cmncof_a5");
//  cmncof_a6 = load(assets, "cmncof_a6");
//  cmncof_a7 = load(assets, "cmncof_a7");
    active = cmncof_a1;
  }

  private COFD2 load(AssetManager assets, String fileName) {
    //AssetDescriptor<COFD2> descriptor = getDescriptor(fileName);
    //assets.load(descriptor);
    //assets.finishLoadingAsset(descriptor);
    //return assets.get(descriptor);
    return COFD2.loadFromFile(Diablo.mpqs.resolve(COFD2_DIR + fileName + ".d2"));
  }

  private static AssetDescriptor<COFD2> getDescriptor(String fileName) {
    return new AssetDescriptor<>(COFD2_DIR + fileName + ".d2", COFD2.class);
  }

}
