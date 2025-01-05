package com.riiablo.tool.mpqviewer;

import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisTree;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.internal.PreferencesIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.riiablo.Colors;
import com.riiablo.Riiablo;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetManager;
import com.riiablo.asset.AssetUtils;
import com.riiablo.asset.BlockLoader;
import com.riiablo.asset.BlockParams;
import com.riiablo.asset.adapter.MpqFileHandleAdapter;
import com.riiablo.asset.loader.CofLoader;
import com.riiablo.asset.loader.Dc6Loader;
import com.riiablo.asset.loader.DccLoader;
import com.riiablo.asset.loader.Dt1Loader;
import com.riiablo.asset.param.DcParams;
import com.riiablo.asset.param.Dt1Params;
import com.riiablo.asset.param.MpqParams;
import com.riiablo.file.Animation;
import com.riiablo.file.Cof;
import com.riiablo.file.CofInfo;
import com.riiablo.file.Dc;
import com.riiablo.file.Dc6;
import com.riiablo.file.Dc6Info;
import com.riiablo.file.Dcc;
import com.riiablo.file.DccInfo;
import com.riiablo.map5.Dt1Info;
import com.riiablo.file.Palette;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.map5.Block;
import com.riiablo.map5.Dt1;
import com.riiablo.map5.Tile;
import com.riiablo.map5.TileRenderer;
import com.riiablo.mpq.widget.DirectionActor;
import com.riiablo.mpq_bytebuf.MpqFileHandle;
import com.riiablo.mpq_bytebuf.MpqFileResolver;
import com.riiablo.tool.Lwjgl3Tool;
import com.riiablo.tool.Tool;
import com.riiablo.tool.mpqviewer.widget.BorderedVisImageButton;
import com.riiablo.tool.mpqviewer.widget.BorderedVisTextField;
import com.riiablo.tool.mpqviewer.widget.CollapsibleVisTable;
import com.riiablo.tool.mpqviewer.widget.TabbedPane;

import static com.badlogic.gdx.utils.Align.bottomRight;
import static com.badlogic.gdx.utils.Align.center;
import static com.badlogic.gdx.utils.Align.top;
import static com.badlogic.gdx.utils.Align.topLeft;
import static com.kotcrab.vis.ui.widget.file.FileChooser.Mode.OPEN;
import static com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode.DIRECTORIES;
import static com.riiablo.graphics.PaletteIndexedPixmap.INDEXED;

public class MpqViewer extends Tool {
  private static final Logger log = LogManager.getLogger(MpqViewer.class);

  public static void main(String[] args) throws Exception {
    Lwjgl3Tool.create(MpqViewer.class, "mpq-viewer", args)
        .size(1600, 1080, true) // arbitrary, comfortable widget layout
        .config((Lwjgl3Tool.Lwjgl3ToolConfigurator) config -> {
          config.setWindowSizeLimits(640, 480, -1, -1);
          config.useVsync(false);
          config.setForegroundFPS(300);
        })
        .start();
  }

