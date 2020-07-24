package com.riiablo;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.riiablo.audio.Audio;
import com.riiablo.audio.MusicController;
import com.riiablo.audio.MusicVolumeController;
import com.riiablo.audio.SoundVolumeController;
import com.riiablo.audio.VolumeControlledMusicLoader;
import com.riiablo.audio.VolumeControlledSoundLoader;
import com.riiablo.codec.COF;
import com.riiablo.codec.D2;
import com.riiablo.codec.DC6;
import com.riiablo.codec.DCC;
import com.riiablo.codec.FontTBL;
import com.riiablo.codec.Index;
import com.riiablo.codec.Palette;
import com.riiablo.codec.StringTBLs;
import com.riiablo.console.RenderedConsole;
import com.riiablo.cvar.Cvar;
import com.riiablo.cvar.CvarStateAdapter;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.BitmapFontLoader;
import com.riiablo.loader.COFLoader;
import com.riiablo.loader.DC6Loader;
import com.riiablo.loader.DCCLoader;
import com.riiablo.loader.IndexLoader;
import com.riiablo.loader.PaletteLoader;
import com.riiablo.map.DS1;
import com.riiablo.map.DS1Loader;
import com.riiablo.map.DT1;
import com.riiablo.map.DT1Loader;
import com.riiablo.mpq.MPQFileHandleResolver;
import com.riiablo.save.CharData;
import com.riiablo.screen.AudioUnpackerScreen;
import com.riiablo.screen.SplashScreen;

public class Client extends Game {
  private static final String TAG = "Client";

  private static final boolean DEBUG_AUDIO_UNPACKER = !true;
  private static final boolean DEBUG_VIEWPORTS = !true;

  private final Matrix4 BATCH_RESET = new Matrix4();

  private int viewportHeight;
  private float width, height;

  private final Array<Screen> screens = new Array<>(16);
  private final SnapshotArray<ScreenBoundsListener> screenBoundsListeners = new SnapshotArray<>(ScreenBoundsListener.class);

  private FileHandle            home;
  private Viewport              viewport;
  private Viewport              defaultViewport;
  private ScalingViewport       scalingViewport;
  private ExtendViewport        extendViewport;
  private PaletteIndexedBatch   batch;
  private ShaderProgram         shader;
  private ShapeRenderer         shapes;
  private MPQFileHandleResolver mpqs;
  private AssetManager          assets;
  private InputProcessor        input;
  private RenderedConsole       console;
  private GdxCommandManager     commands;
  private GdxCvarManager        cvars;
  private GdxKeyMapper          keys;
  private I18NBundle            bundle;
  private StringTBLs            string;
  private Colors                colors;
  private Palettes              palettes;
  private Colormaps             colormaps;
  private Fonts                 fonts;
  private Files                 files;
  private COFs                  cofs;
  private Textures              textures;
  private Audio                 audio;
  private MusicController       music;
  private Cursor                cursor;
  private CharData              charData;
  private D2                    anim;
  private Metrics               metrics;

  private boolean forceWindowed;
  private boolean forceDrawFps;
  private byte    drawFpsMethod;
  public static final byte FPS_NONE        = 0;
  public static final byte FPS_TOPLEFT     = 1;
  public static final byte FPS_TOPRIGHT    = 2;
  public static final byte FPS_BOTTOMLEFT  = 3;
  public static final byte FPS_BOTTOMRIGHT = 4;
  public static final byte FPS_MAX         = FPS_BOTTOMRIGHT;

  private final GlyphLayout fps = new GlyphLayout();

  private String realm;

  public Client(FileHandle home) {
    this(home, Riiablo.DESKTOP_VIEWPORT_HEIGHT);
  }

  public Client(FileHandle home, int viewportHeight) {
    this.home = home;
    this.viewportHeight = viewportHeight;
  }

  public float width() {
    return width;
  }

