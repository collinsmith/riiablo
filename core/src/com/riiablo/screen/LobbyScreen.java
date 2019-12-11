package com.riiablo.screen;

import com.google.flatbuffers.FlatBufferBuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.riiablo.CharData;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.DC6Loader;
import com.riiablo.net.Account;
import com.riiablo.net.GameSession;
import com.riiablo.net.packet.mcp.CreateGame;
import com.riiablo.net.packet.mcp.JoinGame;
import com.riiablo.net.packet.mcp.ListGames;
import com.riiablo.net.packet.mcp.MCP;
import com.riiablo.net.packet.mcp.MCPData;
import com.riiablo.net.packet.mcp.Result;
import com.riiablo.util.EventUtils;
import com.riiablo.widget.Label;
import com.riiablo.widget.TextArea;
import com.riiablo.widget.TextButton;
import com.riiablo.widget.TextField;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class LobbyScreen extends ScreenAdapter {
  private static final String TAG = "LobbyScreen";
  private static final boolean DEBUG            = true;
  private static final boolean DEBUG_CONNECTION = DEBUG && true;

  // FIXME: This background is not feasible and will always require shaving, easier to just use
  //        component panels and button groups to create my own?
  final AssetDescriptor<DC6> waitingroombkgdDescriptor = new AssetDescriptor<>("data\\global\\ui\\BIGMENU\\waitingroombkgd.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion waitingroombkgd;

  final AssetDescriptor<DC6> blankbckgDescriptor = new AssetDescriptor<>("data\\global\\ui\\BIGMENU\\blankbckg2.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion blankbckg;

  final AssetDescriptor<DC6> creategamebckgDescriptor = new AssetDescriptor<>("data\\global\\ui\\BIGMENU\\creategamebckg.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion creategamebckg;

  final AssetDescriptor<DC6> joingamebckgDescriptor = new AssetDescriptor<>("data\\global\\ui\\BIGMENU\\joingamebckg.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion joingamebckg;

  final AssetDescriptor<DC6> chatrighttopbuttonsDescriptor = new AssetDescriptor<>("data\\global\\ui\\BIGMENU\\chatrighttopbuttons.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  private TextButton btnCreate;
  private TextButton btnJoin;

  final AssetDescriptor<DC6> chatleftbuttonsDescriptor = new AssetDescriptor<>("data\\global\\ui\\BIGMENU\\chatleftbuttons.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  private Button btnChannel;
  private Button btnLadder;
  private Button btnQuit;

  final AssetDescriptor<DC6> cancelbuttonblankDescriptor = new AssetDescriptor<>("data\\global\\ui\\BIGMENU\\cancelbuttonblank.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  final AssetDescriptor<DC6> gamebuttonblankDescriptor = new AssetDescriptor<>("data\\global\\ui\\BIGMENU\\gamebuttonblank.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);

  private Stage stage;

  private Account  account;
  private CharData player;

  private TextArea taChatOutput;
  private TextField tfChatInput;

  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;

  private MCPConnection connection;

  public LobbyScreen(Account account, CharData player) {
    this.account = account;
    this.player = player;

    Riiablo.assets.load(waitingroombkgdDescriptor);
    Riiablo.assets.load(blankbckgDescriptor);
    Riiablo.assets.load(creategamebckgDescriptor);
    Riiablo.assets.load(joingamebckgDescriptor);
    Riiablo.assets.load(chatleftbuttonsDescriptor);
    Riiablo.assets.load(chatrighttopbuttonsDescriptor);
    Riiablo.assets.load(cancelbuttonblankDescriptor);
    Riiablo.assets.load(gamebuttonblankDescriptor);

    stage = new Stage(Riiablo.extendViewport, Riiablo.batch);
  }

  @Override
  public void show() {
    Riiablo.viewport = Riiablo.extendViewport;
    Riiablo.assets.finishLoadingAsset(waitingroombkgdDescriptor);
    waitingroombkgd = Riiablo.assets.get(waitingroombkgdDescriptor).getTexture();

    Riiablo.assets.finishLoadingAsset(blankbckgDescriptor);
    blankbckg = Riiablo.assets.get(blankbckgDescriptor).getTexture();

    Riiablo.assets.finishLoadingAsset(creategamebckgDescriptor);
    creategamebckg = Riiablo.assets.get(creategamebckgDescriptor).getTexture();

    Riiablo.assets.finishLoadingAsset(joingamebckgDescriptor);
    joingamebckg = Riiablo.assets.get(joingamebckgDescriptor).getTexture();

    stage.setDebugAll(true);

    ClickListener clickListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Actor actor = event.getListenerActor();
        if (actor == btnChannel) {
        } else if (actor == btnLadder) {
        } else if (actor == btnQuit) {
          Riiablo.client.popScreen();
        }
      }
    };

    // FIXME: disabled buttons look like a tinted up, which isn't possible now, maybe in future?
    final TextButton.TextButtonStyle style2 = new TextButton.TextButtonStyle() {{
      Riiablo.assets.finishLoadingAsset(chatrighttopbuttonsDescriptor);
      DC6 chatrighttopbuttons = Riiablo.assets.get(chatrighttopbuttonsDescriptor);
      up       = new TextureRegionDrawable(chatrighttopbuttons.getTexture(0));
      down     = new TextureRegionDrawable(chatrighttopbuttons.getTexture(1));
      checked  = down;
      disabled = down;
      font     = Riiablo.fonts.fontridiculous;

      unpressedOffsetY = 0;
      pressedOffsetX = pressedOffsetY = -1;
      checkedOffsetX = pressedOffsetX;
      checkedOffsetY = pressedOffsetY;
    }};
    btnCreate = new TextButton(5312, style2);
    btnJoin = new TextButton(5313, style2);

    final TextButton.TextButtonStyle style = new TextButton.TextButtonStyle() {{
      Riiablo.assets.finishLoadingAsset(chatleftbuttonsDescriptor);
      DC6 chatleftbuttons = Riiablo.assets.get(chatleftbuttonsDescriptor);
      up       = new TextureRegionDrawable(chatleftbuttons.getTexture(0));
      down     = new TextureRegionDrawable(chatleftbuttons.getTexture(1));
      disabled = down;
      font     = Riiablo.fonts.fontridiculous;

      checkedOffsetY = unpressedOffsetY = 0;
      pressedOffsetX = pressedOffsetY = -1;
    }};
    btnChannel = new TextButton(5314, style);
    btnChannel.setDisabled(true);
    btnChannel.addListener(clickListener);
    btnLadder = new TextButton(5315, style);
    btnLadder.setDisabled(true);
    btnLadder.addListener(clickListener);
    btnQuit = new TextButton(5316, style);
    btnQuit.addListener(clickListener);

    final Table panel = new Table() {{
      add(new Table() {{
        add(btnCreate).space(1);
        add(btnJoin).space(1);
      }}).space(1).row();
      add(new Table() {{
        add(btnChannel).space(1);
        add(btnLadder).space(1);
        add(btnQuit).space(1);
      }}).space(1).row();
      pack();
      setPosition(stage.getWidth() - getWidth() - 55, 109 - 48);
    }};
    stage.addActor(panel);

    final TextButton.TextButtonStyle cancelbuttonblankStyle = new TextButton.TextButtonStyle() {{
      Riiablo.assets.finishLoadingAsset(cancelbuttonblankDescriptor);
      DC6 cancelbuttonblank = Riiablo.assets.get(cancelbuttonblankDescriptor);
      up       = new TextureRegionDrawable(cancelbuttonblank.getTexture(0));
      down     = new TextureRegionDrawable(cancelbuttonblank.getTexture(1));
      disabled = down;
      font     = Riiablo.fonts.fontridiculous;

      checkedOffsetY = unpressedOffsetY = 0;
      pressedOffsetX = pressedOffsetY = -1;
    }};

    final TextButton.TextButtonStyle gamebuttonblankStyle = new TextButton.TextButtonStyle() {{
      Riiablo.assets.finishLoadingAsset(gamebuttonblankDescriptor);
      DC6 gamebuttonblank = Riiablo.assets.get(gamebuttonblankDescriptor);
      up = new TextureRegionDrawable(gamebuttonblank.getTexture(0));
      down = new TextureRegionDrawable(gamebuttonblank.getTexture(1));
      disabled = down;
      font = Riiablo.fonts.fontridiculous;

      checkedOffsetY = unpressedOffsetY = 0;
      pressedOffsetX = pressedOffsetY = -1;
    }};

    final TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle() {{
      font = Riiablo.fonts.fontformal10;
      fontColor = Riiablo.colors.white;
      cursor = new TextureRegionDrawable(Riiablo.textures.white);
    }};

    final TabbedPane right = new TabbedPane(blankbckg) {{
      pack();
      setPosition(stage.getWidth() - getWidth() - 39, 105);
      final ClickListener cancelListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          clearSelection();
        }
      };

      addTab(btnCreate, new TabGroup() {{
        setBackground(creategamebckg);
        addActor(new Label(5150, Riiablo.fonts.font30) {{
          setPosition(
              creategamebckg.getRegionWidth() / 2 - getWidth() / 2,
              creategamebckg.getRegionHeight() - getHeight());
        }});
        addActor(new Label(5274, Riiablo.fonts.font16, Riiablo.colors.gold) {{
          setPosition(12, 315);
        }});
        addActor(new Label(5256, Riiablo.fonts.font16, Riiablo.colors.gold) {{
          setPosition(12, 260);
        }});
        addActor(new Label(5257, Riiablo.fonts.font16, Riiablo.colors.gold) {{
          setPosition(12, 205);
        }});
        addActor(new Label(5258, Riiablo.fonts.font16, Riiablo.colors.gold) {{
          setPosition(20, 140);
        }});
        addActor(new Label(5259, Riiablo.fonts.font16, Riiablo.colors.gold) {{
          setPosition(20, 105);
        }});

        final TextField tfGameName = new TextField("", textFieldStyle);
        tfGameName.setPosition(14, 288);
        tfGameName.setSize(180, 20);
        addActor(tfGameName);

        final TextField tfPassword = new TextField("", textFieldStyle);
        tfPassword.setPasswordMode(true);
        tfPassword.setPosition(14, 234);
        tfPassword.setSize(180, 20);
        addActor(tfPassword);

        final TextField tfDesc = new TextField("", textFieldStyle);
        tfDesc.setPosition(14, 180);
        tfDesc.setSize(336, 20);
        addActor(tfDesc);

        final TextButton btnCancel = new TextButton(5134, cancelbuttonblankStyle);
        btnCancel.addListener(cancelListener);
        btnCancel.setPosition(18, 14);
        addActor(btnCancel);
        final TextButton btnCreateGame = new TextButton(5150, gamebuttonblankStyle);
        btnCreateGame.setPosition(179, 14);
        btnCreateGame.addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            final GameSession session = new GameSession.Builder() {{
              name     = tfGameName.getText();
              password = tfPassword.getText();
              desc     = tfDesc.getText();
            }}.build();
            CreateGame(session, new ResponseListener() {
              @Override
              public void handleResponse(MCP packet) {
                CreateGame createGame = (CreateGame) packet.data(new CreateGame());
                switch (createGame.result()) {
                  case Result.SUCCESS:
                    Gdx.app.debug(TAG, "Session created! " + session);
                    tryJoinGame(session);
                    break;
                  case Result.ALREADY_EXISTS:
                    Gdx.app.debug(TAG, "ALREADY_EXISTS");
                    break;
                  case Result.SERVER_DOWN:
                    Gdx.app.debug(TAG, "SERVER_DOWN");
                    break;
                }
              }

              @Override
              public void failed(Throwable t) {
                Gdx.app.error(TAG, t.getMessage(), t);
              }
            });
          }
        });
        tfGameName.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            btnCreateGame.setDisabled(tfGameName.getText().isEmpty());
          }
        });
        addActor(btnCreateGame);
        setSize(getBackground().getMinWidth(), getBackground().getMinHeight());

        final EventListener keyListener = new InputListener() {
          @Override
          public boolean keyDown(InputEvent e, int keycode) {
            switch (keycode) {
              case Input.Keys.ESCAPE:
                if (EventUtils.click(btnCancel)) {
                  return true;
                }
                break;

              case Input.Keys.ENTER:
                if (EventUtils.click(btnCreateGame)) {
                  return true;
                }
                break;
            }

            return super.keyDown(e, keycode);
          }
        };
        setListener(new TabAdapter() {
          @Override
          public void exited() {
            removeListener(keyListener);
          }

          @Override
          public void entered() {
            addListener(keyListener);
            stage.setKeyboardFocus(tfGameName);
          }
        });
      }});
      addTab(btnJoin, new TabGroup() {{
        setBackground(joingamebckg);
        addActor(new Label(5151, Riiablo.fonts.font30) {{
          setPosition(
              joingamebckg.getRegionWidth() / 2 - getWidth() / 2,
              joingamebckg.getRegionHeight() - getHeight());
        }});
        addActor(new Label(5274, Riiablo.fonts.font16, Riiablo.colors.gold) {{
          setPosition(12, 328);
        }});
        addActor(new Label(5225, Riiablo.fonts.font16, Riiablo.colors.gold) {{
          setPosition(188, 328);
        }});
        addActor(new Label(5275, Riiablo.fonts.font16, Riiablo.colors.gold) {{
          setPosition(16, 236);
        }});

        final TextField tfGameName = new TextField("", textFieldStyle);
        tfGameName.setPosition(14, 302);
        tfGameName.setSize(160, 20);
        addActor(tfGameName);

        final TextField tfPassword = new TextField("", textFieldStyle);
        tfPassword.setPasswordMode(true);
        tfPassword.setPosition(188, 302);
        tfPassword.setSize(160, 20);
        addActor(tfPassword);

        List.ListStyle style3 = new List.ListStyle(Riiablo.fonts.fontformal10, Riiablo.colors.gold, Riiablo.colors.white,
            new TextureRegionDrawable(Riiablo.textures.white));
        final List<GameSession> list = new List<>(style3);
        list.setPosition(14, 54);
        list.setSize(158, 177);
        addActor(list);

        final TextButton btnCancel = new TextButton(5134, cancelbuttonblankStyle);
        btnCancel.addListener(cancelListener);
        btnCancel.setPosition(18, 14);
        addActor(btnCancel);
        final TextButton btnJoinGame = new TextButton(5151, gamebuttonblankStyle);
        btnJoinGame.setPosition(179, 14);
        addActor(btnJoinGame);
        setSize(getBackground().getMinWidth(), getBackground().getMinHeight());

        btnJoinGame.setDisabled(true);
        list.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            list.getSelection().setRequired(true);
            btnJoinGame.setDisabled(list.getSelection().isEmpty());
            GameSession selected = list.getSelected();
            tfGameName.setText(selected != null ? selected.toString() : "");
          }
        });
        btnJoinGame.addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (btnJoinGame.isDisabled()) return;
            GameSession session = list.getSelected();
            tryJoinGame(session);
          }
        });

        final EventListener keyListener = new InputListener() {
          @Override
          public boolean keyDown(InputEvent e, int keycode) {
            switch (keycode) {
              case Input.Keys.ESCAPE:
                return EventUtils.click(btnCancel);

              case Input.Keys.ENTER:
                return EventUtils.click(btnJoinGame);
            }

            return super.keyDown(e, keycode);
          }
        };
        setListener(new TabAdapter() {
          @Override
          public void exited() {
            removeListener(keyListener);
          }

          @Override
          public void entered() {
            addListener(keyListener);
            list.clearItems();
            list.getSelection().setRequired(false);
            stage.setKeyboardFocus(tfGameName);

            ListGames(new ResponseListener() {
              @Override
              public void handleResponse(MCP packet) {
                Array<GameSession> sessions = new Array<>();
                ListGames listGames = (ListGames) packet.data(new ListGames());
                for (int i = 0, length = listGames.gamesLength(); i < length; i++) {
                  sessions.add(new GameSession(listGames.games(i)));
                }
                Gdx.app.log(TAG, sessions.toString());
                list.setItems(sessions);
              }

              @Override
              public void failed(Throwable t) {
                Gdx.app.error(TAG, t.getMessage(), t);
              }
            });
          }
        });
      }});
    }};
    stage.addActor(right);

    final Table left = new Table() {{
      setSize(350, 332);
      setPosition(55, 115);
      add(taChatOutput = new TextArea("", new TextArea.TextFieldStyle() {{
        font = Riiablo.fonts.fontformal10;
        fontColor = Riiablo.colors.white;
        cursor = new TextureRegionDrawable(Riiablo.textures.white);
      }}) {{
        setDisabled(true);
      }}).grow().row();
      add(tfChatInput = new TextField("", textFieldStyle) {{
        setTextFieldListener(new TextFieldListener() {
          @Override
          public void keyTyped(com.badlogic.gdx.scenes.scene2d.ui.TextField textField, char c) {
            if (c == '\r' || c == '\n') {
              if (socket != null && socket.isConnected()) {
                out.println(textField.getText());
                textField.setText("");
              }
            }
          }
        });
      }}).growX();
    }};
    stage.addActor(left);
    stage.setKeyboardFocus(tfChatInput);

    Riiablo.input.addProcessor(stage);
    connect();
    connectToMCP();
  }

  // BNCS
  private void connect() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          socket = Gdx.net.newClientSocket(Net.Protocol.TCP, Riiablo.client.getRealm(), 6113, new SocketHints());
          in = IOUtils.buffer(new InputStreamReader(socket.getInputStream()));
          out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Throwable t) {
          Gdx.app.error(TAG, t.getMessage());
          taChatOutput.appendText(t.getMessage());
          taChatOutput.appendText("\n");
        }
      }
    }).start();
  }

  private void connectToMCP() {
    assert connection == null;
    Socket socket = null;
    try {
      socket = Gdx.net.newClientSocket(Net.Protocol.TCP, Riiablo.client.getRealm(), 6111, null);
      connection = new MCPConnection(socket);
      connection.start();
    } catch (GdxRuntimeException t) {
      Gdx.app.error(TAG, t.getMessage());
      if (connection != null) connection.kill.set(true);
      else if (socket != null) socket.dispose();
    }
  }

  @Override
  public void hide() {
    Riiablo.input.removeProcessor(stage);
    IOUtils.closeQuietly(out);
    if (socket != null) socket.dispose();
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(waitingroombkgdDescriptor.fileName);
    Riiablo.assets.unload(blankbckgDescriptor.fileName);
    Riiablo.assets.unload(creategamebckgDescriptor.fileName);
    Riiablo.assets.unload(joingamebckgDescriptor.fileName);
    Riiablo.assets.unload(chatleftbuttonsDescriptor.fileName);
    Riiablo.assets.unload(chatrighttopbuttonsDescriptor.fileName);
    Riiablo.assets.unload(cancelbuttonblankDescriptor.fileName);
    Riiablo.assets.unload(gamebuttonblankDescriptor.fileName);
    if (connection != null) connection.dispose();
  }

  @Override
  public void render(float delta) {
    if (in != null) {
      try {
        for (String str; in.ready() && (str = in.readLine()) != null;) {
          taChatOutput.appendText(str);
          taChatOutput.appendText("\n");
        }
      } catch (IOException e) {
        Gdx.app.error(TAG, e.getMessage());
      }
    }

    PaletteIndexedBatch b = Riiablo.batch;
    b.begin(Riiablo.palettes.act1);
    b.draw(waitingroombkgd,
        (stage.getWidth() / 2) - (waitingroombkgd.getRegionWidth() / 2),
        stage.getHeight() - waitingroombkgd.getRegionHeight() + 72);
    b.end();

    stage.act(delta);
    stage.draw();
  }

  private void process(Socket socket, MCP packet) throws IOException {
    switch (packet.dataType()) {
      case MCPData.ConnectionClosed:
        Gdx.app.debug(TAG, "Connection closed :(");
        break;
      case MCPData.ConnectionAccepted:
        Gdx.app.debug(TAG, "Connection accepted!");
        break;
      default:
        Gdx.app.error(TAG, "Unknown packet type: " + packet.dataType());
    }
  }

  private void ListGames(ResponseListener listener) {
    Gdx.app.debug(TAG, "Requesting games list");
    FlatBufferBuilder builder = new FlatBufferBuilder();
    ListGames.startListGames(builder);
    int listGamesOffset = ListGames.endListGames(builder);
    int id = MCP.createMCP(builder, MCPData.ListGames, listGamesOffset);
    builder.finish(id);
    ByteBuffer data = builder.dataBuffer();
    connection.sendRequest(data, listener);
  }

  private void CreateGame(GameSession session, ResponseListener listener) {
    Gdx.app.debug(TAG, "Creating game " + session);
    FlatBufferBuilder builder = new FlatBufferBuilder();
    int gameNameOffset = builder.createString(session.name);
    int descriptionOffset = builder.createString(session.desc);
    CreateGame.startCreateGame(builder);
    CreateGame.addGameName(builder, gameNameOffset);
    CreateGame.addDescription(builder, descriptionOffset);
    int createGameOffset = CreateGame.endCreateGame(builder);
    int id = MCP.createMCP(builder, MCPData.CreateGame, createGameOffset);
    builder.finish(id);
    ByteBuffer data = builder.dataBuffer();
    connection.sendRequest(data, listener);
  }

  private void JoinGame(GameSession session, ResponseListener listener) {
    Gdx.app.debug(TAG, "Joining game " + session);
    FlatBufferBuilder builder = new FlatBufferBuilder();
    int gameNameOffset = builder.createString(session.name);
    JoinGame.startJoinGame(builder);
    JoinGame.addGameName(builder, gameNameOffset);
    int joinGameOffset = JoinGame.endJoinGame(builder);
    int id = MCP.createMCP(builder, MCPData.JoinGame, joinGameOffset);
    builder.finish(id);
    ByteBuffer data = builder.dataBuffer();
    connection.sendRequest(data, listener);
  }

  private void tryJoinGame(final GameSession session) {
    JoinGame(session, new ResponseListener() {
      @Override
      public void handleResponse(MCP packet) {
        JoinGame joinGame = (JoinGame) packet.data(new JoinGame());
        session.setConnectInfo(joinGame);
        switch (joinGame.result()) {
          case Result.SUCCESS:
            Gdx.app.debug(TAG, "Session joined! " + session + "@" + session.ip + ":" + session.port);
//            Socket socket = Gdx.net.newClientSocket(Net.Protocol.TCP, "127.0.0.1", session.port, null);
//            socket.dispose();
            //Riiablo.client.pushScreen(new GameScreen(player, socket));
            break;
          case Result.GAME_DOES_NOT_EXIST:
            Gdx.app.debug(TAG, "GAME_DOES_NOT_EXIST");
            break;
          case Result.GAME_IS_FULL:
            Gdx.app.debug(TAG, "GAME_IS_FULL");
            break;
        }
      }

      @Override
      public void failed(Throwable t) {
        Gdx.app.error(TAG, t.getMessage(), t);
      }
    });
  }

  static class TabbedPane extends Container<TabbedPane.TabGroup> {
    TextureRegion defaultBackground;
    ClickListener clickListener;
    ButtonGroup buttons = new ButtonGroup();
    ObjectMap<String, TabGroup> map = new ObjectMap<>();
    TextureRegionDrawable background = new TextureRegionDrawable();
    TabbedPane(TextureRegion defaultBackground) {
      background.setRegion(this.defaultBackground = defaultBackground);
      setBackground(background);
      buttons.setMinCheckCount(0);
      clickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          Button button = (Button) event.getListenerActor();
          TabGroup previous = getActor();
          TabGroup content = map.get(button.getName());
          setActor(content);
          setBackground(content.background);
          content.exited();
          content.entered();
        }
      };
    }

    public void addTab(TextButton button, TabGroup content) {
      button.setName(button.getText());
      button.setProgrammaticChangeEvents(false);
      button.addListener(clickListener);

      buttons.add(button);
      buttons.setMinCheckCount(1);
      map.put(button.getName(), content);
    }

    public void clearSelection() {
      setActor(null);
      setBackground(defaultBackground);
      buttons.setMinCheckCount(0);
      buttons.uncheckAll();
    }

    public Object getSelected() {
      return buttons.getChecked();
    }

    public void setBackground(TextureRegion texture) {
      background.setRegion(texture);
    }

    static class TabGroup extends Group implements TabListener {
      TextureRegion background;
      TabListener listener;

      void setBackground(TextureRegion background) {
        this.background = background;
      }

      void setListener(TabListener l) {
        this.listener = l;
      }

      @Override
      public void entered() {
        if (listener != null) listener.entered();
      }

      @Override
      public void exited() {
        if (listener != null) listener.exited();
      }
    }

    interface TabListener {
      void entered();
      void exited();
    }

    static abstract class TabAdapter implements TabListener {
      @Override public void entered() {}
      @Override public void exited() {}
    }
  }

  private enum State {
    PENDING,
    WAITING
  }

  interface ResponseListener {
    void handleResponse(MCP packet);
    void failed(Throwable t);
  }

  class MCPConnection extends Thread implements Disposable {
    Socket socket;
    ByteBuffer buffer = BufferUtils.newByteBuffer(4096);
    AtomicBoolean kill = new AtomicBoolean(false);

    FlatBufferBuilder builder = new FlatBufferBuilder();
    LobbyScreen.State state = LobbyScreen.State.PENDING;

    MCPConnection(Socket socket) {
      super(MCPConnection.class.getName());
      this.socket = socket;
    }

    public void sendRequest(ByteBuffer data, ResponseListener listener) {
      if (state != LobbyScreen.State.WAITING) throw new IllegalStateException("Sending request before connection has been accepted!");
      try {
        OutputStream out = socket.getOutputStream();
        WritableByteChannel channelOut = Channels.newChannel(out);
        channelOut.write(data);

        buffer.clear();
        buffer.mark();
        InputStream in = socket.getInputStream();
        ReadableByteChannel channelIn = Channels.newChannel(in);
        channelIn.read(buffer);
        buffer.limit(buffer.position());
        buffer.reset();

        MCP packet = MCP.getRootAsMCP(buffer);
        Gdx.app.log(TAG, "packet type " + MCPData.name(packet.dataType()));
        listener.handleResponse(packet);
      } catch (Throwable t) {
        listener.failed(t);
      }
    }

    @Override
    public void run() {
      Gdx.app.log(TAG, "Connecting to MCP " + socket.getRemoteAddress());
      while (!kill.get()) {
        try {
          switch (state) {
            case PENDING: {
              if (DEBUG_CONNECTION) Gdx.app.debug(TAG, "pending connection...");
              buffer.clear();
              buffer.mark();
              ReadableByteChannel in = Channels.newChannel(socket.getInputStream());
              in.read(buffer);
              buffer.limit(buffer.position());
              buffer.reset();

              MCP packet = MCP.getRootAsMCP(buffer);
              Gdx.app.log(TAG, "packet type " + MCPData.name(packet.dataType()));
              process(socket, packet);
              state = LobbyScreen.State.WAITING;
            }
              break;
            case WAITING:
              try {
                Thread.sleep(100); // sleep to save cpu
              } catch (Throwable ignored) {}
              break;
          }
        } catch (Throwable t) {
          Gdx.app.log(TAG, t.getMessage(), t);
          kill.set(true);
        }
      }

      Gdx.app.log(TAG, "closing socket...");
      if (socket != null) socket.dispose();
    }

    @Override
    public void dispose() {
      kill.set(true);
    }
  }
}
