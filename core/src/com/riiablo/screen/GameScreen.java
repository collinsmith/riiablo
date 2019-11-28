package com.riiablo.screen;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
import com.riiablo.CharData;
import com.riiablo.Client;
import com.riiablo.Cvars;
import com.riiablo.Keys;
import com.riiablo.Riiablo;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.codec.D2S;
import com.riiablo.cvar.Cvar;
import com.riiablo.cvar.CvarStateAdapter;
import com.riiablo.engine.Engine;
import com.riiablo.engine.Flags;
import com.riiablo.engine.component.AngleComponent;
import com.riiablo.engine.component.LabelComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.VelocityComponent;
import com.riiablo.engine.system.AISystem;
import com.riiablo.engine.system.AngleSystem;
import com.riiablo.engine.system.AngularVelocitySystem;
import com.riiablo.engine.system.AnimationLoaderSystem;
import com.riiablo.engine.system.AnimationSystem;
import com.riiablo.engine.system.AutoInteractSystem;
import com.riiablo.engine.system.Box2DBodySystem;
import com.riiablo.engine.system.CofLoaderSystem;
import com.riiablo.engine.system.CofSystem;
import com.riiablo.engine.system.CollisionSystem;
import com.riiablo.engine.system.IdSystem;
import com.riiablo.engine.system.ItemLoaderSystem;
import com.riiablo.engine.system.LabelSystem;
import com.riiablo.engine.system.MovementModeSystem;
import com.riiablo.engine.system.ObjectSystem;
import com.riiablo.engine.system.PathfindSystem;
import com.riiablo.engine.system.PlayerSystem;
import com.riiablo.engine.system.SelectableSystem;
import com.riiablo.engine.system.SelectedSystem;
import com.riiablo.engine.system.TouchMovementSystem;
import com.riiablo.engine.system.WarpSystem;
import com.riiablo.engine.system.ZoneEntrySystem;
import com.riiablo.engine.system.ZoneSystem;
import com.riiablo.engine.system.ZoneUpdateSystem;
import com.riiablo.engine.system.cof.AlphaUpdateSystem;
import com.riiablo.engine.system.cof.TransformUpdateSystem;
import com.riiablo.engine.system.debug.Box2DDebugRenderSystem;
import com.riiablo.engine.system.debug.PathfindDebugSystem;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.graphics.PaletteIndexedColorDrawable;
import com.riiablo.item.Item;
import com.riiablo.key.MappedKey;
import com.riiablo.key.MappedKeyStateAdapter;
import com.riiablo.map.Box2DPhysicsSystem;
import com.riiablo.map.Map;
import com.riiablo.map.RenderSystem;
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
import com.riiablo.server.PipedSocket;
import com.riiablo.widget.NpcDialogBox;
import com.riiablo.widget.NpcMenu;
import com.riiablo.widget.TextArea;

public class GameScreen extends ScreenAdapter implements LoadingScreen.Loadable {
  private static final String TAG = "ClientScreen";
  private static final boolean DEBUG          = true;
  private static final boolean DEBUG_TOUCHPAD = !true;
  private static final boolean DEBUG_MOBILE   = !true;
  private static final boolean DEBUG_HIT      = DEBUG && !true;

  private final Vector2 tmpVec2 = new Vector2();
  private final Vector2 tmpVec2b = new Vector2();

  final AssetDescriptor<Sound> windowopenDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\windowopen.wav", Sound.class);

  final AssetDescriptor<Texture> touchpadBackgroundDescriptor = new AssetDescriptor<>("textures/touchBackground.png", Texture.class);
  final AssetDescriptor<Texture> touchpadKnobDescriptor = new AssetDescriptor<>("textures/touchKnob.png", Texture.class);
  Touchpad touchpad;

  final Array<AssetDescriptor> preloadedAssets = new Array<AssetDescriptor>() {{
    add(windowopenDescriptor);
    if (Gdx.app.getType() == Application.ApplicationType.Android || DEBUG_TOUCHPAD) {
      add(touchpadBackgroundDescriptor);
      add(touchpadKnobDescriptor);
    }
  }};


  Stage stage;
  Stage scaledStage;
  Viewport viewport;
  boolean isDebug;
  MappedKeyStateAdapter debugKeyListener = new MappedKeyStateAdapter() {
    @Override
    public void onPressed(MappedKey key, int keycode) {
      isDebug = !isDebug;
    }
  };

