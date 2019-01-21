package gdx.diablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;

import gdx.diablo.audio.MusicController;
import gdx.diablo.audio.MusicVolumeController;
import gdx.diablo.audio.SoundVolumeController;
import gdx.diablo.audio.VolumeControlledMusicLoader;
import gdx.diablo.audio.VolumeControlledSoundLoader;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.DCC;
import gdx.diablo.codec.FontTBL;
import gdx.diablo.codec.Index;
import gdx.diablo.codec.Palette;
import gdx.diablo.codec.StringTBLs;
import gdx.diablo.codec.TXT;
import gdx.diablo.console.RenderedConsole;
import gdx.diablo.cvar.Cvar;
import gdx.diablo.cvar.CvarStateAdapter;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.key.MappedKey;
import gdx.diablo.loader.BitmapFontLoader;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.loader.DCCLoader;
import gdx.diablo.loader.IndexLoader;
import gdx.diablo.loader.PaletteLoader;
import gdx.diablo.loader.TXTLoader;
import gdx.diablo.map.DS1;
import gdx.diablo.map.DS1Loader;
import gdx.diablo.map.DT1;
import gdx.diablo.map.DT1Loader;
import gdx.diablo.map.Map;
import gdx.diablo.map.MapLoader;
import gdx.diablo.mpq.MPQFileHandleResolver;
import gdx.diablo.screen.AudioUnpackerScreen;
import gdx.diablo.screen.SplashScreen;

public class Client extends Game {
  private static final String TAG = "Client";

  private static final boolean DEBUG_AUDIO_UNPACKER = !true;

  private int width, height;

  private Array<Screen> screens = new Array<>(8);

  private final Matrix4 BATCH_RESET = new Matrix4();
  private Viewport viewport;
  private PaletteIndexedBatch batch;
  private ShaderProgram shader;
  private ShapeRenderer shapes;

  private MPQFileHandleResolver mpqs;
  private FileHandle        home;
  private StringTBLs        string;
  private InputProcessor    input;
  private RenderedConsole   console;
  private AssetManager      assets;
  private MusicController   music;
  private GdxCommandManager commands;
  private GdxCvarManager    cvars;
  private GdxKeyMapper      keys;
  private I18NBundle        bundle;
  private Fonts             fonts;
  private Palettes          palettes;
  private Colormaps         colormaps;
  private Files             txts;
  private COFs              cofs;
  private Colors            colors;
  private Cursor            cursor;
  private Audio             audio;
  private Textures          textures;

  private boolean forceWindowed;
  private boolean forceDrawFps;
  private byte    drawFpsMethod;

  public Client(FileHandle home) {
    this(home, Diablo.VIRTUAL_WIDTH, Diablo.VIRTUAL_HEIGHT);
  }

