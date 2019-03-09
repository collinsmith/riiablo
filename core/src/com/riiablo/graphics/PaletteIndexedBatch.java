package com.riiablo.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import com.riiablo.codec.Index;

public class PaletteIndexedBatch extends SpriteBatch {
  private static final int PALETTE_TEXTURE_ID  = 1;
  private static final int COLORMAP_TEXTURE_ID = 2;

  private final int PALETTE_LOCATION;
  private final int BLENDMODE_LOCATION;
  private final int COLORMAP_LOCATION;
  private final int COLORMAPID_LOCATION;
  private final int GAMMA_LOCATION;
  private final ShaderProgram shader;

  private Texture palette;
  private Texture colormap;
  private int blendMode;
  private int colormapId;
  private Color color = Color.WHITE.cpy();
  private float gamma = 1.0f;
  private boolean disabled = false;

  public PaletteIndexedBatch(int size, ShaderProgram shader) {
    super(size);
    this.shader = shader;
    PALETTE_LOCATION    = shader.getUniformLocation("ColorTable");
    COLORMAP_LOCATION   = shader.getUniformLocation("ColorMap");
    BLENDMODE_LOCATION  = shader.getUniformLocation("blendMode");
    COLORMAPID_LOCATION = shader.getUniformLocation("colormapId");
    GAMMA_LOCATION      = shader.getUniformLocation("gamma");
  }

  public void setPalette(Texture palette) {
    this.palette = palette;
    if (isDrawing()) {
      flush();
      applyPalette();
    }
  }

  public void setColormap(Index colormap, int id) {
    setColormap(colormap != null ? colormap.texture : null, id);
  }

  public void setColormap(Texture colormap, int id) {
    if (id == 0 || colormap == null) {
      resetColormap();
      return;
    }

    this.colormap = colormap;
    this.colormapId = id;
    if (isDrawing()) {
      flush();
      applyColormap();
    }
  }

  public void resetColormap() {
    if (isDrawing()) {
      flush();
    }

    colormapId = 0;
    shader.setUniformi(COLORMAPID_LOCATION, colormapId);
  }

  public void setBlendMode(int blendMode, Color tint) {
    setBlendMode(blendMode, tint, false);
  }

  public void setBlendMode(int blendMode, Color tint, boolean force) {
    setBlendMode(blendMode);
    setColor(tint);
  }

  public void setBlendMode(int blendMode) {
    if (this.blendMode != blendMode) {
      if (isDrawing()) flush();
      shader.setUniformi(BLENDMODE_LOCATION, blendMode);
      this.blendMode = blendMode;
    }
  }

  public void resetBlendMode() {
    setBlendMode(BlendMode.ID, Color.WHITE);
  }

  public void setAlpha(float a) {
    color.a = a;
    setColor(color);
  }

  public void resetColor() {
    setColor(Color.WHITE);
  }

  public float getGamma() {
    return gamma;
  }

  public void setGamma(float gamma) {
    if (this.gamma != gamma) {
      this.gamma = gamma;
    }
  }

  // TODO: support flush
  public void setDisabled(boolean disabled) {
    if (this.disabled != disabled) {
      if (isDrawing()) flush();
      this.disabled = disabled;
    }
  }

  public boolean isDisabled() {
    return disabled;
  }

  @Override
  public void begin() {
    if (disabled) {
      super.begin();
      return;
    }

    setShader(shader);
    super.begin();
    resetBlendMode();
    resetColormap();
    applyPalette();
    applyGamma();
  }

  public void begin(Texture palette) {
    setPalette(palette);
    begin();
  }

  private void applyPalette() {
    if (palette == null) return;
    palette.bind(PALETTE_TEXTURE_ID);
    shader.setUniformi(PALETTE_LOCATION, PALETTE_TEXTURE_ID);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
  }

  private void applyColormap() {
    colormap.bind(COLORMAP_TEXTURE_ID);
    shader.setUniformi(COLORMAP_LOCATION, COLORMAP_TEXTURE_ID);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    shader.setUniformi(COLORMAPID_LOCATION, colormapId);
  }

  private void applyGamma() {
    shader.setUniformf(GAMMA_LOCATION, gamma);
  }
}