  Engine engine;
  RenderSystem renderer;
  float accumulator;
  float lastUpdate;
  public Entity player;
  CharData charData;

  Map map;
  IsometricCamera iso;
  InputProcessor testingInputProcessor;

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
  NpcMenu menu;
  NpcDialogBox dialog;

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
    this(charData, new PipedSocket());
  }

  public GameScreen(CharData charData, Socket socket) {
    this.charData = charData;
    charData.getD2S().loadRemaining();
    charData.updateD2S(2);
    charData.loadItems();
    D2S.ItemData items = charData.getD2S().header.merc.items.items;
    if (items != null) for (Item item : items.items) item.load();

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
          Riiablo.charData.alternate();
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

          default:
            return false;
        }
      }
    };

    map = new Map(0, 0);
    renderer = new RenderSystem(Riiablo.batch);
    renderer.setMap(map);
    iso = renderer.iso();
    scaledStage = new Stage(new ScreenViewport(iso), Riiablo.batch);

    engine = Riiablo.engine = new Engine();
    engine.addSystem(new IdSystem());

    engine.addSystem(new CollisionSystem());
    if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
      engine.addSystem(new TouchMovementSystem(iso, renderer));
    }

    engine.addSystem(new AISystem(renderer));

    engine.addSystem(new PathfindSystem());
    engine.addSystem(new Box2DBodySystem());
    engine.addSystem(new Box2DPhysicsSystem(1 / 60f));

    //engine.addSystem(new TargetInteractSystem());

    //engine.addSystem(new PhysicsSystem());
    engine.addSystem(new ZoneSystem());
    engine.addSystem(new ZoneEntrySystem(renderer, stage)); // needs to run before ZoneUpdateSystem removes component
    engine.addSystem(new ZoneUpdateSystem());
    engine.addSystem(new MovementModeSystem());
    engine.addSystem(new CofSystem());
    engine.addSystem(new CofLoaderSystem());
    engine.addSystem(new AnimationLoaderSystem());
    engine.addSystem(new ItemLoaderSystem());
    //engine.addSystem(new ModeUpdateSystem());
    engine.addSystem(new TransformUpdateSystem());
    engine.addSystem(new AlphaUpdateSystem());
    engine.addSystem(new AnimationSystem());
    engine.addSystem(new ObjectSystem());
    engine.addSystem(new WarpSystem());
    engine.addSystem(new SelectableSystem());
    if (!DEBUG_TOUCHPAD && Gdx.app.getType() == Application.ApplicationType.Desktop) {
      engine.addSystem(new SelectedSystem(iso));
    }
    engine.addSystem(renderer);
    engine.addSystem(new LabelSystem(iso));
    engine.addSystem(new PlayerSystem());
    engine.addSystem(new AngularVelocitySystem());
    engine.addSystem(new AngleSystem());
    if (DEBUG_TOUCHPAD || Gdx.app.getType() == Application.ApplicationType.Android) {
      engine.addSystem(new AutoInteractSystem(renderer, 2.0f));
    }
    engine.addSystem(new Box2DDebugRenderSystem(renderer));
    engine.addSystem(new PathfindDebugSystem(iso, renderer, Riiablo.batch, Riiablo.shapes));
  }

  @Override
  public void resume() {
    Riiablo.engine = engine;
    Riiablo.game = this;
  }

  @Override
  public void render(float delta) {
    /*
    accumulator += delta;
    while (accumulator >= Animation.FRAME_DURATION) {
      lastUpdate = accumulator;
      accumulator -= Animation.FRAME_DURATION;
      engine.update(Animation.FRAME_DURATION);
    }

    renderer.update(delta);
    */

    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
      player.flags &= ~Flags.RUNNING;
    } else {
      player.flags |= Flags.RUNNING;
    }

    if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
      stage.screenToStageCoordinates(tmpVec2.set(Gdx.input.getX(), Gdx.input.getY()));
      Actor hit1 = stage.hit(tmpVec2.x, tmpVec2.y, true);
      scaledStage.screenToStageCoordinates(tmpVec2.set(Gdx.input.getX(), Gdx.input.getY()));
      Actor hit2 = scaledStage.hit(tmpVec2.x, tmpVec2.y, true);
      boolean hit = hit1 != null || hit2 != null;
      EntitySystem system;
      if ((system = engine.getSystem(TouchMovementSystem.class)) != null) system.setProcessing(!DEBUG_TOUCHPAD && !hit);
    }
    /*
    if (!DEBUG_TOUCHPAD && hit == null) {
      if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
        iso.agg(tmpVec2.set(Gdx.input.getX(), Gdx.input.getY())).unproject().toWorld();

        PositionComponent positionComponent = player.getComponent(PositionComponent.class);
        Vector2 position = positionComponent.position;

        AngleComponent angleComponent = player.getComponent(AngleComponent.class);
        angleComponent.target.set(tmpVec2.sub(position)).nor();

        VelocityComponent velocityComponent = player.getComponent(VelocityComponent.class);
        velocityComponent.velocity.set(tmpVec2)
            .setLength((player.flags & Flags.RUNNING) == Flags.RUNNING
                ? velocityComponent.runSpeed : velocityComponent.walkSpeed);
        playerBody.setLinearVelocity(velocityComponent.velocity);
      } else {
        VelocityComponent velocityComponent = player.getComponent(VelocityComponent.class);
        velocityComponent.velocity.setZero();
        playerBody.setLinearVelocity(velocityComponent.velocity);
      }
    }
    */

    if (DEBUG_TOUCHPAD || Gdx.app.getType() == Application.ApplicationType.Android) {
      tmpVec2.set(touchpad.getKnobPercentX(), touchpad.getKnobPercentY()).nor();
      if (tmpVec2.isZero()) {
        VelocityComponent velocityComponent = player.getComponent(VelocityComponent.class);
        velocityComponent.velocity.setZero();
        //playerBody.setLinearVelocity(velocityComponent.velocity);
      } else {
        PositionComponent positionComponent = player.getComponent(PositionComponent.class);
        Vector2 position = positionComponent.position;
        iso.toScreen(tmpVec2b.set(position)).add(tmpVec2);
        iso.toWorld(tmpVec2b).sub(position);

        AngleComponent angleComponent = player.getComponent(AngleComponent.class);
        angleComponent.target.set(tmpVec2b).nor();

        VelocityComponent velocityComponent = player.getComponent(VelocityComponent.class);
        velocityComponent.velocity.set(tmpVec2b)
            .setLength((player.flags & Flags.RUNNING) == Flags.RUNNING
                ? velocityComponent.runSpeed : velocityComponent.walkSpeed);
        //playerBody.setLinearVelocity(velocityComponent.velocity);

        setDialog(null);
        setMenu(null, null);
      }
    }

    Riiablo.assets.update();
    engine.update(delta);

    PaletteIndexedBatch batch = Riiablo.batch;
    batch.begin(Riiablo.palettes.act1);
    //batch.disableBlending();
    renderer.update(delta);

    if (menu == null && dialog == null) {
      LabelSystem labelSystem = engine.getSystem(LabelSystem.class);
      labelSystem.update(0);
      Array<Actor> labels = labelSystem.getLabels();
      layoutLabels(labels);
      for (Actor label : labels) {
        label.draw(batch, 1);
      }

      Actor monsterLabel = labelSystem.getMonsterLabel();
      if (monsterLabel != null && monsterLabel.isVisible()) {
        tmpVec2.set(Gdx.graphics.getWidth() / 2, iso.viewportHeight * 0.05f);
        iso.unproject(tmpVec2);
        monsterLabel.setPosition(tmpVec2.x, tmpVec2.y, Align.top | Align.center);
        monsterLabel.draw(batch, 1);
      }
    }

    //if (menu != null) menu.draw(batch, 1);

    batch.end();

    if (isDebug) {
      ShapeRenderer shapes = Riiablo.shapes;
      shapes.identity();
      shapes.setProjectionMatrix(iso.combined);
      shapes.setAutoShapeType(true);
      shapes.begin(ShapeRenderer.ShapeType.Line);
      renderer.drawDebug(shapes);
      shapes.end();

      EntitySystem system;
      if ((system = engine.getSystem(PathfindDebugSystem.class)) != null) system.update(delta);
      if ((system = engine.getSystem(Box2DDebugRenderSystem.class)) != null) system.update(delta);
    }

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

