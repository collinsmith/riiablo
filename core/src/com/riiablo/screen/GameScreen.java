package com.riiablo.screen;

import com.artemis.Aspect;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.managers.TagManager;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.riiablo.Client;
import com.riiablo.Cvars;
import com.riiablo.Keys;
import com.riiablo.Riiablo;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.Sounds;
import com.riiablo.cvar.Cvar;
import com.riiablo.cvar.CvarStateAdapter;
import com.riiablo.engine.Engine;
import com.riiablo.engine.EntityFactory;
import com.riiablo.engine.client.AnimationStepper;
import com.riiablo.engine.client.AutoInteracter;
import com.riiablo.engine.client.ClientEntityFactory;
import com.riiablo.engine.client.ClientItemManager;
import com.riiablo.engine.client.CofAlphaHandler;
import com.riiablo.engine.client.CofLayerCacher;
import com.riiablo.engine.client.CofLayerLoader;
import com.riiablo.engine.client.CofLayerUnloader;
import com.riiablo.engine.client.CofLoader;
import com.riiablo.engine.client.CofResolver;
import com.riiablo.engine.client.CofTransformHandler;
import com.riiablo.engine.client.CofUnloader;
import com.riiablo.engine.client.CursorMovementSystem;
import com.riiablo.engine.client.DialogManager;
import com.riiablo.engine.client.DirectionResolver;
import com.riiablo.engine.client.HoveredManager;
import com.riiablo.engine.client.ItemEffectManager;
import com.riiablo.engine.client.ItemLoader;
import com.riiablo.engine.client.LabelManager;
import com.riiablo.engine.client.MenuManager;
import com.riiablo.engine.client.MissileLoader;
import com.riiablo.engine.client.MonsterLabelManager;
import com.riiablo.engine.client.NetworkIdManager;
import com.riiablo.engine.client.NetworkedClientItemManager;
import com.riiablo.engine.client.SelectableManager;
import com.riiablo.engine.client.SoundEmitterHandler;
import com.riiablo.engine.client.WarpSubstManager;
import com.riiablo.engine.client.ZoneChangeTracker;
import com.riiablo.engine.client.ZoneEntryDisplayer;
import com.riiablo.engine.client.debug.Box2DDebugger;
import com.riiablo.engine.client.debug.PathDebugger;
import com.riiablo.engine.client.debug.PathfindDebugger;
import com.riiablo.engine.client.debug.RenderSystemDebugger;
import com.riiablo.engine.server.AIStepper;
import com.riiablo.engine.server.AngularVelocity;
import com.riiablo.engine.server.AnimDataResolver;
import com.riiablo.engine.server.AnimStepper;
import com.riiablo.engine.server.Box2DDisposer;
import com.riiablo.engine.server.Box2DSynchronizerPost;
import com.riiablo.engine.server.Box2DSynchronizerPre;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.ItemInteractor;
import com.riiablo.engine.server.ItemManager;
import com.riiablo.engine.server.ObjectCollisionUpdater;
import com.riiablo.engine.server.ObjectInitializer;
import com.riiablo.engine.server.ObjectInteractor;
import com.riiablo.engine.server.Pathfinder;
import com.riiablo.engine.server.PlayerItemHandler;
import com.riiablo.engine.server.SequenceHandler;
import com.riiablo.engine.server.VelocityModeChanger;
import com.riiablo.engine.server.WarpInteractor;
import com.riiablo.engine.server.ZoneMovementModesChanger;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.engine.server.event.ZoneChangeEvent;
import com.riiablo.graphics.PaletteIndexedColorDrawable;
import com.riiablo.item.Item;
import com.riiablo.key.MappedKey;
import com.riiablo.key.MappedKeyStateAdapter;
import com.riiablo.map.Act1MapBuilder;
import com.riiablo.map.Box2DPhysics;
import com.riiablo.map.Map;
import com.riiablo.map.MapManager;
import com.riiablo.map.RenderSystem;
import com.riiablo.save.CharData;
import com.riiablo.screen.panel.CharacterPanel;
import com.riiablo.screen.panel.ControlPanel;
import com.riiablo.screen.panel.CubePanel;
import com.riiablo.screen.panel.EscapeController;
import com.riiablo.screen.panel.EscapePanel;
import com.riiablo.screen.panel.HirelingPanel;
import com.riiablo.screen.panel.InventoryPanel;
import com.riiablo.screen.panel.MobileControls;
import com.riiablo.screen.panel.MobilePanel;
import com.riiablo.screen.panel.QuestsPanel;
import com.riiablo.screen.panel.SpellsPanel;
import com.riiablo.screen.panel.SpellsQuickPanel;
import com.riiablo.screen.panel.StashPanel;
import com.riiablo.screen.panel.WaygatePanel;
import com.riiablo.widget.TextArea;

