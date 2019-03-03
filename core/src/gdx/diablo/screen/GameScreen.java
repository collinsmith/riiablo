package gdx.diablo.screen;

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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Timer;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import gdx.diablo.Diablo;
import gdx.diablo.Keys;
import gdx.diablo.entity.Entity;
import gdx.diablo.entity.Player;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.graphics.PaletteIndexedColorDrawable;
import gdx.diablo.key.MappedKey;
import gdx.diablo.key.MappedKeyStateAdapter;
import gdx.diablo.map.DT1.Tile;
import gdx.diablo.map.Map;
import gdx.diablo.map.MapListener;
import gdx.diablo.map.MapLoader;
import gdx.diablo.map.MapRenderer;
import gdx.diablo.panel.CharacterPanel;
import gdx.diablo.panel.ControlPanel;
import gdx.diablo.panel.EscapePanel;
import gdx.diablo.panel.InventoryPanel;
import gdx.diablo.panel.MobilePanel;
import gdx.diablo.panel.StashPanel;
import gdx.diablo.server.Connect;
import gdx.diablo.server.ConnectResponse;
import gdx.diablo.server.Disconnect;
import gdx.diablo.server.Message;
import gdx.diablo.server.MoveTo;
import gdx.diablo.server.Packet;
import gdx.diablo.server.Packets;
import gdx.diablo.server.PipedSocket;
import gdx.diablo.widget.NpcMenu;
import gdx.diablo.widget.TextArea;

public class GameScreen extends ScreenAdapter implements LoadingScreen.Loadable {
  private static final String TAG = "GameScreen";
  private static final boolean DEBUG_TOUCHPAD = !true;
  private static final boolean DEBUG_MOBILE   = true;

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

  final AssetDescriptor<Map> mapDescriptor = new AssetDescriptor<>("Act 1", Map.class, MapLoader.MapParameters.of(0, 0, 0));
  Map map;
  MapRenderer mapRenderer;
  MapListener mapListener;
  InputProcessor inputProcessorTest;
  final Array<Actor> labels = new Array<>();
  Actor menu;

  public TextArea input;
  TextArea output;

  //Char character;
  public Player player;
  IntMap<Player> entities = new IntMap<>();
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

