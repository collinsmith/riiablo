package gdx.diablo.tester;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.ObjectIntMap;

public class ColumnConsolidator extends ApplicationAdapter {
  private static final String TAG = "ColumnConsolidator";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "ColumnConsolidator";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new ColumnConsolidator(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    String weapons = "name	type	type2	code	alternateGfx	namestr	version	compactsave	rarity	spawnable	mindam	maxdam	1or2handed	2handed	2handmindam	2handmaxdam	minmisdam	maxmisdam		rangeadder	speed	StrBonus	DexBonus	reqstr	reqdex	durability	nodurability	level	levelreq	cost	gamble cost	magic lvl	auto prefix	OpenBetaGfx	normcode	ubercode	ultracode	wclass	2handedwclass	component	hit class	invwidth	invheight	stackable	minstack	maxstack	spawnstack	flippyfile	invfile	uniqueinvfile	setinvfile	hasinv	gemsockets	gemapplytype	special	useable	dropsound	dropsfxframe	usesound	unique	transparent	transtbl	quivered	lightradius	belt	quest	questdiffcheck	missiletype	durwarning	qntwarning	gemoffset	bitfield1	CharsiMin	CharsiMax	CharsiMagicMin	CharsiMagicMax	CharsiMagicLvl	GheedMin	GheedMax	GheedMagicMin	GheedMagicMax	GheedMagicLvl	AkaraMin	AkaraMax	AkaraMagicMin	AkaraMagicMax	AkaraMagicLvl	FaraMin	FaraMax	FaraMagicMin	FaraMagicMax	FaraMagicLvl	LysanderMin	LysanderMax	LysanderMagicMin	LysanderMagicMax	LysanderMagicLvl	DrognanMin	DrognanMax	DrognanMagicMin	DrognanMagicMax	DrognanMagicLvl	HraltiMin	HraltiMax	HraltiMagicMin	HraltiMagicMax	HratliMagicLvl	AlkorMin	AlkorMax	AlkorMagicMin	AlkorMagicMax	AlkorMagicLvl	OrmusMin	OrmusMax	OrmusMagicMin	OrmusMagicMax	OrmusMagicLvl	ElzixMin	ElzixMax	ElzixMagicMin	ElzixMagicMax	ElzixMagicLvl	AshearaMin	AshearaMax	AshearaMagicMin	AshearaMagicMax	AshearaMagicLvl	CainMin	CainMax	CainMagicMin	CainMagicMax	CainMagicLvl	HalbuMin	HalbuMax	HalbuMagicMin	HalbuMagicMax	HalbuMagicLvl	JamellaMin	JamellaMax	JamellaMagicMin	JamellaMagicMax	JamellaMagicLvl	LarzukMin	LarzukMax	LarzukMagicMin	LarzukMagicMax	LarzukMagicLvl	DrehyaMin	DrehyaMax	DrehyaMagicMin	DrehyaMagicMax	DrehyaMagicLvl	MalahMin	MalahMax	MalahMagicMin	MalahMagicMax	MalahMagicLvl	Source Art	Game Art	Transform	InvTrans	SkipName	NightmareUpgrade	HellUpgrade	Nameable	PermStoreItem";
    String armors  = "name	version	compactsave	rarity	spawnable	minac	maxac	absorbs	speed	reqstr	block	durability	nodurability	level	levelreq	cost	gamble cost	code	namestr	magic lvl	auto prefix	alternategfx	OpenBetaGfx	normcode	ubercode	ultracode	spelloffset	component	invwidth	invheight	hasinv	gemsockets	gemapplytype	flippyfile	invfile	uniqueinvfile	setinvfile	rArm	lArm	Torso	Legs	rSPad	lSPad	useable	throwable	stackable	minstack	maxstack	type	type2	dropsound	dropsfxframe	usesound	unique	transparent	transtbl	quivered	lightradius	belt	quest	missiletype	durwarning	qntwarning	mindam	maxdam	StrBonus	DexBonus	gemoffset	bitfield1	CharsiMin	CharsiMax	CharsiMagicMin	CharsiMagicMax	CharsiMagicLvl	GheedMin	GheedMax	GheedMagicMin	GheedMagicMax	GheedMagicLvl	AkaraMin	AkaraMax	AkaraMagicMin	AkaraMagicMax	AkaraMagicLvl	FaraMin	FaraMax	FaraMagicMin	FaraMagicMax	FaraMagicLvl	LysanderMin	LysanderMax	LysanderMagicMin	LysanderMagicMax	LysanderMagicLvl	DrognanMin	DrognanMax	DrognanMagicMin	DrognanMagicMax	DrognanMagicLvl	HraltiMin	HraltiMax	HraltiMagicMin	HraltiMagicMax	HratliMagicLvl	AlkorMin	AlkorMax	AlkorMagicMin	AlkorMagicMax	AlkorMagicLvl	OrmusMin	OrmusMax	OrmusMagicMin	OrmusMagicMax	OrmusMagicLvl	ElzixMin	ElzixMax	ElzixMagicMin	ElzixMagicMax	ElzixMagicLvl	AshearaMin	AshearaMax	AshearaMagicMin	AshearaMagicMax	AshearaMagicLvl	CainMin	CainMax	CainMagicMin	CainMagicMax	CainMagicLvl	HalbuMin	HalbuMax	HalbuMagicMin	HalbuMagicMax	HalbuMagicLvl	JamellaMin	JamellaMax	JamellaMagicMin	JamellaMagicMax	JamellaMagicLvl	LarzukMin	LarzukMax	LarzukMagicMin	LarzukMagicMax	LarzukMagicLvl	MalahMin	MalahMax	MalahMagicMin	MalahMagicMax	MalahMagicLvl	DrehyaMin	DrehyaMax	DrehyaMagicMin	DrehyaMagicMax	DrehyaMagicLvl	Source Art	Game Art	Transform	InvTrans	SkipName	NightmareUpgrade	HellUpgrade	mindam	maxdam	nameable";
    String misc    = "";

    String weaponCols[] = weapons.split("\t");
    String armorCols[]  = armors.split("\t");

    ObjectIntMap<String> map = new ObjectIntMap<>();
    for (String col : weaponCols) map.getAndIncrement(col, 0, 1);
    for (String col : armorCols)  map.getAndIncrement(col, 0, 1);

    for (ObjectIntMap.Entry<String> entry : map) {
      if (entry.value >= 2) {
        System.out.println(entry.key);
      }
    }

    Gdx.app.exit();
  }
}