import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.plugin.ProfilerPlugin;

public class GameScreen extends ScreenAdapter implements GameLoadingScreen.Loadable {
  private static final String TAG = "GameScreen";
  private static final boolean DEBUG          = true;
  private static final boolean DEBUG_TOUCHPAD = !true;
  private static final boolean DEBUG_MOBILE   = !true;

  private static final boolean PRECACHE_CURSOR = true;
  private static final boolean PRECACHE_ITEMS = true;

  private static final int[] ITEMS = {
      205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223,
      224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242,
      243, 244, 245, 246, 247, 248, 249, 250
  };
  private static final int[] CURSORS = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

  private final Vector2 tmpVec2 = new Vector2();
  private final Vector2 tmpVec2b = new Vector2();

  final AssetDescriptor<DC6> loadingscreenDescriptor = new AssetDescriptor<>("data\\local\\ui\\loadingscreen.dc6", DC6.class);
  final AssetDescriptor<Sound> windowopenDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\windowopen.wav", Sound.class);

  final AssetDescriptor<Texture> touchpadBackgroundDescriptor = new AssetDescriptor<>("textures/touchBackground.png", Texture.class);
  final AssetDescriptor<Texture> touchpadKnobDescriptor = new AssetDescriptor<>("textures/touchKnob.png", Texture.class);
  Touchpad touchpad;

  final Array<AssetDescriptor> preloadedAssets = new Array<AssetDescriptor>() {{
    add(loadingscreenDescriptor);
    add(windowopenDescriptor);
    if (Gdx.app.getType() == Application.ApplicationType.Android || DEBUG_TOUCHPAD) {
      add(touchpadBackgroundDescriptor);
      add(touchpadKnobDescriptor);
    }
    if (PRECACHE_CURSOR) {
      for (int id : CURSORS) {
        Sounds.Entry sound = Riiablo.files.Sounds.get(id);
        add(new AssetDescriptor<>("data\\global\\sfx\\" + sound.FileName, Sound.class));
      }
    }
    if (PRECACHE_ITEMS) {
      for (int id : ITEMS) {
        Sounds.Entry sound = Riiablo.files.Sounds.get(id);
        add(new AssetDescriptor<>("data\\global\\sfx\\" + sound.FileName, Sound.class));
      }
    }
  }};


  Stage stage;
  Stage scaledStage;
  Viewport viewport;
  GameLoadingScreen loadingScreen;
  boolean created;
  boolean isDebug;
  MappedKeyStateAdapter debugKeyListener = new MappedKeyStateAdapter() {
    @Override
    public void onPressed(MappedKey key, int keycode) {
      RenderSystemDebugger debugger = engine.getSystem(RenderSystemDebugger.class);
      debugger.setEnabled(!debugger.isEnabled());
    }
  };

  World engine;
  EntityFactory factory;
  RenderSystem renderer;
  public int player;
  CharData charData;
  Socket socket;

  Map map;
  MapManager mapManager;
  IsometricCamera iso;
  InputProcessor testingInputProcessor;

  ClientItemManager itemController;

  public EscapePanel escapePanel;
  public ControlPanel controlPanel;
  MobilePanel mobilePanel;
  MobileControls mobileControls;

  Client.ScreenBoundsListener screenBoundsListener;
  public TextArea input;
  public TextArea output;

  Actor left;
  Actor right;
  MappedKeyStateAdapter mappedKeyStateListener;
  public InventoryPanel inventoryPanel;
  public CharacterPanel characterPanel;
  public SpellsPanel spellsPanel;
  public StashPanel stashPanel;
  public HirelingPanel hirelingPanel;
  public WaygatePanel waygatePanel;
  public QuestsPanel questsPanel;
  public CubePanel cubePanel;
  public SpellsQuickPanel spellsQuickPanelL;
  public SpellsQuickPanel spellsQuickPanelR;

  Actor details;

  /**
   * FIXME: there has to be a better way of doing this -- some way to layout the stage (or relevant
   *        parts) and get the coordinates I need. Right now it flashes the control panel for a
   *        frame before hiding it.
   */
  boolean firstRender = true;