  public float height() {
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

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    if (!this.realm.equalsIgnoreCase(realm)) {
      Cvars.Client.Realm.setString(realm);
    }
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
      previousScreen.dispose(); // FIXME: memory leak -- need to fix assets loading/unloading scheme
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
    for (Screen s : screens) s.dispose();
    screens.clear();
    setScreen(screen);
  }

  @Override
  public void create() {
    Riiablo.client = this;
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    // This is needed so that home is in a platform-dependent handle
    Riiablo.home = home = Gdx.files.absolute(home.path());

    boolean usesStdOut = true;
    final OutputStream consoleOut = usesStdOut
        ? System.out
        : Gdx.files.internal("console.out").write(false);

    Riiablo.console = console = RenderedConsole.wrap(consoleOut);
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

    Riiablo.mpqs = mpqs = new MPQFileHandleResolver();
    Riiablo.string = string = new StringTBLs(mpqs);

    Riiablo.assets = assets = new AssetManager();
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
    assets.setLoader(DT1.class, new DT1Loader(mpqs));
    assets.setLoader(DS1.class, new DS1Loader(mpqs));
    assets.setLoader(COF.class, new COFLoader(mpqs));

    Riiablo.palettes = palettes = new Palettes(assets);
    Riiablo.colormaps = colormaps = new Colormaps(assets);
    Riiablo.fonts = fonts = new Fonts(assets);
    Riiablo.files = files = new Files(assets);
    Riiablo.cofs = cofs = new COFs(assets);
    Riiablo.audio = audio = new Audio(assets);
    Riiablo.music = music = new MusicController(assets);

    Riiablo.colors = colors = new Colors();
    Riiablo.bundle = bundle = I18NBundle.createBundle(Gdx.files.internal("lang/Client"));
    Riiablo.textures = textures = new Textures();
    Riiablo.cursor = cursor = new Cursor(assets);
    Riiablo.charData = charData = CharData.obtain();
    Riiablo.anim = anim = D2.loadFromFile(mpqs.resolve("data\\global\\eanimdata.d2"));
    Riiablo.metrics = metrics = new Metrics();

    Collection<Throwable> throwables;
    Riiablo.commands = commands = new GdxCommandManager();
    throwables = Commands.addTo(commands);
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    Riiablo.cvars = cvars = new GdxCvarManager();
    throwables = Cvars.addTo(cvars);
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    Riiablo.keys = keys = new GdxKeyMapper();
    throwables = Keys.addTo(keys);
    for (Throwable t : throwables) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    colors.load();

    Riiablo.input = input = new InputProcessor() {{
      addProcessor(console);
      addProcessor(keys.newInputProcessor());
    }};
    Gdx.input.setInputProcessor(input);
    Gdx.input.setCatchBackKey(true);
    Gdx.input.setCatchMenuKey(true);

    Riiablo.scalingViewport = scalingViewport = new ScalingViewport(Scaling.fillY, (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight() * viewportHeight, viewportHeight);
    Riiablo.extendViewport = extendViewport = new ExtendViewport(Riiablo.DESKTOP_VIEWPORT_MIN_WIDTH, Riiablo.DESKTOP_VIEWPORT_HEIGHT, 0, Riiablo.DESKTOP_VIEWPORT_HEIGHT);
    Riiablo.defaultViewport = defaultViewport = viewportHeight < Riiablo.DESKTOP_VIEWPORT_HEIGHT ? scalingViewport : extendViewport;
    Riiablo.viewport = viewport = defaultViewport;
    ShaderProgram.pedantic = false;
    Riiablo.shader = shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette3.vert"),
        Gdx.files.internal("shaders/indexpalette3.frag"));
    Riiablo.batch = batch = new PaletteIndexedBatch(1024, shader); // TODO: adjust this as needed
    Riiablo.shapes = shapes = new ShapeRenderer();

    bindCvars();

    if ((Gdx.app.getType() == Application.ApplicationType.Android && !home.child("data").exists()) || DEBUG_AUDIO_UNPACKER) {
      setScreen(new AudioUnpackerScreen());
    } else {
      setScreen(new SplashScreen());
    }

    // TODO: This needs to be updated if some shader settings change to match the "new" black
    final float color = 10/255f;//0.025f;
    Gdx.gl.glClearColor(color, color, color, 1.0f);

    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
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

    Cvars.Client.Display.VSync.addStateListener(new CvarStateAdapter<Boolean>() {
      @Override
      public void onChanged(Cvar<Boolean> cvar, Boolean from, Boolean to) {
        Gdx.graphics.setVSync(to);
      }
    });

    Cvars.Client.Realm.addStateListener(new CvarStateAdapter<String>() {
      @Override
      public void onChanged(Cvar<String> cvar, String from, String to) {
        realm = to;
      }
    });
  }

