package com.riiablo.map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.riiablo.COFs;
import com.riiablo.CharacterClass;
import com.riiablo.Colors;
import com.riiablo.Files;
import com.riiablo.Fonts;
import com.riiablo.Palettes;
import com.riiablo.Riiablo;
import com.riiablo.Textures;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC6;
import com.riiablo.codec.DCC;
import com.riiablo.codec.FontTBL;
import com.riiablo.codec.Index;
import com.riiablo.codec.Palette;
import com.riiablo.codec.StringTBLs;
import com.riiablo.codec.TXT;
import com.riiablo.entity.Direction;
import com.riiablo.entity.Engine;
import com.riiablo.entity.Entity;
import com.riiablo.entity.Player;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.BitmapFontLoader;
import com.riiablo.loader.COFLoader;
import com.riiablo.loader.DC6Loader;
import com.riiablo.loader.DCCLoader;
import com.riiablo.loader.IndexLoader;
import com.riiablo.loader.PaletteLoader;
import com.riiablo.loader.TXTLoader;
import com.riiablo.mpq.MPQFileHandleResolver;

import box2dLight.PointLight;
import box2dLight.RayHandler;

public class WallAggregatorTool extends ApplicationAdapter {
  private static final String TAG = "WallAggregatorTool";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = TAG;
    config.resizable = true;
    config.width = 1280;
    config.height = 720;
    config.vSyncEnabled = false;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new WallAggregatorTool(args[0]), config);
  }

  FileHandle home;
  OrthographicCamera camera;
  Viewport viewport;
  World world;
  Box2DDebugRenderer box2dDebug;
  Map map;
  MapRenderer mapRenderer;
  Entity src;
  Batch batch;
  Body playerBody;
  RayHandler rayHandler;
  PointLight light;

  WallAggregatorTool(String home) {
    this.home = new FileHandle(home);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    Riiablo.home = home = Gdx.files.absolute(home.path());
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.assets = new AssetManager();
    Riiablo.assets.setLoader(TXT.class, new TXTLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(DS1.class, new DS1Loader(Riiablo.mpqs));
    Riiablo.assets.setLoader(DT1.class, new DT1Loader(Riiablo.mpqs));
    Riiablo.assets.setLoader(COF.class, new COFLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(DC6.class, new DC6Loader(Riiablo.mpqs));
    Riiablo.assets.setLoader(DCC.class, new DCCLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(Palette.class, new PaletteLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(Index.class, new IndexLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(TXT.class, new TXTLoader(Riiablo.mpqs));
    Riiablo.assets.setLoader(FontTBL.BitmapFont.class, new BitmapFontLoader(Riiablo.mpqs));
    Texture.setAssetManager(Riiablo.assets);

    Riiablo.files = new Files(Riiablo.assets);
    Riiablo.fonts = new Fonts(Riiablo.assets);
    Riiablo.colors = new Colors();
    Riiablo.textures = new Textures();
    Riiablo.string = new StringTBLs(Riiablo.mpqs);
    Riiablo.palettes = new Palettes(Riiablo.assets);
    Riiablo.cofs = new COFs(Riiablo.assets);//COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a1.d2"));

    batch = new SpriteBatch();

    ShaderProgram.pedantic = false;
    Riiablo.shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette3.vert"),
        Gdx.files.internal("shaders/indexpalette3.frag"));
    if (!Riiablo.shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + Riiablo.shader.getLog());
    }

    Riiablo.batch = new PaletteIndexedBatch(2048, Riiablo.shader);
    Riiablo.shapes = new ShapeRenderer();

    camera = new OrthographicCamera();
    camera.near = -1024;
    camera.far  =  1024;
    camera.rotate(Vector3.X, 60);
    camera.rotate(Vector3.Z, 45);
    camera.zoom = 0.044f;

    viewport = new ScreenViewport(camera);
    world = new World(new Vector2(), true);
    box2dDebug = new Box2DDebugRenderer();

    Riiablo.engine = new Engine();
    Riiablo.assets.setLoader(Map.class, new MapLoader(Riiablo.mpqs));
    Riiablo.assets.load("Act 1", Map.class, MapLoader.MapParameters.of(0, 0, 0));
    Riiablo.assets.finishLoading();

    map = Riiablo.assets.get("Act 1");

    GridPoint2 origin = map.find(Map.ID.TOWN_ENTRY_1);

    src = new Player("null", CharacterClass.BARBARIAN);
    src.position().set(origin.x, origin.y);

    mapRenderer = new MapRenderer(Riiablo.batch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    mapRenderer.resize();
    mapRenderer.setMap(map);
    mapRenderer.setEntities(Riiablo.engine);
    mapRenderer.setSrc(src);

    BodyDef playerDef = new BodyDef();
    playerDef.type = BodyDef.BodyType.DynamicBody;
    playerDef.fixedRotation = true;
    playerDef.position.set(origin.x, -origin.y);

    CircleShape playerShape = new CircleShape();
    playerShape.setRadius(0.9f);

    playerBody = world.createBody(playerDef);
    playerBody.createFixture(playerShape, 0);

    playerShape.dispose();

    rayHandler = new RayHandler(world);
    rayHandler.setAmbientLight(0.5f);
    rayHandler.setShadows(false);

    light = new PointLight(rayHandler, 360);
    light.attachToBody(playerBody);
    light.setDistance(32);
    light.setSoft(false);
    light.setSoftnessLength(0);
    light.setColor(1, 1, 1, 0.5f);
    light.setContactFilter(
        (short) DT1.Tile.FLAG_BLOCK_LIGHT_LOS,
        (short) 0,
        (short) (DT1.Tile.FLAG_BLOCK_LIGHT_LOS));

    IntMap<Filter> filters = new IntMap<>();

    for (int y = 0; y < 280; y++) {
      for (int x = 0; x < 200; x++) {
        int flags = map.flags(x, y);
        if (flags != 0) {
          BodyDef def = new BodyDef();
          def.type = BodyDef.BodyType.StaticBody;
          def.position.set(x, -(y));

          PolygonShape shape = new PolygonShape();
          shape.setAsBox(0.5f, 0.5f);

          Filter filter = filters.get(flags);
          if (filter == null) {
            filters.put(flags, filter = new Filter());
            filter.categoryBits = 0xFF;
            filter.maskBits     = (short) flags;
            filter.groupIndex   = 0;
          }

          Body body = world.createBody(def);
          Fixture f = body.createFixture(shape, 0);
          f.setFilterData(filter);

          shape.dispose();
        }
      }
    }

    Gdx.input.setInputProcessor(new InputAdapter() {
      @Override
      public boolean scrolled(int amount) {
        final float AMOUNT = 0.001f;
        switch (amount) {
          case -1:
            camera.zoom = MathUtils.clamp(camera.zoom - AMOUNT, 0.01f, 2);
            camera.update();
            break;
          case 1:
            camera.zoom = MathUtils.clamp(camera.zoom + AMOUNT, 0.01f, 2);
            camera.update();
            break;
        }
        System.out.println(camera.zoom);
        return super.scrolled(amount);
      }
    });
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height);
  }

  final Vector2 vec2a = new Vector2();
  final Vector2 vec2b = new Vector2();

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    if (Gdx.input.isTouched()) {
      final float VELOCITY = 16;
      vec2a.set(camera.viewportWidth / 2, camera.viewportHeight / 2);
      vec2b.set(Gdx.input.getX(), Gdx.input.getY());

      vec2b.sub(vec2a).nor();
      float rad = Direction.snapToDirection(vec2b.angleRad(), 32);
      vec2a.nor().setAngleRad(rad);
      vec2a.scl(VELOCITY).rotate(-45);
      vec2a.y = -vec2a.y;
      playerBody.setLinearVelocity(vec2a);
      playerBody.setTransform(playerBody.getPosition(), vec2a.angleRad());
    } else {
      playerBody.setLinearVelocity(0, 0);
    }

//    Riiablo.shapes.setProjectionMatrix(camera.combined);
//    Riiablo.shapes.begin(ShapeRenderer.ShapeType.Filled);
//    Riiablo.shapes.rect(2, 2, 8, 8);
//    Riiablo.shapes.rect(2, -10, 8, 8);
//    Riiablo.shapes.rect(-10, 2, 8, 8);
//    Riiablo.shapes.rect(-10, -10, 8, 8);
//    Riiablo.shapes.end();

    world.step(Gdx.graphics.getDeltaTime(), 6, 2);
    camera.position.set(playerBody.getPosition(), 0);
    camera.update();
    src.position().set(playerBody.getPosition().x, -playerBody.getPosition().y);

    Riiablo.batch.begin(Riiablo.palettes.act1);
    mapRenderer.update();
    mapRenderer.draw(Gdx.graphics.getDeltaTime());
    Riiablo.batch.end();

    box2dDebug.render(world, camera.combined);

    rayHandler.setCombinedMatrix(camera);
    rayHandler.updateAndRender();

    batch.begin();
    Riiablo.fonts.consolas16.draw(batch, String.valueOf(Gdx.graphics.getFramesPerSecond()), 0, Gdx.graphics.getHeight());
    batch.end();
  }

  @Override
  public void dispose() {
    batch.dispose();
    map.dispose();
    world.dispose();
    rayHandler.dispose();
    Riiablo.assets.dispose();
    Riiablo.palettes.dispose();
    Riiablo.textures.dispose();
    Riiablo.batch.dispose();
    Riiablo.shader.dispose();
    Riiablo.shapes.dispose();
  }
}