  @Override
  public Array<AssetDescriptor> getDependencies() {
    return preloadedAssets;
  }

  public GameScreen(CharData charData) {
    this(charData, null);
  }

  public GameScreen(CharData charData, Socket socket) {
    this.charData = charData;
    this.socket = socket;

    Riiablo.viewport = viewport = Riiablo.extendViewport;
    stage = new Stage(viewport, Riiablo.batch);

    input = new TextArea("", new TextArea.TextFieldStyle() {{
      this.font = Riiablo.fonts.fontformal12;
      this.fontColor = Riiablo.colors.white;
      this.background = new PaletteIndexedColorDrawable(Riiablo.colors.modal50);
      this.cursor = new TextureRegionDrawable(Riiablo.textures.white);

      float padding = 4;
      background.setLeftWidth(padding);
      background.setTopHeight(padding);
      background.setRightWidth(padding);
      background.setBottomHeight(padding);
    }}) {
      {
        writeEnters = false;
      }

      @Override
      public void setVisible(boolean visible) {
        if (!visible) input.setText("");
        else {
          stage.setKeyboardFocus(this);
          Gdx.input.setOnscreenKeyboardVisible(true);
        }
        super.setVisible(visible);
      }
    };
    //input.setDebug(true);
    input.setSize(stage.getWidth() * 0.75f, Riiablo.fonts.fontformal12.getLineHeight() * 3);
    input.setPosition(stage.getWidth() / 2 - input.getWidth() / 2, 100);
    input.setAlignment(Align.topLeft);
    input.setVisible(false);
    if (Gdx.app.getType() != Application.ApplicationType.Android) input.setTouchable(Touchable.disabled);
    stage.addActor(input);

    output = new TextArea("", new TextArea.TextFieldStyle() {{
      this.font = Riiablo.fonts.fontformal12;
      this.fontColor = Riiablo.colors.white;
      this.cursor = new TextureRegionDrawable(Riiablo.textures.white);
    }});
    //output.setDebug(true);
    output.setSize(stage.getWidth() * 0.75f, Riiablo.fonts.fontformal12.getLineHeight() * 8);
    output.setPosition(10, stage.getHeight() - 10, Align.topLeft);
    output.setAlignment(Align.topLeft);
    output.setDisabled(true);
    output.setVisible(true);
    output.setTouchable(Touchable.disabled);
    stage.addActor(output);

    escapePanel = new EscapePanel();
    stage.addActor(escapePanel);

    controlPanel = new ControlPanel();
    controlPanel.setPosition(stage.getWidth() / 2, 0, Align.bottom | Align.center);
    controlPanel.pack();
    stage.addActor(controlPanel);

    if (DEBUG_MOBILE || Gdx.app.getType() == Application.ApplicationType.Android) {
      mobilePanel = new MobilePanel();
      mobilePanel.setPosition(0, 0);
      mobilePanel.setWidth(stage.getWidth());
      stage.addActor(mobilePanel);

      mobileControls = new MobileControls();
      mobileControls.setPosition(
          stage.getWidth() - mobileControls.getWidth(),
          mobilePanel.getHeight());
      stage.addActor(mobileControls);
    }

    inventoryPanel = new InventoryPanel();
    inventoryPanel.setPosition(
        stage.getWidth() - inventoryPanel.getWidth(),
        stage.getHeight() - inventoryPanel.getHeight());
    stage.addActor(inventoryPanel);

    hirelingPanel = new HirelingPanel();
    hirelingPanel.setPosition(0, stage.getHeight() - hirelingPanel.getHeight());
    stage.addActor(hirelingPanel);

    stashPanel = new StashPanel();
    stashPanel.setPosition(0, stage.getHeight() - stashPanel.getHeight());
    stage.addActor(stashPanel);

    cubePanel = new CubePanel();
    cubePanel.setPosition(0, stage.getHeight() - cubePanel.getHeight());
    stage.addActor(cubePanel);

    characterPanel = new CharacterPanel();
    characterPanel.setPosition(0, stage.getHeight() - characterPanel.getHeight());
    stage.addActor(characterPanel);

    questsPanel = new QuestsPanel();
    questsPanel.setPosition(0, stage.getHeight() - questsPanel.getHeight());
    stage.addActor(questsPanel);

    waygatePanel = new WaygatePanel();
    waygatePanel.setPosition(0, stage.getHeight() - waygatePanel.getHeight());
    stage.addActor(waygatePanel);

    spellsPanel = new SpellsPanel();
    spellsPanel.setPosition(
        stage.getWidth() - spellsPanel.getWidth(),
        stage.getHeight() - spellsPanel.getHeight());
    stage.addActor(spellsPanel);

    spellsQuickPanelL = new SpellsQuickPanel(controlPanel.getLeftSkill(), true);
    spellsQuickPanelL.setPosition(0, 100, Align.bottomLeft);
    spellsQuickPanelL.setVisible(false);
    stage.addActor(spellsQuickPanelL);

    spellsQuickPanelR = new SpellsQuickPanel(controlPanel.getRightSkill(), false);
    spellsQuickPanelR.setPosition(stage.getWidth(), 100, Align.bottomRight);
    spellsQuickPanelR.setVisible(false);
    stage.addActor(spellsQuickPanelR);

    mappedKeyStateListener = new MappedKeyStateAdapter() {
      @Override
      public void onPressed(MappedKey key, int keycode) {
        if (input.isVisible() && (key != Keys.Enter && key != Keys.Esc)) {
          return;
        }

        if (key == Keys.Esc) {
          if (escapePanel.isVisible()) {
            escapePanel.setVisible(false);
          } else if (input.isVisible()) {
            input.setVisible(false);
          } else if (left != null || right != null) {
            setLeftPanel(null);
            setRightPanel(null);
          } else {
            escapePanel.setVisible(true);
          }
        } else if (key == Keys.Enter) {
          boolean visible = !input.isVisible();
          if (!visible) {
            String text = input.getText();
            if (!text.isEmpty()) {
              Gdx.app.debug(TAG, text);
              //Message message = new Message(player.stats.getName(), text);
              //out.println(Packets.build(message));
              output.appendText(text);
              output.appendText("\n");
              input.setText("");
            }
          }

          input.setVisible(visible);
          if (visible) {
            input.setText("");
            stage.setKeyboardFocus(input);
          }
        } else if (key == Keys.Inventory) {
          setRightPanel(inventoryPanel.isVisible() ? null : inventoryPanel);
        } else if (key == Keys.Character) {
          setLeftPanel(characterPanel.isVisible() ? null : characterPanel);
        } else if (key == Keys.Stash) {
          setLeftPanel(stashPanel.isVisible() ? null : stashPanel);
        } else if (key == Keys.Hireling) {
          setLeftPanel(hirelingPanel.isVisible() ? null : hirelingPanel);
        } else if (key == Keys.Spells) {
          setRightPanel(spellsPanel.isVisible() ? null : spellsPanel);
        } else if (key == Keys.Quests) {
          setLeftPanel(questsPanel.isVisible() ? null : questsPanel);
        } else if (key == Keys.SwapWeapons) {
          Riiablo.charData.getItems().alternate();
        }
      }
    };

    testingInputProcessor = new InputAdapter() {
      private final float ZOOM_AMOUNT = 0.1f;

      @Override
      public boolean scrolled(int amount) {
        switch (amount) {
          case -1:
            if (UIUtils.ctrl()) {
              renderer.zoom(Math.max(0.20f, renderer.zoom() - ZOOM_AMOUNT));
            }

            break;
          case 1:
            if (UIUtils.ctrl()) {
              renderer.zoom(Math.min(5.00f, renderer.zoom() + ZOOM_AMOUNT));
            }

            break;
          default:
        }

        return true;
      }

      @Override
      public boolean keyDown(int keycode) {
        switch (keycode) {
          case Input.Keys.TAB:
            if (UIUtils.shift()) {
              RenderSystem.RENDER_DEBUG_WALKABLE = RenderSystem.RENDER_DEBUG_WALKABLE == 0 ? 1 : 0;
            } else {
              RenderSystem.RENDER_DEBUG_GRID++;
              if (RenderSystem.RENDER_DEBUG_GRID > RenderSystem.DEBUG_GRID_MODES) {
                RenderSystem.RENDER_DEBUG_GRID = 0;
              }
            }
            return true;

          case Input.Keys.F9: {
            PathDebugger debugger = engine.getSystem(PathDebugger.class);
            debugger.setEnabled(!debugger.isEnabled());
            return true;
          }

          case Input.Keys.F11: {
            Box2DDebugger debugger = engine.getSystem(Box2DDebugger.class);
            debugger.setEnabled(!debugger.isEnabled());
            return true;
          }

          case Input.Keys.F10: {
            PathfindDebugger debugger = engine.getSystem(PathfindDebugger.class);
            debugger.setEnabled(!debugger.isEnabled());
            return true;
          }

          default:
            return false;
        }
      }
    };

    map = new Map(0, 0);
    mapManager = new MapManager();
    renderer = new RenderSystem(Riiablo.batch, map);
    iso = renderer.iso();
    scaledStage = new Stage(new ScreenViewport(iso), Riiablo.batch);
    factory = new ClientEntityFactory();
    itemController = socket == null ? new ClientItemManager() : new NetworkedClientItemManager();

    WorldConfiguration config = getWorldConfiguration();
    config
        .register("iso", iso)
        .register("map", map)
        .register("factory", factory)
        .register("itemController", itemController)
        .register("batch", Riiablo.batch)
        .register("shapes", Riiablo.shapes)
        .register("stage", stage)
        .register("scaledStage", scaledStage)
        .register("input", input)
        .register("output", output)
        ;
    if (socket != null) config.register("client.socket", socket);
    engine = Riiablo.engine = new World(config);

    // hacked until I can rewrite into proper system
    engine.inject(map);
    engine.inject(Act1MapBuilder.INSTANCE);

    if (mobileControls != null) engine.inject(mobileControls);

    injectPanels();

    // TODO: better place to put this?
    charData.getItems().addLocationListener(Riiablo.cursor);
    charData.getMerc().getItems().addLocationListener(Riiablo.cursor);

    // FIXME: #75 Initial CharData update event
    charData.update();

    loadingScreen = new GameLoadingScreen(map, getDependencies());
  }