  @Override
  public void resize(int width, int height) {
    this.width  = width;
    this.height = height;
    BATCH_RESET.setToOrtho2D(0, 0, width, height);
    console.resize(width, height);
    //viewport.update(width, height, true);
    scalingViewport.update(width, height, true);
    extendViewport.update(width, height, true);
    super.resize(width, height);
    Gdx.app.debug(TAG, viewport + "; " + width + "x" + height + "; " + viewport.getWorldWidth() + "x" + viewport.getWorldHeight());
  }

  @Override
  public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    audio.update();

    Camera camera = viewport.getCamera();
    camera.update();
    batch.setProjectionMatrix(camera.combined);
    shapes.setProjectionMatrix(camera.combined);

    if (DEBUG_VIEWPORTS) {
      shapes.begin(ShapeRenderer.ShapeType.Filled);
      shapes.setColor(Color.DARK_GRAY);
      shapes.rect(0, 0, 854, 480);
      shapes.setColor(Color.GRAY);
      shapes.rect(0, 0, 640, 480);
      shapes.setColor(Color.BLUE);
      shapes.rect(0, 0, 840, 360);
      shapes.setColor(Color.RED);
      shapes.rect(0, 0, 720, 360);
      shapes.setColor(Color.LIGHT_GRAY);
      shapes.rect(0, 0, 640, 360);
      shapes.setColor(Color.WHITE);
      shapes.rect(0, 0, 100, 100);
      shapes.setColor(Color.GREEN);
      shapes.rect(0, 0, 2, viewport.getWorldHeight());
      shapes.rect(0, viewport.getWorldHeight() - 2, viewport.getWorldWidth(), viewport.getWorldHeight());
      shapes.rect(viewport.getWorldWidth() - 2, 0, 2, viewport.getWorldHeight());
      shapes.rect(0, 0, viewport.getWorldWidth(), 2);
      shapes.end();
    }

    super.render();
    cursor.act(Gdx.graphics.getDeltaTime());
    cursor.render(batch);

