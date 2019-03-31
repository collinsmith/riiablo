package com.riiablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.Quests;
import com.riiablo.graphics.BlendMode;
import com.riiablo.loader.DC6Loader;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.AnimationWrapper;
import com.riiablo.widget.Button;
import com.riiablo.widget.DCWrapper;
import com.riiablo.widget.DialogScroller;
import com.riiablo.widget.Label;

import java.util.Comparator;

public class QuestsPanel extends WidgetGroup implements Disposable {
  private static final String TAG = "QuestsPanel";

  final AssetDescriptor<DC6> questbackgroundDescriptor = new AssetDescriptor<>("data\\global\\ui\\MENU\\questbackground.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion questbackground;

  final AssetDescriptor<DC6> expquesttabsDescriptor = new AssetDescriptor<>("data\\global\\ui\\MENU\\expquesttabs.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  DC expquesttabs;

  final AssetDescriptor<DC6> questlastDescriptor = new AssetDescriptor<>("data\\global\\ui\\MENU\\questlast.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  Button btnPlayQuest;

  final AssetDescriptor<DC6> questsocketsDescriptor = new AssetDescriptor<>("data\\global\\ui\\MENU\\questsockets.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  DC questsockets;

  final AssetDescriptor<DC6> questdoneDescriptor = new AssetDescriptor<>("data\\global\\ui\\MENU\\questdone.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  DC questdone;

  final AssetDescriptor<DC6>[] questiconsDescriptor;
  DC[] questicons;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  Button btnExit;

  final GameScreen gameScreen;
  Tab activeTab;

  private static final int[] QUESTS = { 6, 6, 6, 3, 6 };

  @SuppressWarnings("unchecked")
  public QuestsPanel(final GameScreen gameScreen) {
    this.gameScreen = gameScreen;

    Riiablo.assets.load(questbackgroundDescriptor);
    Riiablo.assets.finishLoadingAsset(questbackgroundDescriptor);
    questbackground = Riiablo.assets.get(questbackgroundDescriptor).getTexture();
    setSize(questbackground.getRegionWidth(), questbackground.getRegionHeight());
    setTouchable(Touchable.enabled);
    setVisible(false);

    btnPlayQuest = new Button(new Button.ButtonStyle() {{
      Riiablo.assets.load(questlastDescriptor);
      Riiablo.assets.finishLoadingAsset(questlastDescriptor);
      up   = new TextureRegionDrawable(Riiablo.assets.get(questlastDescriptor).getTexture(0));
      down = new TextureRegionDrawable(Riiablo.assets.get(questlastDescriptor).getTexture(1));
    }});
    btnPlayQuest.setPosition(227, 10);
    addActor(btnPlayQuest);

    btnExit = new Button(new Button.ButtonStyle() {{
      Riiablo.assets.load(buysellbtnDescriptor);
      Riiablo.assets.finishLoadingAsset(buysellbtnDescriptor);
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(11));
    }});
    btnExit.setPosition(278, 10);
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    addActor(btnExit);

    Riiablo.assets.load(expquesttabsDescriptor);
    Riiablo.assets.finishLoadingAsset(expquesttabsDescriptor);
    expquesttabs = Riiablo.assets.get(expquesttabsDescriptor);

    Riiablo.assets.load(questsocketsDescriptor);
    Riiablo.assets.finishLoadingAsset(questsocketsDescriptor);
    questsockets = Riiablo.assets.get(questsocketsDescriptor);

    Riiablo.assets.load(questdoneDescriptor);
    Riiablo.assets.finishLoadingAsset(questdoneDescriptor);
    questdone = Riiablo.assets.get(questdoneDescriptor);

    @SuppressWarnings("unchecked")
    Array<Quests.Entry>[] quests = (Array<Quests.Entry>[]) new Array[5];
    for (int i = 0; i < quests.length; i++) quests[i] = new Array<>(6);
    for (Quests.Entry quest : Riiablo.files.quests) {
      if (quest.visible) {
        quests[quest.act].add(quest);
      }
    }
    Comparator<Quests.Entry> comparator = new Comparator<Quests.Entry>() {
      @Override
      public int compare(Quests.Entry o1, Quests.Entry o2) {
        return o1.order - o2.order;
      }
    };
    int numQuests = 0;
    for (Array<Quests.Entry> quest : quests) {
      quest.sort(comparator);
      numQuests += quest.size;
    }

    questiconsDescriptor = (AssetDescriptor<DC6>[]) new AssetDescriptor[numQuests];
    questicons = new DC[numQuests];
    for (int act = 0, q = 0; act < 5; act++) {
      for (Quests.Entry quest : quests[act]) {
        questiconsDescriptor[q] = new AssetDescriptor<>("data\\global\\ui\\MENU\\" + quest.icon + ".dc6", DC6.class);
        Riiablo.assets.load(questiconsDescriptor[q]);
        Riiablo.assets.finishLoadingAsset(questiconsDescriptor[q]);
        questicons[q] = Riiablo.assets.get(questiconsDescriptor[q]);
        q++;
      }
    }

    final Tab[] tabs = new Tab[5];
    for (int i = 0, q = 0; i < tabs.length; i++) {
      Tab tab = tabs[i] = new Tab();
      for (Quests.Entry quest : quests[i]) {
        tab.addQuest(quest, q++);
      }

      tab.pack();
      //tab.questIcons.setSize(315, 200);
      //tab.questIcons.layout();
      tab.setSize(315, 352);
      tab.layout();
      tab.setPosition(3, getHeight() - 32, Align.topLeft);
      //tab.questIcons.setY(tab.getHeight(), Align.top);
      tab.setVisible(false);
      //tab.setDebug(true, true);
      addActor(tab);
    }

    float x = 2, y = getHeight() - 3;
    Button[] actors = new Button[5];
    for (int i = 0; i < actors.length; i++) {
      final int j = i << 1;
      final Button actor = actors[i] = new Button(new Button.ButtonStyle() {{
        down = new TextureRegionDrawable(expquesttabs.getTexture(j));
        up   = new TextureRegionDrawable(expquesttabs.getTexture(j + 1));
        checked = down;
      }});
      actor.setHighlightedBlendMode(BlendMode.ID, Color.WHITE);
      actor.setPosition(x, y, Align.topLeft);
      actor.setUserObject(tabs[i]);
      actor.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          for (Tab tab : tabs) if (tab != null) tab.setVisible(false);
          activeTab = (Tab) actor.getUserObject();
          activeTab.setVisible(true);
        }
      });
      addActor(actor);
      x += actor.getWidth();
    }

    ButtonGroup<Button> tabGroup = new ButtonGroup<>();
    tabGroup.add(actors);
    tabGroup.setMinCheckCount(1);
    tabGroup.setMaxCheckCount(1);
    activeTab = tabs[0];
    activeTab.setVisible(true);

    btnPlayQuest.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (activeTab != null) {
          activeTab.questDialog.play("akara_act1_q1_init");
        }
      }
    });

    //setDebug(true, true);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    batch.draw(questbackground, getX(), getY());
    super.draw(batch, parentAlpha);
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(questbackgroundDescriptor.fileName);
    Riiablo.assets.unload(expquesttabsDescriptor.fileName);
    Riiablo.assets.unload(questlastDescriptor.fileName);
    Riiablo.assets.unload(buysellbtnDescriptor.fileName);
    Riiablo.assets.unload(questdoneDescriptor.fileName);
    Riiablo.assets.unload(questsocketsDescriptor.fileName);
    for (AssetDescriptor assetDescriptor : questiconsDescriptor) Riiablo.assets.unload(assetDescriptor.fileName);
  }

  private class Tab extends Table {
    private QuestButton selected = null;
    Table questIcons;
    Label questName;
    DialogScroller questDialog;

    Tab() {
      questIcons = new Table();
      questIcons.columnDefaults(0).size(80, 95).space(4, 16, 4, 16);
      questIcons.columnDefaults(1).size(80, 95).space(4, 16, 4, 16);
      questIcons.columnDefaults(2).size(80, 95).space(4, 16, 4, 16);
      questIcons.align(Align.top | Align.center);
      add(questIcons).height(197).growX().row();

      questName = new Label(Riiablo.fonts.font16);
      questName.setAlignment(Align.center);
      add(questName).height(24).growX().row();

      questDialog = new DialogScroller(new DialogScroller.DialogCompletionListener() {
        @Override
        public void onCompleted(DialogScroller d) {
          d.dispose();
        }
      });
      add(questDialog).grow().row();
    }

    void setSelected(QuestButton quest) {
      if (selected != quest) {
        if (selected != null) selected.setSelected(false);
        selected = quest;
        quest.setSelected(true);
        questName.setText(Riiablo.string.lookup(quest.getName()));
        questDialog.dispose();
        questDialog.setTextId(quest.quest.qsts[0]);
      }
    }

    void addQuest(Quests.Entry quest, int q) {
      QuestButton button = new QuestButton(this, quest, q);
      questIcons.add(button);
      if (questIcons.getCells().size % 3 == 0) {
        questIcons.row();
      }
    }
  }

  private class QuestButton extends WidgetGroup {
    private static final int FRAME_UP       = 0;
    private static final int FRAME_DOWN     = 25;
    private static final int FRAME_DISABLED = 26;

    final Quests.Entry quest;
    final Tab parent;
    final Animation anim;
    final DCWrapper overlay;
    final ClickListener clickListener;

    QuestButton(Tab tab, Quests.Entry quest, int q) {
      this.parent = tab;
      this.quest = quest;
      setName(quest.qstr);

      DCWrapper background = new DCWrapper();
      background.setDrawable(questdone.getTexture(quest.questdone));
      background.setPosition(5, 4);
      background.setSize(72, 86);
      addActor(background);

      anim = Animation.newAnimation(questicons[q]);
      anim.setClamp(true);
      anim.setFrameDuration(Float.MAX_VALUE);
      AnimationWrapper animWrapper = new AnimationWrapper(anim);
      animWrapper.setPosition(5, 4);
      addActor(animWrapper);

      overlay = new DCWrapper();
      overlay.setDrawable(questsockets.getTexture(0));
      overlay.setSize(80, 95);
      addActor(overlay);

      addListener(clickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          parent.setSelected(QuestButton.this);
        }
      });
    }

    void setSelected(boolean b) {
      overlay.setDrawable(questsockets.getTexture(b ? 1 : 0));
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      if (clickListener.isVisualPressed()) {
        anim.setFrame(FRAME_DOWN);
      } else {
        anim.setFrame(FRAME_UP);
      }
    }
  }
}