  private void injectPanels() {
    engine.inject(inventoryPanel);
    engine.inject(hirelingPanel);
    engine.inject(controlPanel);
    engine.inject(cubePanel);
    engine.inject(stashPanel);
// TODO: maybe it would be better to do more like?:
//    for (Actor actor : stage.getActors()) {
//      engine.inject(actor);
//    }
  }

  protected WorldConfiguration getWorldConfiguration() {
    return getWorldConfigurationBuilder().build();
  }

  protected WorldConfigurationBuilder getWorldConfigurationBuilder() {
    WorldConfigurationBuilder builder = new WorldConfigurationBuilder()
        .with(new NetworkIdManager())
        .with(new EventSystem())
        .with(new TagManager())
        .with(mapManager)
        .with(itemController, new ItemManager())
        .with(new CofManager())
        .with(new ObjectInitializer())
        .with(new ObjectInteractor(), new WarpInteractor(), new ItemInteractor())
        .with(new MenuManager(), new DialogManager())
        ;
    if (!DEBUG_TOUCHPAD && Gdx.app.getType() == Application.ApplicationType.Desktop) {
      builder.with(new CursorMovementSystem());
    }
    if (socket == null) {
      builder.with(new AIStepper());
    }
    builder
        .with(new Pathfinder())

        .with(new SoundEmitterHandler())

        .with(factory)
        .with(new AnimDataResolver())
        .with(new AnimStepper())
        .with(new CofUnloader(), new CofResolver(), new CofLoader())
        .with(new CofLayerUnloader(), new CofLayerLoader(), new CofLayerCacher())
        .with(new CofAlphaHandler(), new CofTransformHandler())
        .with(new ItemLoader())
        .with(new MissileLoader())
        .with(new AnimationStepper())
        .with(new ObjectCollisionUpdater())

        .with(new VelocityModeChanger());
//        .with(new VelocityAdder());
    if (socket != null) {
      // FIXME: crash when changing acts in multiplayer
      builder.with(new Box2DDisposer());
    }
    builder
        .with(new Box2DSynchronizerPre())
        .with(new Box2DPhysics(1 / 60f))
        .with(new Box2DSynchronizerPost())

        .with(new ZoneChangeTracker())
        .with(new ZoneMovementModesChanger())
        .with(new ZoneEntryDisplayer())

        .with(new SelectableManager())
        .with(new HoveredManager())
        .with(new WarpSubstManager())
        ;
    if (DEBUG_TOUCHPAD || Gdx.app.getType() == Application.ApplicationType.Android) {
      builder.with(new AutoInteracter());
    }
    builder
        .with(new PlayerItemHandler())

        .with(new SequenceHandler())

        .with(new AngularVelocity())
        .with(new DirectionResolver())

        .with(renderer)
        .with(new LabelManager())
        .with(new MonsterLabelManager())

        .with(new ItemEffectManager())

        .with(new PathDebugger())
        .with(new Box2DDebugger())
        .with(new PathfindDebugger())
        .with(new RenderSystemDebugger())

        .dependsOn(ProfilerPlugin.class)
        ;
    return builder;
  }

