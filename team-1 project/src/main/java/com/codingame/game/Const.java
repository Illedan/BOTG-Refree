package com.codingame.game;

import java.util.Random;

public class Const {

    //LEAGUE
    // wood3 = 0;
    // wood2 = 1;
    // wood1 = 2;
    // bronze and above = 3+;

	static Random random;

	public static int MAXINT = Integer.MAX_VALUE;
	public static double MAXDOUBLE = Double.MAX_VALUE;

    public static boolean REMOVEFORESTCREATURES = false;
    public static boolean IGNOREITEMS = false;
    public static boolean IGNORESKILLS = false;
    public static double TOWERHEALTHSCALE = 1.0;
    public static boolean IGNOREBUSHES = false;

    //MISC
    public static double EPSILON = 0.00001;
    public static double ROUNDTIME = 1.0;
    static int Rounds = 250;
    static int MAPWIDTH = 1920;
    static int MAPHEIGHT = 780;
    static int HEROCOUNT = 2;
    static int MAXITEMCOUNT = 4;
    static int MELEE_UNIT_COUNT = 3;
    static int RANGED_UNIT_COUNT = 1;
    static double SELLITEMREFUND = 0.5;

    //TEAM0
    public final static Point TOWERTEAM0 = new Point(100, 540);
    public final static Point SPAWNTEAM0 = new Point(TOWERTEAM0.x+60, TOWERTEAM0.y-50);
    public final static Point HEROSPAWNTEAM0 = new Point(TOWERTEAM0.x+100, TOWERTEAM0.y+50);
    public final static Point HEROSPAWNTEAM0HERO2 = new Point(HEROSPAWNTEAM0.x, TOWERTEAM0.y-50);

    //TEAM
    public final static Point TOWERTEAM1 = new Point(MAPWIDTH-TOWERTEAM0.x, TOWERTEAM0.y);
    public final static Point SPAWNTEAM1 = new Point(MAPWIDTH-SPAWNTEAM0.x, SPAWNTEAM0.y);
    public final static Point HEROSPAWNTEAM1 = new Point(MAPWIDTH-HEROSPAWNTEAM0.x, HEROSPAWNTEAM0.y);
    public final static Point HEROSPAWNTEAM1HERO2 = new Point(HEROSPAWNTEAM1.x, HEROSPAWNTEAM0HERO2.y);

    //HERO
    static int SKILLCOUNT = 3;
    static int MAXMOVESPEED = 450;

    //UNIT
    static int SPAWNRATE = 15;
    static int UNITTARGETDISTANCE = 400;
    static int UNITTARGETDISTANCE2 = UNITTARGETDISTANCE*UNITTARGETDISTANCE;
    static int AGGROUNITRANGE = 300;
    static int AGGROUNITRANGE2 = AGGROUNITRANGE*AGGROUNITRANGE;
    static int AGGROUNITTIME = 3;
    static double DENYHEALTH = 0.4;

    static double BUSHRADIUS = 50;

    //TOWERS
    static int TOWERHEALTH = 3000;

    //NEUTRAL CREEP
    static int NEUTRALSPAWNTIME = 4;
    static int NEUTRALSPAWNRATE = 40;
    static int NEUTRALGOLD = 100;

//SPELLS
    // KNIGHT
    public static double EXPLOSIVESHIELDRANGE2 = 151*151;
    public static int EXPLOSIVESHIELDDAMAGE = 50;

    // LANCER
    public static int POWERUPMOVESPEED = 0;
    public static double POWERUPDAMAGEINCREASE = 0.3;
    public static int POWERUPRANGE = 10;

    //GOLD UNIT VALUES
    public static int MELEE_UNIT_GOLD_VALUE = 30;
    public static int RANGER_UNIT_GOLD_VALUE = 50;
    public static int HERO_GOLD_VALUE = 300;

    public static int GLOBAL_ID = 1;


//GRAPHICS
    // Hero sprites
    static String IRONMAN = "iron-man.png";
    static String HULK = "hulk.png";
    static String VALKYRIE = "valkyrie.png";
    static String DEADPOOL = "deadpool.png";
    static String DOCTOR_STRANGE = "doc-strange.png";

    // avatars
    static String MAGEAVATAR = "mage.png";

    // environment sprites
    static String BACKGROUND = "background.jpg";
    static String BUSH = "bush-orbs.png";

    // neutral creep
    static String GROOT = "groot.png";

    // team based sprites
    static String BLUETOWER = "tower-blue.png";
    static String REDTOWER = "tower-red.png";

    // Items
    static String POTION = "potion.png";

    //ITEM STATS
    static final String DAMAGE = "damage";
    static final String HEALTH = "health";
    static final String MAXHEALTH = "maxHealth";
    static final String MAXMANA = "maxMana";
    static final String MANA = "mana";
    static final String MOVESPEED = "moveSpeed";
    static final String MANAREGEN = "manaregeneration";
    public static final String[] STATS = {DAMAGE, HEALTH, MANA, MOVESPEED, MANAREGEN};
    static final int MINIMUM_STAT_COST = 30;
    public static final int NB_ITEMS_PER_LEVEL = 5;


    //Container
    public static ViewController viewController = new ViewController();

    public static Game game = new Game();

    public static MapFactory mapFactory = new MapFactory();

}