  public static final ClickListener SCROLL_ON_HOVER = new ClickListener() {
    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
      event.getStage().setScrollFocus(event.getTarget());
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
      event.getStage().setScrollFocus(null);
    }
  };

  @Override
  protected void createCliOptions(Options options) {
    super.createCliOptions(options);
    options.addOption(Option
        .builder("f")
        .longOpt("file")
        .desc("initial file to open")
        .hasArg()
        .argName("path")
        .build());
    options.addOption(Option
        .builder("d")
        .longOpt("debug")
        .desc("enabled debug mode")
        .build());
  }

  @Override
  protected void handleCliOptions(String cmd, Options options, CommandLine cli) throws Exception {
    super.handleCliOptions(cmd, options, cli);
    initialFile = cli.getOptionValue("file");
    debugMode = cli.hasOption("debug");
  }

  void title(String fileName) {
    final String title;
    if (fileName == null) {
      title = i18n("mpq-viewer");
    } else {
      title = i18n("mpq-viewer-with-file", fileName);
    }

    log.debug("title -> {}", title);
    Gdx.graphics.setTitle(title);
  }

  public String i18n(String key, Object... args) {
    return bundle.format(key, args);
  }

  public static Skin getSkin() {
    return instance.mpqViewerAssets;
  }

  public static MpqViewer instance;

  static void click(Actor actor) {
    InputEvent event = new InputEvent();
    event.setListenerActor(actor);
    for (EventListener l : actor.getListeners()) {
      if (l instanceof ClickListener) ((ClickListener) l).clicked(event, 0, 0);
    }
  }

  Skin mpqViewerAssets;
  MpqFileResolver mpqs;
  AssetManager assets;

  PaletteIndexedBatch batch;
  ShaderProgram shader;
  ShapeRenderer shapes;

  String initialFile;
  boolean debugMode;

  Preferences prefs;
  I18NBundle bundle;

  Stage stage;
  VisTable root;

  FileChooser fileChooser;
  ColorPicker colorPicker;

  MenuBar menu;
  Menu fileMenu;
  MenuItem file_open;
  MenuItem file_exit;
  Menu optionsMenu;
  MenuItem options_checkExisting;
  MenuItem options_useExternalList;

  VisTable content;
  VisSplitPane verticalSplit;
  VisSplitPane horizontalSplit;

  VisTextField addressBar;
  PopupMenu addressBarMenu;
  MenuItem address_copy;
  MenuItem address_copyFixed;
  MenuItem address_paste;
  ClickListener address_paste_clickListener;

  VisTree fileTree;
  VisScrollPane fileTreeScroller;
  VisTextField fileTreeFilter;
  Trie<String, Node> fileTreeNodes;
  Trie<String, Node> fileTreeCofNodes;

  Renderer renderer;
  VisScrollPane rendererScroller;
  Cell<Stack> rendererStack;
  FullscreenListener fullscreenListener;
  PopupMenu rendererMenu;
  MenuItem renderer_changeBackground;

  VisTable controlPanel;
  Array<CollapsibleVisTable> controlPanels;

  CollapsibleVisTable animationControls;
  TabbedPane animationControlsTabs;
  int ANIMATION_TAB = -1;
  int PAGE_TAB = -1;

  // Animation tab controls
  Button btnPlayPause;
  Button btnFirstFrame;
  Button btnLastFrame;
  Button btnPrevFrame;
  Button btnNextFrame;
  DirectionActor daDirection;
  VisLabel lbDirection;
  VisSlider slDirection;
  VisLabel lbFrameIndex;
  VisSlider slFrameIndex;
  VisLabel lbFrameDuration;
  VisSlider slFrameDuration;
  VisCheckBox cbDebugMode;
  VisSelectBox<BlendModes> sbBlendMode;
  VisImageButton btnBlendColor;
  final Color blendColor = Color.WHITE.cpy();
  Texture blendColorTexture;

  // Page tab controls
  VisLabel lbPage;
  VisSlider slPage;
  Button btnFirstPage;
  Button btnLastPage;
  Button btnPrevPage;
  Button btnNextPage;
  VisLabel lbDirectionPage;
  VisSlider slDirectionPage;
  VisSelectBox<BlendModes> sbBlendModePage;

  CollapsibleVisTable tileControls;
  Button btnFirstTile;
  Button btnLastTile;
  Button btnPrevTile;
  Button btnNextTile;
  VisLabel lbTileIndex;
  VisSlider slTileIndex;
  VisCheckBox cbTileDebug;

  CollapsibleVisTable dt1Controls;
  Dt1Info dt1Info;

  CollapsibleVisTable paletteControls;
  Trie<String, Texture> palettes;
  VisList<String> paletteList;
  VisScrollPane paletteScroller;

  CollapsibleVisTable dccControls;
  DccInfo dccInfo;

  CollapsibleVisTable dc6Controls;
  Dc6Info dc6Info;

  CollapsibleVisTable cofControls;
  CofInfo cofInfo;
  EnumMap<Cof.Keyframe, VisLabel> lbKeyframes;
  VisList<String> components;
  VisScrollPane componentScroller;
  VisList<String> wclasses;
  VisScrollPane wclassScroller;
  ObjectMap<String, Array<String>> compClasses;
  String selectedWClass[];
  static final String[] DC_EXTS = new String[] { "DCC", "DC6" };
  static final ObjectIntMap<String> COMP_TO_ID = new ObjectIntMap<>();
  static {
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.HD), Cof.Component.HD);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.TR), Cof.Component.TR);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.LG), Cof.Component.LG);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.RA), Cof.Component.RA);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.LA), Cof.Component.LA);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.RH), Cof.Component.RH);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.LH), Cof.Component.LH);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.SH), Cof.Component.SH);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.S1), Cof.Component.S1);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.S2), Cof.Component.S2);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.S3), Cof.Component.S3);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.S4), Cof.Component.S4);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.S5), Cof.Component.S5);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.S6), Cof.Component.S6);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.S7), Cof.Component.S7);
    COMP_TO_ID.put(Cof.Component.toString(Cof.Component.S8), Cof.Component.S8);
  }

  @Override
  public void create() {
    instance = this;
    Gdx.app.setLogLevel(com.badlogic.gdx.utils.Logger.DEBUG);
    LogManager.setLevel(MpqViewer.class.getCanonicalName(), Level.ALL);

    LogManager.setLevel("com.riiablo.file", Level.TRACE);
    // LogManager.setLevel("com.riiablo.asset.AssetManager", Level.TRACE);
    // LogManager.setLevel(Dt1Decoder7.class.getCanonicalName(), Level.TRACE);

    prefs = Gdx.app.getPreferences(MpqViewer.class.getCanonicalName());
    bundle = I18NBundle.createBundle(Gdx.files.internal("lang/MpqViewer"));
    title(null);

    log.debug("loading VisUI...");
    VisUI.load(Gdx.files.internal("skin/x1/uiskin.json"));
    PreferencesIO.setDefaultPrefsName(MpqViewer.class.getCanonicalName());
    FileChooser.setSaveLastDirectory(true);
    // TODO: pack mpq-viewer assets with VisUI skin
    final TextureAtlas mpqViewerAtlas = new TextureAtlas(Gdx.files.internal("skin/mpq-viewer/mpq-viewer.atlas"));
    mpqViewerAssets = new Skin(mpqViewerAtlas);

    log.debug("creating placeholder textures...");
    Dc.MISSING_TEXTURE = new Texture(0, 0, INDEXED);
    Dt1.MISSING_TEXTURE = new Texture(0, 0, INDEXED);

    log.debug("creating menu bar...");
    menu = new MenuBar() {{
      addMenu(fileMenu = new Menu(i18n("menu-file")) {{
        addItem(file_open = new MenuItem(i18n("menu-open")) {{
          setShortcut(Keys.CONTROL_LEFT, Keys.O);
          addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              if (fileChooser != null) return;
              openMpqs();
            }
          });
        }});
        addItem(file_exit = new MenuItem(i18n("menu-exit")) {{
          setShortcut(Keys.ALT_LEFT, Keys.F4);
          addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              Gdx.app.exit(); // calls #dispose()
            }
          });
        }});
      }});
      addMenu(optionsMenu = new Menu(i18n("menu-options")) {{
        addItem(options_checkExisting = new MenuItem(
            i18n("menu-check-files"),
            VisUI.getSkin().getDrawable("check-on")
        ) {{
          setChecked(prefs.getBoolean("menu-check-files", true));
          getImageCell().size(getImage().getPrefWidth(), getImage().getPrefHeight());
          addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              final boolean isChecked = isChecked();
              prefs.putBoolean("menu-check-files", isChecked).flush();
              getImage().setDrawable(VisUI.getSkin(), isChecked ? "check-on" : "check-off");
              reloadMpqs();
            }
          });
        }});
        /*
        // TODO: add support for custom listfile
        addSeparator();
        addItem(options_useExternalList = new MenuItem(
            i18n("menu-custom-listfile"),
            VisUI.getSkin().getDrawable("check-off")
        ) {{
          setChecked(prefs.getBoolean("menu-custom-listfile", false));
          getImageCell().size(getImage().getPrefWidth(), getImage().getPrefHeight());
          addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              final boolean isChecked = isChecked();
              getImage().setDrawable(VisUI.getSkin(), isChecked ? "check-on" : "check-off");
              reloadMpqs();
            }
          });
        }});
        */
      }});
    }};

    content = new VisTable() {{
      add(verticalSplit = new VisSplitPane(null, null, false) {{
        setSplitAmount(0.15f);
        setMinSplitAmount(0.00f);
        setMaxSplitAmount(1.00f);
        setBackground(VisUI.getSkin().getDrawable("grey"));
        setFirstWidget(new VisTable() {{
          pad(4);
          left();
          add(fileTreeFilter = new BorderedVisTextField() {{
            setMessageText(i18n("filter-hint"));
            setFocusTraversal(false);
            addListener(new InputListener() {
              @Override
              public boolean keyDown(InputEvent event, int keycode) {
                if (mpqs != null && keycode == Keys.TAB) {
                  String text = getText().toUpperCase(Locale.ROOT);
                  if (text.endsWith("\\")) {
                    return true;
                  }

                  String key;
                  Node selectedNode = null;
                  SortedMap<String, Node> prefixMap = fileTreeNodes.prefixMap(text);
                  if (prefixMap.isEmpty()) {
                    text = text.trim();
                    if (text.length() != 7) {
                      return true;
                    } else {
                      selectedNode = fileTreeCofNodes.get(text);
                      if (selectedNode == null) return true;
                      key = text;
                      log.debug("Found {} at {}", text, selectedNode.getValue());
                    }
                  } else {
                    key = prefixMap.firstKey();
                  }

                  setText(key);
                  setCursorAtTextEnd();

                  if (selectedNode == null) selectedNode = fileTreeNodes.get(key);
                  if (selectedNode != null) {
                    fileTree.collapseAll();
                    selectedNode.expandTo();

                    Array<Node> children = selectedNode.getChildren();
                    if (children.size > 0) {
                      selectedNode.setExpanded(true);
                    } else {
                      fileTree.getSelection().set(selectedNode);
                    }

                    fileTree.layout();
                    Actor actor = selectedNode.getActor();
                    fileTreeScroller.scrollTo(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight(), false, false);
                  }
                }

                return true;
              }
            });
          }}).growX().row();
          add(new VisTable() {{
            setBackground(VisUI.getSkin().getDrawable("default-pane"));
            add(fileTreeScroller = new VisScrollPane(fileTree = new VisTree() {{
              TreeStyle style = new TreeStyle(getStyle());
              style.plus = mpqViewerAssets.getDrawable("chevron-right");
              style.minus = mpqViewerAssets.getDrawable("chevron-down");
              setStyle(style);
            }}) {
              {
                setStyle(new ScrollPaneStyle(getStyle()) {{
                  vScroll = null;
                  vScrollKnob = mpqViewerAssets.getDrawable("vscroll");
                }});
                // setForceScroll(false, true);
                setFadeScrollBars(false);
                setScrollbarsOnTop(true);
                addListener(SCROLL_ON_HOVER);
              }

              @Override
              protected void drawScrollBars(Batch batch, float r, float g, float b, float a) {
                super.drawScrollBars(batch, r, g, b, a * 0.5f);
              }
            }).grow();
          }}).space(4).grow();
        }});
        setSecondWidget(horizontalSplit = new VisSplitPane(null, null, true) {{
          setSplitAmount(0.60f);
          setMinSplitAmount(0.50f);
          setMaxSplitAmount(1.00f);
          setFirstWidget(new VisTable() {{
            pad(4);
            add(new VisTable() {{
              setBackground(VisUI.getSkin().getDrawable("default-pane"));
              rendererMenu = new PopupMenu() {{
                addItem(renderer_changeBackground = new MenuItem(i18n("renderer-change-background")) {{
                  addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                      if (colorPicker != null) return;
                      ColorPicker cp = colorPicker = new ColorPicker(
                          i18n("renderer-change-background-title"),
                          new ColorPickerAdapter() {
                            @Override
                            public void finished(Color newColor) {
                              renderer.setBackground(newColor);
                              dispose();
                            }

                            @Override
                            public void canceled(Color oldColor) {
                              dispose();
                            }

                            void dispose() {
                              colorPicker = null;
                            }
                          });
                      cp.setColor(renderer.getBackground());
                      stage.addActor(cp.fadeIn());
                    }
                  });
                }});
              }};
              rendererScroller = new VisScrollPane(renderer = new Renderer()) {
                {
                  // copy "list" style into "renderer scroller" style
                  setStyle(new ScrollPaneStyle(VisUI.getSkin().get("list", ScrollPaneStyle.class)) {{
                    hScroll = null;
                    hScrollKnob = mpqViewerAssets.getDrawable("hscroll-light");
                    vScroll = null;
                    vScrollKnob = mpqViewerAssets.getDrawable("vscroll-light");
                  }});
                  // setupFadeScrollBars(0, 0);
                  setFadeScrollBars(false);
                  setSmoothScrolling(false);
                  setFlingTime(0);
                  setOverscroll(false, false);
                  addListener(fullscreenListener = new FullscreenListener());
                  addListener(new ClickListener(Input.Buttons.RIGHT) {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                      rendererMenu.showMenu(stage, event.getStageX(), event.getStageY());
                    }
                  });
                }

                @Override
                public void layout() {
                  super.layout();
                  setScrollPercentX(0.5f);
                  setScrollPercentY(0.5f);
                }

                @Override
                protected float getMouseWheelX() {
                  return 0;
                }

                @Override
                protected float getMouseWheelY() {
                  return 0;
                }
              };

              VisTable overlay = new VisTable();
              overlay.align(topLeft);
              overlay.pad(8);
              //overlay.add(lbFrameIndex = new VisLabel());

              VisTable controls = new VisTable();
              controls.align(bottomRight);
              controls.pad(8);
              controls.defaults().space(4);
              controls.padBottom(controls.getPadBottom() + 18); // this is just a guess
              controls.padRight(controls.getPadRight() + 18); // this is just a guess

              controls.add(new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
                up = mpqViewerAssets.getDrawable("center");
              }}, VisUI.getSkin().getDrawable("border"), i18n("renderer-center")) {{
                addListener(new ClickListener() {
                  @Override
                  public void clicked(InputEvent event, float x, float y) {
                    rendererScroller.setScrollPercentX(0.5f);
                    rendererScroller.setScrollPercentY(0.5f);
                  }
                });
              }}).size(24);

              controls.add(fullscreenListener.fullscreenButton = new BorderedVisImageButton(
                  new VisImageButton.VisImageButtonStyle() {{
                    up = mpqViewerAssets.getDrawable("fullscreen");
                    checked = mpqViewerAssets.getDrawable("fullscreen-exit");
                  }},
                  VisUI.getSkin().getDrawable("border"), i18n("renderer-fullscreen")) {{
                    addListener(new ClickListener() {
                      @Override
                      public void clicked(InputEvent event, float x, float y) {
                        fullscreenListener.fullscreen(isChecked());
                      }
                    });
              }}).size(24);

              rendererStack = stack(rendererScroller, controls, overlay).grow();
            }}).grow();
          }});
          setSecondWidget(new VisTable() {{
            add(controlPanel = new VisTable() {{
              // setBackground(VisUI.getSkin().getDrawable("default-pane"));
            }}).grow();
          }});
        }});
      }}).grow();
    }};

    fileTree.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Selection<Node> selection = fileTree.getSelection();
        if (selection.isEmpty()) return;

        Node node = selection.first();
        if (node.getChildren().size > 0) {
          node.setExpanded(!node.isExpanded());
          selection.remove(node);
          return;
        }

        for (CollapsibleVisTable o : controlPanels) o.setCollapsed(true);

        String filename = (String) fileTree.getSelectedNode().getValue();
        addressBar.setText(filename);
        selectFile(selection, node, filename);

        rendererScroller.setScrollPercentX(0.5f);
        rendererScroller.setScrollPercentY(0.5f);
      }
    });

    // controlPanel.setDebug(true, true);
    controlPanel
        .align(topLeft)
        .pad(4)
        ;
    controlPanel
        .defaults()
        .align(topLeft)
        .growY()
        // .space(4)
        ;
    final float controlPadding = 4;
    final float labelSpacing = 4;
    controlPanel.add(new VisTable() {{
      align(top);
      defaults().growX();
      add(new VisTextButton("1") {{
        addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            animationControls.setCollapsed(!animationControls.isCollapsed());
          }
        });
      }}).row();
      add(new VisTextButton("2") {{
        addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            paletteControls.setCollapsed(!paletteControls.isCollapsed());
          }
        });
      }}).row();
      add(new VisTextButton("3") {{
        addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            // audioPanel.setCollapsed(!audioPanel.isCollapsed());
          }
        });
      }}).row();
      add(new VisTextButton("4") {{
        addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            cofControls.setCollapsed(!cofControls.isCollapsed());
          }
        });
      }}).row();
      add(new VisTextButton("5") {{
        addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            dccControls.setCollapsed(!dccControls.isCollapsed());
          }
        });
      }}).row();
      add(new VisTextButton("6") {{
        addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            dc6Controls.setCollapsed(!dc6Controls.isCollapsed());
          }
        });
      }}).row();
      add(new VisTextButton("7") {{
        addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            dt1Controls.setCollapsed(!dt1Controls.isCollapsed());
          }
        });
      }}).row();
      add(new VisTextButton("8") {{
        addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            // ds1Panel.setCollapsed(!ds1Panel.isCollapsed());
          }
        });
      }}).row();
    }});
    controlPanel.add(animationControls = new CollapsibleVisTable() {{
      // debug();
      add(new VisTable() {{
        add(animationControlsTabs = new TabbedPane() {{
          align(topLeft);
          ANIMATION_TAB = addTab(i18n("animation"), new VisTable() {{
            add(new VisTable() {{
              setBackground(VisUI.getSkin().getDrawable("grey"));
              defaults().size(24);
              add(btnFirstFrame = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
                up = mpqViewerAssets.getDrawable("first-frame");
              }}, VisUI.getSkin().getDrawable("border"), i18n("first-frame")));
              add(btnPrevFrame = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
                up = mpqViewerAssets.getDrawable("prev-frame");
              }}, VisUI.getSkin().getDrawable("border"), i18n("prev-frame")));
              add(btnPlayPause = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
                up = mpqViewerAssets.getDrawable("play");
                checked = mpqViewerAssets.getDrawable("pause");
              }}, VisUI.getSkin().getDrawable("border"), i18n("play-pause")));
              add(btnNextFrame = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
                up = mpqViewerAssets.getDrawable("next-frame");
              }}, VisUI.getSkin().getDrawable("border"), i18n("next-frame")));
              add(btnLastFrame = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
                up = mpqViewerAssets.getDrawable("last-frame");
              }}, VisUI.getSkin().getDrawable("border"), i18n("last-frame")));
            }}).row();
            add(new VisTable() {{
              add(i18n("direction")).space(labelSpacing).growX();
              add(lbDirection = new VisLabel()).row();
              add(slDirection = new VisSlider(0, 0, 1, false) {{
                ChangeListener l;
                addListener(l = new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent event, Actor actor) {
                    lbDirection.setText(i18n("direction-label", getValue() + 1, getMaxValue() + 1));
                  }
                });
                l.changed(null, null);
              }}).growX().colspan(2).row();
              add(daDirection = new DirectionActor(16)).colspan(2).row();
            }}).growX().row();
            add(new VisTable() {{
              add(i18n("frame")).space(labelSpacing).growX();
              add(lbFrameIndex = new VisLabel()).row();
              add(slFrameIndex = new VisSlider(0, 0, 1, false) {{
                ChangeListener l;
                addListener(l = new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent event, Actor actor) {
                    lbFrameIndex.setText(i18n("frame-label", getValue() + 1, getMaxValue() + 1));
                  }
                });
                l.changed(null, null);
              }}).growX().colspan(2).row();
            }}).growX().row();
            add(new VisTable() {{
              add(i18n("speed")).space(labelSpacing).growX();
              add(lbFrameDuration = new VisLabel()).row();
              add(slFrameDuration = new VisSlider(0, 1024, 8, false) {{
                ChangeListener l;
                addListener(l = new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent event, Actor actor) {
                    lbFrameDuration.setText(i18n("speed-label", getValue()));
                  }
                });
                l.changed(null, null);
              }}).growX().colspan(2).row();
            }}).growX().row();
            add(new VisTable() {{
              add(i18n("blend")).space(labelSpacing).growX();
              add(sbBlendMode = new VisSelectBox<BlendModes>() {{
                setItems(BlendModes.values());
                setSelectedIndex(0);
                setDisabled(true); // disabled -- applied through animation
              }}).row();
              add(i18n("blendColor")).space(labelSpacing).growX();
              final int buttonSize = 22;
              if (blendColorTexture != null) blendColorTexture.dispose();
              Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
              try {
                p.drawPixel(0, 0, Color.rgba8888(blendColor));
                blendColorTexture = new Texture(p);
              } finally {
                p.dispose();
              }
              add(btnBlendColor = new VisImageButton(new VisImageButton.VisImageButtonStyle() {{
                imageUp =
                imageDown =
                imageOver =
                imageChecked =
                imageCheckedOver =
                imageDisabled = new TextureRegionDrawable(blendColorTexture) {{
                  setMinSize(buttonSize, buttonSize);
                }};
              }}) {{
                addListener(new ClickListener(Input.Buttons.LEFT) {
                  @Override
                  public void clicked(InputEvent event, float x, float y) {
                    ColorPicker cp = new ColorPicker(
                        i18n("blendColor"),
                        new ColorPickerAdapter() {
                          @Override
                          public void finished(Color newColor) {
                            blendColor.set(newColor);
                            if (blendColorTexture != null) blendColorTexture.dispose();
                            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                            try {
                              p.drawPixel(0, 0, Color.rgba8888(blendColor));
                              blendColorTexture = new Texture(p);
                              getStyle().imageUp =
                              getStyle().imageDown =
                              getStyle().imageOver =
                              getStyle().imageChecked =
                              getStyle().imageCheckedOver =
                              getStyle().imageDisabled = new TextureRegionDrawable(blendColorTexture) {{
                                setMinSize(buttonSize, buttonSize);
                              }};
                            } finally {
                              p.dispose();
                            }
                          }
                        }
                    );
                    cp.setColor(blendColor);
                    stage.addActor(cp.fadeIn());
                  }
                });
              }}).left().row();
            }}).growX().row();
            add(new VisTable() {{
              align(topLeft);
              add(cbDebugMode = new VisCheckBox(i18n("debug-bounds"), debugMode));
            }}).growX().row();
            add().growY();
            setFillParent(true);
          }});
          PAGE_TAB = addTab(i18n("pages"), new VisTable() {{
            add(new VisTable() {{
              setBackground(VisUI.getSkin().getDrawable("grey"));
              defaults().size(24);
              add(btnFirstPage = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
                up = mpqViewerAssets.getDrawable("first-frame");
              }}, VisUI.getSkin().getDrawable("border"), i18n("first-frame")));
              add(btnPrevPage = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
                up = mpqViewerAssets.getDrawable("prev-frame");
              }}, VisUI.getSkin().getDrawable("border"), i18n("prev-frame")));
              add(btnNextPage = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
                up = mpqViewerAssets.getDrawable("next-frame");
              }}, VisUI.getSkin().getDrawable("border"), i18n("next-frame")));
              add(btnLastPage = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
                up = mpqViewerAssets.getDrawable("last-frame");
              }}, VisUI.getSkin().getDrawable("border"), i18n("last-frame")));
            }}).row();
            add(new VisTable() {{
              add(i18n("direction")).space(labelSpacing).growX();
              add(lbDirectionPage = new VisLabel()).row();
              add(slDirectionPage = new VisSlider(0, 0, 1, false) {{
                ChangeListener l;
                addListener(l = new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent event, Actor actor) {
                    lbDirectionPage.setText(i18n("direction-label", getValue() + 1, getMaxValue() + 1));
                  }
                });
                l.changed(null, null);
              }}).growX().colspan(2).row();
            }}).growX().row();
            add(new VisTable() {{
              add(i18n("page")).space(labelSpacing).growX();
              add(lbPage = new VisLabel()).row();
              add(slPage = new VisSlider(0, 0, 1, false) {{
                ChangeListener l;
                addListener(l = new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent event, Actor actor) {
                    lbPage.setText(i18n("page-label", getValue() + 1, getMaxValue() + 1));
                  }
                });
                l.changed(null, null);
              }}).growX().colspan(2).row();
            }}).growX().row();
            add(new VisTable() {{
              add(i18n("blend")).space(labelSpacing).growX();
              add(sbBlendModePage = new VisSelectBox<BlendModes>() {{
                setItems(BlendModes.values());
                setSelectedIndex(0);
                setDisabled(true); // disabled -- applied through animation
              }}).row();
            }}).growX().row();
          }});
        }}).pad(4).grow();
        add().growY();
      }}).growY();
    }
      @Override
      public void setCollapsed(boolean collapsed) {
        super.setCollapsed(collapsed);
        daDirection.setVisible(!collapsed());
      }
    });
    controlPanel.add(tileControls = new CollapsibleVisTable() {{
      align(topLeft);
      add(new VisTable() {{
        setBackground(VisUI.getSkin().getDrawable("default-pane"));
        pad(controlPadding);
        add(new VisTable() {{
          setBackground(VisUI.getSkin().getDrawable("grey"));
          defaults().size(24);
          add(btnFirstTile = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
            up = mpqViewerAssets.getDrawable("first-frame");
          }}, VisUI.getSkin().getDrawable("border"), i18n("first-tile")));
          add(btnPrevTile = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
            up = mpqViewerAssets.getDrawable("prev-frame");
          }}, VisUI.getSkin().getDrawable("border"), i18n("prev-tile")));
          add(btnNextTile = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
            up = mpqViewerAssets.getDrawable("next-frame");
          }}, VisUI.getSkin().getDrawable("border"), i18n("next-tile")));
          add(btnLastTile = new BorderedVisImageButton(new VisImageButton.VisImageButtonStyle() {{
            up = mpqViewerAssets.getDrawable("last-frame");
          }}, VisUI.getSkin().getDrawable("border"), i18n("last-tile")));
        }}).row();
        add(new VisTable() {{
          add(i18n("tile")).space(labelSpacing).growX();
          add(lbTileIndex = new VisLabel()).row();
          add(slTileIndex = new VisSlider(0, 0, 1, false) {{
            ChangeListener l;
            addListener(l = new ChangeListener() {
              @Override
              public void changed(ChangeEvent event, Actor actor) {
                lbTileIndex.setText(i18n("tile-label", getValue() + 1, getMaxValue() + 1));
              }
            });
            l.changed(null, null);
          }}).growX().colspan(2).row();
        }}).row();
        add(new VisTable() {{
          align(topLeft);
          add(cbTileDebug = new VisCheckBox(i18n("debug-bounds"), debugMode));
        }}).growX().row();
        add().growY();
      }}).pad(4).growY();
    }});
    controlPanel.add(paletteControls = new CollapsibleVisTable() {{
      add(new VisTable() {{
        setBackground(VisUI.getSkin().getDrawable("default-pane"));
        pad(controlPadding);
        padTop(0); // 0 on top to account for font height
        // debug();
        add(i18n("palette")).align(topLeft).row();
        add(new VisTable() {{
          String[] paletteNames = new String[]{
              "ACT1", "ACT2", "ACT3", "ACT4", "ACT5",
              "EndGame", "fechar", "loading",
              "Menu0", "menu1", "menu2", "menu3", "menu4",
              "Sky", "STATIC", "Trademark",
              "Units",
          };

          palettes = new PatriciaTrie<>();
          for (String name : paletteNames) {
            FileHandle handle = Gdx.files.internal("palettes/" + name + "/pal.dat");
            log.debug("Reading palette {}", handle);
            ByteBuf buffer = Unpooled.wrappedBuffer(handle.readBytes());
            Palette palette = Palette.read(buffer);
            palettes.put(name, palette.texture());
          }

          paletteList = new VisList<>();
          paletteList.setItems(paletteNames);
          paletteList.setSelectedIndex(0);
          add(paletteScroller = new VisScrollPane(paletteList) {
            {
              setStyle(new ScrollPaneStyle(getStyle()) {{
                vScroll = null;
                vScrollKnob = mpqViewerAssets.getDrawable("vscroll");
              }});
              setBackground(VisUI.getSkin().getDrawable("default-pane"));
              setFadeScrollBars(false);
              setScrollingDisabled(true, false);
              setForceScroll(false, true);
              setOverscroll(false, false);
              setScrollbarsOnTop(true);
              addListener(SCROLL_ON_HOVER);
            }

            @Override
            protected void drawScrollBars(Batch batch, float r, float g, float b, float a) {
              super.drawScrollBars(batch, r, g, b, a * 0.5f);
            }
          }).growY();
        }}).row();
        add().growY();
      }}).pad(4).growY();
    }});
    controlPanel.add(cofControls = new CollapsibleVisTable() {{
      add(new VisTable() {{
        setBackground(VisUI.getSkin().getDrawable("default-pane"));
        pad(controlPadding);
        padTop(0); // 0 on top to account for font height
        // debug();
        add(i18n("cof")).align(topLeft).row();
        add(new VisTable() {{
          defaults().growY();
          add(new VisTable() {{
            add(i18n("triggers")).growX().row();
            add(new VisTable() {{
              setBackground(VisUI.getSkin().getDrawable("default-pane"));
              padLeft(4);
              padRight(4);
              VisLabel label;
              lbKeyframes = new EnumMap<>(Cof.Keyframe.class);
              Cof.Keyframe[] keyframes = Cof.Keyframe.values();
              for (Cof.Keyframe keyframe : keyframes) {
                lbKeyframes.put(keyframe, label = new VisLabel());
                add(keyframe.name()).spaceRight(4).left();
                add(label);
                row();
              }
            }}).growX().minWidth(80).row();
            add().growY();
          }});
          add(new VisTable() {{
            add(i18n("layers")).colspan(2).growX().row();
            add(new VisTable() {{
              setBackground(VisUI.getSkin().getDrawable("default-pane"));
              components = new VisList<>();
              components.setItems(Cof.Component.values());
              components.setSelectedIndex(0);
              add(componentScroller = new VisScrollPane(components) {{
                setStyle(new ScrollPaneStyle(getStyle()) {{
                  vScroll = null;
                  vScrollKnob = mpqViewerAssets.getDrawable("vscroll");
                }});
                setBackground(VisUI.getSkin().getDrawable("default-pane"));
                setFadeScrollBars(false);
                setScrollingDisabled(true, false);
                setForceScroll(false, true);
                setOverscroll(false, false);
                setScrollbarsOnTop(true);
                addListener(SCROLL_ON_HOVER);
              }}).minWidth(32).growY();
            }}).grow();
            add(new VisTable() {{
              setBackground(VisUI.getSkin().getDrawable("default-pane"));
              wclasses = new VisList<>();
              add(wclassScroller = new VisScrollPane(wclasses) {{
                setStyle(new ScrollPaneStyle(getStyle()) {{
                  vScroll = null;
                  vScrollKnob = mpqViewerAssets.getDrawable("vscroll");
                }});
                setBackground(VisUI.getSkin().getDrawable("default-pane"));
                setFadeScrollBars(false);
                setScrollingDisabled(true, false);
                setForceScroll(false, true);
                setOverscroll(false, false);
                setScrollbarsOnTop(true);
                addListener(SCROLL_ON_HOVER);
              }}).minWidth(64).growY();
            }}).grow();
          }}).growY();
          add(new VisTable() {{
            add(cofInfo = new CofInfo()).row();
            add().growY();
          }});
        }}).grow();
      }}).pad(4).growY();
    }});
    controlPanel.add(dccControls = new CollapsibleVisTable() {{
      add(new VisTable() {{
        setBackground(VisUI.getSkin().getDrawable("default-pane"));
        pad(controlPadding);
        padTop(0); // 0 on top to account for font height
        // debug();
        add(i18n("dcc")).align(topLeft).row();
        add(dccInfo = new DccInfo()).row();
        add().growY();
      }}).pad(4).growY();
    }});
    controlPanel.add(dc6Controls = new CollapsibleVisTable() {{
      add(new VisTable() {{
        setBackground(VisUI.getSkin().getDrawable("default-pane"));
        pad(controlPadding);
        padTop(0); // 0 on top to account for font height
        // debug();
        add(i18n("dc6")).align(topLeft).row();
        add(dc6Info = new Dc6Info()).row();
        add().growY();
      }}).pad(4).growY();
    }});
    controlPanel.add(dt1Controls = new CollapsibleVisTable() {{
      add(new VisTable() {{
        setBackground(VisUI.getSkin().getDrawable("default-pane"));
        pad(controlPadding);
        padTop(0); // 0 on top to account for font height
        // debug();
        add(i18n("dt1")).align(topLeft).row();
        add(dt1Info = new Dt1Info()).row();
        add().growY();
      }}).pad(4).growY();
    }});

    controlPanels = new Array<>();
    controlPanels.add(animationControls);
    controlPanels.add(tileControls);
    controlPanels.add(paletteControls);
    controlPanels.add(cofControls);
    controlPanels.add(dccControls);
    controlPanels.add(dc6Controls);
    for (CollapsibleVisTable o : controlPanels) {
      o.setCollapsed(true);
    }

    log.debug("constructing root view...");
    root = new VisTable();
    root.add(new VisTable() {{
      final int menuPadding = 4;
      setBackground(VisUI.getSkin().getDrawable("textfield"));
      add(menu.getTable()).pad(menuPadding);
      add(addressBar = new BorderedVisTextField() {{
        setReadOnly(true);
        setDisabled(true);
        setMessageText(i18n("address-hint"));
        addressBarMenu = new PopupMenu() {{
          addItem(address_copy = new MenuItem(i18n("copy")) {{
            addListener(new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                Gdx.app.getClipboard().setContents(addressBar.getText());
              }
            });
          }});
          addItem(address_copyFixed = new MenuItem(i18n("copy_as_path")) {{
            addListener(new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                Gdx.app.getClipboard().setContents(addressBar.getText().replace('\\', '/'));
              }
            });
          }});
          addItem(address_paste = new MenuItem(i18n("paste")) {{
              addListener(address_paste_clickListener = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                  if (mpqs == null) return;

                  String clipboardContents = Gdx.app.getClipboard().getContents();
                  if (clipboardContents == null) return;

                  clipboardContents = clipboardContents.replaceAll("/", "\\\\").toUpperCase(Locale.ROOT);
                  Node selectedNode = fileTreeNodes.get(clipboardContents);
                  if (selectedNode != null) {
                    fileTree.collapseAll();
                    selectedNode.expandTo();

                    Array<Node> children = selectedNode.getChildren();
                    if (children.size > 0) {
                      selectedNode.setExpanded(true);
                    } else {
                      fileTree.getSelection().set(selectedNode);
                    }

                    fileTree.layout();
                    Actor actor = selectedNode.getActor();
                    // fileTreeScroller.scrollTo(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight(), false, false);
                  }
                }
              });
            }});
        }};

        addListener(new ClickListener(Input.Buttons.RIGHT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            addressBarMenu.showMenu(stage, event.getStageX(), event.getStageY());
          }
        });
      }}).pad(menuPadding).growX();
      row();
      addSeparator().colspan(2);
    }}).growX().row();
    root.add(content).grow().row();
    root.setFillParent(true);

    log.debug("constructing stage...");
    stage = new Stage(new ScreenViewport());
    stage.addActor(root);
    stage.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Keys.O && UIUtils.ctrl()) {
          click(file_open);
          return true;
        }

        return false;
      }
    });
    stage.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Keys.F && UIUtils.ctrl()) {
          fileTreeFilter.clearText();
          fileTreeFilter.focusField();
          return true;
        }

        return false;
      }
    });
    stage.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (animationControls.collapsed()) return false;
        if (animationControlsTabs.getTabIndex() != ANIMATION_TAB) return false;
        if (keycode == Keys.SPACE) {
          btnPlayPause.toggle();
          return true;
        } else if (btnPlayPause.isChecked() && keycode == Keys.LEFT) {
          click(slFrameIndex.getValue() <= slFrameIndex.getMinValue() ? btnLastFrame : btnPrevFrame);
          return true;
        } else if (btnPlayPause.isChecked() && keycode == Keys.RIGHT) {
          click(slFrameIndex.getValue() >= slFrameIndex.getMaxValue() ? btnFirstFrame : btnNextFrame);
          return true;
        }
        return false;
      }
    });
    stage.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (animationControls.collapsed()) return false;
        if (animationControlsTabs.getTabIndex() != PAGE_TAB) return false;
        if (keycode == Keys.LEFT) {
          click(slPage.getValue() <= slPage.getMinValue() ? btnLastPage : btnPrevPage);
          return true;
        } else if (keycode == Keys.RIGHT) {
          click(slPage.getValue() >= slPage.getMaxValue() ? btnFirstPage : btnNextPage);
          return true;
        }
        return false;
      }
    });

    log.debug("setting stage as input processor...");
    Gdx.input.setInputProcessor(stage);

    ShaderProgram.pedantic = false;
    Riiablo.shader = shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette3.vert"),
        Gdx.files.internal("shaders/indexpalette3.frag"));
    Riiablo.batch = batch = new PaletteIndexedBatch(256, shader);
    Riiablo.shapes = shapes = new ShapeRenderer();
    Riiablo.colors = new Colors();

    reloadMpqs();

    if (mpqs != null && initialFile != null) {
      log.debug("Selecting initial file: {}", initialFile);
      Gdx.app.getClipboard().setContents(initialFile);
      address_paste_clickListener.clicked(null, -1, -1);
    }
  }

  @Override
  public void dispose() {
    log.debug("disposing shader...");
    shader.dispose();

    log.debug("disposing batch...");
    batch.dispose();

    log.debug("disposing shape renderer...");
    shader.dispose();

    log.debug("disposing palettes...");
    for (Texture palette : palettes.values()) {
      palette.dispose();
    }

    log.debug("flushing preferences...");
    prefs.flush();

    log.debug("disposing renderer...");
    renderer.dispose();

    log.debug("disposing stage...");
    stage.dispose();
    blendColorTexture.dispose();

    log.debug("disposing placeholder textures...");
    Dc.MISSING_TEXTURE.dispose();
    Dt1.MISSING_TEXTURE.dispose();

    log.debug("disposing VisUI...");
    VisUI.dispose();
    mpqViewerAssets.dispose();

    log.debug("disposing asset manager...");
    assets.dispose();
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    assets.sync(1000 / 60L); // 1/60th of a second
    stage.act();
    stage.draw();
  }

  void openMpqs() {
    assert fileChooser == null;
    log.debug("opening mpqs...");

    final String home = prefs.getString("lastHome", Gdx.files.getLocalStoragePath());
    log.debug("home: {}", home);

    FileChooser fc = fileChooser = new FileChooser(OPEN);
    fc.setDirectory(home);
    fc.setSize(800, 600);
    fc.setKeepWithinStage(false);
    fc.setMultiSelectionEnabled(false);
    fc.setSelectionMode(DIRECTORIES);
    fc.setListener(new FileChooserAdapter() {
      @Override
      public void selected(Array<FileHandle> files) {
        assert files.size == 1;
        FileHandle handle = files.first();
        loadMpqs(handle);
        dispose();
      }

      @Override
      public void canceled() {
        dispose();
      }

      void dispose() {
        fileChooser = null;
      }
    });

    stage.addActor(fc.fadeIn());
  }

  void loadMpqs(FileHandle home) {
    log.debug("loading mpqs at {}", home);
    title(home.path());

    if (assets != null) {
      log.debug("disposing asset manager...");
      assets.dispose();
      assets = null;
    }

    mpqs = new MpqFileResolver(home);
    prefs.putString("lastHome", home.path()).flush();
    readMpqs();

    log.debug("initializing asset manager...");
    assets = new AssetManager()
        .resolver(mpqs)
        .paramResolver(Dc.class, DcParams.class)
        .adapter(MpqFileHandle.class, new MpqFileHandleAdapter())
        .loader(Cof.class, new CofLoader())
        .loader(Dcc.class, new DccLoader())
        .loader(Dc6.class, new Dc6Loader())
        .loader(Dt1.class, new Dt1Loader())
        .loader(Block[].class, new BlockLoader())
        ;
  }

  void readMpqs() {
    if (fileTreeNodes == null) {
      fileTreeNodes = new PatriciaTrie<>();
      fileTreeCofNodes = new PatriciaTrie<>();
    } else {
      fileTreeNodes.clear();
      fileTreeCofNodes.clear();
    }

    final FileHandle listfile;
    listfile = Gdx.files.internal("(listfile)");
    //if (options_useExternalList.isChecked()) {
    //  use internal listfile
    //} else {
    //  use external listfile or default to internal
    //  try {
    //    reader = new BufferedReader(new InputStreamReader((new ByteArrayInputStream(mpq.readBytes("(listfile)")))));
    //  } catch (Throwable t) {
    //    reader = Gdx.files.internal(ASSETS + "(listfile)").reader(4096);
    //  }
    //}
    log.debug("listfile: {}", listfile);

    BufferedReader reader = null;
    try {
      reader = listfile.reader(4096);
      Node<Node, Object, Actor> root = new FileTreeNode(new VisLabel("root"));
      final boolean checkExisting = options_checkExisting.isChecked();

      log.debug("parsing listfile...");
      for (String fileName; (fileName = reader.readLine()) != null;) {
        final boolean exists = mpqs.contains(fileName);
        if (checkExisting && !exists) continue;

        String path = FilenameUtils.getFullPathNoEndSeparator(fileName).toUpperCase(Locale.ROOT);
        treeify(fileTreeNodes, root, path);

        // hack to allow accessing files without localization metadata from mpq itself
        VisLabel label = new VisLabel(FilenameUtils.getName(fileName));
        final Node node = new FileTreeNode(label);
        node.setValue(fileName);
        if (!exists) node.setSelectable(false);
        // add listener popup
        String key = fileName.toUpperCase(Locale.ROOT);
        fileTreeNodes.put(key, node);
        if (FilenameUtils.isExtension(key, "cof")) {
          key = FilenameUtils.getBaseName("key");
          fileTreeCofNodes.put(key, node);
        }

        if (path.isEmpty()) {
          root.add(node);
        } else {
          fileTreeNodes.get(path + "\\").add(node);
        }
      }

      sort(root);
      fileTree.clearChildren();
      for (Node child : root.getChildren()) {
        fileTree.add(child);
      }

      fileTree.layout();
      fileTreeFilter.clearText();
    } catch (IOException t) {
      log.error("Failed to parse listfile {}", listfile, t);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  void reloadMpqs() {
    String home = prefs.getString("lastHome");
    if (StringUtils.isNotBlank(home)) {
      loadMpqs(Gdx.files.absolute(home));
    }
  }

  void treeify(Trie<String, Node> nodes, Node root, String path) {
    Node parent = root;
    String[] parts = StringUtils.split(path, '\\');
    StringBuilder builder = new StringBuilder(path.length());
    for (String part : parts) {
      if (part.isEmpty()) {
        break;
      }

      builder.append(part).append('\\');
      String partPath = builder.toString();
      Node node = nodes.get(partPath);
      if (node == null) {
        node = new FileTreeNode(new VisLabel(part));
        nodes.put(partPath, node);
        parent.add(node);
      }

      parent = node;
    }
  }

  void sort(Node root) {
    if (root.getChildren().size == 0) return;
    root.getChildren().sort(new Comparator<Node>() {
      @Override
      public int compare(Node o1, Node o2) {
        boolean o1Empty = o1.getChildren().size == 0;
        boolean o2Empty = o2.getChildren().size == 0;
        if (!o1Empty && o2Empty) {
          return -1;
        } else if (o1Empty && !o2Empty) {
          return 1;
        }

        VisLabel l1 = (VisLabel) o1.getActor();
        VisLabel l2 = (VisLabel) o2.getActor();
        return StringUtils.compare(
            l1.getText().toString().toUpperCase(Locale.ROOT),
            l2.getText().toString().toUpperCase(Locale.ROOT));
      }
    });

    root.updateChildren();
    for (Node child : (Array<Node>) root.getChildren()) {
      sort(child);
    }
  }

  void selectFile(Selection<Node> selection, Node node, String filename) {
    log.debug("selectFile(filename: {})", filename);
    final String extension = FilenameUtils.getExtension(filename).toUpperCase(Locale.ROOT);
    if (extension.equals("COF")) {
      animationControls.setCollapsed(false);
      paletteControls.setCollapsed(false);
      cofControls.setCollapsed(false);
      animationControlsTabs.setDisabled(PAGE_TAB, true);
      Animation anim = Animation.newAnimation();
      AssetDesc<Cof> asset = AssetDesc.of(filename, Cof.class, MpqParams.of());
      AssetDesc[] layers = new AssetDesc[Cof.Component.NUM_COMPONENTS];
      assets.load(asset)
          .addListener(future -> {
            if (future.cause() != null) {
              Dialogs
                  .showErrorDialog(
                      stage,
                      "Failed to load " + asset.path(),
                      future.cause())
                  .show(stage);
              return;
            }
            Cof cof = (Cof) future.getNow();
            cofInfo.setCof(cof);
            anim.setCof(cof);
            renderer.initialize();
          });
      renderer.setDrawable(new DelegatingDrawable<Animation>(anim) {
        @Override
        protected void initialize() {
          Cof cof = delegate.getCof();
          Cof.Keyframe[] keyframes = Cof.Keyframe.values();
          for (Cof.Keyframe keyframe : keyframes) {
            lbKeyframes.get(keyframe).setText(cof.findKeyframe(keyframe));
          }

          String path = filename.toLowerCase();
          String name = FilenameUtils.getBaseName(path).toLowerCase();
          final String token = name.substring(0, 2);
          final String mode = name.substring(2, 4);
          final String wclass = name.substring(4);
          final String type;
          if (path.contains("monsters")) {
            type = "monsters";
          } else if (path.contains("chars")) {
            type = "chars";
          } else if (path.contains("objects")) {
            type = "objects";
          } else {
            type = "null";
          }

          if (compClasses == null) {
            compClasses = new ObjectMap<>();
            for (String comp : components.getItems()) {
              comp = comp.toUpperCase(Locale.ROOT);
              compClasses.put(comp, Array.with("NONE"));
            }
            selectedWClass = new String[Cof.Component.NUM_COMPONENTS];
          } else {
            for (Array<String> a : compClasses.values()) {
              a.clear();
              a.add("NONE");
            }
            Arrays.fill(selectedWClass, null);
          }

          for (String comp : components.getItems()) {
            comp = comp.toUpperCase(Locale.ROOT);
            String prefix = String.format("data\\global\\%s\\%2$s\\%3$s\\%2$s%3$s", type, token, comp).toUpperCase(Locale.ROOT);
            SortedMap<String, Node> dcs = fileTreeNodes.prefixMap(prefix);
            if (dcs.isEmpty()) {
              continue;
            }

            log.trace(prefix);
            Array<String> wclasses = compClasses.get(comp);
            for (String dc : dcs.keySet()) {
              if (!FilenameUtils.isExtension(dc, DC_EXTS)) {
                continue;
              }

              // TODO: hth should probably only be included if wclass doesn't exist to override it
              //       some reuse hth if they don't have a different animation (e.g., assassin)
              if (!dc.substring(prefix.length() + 5, prefix.length() + 8).equalsIgnoreCase(wclass)
               && !dc.substring(prefix.length() + 5, prefix.length() + 8).equalsIgnoreCase("HTH")) {
                continue;
              }

              if (!dc.substring(prefix.length() + 3, prefix.length() + 5).equalsIgnoreCase(mode)) {
                continue;
              }

              String clazz = dc.substring(prefix.length(), prefix.length() + 3);
              if (!wclasses.contains(clazz, false)) wclasses.add(clazz);
              log.trace("\t{} {}", dc, clazz);

              int l = COMP_TO_ID.get(comp, -1);
              if (selectedWClass[l] == null) selectedWClass[l] = clazz;
            }
          }

          log.trace("selectedWClass: {}", Arrays.toString(selectedWClass));
          components.setSelectedIndex(0);
          String comp = components.getSelected().toUpperCase(Locale.ROOT);
          wclasses.setItems(compClasses.get(comp));
          wclasses.setSelected(selectedWClass[COMP_TO_ID.get(comp, -1)]);

          for (int l = 0; l < cof.numLayers(); l++) {
            Cof.Layer layer = cof.layer(l);

            String clazz = selectedWClass[layer.component];
            if (clazz == null) continue;

            comp = components.getItems().get(layer.component);
            String dcPath = String.format("data\\global\\%s\\%2$s\\%3$s\\%2$s%3$s%4$s%5$s%6$s", type, token, comp, clazz, mode, layer.weaponClass);

            for (String ext : DC_EXTS) {
              final Class<? extends Dc<?>> dcType = ext.equals("DCC") ? Dcc.class : Dc6.class;
              AssetDesc<Dc<?>> desc = AssetDesc.of(dcPath + "." + ext, dcType, DcParams.of(0));
              if (mpqs.contains(desc.path())) {
                layers[l] = desc;
                assets.load(desc)
                    .addListener(future -> {
                      log.debug("Loaded {}", desc);
                      delegate.setLayer(layer, (Dc<?>) future.getNow(), true);
                    });
              }
            }
          }

          slDirection.setValue(0);
          slDirection.setRange(0, delegate.getNumDirections() - 1);
          slDirection.fire(new ChangeEvent());
          daDirection.setDirections(delegate.getNumDirections());

          slFrameIndex.setValue(0);
          slFrameIndex.setRange(0, delegate.getNumFramesPerDir() - 1);
          slFrameIndex.fire(new ChangeEvent());

          //sbBlendMode.setSelectedIndex(0);
          //cbCombineFrames.setChecked(false);

          slFrameDuration.setValue(delegate.getFrameDuration());
          delegate.setDirection((int) slDirection.getValue());
          animationControlsTabs.update();

          String palette = paletteList.getSelected();
          Riiablo.batch.setPalette(palettes.get(palette));
        }

        @Override
        public void dispose() {
          super.dispose();
          assets.unload(asset);
          log.debug("Unloading {}", asset);
          for (AssetDesc asset : layers) {
            if (asset != null) {
              assets.unload(asset);
              log.debug("Unloading {}", asset);
            }
          }
        }

        void updateInfo() {
          cofInfo.update(delegate.getDirection(), delegate.getFrame());
        }

        @Override
        protected void changed(ChangeEvent event, Actor actor) {
          if (actor == components) {
            String comp = components.getSelected();
            wclasses.setItems(compClasses.get(comp));
            wclasses.setSelected(selectedWClass[COMP_TO_ID.get(comp, -1)]);
          } else if (actor == daDirection) {
            int d = daDirection.getDirection();
            updateDirection(d);
            delegate.setDirection(d);
            slDirection.setValue(d);
          } else if (actor == slDirection) {
            int d = (int) slDirection.getValue();
            updateDirection(d);
            delegate.setDirection(d);
            updateInfo();
          }
        }

        void updateDirection(int d) {
          if (delegate.getDirection() == d) return;
          log.traceEntry("updateDirection(d: {})", d);

          // Cache old layers to unload after new ones are queued
          AssetDesc[] oldLayers = Arrays.copyOf(layers, layers.length);

          anim.setDirection(d);
          Cof cof = anim.getCof();
          for (int l = 0; l < cof.numLayers(); l++) {
            Cof.Layer layer = cof.layer(l);
            AssetDesc<Dc<?>> desc = AssetDesc.of(layers[l], DcParams.of(d));
            if (mpqs.contains(desc.path())) {
              layers[l] = desc;
              assets.load(desc)
                  .addListener(future -> {
                    log.debug("Loaded {}", desc);
                    delegate.setLayer(layer, (Dc<?>) future.getNow(), true);
                  });
            }
          }

          assets.unloadAll(oldLayers);
        }

        @Override
        public void draw(Batch batch, float x, float y, float width, float height) {
          PaletteIndexedBatch b = Riiablo.batch;
          if (!btnPlayPause.isChecked()) {
            delegate.act();
            slFrameIndex.setValue(delegate.getFrame());
            updateInfo();
          }

          batch.end();

          b.setTransformMatrix(batch.getTransformMatrix());
          b.begin();
          super.draw(b, x, y, width, height);
          b.end();

          shapes.setTransformMatrix(batch.getTransformMatrix());
          if (cbDebugMode.isChecked()) {
            shapes.begin(ShapeRenderer.ShapeType.Line);
            delegate.drawDebug(shapes, x, y);
            shapes.end();
          }

          batch.begin();
        }
      });
    } else if (extension.equals("DCC") || extension.equals("DC6")) {
      animationControls.setCollapsed(false);
      paletteControls.setCollapsed(false);
      final Class<? extends Dc<?>> type = extension.equals("DCC") ? Dcc.class : Dc6.class;
      (type == Dcc.class ? dccControls : dc6Controls).setCollapsed(false);
      Animation anim = Animation.newAnimation();
      AssetDesc<Dc<?>> asset = AssetDesc.of(filename, type, DcParams.of(0, -1));
      AtomicReference<AssetDesc<Dc<?>>> ref = new AtomicReference<>(asset);
      assets.load(asset)
          .addListener(future -> {
            log.debug("Loaded {}", asset);
            final Dc<?> dc = (Dc<?>) future.getNow();
            if (dc instanceof Dcc) {
              Dcc dcc = (Dcc) dc;
              dccInfo.setDcc(dcc);
              animationControlsTabs.setDisabled(PAGE_TAB, true);
            } else if (dc instanceof Dc6) {
              Dc6 dc6 = (Dc6) dc;
              dc6Info.setDc6(dc6);
              int tabIndex = dc.direction(0).frame(0).width() >= Dc6.PAGE_SIZE
                  ? PAGE_TAB
                  : ANIMATION_TAB;
              animationControlsTabs.switchTo(tabIndex);
            }

            anim.edit().layer(dc).build();
            renderer.initialize();
          });
      renderer.setDrawable(new DelegatingDrawable<Animation>(anim) {
        boolean isAnimationTab;
        int page = 0;

        {
          String palette = paletteList.getSelected();
          Riiablo.batch.setPalette(palettes.get(palette));
          isAnimationTab = animationControlsTabs.getTabIndex() == ANIMATION_TAB;
        }

        @Override
        protected void initialize() {
          Dc<?> dc;
          dc = delegate.getLayerRaw(0).getDc();

          slDirection.setValue(0);
          slDirection.setRange(0, dc.numDirections() - 1);
          slDirection.fire(new ChangeEvent());
          daDirection.setDirections(dc.numDirections());

          slFrameIndex.setValue(0);
          slFrameIndex.setRange(0, dc.numFrames() - 1);
          slFrameIndex.fire(new ChangeEvent());

          sbBlendMode.setDisabled(false);
          sbBlendMode.setSelectedIndex(0);
          //cbCombineFrames.setChecked(false);

          slPage.setValue(0);
          slPage.setRange(0, dc.numPages() - 1);
          slPage.fire(new ChangeEvent());

          slFrameDuration.setValue(256);
          delegate.setDirection((int) slDirection.getValue());
          delegate.setFrameDelta((int) slFrameDuration.getValue());
          animationControlsTabs.update();
        }

        @Override
        public void dispose() {
          super.dispose();
          assets.unload(ref.get());
          log.debug("Unloading {}", ref.get());
        }

        void updateInfo() {
          if (anim.getNumFramesPerDir() <= 0) return;
          Dc<?> dc = delegate.getLayerRaw(0).getDc();
          if (dc instanceof Dcc) {
            dccInfo.update(delegate.getDirection(), delegate.getFrame());
          } else if (dc instanceof Dc6) {
            dc6Info.update(delegate.getDirection(), delegate.getFrame());
          }
        }

        @Override
        public void switchedTab(int tabIndex) {
          log.traceEntry("switchedTab(tabIndex: {})", tabIndex);
          isAnimationTab = tabIndex == ANIMATION_TAB;
          final AssetDesc<Dc<?>> asset, oldAsset;
          assets.unload(oldAsset = ref.get());
          ref.set(asset = AssetDesc.of(oldAsset, DcParams.of(0, isAnimationTab ? 0 : 1)));
          assets.load(asset)
              .addListener(future -> {
                if (future.cause() != null) {
                  Dialogs
                      .showErrorDialog(
                          stage,
                          "Failed to load " + asset.path(),
                          future.cause())
                      .show(stage);
                  animationControlsTabs.switchTo(isAnimationTab ? PAGE_TAB : ANIMATION_TAB);
                  return;
                }

                log.debug("Loaded {}", asset);
                final Dc<?> dc = (Dc<?>) future.getNow();
                if (dc instanceof Dcc) {
                  Dcc dcc = (Dcc) dc;
                  dccInfo.setDcc(dcc);
                } else if (dc instanceof Dc6) {
                  Dc6 dc6 = (Dc6) dc;
                  dc6Info.setDc6(dc6);
                }

                delegate.edit().layer(dc).build();
                renderer.initialize();
              });
        }

        void updateDirection(int d) {
          if (delegate.getDirection() == d) return;
          log.traceEntry("updateDirection(d: {})", d);
          final AssetDesc<Dc<?>> oldAsset = ref.get();
          assert oldAsset.params(DcParams.class).direction != d;
          final AssetDesc<Dc<?>> asset = AssetDesc.of(oldAsset, oldAsset.params(DcParams.class).copy(d));
          ref.set(asset);
          assets.load(asset)
              .addListener(future -> {
                if (future.cause() != null) {
                  Dialogs.showDetailsDialog(
                          stage,
                          "Failed to load " + asset.path(),
                          "Error",
                          ExceptionUtils.getStackTrace(future.cause()),
                          true)
                      .show(stage);
                  animationControlsTabs.switchTo(isAnimationTab ? PAGE_TAB : ANIMATION_TAB);
                  return;
                }

                log.debug("Loaded {}", asset);
                assets.unload(oldAsset);
                final Dc<?> dc = (Dc<?>) future.getNow();
                if (dc instanceof Dcc) {
                  Dcc dcc = (Dcc) dc;
                  dccInfo.setDcc(dcc);
                } else if (dc instanceof Dc6) {
                  Dc6 dc6 = (Dc6) dc;
                  dc6Info.setDc6(dc6);
                }

                delegate.edit().layer(dc).build();
              });
        }

        @Override
        protected void clicked(InputEvent event, float x, float y) {
          Actor actor = event.getListenerActor();
          if (actor == btnPlayPause) {
            slFrameIndex.setDisabled(!btnPlayPause.isChecked());
          } else if (actor == btnFirstFrame) {
            delegate.setFrame(0);
            slFrameIndex.setValue(delegate.getFrame());
          } else if (actor == btnLastFrame) {
            delegate.setFrame(delegate.getNumFramesPerDir() - 1);
            slFrameIndex.setValue(delegate.getFrame());
          } else if (actor == btnPrevFrame) {
            int frame = delegate.getFrame();
            if (frame > 0) {
              delegate.setFrame(frame - 1);
              slFrameIndex.setValue(delegate.getFrame());
            }
          } else if (actor == btnNextFrame) {
            int frame = delegate.getFrame();
            if (frame < delegate.getNumFramesPerDir() - 1) {
              delegate.setFrame(frame + 1);
              slFrameIndex.setValue(delegate.getFrame());
            }
          /*
          } else if (actor == cbCombineFrames && cbCombineFrames.isChecked()) {
            if (combined != null) combined.dispose();
            combined = dc6.render((int) slDirection.getValue(), palettes.get(paletteList
            .getSelected()));
          */
          } else if (actor == btnFirstPage) {
            slPage.setValue(page = 0);
          } else if (actor == btnLastPage) {
            Dc<?> dc = delegate.getLayerRaw(0).getDc();
            slPage.setValue(page = dc.numPages() - 1);
          } else if (actor == btnPrevPage) {
            if (page > 0) slPage.setValue(--page);
          } else if (actor == btnNextPage) {
            Dc<?> dc = delegate.getLayerRaw(0).getDc();
            if (page < dc.numPages() - 1) slPage.setValue(++page);
          }
        }

        @Override
        protected void changed(ChangeEvent event, Actor actor) {
          if (actor == daDirection) {
            int d = daDirection.getDirection();
            updateDirection(d);
            delegate.setDirection(d);
            slDirection.setValue(d);
          } else if (actor == slDirection) {
            int d = (int) slDirection.getValue();
            updateDirection(d);
            delegate.setDirection(d);
            updateInfo();
          } else if (actor == paletteList) {
            String palette = paletteList.getSelected();
            Riiablo.batch.setPalette(palettes.get(palette));
            log.debug("palette -> {}", palette);
          } else if (actor == sbBlendMode || actor == btnBlendColor) {
            anim.getLayer(0).setBlendMode(sbBlendMode.getSelectedIndex(), blendColor);
          /*} else if (actor == sbBlendMode) {
            int frame = delegate.getFrame();
            //if (pages != null) {
            //  for (int p = 0; p < pages.size; p++) pages.get(p).dispose();
            //  pages = new DC6.PageList(dc6.pages((int) slDirectionPage.getValue(), palettes.get(paletteList.getSelected()), sbBlendModePage.getSelected()));
            //}
            if (combined != null) {
              combined.dispose();
              combined = dc6.render((int) slDirection.getValue(), palettes.get(paletteList.getSelected()), sbBlendMode.getSelected());
            }*/
          } else if (actor == slFrameIndex) {
            delegate.setFrame((int) slFrameIndex.getValue());
            updateInfo();
          } else if (actor == slFrameDuration) {
            //delegate.setFrameDuration(1 / slFrameDuration.getValue());
            delegate.setFrameDelta((int) slFrameDuration.getValue());
          } else if (actor == slPage) {
            //delegate.setFrame((int) slPage.getValue());
            //} else if (actor == slDirectionPage || /*actor == sbBlendModePage || */(actor == paletteList && pages != null)) {
            //for (int p = 0; p < pages.size; p++) pages.get(p).dispose();
            //pages = new DC6.PageList(dc6.pages((int) slDirectionPage.getValue(), palettes.get(paletteList.getSelected()), sbBlendModePage.getSelected()));
          }
        }

        @Override
        public void draw(Batch batch, float x, float y, float width, float height) {
          if (delegate.getLayerRaw(0) == null) return;
          Dc<?> dc = delegate.getLayerRaw(0).getDc();
          PaletteIndexedBatch b = Riiablo.batch;
          if (!isAnimationTab && dc != null) {
            batch.end();

            b.setTransformMatrix(batch.getTransformMatrix());
            b.begin();
            TextureRegion page = dc.page((int) slDirectionPage.getValue(), (int) slPage.getValue());

            b.draw(page, x - (page.getRegionWidth() / 2f), y - (page.getRegionHeight() / 2f));
            b.end();

            batch.begin();
            return;
          }

          if (!btnPlayPause.isChecked()) {
            delegate.act();
            slFrameIndex.setValue(delegate.getFrame());
            updateInfo();
          }

          batch.end();

          b.setTransformMatrix(batch.getTransformMatrix());
          b.begin();
          super.draw(b, x, y, width, height);
          b.end();

          shapes.setTransformMatrix(batch.getTransformMatrix());
          if (cbDebugMode.isChecked()) {
            shapes.begin(ShapeRenderer.ShapeType.Line);
            delegate.drawDebug(shapes, x, y);
            shapes.end();
          }

          batch.begin();
        }
      });
    } else if (extension.equals("DT1")) {
      tileControls.setCollapsed(false);
      paletteControls.setCollapsed(false);
      final AssetDesc<Dt1> dt1Library = AssetDesc.of(filename, Dt1.class, Dt1Params.library());
      AtomicReference<AssetDesc<Dt1>> ref = new AtomicReference<>();
      AtomicReference<Dt1> dt1Ref = new AtomicReference<>();
      assets.load(dt1Library)
          .addListener(future -> {
            dt1Ref.set((Dt1) future.getNow());
            log.debug("Loaded dt1 library {}", dt1Library);
            renderer.initialize();
          });
      renderer.setDrawable(new DelegatingDrawable<Drawable>() {
        final TileRenderer tileRenderer = TileRenderer.INSTANCE;
        int tileId = -1;
        AssetDesc<Block[]> blocksRef = null;
        Block[] blocks = null;

        @Override
        protected void initialize() {
          Dt1 dt1 = dt1Ref.get();
          dt1Info.setDt1(dt1);

          slTileIndex.setValue(0);
          slTileIndex.setRange(0, dt1.numTiles() - 1);
          slTileIndex.fire(new ChangeEvent());
        }

        @Override
        public void dispose() {
          super.dispose();
          log.debug("Unloading {}", ref.get());
          assets.unload(ref.get());
          log.debug("Unloading dt1 blocks {}", blocksRef);
          assets.unload(blocksRef);
          log.debug("Unloading dt1 library {}", dt1Library);
          assets.unload(dt1Library);
        }

        void updateTile(int t) {
          if (tileId == t) return; // unnecessary call
          log.traceEntry("updateTile(t: {})", t);
          tileId = t;
          blocks = null;
          final AssetDesc<Dt1> oldAsset = ref.get();
          assert oldAsset == null || oldAsset.params(Dt1Params.class).tileId != t;
          final AssetDesc<Dt1> asset = AssetDesc.of(dt1Library, dt1Library.params(Dt1Params.class).copy(t));
          ref.set(asset);
          assets.load(asset)
              .addListener(future -> {
                if (future.cause() != null) {
                  Dialogs.showDetailsDialog(
                          stage,
                          "Failed to load " + asset.path(),
                          "Error",
                          ExceptionUtils.getStackTrace(future.cause()),
                          true)
                      .show(stage);
                  return;
                }

                if (oldAsset != null) {
                  log.debug("Unloading {}", oldAsset);
                  assets.unload(oldAsset);
                }

                if (blocksRef != null) {
                  log.debug("Unloading blocks {}", blocksRef);
                  assets.unload(blocksRef);
                }

                blocksRef = AssetDesc.of(asset.path(), Block[].class, BlockParams.of(asset.params(Dt1Params.class).tileId));
                assets.load(blocksRef)
                    .addListener(future1 -> {
                      blocks = (Block[]) future1.getNow();
                      log.debug("Loaded blocks {}", (Object) blocks);
                      dt1Info.updateBlocks(blocks);
                    });

                log.debug("Loaded {}", asset);
                dt1Info.update(asset.params(Dt1Params.class).tileId);
              });
        }

        @Override
        protected void clicked(InputEvent event, float x, float y) {
          Actor actor = event.getListenerActor();
          if (actor == btnFirstTile) {
            slTileIndex.setValue(0);
          } else if (actor == btnLastTile) {
            Dt1 dt1 = dt1Ref.get();
            slTileIndex.setValue(dt1.numTiles() - 1);
          } else if (actor == btnPrevTile) {
            if (tileId > 0) slTileIndex.setValue(tileId - 1);
          } else if (actor == btnNextTile) {
            Dt1 dt1 = dt1Ref.get();
            if (tileId < dt1.numTiles() - 1) slTileIndex.setValue(tileId + 1);
          }
        }

        @Override
        protected void changed(ChangeEvent event, Actor actor) {
          if (actor == slTileIndex) {
            updateTile((int) slTileIndex.getValue());
          }
        }

        @Override
        public void draw(Batch batch, float x, float y, float width, float height) {
          Dt1 dt1 = dt1Ref.get();
          if (dt1 == null || tileId == -1) return;
          Tile tile = dt1.get(tileId);
          if (tile == null) return;
          PaletteIndexedBatch b = Riiablo.batch;
          batch.end();

          // Apple offset to center tile in frame
          x -= Tile.WIDTH50;
          y -= Tile.HEIGHT;

          String palette = paletteList.getSelected();
          b.setPalette(palettes.get(palette));
          b.setTransformMatrix(batch.getTransformMatrix());
          b.begin();
          tileRenderer.draw(b, tile, x, y);
          b.end();

          shapes.setTransformMatrix(batch.getTransformMatrix());
          if (cbTileDebug.isChecked()) {
            tileRenderer.drawDebug(shapes, tile, x, y);
            shapes.setAutoShapeType(true);
            shapes.begin();
            tileRenderer.drawDebugFlags(shapes, tile, x, y);
            shapes.end();
            if (blocks != null) {
              tileRenderer.drawDebug(shapes, tile, blocks, tile.numBlocks, x, y);
            }
          }

          batch.begin();
        }
      });
    } else if (extension.equals("DAT")) { // palette
      // TODO: update to new asset manager?
      AssetDesc<Palette> asset = AssetDesc.of(filename, Palette.class, MpqParams.of());
      MpqFileHandle handle = mpqs.resolve(asset);
      try {
        Palette pal = Palette.read(handle.buffer());
        renderer.setDrawable(new TextureRegionDrawable(pal.texture()) {{
          final float scale = 8;
          setMinSize(
              pal.texture().getWidth() * scale,
              pal.texture().getHeight() * scale);
        }});
      } finally {
        handle.release();
      }
    }
  }

  static final class FileTreeNode extends Node<Node, Object, Actor> {
    FileTreeNode(Actor actor) {
      super(actor);
    }
  }

  final class FullscreenListener extends ClickListener {
    float verticalSplitAmount;
    float horizontalSplitAmount;
    Button fullscreenButton;

    boolean filled = false;

    @Override
    public void clicked(InputEvent event, float x, float y) {
      final int tapCount = getTapCount();
      if (tapCount >= 2 && tapCount % 2 == 0) {
        fullscreen(!filled);
      }
    }

    void fullscreen(boolean b) {
      filled = b;
      fullscreenButton.setChecked(filled);
      if (filled) {
        verticalSplitAmount = verticalSplit.getSplit();
        horizontalSplitAmount = horizontalSplit.getSplit();
        verticalSplit.setSplitAmount(0);
        horizontalSplit.setSplitAmount(1);
      } else {
        verticalSplit.setSplitAmount(verticalSplitAmount);
        horizontalSplit.setSplitAmount(horizontalSplitAmount);
      }
    }
  }

  enum BlendModes {
    NONE(BlendMode.NONE),
    ID(BlendMode.ID),
    LUMINOSITY(BlendMode.LUMINOSITY),
    LUMINOSITY_TINT(BlendMode.LUMINOSITY_TINT),
    SOLID(BlendMode.SOLID),
    TINT_BLACKS(BlendMode.TINT_BLACKS),
    TINT_WHITES(BlendMode.TINT_WHITES),
    TINT_ID(BlendMode.TINT_ID),
    BRIGHTEN(BlendMode.BRIGHTEN),
    TINT_ID_RED(BlendMode.TINT_ID_RED),
    DARKEN(BlendMode.DARKEN),
    ;

    final int value;
    BlendModes(int value) {
      this.value = value;
    }
  }

  static final class Renderer extends Actor implements Disposable {
    final Color backgroundColor = Color.BLACK.cpy();
    Texture background;
    Drawable drawable;

    Renderer() {
      setSize(2048, 2048);
      updateBackground();
    }

    @Override
    public void dispose() {
      AssetUtils.dispose(background);
      disposeDrawable();
    }

    void disposeDrawable() {
      AssetUtils.dispose(drawable);
      if (drawable instanceof TextureRegionDrawable) {
        ((TextureRegionDrawable) drawable).getRegion().getTexture().dispose();
      }
    }

    protected void initialize() {
      if (drawable instanceof DelegatingDrawable) ((DelegatingDrawable) drawable).initialize();
    }

    public Color getBackground() {
      return backgroundColor;
    }

    public void setBackground(Color color) {
      if (backgroundColor.equals(color)) return;
      backgroundColor.set(color);
      updateBackground();
    }

    void updateBackground() {
      if (background == null) background = new Texture(1, 1, Pixmap.Format.RGBA8888);
      Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      try {
        p.setColor(backgroundColor);
        p.drawPixel(0, 0);
        background.draw(p, 0, 0);
      } finally {
        p.dispose();
      }
    }

    public void setDrawable(Drawable drawable) {
      if (Objects.equals(drawable, this.drawable)) return;
      disposeDrawable();
      this.drawable = drawable;
    }

    @Override
    public void draw(Batch batch, float a) {
      batch.draw(background, 0, 0, getWidth(), getHeight());
      if (drawable != null) drawDelegate(batch, a);
    }

    protected void drawDelegate(Batch batch, float a) {
      /*ShaderProgram s = null;
      if (shader != null && palette != null) {
        batch.flush();
        s = batch.getShader();
        batch.setShader(shader);

        palette.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        shader.setUniformi("ColorTable", 1);
      }*/

      drawable.draw(batch,
          getX(center) - (drawable.getMinWidth() / 2),
          getY(center) - (drawable.getMinHeight() / 2),
          drawable.getMinWidth(), drawable.getMinHeight());

      /*if (shader != null && palette != null) {
        batch.setShader(s);
      }*/
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
      //drawDebugOrigin(shapes);
      super.drawDebug(shapes);
    }
  }

  public class DelegatingDrawable<T extends Drawable> extends BaseDrawable implements Disposable, TabbedPane.TabListener {
    protected T delegate;
    protected ClickListener clickListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        DelegatingDrawable.this.clicked(event, x, y);
      }
    };
    protected ChangeListener changeListener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        DelegatingDrawable.this.changed(event, actor);
      }
    };

    public DelegatingDrawable() {
      sbBlendMode.setDisabled(true);
      animationControlsTabs.addListener(this);
      animationControlsTabs.setDisabled(PAGE_TAB, false);
      animationControlsTabs.setDisabled(ANIMATION_TAB, false);
      btnPlayPause     .addListener(clickListener);
      btnFirstFrame    .addListener(clickListener);
      btnLastFrame     .addListener(clickListener);
      btnPrevFrame     .addListener(clickListener);
      btnNextFrame     .addListener(clickListener);
      paletteList      .addListener(changeListener);
      daDirection      .addListener(changeListener);
      slDirection      .addListener(changeListener);
      slFrameIndex     .addListener(changeListener);
      slFrameDuration  .addListener(changeListener);
      sbBlendMode      .addListener(changeListener);
      //cbCombineFrames.addListener(sbBlendModePage);
      // btnPlayPauseAudio.addListener(clickListener);
      // btnRestartAudio  .addListener(clickListener);
      // slAudioScrubber  .addListener(changeListener);
      // slVolume         .addListener(changeListener);
      animationControls.addListener(changeListener);
      slPage           .addListener(changeListener);
      btnFirstPage     .addListener(clickListener);
      btnLastPage      .addListener(clickListener);
      btnPrevPage      .addListener(clickListener);
      btnNextPage      .addListener(clickListener);
      slDirectionPage  .addListener(changeListener);
      // sbBlendModePage  .addListener(changeListener);
      components       .addListener(changeListener);
      wclasses         .addListener(changeListener);
      btnFirstTile     .addListener(clickListener);
      btnLastTile      .addListener(clickListener);
      btnPrevTile      .addListener(clickListener);
      btnNextTile      .addListener(clickListener);
      slTileIndex      .addListener(changeListener);
    }

    public DelegatingDrawable(T delegate) {
      this();
      this.delegate = delegate;
    }

    protected void initialize() {}

    @Override
    public void dispose() {
      if (delegate instanceof Disposable) ((Disposable) delegate).dispose();
      animationControlsTabs.removeListener(this);
      btnPlayPause     .removeListener(clickListener);
      btnFirstFrame    .removeListener(clickListener);
      btnLastFrame     .removeListener(clickListener);
      btnPrevFrame     .removeListener(clickListener);
      btnNextFrame     .removeListener(clickListener);
      paletteList      .removeListener(changeListener);
      daDirection      .removeListener(changeListener);
      slDirection      .removeListener(changeListener);
      slFrameIndex     .removeListener(changeListener);
      slFrameDuration  .removeListener(changeListener);
      sbBlendMode      .removeListener(changeListener);
      //cbCombineFrames.removeListener(clickListener);
      // btnPlayPauseAudio.removeListener(clickListener);
      // btnRestartAudio  .removeListener(clickListener);
      // slAudioScrubber  .removeListener(changeListener);
      // slVolume         .removeListener(changeListener);
      animationControls.removeListener(changeListener);
      slPage           .removeListener(changeListener);
      btnFirstPage     .removeListener(clickListener);
      btnLastPage      .removeListener(clickListener);
      btnPrevPage      .removeListener(clickListener);
      btnNextPage      .removeListener(clickListener);
      slDirectionPage  .removeListener(changeListener);
      //sbBlendModePage  .removeListener(changeListener);
      components       .removeListener(changeListener);
      wclasses         .removeListener(changeListener);
      btnFirstTile     .removeListener(clickListener);
      btnLastTile      .removeListener(clickListener);
      btnPrevTile      .removeListener(clickListener);
      btnNextTile      .removeListener(clickListener);
      slTileIndex      .removeListener(changeListener);
    }

    protected void clicked(InputEvent event, float x, float y) {}

    protected void changed(ChangeEvent event, Actor actor) {}

    @Override
    public void switchedTab(int tabIndex) {}

    public void setDelegate(T drawable) {
      if (Objects.equals(drawable, delegate)) {
        return;
      }

      if (delegate instanceof Disposable) ((Disposable) delegate).dispose();
      delegate = drawable;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
      if (delegate != null) delegate.draw(batch, x, y, width, height);
    }
  }
}