  public void create() {
    if (created) return;
    created = true;

    isDebug = DEBUG && Gdx.app.getType() == Application.ApplicationType.Desktop;

    if (DEBUG_TOUCHPAD || Gdx.app.getType() == Application.ApplicationType.Android) {
      touchpad = new Touchpad(10, new Touchpad.TouchpadStyle() {{
        //background = new TextureRegionDrawable(Riiablo.assets.get(touchpadBackgroundDescriptor));
        background = null;
        knob = new TextureRegionDrawable(Riiablo.assets.get(touchpadKnobDescriptor));
      }});
      touchpad.setSize(164, 164);
      touchpad.setPosition(0, mobilePanel != null ? mobilePanel.getHeight() : 0);
      stage.addActor(touchpad);
      if (!DEBUG_TOUCHPAD) touchpad.toBack();
    }

    // TODO: sort children based on custom indexes
    controlPanel.toFront();
    output.toFront();
    if (mobilePanel != null) mobilePanel.toFront();
//  if (mobileControls != null) mobileControls.toFront();
    if (touchpad != null) touchpad.toBack();
    input.toFront();
    escapePanel.toFront();

    if (Gdx.app.getType() == Application.ApplicationType.Android
     || Riiablo.defaultViewport.getWorldHeight() == Riiablo.MOBILE_VIEWPORT_HEIGHT) {
      renderer.zoom(Riiablo.MOBILE_VIEWPORT_HEIGHT / (float) Gdx.graphics.getHeight());
    } else {
      renderer.zoom(Riiablo.DESKTOP_VIEWPORT_HEIGHT / (float) Gdx.graphics.getHeight());
    }
    renderer.resize();
  }