  public Client(FileHandle home, int width, int height) {
    this.home = home;
    this.width = width;
    this.height = height;
    Diablo.client = this;
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  public boolean isWindowedForced() {
    return forceWindowed;
  }

  public void setWindowedForced(boolean b) {
    forceWindowed = b;
  }

  public boolean isDrawFPSForced() {
    return forceDrawFps;
  }

  public void setDrawFPSForced(boolean b) {
    forceDrawFps = b;
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    // This is needed so that home is in a platform-dependent handle
    Diablo.home = home = Gdx.files.absolute(home.path());

    boolean usesStdOut = true;
    final OutputStream consoleOut = usesStdOut
        ? System.out
        : Gdx.files.internal("console.out").write(false);

    Diablo.console = console = RenderedConsole.wrap(consoleOut);
    try {
      System.setOut(console.out);
      System.setErr(console.out);
    } catch (SecurityException e) {
      console.out.println("stdout could not be redirected to console: " + e.getMessage());
      throw new GdxRuntimeException("Unable to bind console out.", e);
    } finally {
      console.setVisible(false);
      console.addProcessor(CommandProcessor.INSTANCE);
      console.addSuggestionProvider(CommandProcessor.INSTANCE);
      Calendar calendar = Calendar.getInstance();
      DateFormat format = DateFormat.getDateTimeInstance();
      console.out.println(format.format(calendar.getTime()));
      console.out.println(home.path());
    }

    if (!home.exists() || !home.child("d2data.mpq").exists()) {
      throw new GdxRuntimeException("home does not refer to a valid D2 installation. Copy MPQs to " + home);
    }

    Diablo.mpqs = mpqs = new MPQFileHandleResolver();
    mpqs.add(home.child("patch_d2.mpq"));
    mpqs.add(home.child("d2exp.mpq"));
    mpqs.add(home.child("d2xmusic.mpq"));
    mpqs.add(home.child("d2xtalk.mpq"));
    mpqs.add(home.child("d2xvideo.mpq"));
    mpqs.add(home.child("d2data.mpq"));
    mpqs.add(home.child("d2char.mpq"));
    mpqs.add(home.child("d2sfx.mpq"));
    mpqs.add(home.child("d2music.mpq"));
    mpqs.add(home.child("d2speech.mpq"));
    mpqs.add(home.child("d2video.mpq"));

    Diablo.string = string = new StringTBLs(mpqs);

    Diablo.assets = assets = new AssetManager();
    Texture.setAssetManager(assets);
    console.create();

    FileHandleResolver soundResolver = Gdx.app.getType() == Application.ApplicationType.Android
        ? new HomeFileHandleResolver(home)
        : mpqs;
    assets.setLoader(Sound.class, new VolumeControlledSoundLoader(soundResolver, new SoundVolumeController()));
    assets.setLoader(Music.class, new VolumeControlledMusicLoader(soundResolver, new MusicVolumeController()));
    assets.setLoader(DC6.class, new DC6Loader(mpqs));
    assets.setLoader(DCC.class, new DCCLoader(mpqs));
    assets.setLoader(Palette.class, new PaletteLoader(mpqs));
    assets.setLoader(Index.class, new IndexLoader(mpqs));
    assets.setLoader(FontTBL.BitmapFont.class, new BitmapFontLoader(mpqs));
    //assets.setLoader(Animation.class, new AnimationLoader(mpqs));
    //assets.setLoader(Animation.Layer.class, new AnimationLayerLoader(mpqs));
    assets.setLoader(DT1.class, new DT1Loader(mpqs));
    assets.setLoader(DS1.class, new DS1Loader(mpqs));
    assets.setLoader(TXT.class, new TXTLoader(mpqs));
    assets.setLoader(Map.class, new MapLoader(mpqs));

    //assets.load("data\\local\\font\\latin\\fontridiculous.DC6", FontTBL.BitmapFont.class);

    Diablo.palettes  = palettes  = new Palettes(assets);
    Diablo.colormaps = colormaps = new Colormaps(assets);
    Diablo.fonts     = fonts     = new Fonts(assets);
    Diablo.music     = music     = new MusicController(assets);
    Diablo.files     = txts      = new Files(assets);
    Diablo.cofs      = cofs      = new COFs(assets);
    Diablo.colors    = colors    = new Colors();
    Diablo.cursor    = cursor    = new Cursor();
    Diablo.audio     = audio     = new Audio(assets);
    Diablo.textures  = textures  = new Textures();

    Collection<Throwable> throwables;
    Diablo.commands = commands = new GdxCommandManager();
    throwables = Commands.addTo(commands);
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    Diablo.cvars = cvars = new GdxCvarManager();
    throwables = Cvars.addTo(cvars);
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    Diablo.keys = keys = new GdxKeyMapper();
    throwables = Keys.addTo(keys);
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    colors.load();

    // TODO: Conditionally enable console on Android
    if (false) {
      Keys.Console.assign(MappedKey.SECONDARY_MAPPING, Input.Keys.MENU);
    }

    Diablo.input = input = new InputProcessor() {{
      addProcessor(console);
      addProcessor(keys.newInputProcessor());
    }};
    Gdx.input.setInputProcessor(input);
    Gdx.input.setCatchBackKey(true);
    Gdx.input.setCatchMenuKey(true);

    Diablo.viewport = viewport = new ScalingViewport(Scaling.fill, Diablo.VIRTUAL_WIDTH, Diablo.VIRTUAL_HEIGHT);
    ShaderProgram.pedantic = false;
    Diablo.shader = shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette3.vert"),
        Gdx.files.internal("shaders/indexpalette3.frag"));
    Diablo.batch = batch = new PaletteIndexedBatch(1024, shader); // TODO: adjust this as needed
    Diablo.shapes = shapes = new ShapeRenderer();

    Diablo.bundle = bundle = I18NBundle.createBundle(Gdx.files.internal("lang/Client"));

    bindCvars();

    if ((Gdx.app.getType() == Application.ApplicationType.Android && !home.child("data").exists()) || DEBUG_AUDIO_UNPACKER) {
      setScreen(new AudioUnpackerScreen());
    } else {
      setScreen(new SplashScreen());
    }

    Gdx.gl.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
  }