//    if (menu == null && !labels.isEmpty()) {
//      layoutLabels();
//      b.begin();
//      mapRenderer.prepare(b);
//      for (Actor label : labels) label.draw(b, 1);
//      b.end();
//    } else if (menu == null && details != null) {
//      b.begin();
//      details.draw(b, 1);
//      b.end();
//    }
    if (details != null) {
      batch.begin();
      details.draw(batch, 1);
      batch.end();
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
    Riiablo.game = this;
    isDebug = DEBUG && Gdx.app.getType() == Application.ApplicationType.Desktop;
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

    renderer.setProcessing(false);
    if (Gdx.app.getType() == Application.ApplicationType.Android
     || Riiablo.defaultViewport.getWorldHeight() == Riiablo.MOBILE_VIEWPORT_HEIGHT) {
      renderer.zoom(Riiablo.MOBILE_VIEWPORT_HEIGHT / (float) Gdx.graphics.getHeight());
    } else {
      renderer.zoom(Riiablo.DESKTOP_VIEWPORT_HEIGHT / (float) Gdx.graphics.getHeight());
    }
    renderer.resize();

    // TODO: move map into constructor of below methods or move these lines up top where they are created
    renderer.setMap(map);
    engine.getSystem(WarpSystem.class).setMap(map);
    engine.getSystem(Box2DPhysicsSystem.class).setMap(map, iso);

    Vector2 origin = map.find(Map.ID.TOWN_ENTRY_1);
    Map.Zone zone = map.getZone(origin);
    player = engine.createPlayer(map, zone, charData, origin.x, origin.y);

    engine.addEntity(player);
    renderer.setSrc(player);
    renderer.updatePosition(true);
    //charData.loadItems();
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
    engine.removeAllEntities();
    engine.removeAllSystems();
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

  private void layoutLabels(Array<Actor> labels) {
    for (Actor label : labels) {
      tmpVec2.x = label.getX();
      tmpVec2.y = label.getY();
      tmpVec2.x = MathUtils.clamp(tmpVec2.x, renderer.getMinX(), renderer.getMaxX() - label.getWidth());
      tmpVec2.y = MathUtils.clamp(tmpVec2.y, renderer.getMinY(), renderer.getMaxY() - label.getHeight());
      label.setPosition(tmpVec2.x, tmpVec2.y);
    }
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

  public NpcMenu getMenu() {
    return menu;
  }

  // TODO: notify menu open/close to set AI for owner to not move
  public void setMenu(NpcMenu menu, Entity owner) {
    if (this.menu == menu) return;
    if (this.menu != null) {
      // FIXME: Validate that cancel is only called if upnav, downnav -- looks good at a glance
      if (menu == null || menu.getParent() != this.menu) {
        NpcMenu parent = this.menu;
        do parent.cancel(); while ((parent = parent.getParent()) != menu);
      }
      scaledStage.getRoot().removeActor(this.menu);
    }

    this.menu = menu;
    if (menu != null && owner != null) {
      scaledStage.addActor(menu);

      LabelComponent labelComponent = owner.getComponent(LabelComponent.class);
      PositionComponent positionComponent = owner.getComponent(PositionComponent.class);
      iso.toScreen(tmpVec2.set(positionComponent.position));
      tmpVec2.add(labelComponent.offset);
      iso.project(tmpVec2);
      tmpVec2.y = iso.viewportHeight - tmpVec2.y; // stage coords expect y-down coords
      scaledStage.screenToStageCoordinates(tmpVec2);
      menu.setPosition(tmpVec2.x, tmpVec2.y, Align.center | Align.bottom);
    }
  }

  public NpcDialogBox getDialog() {
    return dialog;
  }

  public void setDialog(NpcDialogBox dialog) {
    if (this.dialog != dialog) {
      if (this.dialog != null) {
        this.dialog.remove();
        this.dialog.dispose();
        if (menu != null) menu.setVisible(true);
      }

      this.dialog = dialog;
      if (dialog != null) {
        if (menu != null) menu.setVisible(false);
        //dialog.setPosition(stage.getWidth() / 2, stage.getHeight(), Align.top | Align.center);
        tmpVec2.set(Gdx.graphics.getWidth() / 2, 0);
        scaledStage.screenToStageCoordinates(tmpVec2);
        dialog.setPosition(tmpVec2.x, tmpVec2.y, Align.top | Align.center);
        scaledStage.addActor(dialog);
      }
    }
  }
}