  @Override
  public void resume() {
    Riiablo.engine = engine;
    Riiablo.game = this;
  }

  @Override
  public void render(float delta) {
    // TODO: move to a separate system TouchpadMovementSystem
    if (touchpad != null) {
      tmpVec2.set(touchpad.getKnobPercentX(), touchpad.getKnobPercentY()).nor();
      if (tmpVec2.isZero()) {
        Velocity velocity = engine.getMapper(Velocity.class).get(player);
        velocity.velocity.setZero();
      } else {
        Vector2 position = engine.getMapper(Position.class).get(player).position;
        iso.toScreen(tmpVec2b.set(position)).add(tmpVec2);
        iso.toWorld(tmpVec2b).sub(position);

        Angle angle = engine.getMapper(Angle.class).get(player);
        angle.target.set(tmpVec2b).nor();

        Velocity velocity = engine.getMapper(Velocity.class).get(player);
        velocity.velocity.set(tmpVec2b);

        engine.getSystem(DialogManager.class).setDialog(null);
        engine.getSystem(MenuManager.class).setMenu(null, Engine.INVALID_ENTITY);
      }
    }

    Riiablo.assets.update();
    engine.setDelta(delta);
    engine.process();

    scaledStage.act(delta);
    scaledStage.draw();

    details = null;
    stage.act(delta);
    stage.draw();
    if (firstRender) {
      firstRender = false;
      for (Actor actor : stage.getActors()) {
        if (actor == controlPanel) continue; // FIXME: renders over belt
        if (actor instanceof EscapeController) {
          EscapeController escapeController = (EscapeController) actor;
          Actor escape = escapeController.getEscapeButton();
          escape.localToStageCoordinates(tmpVec2.set(0, 0));
          escape.setPosition(tmpVec2.x, tmpVec2.y);
          stage.addActor(escape);
        }
      }

      controlPanel.setMinipanelVisible(false);
    }

    if (details != null) {
      Riiablo.batch.begin();
      details.draw(Riiablo.batch, 1);
      Riiablo.batch.end();
    }

    //3 modes
    //  client and server (single player)
    //    pausing client pauses engine
    //    possibility of interpolating server frames
    //  client hosts server (tcp multiplayer)
    //    similar to previous -- pausing doesn't pause engine
    //  client connects server (bnet)
    //    client maintains copy of engine

    //map object
    //  reset using seed
    //  list of used dt1s
    //  list of used ds1s
  }

