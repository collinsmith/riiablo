package com.riiablo.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.riiablo.Keys;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.entity.Entity;
import com.riiablo.entity.ItemHolder;
import com.riiablo.entity.Player;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.graphics.PaletteIndexedColorDrawable;
import com.riiablo.item.Item;
import com.riiablo.key.MappedKey;
import com.riiablo.key.MappedKeyStateAdapter;
import com.riiablo.loader.DC6Loader;
import com.riiablo.map.DT1.Tile;
import com.riiablo.map.Map;
import com.riiablo.map.MapListener;
import com.riiablo.map.MapLoader;
import com.riiablo.map.MapRenderer;
import com.riiablo.panel.CharacterPanel;
import com.riiablo.panel.ControlPanel;
import com.riiablo.panel.EscapePanel;
import com.riiablo.panel.InventoryPanel;
import com.riiablo.panel.MobilePanel;
import com.riiablo.panel.StashPanel;
import com.riiablo.server.Connect;
import com.riiablo.server.ConnectResponse;
import com.riiablo.server.Disconnect;
import com.riiablo.server.Message;
import com.riiablo.server.MoveTo;
import com.riiablo.server.Packet;
import com.riiablo.server.Packets;
import com.riiablo.server.PipedSocket;
import com.riiablo.widget.DCWrapper;
import com.riiablo.widget.NpcDialogBox;
import com.riiablo.widget.NpcMenu;
import com.riiablo.widget.TextArea;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class GameScreen extends ScreenAdapter implements LoadingScreen.Loadable {
  private static final String TAG = "GameScreen";
  private static final boolean DEBUG          = true;
  private static final boolean DEBUG_TOUCHPAD = !true;
  private static final boolean DEBUG_MOBILE   = !true;
  private static final boolean DEBUG_HIT      = DEBUG && !true;

  final AssetDescriptor<Sound> windowopenDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\windowopen.wav", Sound.class);

  final AssetDescriptor<Texture> touchpadBackgroundDescriptor = new AssetDescriptor<>("textures/touchBackground.png", Texture.class);
  final AssetDescriptor<Texture> touchpadKnobDescriptor = new AssetDescriptor<>("textures/touchKnob.png", Texture.class);

  Touchpad touchpad;
  public EscapePanel escapePanel;
  Actor left;
  Actor right;

  ControlPanel controlPanel;
  MobilePanel mobilePanel;
  public InventoryPanel inventoryPanel;
  public CharacterPanel characterPanel;
  public StashPanel stashPanel;
  MappedKeyStateAdapter mappedKeyStateListener;

  Stage stage;
  Viewport viewport;

  final AssetDescriptor<Map> mapDescriptor = new AssetDescriptor<>("Act 1", Map.class, MapLoader.MapParameters.of(0, 0, 0));
  Map map;
  MapRenderer mapRenderer;
  MapListener mapListener;
  InputProcessor inputProcessorTest;
  public final Array<Actor> labels = new Array<>();
  NpcMenu menu;
  NpcDialogBox dialog;
  Actor details;
  boolean showItems;

  final String[] ACT_NAME = { "act1", "act2", "act3", "act4", "expansion" };
  Map.Zone curZone;
  DCWrapper enteringImage;

  public TextArea input;
  TextArea output;

  //Char character;
  public Player player;
  public IntMap<Entity> entities = new IntMap<>();
  Timer.Task updateTask;

  Socket socket;
  PrintWriter out;
  BufferedReader in;

  private static final Vector2 tmpVec2 = new Vector2();

  @Override
  public Array<AssetDescriptor> getDependencies() {
    Array<AssetDescriptor> dependencies = new Array<>();
    dependencies.add(windowopenDescriptor);
    dependencies.add(mapDescriptor);
    return dependencies;
  }

  public GameScreen(Player player) {
    this(player, new PipedSocket());
  }

  //public GameScreen(final Char character) {
  public GameScreen(final Player player, Socket socket) {
    this.player = player;
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
    }}) {{
        writeEnters = false;
      }

      @Override
      public void setVisible(boolean visible) {
        if (!visible) input.setText("");
        super.setVisible(visible);
      }
    };
    input.setDebug(true);
    input.setSize(stage.getWidth() * 0.75f, Riiablo.fonts.fontformal12.getLineHeight() * 3);
    input.setPosition(stage.getWidth() / 2 - input.getWidth() / 2, 100);
    input.setAlignment(Align.topLeft);
    input.setVisible(false);
    input.setTouchable(Touchable.disabled);

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

    escapePanel = new EscapePanel();

    controlPanel = new ControlPanel(this);
    controlPanel.setPosition(
        stage.getWidth() / 2 - (controlPanel.getWidth() / 2),
        0);

    if (Gdx.app.getType() == Application.ApplicationType.Android || DEBUG_MOBILE) {
      mobilePanel = new MobilePanel(this);
      mobilePanel.setPosition(
          stage.getWidth()  - mobilePanel.getWidth(),
          0);//Diablo.VIRTUAL_HEIGHT - mobilePanel.getHeight());
    }

    inventoryPanel = new InventoryPanel(this);
    inventoryPanel.setPosition(
        stage.getWidth() - inventoryPanel.getWidth(),
        stage.getHeight() - inventoryPanel.getHeight());

    characterPanel = new CharacterPanel(this);
    characterPanel.setPosition(
        0,
        stage.getHeight() - characterPanel.getHeight());

    stashPanel = new StashPanel(this);
    stashPanel.setPosition(
        0,
        stage.getHeight() - stashPanel.getHeight());

    //stage.setDebugAll(true);
    if (mobilePanel != null) stage.addActor(mobilePanel);
    stage.addActor(input);
    stage.addActor(output);
    stage.addActor(controlPanel);
    stage.addActor(escapePanel);
    stage.addActor(inventoryPanel);
    stage.addActor(characterPanel);
    stage.addActor(stashPanel);
    controlPanel.toFront();
    if (mobilePanel != null) mobilePanel.toFront();
    output.toFront();
    input.toFront();
    escapePanel.toFront();

    if (Gdx.app.getType() == Application.ApplicationType.Android || DEBUG_TOUCHPAD) {
      Riiablo.assets.load(touchpadBackgroundDescriptor);
      Riiablo.assets.load(touchpadKnobDescriptor);
      Riiablo.assets.finishLoadingAsset(touchpadBackgroundDescriptor);
      Riiablo.assets.finishLoadingAsset(touchpadKnobDescriptor);
      touchpad = new Touchpad(10, new Touchpad.TouchpadStyle() {{
        //background = new TextureRegionDrawable(Diablo.assets.get(touchpadBackgroundDescriptor));
        background = null;
        knob = new TextureRegionDrawable(Riiablo.assets.get(touchpadKnobDescriptor));
      }});
      touchpad.setSize(164, 164);
      touchpad.setPosition(0, 0);
      stage.addActor(touchpad);
    }

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
          } else if (inventoryPanel.isVisible()
                  || characterPanel.isVisible()) {
            inventoryPanel.setVisible(false);
            characterPanel.setVisible(false);
          } else {
            escapePanel.setVisible(true);
          }
        } else if (key == Keys.Enter) {
          boolean visible = !input.isVisible();
          if (!visible) {
            String text = input.getText();
            if (!text.isEmpty()) {
              Gdx.app.debug(TAG, text);
              Message message = new Message(player.stats.getName(), text);
              out.println(Packets.build(message));
              input.setText("");
            }
          }

          input.setVisible(visible);
          if (visible) {
            stage.setKeyboardFocus(input);
          }
        } else if (key == Keys.Inventory) {
          inventoryPanel.setVisible(!inventoryPanel.isVisible());
        } else if (key == Keys.Character) {
          stashPanel.setVisible(false);
          characterPanel.setVisible(!characterPanel.isVisible());
        } else if (key == Keys.Stash) {
          characterPanel.setVisible(false);
          stashPanel.setVisible(!stashPanel.isVisible());
        } else if (key == Keys.SwapWeapons) {
          player.setAlternate(!player.isAlternate());
        }
      }
    };

    inputProcessorTest = new InputAdapter() {
      private final float ZOOM_AMOUNT = 0.1f;

      @Override
      public boolean scrolled(int amount) {
        switch (amount) {
          case -1:
            if (UIUtils.ctrl()) {
              mapRenderer.zoom(Math.max(0.25f, mapRenderer.zoom() - ZOOM_AMOUNT));
            }

            break;
          case 1:
            if (UIUtils.ctrl()) {
              mapRenderer.zoom(Math.min(10.00f, mapRenderer.zoom() + ZOOM_AMOUNT));
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
              MapRenderer.RENDER_DEBUG_WALKABLE = MapRenderer.RENDER_DEBUG_WALKABLE == 0 ? 1 : 0;
            } else {
              MapRenderer.RENDER_DEBUG_GRID++;
              if (MapRenderer.RENDER_DEBUG_GRID > MapRenderer.DEBUG_GRID_MODES) {
                MapRenderer.RENDER_DEBUG_GRID = 0;
              }
            }
            return true;

          default:
            return false;
        }
      }
    };
  }

  @Override
  public void render(float delta) {
    try {
      for (String str; in.ready() && (str = in.readLine()) != null;) {
        Packet packet = Packets.parse(str);
        switch (packet.type) {
          case Packets.MESSAGE:
            Message message = packet.readValue(Message.class);
            output.appendText(message.toString());
            output.appendText("\n");
            break;
          case Packets.CONNECT:
            Connect connect = packet.readValue(Connect.class);
            output.appendText(Riiablo.string.format(3641, connect.name));
            output.appendText("\n");

            // FIXME: Default position is in subtiles? Divide 5 temp fix
            Player connector = new Player(connect);
            GridPoint2 startPos = map.find(Map.ID.TOWN_ENTRY_1);
            connector.position().set(startPos.x, startPos.y);
            entities.put(connect.id, connector);
            break;
          case Packets.DISCONNECT:
            Disconnect disconnect = packet.readValue(Disconnect.class);
            output.appendText(Riiablo.string.format(3642, disconnect.name));
            output.appendText("\n");
            entities.remove(disconnect.id);
            break;
          case Packets.MOVETO:
            MoveTo moveTo = packet.readValue(MoveTo.class);
            Entity p = entities.get(moveTo.id);
            //if (p == player) break; // Disable forced update positions for now
            if (p != null) {
              p.setPath(map, new Vector2(moveTo.x, moveTo.y));
              //p.setAngle(moveTo.angle);
            }
            break;
          case Packets.CONNECT_RESPONSE:
            ConnectResponse connectResponse = packet.readValue(ConnectResponse.class);
            entities.put(connectResponse.id, player);
            break;
        }
      }
    } catch (IOException e) {
      Gdx.app.error(TAG, e.getMessage());
    }

    PaletteIndexedBatch b = Riiablo.batch;
    b.setPalette(Riiablo.palettes.act1);

    if (DEBUG_TOUCHPAD || Gdx.app.getType() == Application.ApplicationType.Android) {
      float x = touchpad.getKnobPercentX();
      float y = touchpad.getKnobPercentY();
      if (x == 0 && y == 0) {
        player.setPath(map, null);
      } else if (getDialog() != null) {
        setDialog(null);
      } else if (getMenu() != null) {
        setMenu(null, null);
      } else {
        //float rad = MathUtils.atan2(y, x);
        //x = Direction.getOffX(rad);
        //y = Direction.getOffY(rad);
        //player.setPath(map, new Vector3(x, y, 0).add(player.position()), 3);

        Vector2 position = new Vector2(player.position());
        Vector2 target = new Vector2(x, y).scl(Tile.WIDTH).add(mapRenderer.project(position.x, position.y, new Vector2()));
        GridPoint2 coords = mapRenderer.coords(target.x, target.y, new GridPoint2());
        target.set(coords.x, coords.y);
        Ray<Vector2> ray = new Ray<>(position, target);
        Collision<Vector2> collision = new Collision<>(new Vector2(), new Vector2());
        boolean hit = map.castRay(collision, ray);
        if (hit) {
          if (position.epsilonEquals(collision.point, 1.0f)) {
            /*System.out.println("against wall");

            float rad = MathUtils.atan2(y, x);
            if (rad > MathUtils.PI - 0.46365f) {
              rad -= MathUtils.PI - 0.46365f;
              System.out.println("1 " + rad);
            } else if (rad < -0.46365f) {
              rad += MathUtils.PI + 0.46365f;
              System.out.println("2 " + rad);
            }

            if (rad > 2.0944f) {
              Vector3 newTarget = new Vector3(player.position()).add(1, 0, 0);
              player.setPath(map, newTarget, 2);
              System.out.println("down " + player.position() + "; " + newTarget);
            } else if (rad > 0 && rad < 1.0472f) {
              Vector3 newTarget = new Vector3(player.position()).add(-1, 0, 0);
              player.setPath(map, newTarget, 2);
              System.out.println("up " + player.position() + "; " + newTarget);
            }*/
          } else {
            //System.out.println("headed for wall " + position + ", " + collision.point);
            player.setPath(map, collision.point, 3);
          }
        } else {
          //System.out.println("freedom baby");
          player.target().set(target);
        }

        //System.out.println("hit " + hit + "; " + collision.point + "; " + collision.normal);
      }

      clearLabels();
      Array<Entity> nearby = mapRenderer.getNearbyEntities();
      for (Entity entity : nearby) {
        if (entity.isSelectable() && entity.position().dst(player.position()) <= entity.getInteractRange() * 2) {
          entity.setOver(true);
          addLabel(entity.getLabel());
        } else {
          entity.setOver(false);
        }
      }
    } else {
      stage.screenToStageCoordinates(tmpVec2.set(Gdx.input.getX(), Gdx.input.getY()));
      Actor hit = stage.hit(tmpVec2.x, tmpVec2.y, true);
      if (hit == null) {
        /*boolean pressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        Item cursor = Riiablo.cursor.getItem();
        if (cursor != null && pressed) {
          Riiablo.cursor.setItem(null);
          Entity item = new ItemHolder(cursor);
          item.position().set(player.position());
          entities.put(entities.size + 1, item);
        } else {*/
          mapListener.update();
        //}
      }
      else if (DEBUG_HIT) Gdx.app.debug(TAG, hit.toString());
    }

    /*
    for (Entity entity : entities.values()) {
      entity.update(delta);
      if (!entity.target().isZero() && !entity.position().epsilonEquals(entity.target())) {
        float angle = mapRenderer.angle(entity.position(), entity.target());
        entity.setAngle(angle);
      }
    }
    */

    Map.Zone prevZone = curZone;
    mapRenderer.update();
    curZone = map.getZone(player.position());
    if (prevZone != curZone && prevZone != null) {
      displayEntry();
    }

    b.begin();
    mapRenderer.draw(delta);
    b.end();

    Riiablo.shapes.setAutoShapeType(true);
    Riiablo.shapes.begin(ShapeRenderer.ShapeType.Line);
    mapRenderer.drawDebug(Riiablo.shapes);
    mapRenderer.drawDebugPath(Riiablo.shapes, player.path());
    Riiablo.shapes.end();

    b.setProjectionMatrix(viewport.getCamera().combined);

    details = null;
    stage.act();
    stage.draw();

    if (menu == null && !labels.isEmpty()) {
      layoutLabels();
      b.begin();
      for (Actor label : labels) label.draw(b, 1);
      b.end();
    } else if (menu == null && details != null) {
      b.begin();
      details.draw(b, 1);
      b.end();
    }


    showItems = UIUtils.alt();
    if (showItems) {
      clearLabels();
      for (Entity entity : entities.values()) {
        if (entity instanceof ItemHolder) {
          Actor label = entity.getLabel();
          addLabel(label);
        }
      }
      layoutLabels();

      b.begin();
      for (Actor label : labels) label.draw(b, 1);
      b.end();
    }
  }

  @Override
  public void show() {
    Riiablo.viewport = viewport;
    Riiablo.music.stop();
    Riiablo.assets.get(windowopenDescriptor).play();

    Map.instance = map = Riiablo.assets.get(mapDescriptor); // TODO: remove Map.instance
    mapRenderer = new MapRenderer(Riiablo.batch, viewport.getWorldWidth(), viewport.getWorldHeight());
    //mapRenderer = new MapRenderer(Riiablo.batch, 480f * 16f / 9f, 480f);
    mapRenderer.setMap(map);
    mapRenderer.setSrc(player);
    mapRenderer.setEntities(entities);
    if (Gdx.app.getType() == Application.ApplicationType.Android
     || Riiablo.defaultViewport.getWorldHeight() == Riiablo.MOBILE_VIEWPORT_HEIGHT) {
      mapRenderer.zoom(0.80f);
    }
    mapRenderer.resize();
    mapListener = new MapListener(this, map, mapRenderer);

    GridPoint2 origin = map.find(Map.ID.TOWN_ENTRY_1);
    player.position().set(origin.x, origin.y);

    Keys.Esc.addStateListener(mappedKeyStateListener);
    Keys.Inventory.addStateListener(mappedKeyStateListener);
    Keys.Character.addStateListener(mappedKeyStateListener);
    Keys.Stash.addStateListener(mappedKeyStateListener);
    Keys.SwapWeapons.addStateListener(mappedKeyStateListener);
    Keys.Enter.addStateListener(mappedKeyStateListener);
    Riiablo.input.addProcessor(stage);
    Riiablo.input.addProcessor(inputProcessorTest);

    if (socket != null && socket.isConnected()) {
      Gdx.app.log(TAG, "connecting to " + socket.getRemoteAddress() + "...");
      in = IOUtils.buffer(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);
      if (!(socket instanceof PipedSocket)) {
        String connect = Packets.build(new Connect(player));
        out.println(connect);
      } else {
        String connectResponse = Packets.build(new ConnectResponse(1));
        out.println(connectResponse);
      }
    }

    /*
    updateTask = Timer.schedule(new Timer.Task() {
      GridPoint2 position = new GridPoint2();

      @Override
      public void run() {
        Vector3 pos = player.target();
        position.set((int) pos.x, (int) pos.y);
        String moveTo = Packets.build(new MoveTo(position, player.getAngle()));
        out.println(moveTo);
      }
    }, 0, 1 / 25f);
    */
  }

  @Override
  public void hide() {
    IOUtils.closeQuietly(in);
    IOUtils.closeQuietly(out);
    socket.dispose();
    Gdx.app.log(TAG, "Disposing socket... " + socket.isConnected());

    Keys.Esc.removeStateListener(mappedKeyStateListener);
    Keys.Inventory.removeStateListener(mappedKeyStateListener);
    Keys.Character.removeStateListener(mappedKeyStateListener);
    Keys.Stash.removeStateListener(mappedKeyStateListener);
    Keys.SwapWeapons.removeStateListener(mappedKeyStateListener);
    Keys.Enter.removeStateListener(mappedKeyStateListener);
    Riiablo.input.removeProcessor(stage);
    Riiablo.input.removeProcessor(inputProcessorTest);

    //updateTask.cancel();
  }

  @Override
  public void dispose() {
    stage.dispose();
    escapePanel.dispose();
    controlPanel.dispose();
    inventoryPanel.dispose();
    characterPanel.dispose();
    if (mobilePanel != null) mobilePanel.dispose();
    if (Riiablo.assets.isLoaded(touchpadBackgroundDescriptor)) Riiablo.assets.load(touchpadBackgroundDescriptor);
    if (Riiablo.assets.isLoaded(touchpadKnobDescriptor))       Riiablo.assets.load(touchpadKnobDescriptor);
    Riiablo.assets.unload(windowopenDescriptor.fileName);
    for (AssetDescriptor asset : getDependencies()) if (Riiablo.assets.isLoaded(asset)) Riiablo.assets.unload(asset.fileName);
  }

  @Override
  public void pause() {
    //escapePanel.setVisible(true);
  }

  @Override
  public void resume() {
    //escapePanel.setVisible(false);
  }

  public void clearLabels() {
    labels.size = 0;
  }

  public void addLabel(Actor label) {
    labels.add(label);
  }

  public void layoutLabels() {
    Vector2 tmp = new Vector2();
    for (Actor label : labels) {
      tmp.x = label.getX();
      tmp.y = label.getY();
      mapRenderer.projectScaled(tmp);
      tmp.x = MathUtils.clamp(tmp.x, 0, stage.getWidth()  - label.getWidth());
      tmp.y = MathUtils.clamp(tmp.y, 0, stage.getHeight() - label.getHeight());
      label.setPosition(tmp.x, tmp.y);
    }
  }

  public void setDetails(Actor details, Item item, Actor parent, Actor slot) {
    if (this.details != details) {
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
  }

  public NpcMenu getMenu() {
    return menu;
  }

  public void setMenu(NpcMenu menu, Entity owner) {
    if (this.menu != menu) {
      if (this.menu != null) {
        // FIXME: Validate that cancel is only called if upnav, downnav -- looks good at a glance
        if (menu == null || menu.getParent() != this.menu) {
          NpcMenu parent = this.menu;
          do parent.cancel(); while ((parent = parent.getParent()) != menu);
        }
        stage.getRoot().removeActor(this.menu);
      }
      this.menu = menu;
      if (menu != null && owner != null) {
        stage.addActor(menu);

        Vector2 position = owner.position();
        float x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
        float y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
        menu.setPosition(x, y + owner.getLabelOffset(), Align.center | Align.bottom);

        Vector2 tmp = new Vector2();
        tmp.x = menu.getX();
        tmp.y = menu.getY();
        mapRenderer.projectScaled(tmp);
        tmp.x = MathUtils.clamp(tmp.x, 0, stage.getWidth()  - menu.getWidth());
        tmp.y = MathUtils.clamp(tmp.y, 0, stage.getHeight() - menu.getHeight());
        menu.setPosition(tmp.x, tmp.y);
      }
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
        dialog.setPosition(stage.getWidth() / 2, stage.getHeight(), Align.top | Align.center);
        stage.addActor(dialog);
      }
    }
  }

  public void pickup(ItemHolder entity) {
    Riiablo.cursor.setItem(entity.item);
    int key = entities.findKey(entity, true, -1);
    if (key != -1) {
      entities.remove(key);
      mapListener.requireRelease = true;
    }
  }

  private void displayEntry() {
    if (enteringImage == null) {
      enteringImage = new DCWrapper();
      enteringImage.setScaling(Scaling.none);
      enteringImage.setAlign(Align.center);
      enteringImage.setBlendMode(BlendMode.TINT_ID_RED);
      stage.addActor(enteringImage);
    }

    // TODO: i18n? Not sure if these have translations.
    String entryFile = "data\\local\\ui\\eng\\" + ACT_NAME[map.act] + "\\" + curZone.level.EntryFile + ".dc6";
    AssetDescriptor<DC6> entryDescriptor = new AssetDescriptor<>(entryFile, DC6.class, DC6Loader.DC6Parameters.COMBINE);
    Riiablo.assets.load(entryDescriptor);
    Riiablo.assets.finishLoadingAsset(entryDescriptor);
    enteringImage.setDrawable(Riiablo.assets.get(entryDescriptor));
    enteringImage.setPosition(stage.getWidth() / 2, stage.getHeight() * 0.75f, Align.center);
    System.out.println(enteringImage.getWidth() + ", " + enteringImage.getHeight());
    enteringImage.clearActions();
    enteringImage.addAction(Actions.sequence(
        Actions.show(),
        Actions.alpha(1),
        Actions.delay(4, Actions.fadeOut(1, Interpolation.pow2In)),
        Actions.hide()));
  }
}