    input = new TextArea("", new TextArea.TextFieldStyle() {{
      this.font = Diablo.fonts.fontformal12;
      this.fontColor = Diablo.colors.white;
      this.background = new PaletteIndexedColorDrawable(Diablo.colors.modal50);
      this.cursor = new TextureRegionDrawable(Diablo.textures.white);

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
    input.setSize(Diablo.VIRTUAL_WIDTH * 0.75f, Diablo.fonts.fontformal12.getLineHeight() * 3);
    input.setPosition(Diablo.VIRTUAL_WIDTH_CENTER - input.getWidth() / 2, 100);
    input.setAlignment(Align.topLeft);
    input.setVisible(false);

    output = new TextArea("", new TextArea.TextFieldStyle() {{
      this.font = Diablo.fonts.fontformal12;
      this.fontColor = Diablo.colors.white;
      this.cursor = new TextureRegionDrawable(Diablo.textures.white);
    }});
    //output.setDebug(true);
    output.setSize(Diablo.VIRTUAL_WIDTH * 0.75f, Diablo.fonts.fontformal12.getLineHeight() * 8);
    output.setPosition(10, Diablo.VIRTUAL_HEIGHT - 10, Align.topLeft);
    output.setAlignment(Align.topLeft);
    output.setDisabled(true);
    output.setVisible(true);
    output.setTouchable(Touchable.disabled);

    escapePanel = new EscapePanel();

    controlPanel = new ControlPanel(this);
    controlPanel.setPosition(
        Diablo.VIRTUAL_WIDTH_CENTER - (controlPanel.getWidth() / 2),
        0);

    if (Gdx.app.getType() == Application.ApplicationType.Android || DEBUG_MOBILE) {
      mobilePanel = new MobilePanel(this);
      mobilePanel.setPosition(
          Diablo.VIRTUAL_WIDTH  - mobilePanel.getWidth(),
          0);//Diablo.VIRTUAL_HEIGHT - mobilePanel.getHeight());
    }

    inventoryPanel = new InventoryPanel(this);
    inventoryPanel.setPosition(
        Diablo.VIRTUAL_WIDTH - inventoryPanel.getWidth(),
        Diablo.VIRTUAL_HEIGHT - inventoryPanel.getHeight());

    characterPanel = new CharacterPanel(this);
    characterPanel.setPosition(
        0,
        Diablo.VIRTUAL_HEIGHT - characterPanel.getHeight());

    stashPanel = new StashPanel(this);
    stashPanel.setPosition(
        0,
        Diablo.VIRTUAL_HEIGHT - stashPanel.getHeight());

    stage = new Stage(Diablo.viewport, Diablo.batch);
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
      Diablo.assets.load(touchpadBackgroundDescriptor);
      Diablo.assets.load(touchpadKnobDescriptor);
      Diablo.assets.finishLoadingAsset(touchpadBackgroundDescriptor);
      Diablo.assets.finishLoadingAsset(touchpadKnobDescriptor);
      touchpad = new Touchpad(10, new Touchpad.TouchpadStyle() {{
        //background = new TextureRegionDrawable(Diablo.assets.get(touchpadBackgroundDescriptor));
        background = null;
        knob = new TextureRegionDrawable(Diablo.assets.get(touchpadKnobDescriptor));
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
              mapRenderer.zoom(Math.min(2.50f, mapRenderer.zoom() + ZOOM_AMOUNT));
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
            output.appendText(Diablo.string.format(3641, connect.name));
            output.appendText("\n");

            // FIXME: Default position is in subtiles? Divide 5 temp fix
            Player connector = new Player(connect);
            GridPoint2 startPos = map.find(Map.ID.TOWN_ENTRY_1);
            connector.position().set(startPos.x, startPos.y, 0);
            entities.put(connect.id, connector);
            break;
          case Packets.DISCONNECT:
            Disconnect disconnect = packet.readValue(Disconnect.class);
            output.appendText(Diablo.string.format(3642, disconnect.name));
            output.appendText("\n");
            entities.remove(disconnect.id);
            break;
          case Packets.MOVETO:
            MoveTo moveTo = packet.readValue(MoveTo.class);
            Player p = entities.get(moveTo.id);
            //if (p == player) break; // Disable forced update positions for now
            if (p != null) {
              p.setPath(map, new Vector3(moveTo.x, moveTo.y, 0));
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

    PaletteIndexedBatch b = Diablo.batch;
    b.setPalette(Diablo.palettes.act1);

    if (DEBUG_TOUCHPAD || Gdx.app.getType() == Application.ApplicationType.Android) {
      float x = touchpad.getKnobPercentX();
      float y = touchpad.getKnobPercentY();
      if (x == 0 && y == 0) {
        player.setPath(map, null);
      } else {
        //float rad = MathUtils.atan2(y, x);
        //x = Direction.getOffX(rad);
        //y = Direction.getOffY(rad);
        //player.setPath(map, new Vector3(x, y, 0).add(player.position()), 3);

        Vector2 position = new Vector2(player.position().x, player.position().y);
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
            player.setPath(map, new Vector3(collision.point, 0), 3);
          }
        } else {
          //System.out.println("freedom baby");
          player.target().set(target, 0);
        }

        //System.out.println("hit " + hit + "; " + collision.point + "; " + collision.normal);
      }
    } else {
      // TODO: this requires a bit more thorough checking - touchable flags need to be checked on each panel and unset on output/input areas
      stage.screenToStageCoordinates(tmpVec2.set(Gdx.input.getX(), Gdx.input.getY()));
      Actor hit = stage.hit(tmpVec2.x, tmpVec2.y, false);
      if (hit == null || hit == output || hit == input) mapListener.update();
      //if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
      //  GridPoint2 coords = mapRenderer.coords();
      //  player.setPath(map, new Vector3(coords.x, coords.y, 0));
      //}
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

    mapRenderer.update();

    b.begin();
    mapRenderer.draw(delta);
    b.end();

    Diablo.shapes.setAutoShapeType(true);
    Diablo.shapes.begin(ShapeRenderer.ShapeType.Line);
    mapRenderer.drawDebug(Diablo.shapes);
    mapRenderer.drawDebugPath(Diablo.shapes, player.path());
    player.drawDebug(Diablo.batch, Diablo.shapes);
    Diablo.shapes.end();

    b.setProjectionMatrix(Diablo.viewport.getCamera().combined);

    stage.act();
    stage.draw();

    /*b.begin();
    for (Entity entity : labels) {
      entity.drawLabel(b);
      System.out.println("label!");
    }
    b.end();*/

    if (menu == null && !labels.isEmpty()) {
      layoutLabels();
      b.begin();
      for (Actor label : labels) label.draw(b, 1);
      b.end();
    }
  }

  @Override
  public void show() {
    Diablo.music.stop();
    Diablo.assets.get(windowopenDescriptor).play();

    map = Diablo.assets.get(mapDescriptor);
    mapRenderer = new MapRenderer(Diablo.batch);
    mapRenderer.setMap(map);
    mapRenderer.setSrc(player);
    mapRenderer.setEntities(entities);
    if (Gdx.app.getType() == Application.ApplicationType.Android) {
      mapRenderer.zoom(0.80f);
    }
    mapRenderer.resize();
    mapListener = new MapListener(this, map, mapRenderer);

    GridPoint2 origin = map.find(Map.ID.TOWN_ENTRY_1);
    player.position().set(origin.x, origin.y, 0);

    Keys.Esc.addStateListener(mappedKeyStateListener);
    Keys.Inventory.addStateListener(mappedKeyStateListener);
    Keys.Character.addStateListener(mappedKeyStateListener);
    Keys.Stash.addStateListener(mappedKeyStateListener);
    Keys.SwapWeapons.addStateListener(mappedKeyStateListener);
    Keys.Enter.addStateListener(mappedKeyStateListener);
    Diablo.input.addProcessor(stage);
    Diablo.input.addProcessor(inputProcessorTest);

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
    Diablo.input.removeProcessor(stage);
    Diablo.input.removeProcessor(inputProcessorTest);

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
    if (Diablo.assets.isLoaded(touchpadBackgroundDescriptor)) Diablo.assets.load(touchpadBackgroundDescriptor);
    if (Diablo.assets.isLoaded(touchpadKnobDescriptor))       Diablo.assets.load(touchpadKnobDescriptor);
    Diablo.assets.unload(windowopenDescriptor.fileName);
    for (AssetDescriptor asset : getDependencies()) if (Diablo.assets.isLoaded(asset)) Diablo.assets.unload(asset.fileName);
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
      tmp.x = MathUtils.clamp(tmp.x, 0, Diablo.VIRTUAL_WIDTH  - label.getWidth());
      tmp.y = MathUtils.clamp(tmp.y, 0, Diablo.VIRTUAL_HEIGHT - label.getHeight());
      label.setPosition(tmp.x, tmp.y);
    }
  }

  public Actor getMenu() {
    return menu;
  }

  public void setMenu(Actor menu, Entity owner) {
    if (this.menu != menu) {
      if (this.menu != null) {
        if (this.menu instanceof NpcMenu) ((NpcMenu) this.menu).cancel();
        stage.getRoot().removeActor(this.menu);
      }
      this.menu = menu;
      if (menu != null && owner != null) {
        stage.addActor(menu);

        Vector3 position = owner.position();
        float x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
        float y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
        menu.setPosition(x, y + owner.getLabelOffset(), Align.center | Align.bottom);

        Vector2 tmp = new Vector2();
        tmp.x = menu.getX();
        tmp.y = menu.getY();
        mapRenderer.projectScaled(tmp);
        tmp.x = MathUtils.clamp(tmp.x, 0, Diablo.VIRTUAL_WIDTH  - menu.getWidth());
        tmp.y = MathUtils.clamp(tmp.y, 0, Diablo.VIRTUAL_HEIGHT - menu.getHeight());
        menu.setPosition(tmp.x, tmp.y);
      }
    }
  }
}