  @Override
  public void show() {
    if (map.getAct() == -1) {
      setAct(0);
      return;
    }

    create();

    Riiablo.game = this;
    Keys.DebugMode.addStateListener(debugKeyListener);
    Keys.Esc.addStateListener(mappedKeyStateListener);
    Keys.Enter.addStateListener(mappedKeyStateListener);
    Keys.Inventory.addStateListener(mappedKeyStateListener);
    Keys.Character.addStateListener(mappedKeyStateListener);
    Keys.Hireling.addStateListener(mappedKeyStateListener);
    Keys.Spells.addStateListener(mappedKeyStateListener);
    Keys.Quests.addStateListener(mappedKeyStateListener);
    Keys.SwapWeapons.addStateListener(mappedKeyStateListener);
    Keys.Stash.addStateListener(mappedKeyStateListener);
    Riiablo.input.addProcessor(testingInputProcessor);
    Riiablo.input.addProcessor(stage);
    Riiablo.input.addProcessor(scaledStage);
    Riiablo.client.addScreenBoundsListener(screenBoundsListener = new Client.ScreenBoundsListener() {
      final float THRESHOLD = 150;
      float prevY = 0;

      @Override
      public void updateScreenBounds(float x, float y, float width, float height) {
        if (y < THRESHOLD && prevY >= THRESHOLD) input.setVisible(false);
        input.setPosition(stage.getWidth() / 2, y + 100, Align.bottom | Align.center);
        prevY = y;
      }
    });
    screenBoundsListener.updateScreenBounds(0, 0, 0, 0);

    if (!DEBUG_MOBILE && Gdx.app.getType() == Application.ApplicationType.Desktop) {
      Cvars.Client.Display.KeepControlPanelGrouped.addStateListener(new CvarStateAdapter<Boolean>() {
        @Override
        public void onChanged(Cvar<Boolean> cvar, Boolean from, Boolean to) {
          if (to) {
            controlPanel.pack();
            controlPanel.setPosition(stage.getWidth() / 2, 0, Align.bottom | Align.center);
          } else {
            controlPanel.setX(0);
            controlPanel.setWidth(stage.getWidth());
            controlPanel.layout();
          }
        }
      });
    }

    Riiablo.viewport = viewport;
    Riiablo.music.stop();
    Riiablo.assets.get(windowopenDescriptor).play();

    if (socket == null) mapManager.createEntities();

    engine.getSystem(Box2DPhysics.class).createBodies();

    Vector2 origin = map.find(Map.ID.TOWN_ENTRY_1);
    if (origin == null) origin = map.find(Map.ID.TOWN_ENTRY_2);
    if (origin == null) origin = map.find(Map.ID.TP_LOCATION);
    Map.Zone zone = map.getZone(origin);
    player = factory.createPlayer(charData, origin);
    engine.getSystem(EventSystem.class).dispatch(ZoneChangeEvent.obtain(player, zone));

    renderer.setSrc(player);
    renderer.updatePosition(true);
  }

  @Override
  public void hide() {
    Keys.DebugMode.removeStateListener(debugKeyListener);
    Keys.Esc.removeStateListener(mappedKeyStateListener);
    Keys.Enter.removeStateListener(mappedKeyStateListener);
    Keys.Inventory.removeStateListener(mappedKeyStateListener);
    Keys.Character.removeStateListener(mappedKeyStateListener);
    Keys.Hireling.removeStateListener(mappedKeyStateListener);
    Keys.Spells.removeStateListener(mappedKeyStateListener);
    Keys.Quests.removeStateListener(mappedKeyStateListener);
    Keys.SwapWeapons.removeStateListener(mappedKeyStateListener);
    Keys.Stash.removeStateListener(mappedKeyStateListener);
    Riiablo.input.removeProcessor(testingInputProcessor);
    Riiablo.input.removeProcessor(stage);
    Riiablo.input.removeProcessor(scaledStage);
    Riiablo.client.removeScreenBoundsListener(screenBoundsListener);
    Cvars.Client.Display.KeepControlPanelGrouped.clearStateListeners();
  }

  @Override
  public void dispose() {
    //map.dispose(); // FIXME: additional instances aren't reloading textures properly (DT1s disposal)
    charData.clearListeners();
    engine.dispose();
    for (Actor actor : stage.getActors()) if (actor instanceof Disposable) ((Disposable) actor).dispose();
    stage.dispose();
    for (AssetDescriptor asset : preloadedAssets) Riiablo.assets.unload(asset.fileName);
  }

