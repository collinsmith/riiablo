package gdx.diablo.tester;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.ObjectIntMap;

import gdx.diablo.Diablo;
import gdx.diablo.Files;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class ItemSheetsCheck extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "ItemSheetsCheck";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new ItemSheetsCheck(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    Diablo.mpqs = new MPQFileHandleResolver();
    Diablo.mpqs.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
    Diablo.mpqs.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    Diablo.mpqs.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));

    Diablo.assets = new AssetManager(Diablo.mpqs);
    Diablo.files = new Files(Diablo.assets);

    String armor = "name	version	compactsave	rarity	spawnable	minac	maxac	absorbs	speed	reqstr	block	durability	nodurability	level	levelreq	cost	gamble cost	code	namestr	magic lvl	auto prefix	alternategfx	OpenBetaGfx	normcode	ubercode	ultracode	spelloffset	component	invwidth	invheight	hasinv	gemsockets	gemapplytype	flippyfile	invfile	uniqueinvfile	setinvfile	rArm	lArm	Torso	Legs	rSPad	lSPad	useable	throwable	stackable	minstack	maxstack	type	type2	dropsound	dropsfxframe	usesound	unique	transparent	transtbl	quivered	lightradius	belt	quest	missiletype	durwarning	qntwarning	mindam	maxdam	StrBonus	DexBonus	gemoffset	bitfield1	CharsiMin	CharsiMax	CharsiMagicMin	CharsiMagicMax	CharsiMagicLvl	GheedMin	GheedMax	GheedMagicMin	GheedMagicMax	GheedMagicLvl	AkaraMin	AkaraMax	AkaraMagicMin	AkaraMagicMax	AkaraMagicLvl	FaraMin	FaraMax	FaraMagicMin	FaraMagicMax	FaraMagicLvl	LysanderMin	LysanderMax	LysanderMagicMin	LysanderMagicMax	LysanderMagicLvl	DrognanMin	DrognanMax	DrognanMagicMin	DrognanMagicMax	DrognanMagicLvl	HraltiMin	HraltiMax	HraltiMagicMin	HraltiMagicMax	HratliMagicLvl	AlkorMin	AlkorMax	AlkorMagicMin	AlkorMagicMax	AlkorMagicLvl	OrmusMin	OrmusMax	OrmusMagicMin	OrmusMagicMax	OrmusMagicLvl	ElzixMin	ElzixMax	ElzixMagicMin	ElzixMagicMax	ElzixMagicLvl	AshearaMin	AshearaMax	AshearaMagicMin	AshearaMagicMax	AshearaMagicLvl	CainMin	CainMax	CainMagicMin	CainMagicMax	CainMagicLvl	HalbuMin	HalbuMax	HalbuMagicMin	HalbuMagicMax	HalbuMagicLvl	JamellaMin	JamellaMax	JamellaMagicMin	JamellaMagicMax	JamellaMagicLvl	LarzukMin	LarzukMax	LarzukMagicMin	LarzukMagicMax	LarzukMagicLvl	MalahMin	MalahMax	MalahMagicMin	MalahMagicMax	MalahMagicLvl	DrehyaMin	DrehyaMax	DrehyaMagicMin	DrehyaMagicMax	DrehyaMagicLvl	Source Art	Game Art	Transform	InvTrans	SkipName	NightmareUpgrade	HellUpgrade	mindam	maxdam	nameable";
    String weapons = "name	type	type2	code	alternateGfx	namestr	version	compactsave	rarity	spawnable	mindam	maxdam	1or2handed	2handed	2handmindam	2handmaxdam	minmisdam	maxmisdam		rangeadder	speed	StrBonus	DexBonus	reqstr	reqdex	durability	nodurability	level	levelreq	cost	gamble cost	magic lvl	auto prefix	OpenBetaGfx	normcode	ubercode	ultracode	wclass	2handedwclass	component	hit class	invwidth	invheight	stackable	minstack	maxstack	spawnstack	flippyfile	invfile	uniqueinvfile	setinvfile	hasinv	gemsockets	gemapplytype	special	useable	dropsound	dropsfxframe	usesound	unique	transparent	transtbl	quivered	lightradius	belt	quest	questdiffcheck	missiletype	durwarning	qntwarning	gemoffset	bitfield1	CharsiMin	CharsiMax	CharsiMagicMin	CharsiMagicMax	CharsiMagicLvl	GheedMin	GheedMax	GheedMagicMin	GheedMagicMax	GheedMagicLvl	AkaraMin	AkaraMax	AkaraMagicMin	AkaraMagicMax	AkaraMagicLvl	FaraMin	FaraMax	FaraMagicMin	FaraMagicMax	FaraMagicLvl	LysanderMin	LysanderMax	LysanderMagicMin	LysanderMagicMax	LysanderMagicLvl	DrognanMin	DrognanMax	DrognanMagicMin	DrognanMagicMax	DrognanMagicLvl	HraltiMin	HraltiMax	HraltiMagicMin	HraltiMagicMax	HratliMagicLvl	AlkorMin	AlkorMax	AlkorMagicMin	AlkorMagicMax	AlkorMagicLvl	OrmusMin	OrmusMax	OrmusMagicMin	OrmusMagicMax	OrmusMagicLvl	ElzixMin	ElzixMax	ElzixMagicMin	ElzixMagicMax	ElzixMagicLvl	AshearaMin	AshearaMax	AshearaMagicMin	AshearaMagicMax	AshearaMagicLvl	CainMin	CainMax	CainMagicMin	CainMagicMax	CainMagicLvl	HalbuMin	HalbuMax	HalbuMagicMin	HalbuMagicMax	HalbuMagicLvl	JamellaMin	JamellaMax	JamellaMagicMin	JamellaMagicMax	JamellaMagicLvl	LarzukMin	LarzukMax	LarzukMagicMin	LarzukMagicMax	LarzukMagicLvl	DrehyaMin	DrehyaMax	DrehyaMagicMin	DrehyaMagicMax	DrehyaMagicLvl	MalahMin	MalahMax	MalahMagicMin	MalahMagicMax	MalahMagicLvl	Source Art	Game Art	Transform	InvTrans	SkipName	NightmareUpgrade	HellUpgrade	Nameable	PermStoreItem";
    String misc = "name	*name	szFlavorText	compactsave	version	level	levelreq	rarity	spawnable	speed	nodurability	cost	gamble cost	code	alternategfx	namestr	component	invwidth	invheight	hasinv	gemsockets	gemapplytype	flippyfile	invfile	uniqueinvfile	special	Transmogrify	TMogType	TMogMin	TMogMax	useable	throwable	type	type2	dropsound	dropsfxframe	usesound	unique	transparent	transtbl	lightradius	belt	autobelt	stackable	minstack	maxstack	spawnstack	quest	questdiffcheck	missiletype	spellicon	pSpell	state	cstate1	cstate2	len	stat1	calc1	stat2	calc2	stat3	calc3	spelldesc	spelldescstr	spelldesccalc	durwarning	qntwarning	gemoffset	BetterGem	bitfield1	CharsiMin	CharsiMax	CharsiMagicMin	CharsiMagicMax	CharsiMagicLvl	GheedMin	GheedMax	GheedMagicMin	GheedMagicMax	GheedMagicLvl	AkaraMin	AkaraMax	AkaraMagicMin	AkaraMagicMax	AkaraMagicLvl	FaraMin	FaraMax	FaraMagicMin	FaraMagicMax	FaraMagicLvl	LysanderMin	LysanderMax	LysanderMagicMin	LysanderMagicMax	LysanderMagicLvl	DrognanMin	DrognanMax	DrognanMagicMin	DrognanMagicMax	DrognanMagicLvl	HraltiMin	HraltiMax	HraltiMagicMin	HraltiMagicMax	HraltiMagicLvl	AlkorMin	AlkorMax	AlkorMagicMin	AlkorMagicMax	AlkorMagicLvl	OrmusMin	OrmusMax	OrmusMagicMin	OrmusMagicMax	OrmusMagicLvl	ElzixMin	ElzixMax	ElzixMagicMin	ElzixMagicMax	ElzixMagicLvl	AshearaMin	AshearaMax	AshearaMagicMin	AshearaMagicMax	AshearaMagicLvl	CainMin	CainMax	CainMagicMin	CainMagicMax	CainMagicLvl	HalbuMin	HalbuMax	HalbuMagicMin	HalbuMagicMax	HalbuMagicLvl	MalahMin	MalahMax	MalahMagicMin	MalahMagicMax	MalahMagicLvl	LarzukMin	LarzukMax	LarzukMagicMin	LarzukMagicMax	LarzukMagicLvl	DrehyaMin	DrehyaMax	DrehyaMagicMin	DrehyaMagicMax	DrehyaMagicLvl	JamellaMin	JamellaMax	JamellaMagicMin	JamellaMagicMax	JamellaMagicLvl	Source Art	Game Art	Transform	InvTrans	SkipName	NightmareUpgrade	HellUpgrade	mindam	maxdam	PermStoreItem	multibuy	Nameable	*eol";

    String armor2[] = armor.split("\t");
    String weapons2[] = weapons.split("\t");
    String misc2[] = misc.split("\t");

    ObjectIntMap<String> counter = new ObjectIntMap<>();
    for (int i = 0; i < armor2.length; i++)   counter.getAndIncrement(armor2[i].toLowerCase(), 0, 1);
    for (int i = 0; i < weapons2.length; i++) counter.getAndIncrement(weapons2[i].toLowerCase(), 0, 1);
    for (int i = 0; i < misc2.length; i++)    counter.getAndIncrement(misc2[i].toLowerCase(), 0, 1);

    int buggedColumns = 0;
    int extraColumns = 0;
    int commonColumns = 0;
    for (ObjectIntMap.Entry<String> entry : counter) {
      if (entry.value < 3) {
        extraColumns++;
      } else if (entry.value == 3) {
        commonColumns++;
      } else {
        buggedColumns++;
        System.out.println(entry.key + ":" + entry.value);
      }
    }

    System.out.println("total extra columns = " + extraColumns);
    System.out.println("total common columns = " + commonColumns);
    System.out.println("total bugged columns = " + buggedColumns);

    for (ObjectIntMap.Entry<String> entry : counter) {
      System.out.println(entry.key);
    }

    Diablo.assets.dispose();
    Gdx.app.exit();
  }
}
