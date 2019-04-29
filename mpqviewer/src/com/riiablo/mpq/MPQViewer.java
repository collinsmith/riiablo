package com.riiablo.mpq;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisScrollPane;
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
import com.riiablo.Colors;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.DCC;
import com.riiablo.codec.Palette;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.mpq.widget.CollapsibleVisTable;
import com.riiablo.mpq.widget.TabbedPane;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Objects;
import java.util.SortedMap;

public class MPQViewer {

  private static final String TAG = "MPQViewer";
  private static final String TITLE = "Riiablo MPQ Viewer";
  private static final String ASSETS = "mpqviewer/assets/";
  private static final String EXCEL_PATH = "C:\\Program Files (x86)\\OpenOffice\\program\\scalc.exe";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = TITLE;
    config.addIcon(ASSETS + "ic_launcher_128.png", Files.FileType.Internal);
    config.addIcon(ASSETS + "ic_launcher_32.png",  Files.FileType.Internal);
    config.addIcon(ASSETS + "ic_launcher_16.png",  Files.FileType.Internal);
    config.resizable = true;
    config.width  = 1280;
    config.height = 960;
    config.foregroundFPS = config.backgroundFPS = 144;
    Client client = new Client();
    new LwjglApplication(client, config);
  }

  private static class Client extends ApplicationAdapter {
    Preferences        prefs;
    Stage              stage;
    VisTable           root;
    VisSplitPane       verticalSplit;
    VisSplitPane       horizontalSplit;

    MenuBar            menu;
    Menu               fileMenu;
    MenuItem           file_open;
    MenuItem           file_exit;
    Menu               optionsMenu;
    MenuItem           options_checkExisting;
    //MenuItem         options_useExternalList;

    VisTextField       addressBar;
    PopupMenu          addressBarMenu;
    MenuItem           address_copy;
    MenuItem           address_copyFixed;
    MenuItem           address_paste;

    VisTextField       fileTreeFilter;
    Trie<String, Node> fileTreeNodes;
    VisTree            fileTree;
    VisScrollPane      fileTreeScroller;

    Renderer           renderer;
    VisScrollPane      rendererScroller;
    PopupMenu          rendererMenu;
    MenuItem           renderer_changeBackground;

    VisTable                   optionsPanel;
    Array<CollapsibleVisTable> optionsSubpanels;

    CollapsibleVisTable   imageControlsPanel;
    TabbedPane            imageControls;
    TextButton            btnPlayPause;
    Button                btnFirstFrame;
    Button                btnLastFrame;
    Button                btnPrevFrame;
    Button                btnNextFrame;
    VisLabel              lbDirection;
    VisSlider             slDirection;
    VisLabel              lbFrameIndex;
    VisSlider             slFrameIndex;
    VisLabel              lbFrameDuration;
    VisSlider             slFrameDuration;
    VisCheckBox           cbDebugMode;
    //VisSelectBox<Palette.BlendMode> sbBlendMode;
    //VisCheckBox         cbCombineFrames;
    VisLabel              lbPage;
    VisSlider             slPage;
    Button                btnFirstPage;
    Button                btnLastPage;
    Button                btnPrevPage;
    Button                btnNextPage;
    VisLabel              lbDirectionPage;
    VisSlider             slDirectionPage;
    //VisSelectBox<Palette.BlendMode> sbBlendModePage;

    CollapsibleVisTable   palettePanel;
    Trie<String, Texture> palettes;
    VisList<String>       paletteList;
    VisScrollPane         paletteScroller;

    CollapsibleVisTable   audioPanel;
    VisLabel              lbAudioScrubber;
    VisSlider             slAudioScrubber;
    TextButton            btnPlayPauseAudio;
    Button                btnRestartAudio;
    VisLabel              lbVolume;
    VisSlider             slVolume;

    CollapsibleVisTable   cofPanel;
    EnumMap<COF.Keyframe, VisLabel> lbKeyframes;
    VisList<String>       components;
    VisScrollPane         componentScroller;
    VisList<String>       wclasses;
    VisScrollPane         wclassScroller;
    ObjectMap<String, Array<String>> compClasses;
    String                selectedWClass[];
    static final ObjectIntMap<String> COMP_TO_ID = new ObjectIntMap<>();
    static {
      COMP_TO_ID.put("hd", COF.Component.HD);
      COMP_TO_ID.put("tr", COF.Component.TR);
      COMP_TO_ID.put("lg", COF.Component.LG);
      COMP_TO_ID.put("ra", COF.Component.RA);
      COMP_TO_ID.put("la", COF.Component.LA);
      COMP_TO_ID.put("rh", COF.Component.RH);
      COMP_TO_ID.put("lh", COF.Component.LH);
      COMP_TO_ID.put("sh", COF.Component.SH);
      COMP_TO_ID.put("s1", COF.Component.S1);
      COMP_TO_ID.put("s2", COF.Component.S2);
      COMP_TO_ID.put("s3", COF.Component.S3);
      COMP_TO_ID.put("s4", COF.Component.S4);
      COMP_TO_ID.put("s5", COF.Component.S5);
      COMP_TO_ID.put("s6", COF.Component.S6);
      COMP_TO_ID.put("s7", COF.Component.S7);
      COMP_TO_ID.put("s8", COF.Component.S8);
    }

    PaletteIndexedBatch batch;
    ShaderProgram       shader;
    ShapeRenderer       shapes;
    Texture             DEFAULT_PALETTE;

    @Override
    public void create() {
      Gdx.app.setLogLevel(Logger.DEBUG);
      prefs = Gdx.app.getPreferences("com.riiablo.mpq.MPQViewer");

      VisUI.load();

      menu = new MenuBar() {{
        addMenu(fileMenu = new Menu("File") {{
          addItem(file_open = new MenuItem("Open") {{
            setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.O);
            addListener(new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                openMPQs();
              }
            });
          }});
          addItem(file_exit = new MenuItem("Exit") {{
            setShortcut(Input.Keys.ALT_LEFT, Input.Keys.F4);
            addListener(new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
              }
            });
          }});
        }});
        addMenu(optionsMenu = new Menu("Options") {{
          addItem(options_checkExisting = new MenuItem("Existing Files Only", VisUI.getSkin().getDrawable("check-on")) {{
            setChecked(true);
            addListener(new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                getImage().setDrawable(VisUI.getSkin(), isChecked() ? "check-on" : "check-off");
                reloadMPQ();
              }
            });
          }});
          /*addSeparator();
          addItem(options_useExternalList = new MenuItem("Use External List", VisUI.getSkin().getDrawable("check-off")) {{
            addListener(new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                getImage().setDrawable(VisUI.getSkin(), isChecked() ? "check-on" : "check-off");
                reloadMPQ();
              }
            });
          }});*/
        }});
      }};

      VisTable contentPanel = new VisTable() {{
        add(new VisSplitPane(null, null, false) {{
          verticalSplit = this;
          setSplitAmount(0.20f);
          setMinSplitAmount(0.00f);
          setMaxSplitAmount(1.00f);
          setFirstWidget(new VisTable() {{
            pad(4);
            add(fileTreeFilter = new VisTextField() {{
              setMessageText("filter...");
              setFocusTraversal(false);
              addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                  if (Riiablo.mpqs != null && keycode == Input.Keys.TAB) {
                    String text = getText();
                    if (text.endsWith("\\")) {
                      return true;
                    }

                    SortedMap<String, Node> prefixMap = fileTreeNodes.prefixMap(text);
                    if (prefixMap.isEmpty()) {
                      return true;
                    }

                    String key = prefixMap.firstKey();
                    setText(key);
                    setCursorAtTextEnd();

                    Node selectedNode = fileTreeNodes.get(key);
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
                      fileTreeScroller.scrollTo(actor.getX(), actor.getY(), actor.getWidth(), children.size * selectedNode.getHeight(), false, true);
                    }
                  }

                  return true;
                }
              });
            }}).growX().row();
            add(new VisTable() {{
              setBackground(VisUI.getSkin().getDrawable("default-pane"));
              add(fileTreeScroller = new VisScrollPane(fileTree = new VisTree()) {{
                //setForceScroll(false, true);
                setFadeScrollBars(false);
                addListener(new ClickListener() {
                  @Override
                  public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    stage.setScrollFocus(fileTreeScroller);
                  }

                  @Override
                  public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    stage.setScrollFocus(null);
                  }
                });
              }}).grow();
            }}).space(4).grow();
          }});
          setSecondWidget(new VisTable() {{
            add(new VisSplitPane(null, null, true) {{
              horizontalSplit = this;
              setSplitAmount(0.67f);
              setMinSplitAmount(0.50f);
              setMaxSplitAmount(1.00f);
              setFirstWidget(new VisTable() {{
                pad(4);
                add(new VisTable() {{
                  setBackground(VisUI.getSkin().getDrawable("default-pane"));
                  rendererMenu = new PopupMenu() {{
                    addItem(renderer_changeBackground = new MenuItem("Background") {{
                      addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                          ColorPicker picker = new ColorPicker("Background Color", new ColorPickerAdapter() {
                            @Override
                            public void finished(Color newColor) {
                              renderer.setBackground(newColor);
                            }
                          });
                          picker.setColor(renderer.getBackground());
                          stage.addActor(picker.fadeIn());
                        }
                      });
                    }});
                  }};
                  rendererScroller = new VisScrollPane(renderer = new Renderer()) {
                    {
                      setupFadeScrollBars(0, 0);
                      setFadeScrollBars(true);
                      setSmoothScrolling(false);
                      setFlingTime(0);
                      setOverscroll(false, false);
                      addListener(new ClickListener() {
                        float verticalSplitAmount;
                        float horizontalSplitAmount;

                        boolean filled = false;

                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                          final int tapCount = getTapCount();
                          if (tapCount >= 2 && tapCount % 2 == 0) {
                            if (filled) {
                              verticalSplit.setSplitAmount(verticalSplitAmount);
                              horizontalSplit.setSplitAmount(horizontalSplitAmount);
                            } else {
                              verticalSplitAmount = verticalSplit.getSplit();
                              horizontalSplitAmount = horizontalSplit.getSplit();
                              verticalSplit.setSplitAmount(0);
                              horizontalSplit.setSplitAmount(1);
                            }

                            filled = !filled;
                          }
                        }
                      });
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
                  overlay.align(Align.topLeft);
                  overlay.pad(8);
                  //overlay.add(lbFrameIndex = new VisLabel());

                  VisTable controls = new VisTable();
                  controls.align(Align.bottomLeft);
                  controls.pad(8);
                  controls.add(new VisTextButton("[ ]") {{
                    addListener(new ClickListener() {
                      @Override
                      public void clicked(InputEvent event, float x, float y) {
                        rendererScroller.setScrollPercentX(0.5f);
                        rendererScroller.setScrollPercentY(0.5f);
                      }
                    });
                  }}).size(24);

                  stack(rendererScroller, controls, overlay).grow();
                }}).grow();
              }});
              setSecondWidget(new VisTable() {{
                add(optionsPanel = new VisTable() {{
                  setBackground(VisUI.getSkin().getDrawable("default-pane"));
                }}).pad(4).grow();
              }});
            }}).grow();
          }});
        }}).grow();
      }};
      fileTree.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          Selection<Node> selection = fileTree.getSelection();
          if (selection.isEmpty()) {
            return;
          }

          Node node = selection.first();
          if (node.getChildren().size > 0) {
            node.setExpanded(!node.isExpanded());
            selection.remove(node);
            return;
          }

          for (CollapsibleVisTable o : optionsSubpanels) o.setCollapsed(true);

          MPQFileHandle handle = (MPQFileHandle) fileTree.getSelection().first().getObject();
          addressBar.setText(handle.fileName);
          MPQViewer.Client.this.open(selection, node, handle);

          rendererScroller.setScrollPercentX(0.5f);
          rendererScroller.setScrollPercentY(0.5f);
        }
      });
      final Selection<Node> selection = fileTree.getSelection();
      selection.setRequired(true);
      selection.setMultiple(false);

      //optionsPanel.setDebug(true, true);
      optionsPanel.align(Align.left);
      optionsPanel.pad(4);
      optionsPanel.add(new VisTable() {{
        add(new VisTextButton("1") {{
          addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              imageControlsPanel.setCollapsed(!imageControlsPanel.isCollapsed());
            }
          });
        }}).row();
        add(new VisTextButton("2") {{
          addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              palettePanel.setCollapsed(!palettePanel.isCollapsed());
            }
          });
        }}).row();
        add(new VisTextButton("3") {{
          addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              audioPanel.setCollapsed(!audioPanel.isCollapsed());
            }
          });
        }}).row();
        add(new VisTextButton("4") {{
          addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              cofPanel.setCollapsed(!cofPanel.isCollapsed());
            }
          });
        }}).row();
      }}).align(Align.top).space(4);
      optionsPanel.add(imageControlsPanel = new CollapsibleVisTable() {{
        add(imageControls = new TabbedPane() {{
          align(Align.top);
          addTab("Animation", new VisTable() {{
            align(Align.top);
            add(new VisTable() {{
              add(btnFirstFrame = new VisTextButton("<<"));
              add(btnPrevFrame = new VisTextButton("<"));
              add(btnPlayPause = new VisTextButton("Play") {{
                addListener(new ClickListener() {
                  @Override
                  public void clicked(InputEvent event, float x, float y) {
                    setText(!isChecked() ? "Play" : "Pause");
                  }
                });
              }}).growX();
              add(btnNextFrame = new VisTextButton(">"));
              add(btnLastFrame = new VisTextButton(">>"));
            }}).growX().row();
            add(new VisTable() {{
              add("Direction:").growX();
              add(lbDirection = new VisLabel()).row();
              add(slDirection = new VisSlider(0, 0, 1, false) {{
                addListener(new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent event, Actor actor) {
                    lbDirection.setText((int) (getValue() + 1) + " / " + (int) (getMaxValue() + 1));
                  }
                });
              }}).growX().colspan(2).row();
            }}).growX().row();
            add(new VisTable() {{
              add("Frame:").growX();
              add(lbFrameIndex = new VisLabel()).row();
              add(slFrameIndex = new VisSlider(0, 0, 1, false) {{
                addListener(new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent event, Actor actor) {
                    lbFrameIndex.setText((int) (getValue() + 1) + " / " + (int) (getMaxValue() +
                        1));
                  }
                });
              }}).growX().colspan(2).row();
            }}).growX().row();
            add(new VisTable() {{
              add("Speed:").growX();
              add(lbFrameDuration = new VisLabel()).row();
              add(slFrameDuration = new VisSlider(1, 25, 0.5f, false) {{
                addListener(new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent event, Actor actor) {
                    lbFrameDuration.setText(getValue() + " fps");
                  }
                });
              }}).growX().colspan(2).row();
            }}).growX().row();
            /*add(new VisTable() {{
              add("Blend:").growX();
              add(sbBlendMode = new VisSelectBox<Palette.BlendMode>() {{
                setItems(Palette.BlendMode.values());
                setSelectedIndex(0);
              }}).row();
            }}).growX().row();*/
            add(new VisTable() {{
              add(cbDebugMode = new VisCheckBox("Debug Bounds", false)).growX();
            }}).growX().row();
          }});
          addTab("Pages", new VisTable() {{
            align(Align.top);
            add(new VisTable() {{
              add(btnFirstPage = new VisTextButton("<<"));
              add(btnPrevPage = new VisTextButton("<"));
              add(btnNextPage = new VisTextButton(">"));
              add(btnLastPage = new VisTextButton(">>"));
            }}).growX().row();
            add(new VisTable() {{
              add("Direction:").growX();
              add(lbDirectionPage = new VisLabel()).row();
              add(slDirectionPage = new VisSlider(0, 0, 1, false) {{
                addListener(new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent event, Actor actor) {
                    lbDirectionPage.setText((int) (getValue() + 1) + " / " + (int) (getMaxValue() + 1));
                  }
                });
              }}).growX().colspan(2).row();
            }}).growX().row();
            add(new VisTable() {{
              add("Page:").growX();
              add(lbPage = new VisLabel()).row();
              add(slPage = new VisSlider(0, 0, 1, false) {{
                addListener(new ChangeListener() {
                  @Override
                  public void changed(ChangeEvent event, Actor actor) {
                    lbPage.setText((int) (getValue() + 1) + " / " + (int) (getMaxValue() + 1));
                  }
                });
              }}).growX().colspan(2).row();
            }}).growX().row();
            /*add(new VisTable() {{
              add("Blend:").growX();
              add(sbBlendModePage = new VisSelectBox<Palette.BlendMode>() {{
                setItems(Palette.BlendMode.values());
                setSelectedIndex(0);
              }}).row();
            }}).growX().row();*/
          }});
        }}).grow();
      }}).growY().space(4);
      optionsPanel.add(palettePanel = new CollapsibleVisTable() {{
        add("Palette:").align(Align.left).row();
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
            Palette palette = Palette.loadFromFile(Gdx.files.internal(ASSETS + "palettes/" + name + "/pal.dat"));
            palettes.put(name, palette.render());
          }

          paletteList = new VisList<>();
          paletteList.setItems(paletteNames);
          paletteList.setSelectedIndex(0);
          add(paletteScroller = new VisScrollPane(paletteList) {{
            setBackground(VisUI.getSkin().getDrawable("default-pane"));
            setFadeScrollBars(false);
            setScrollingDisabled(true, false);
            setForceScroll(false, true);
            setOverscroll(false, false);
            addListener(new ClickListener() {
              @Override
              public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                stage.setScrollFocus(paletteScroller);
              }

              @Override
              public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                stage.setScrollFocus(null);
              }
            });
          }}).growY();
        }}).growY();
      }}).growY().space(4);
      optionsPanel.add(audioPanel = new CollapsibleVisTable() {{
        add("Audio:").align(Align.left).row();
        add(new VisTable() {{
          add(btnRestartAudio = new VisTextButton("<<"));
          add(btnPlayPauseAudio = new VisTextButton("Play") {{
            addListener(new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                setText(!isChecked() ? "Play" : "Pause");
              }
            });
          }}).growX();
        }}).growX().row();
        add(new VisTable() {{
          add("Scrubber:").growX();
          add(lbAudioScrubber = new VisLabel()).row();
          add(slAudioScrubber = new VisSlider(0, 0, 0.1f, false) {{
            addListener(new ChangeListener() {
              @Override
              public void changed(ChangeEvent event, Actor actor) {
                lbAudioScrubber.setText(String.format("%.1f / %.1f", getValue(), getMaxValue()));
              }
            });
          }}).growX().colspan(2).row();
        }}).growX().row();
        add(new VisTable() {{
          add("Volume:").growX();
          add(lbVolume = new VisLabel()).row();
          add(slVolume = new VisSlider(0, 1, 0.01f, false) {{
            setValue(0.25f);
            addListener(new ChangeListener() {
              @Override
              public void changed(ChangeEvent event, Actor actor) {
                lbVolume.setText(Integer.toString((int) (getValue() * 100)) + "%");
              }
            });
          }}).growX().colspan(2).row();
        }}).growX().row();
        add().growY();
      }}).growY().space(4);
      optionsPanel.add(cofPanel = new CollapsibleVisTable() {{
        add("COF:").align(Align.left).row();
        add(new VisTable() {{
          add(new VisTable() {{
            add("Triggers:").growX().row();
            add(new VisTable() {{
              setBackground(VisUI.getSkin().getDrawable("default-pane"));
              padLeft(4);
              padRight(4);
              VisLabel label;
              lbKeyframes = new EnumMap<>(COF.Keyframe.class);
              COF.Keyframe[] keyframes = COF.Keyframe.values();
              for (COF.Keyframe keyframe : keyframes) {
                lbKeyframes.put(keyframe, label = new VisLabel());
                add(keyframe.name()).spaceRight(4).left();
                add(label);
                row();
              }
            }}).growX().minWidth(80);
          }}).top();
          add(new VisTable() {{
            add("Layers:").colspan(2).growX().row();
            add(new VisTable() {{
              setBackground(VisUI.getSkin().getDrawable("default-pane"));
              String[] componentNames = new String[]{
                  "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH",
                  "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8",
              };

              components = new VisList<>();
              components.setItems(componentNames);
              components.setSelectedIndex(0);
              add(componentScroller = new VisScrollPane(components) {{
                setFadeScrollBars(false);
                setScrollingDisabled(true, false);
                setForceScroll(false, true);
                setOverscroll(false, false);
                addListener(new ClickListener() {
                  @Override
                  public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    stage.setScrollFocus(componentScroller);
                  }

                  @Override
                  public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    stage.setScrollFocus(null);
                  }
                });
              }}).growY();
            }}).grow();
            add(new VisTable() {{
              setBackground(VisUI.getSkin().getDrawable("default-pane"));
              String[] wclassNames = new String[]{
                  "NONE",
              };

              wclasses = new VisList<>();
              wclasses.setItems(wclassNames);
              wclasses.setSelectedIndex(0);
              add(wclassScroller = new VisScrollPane(wclasses) {{
                setFadeScrollBars(false);
                setScrollingDisabled(true, false);
                setForceScroll(false, true);
                setOverscroll(false, false);
                addListener(new ClickListener() {
                  @Override
                  public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    stage.setScrollFocus(wclassScroller);
                  }

                  @Override
                  public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    stage.setScrollFocus(null);
                  }
                });
              }}).minWidth(64).growY();
            }}).grow();
          }}).growY();
        }}).grow().row();
      }}).growY().space(4);

      optionsSubpanels = new Array<>();
      optionsSubpanels.add(imageControlsPanel);
      optionsSubpanels.add(palettePanel);
      optionsSubpanels.add(audioPanel);
      optionsSubpanels.add(cofPanel);
      for (CollapsibleVisTable o : optionsSubpanels) {
        o.setCollapsed(true);
      }

      root = new VisTable();
      root.add(new VisTable() {{
        setBackground(VisUI.getSkin().getDrawable("textfield"));
        add(menu.getTable()).pad(4);
        add(addressBar = new VisTextField() {{
          setReadOnly(true);
          //setDisabled(true);
          setMessageText("path...");
          setStyle(new VisTextFieldStyle(getStyle()));
          getStyle().background = VisUI.getSkin().getDrawable("default-pane");
          addressBarMenu = new PopupMenu() {{
            addItem(address_copy = new MenuItem("Copy") {{
              addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                  Gdx.app.getClipboard().setContents(addressBar.getText());
                }
              });
            }});
            addItem(address_copyFixed = new MenuItem("Copy as Path") {{
              addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                  Gdx.app.getClipboard().setContents(addressBar.getText().replaceAll("\\\\", "/"));
                }
              });
            }});
            addItem(address_paste = new MenuItem("Paste") {{
              addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                  if (Riiablo.mpqs == null) {
                    return;
                  }

                  String clipboardContents = Gdx.app.getClipboard().getContents();
                  if (clipboardContents == null) {
                    return;
                  }

                  clipboardContents = clipboardContents.replaceAll("/", "\\\\").toLowerCase();
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
                    fileTreeScroller.scrollTo(actor.getX(), actor.getY(), actor.getWidth(), children.size * selectedNode.getHeight(), false, true);
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
        }}).pad(4).growX();
      }}).growX().row();
      root.add(contentPanel).grow();
      root.setFillParent(true);

      DEFAULT_PALETTE = palettes.get("ACT1");
      ShaderProgram.pedantic = false;
      Riiablo.shader = shader = new ShaderProgram(
          Gdx.files.internal(ASSETS + "shaders/indexpalette3.vert"),
          Gdx.files.internal(ASSETS + "shaders/indexpalette3.frag"));
      Riiablo.batch = batch = new PaletteIndexedBatch(256, shader);
      Riiablo.shapes = shapes = new ShapeRenderer();

      Riiablo.colors = new Colors();

      stage = new Stage(new ScreenViewport());
      stage.addActor(root);
      stage.addListener(new InputListener() {
        @Override
        public boolean keyDown(InputEvent event, int keycode) {
          if (keycode == Input.Keys.O && UIUtils.ctrl()) {
            Array<EventListener> listeners = file_open.getListeners();
            for (EventListener l : listeners) {
              if (l instanceof ClickListener) {
                ((ClickListener) l).clicked(null, 0, 0);
              }
            }

            return true;
          }

          return false;
        }
      });
      stage.addListener(new InputListener() {
        @Override
        public boolean keyDown(InputEvent event, int keycode) {
          if (keycode == Input.Keys.F && UIUtils.ctrl()) {
            fileTreeFilter.clearText();
            fileTreeFilter.focusField();
            return true;
          }

          return false;
        }
      });

      Gdx.input.setInputProcessor(stage);

      String home = prefs.getString("home");
      if (home != null && !home.isEmpty()) {
        loadMPQs(Gdx.files.absolute(home));
      }
    }

    @Override
    public void resize(int width, int height) {
      stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
      Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

      stage.act();
      stage.draw();
    }

    @Override
    public void dispose() {
      Gdx.app.debug(TAG, "disposing...");
      prefs.flush();
      stage.dispose();
      Gdx.app.debug(TAG, "disposing palettes...");
      for (Texture palette : palettes.values()) palette.dispose();
    }

    private void openMPQs() {
      FileChooser.setSaveLastDirectory(true);
      FileChooser chooser = new FileChooser(FileChooser.Mode.OPEN);
      chooser.setSize(800, 600);
      chooser.setKeepWithinStage(false);
      chooser.setDirectory(prefs.getString("home", Gdx.files.getLocalStoragePath()));
      chooser.setMultiSelectionEnabled(false);
      chooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
      chooser.setListener(new FileChooserAdapter() {
        @Override
        public void selected(Array<FileHandle> files) {
          assert files.size == 1;
          FileHandle file = files.first();
          loadMPQs(file);
        }
      });

      stage.addActor(chooser.fadeIn());
    }

    private void loadMPQs(FileHandle file) {
      Gdx.graphics.setTitle(TITLE + " - " + file.path());
      Riiablo.mpqs = new MPQFileHandleResolver(Riiablo.home = file);
      prefs.putString("home", file.path());
      prefs.flush();
      readMPQs();
    }

    private void readMPQs() {
      if (fileTreeNodes == null) {
        fileTreeNodes = new PatriciaTrie<>();
      } else {
        fileTreeNodes.clear();
      }

      BufferedReader reader = null;
      try {
        //if (options_useExternalList.isChecked()) {
          reader = Gdx.files.internal(ASSETS + "(listfile)").reader(4096);
        //} else {
        //  try {
        //    reader = new BufferedReader(new InputStreamReader((new ByteArrayInputStream(mpq.readBytes("(listfile)")))));
        //  } catch (Throwable t) {
        //    reader = Gdx.files.internal(ASSETS + "(listfile)").reader(4096);
        //  }
        //}

        Node root = new Node(new VisLabel("root"));
        final boolean checkExisting = options_checkExisting.isChecked();

        String fileName;
        while ((fileName = reader.readLine()) != null) {
          if (checkExisting && !Riiablo.mpqs.contains(fileName)) {
            continue;
          }

          String path = FilenameUtils.getPathNoEndSeparator(fileName).toLowerCase();
          treeify(fileTreeNodes, root, path);

          final MPQFileHandle handle = (MPQFileHandle) Riiablo.mpqs.resolve(fileName);
          VisLabel label = new VisLabel(FilenameUtils.getName(fileName));
          final Node node = new Node(label);
          node.setObject(handle);
          label.addListener(new ClickListener(Input.Buttons.RIGHT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              showPopmenu(node, handle);
            }
          });

          fileTreeNodes.put(fileName.toLowerCase(), node);
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
      } catch (IOException e) {
        throw new GdxRuntimeException("Failed to read list file.", e);
      } finally {
        StreamUtils.closeQuietly(reader);
      }
    }

    private void treeify(Trie<String, Node> nodes, Node root, String path) {
      Node parent = root;
      String[] parts = path.split("\\\\");
      StringBuilder builder = new StringBuilder(path.length());
      for (String part : parts) {
        if (part.isEmpty()) {
          break;
        }

        builder.append(part).append("\\");
        String partPath = builder.toString();
        Node node = nodes.get(partPath);
        if (node == null) {
          node = new Node(new VisLabel(part));
          nodes.put(partPath, node);
          parent.add(node);
        }

        parent = node;
      }
    }

    private void sort(Node root) {
      if (root.getChildren().size == 0) {
        return;
      }

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
          return StringUtils.compare(l1.getText().toString().toLowerCase(), l2.getText().toString().toLowerCase());
        }
      });

      root.updateChildren();
      for (Node child : root.getChildren()) {
        sort(child);
      }
    }

    private void reloadMPQ() {
      if (Riiablo.mpqs != null) {
        loadMPQs(Riiablo.home);
      }
    }

    // TODO: populate
    private void showPopmenu(Node node, final MPQFileHandle handle) {
      PopupMenu menu = new PopupMenu();
      menu.addItem(new MenuItem("Open with...") {{
        addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            try {
              FileHandle f = extract(handle, null);
              Desktop.getDesktop().open(f.file());
            } catch (IOException e) {
              Gdx.app.error(TAG, e.getMessage(), e);
            }
          }
        });
      }});
      menu.addItem(new MenuItem("Extract") {{
        addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            FileChooser.setSaveLastDirectory(true);
            FileChooser chooser = new FileChooser(FileChooser.Mode.SAVE);
            chooser.setSize(800, 600);
            chooser.setKeepWithinStage(false);
            chooser.setDirectory(Gdx.files.getLocalStoragePath());
            chooser.setMultiSelectionEnabled(false);
            chooser.setSelectionMode(FileChooser.SelectionMode.FILES);

            stage.addActor(chooser.fadeIn());
          }
        });
      }});
      menu.showMenu(stage, node.getActor());
    }

    private FileHandle extract(MPQFileHandle handle, FileHandle dst) {
      if (dst == null) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        dst = new FileHandle(new File(tmpDir, handle.name()));
      }

      if (dst.exists()) {
        VisDialog dialog = new VisDialog("File already exists!");
        dialog.getContentTable().align(Align.left);
        dialog.getContentTable().add(new VisLabel("File already exists!")).row();
        dialog.getContentTable().add(new VisCheckBox("Don't ask again")).row();
        dialog.button("Open",      1);
        dialog.button("Overwrite", 2);
        dialog.button("Cancel",    3);
        dialog.show(stage);
      }

      //write(handle, dst, true);
      return dst;
    }

    private void write(MPQFileHandle handle, FileHandle dst, boolean tmp) {
      dst.writeBytes(handle.readBytes(), false);
      if (tmp) dst.file().deleteOnExit();
    }

    private void open(Selection<Node> selection, Node node, final MPQFileHandle handle) {
      final String extension = FilenameUtils.getExtension(handle.fileName).toLowerCase();
      if (extension.equals("txt")) {
        try {
          String tmpDir = System.getProperty("java.io.tmpdir");
          final FileHandle tmp = new FileHandle(new File(tmpDir, handle.name()));
          if (tmp.exists()) {
            VisDialog dialog = new VisDialog("File already exists!") {
              @Override
              protected void result(Object object) {
                File tmpFile = tmp.file();
                int value = (Integer) object;
                switch (value) {
                  case 2:
                    tmp.writeBytes(handle.readBytes(), false);
                    tmpFile.deleteOnExit();
                  case 1:
                    try {
                      Runtime.getRuntime().exec(EXCEL_PATH + " -view \"" + tmpFile + "\"");
                    } catch (IOException e) {
                      Gdx.app.error(TAG, e.getMessage(), e);
                    }
                    break;
                  case 3:
                    System.out.println("Cancel");
                    break;
                }
              }
            };
            dialog.addCloseButton();
            dialog.button("Open",      1);
            dialog.button("Overwrite", 2);
            dialog.button("Cancel",    3);
            dialog.text("File already exists!");
            dialog.show(stage);
          } else {
            tmp.writeBytes(handle.readBytes(), false);
            File tmpFile = tmp.file();
            tmpFile.deleteOnExit();
            //Desktop.getDesktop().edit(tmpFile);
            Runtime.getRuntime().exec(EXCEL_PATH + " -view \"" + tmpFile + "\"");
          }

          selection.remove(node);
        } catch (IOException e) {
          Gdx.app.error(TAG, e.getMessage(), e);
        }
      } else if (extension.equals("dc6")
              || extension.equals("dcc")) {
        imageControlsPanel.setCollapsed(false);
        palettePanel.setCollapsed(false);
        final DC dc = extension.equals("dc6") ? DC6.loadFromFile(handle) : DCC.loadFromFile(handle);
        renderer.setDrawable(new DelegatingDrawable<Animation>() {
          int page = 0;
          boolean isAnimationTab;
          DC pages;

          {
            imageControls.switchTo(dc.getFrame(0, 0).getWidth() >= DC6.PAGE_SIZE ? "Pages" : "Animation");
            isAnimationTab = imageControls.getTab().equals("Animation");

            slDirection.setValue(0);
            slDirection.setRange(0, dc.getNumDirections() - 1);
            lbDirection.setText((int) slDirection.getMinValue() + " / " + (int) slDirection.getMaxValue());

            slFrameIndex.setValue(0);
            slFrameIndex.setRange(0, dc.getNumFramesPerDir() - 1);
            lbFrameIndex.setText((int) slFrameIndex.getMinValue() + " / " + (int) slFrameIndex.getMaxValue());

            //sbBlendMode.setSelectedIndex(0);
            //cbCombineFrames.setChecked(false);

            String palette = paletteList.getSelected();
            Riiablo.batch.setPalette(palettes.get(palette));

            Animation anim = Animation.newAnimation(dc);
            anim.setDirection((int) slDirection.getValue());
            anim.setFrameDuration(1 / slFrameDuration.getValue());
            setDelegate(anim);
          }

          @Override
          public void dispose() {
            super.dispose();
            //if (combined != null) combined.dispose();
            if (pages != null) pages.dispose();
          }

          @Override
          protected void clicked(InputEvent event, float x, float y) {
            if (delegate == null) {
              return;
            }

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
              slPage.setValue(page = pages.getNumPages() - 1);
            } else if (actor == btnPrevPage) {
              if (page > 0) slPage.setValue(--page);
            } else if (actor == btnNextPage) {
              if (page < pages.getNumPages() - 1) slPage.setValue(++page);
            }
          }

          @Override
          protected void changed(ChangeEvent event, Actor actor) {
            if (delegate == null) {
              return;
            }

            if (actor == slDirection) {
              delegate.setDirection((int) slDirection.getValue());
            } else if (actor == paletteList) {
              String palette = paletteList.getSelected();
              Riiablo.batch.setPalette(palettes.get(palette));
              Gdx.app.debug(TAG, "palette set to " + palette);
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
            } else if (actor == slFrameDuration) {
              delegate.setFrameDuration(1 / slFrameDuration.getValue());
            } else if (actor == slPage) {
              //delegate.setFrame((int) slPage.getValue());
            //} else if (actor == slDirectionPage || /*actor == sbBlendModePage || */(actor == paletteList && pages != null)) {
              //for (int p = 0; p < pages.size; p++) pages.get(p).dispose();
              //pages = new DC6.PageList(dc6.pages((int) slDirectionPage.getValue(), palettes.get(paletteList.getSelected()), sbBlendModePage.getSelected()));
            }
          }

          @Override
          public void switchedTab(String fromTab, String toTab) {
            isAnimationTab = toTab.equals("Animation");
            if (!isAnimationTab) {
              // TODO: This will crash if unsupported by dc6
              //pages = new DC6.PageList(dc6.pages((int) slDirectionPage.getValue(), palettes.get(paletteList.getSelected()), sbBlendModePage.getSelected()));
              pages = extension.equals("dc6") ? DC6.loadFromFile(handle) : DCC.loadFromFile(handle);
              pages.loadDirections(true);

              // FIXME: pages.getNumPages() will return based on dir, may be problem, but all I've
              //        seen has same number of pages for each direction.
              slPage.setValue(0);
              slPage.setRange(0, pages.getNumPages() - 1);
              lbPage.setText((int) slPage.getMinValue() + " / " + (int) slPage.getMaxValue());
            }
          }

          @Override
          public void setDelegate(Animation drawable) {
            super.setDelegate(drawable);
            slFrameDuration.setValue(25);
          }

          @Override
          public void draw(Batch batch, float x, float y, float width, float height) {
            PaletteIndexedBatch b = Riiablo.batch;
            if (!isAnimationTab) {
              batch.end();

              b.setTransformMatrix(batch.getTransformMatrix());
              b.begin();
              TextureRegion page = pages.getTexture((int) slDirectionPage.getValue(), (int) slPage.getValue());
              b.draw(page, x - (page.getRegionWidth() / 2), y - (page.getRegionHeight() / 2));
              b.end();

              batch.begin();
              return;
            }

            if (!btnPlayPause.isChecked()) {
              delegate.act();
              slFrameIndex.setValue(delegate.getFrame());
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
      } else if (extension.equals("dt1")) {
      } else if (extension.equals("ds1")) {
      } else if (extension.equals("wav")) {
        audioPanel.setCollapsed(false);
        final Music sound = Gdx.audio.newMusic(handle);
        // TODO: This is just an estimate, I don't know if 2 channels is guaranteed for all files
        // FIXME: This doesn't work well at all for shorter stuff or speech dialog files
        long fileSize = handle.length();
        final float audioLength = (fileSize * Byte.SIZE) / (22050 * 16 * 2);
        renderer.setDrawable(new DelegatingDrawable<Drawable>() {
          {
            //slAudioScrubber.setDisabled(true);
            slAudioScrubber.setRange(0, audioLength);
            lbAudioScrubber.setText(slAudioScrubber.getMinValue() + " / " + slAudioScrubber.getMaxValue());

            try {
              sound.play();
              sound.setVolume(slVolume.getValue());
              sound.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                  btnPlayPauseAudio.setChecked(false);
                }
              });
            } catch (Throwable t) {
              Gdx.app.error(TAG, t.getMessage(), t);
              // FIXME: This happens periodically, as some sounds take a bit longer to dispose?
            }
          }

          @Override
          public void dispose() {
            sound.stop();
            sound.dispose();
            super.dispose();
          }

          @Override
          protected void clicked(InputEvent event, float x, float y) {
            Actor actor = event.getListenerActor();
            if (actor == btnRestartAudio) {
              sound.setPosition(0);
              sound.play();
            } else if (actor == btnPlayPauseAudio) {
              if (btnPlayPauseAudio.isChecked()) {
                sound.pause();
              } else {
                sound.play();
              }
            }
          }

          @Override
          protected void changed(ChangeEvent event, Actor actor) {
            if (actor == slAudioScrubber) {
              sound.setPosition(slAudioScrubber.getValue());
            } else if (actor == slVolume) {
              sound.setVolume(slVolume.getValue());
            }
          }

          @Override
          public void draw(Batch batch, float x, float y, float width, float height) {
            if (!slAudioScrubber.isDragging()) {
              slAudioScrubber.removeListener(changeListener);
              slAudioScrubber.setValue(sound.getPosition());
              slAudioScrubber.addListener(changeListener);
            }
          }
        });
      } else if (extension.equals("cof")) {
        imageControlsPanel.setCollapsed(false);
        palettePanel.setCollapsed(false);
        cofPanel.setCollapsed(false);
        final COF cof = COF.loadFromFile(handle);
        COF.Keyframe[] keyframes = COF.Keyframe.values();
        for (COF.Keyframe keyframe : keyframes) {
          lbKeyframes.get(keyframe).setText(cof.getKeyframeFrame(keyframe));
        }

        String path = handle.fileName.toLowerCase();
        String name = FilenameUtils.getBaseName(path).toLowerCase();
        final String token  = name.substring(0,2);
        final String mode   = name.substring(2,4);
        final String wclass = name.substring(4);

        final String type;
        if (path.contains("monsters")) {
          type = "monsters";
        } else if (path.contains("chars")) {
          type = "chars";
        } else {
          type = "null";
        }

        if (compClasses == null) {
          compClasses = new ObjectMap<>();
          for (String comp : components.getItems()) {
            comp = comp.toLowerCase();
            compClasses.put(comp, new Array<String>());
          }
          selectedWClass = new String[COF.Component.NUM_COMPONENTS];
        } else {
          for (Array<String> a : compClasses.values()) {
            a.clear();
            a.add("NONE");
          }
          Arrays.fill(selectedWClass, null);
        }

        for (String comp : components.getItems()) {
          comp = comp.toLowerCase();
          String prefix = String.format("data\\global\\%s\\%2$s\\%3$s\\%2$s%3$s", type, token, comp);
          SortedMap<String, Node> dccs = fileTreeNodes.prefixMap(prefix);
          if (dccs.isEmpty()) {
            continue;
          }

          System.out.println(prefix);
          Array<String> wclasses = compClasses.get(comp);
          for (String dcc : dccs.keySet()) {
            if (!FilenameUtils.isExtension(dcc, "dcc")) {
              continue;
            }

            // TODO: hth should probably only be included if wclass doesn't exist to overwrite it
            if (!dcc.substring(prefix.length() + 5, prefix.length() + 8).equalsIgnoreCase(wclass)
             && !dcc.substring(prefix.length() + 5, prefix.length() + 8).equalsIgnoreCase("HTH")) {
              continue;
            }

            if (!dcc.substring(prefix.length() + 3, prefix.length() + 5).equalsIgnoreCase(mode)) {
              continue;
            }

            String clazz = dcc.substring(prefix.length(), prefix.length() + 3);
            wclasses.add(clazz);
            System.out.println("\t" + dcc + " " + clazz);

            int l = COMP_TO_ID.get(comp, -1);
            if (selectedWClass[l] == null) selectedWClass[l] = clazz;
          }
        }

        System.out.println(Arrays.toString(selectedWClass));

        renderer.setDrawable(new DelegatingDrawable<Animation>() {
          {
            components.setSelectedIndex(0);
            String comp = components.getSelected().toLowerCase();
            wclasses.setItems(compClasses.get(comp));

            String palette = paletteList.getSelected();
            Riiablo.batch.setPalette(palettes.get(palette));

            Animation anim = Animation.newAnimation(cof);
            for (int l = 0; l < cof.getNumLayers(); l++) {
              COF.Layer layer = cof.getLayer(l);

              String clazz = selectedWClass[layer.component];
              if (clazz == null) continue;

              comp = components.getItems().get(layer.component);
              String dcc = String.format("data\\global\\%s\\%2$s\\%3$s\\%2$s%3$s%4$s%5$s%6$s.dcc", type, token, comp, clazz, mode, layer.weaponClass);
              System.out.println(comp + "=" + dcc);

              DC dc = DCC.loadFromFile(Riiablo.mpqs.resolve(dcc));
              anim.setLayer(layer, dc, false);
            }
            setDelegate(anim);
          }

          @Override
          protected void changed(ChangeEvent event, Actor actor) {
            if (actor == components) {
              String comp = components.getSelected().toLowerCase();
              wclasses.setItems(compClasses.get(comp));
              wclasses.setSelected(selectedWClass[COMP_TO_ID.get(comp, -1)]);
            } else if (actor == wclasses && delegate != null) {
              String comp = components.getSelected().toLowerCase();
              String clazz = wclasses.getSelected();
              if (clazz == null) return;
              clazz = clazz.toLowerCase();
              int c = COMP_TO_ID.get(comp, -1);
              Animation.Layer animLayer = delegate.getLayer(c);
              DC old = animLayer != null ? animLayer.getDC() : null;

              COF.Layer layer = cof.getComponent(c);
              String dcc = String.format("data\\global\\%s\\%2$s\\%3$s\\%2$s%3$s%4$s%5$s%6$s.dcc", type, token, comp, clazz, mode, layer.weaponClass);
              System.out.println(comp + "=" + dcc);

              DC dc = DCC.loadFromFile(Riiablo.mpqs.resolve(dcc));
              delegate.setLayer(layer, dc, false);
              if (old != null) old.dispose();
            }
          }

          @Override
          public void dispose() {
            for (int l = 0; l < cof.getNumLayers(); l++) {
              COF.Layer layer = cof.getLayer(l);
              Animation.Layer animLayer = delegate.getLayer(layer.component);
              if (animLayer != null) animLayer.getDC().dispose();
            }
            super.dispose();
          }

          @Override
          public void draw(Batch batch, float x, float y, float width, float height) {
            PaletteIndexedBatch b = Riiablo.batch;
            if (!btnPlayPause.isChecked()) {
              delegate.act();
              slFrameIndex.setValue(delegate.getFrame());
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
      } else if (extension.equals("pl2")) {
      } else if (extension.equals("dat")) {
        Palette pal = Palette.loadFromFile(handle);
        renderer.setDrawable(new TextureRegionDrawable(pal.render(32)));
      } else if (extension.equals("tbl")) {
      } else {
        renderer.setDrawable(null);
      }

      rendererScroller.setScrollPercentX(0.5f);
      rendererScroller.setScrollPercentY(0.5f);
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
        btnPlayPause     .addListener(clickListener);
        btnFirstFrame    .addListener(clickListener);
        btnLastFrame     .addListener(clickListener);
        btnPrevFrame     .addListener(clickListener);
        btnNextFrame     .addListener(clickListener);
        paletteList      .addListener(changeListener);
        slDirection      .addListener(changeListener);
        slFrameIndex     .addListener(changeListener);
        slFrameDuration  .addListener(changeListener);
        //sbBlendMode      .addListener(changeListener);
        //cbCombineFrames.addListener(sbBlendModePage);
        btnPlayPauseAudio.addListener(clickListener);
        btnRestartAudio  .addListener(clickListener);
        slAudioScrubber  .addListener(changeListener);
        slVolume         .addListener(changeListener);
        imageControls    .addListener(this);
        slPage           .addListener(changeListener);
        btnFirstPage     .addListener(clickListener);
        btnLastPage      .addListener(clickListener);
        btnPrevPage      .addListener(clickListener);
        btnNextPage      .addListener(clickListener);
        slDirectionPage  .addListener(changeListener);
        //sbBlendModePage  .addListener(changeListener);
        components       .addListener(changeListener);
        wclasses         .addListener(changeListener);
      }

      public DelegatingDrawable(T delegate) {
        this();
        this.delegate = delegate;
      }

      @Override
      public void dispose() {
        if (delegate instanceof Disposable) ((Disposable) delegate).dispose();
        btnPlayPause     .removeListener(clickListener);
        btnFirstFrame    .removeListener(clickListener);
        btnLastFrame     .removeListener(clickListener);
        btnPrevFrame     .removeListener(clickListener);
        btnNextFrame     .removeListener(clickListener);
        paletteList      .removeListener(changeListener);
        slDirection      .removeListener(changeListener);
        slFrameIndex     .removeListener(changeListener);
        slFrameDuration  .removeListener(changeListener);
        //sbBlendMode      .removeListener(changeListener);
        //cbCombineFrames.removeListener(clickListener);
        btnPlayPauseAudio.removeListener(clickListener);
        btnRestartAudio  .removeListener(clickListener);
        slAudioScrubber  .removeListener(changeListener);
        slVolume         .removeListener(changeListener);
        imageControls    .removeListener(this);
        slPage           .removeListener(changeListener);
        btnFirstPage     .removeListener(clickListener);
        btnLastPage      .removeListener(clickListener);
        btnPrevPage      .removeListener(clickListener);
        btnNextPage      .removeListener(clickListener);
        slDirectionPage  .removeListener(changeListener);
        //sbBlendModePage  .removeListener(changeListener);
        components       .removeListener(changeListener);
        wclasses         .removeListener(changeListener);
      }

      protected void clicked(InputEvent event, float x, float y) {}

      protected void changed(ChangeEvent event, Actor actor) {}

      @Override
      public void switchedTab(String fromTab, String toTab) {}

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

  public static class Renderer extends Actor implements Disposable {
    private Color background;
    private Texture backgroundTexture;
    private Drawable drawable;

    public Renderer() {
      setSize(2048, 2048);
      setBackground(Color.BLACK);
    }

    @Override
    public void dispose() {
      if (backgroundTexture != null) backgroundTexture.dispose();
      if (drawable instanceof Disposable) ((Disposable) drawable).dispose();
      else if (drawable instanceof TextureRegionDrawable)
        ((TextureRegionDrawable) drawable).getRegion().getTexture().dispose();
    }

    public Color getBackground() {
      return background;
    }

    public void setBackground(Color background) {
      if (Objects.equals(background, this.background)) {
        return;
      }

      this.background = background;
      if (background == null) {
        backgroundTexture = null;
        return;
      }

      Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      p.setColor(background);
      p.drawPixel(0, 0);

      if (backgroundTexture != null) backgroundTexture.dispose();
      backgroundTexture = new Texture(p);
      p.dispose();
    }

    public void setDrawable(Drawable drawable) {
      if (Objects.equals(drawable, this.drawable)) {
        return;
      }

      if (this.drawable instanceof Disposable) ((Disposable) this.drawable).dispose();
      this.drawable = drawable;
    }

    @Override
    public void draw(Batch batch, float a) {
      if (backgroundTexture != null) batch.draw(backgroundTexture, 0, 0, getWidth(), getHeight());
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
          getX(Align.center) - (drawable.getMinWidth() / 2),
          getY(Align.center) - (drawable.getMinHeight() / 2),
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
}