  public void setRightPanel(Actor actor) {
    if (right != null) {
      right.setVisible(false);
      right = null;
    }
    if (actor == null) return;
    actor.setVisible(true);
    right = actor;
  }

  public void setLeftPanel(Actor actor) {
    if (left != null) {
      left.setVisible(false);
      left = null;
    }
    if (actor == null) return;
    actor.setVisible(true);
    left = actor;
  }

  public void setDetails(Actor details, Item item, Actor parent, Actor slot) {
    if (this.details == details) return;
    this.details = details;
    if (slot != null) {
      details.setPosition(slot.getX() + slot.getWidth() / 2, slot.getY() + slot.getHeight(), Align.bottom | Align.center);
      tmpVec2.set(details.getX(), details.getY());
      parent.localToStageCoordinates(tmpVec2);
      tmpVec2.x = MathUtils.clamp(tmpVec2.x, 0, stage.getWidth()  - details.getWidth());
      tmpVec2.y = MathUtils.clamp(tmpVec2.y, 0, stage.getHeight() - details.getHeight());
      details.setPosition(tmpVec2.x, tmpVec2.y);
      tmpVec2.set(slot.getX(), slot.getY());
      parent.localToStageCoordinates(tmpVec2);
      if (details.getY() < tmpVec2.y + slot.getHeight()) {
        details.setPosition(slot.getX() + slot.getWidth() / 2, slot.getY(), Align.top | Align.center);
        tmpVec2.set(details.getX(), details.getY());
        parent.localToStageCoordinates(tmpVec2);
        tmpVec2.x = MathUtils.clamp(tmpVec2.x, 0, stage.getWidth()  - details.getWidth());
        tmpVec2.y = MathUtils.clamp(tmpVec2.y, 0, stage.getHeight() - details.getHeight());
        details.setPosition(tmpVec2.x, tmpVec2.y);
      }
    } else {
      details.setPosition(item.getX() + item.getWidth() / 2, item.getY(), Align.top | Align.center);
      tmpVec2.set(details.getX(), details.getY());
      parent.localToStageCoordinates(tmpVec2);
      tmpVec2.x = MathUtils.clamp(tmpVec2.x, 0, stage.getWidth()  - details.getWidth());
      tmpVec2.y = MathUtils.clamp(tmpVec2.y, 0, stage.getHeight() - details.getHeight());
      details.setPosition(tmpVec2.x, tmpVec2.y);
    }
  }

  public void setAct(int act) {
    player = Engine.INVALID_ENTITY;
    IntBag entities = engine.getAspectSubscriptionManager().get(Aspect.all()).getEntities();
    for (int i = 0, size = entities.size(); i < size; i++) {
      engine.delete(entities.get(i));
    }

    engine.getSystem(Box2DPhysics.class).clear();

    loadingScreen.loadAct(act);
    Riiablo.client.pushScreen(loadingScreen);
  }

  public void setLevel(Levels.Entry target) {
    assert target.Waypoint != 0xFF;
    if (target.Act != map.getAct()) {
      setAct(target.Act);
      return;
    }

    // TODO: support waypoints from same act
//    Riiablo.engine.removeAllEntities(Family.exclude(PlayerComponent.class).get());
//    Box2DPhysicsSystem system = engine.getSystem(Box2DPhysicsSystem.class);
//    World body2dWorld = system.world;
//    Array<Body> bodies = new Array<>(32768);
//    body2dWorld.getBodies(bodies);
//    for (Body body : bodies) body2dWorld.destroyBody(body);
//    player.getComponent(Box2DComponent.class).body = null;
//    system.entityAdded(player);
//
//    loadingScreen.loadAct(target.Act);
//    Riiablo.client.pushScreen(loadingScreen);
    /*
    map.clear();
    map.setAct(target.Act);
    map.load();
    map.finishLoading();
    map.generate();
    */

//    Vector2 origin = map.find(Map.ID.TOWN_ENTRY_1);
//    if (origin == null) origin = map.find(Map.ID.TOWN_ENTRY_2);
//    if (origin == null) origin = map.find(Map.ID.TP_LOCATION);
//    player.getComponent(PositionComponent.class).position.set(origin);
//    player.getComponent(Box2DComponent.class).body.setTransform(origin, 0);
//    player.getComponent(MapComponent.class).zone = map.getZone(origin);

  }
}