    Batch b = batch;
    b.setProjectionMatrix(BATCH_RESET);
    b.begin(); {
      batch.setShader(null);
      if (!Riiablo.assets.update(8)) { // TODO: (1 / 60f) - delta  --  remainder of frame time
        drawLoading(b);
      }

      if (drawFpsMethod > 0 || forceDrawFps) {
        drawFps(b);
      }

      console.render(b);
    } b.end();
  }

  private void drawLoading(Batch b) {
    BitmapFont font = console.getFont();
    if (font == null) return;
    GlyphLayout ellipsis = new GlyphLayout(font, "Loading... " + (int) (Riiablo.assets.getProgress() * 100) + "%");
    font.draw(b, ellipsis, 0, ellipsis.height);
  }

  private void drawFps(Batch b) {
    BitmapFont font = console.getFont();
    if (font == null) return;

    StringBuilder builder = new StringBuilder(64);
    builder
                     .append(String.format("%d FPS", Gdx.graphics.getFramesPerSecond()))
        .append('\n').append(String.format("MEM:  %d / %d MB", Gdx.app.getJavaHeap() / (1 << 20), Runtime.getRuntime().totalMemory() / (1 << 20)))
        .append('\n').append(String.format("Ping: %3d ms", Riiablo.metrics.ping))
        .append('\n').append(String.format("RTT:  %3d ms", Riiablo.metrics.rtt))
        .append('\n').append(String.format("CPU:  %2.1f ms", Riiablo.metrics.cpu))
        .append('\n').append(String.format("GPU:  %2.1f ms", Riiablo.metrics.gpu))
        ;
    fps.setText(font, builder.toString());
    int drawFpsMethod = this.drawFpsMethod;
    if (forceDrawFps && drawFpsMethod == FPS_NONE) {
      drawFpsMethod = FPS_TOPLEFT;
    }

    float x, y;
    switch (drawFpsMethod) {
      case FPS_TOPLEFT:
        x = 2;
        y = Riiablo.viewport.getScreenHeight() - 2;
        break;
      case FPS_TOPRIGHT:
        x = Riiablo.viewport.getScreenWidth() - fps.width;
        y = viewportHeight - 2;
        break;
      case FPS_BOTTOMLEFT:
        x = 2;
        y = fps.height;
        break;
      case FPS_BOTTOMRIGHT:
        x = Riiablo.viewport.getScreenWidth() - fps.width;
        y = fps.height;
        break;
      default:
        Gdx.app.error(TAG, "Invalid draw fps method: " + drawFpsMethod);
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
    Riiablo.client = this;
    Riiablo.home = home;
    Riiablo.viewport = viewport;
    Riiablo.defaultViewport = defaultViewport;
    Riiablo.scalingViewport = scalingViewport;
    Riiablo.extendViewport = extendViewport;
    Riiablo.batch = batch;
    Riiablo.shader = shader;
    Riiablo.shapes = shapes;
    Riiablo.mpqs = mpqs;
    Riiablo.assets = assets;
    Riiablo.input = input;
    Riiablo.console = console;
    Riiablo.commands = commands;
    Riiablo.cvars = cvars;
    Riiablo.keys = keys;
    Riiablo.bundle = bundle;
    Riiablo.string = string;
    Riiablo.colors = colors;
    Riiablo.palettes = palettes;
    Riiablo.colormaps = colormaps;
    Riiablo.fonts = fonts;
    Riiablo.files = files;
    Riiablo.cofs = cofs;
    Riiablo.textures = textures;
    Riiablo.audio = audio;
    Riiablo.music = music;
    Riiablo.cursor = cursor;
    Riiablo.charData = charData;
    Riiablo.anim = anim;
    Riiablo.metrics = metrics;
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

  // TODO: designed for camera bounds on android for soft keyboard updates, but will change to be
  //       camera world bounds later (i.e., to layout UI components on a TV without clipping edges)
  public void updateScreenBounds(int x, int y, int width, int height) {
    if (viewport == null) return;
    Vector2 coords = new Vector2(x, y);
    viewport.unproject(coords);
    /**
     * FIXME: Find out why inverse height is the correct one. Android passes in the height of the
     *        keyboard in screen space, which when unprojected should result in equivalent location
     *        in world space? Below works for my device, need to double check
     */
    coords.y = viewportHeight - coords.y;
    ScreenBoundsListener[] listeners = screenBoundsListeners.begin();
    for (ScreenBoundsListener l : listeners) {
      if (l != null) l.updateScreenBounds(coords.x, coords.y, width, height);
    }
    screenBoundsListeners.end();
  }

  public void addScreenBoundsListener(ScreenBoundsListener l) {
    screenBoundsListeners.add(l);
  }

  public void removeScreenBoundsListener(ScreenBoundsListener l) {
    screenBoundsListeners.removeValue(l, true);
  }

  public interface ScreenBoundsListener {
    void updateScreenBounds(float x, float y, float width, float height);
  }
}