  @Override
  public void resize(int width, int height) {
    this.width = width;
    this.height = height;
    BATCH_RESET.setToOrtho2D(0, 0, width, height);
    console.resize(width, height);
    viewport.update(width, height, true);
    super.resize(width, height);
  }

  @Override
  public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    Camera camera = viewport.getCamera();
    camera.update();
    batch.setProjectionMatrix(camera.combined);
    shapes.setProjectionMatrix(camera.combined);
    super.render();
    Diablo.cursor.render(batch);

    Batch b = batch;
    b.setProjectionMatrix(BATCH_RESET);
    b.begin(); {
      batch.setShader(null);
      if (!Diablo.assets.update()) {
        drawLoading(b);
      }

      if (drawFpsMethod > 0 || forceDrawFps) {
        drawFps(b);
      }

      console.render(b);
    } b.end();
  }

  private String screenToString(Screen screen) {
    return screen != null ? screen.getClass().getSimpleName() : null;
  }

  @Override
  public void setScreen(Screen screen) {
    Screen previousScreen = this.screen;
    Gdx.app.debug(TAG, "Scene change: " + screenToString(previousScreen) + " -> " + screenToString(screen));
    if (previousScreen instanceof com.badlogic.gdx.InputProcessor) {
      input.removeProcessor((com.badlogic.gdx.InputProcessor) previousScreen);
    }

    super.setScreen(screen);
    if (previousScreen != null && screens.isEmpty()) {
      Gdx.app.debug(TAG, "Disposing " + screenToString(previousScreen));
      previousScreen.dispose();
    }
    if (screen instanceof com.badlogic.gdx.InputProcessor) {
      input.addProcessor((com.badlogic.gdx.InputProcessor) screen);
    }
  }

  public void pushScreen(Screen screen) {
    if (this.screen != null) {
      screens.add(this.screen);
    }

    setScreen(screen);
  }

  public void popScreen() {
    assert screens.size > 0;
    setScreen(screens.pop());
  }

  public void clearAndSet(Screen screen) {
    setScreen(screen);
    for (Screen s : screens) s.dispose();
    screens.clear();
  }

  private void drawLoading(Batch b) {
    BitmapFont font = console.getFont();
    if (font == null) return;
    GlyphLayout ellipsis = new GlyphLayout(font, "Loading... " + (int) (Diablo.assets.getProgress() * 100) + "%");
    font.draw(b, ellipsis, 0, ellipsis.height);
  }

  private void drawFps(Batch b) {
    BitmapFont font = console.getFont();
    if (font == null) return;

    GlyphLayout fps = new GlyphLayout(font, Integer.toString(Gdx.graphics.getFramesPerSecond()));
    int drawFpsMethod = this.drawFpsMethod;
    if (forceDrawFps && drawFpsMethod == 0) {
      drawFpsMethod = 1;
    }

    float x, y;
    switch (drawFpsMethod) {
      case 1:
        x = 0;
        y = height;
        break;
      case 2:
        x = width - fps.width;
        y = height;
        break;
      case 3:
        x = 0;
        y = fps.height;
        break;
      case 4:
        x = width() - fps.width;
        y = 0;
        break;
      default:
        return;
    }

    font.draw(b, fps, x, y);
  }

  @Override
  public void pause() {
    super.pause();
  }

  @Override
  public void resume() {
    Diablo.client = this;
    Diablo.home = home;
    Diablo.mpqs = mpqs;
    Diablo.string = string;
    Diablo.input = input;
    Diablo.batch = batch;
    Diablo.shader = shader;
    Diablo.shapes = shapes;
    Diablo.viewport = viewport;
    Diablo.console = console;
    Diablo.assets = assets;
    Diablo.palettes = palettes;
    Diablo.colormaps = colormaps;
    Diablo.fonts = fonts;
    Diablo.files = txts;
    Diablo.music = music;
    Diablo.commands = commands;
    Diablo.cvars = cvars;
    Diablo.keys = keys;
    Diablo.bundle = bundle;
    Diablo.cofs = cofs;
    Diablo.colors = colors;
    Diablo.cursor = cursor;
    Diablo.audio = audio;
    Diablo.textures = textures;
    super.resume();
  }

  @Override
  public void dispose() {
    Gdx.app.debug(TAG, "Disposing screen...");
    super.dispose();

    Gdx.app.debug(TAG, "Disposing shader...");
    shader.dispose();
    Gdx.app.debug(TAG, "Disposing batch...");
    batch.dispose();

    Collection<Throwable> throwables;
    Gdx.app.debug(TAG, "Saving CVARS...");
    throwables = cvars.saveAll();
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    Gdx.app.debug(TAG, "Saving key assignments...");
    throwables = keys.saveAll();
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    Gdx.app.debug(TAG, "Disposing console...");
    console.dispose();

    Gdx.app.debug(TAG, "Disposing assets...");
    palettes.dispose();
    colormaps.dispose();
    textures.dispose();
    assets.dispose();

    try {
      Gdx.app.debug(TAG, "Resetting stdout...");
      System.setOut(System.out);
      Gdx.app.debug(TAG, "Resetting stderr...");
      System.setErr(System.err);
    } catch (SecurityException ignored) {
    } finally {
      Gdx.app.debug(TAG, "Flushing console...");
      console.out.flush();
      console.out.close();
    }
  }

  private void bindCvars() {
    Cvars.Client.Display.ShowFPS.addStateListener(new CvarStateAdapter<Byte>() {
      @Override
      public void onChanged(Cvar<Byte> cvar, Byte from, Byte to) {
        drawFpsMethod = to;
      }
    });

    Cvars.Client.Display.Gamma.addStateListener(new CvarStateAdapter<Float>() {
      @Override
      public void onChanged(Cvar<Float> cvar, Float from, Float to) {
        batch.setGamma(to);
      }
    });
  }

  public static class InputProcessor extends InputMultiplexer {
    boolean vibration = false;

    public InputProcessor() {
      Cvars.Client.Input.Vibration.addStateListener(new CvarStateAdapter<Boolean>() {
        @Override
        public void onChanged(Cvar<Boolean> cvar, Boolean from, Boolean to) {
          vibration = to;
        }
      });
    }

    public void vibrate(int millis) {
      if (vibration) Gdx.input.vibrate(millis);
    }

    public void vibrate(long[] pattern, int repeat) {
      if (vibration) Gdx.input.vibrate(pattern, repeat);
    }
  }
}
