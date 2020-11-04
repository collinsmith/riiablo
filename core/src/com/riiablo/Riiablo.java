package com.riiablo;

import com.artemis.World;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.riiablo.audio.Audio;
import com.riiablo.audio.MusicController;
import com.riiablo.codec.D2;
import com.riiablo.codec.StringTBLs;
import com.riiablo.console.RenderedConsole;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.mpq.MPQFileHandleResolver;
import com.riiablo.save.CharData;
import com.riiablo.screen.GameScreen;

public class Riiablo {
  private Riiablo() {}

  public static final int NORMAL    = 0;
  public static final int NIGHTMARE = 1;
  public static final int HELL      = 2;

  public static final int ACT1 = 0;
  public static final int ACT2 = 1;
  public static final int ACT3 = 2;
  public static final int ACT4 = 3;
  public static final int ACT5 = 4;

  public static final byte AMAZON      = 0;
  public static final byte SORCERESS   = 1;
  public static final byte NECROMANCER = 2;
  public static final byte PALADIN     = 3;
  public static final byte BARBARIAN   = 4;
  public static final byte DRUID       = 5;
  public static final byte ASSASSIN    = 6;

  public static final int NUM_DIFFS = 3;
  public static final int NUM_ACTS = 5;
  public static final int NUM_CLASSES = 7;
  public static final int MAX_PLAYERS = 8;
  public static final int MAX_NAME_LENGTH = 15;

  public static final int DESKTOP_VIEWPORT_HEIGHT = 480;
  public static final int MOBILE_VIEWPORT_HEIGHT  = 360;
  public static final int DESKTOP_VIEWPORT_MIN_WIDTH = 640;

  public static Client                client;
  public static FileHandle            home;
  public static FileHandle            saves;
  public static Viewport              viewport;
  public static Viewport              defaultViewport;
  public static ScalingViewport       scalingViewport; // 480p -> 360p for mobile
  public static ExtendViewport        extendViewport;  // 480p /w dynamic width
  public static PaletteIndexedBatch   batch;
  public static ShaderProgram         shader;
  public static ShapeRenderer         shapes;
  public static MPQFileHandleResolver mpqs;
  public static AssetManager          assets;
  public static Client.InputProcessor input;
  public static RenderedConsole       console;
  public static GdxCommandManager     commands;
  public static GdxCvarManager        cvars;
  public static GdxKeyMapper          keys;
  public static I18NBundle            bundle;
  public static StringTBLs            string;
  public static Colors                colors;
  public static Palettes              palettes;
  public static Colormaps             colormaps;
  public static Fonts                 fonts;
  public static Files                 files;
  public static COFs                  cofs;
  public static Textures              textures;
  public static Audio                 audio;
  public static MusicController       music;
  public static Cursor                cursor;
  public static CharData              charData;
  public static World                 engine;
  public static GameScreen            game;
  public static D2                    anim;
  public static Metrics               metrics;
  public static GdxLoggerManager      logs;
}
