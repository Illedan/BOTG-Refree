import java.io.Console;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.codingame.game.*;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.module.entities.Entity;
import com.codingame.gameengine.module.entities.GraphicEntityModule;

import tooltipModule.TooltipModule;

public class UnitTests {

    // Verification of different aspects of the game.

    public static void main(String[] args) {
        try {
            Class c = UnitTests.class;
            Method[] m = c.getDeclaredMethods();
            int testCount = 0;
            int failCount = 0;
            for (int i = 0; i < m.length; i++){
                if(m[i].getName().contains("Test")){
                    UnitTests testClass = new UnitTests();
                    testClass.resetGameWorld();
                    boolean result = false;
                    try{
                        result = (boolean)m[i].invoke(testClass);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    System.err.println((result ? "PASSED" : "FAILED") + " " + m[i].getName());

                    if(result) testCount++;
                    else failCount++;
                }
            }

            System.err.println("------------------------------------");
            System.err.println("Total tests: " + (testCount+failCount) + " Failed: " + failCount);

        } catch (Exception e) {
            System.err.println("TESTRUN failed: " + e.getMessage());
        }
    }

    private void resetGameWorld(){
        Const.game = new Game();
        Const.viewController = new ViewControllerTest();
        players = new ArrayList<>();
        players.add(new Player());
        players.add(new Player());

        players.get(0).tower = Factories.generateTower(players.get(0),0);
        players.get(1).tower = Factories.generateTower(players.get(1),1);
        players.get(0).heroes.add(Factories.generateHero("IRONMAN", players.get(0), new Point(0,0)));
        players.get(0).heroes.get(0).team = 0;
        players.get(1).heroes.add(Factories.generateHero("IRONMAN", players.get(1), new Point(0,0)));
        players.get(1).heroes.get(0).team = 1;
    }

    private void removeHeroes(){
        if(Const.game.allUnits.size() == 0) return;
        for(int i = Const.game.allUnits.size(); i >= 0; i++){
            if(Const.game.allUnits.get(i) instanceof Hero){
                Const.game.allUnits.remove(i);
            }
        }
    }

    private ArrayList<Player> players;


    // TEST START

    public boolean grootAttacks_invisHeroTest() {
        Hero hero0 = createAndReplaceHero("DEADPOOL", 40, 40, 500, 500, 100);

        Const.game.bushes.add(new Bush(60, 60));

        Creature groot = Factories.generateCreature(new Point(60, 60), 1);
        groot.health = 1000;
        groot.maxHealth = 1000;
        Const.game.allUnits.add(groot);

        try
        {
            doHeroCommandAndRun(players.get(0), "WAIT", 5);
        }catch (Exception e){
            return false;
        }

        try
        {
            doHeroCommandAndRun(players.get(0), "WAIT", 5);
        }catch (Exception e){
            return false;
        }

        try
        {
            doHeroCommandAndRun(players.get(0), "ATTACK " + groot.id, 5);
        }catch (Exception e){
            return false;
        }

        try
        {
            doHeroCommandAndRun(players.get(0), "ATTACK " + groot.id, 5);
        }catch (Exception e){
            return false;
        }

        return assertValue(500-groot.damage, hero0.health)
                && assertValue(1000-hero0.damage*2, groot.health)
                && assertValue(false, hero0.visible);

    }

    public boolean tower_hero_diesAtSameRound_draw_Test(){
        Hero hero0 = createAndReplaceHero("DEADPOOL", 500, 500, 100, 142, 100);
        players.get(1).heroes.clear();
        Hero hero1 = Factories.generateHero("HULK", players.get(1), new Point(500, 500));
        hero1.team = 1;
        hero1.health=10;
        Const.game.allUnits.add(hero1);
        Tower tower = players.get(0).tower;
        Const.game.allUnits.add(tower);
        tower.health = 11;
        tower.x = 499;
        tower.y = 499;
        players.get(1).heroes.add(hero1);

        try {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"ATTACK " + hero1.id});
            players.get(1).handlePlayerOutputs(new String[]{"ATTACK " + tower.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        return assertValue(11-hero1.damage, tower.health)
                && assertValue(10-hero0.damage, hero1.health);
    }



    public boolean ironman_spell2_Test(){
        Hero hero = createAndReplaceHero("IRONMAN", 40, 40, 100, 200, 100);

        LaneUnit unit4 = new LaneUnit(100,40,500,1,100, new Point(50,50), players.get(1));
        unit4.range=100;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        LaneUnit unit5 = new LaneUnit(500,91,500,1,900, new Point(91,91), players.get(1));
        unit5.range=100;
        unit5.damage=55;
        Const.game.allUnits.add(unit5);

        LaneUnit unit6 = new LaneUnit(200,40,500,1,100, new Point(50,50), players.get(1));
        unit6.range=100;
        unit6.damage=55;
        Const.game.allUnits.add(unit6);

        try
        {
            doHeroCommandAndRun(players.get(0), "BURNING " + 100 + " " + 40, 5);
        }catch (Exception e){
            return false;
        }
        int dmg = hero.manaregeneration*5+30;

        return assertValue(500-dmg, unit4.health)
                && assertValue(500-dmg, unit6.health)
                && assertValue(500, unit5.health);
    }


    public boolean MoveAttack_BlinkOut_DiscardsAttack_Test(){
        Hero hero = createAndReplaceHero("IRONMAN", 40, 40, 100, 200, 100);
        hero.moveSpeed=400  ;
        Hero hero0 = players.get(1).heroes.get(0);
        hero0.x = 40;
        hero0.y = 200;
        Const.game.allUnits.add(hero0);
        int hero0Health = hero0.health;

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"MOVE_ATTACK 40 200 " +hero0.id});
            players.get(1).handlePlayerOutputs(new String[]{"BLINK 200 200"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertValue(hero0Health, hero0.health);
    }


    public boolean ironman_spell1_blinkOutOfFireBall_Test(){
        Hero hero = createAndReplaceHero("IRONMAN", 40, 40, 100, 200, 100);
        Hero hero0 = players.get(1).heroes.get(0);
        hero0.x = 40;
        hero0.y = 200;
        Const.game.allUnits.add(hero0);
        int hero0Health = hero0.health;

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"FIREBALL 40 200"});
            players.get(1).handlePlayerOutputs(new String[]{"BLINK 200 200"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertValue(hero0Health, hero0.health);
    }

    public boolean ironman_spell1_blinkIntoFireball_Test(){
        Hero hero = createAndReplaceHero("IRONMAN", 40, 40, 100, 200, 100);
        Hero hero0 = players.get(1).heroes.get(0);
        hero0.x = 200;
        hero0.y = 900;
        Const.game.allUnits.add(hero0);
        int hero0Health = hero0.health;

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"FIREBALL 40 200"});
            players.get(1).handlePlayerOutputs(new String[]{"BLINK 40 900"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertValue(true, hero0.health < hero0Health-0.2*hero.mana);
    }


    public boolean ironman_fireballNotHittingEnemiesTest_Test(){
        Hero hero = createAndReplaceHero("IRONMAN", 233, 550, 100, 200, 100);
        Hero hero1 = players.get(1).heroes.get(0);
        hero1.moveSpeed = 212;
        hero1.x = 582;
        hero1.y = 380;
        int health1 = hero1.health;
        Const.game.allUnits.add(hero1);

        Hero hero2 = Factories.generateHero("HULK", players.get(1), new Point(582, 380));
        hero2.moveSpeed = 200;
        hero2.team = 1;
        int health2 = hero2.health;
        players.get(1).heroes.add(hero2);
        Const.game.allUnits.add(hero2);

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"FIREBALL 582 380"});
            players.get(1).handlePlayerOutputs(new String[]{"MOVE 582 380", "MOVE 582 380"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        int fbDmg = (int)(55*((hero.distance(hero1)-50)/1000)+(int)(0.2*(hero.mana+60)));

        return assertValue((int)(health1-fbDmg), hero1.health)
                && assertValue((int)(health2-fbDmg), hero2.health);
    }

    public boolean ironman_spell1_JustOutside_Test(){
        Hero hero = createAndReplaceHero("IRONMAN", 40, 40, 100, 200, 100);
        Hero hero1 = players.get(1).heroes.get(0);
        hero1.x = 40+900+51;
        hero1.y = 40;
        int health = hero1.health;
        Const.game.allUnits.add(hero1);

        try
        {
            doHeroCommandAndRun(players.get(0), "FIREBALL " + 900 + " " + 40, 5);
        }catch (Exception e){
            return false;
        }

        return assertValue(health, hero1.health);
    }


    public boolean ironman_spell1_Test(){
        Hero hero = createAndReplaceHero("IRONMAN", 40, 40, 100, 200, 100);
        Hero hero1 = players.get(1).heroes.get(0);
        hero1.x = 900;
        hero1.y = 40;
        int health = hero1.health;
        Const.game.allUnits.add(hero1);

        try
        {
            doHeroCommandAndRun(players.get(0), "FIREBALL " + 900 + " " + 40, 5);
        }catch (Exception e){
            return false;
        }

        return assertValue(true, hero1.health < health-0.2*hero.mana);
    }


    public boolean ironman_spell0_OutsideRange_Test(){
        Hero hero = createAndReplaceHero("IRONMAN", 40, 40, 100, 200, 100);

        LaneUnit unit4 = new LaneUnit(100,40,500,1,100, new Point(50,50), players.get(1));
        unit4.range=100;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        try
        {
            doHeroCommandAndRun(players.get(0), "BLINK " + 900 + " " + 40, 5);
        }catch (Exception e){
            return false;
        }

        return assertValue(100, hero.health)
                && assertDouble(40+hero.skills[0].range, hero.x)
                && assertDouble(40, hero.y);
    }

    public boolean ironman_spell0_Test(){
        Hero hero = createAndReplaceHero("IRONMAN", 40, 40, 100, 200, 100);

        LaneUnit unit4 = new LaneUnit(100,40,500,1,100, new Point(50,50), players.get(1));
        unit4.range=100;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        try
        {
            doHeroCommandAndRun(players.get(0), "BLINK " + 150 + " " + 150, 5);
        }catch (Exception e){
            return false;
        }

        return assertValue(100, hero.health)
                && assertDouble(150, hero.x)
                && assertDouble(150, hero.y);
    }

    public boolean lancer_spell2_Test(){
        Hero hero = createAndReplaceHero("VALKYRIE", 40, 40, 100, 200, 100);

        int prevMs = hero.moveSpeed;
        int prevDmg = hero.damage;
        int prevRange = hero.range;

        try
        {
            doHeroCommandAndRun(players.get(0), "POWERUP", 5);
        }catch (Exception e){
            return false;
        }

        boolean result = assertValue(prevMs+Const.POWERUPMOVESPEED, hero.moveSpeed)
                && assertValue(prevDmg+(int)(prevMs*Const.POWERUPDAMAGEINCREASE), hero.damage)
                && assertValue(prevRange+Const.POWERUPRANGE, hero.range);

        runRound(5);
        result &= assertValue(prevMs+Const.POWERUPMOVESPEED, hero.moveSpeed)
                && assertValue(prevDmg+(int)(prevMs*Const.POWERUPDAMAGEINCREASE), hero.damage)
                && assertValue(prevRange+Const.POWERUPRANGE, hero.range);
        runRound(5);
        result &= assertValue(prevMs+Const.POWERUPMOVESPEED, hero.moveSpeed)
                && assertValue(prevDmg+(int)(prevMs*Const.POWERUPDAMAGEINCREASE), hero.damage)
                && assertValue(prevRange+Const.POWERUPRANGE, hero.range);
        runRound(5);

        result &= assertValue(prevMs, hero.moveSpeed)
                && assertValue(prevDmg, hero.damage)
                && assertValue(prevRange, hero.range);

        return result;
    }

    public boolean lancer_spell1_Test(){
        Hero hero = createAndReplaceHero("VALKYRIE", 40, 40, 100, 200, 100);

        LaneUnit unit4 = new LaneUnit(350,40,500,1,100, new Point(300,40), players.get(1));
        unit4.range=100;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        try
        {
            doHeroCommandAndRun(players.get(0), "JUMP " + 300 + " " + 40, 5);
        }catch (Exception e){
            return false;
        }

        return assertValue(500-hero.damage, unit4.health)
                && assertDouble(290, hero.x)
                && assertDouble(40, hero.y);
    }


    public boolean lancer_doubleFlip_Test(){
        Hero hero = createAndReplaceHero("VALKYRIE", 400, 500, 100, 200, 100);
        Hero hero0 = Factories.generateHero("VALKYRIE", players.get(0), new Point(350,550));
        players.get(0).addHero(hero0);
        Const.game.allUnits.add(hero0);

        LaneUnit unit4 = new LaneUnit(300,500,500,1,200, new Point(400,500), players.get(1));
        unit4.range=100;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        double xFlip = (hero.x-unit4.x)*2;
        double yFlip = (hero.y-unit4.y)*2;

        double xFlip2 = (hero0.x-unit4.x)*2;
        double yFlip2 = (hero0.y-unit4.y)*2;

        int expectedX = Utilities.round(unit4.x+xFlip+xFlip2);
        int expectedY = Utilities.round(unit4.y+yFlip+yFlip2);

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"SPEARFLIP " + unit4.id, "SPEARFLIP " + unit4.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(expectedX, unit4.x)
                && assertDouble(expectedY, unit4.y);
    }


    public boolean dr_strange_DoublePull_Test(){
        Hero hero = createAndReplaceHero("DOCTOR_STRANGE", 400, 500, 100, 200, 100);
        Hero hero0 = Factories.generateHero("DOCTOR_STRANGE", players.get(0), new Point(350,550));
        players.get(0).addHero(hero0);
        Const.game.allUnits.add(hero0);

        LaneUnit unit4 = new LaneUnit(300,500,500,1,200, new Point(400,500), players.get(1));
        unit4.range=100;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        double dist2 = unit4.distance(hero0);
        double dist = unit4.distance(hero);

        double xPull = (hero.x-unit4.x)/dist*200;
        double yPull = (hero.y-unit4.y)/dist*200;

        double xPull2 = (hero0.x-unit4.x)/dist2*200;
        double yPull2 = (hero0.y-unit4.y)/dist2*200;

        int expectedX = Utilities.round(unit4.x+xPull2+xPull);
        int expectedY = Utilities.round(unit4.y+yPull2+yPull);

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"PULL " + unit4.id, "PULL " + unit4.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(expectedX, unit4.x)
                && assertDouble(expectedY, unit4.y);
    }


    public boolean lancer_spell0_UnitDraggedByStrange_Test(){
        Hero hero = createAndReplaceHero("VALKYRIE", 400, 500, 100, 200, 100);
        Hero hero0 = Factories.generateHero("DOCTOR_STRANGE", players.get(0), new Point(350,550));
        players.get(0).addHero(hero0);
        Const.game.allUnits.add(hero0);

        LaneUnit unit4 = new LaneUnit(300,500,500,1,200, new Point(400,500), players.get(1));
        unit4.range=100;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        double dist2 = unit4.distance(hero0);

        double xFlip = (hero.x-unit4.x)*2;
        double yFlip = (hero.y-unit4.y)*2;

        double xPull = (hero0.x-unit4.x)/dist2*200;
        double yPull = (hero0.y-unit4.y)/dist2*200;

        int expectedX = Utilities.round(unit4.x+xFlip+xPull);
        int expectedY = Utilities.round(unit4.y+yFlip+yPull);

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"SPEARFLIP " + unit4.id, "PULL " + unit4.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(expectedX, unit4.x)
                && assertDouble(expectedY, unit4.y);
    }


    public boolean lancer_spell0_LongerDist_Test(){
        Hero hero = createAndReplaceHero("VALKYRIE", 455, 500, 100, 200, 100);

        LaneUnit unit4 = new LaneUnit(300,500,500,1,200, new Point(400,500), players.get(1));
        unit4.range=100;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        try
        {
            doHeroCommandAndRun(players.get(0), "SPEARFLIP " + unit4.id, 5);
        }catch (Exception e){
            return false;
        }

        return assertDouble(hero.x+155, unit4.x)
                && assertDouble(500, unit4.y)
                && assertDouble(500-hero.damage*0.4, unit4.health);
    }

    public boolean lancer_spell0_Test(){
        Hero hero = createAndReplaceHero("VALKYRIE", 40, 40, 100, 200, 100);

        LaneUnit unit4 = new LaneUnit(30,50,500,1,200, new Point(40,40), players.get(1));
        unit4.range=200;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        try
        {
            doHeroCommandAndRun(players.get(0), "SPEARFLIP " + unit4.id, 5);
        }catch (Exception e){
            return false;
        }

        return assertDouble(50, unit4.x)
                && assertDouble(30, unit4.y);
    }

    public boolean knight_spell2_OutSideRange_NoBash_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);

        LaneUnit unit4 = new LaneUnit(500,50,500,1,500, new Point(40,40), players.get(1));
        unit4.range=200;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        try
        {
            doHeroCommandAndRun(players.get(0), "BASH " + unit4.id, 5);
        }catch (Exception e){
            return false;
        }

        return assertValue(0, unit4.stunTime)
                && assertValue(45, hero.health);
    }

    public boolean knight_spell2_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);

        LaneUnit unit4 = new LaneUnit(50,50,500,1,200, new Point(40,40), players.get(1));
        unit4.range=200;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        try
        {
            doHeroCommandAndRun(players.get(0), "BASH " + unit4.id, 5);
        }catch (Exception e){
            return false;
        }

        return assertValue(1, unit4.stunTime)
                && assertValue(500-hero.damage, unit4.health)
                && assertValue(45, hero.health);
    }


    public boolean knight_Cleric_spell1_doubleShiel_HULKBiggest_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 30000, 3000, 100);
        Hero hero0 = Factories.generateHero("DOCTOR_STRANGE", players.get(0), new Point(90,90));
        players.get(0).addHero(hero0);
        Const.game.allUnits.add(hero0);

        LaneUnit unit4 = new LaneUnit(50,50,500,1,200, new Point(40,40), players.get(1));
        unit4.range=200;
        unit4.damage=100;
        Const.game.allUnits.add(unit4);

        LaneUnit unit5 = new LaneUnit(50,50,500,1,200, new Point(40,40), players.get(1));
        unit5.range=200;
        unit5.damage=100;
        Const.game.allUnits.add(unit5);

        LaneUnit unit6 = new LaneUnit(500,500,500,1,200, new Point(40,40), players.get(1));
        unit6.range=200;
        unit6.damage=55;
        Const.game.allUnits.add(unit6);
        int shield = 0;
        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"EXPLOSIVESHIELD", "SHIELD " + hero.id});
            shield = hero.getShield();
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }
        int shieldedVal = (int)(hero.maxHealth*0.07+50+hero0.maxMana*0.5+50);

        int dmg = unit4.damage+unit5.damage;
        boolean result =  assertValue((int)3000, hero.health)
                && assertValue(shield, shieldedVal)
                && assertValue(shieldedVal-dmg, hero.getShield())
                && assertValue(0, hero.shield)
                && assertValue(shieldedVal-dmg, hero.explosiveShield)
                && assertValue(500, unit4.health)
                && assertValue(500, unit5.health)
                && assertValue(500, unit6.health);
        unit4.damage = 0;
        unit5.damage = 0;
        unit6.damage = 0;
        runRound(5);
        result &= assertValue(shieldedVal-dmg, hero.getShield());
        runRound(5);
        result &= assertValue(shieldedVal-dmg, hero.getShield());
        runRound(5);
        result &= assertValue(0, hero.getShield()) && assertValue(500, unit4.health)
                && assertValue(500, unit5.health)
                && assertValue(500, unit6.health);
        runRound(5);
        runRound(5);
        runRound(5);

        return result && assertValue(0, hero.getShield());
    }


    public boolean knight_Cleric_spell1_doubleShiel_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);
        Hero hero0 = Factories.generateHero("DOCTOR_STRANGE", players.get(0), new Point(90,90));
        players.get(0).addHero(hero0);
        Const.game.allUnits.add(hero0);

        LaneUnit unit4 = new LaneUnit(50,50,500,1,200, new Point(40,40), players.get(1));
        unit4.range=200;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        LaneUnit unit5 = new LaneUnit(50,50,500,1,200, new Point(40,40), players.get(1));
        unit5.range=200;
        unit5.damage=35;
        Const.game.allUnits.add(unit5);

        LaneUnit unit6 = new LaneUnit(500,500,500,1,200, new Point(40,40), players.get(1));
        unit6.range=200;
        unit6.damage=55;
        Const.game.allUnits.add(unit6);
        int shield = 0;
        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"EXPLOSIVESHIELD", "SHIELD " + hero.id});
            shield = hero.getShield();
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }
        int shieldedVal = (int)(hero.maxHealth*0.07+50+hero0.maxMana*0.5+50);

        int dmg = unit4.damage+unit5.damage;
        boolean result =  assertValue((int)100, hero.health)
                && assertValue(shield, shieldedVal)
                && assertValue(shieldedVal-dmg, hero.getShield())
                && assertValue(shieldedVal-dmg, hero.shield)
                && assertValue(0, hero.explosiveShield)
                && assertValue(500-Const.EXPLOSIVESHIELDDAMAGE, unit4.health)
                && assertValue(500-Const.EXPLOSIVESHIELDDAMAGE, unit5.health)
                && assertValue(500, unit6.health);
        unit4.damage = 0;
        unit5.damage = 0;
        unit6.damage = 0;
        runRound(5);
        result &= assertValue(shieldedVal-dmg, hero.getShield());
        runRound(5);
        runRound(5);
        runRound(5);
        runRound(5);
        runRound(5);

        return result && assertValue(0, hero.getShield());
    }

    public boolean knight_spell1_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);

        LaneUnit unit4 = new LaneUnit(50,50,500,1,200, new Point(40,40), players.get(1));
        unit4.range=200;
        unit4.damage=55;
        Const.game.allUnits.add(unit4);

        LaneUnit unit5 = new LaneUnit(50,50,500,1,200, new Point(40,40), players.get(1));
        unit5.range=200;
        unit5.damage=55;
        Const.game.allUnits.add(unit5);

        LaneUnit unit6 = new LaneUnit(500,500,500,1,200, new Point(40,40), players.get(1));
        unit6.range=200;
        unit6.damage=55;
        Const.game.allUnits.add(unit6);

        try
        {
            doHeroCommandAndRun(players.get(0), "EXPLOSIVESHIELD", 5);
        }catch (Exception e){
            return false;
        }

        return assertValue((int)(100+hero.maxHealth*0.07+50-unit4.damage-unit5.damage), hero.health)
                && assertValue(0, hero.shield)
                && assertValue(500-Const.EXPLOSIVESHIELDDAMAGE, unit4.health)
                && assertValue(500-Const.EXPLOSIVESHIELDDAMAGE, unit5.health)
                && assertValue(500, unit6.health);
    }

    public boolean doubleMoveMent_oneDead_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);
        Hero hero0 = Factories.generateHero("HULK", players.get(1), new Point(60, 60));
        hero0.team = 0;
        Const.game.allUnits.add(hero0);
        players.get(0).heroes.add(hero0);
        hero.isDead = true;
        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"MOVE 150 100"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(40, hero.x)
                && assertDouble(40, hero.y)
                && assertDouble(150, hero0.x)
                && assertDouble(100, hero0.y);
    }

    public boolean OneMovement_firstStunnedAndDead_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);
        Hero hero0 = Factories.generateHero("HULK", players.get(0), new Point(60, 60));
        hero0.team = 0;
        Const.game.allUnits.add(hero0);
        players.get(0).heroes.add(hero0);
        hero.stunTime = 3;
        hero.isDead = true;
        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"MOVE 150 100"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(40, hero.x)
                && assertDouble(40, hero.y)
                && assertDouble(150, hero0.x)
                && assertDouble(100, hero0.y);
    }

    public boolean doubleMoveMent_firstStunned_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);
        Hero hero0 = Factories.generateHero("HULK", players.get(0), new Point(60, 60));
        hero0.team = 0;
        Const.game.allUnits.add(hero0);
        players.get(0).heroes.add(hero0);
        hero.stunTime = 3;
        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"MOVE 100 101", "MOVE 150 100"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(40, hero.x)
                && assertDouble(40, hero.y)
                && assertDouble(150, hero0.x)
                && assertDouble(100, hero0.y);
    }

    public boolean doubleMoveMent_oneStunned_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);
        Hero hero0 = Factories.generateHero("HULK", players.get(0), new Point(60, 60));
        hero0.team = 0;
        Const.game.allUnits.add(hero0);
        players.get(0).heroes.add(hero0);
        hero0.stunTime = 3;
        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"MOVE 100 101", "MOVE 150 100"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(100, hero.x)
                && assertDouble(101, hero.y)
                && assertDouble(60, hero0.x)
                && assertDouble(60, hero0.y);
    }


    public boolean knight_spell0_JumpWhenBashed_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);
        players.get(1).heroes.clear();
        Hero hero0 = Factories.generateHero("HULK", players.get(1), new Point(60, 60));
        hero0.team = 1;
        Const.game.allUnits.add(hero0);
        players.get(1).heroes.add(hero0);
        int dmg = hero.damage;

        LaneUnit unit4 = new LaneUnit(300,40,500,1,200, new Point(40,40), players.get(1));
        unit4.range=200;
        unit4.damage=3;
        Const.game.allUnits.add(unit4);

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"CHARGE " + unit4.id});
            players.get(1).handlePlayerOutputs(new String[]{"BASH " + hero.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(300, hero.x)
                && assertDouble(40, hero.y)
                && assertValue(100, hero.health)
                && assertDouble(500-hero.damage*0.5, unit4.health)
                && assertValue(dmg, hero.damage)
                && assertValue(50, unit4.moveSpeed);
    }


    public boolean knight_bash_lancerFlips_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);
        Hero hero0 = Factories.generateHero("VALKYRIE", players.get(0), new Point(150, 40));
        hero0.team=1;
        hero0.health=150;
        hero0.moveSpeed = 200;
        Const.game.allUnits.add(hero0);
        players.get(1).heroes.clear();
        players.get(1).heroes.add(hero0);

        double vx = hero0.x-hero.x;
        double vy = hero0.y-hero.y;
        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"BASH " +hero0.id});
            players.get(1).handlePlayerOutputs(new String[]{"SPEARFLIP " +hero.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble((int)Utilities.round(hero0.x+vx), hero.x)
                && assertDouble((int)Utilities.round(hero0.y+vy), hero.y)
                && assertValue(1, hero0.stunTime)
                && assertDouble((int)(100-hero0.damage*0.4), hero.health)
                && assertDouble(150-hero.damage, hero0.health);
    }

    public boolean knight_lancerFlips_spell0_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);
        Hero hero0 = Factories.generateHero("VALKYRIE", players.get(0), new Point(150, 40));
        hero0.team=1;
        hero0.health=150;
        hero0.moveSpeed = 200;
        Const.game.allUnits.add(hero0);
        players.get(1).heroes.clear();
        players.get(1).heroes.add(hero0);

        double vx = hero0.x-hero.x;
        double vy = hero0.y-hero.y;
        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"CHARGE " +hero0.id});
            players.get(1).handlePlayerOutputs(new String[]{"SPEARFLIP " +hero.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble((int)Utilities.round(hero0.x+vx), hero.x)
                && assertDouble((int)Utilities.round(hero0.y+vy), hero.y)
                && assertDouble(50, hero0.moveSpeed)
                && assertDouble((int)(100), hero.health)
                && assertDouble(150, hero0.health);
    }

    public boolean knight_jumpsKnight_NoAttackDone_bothBlinks_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);
        Hero hero0 = Factories.generateHero("HULK", players.get(0), new Point(60, 45));
        hero0.team=1;
        hero0.moveSpeed = 200;
        int h = hero0.health;
        Const.game.allUnits.add(hero0);
        players.get(1).heroes.clear();
        players.get(1).heroes.add(hero0);

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"CHARGE " +hero0.id});
            players.get(1).handlePlayerOutputs(new String[]{"CHARGE " +hero.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(60, hero.x)
                && assertDouble(45, hero.y)
                && assertDouble(40, hero0.x)
                && assertDouble(40, hero0.y)
                && assertDouble(50, hero0.moveSpeed)
                && assertDouble(100, hero.health)
                && assertDouble(h, hero0.health);
    }

    public boolean groot_dies_shouldStopInRange_Test(){
        Hero hero = createAndReplaceHero("HULK", 600, 400, 100, 200, 100);
        Hero hero0 = Factories.generateHero("HULK", players.get(1), new Point(560, 560));
        hero0.team=1;
        hero.damage = 100;
        hero0.damage = 100;
        hero0.moveSpeed = 200;
        Const.game.allUnits.add(hero0);
        players.get(1).heroes.clear();
        players.get(1).heroes.add(hero0);
        Creature groot = new Creature(600, 600);
        groot.health = 99;
        Const.game.allUnits.add(groot);

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"ATTACK " +groot.id});
            players.get(1).handlePlayerOutputs(new String[]{"ATTACK " +groot.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(groot.y-hero.range, hero.y)
                && assertDouble(600, hero.x);
    }


    public boolean groot_KilledByDifferentTime_Test(){
        Hero hero = createAndReplaceHero("HULK", 600, 700, 100, 200, 100);
        Hero hero0 = Factories.generateHero("HULK", players.get(1), new Point(560, 560));
        hero0.team=1;
        hero.damage = 100;
        hero0.damage = 100;
        hero0.moveSpeed = 200;
        Const.game.allUnits.add(hero0);
        players.get(1).heroes.clear();
        players.get(1).heroes.add(hero0);
        Creature groot = new Creature(600, 600);
        groot.health = 99;
        groot.goldValue = 1337;
        Const.game.allUnits.add(groot);
        players.get(1).gold=1;

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"ATTACK " +groot.id});
            players.get(1).handlePlayerOutputs(new String[]{"ATTACK " +groot.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertValue(0, players.get(0).gold)
                && assertValue(1338, players.get(1).gold);
    }


    public boolean groot_KilledByDifferentDmg_Test(){
        Hero hero = createAndReplaceHero("HULK", 555, 555, 100, 200, 100);
        Hero hero0 = Factories.generateHero("HULK", players.get(1), new Point(560, 560));
        hero0.team=1;
        hero.damage = 101;
        hero0.damage = 100;
        hero0.moveSpeed = 200;
        Const.game.allUnits.add(hero0);
        players.get(1).heroes.clear();
        players.get(1).heroes.add(hero0);
        Creature groot = new Creature(600, 600);
        groot.health = 150;
        groot.goldValue = 1337;
        Const.game.allUnits.add(groot);
        players.get(1).gold=1;

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"ATTACK " +groot.id});
            players.get(1).handlePlayerOutputs(new String[]{"ATTACK " +groot.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertValue(1337, players.get(0).gold)
                && assertValue(1, players.get(1).gold);
    }

    public boolean groot_KilledBy2Teams_BothGetsGold_Test(){
        Hero hero = createAndReplaceHero("HULK", 555, 555, 100, 200, 100);
        Hero hero0 = Factories.generateHero("HULK", players.get(1), new Point(560, 560));
        hero0.team=1;
        hero.damage = 100;
        hero0.damage = 100;
        hero0.moveSpeed = 200;
        Const.game.allUnits.add(hero0);
        players.get(1).heroes.clear();
        players.get(1).heroes.add(hero0);
        Creature groot = new Creature(600, 600);
        groot.health = 150;
        groot.goldValue = 1337;
        Const.game.allUnits.add(groot);
        players.get(1).gold=1;

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"ATTACK " +groot.id});
            players.get(1).handlePlayerOutputs(new String[]{"ATTACK " +groot.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertValue(1337, players.get(0).gold)
                && assertValue(1338, players.get(1).gold);
    }

    public boolean knight_jumpsKnight_spell0_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);
        Hero hero0 = Factories.generateHero("HULK", players.get(0), new Point(300, 45));
        hero0.team=1;
        hero0.moveSpeed = 200;
        Const.game.allUnits.add(hero0);
        players.get(1).heroes.clear();
        players.get(1).heroes.add(hero0);

        try
        {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"CHARGE " +hero0.id});
            players.get(1).handlePlayerOutputs(new String[]{"CHARGE " +hero.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(300, hero.x)
                && assertDouble(45, hero.y)
                && assertDouble(40, hero0.x)
                && assertDouble(40, hero0.y)
                && assertDouble(50, hero0.moveSpeed)
                && assertDouble(50, hero0.moveSpeed)
                && assertDouble(100, hero.health);
    }

    public boolean knight_spell0_Test(){
        Hero hero = createAndReplaceHero("HULK", 40, 40, 100, 200, 100);

        LaneUnit unit4 = new LaneUnit(300,40,500,1,200, new Point(40,40), players.get(1));
        unit4.range=200;
        unit4.damage=3;
        Const.game.allUnits.add(unit4);

        try
        {
            doHeroCommandAndRun(players.get(0), "CHARGE "  + unit4.id, 5);
        }catch (Exception e){
            return false;
        }

        boolean result = assertDouble(300, hero.x)
                && assertDouble(40, hero.y)
                && assertValue(100, hero.health)
                && assertDouble(500-hero.damage*0.5, unit4.health)
                && assertValue(50, unit4.moveSpeed);

        runRound(5);
        result &= assertValue(50, unit4.moveSpeed);
        runRound(5);

        return result && assertValue(200, unit4.moveSpeed);
    }

    public boolean cleric_Pull_CheckBug_Test(){
        Hero hero = createAndReplaceHero("DOCTOR_STRANGE", 823, 546, 100, 200, 100);
        players.get(1).heroes.clear();
        Hero hero1 = Factories.generateHero("VALKYRIE", players.get(1), new Point(989, 590));
        Const.game.allUnits.add(hero1);
        hero1.team=1;
        players.get(1).heroes.add(hero1);
        LaneUnit unit = new LaneUnit(899, 590, 200, 0, 150, new Point(2000, 540), players.get(0));
        unit.team = 0;
        Const.game.allUnits.add(unit);

        double dist = hero.distance(hero1);
        double xPull = (int)((hero.x-hero1.x)/dist*200);
        double yPull = (int)((hero.y-hero1.y)/dist*200);

        double movex = hero1.moveSpeed*0.1;
        double movey = 0;
        try
        {
            Const.game.beforeTurn(5, players);
            players.get(1).handlePlayerOutputs(new String[]{"MOVE " +(hero1.x+hero1.moveSpeed*0.1) + " " + hero1.y});
            players.get(0).handlePlayerOutputs(new String[]{"PULL " + hero1.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble((int)(989+xPull+movex), hero1.x)
                && assertDouble((int)(590+yPull+movey), hero1.y);
    }

    public boolean Attack_NotStopping_Test(){
        String s = "100 540 3000 MainClass+Tower 0 1 |1820 540 3000 MainClass+Tower 1 2 |820 490 1450 MainClass+Hero 0 3 HULK|1100 490 1450 MainClass+Hero 1 4 HULK|855 490 1400 MainClass+Hero 0 5 VALKYRIE|1065 490 1400 MainClass+Hero 1 6 VALKYRIE|915 490 185 MainClass+Unit 0 7 |915 540 325 MainClass+Unit 0 8 |915 590 325 MainClass+Unit 0 9 |785 490 250 MainClass+Unit 0 10 |1005 490 185 MainClass+Unit 1 11 |1005 540 325 MainClass+Unit 1 12 |1005 590 325 MainClass+Unit 1 13 |1135 490 250 MainClass+Unit 1 14 |739 147 400 MainClass+Unit -1 15 |960 53 400 MainClass+Unit -1 16 |1181 147 400 MainClass+Unit -1 17";
        String s2 = s.replaceAll("\\|", "-");
        String[] unitDef = s2.split("-");
        players.get(0).heroes.clear();
        players.get(1).heroes.clear();
        for(String def : unitDef){
            String[] splittedDef = def.trim().split(" ");
            try {
                if(splittedDef.length < 5) continue;
                if (def.contains("Tower") ||  splittedDef[4].equals("-1")) continue;
                int x = Integer.parseInt(splittedDef[0]);
                int y = Integer.parseInt(splittedDef[1]);
                int health = Integer.parseInt(splittedDef[2]);
                int team = Integer.parseInt(splittedDef[4]);
                int id = Integer.parseInt(splittedDef[5]);
                if (def.contains("Hero") && def.contains("HULK")) {
                    Hero hero = Factories.generateHero(splittedDef[6], players.get(team), new Point(x, y));
                    hero.team = team;
                    hero.health = health;
                    hero.id = id;
                    Const.game.allUnits.add(hero);
                    players.get(team).heroes.add(hero);
                } else if(id==11 || id==7){
                    Unit unit = new LaneUnit(x, y, health, team, 150, new Point(960, 540), players.get(team));
                    unit.team = team;
                    unit.id = id;
                    Const.game.allUnits.add(unit);
                }
            }catch (Exception e){
                System.err.println(def);
                e.printStackTrace();
            }
        }

        try{
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"ATTACK 7", "ATTACK 7"});
            players.get(1).handlePlayerOutputs(new String[]{"ATTACK 11", "ATTACK 11"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            return false;
        }

        return assertDouble(1100, players.get(1).heroes.get(0).x);
    }

    public boolean cleric_pullTower_Test(){
        createAndReplaceHero("DOCTOR_STRANGE", 400, 400, 100, 200, 100);
        Tower tower = players.get(0).tower;
        Const.game.allUnits.add(tower);
        tower.x = 500;
        tower.y = 500;

        try
        {
            doHeroCommandAndRun(players.get(0), "PULL " + tower.id, 5);
        }catch (Exception e){
            return false;
        }

        return assertDouble(500, tower.x) && assertDouble(500, tower.y);
    }

    public boolean cleric_spell2_DrainMana_Test(){
        createAndReplaceHero("DOCTOR_STRANGE", 400, 400, 100, 200, 100);
        Hero hero0 = players.get(0).heroes.get(0);
        int prevMana = hero0.mana;
        Hero hero1 = players.get(1).heroes.get(0);
        hero1.x = 400;
        hero1.y = 555;
        hero1.mana = 100;
        hero1.manaregeneration = 5;

        Const.game.allUnits.add(hero1);

        try
        {
            doHeroCommandAndRun(players.get(0), "PULL " + hero1.id, 5);
        }catch (Exception e){
            return false;
        }

        return assertDouble(400, hero1.x)
                && assertDouble(555-200, hero1.y)
                && assertValue(100-hero1.manaregeneration*3+5-5, hero1.mana)
                && assertValue(prevMana-hero0.skills[2].manaCost+hero0.manaregeneration+hero1.manaregeneration*3+5, hero0.mana);
    }

    public boolean cleric_spell2_verifyALLDirections_Test(){

        for(double i = 0; i < 360; i+=0.1) {
            double angle = Math.toRadians(i);
            double xTarget = 720+Math.cos(angle)*1000;
            double yTarget = 490+Math.sin(angle)*1000;
            Point target = new Point(xTarget, yTarget);
            Hero hero0 = createAndReplaceHero("DOCTOR_STRANGE", 1084, 490, 100, 200, 100);
            LaneUnit unit3 = new LaneUnit(720, 490, 10, 1, 200, target, players.get(1));
            unit3.range = 200;
            unit3.damage = 99;
            Const.game.allUnits.add(unit3);

            double unitDist = target.distance(unit3);
            double vx = (xTarget-unit3.x)/unitDist*20;
            double vy = (yTarget-unit3.y)/unitDist*20;


            double dist = hero0.distance(unit3);
            int newX = Utilities.round((hero0.x - unit3.x) / dist * 200 + unit3.x +vx);
            int newY = Utilities.round((hero0.y - unit3.y) / dist * 200 + unit3.y +vy);

            try {
                doHeroCommandAndRun(players.get(0), "PULL " + unit3.id, 5);
            } catch (Exception e) {
                return false;
            }
            Const.game.allUnits.remove(unit3);

            if(assertDouble(newX, unit3.x)
                    && assertDouble(newY, unit3.y)) continue;
            else return false;
        }

        return true;
    }

    public boolean cleric_spell2_Test(){
        createAndReplaceHero("DOCTOR_STRANGE", 400, 400, 100, 200, 100);
        Hero hero0 = players.get(0).heroes.get(0);
        int prevMana = hero0.mana;
        LaneUnit unit3 = new LaneUnit(500,400,10,1,100, new Point(400,400), players.get(1));
        unit3.range=200;
        unit3.damage = 99;
        Const.game.allUnits.add(unit3);

        try
        {
            doHeroCommandAndRun(players.get(0), "PULL " + unit3.id, 5);
        }catch (Exception e){
            return false;
        }

        return assertValue(1, hero0.health)
                && assertDouble(500-200, unit3.x)
                && assertDouble(400, unit3.y)
                && assertValue(prevMana-hero0.skills[2].manaCost+hero0.manaregeneration, hero0.mana);
    }

    public boolean cleric_spell1_ShieldNotRemoved_Test(){
        Hero hero0 = createAndReplaceHero("DOCTOR_STRANGE", 40, 40, 100, 200, 100);

        LaneUnit unit3 = new LaneUnit(55,56,10,1,100, new Point(60,60), players.get(0));
        unit3.damage=42;
        unit3.range=100;
        Const.game.allUnits.add(unit3);

        try
        {
            doHeroCommandAndRun(players.get(0), "SHIELD " + hero0.id, 5);
        }catch (Exception e){
            return false;
        }
        int shield = (int)(hero0.maxMana*0.5+50);

        return assertValue(100, hero0.health)
                && assertValue(shield-unit3.damage, hero0.shield);
    }

    public boolean cleric_spell1_Test(){
        Hero hero0 = createAndReplaceHero("DOCTOR_STRANGE", 40, 40, 100, 200, 100);

        LaneUnit unit3 = new LaneUnit(55,56,10,1,100, new Point(60,60), players.get(0));
        unit3.damage=220;
        unit3.range=100;
        Const.game.allUnits.add(unit3);

        try
        {
            doHeroCommandAndRun(players.get(0), "SHIELD " + hero0.id, 5);
        }catch (Exception e){
            return false;
        }
        int shield = (int)(hero0.maxMana*0.5+50);

        return assertValue(100-unit3.damage+shield, hero0.health)
                && assertValue(0, hero0.shield);
    }

    public boolean cleric_spell0_Test(){
        Hero hero0 = createAndReplaceHero("DOCTOR_STRANGE", 40, 40, 100, 500, 100);

        LaneUnit unit3 = new LaneUnit(55,56,10,0,100, new Point(60,60), players.get(0));
        unit3.health=50;
        unit3.maxHealth = 200;
        Const.game.allUnits.add(unit3);

        LaneUnit unit4 = new LaneUnit(900,900,10,0,100, new Point(60,60), players.get(0));
        unit4.health = 50;
        unit4.maxHealth = 200;
        Const.game.allUnits.add(unit4);

        try
        {
            doHeroCommandAndRun(players.get(0), "AOEHEAL " + 50 + " " + 55, 5);
        }catch (Exception e){
            return false;
        }
        return assertDouble((int)(100+(hero0.mana+50)*0.2), hero0.health)
                && assertDouble((int)(50+(hero0.mana+50)*0.2), unit3.health)
                && assertValue(50, unit4.health);
    }

    public boolean cleric_spell0_NoAboveMaxHealth_Test(){
        Hero hero0 = createAndReplaceHero("DOCTOR_STRANGE", 40, 40, 100, 142, 100);

        LaneUnit unit3 = new LaneUnit(55,56,10,0,100, new Point(60,60), players.get(0));
        unit3.health=50;
        unit3.maxHealth = 200;
        Const.game.allUnits.add(unit3);

        LaneUnit unit4 = new LaneUnit(900,900,10,0,100, new Point(60,60), players.get(0));
        unit4.health = 50;
        unit4.maxHealth = 200;
        Const.game.allUnits.add(unit4);

        try
        {
            doHeroCommandAndRun(players.get(0), "AOEHEAL " + 50 + " " + 55, 5);
        }catch (Exception e){
            return false;
        }
        return assertValue(142, hero0.health)
                && assertDouble((int)(50+(hero0.mana+50)*0.2), unit3.health)
                && assertValue(50, unit4.health);
    }


    public boolean heroDiesInBush_cantseeEnemy_Test(){
        Hero hero0 = createAndReplaceHero("DEADPOOL", 60, 60, 100, 142, 100);
        Hero hero1 = players.get(1).heroes.get(0);
        hero1.x = 60;
        hero1.y = 60;
        hero1.health=10;
        players.get(1).heroes.add(Factories.generateHero("IRONMAN", players.get(1), new Point(500,500)));

        Const.game.bushes.add(new Bush(60, 60));
        Const.game.allUnits.add(hero1);

        try {
            doHeroCommandAndRun(players.get(0), "ATTACK " +hero1.id, 5);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        boolean result = assertValue(true, hero0.visible);
        runRound(5);
        return result && assertValue(false, hero0.visible);
    }

    public boolean ninja_spell2_InBush_Test(){
        Hero hero0 = createAndReplaceHero("DEADPOOL", 40, 40, 100, 142, 100);
        Hero hero1 = players.get(1).heroes.get(0);
        hero1.x = 60;
        hero1.y = 60;

        Const.game.bushes.add(new Bush(60, 60));
        Const.game.allUnits.add(hero1);

        try {
            doHeroCommandAndRun(players.get(0), "STEALTH 60 61", 5);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        boolean result = assertValue(false, hero0.visible)
                && assertValue(true, hero1.visible) && assertDouble(60, hero0.x)
                && assertDouble(61, hero0.y);

        try {
            doHeroCommandAndRun(players.get(0), "MOVE 300 300", 5);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        return result && assertValue(false, hero0.visible)
                && assertValue(false, hero1.visible);
    }


    public boolean ninja_spell2_Test(){
        Hero hero0 = createAndReplaceHero("DEADPOOL", 40, 40, 100, 142, 100);
        int heroHealth = hero0.health;

        LaneUnit unit2 = createPeasant(5, 1);
        unit2.x = 150;

        try {
            doHeroCommandAndRun(players.get(0), "STEALTH 60 61", 5);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        boolean result = assertValue(false, hero0.visible)
                && assertValue(heroHealth-unit2.damage, hero0.health)
                && assertDouble(60, hero0.x)
                && assertDouble(61, hero0.y);

        runRound(5);
        runRound(5);
        try {
            doHeroCommandAndRun(players.get(0), "ATTACK " + unit2.id, 5);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }
        result = result && assertValue(true, hero0.visible);
        runRound(5);

        return result && assertValue(true, hero0.visible) && assertValue(heroHealth-unit2.damage*2, hero0.health);
    }


    public boolean ninja_spell1_TeleportIntoWire_Test(){
        Hero hero0 = createAndReplaceHero("DEADPOOL", 200, 200, 100, 142, 100);

        Hero hero1 = players.get(1).heroes.get(0);
        Const.game.allUnits.add(hero1);
        hero1.x = 400;
        hero1.y = 400;
        int heroHealth = hero1.health;

        try {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"WIRE " + 400 + " " + 200});
            players.get(1).handlePlayerOutputs(new String[]{"BLINK 400 200"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        return assertValue(1, hero1.stunTime) && assertDouble(heroHealth-hero1.maxMana*0.5, hero1.health)
                && assertDouble(400, hero1.x) && assertDouble(200, hero1.y);
    }


    public boolean ninja_spell1_InstantStop_Test(){
        Hero hero0 = createAndReplaceHero("DEADPOOL", 195, 195, 100, 142, 100);

        Hero hero1 = players.get(1).heroes.get(0);
        Const.game.allUnits.add(hero1);
        hero1.x = 200;
        hero1.y = 200;
        int heroHealth = hero1.health;

        try {
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"WIRE " + (int)hero1.x + " " + (int)hero1.y});
            players.get(1).handlePlayerOutputs(new String[]{"MOVE 300 200"});
            Const.game.handleTurn(players);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        return assertValue(1, hero1.stunTime) && assertDouble(heroHealth-hero1.maxMana*0.5, hero1.health)
                && assertDouble(200, hero1.x) && assertDouble(200, hero1.y);
    }

    public boolean ninja_spell1_Test(){
        Hero hero0 = createAndReplaceHero("DEADPOOL", 40, 40, 100, 142, 100);

        Hero hero1 = players.get(1).heroes.get(0);
        Const.game.allUnits.add(hero1);
        hero1.x = 200;
        hero1.y = 200;
        int heroHealth = hero1.health;

        try {
            doHeroCommandAndRun(players.get(0), "WIRE " + (int)hero1.x + " " + (int)hero1.y, 5);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        return assertValue(1, hero1.stunTime) && assertDouble(heroHealth-hero1.maxMana*0.5, hero1.health);
    }


    public boolean ninja_spell0_diesNoDamage_Test(){
        Hero hero0 = createAndReplaceHero("DEADPOOL", 40, 40, 100, 142, 100);
        int hero0Health = hero0.health;

        LaneUnit unit = createPeasant(3, 1);
        LaneUnit unit2 = createPeasant(500, 1);
        unit2.x = 150;
        int unit0Health = unit.health;

        try {
            doHeroCommandAndRun(players.get(0), "COUNTER", 5);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        return assertValue(true, hero0.health <= 0) && assertValue(unit0Health, unit.health);
    }

    public boolean ninja_spell0_Test(){
        Hero hero0 = createAndReplaceHero("DEADPOOL", 40, 40, 100, 142, 100);
        int hero0Health = hero0.health;

        LaneUnit unit = createPeasant(3, 1);
        LaneUnit unit2 = createPeasant(5, 1);
        unit2.x = 150;
        int unit0Health = unit.health;

        try {
            doHeroCommandAndRun(players.get(0), "COUNTER", 5);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        return assertDouble(hero0Health, hero0.health) && assertDouble(unit0Health-(unit.damage+unit2.damage)*1.5, unit.health);
    }

    public boolean attackNearest_Test(){
        Hero hero = players.get(0).heroes.get(0);
        Const.game.allUnits.add(hero);
        hero.x = 150;
        hero.y = 205;
        int health = hero.health;

        LaneUnit unit = new LaneUnit(55,55,10,1,100, new Point(55,55), players.get(1));
        unit.goldValue = 3;
        unit.damage = 5;
        unit.range=300;
        Const.game.allUnits.add(unit);

        LaneUnit unit2 = new LaneUnit(60,60,10,1,100, new Point(60,55), players.get(1));
        unit2.goldValue = 5;
        unit2.damage = 8;
        unit2.range=300;
        Const.game.allUnits.add(unit2);

        try{
            doHeroCommandAndRun(players.get(0), "ATTACK_NEAREST UNIT", 5);
        }catch (Exception e){
            return false;
        }

        return assertValue(5, players.get(0).gold)
                && assertValue(health-13, hero.health)
                && assertValue(true, unit2.isDead);
    }

    public boolean moveCommand_infinity_noMovement_Test(){
        Hero hero = players.get(0).heroes.get(0);
        Const.game.allUnits.add(hero);
        hero.x = 150;
        hero.y = 205;

        try{
            doHeroCommandAndRun(players.get(0), "MOVE Infinity 49", 5);
        }catch (Exception e){
            return false;
        }

        return assertDouble(150, hero.x)
                && assertDouble(205, hero.y);
    }


    public boolean moveCommand_Test(){
        Hero hero = players.get(0).heroes.get(0);
        Const.game.allUnits.add(hero);
        hero.x = 150;
        hero.y = 205;

        try{
            doHeroCommandAndRun(players.get(0), "MOVE 50 49", 5);
        }catch (Exception e){
            return false;
        }

        boolean result = assertDouble(50, hero.x) && assertDouble(49, hero.y);

        try{
            doHeroCommandAndRun(players.get(0), "MOVE -100000 49", 5);
        }catch (Exception e){
            return false;
        }

        return result && assertDouble(30, hero.x);
    }

    public boolean heroDeath_killedByCreep_HalfValue_Test(){
        Const.game.allUnits.add(players.get(0).heroes.get(0));

        LaneUnit unit = new LaneUnit(55,55,10,1,200, new Point(55,55), players.get(1));
        unit.goldValue = 3;
        unit.damage = 100000;
        unit.range=70;
        Const.game.allUnits.add(unit);

        runRound(5);

        return assertValue(true, Const.game.isGameOver(players))
                && assertValue(Const.HERO_GOLD_VALUE/2, players.get(1).gold);
    }

    public boolean farming_UnitDies_OnlyFromOneUnit_Test(){
        Hero hero0 = Factories.generateHero("DEADPOOL", players.get(0), new Point(40,40));
        players.get(0).addHero(hero0);
        Const.game.allUnits.add(hero0);
        Const.game.allUnits.add(players.get(0).heroes.get(0));

        LaneUnit unit = new LaneUnit(55,55,10,1,100, new Point(55,55), players.get(1));
        unit.goldValue = 3;
        unit.damage = 10;
        unit.range=70;
        Const.game.allUnits.add(unit);

        LaneUnit unit2 = new LaneUnit(60,55,10,1,100, new Point(60,55), players.get(1));
        unit2.goldValue = 5;
        unit2.damage = 10;
        unit2.range=70;
        Const.game.allUnits.add(unit2);

        LaneUnit unit3 = new LaneUnit(55,56,10,0,100, new Point(55,56), players.get(0));
        unit3.goldValue = 20;
        unit3.damage = 10;
        unit3.range=70;
        Const.game.allUnits.add(unit3);

        LaneUnit unit4 = new LaneUnit(60,56,10,0,100, new Point(60,56), players.get(0));
        unit4.goldValue = 50;
        unit4.range=70;
        unit4.damage = 10;
        Const.game.allUnits.add(unit4);

        Const.game.beforeTurn(5, players);

        try{
            String[] outputs = new String[]{
              "ATTACK " + unit.id,
              "ATTACK " + unit2.id
            };
            players.get(0).handlePlayerOutputs(outputs);
        }catch (Exception e){
            return false;
        }
        Const.game.handleTurn(players);

        return assertValue(1, players.get(0).unitKills)
                && assertValue(5, players.get(0).gold)
                && assertValue(2, Const.game.allUnits.size())
                && assertValue(0, players.get(0).denies);
    }

    public boolean allWait_draw_Test(){
        Const.REMOVEFORESTCREATURES = true;
        moveHero(Const.HEROSPAWNTEAM0, players.get(0).heroes.get(0));
        moveHero(Const.HEROSPAWNTEAM1, players.get(1).heroes.get(0));
        Const.game.allUnits.add(players.get(0).tower);
        Const.game.allUnits.add(players.get(1).tower);
        Const.game.allUnits.add(players.get(0).heroes.get(0));
        Const.game.allUnits.add(players.get(1).heroes.get(0));

        for(int i = 0; i < 300; i++){
            runRound(i);

            if(!assertValue(0, totalHealth(0) - totalHealth(1))) {
                System.err.println("---------- allWait_draw_Test - round: " + i);
                return false;
            }
        }

        Const.REMOVEFORESTCREATURES = false;
        return assertValue(0, totalHealth(0) - totalHealth(1));
    }

    public boolean groupOfUnits_allAttacked_Test(){
        String[] definitions = new String[]{
                "UNIT 1 250 {941.0,540.0} 12 ranged",
                "UNIT 0 215 {641.0,553.0} 17 melee",
                "UNIT 0 150 {647.0,580.0} 18 melee",
                "UNIT 0 250 {499.0,590.0} 20 ranged",
                "UNIT 1 155 {711.0,551.0} 21 melee",
                "UNIT 1 400 {715.0,600.0} 22 melee",
                "UNIT 1 365 {712.0,608.0} 23 melee",
                "UNIT 1 250 {946.0,595.0} 24 ranged"
        };
        int totalHealth = 0;
        int totalDamage = 0;
        for(String def : definitions){
            String[] params = def.split(" ");
            Unit unit = createRealUnit(Integer.parseInt(params[1]), params[5].equals("ranged")?1:0, players.get(Integer.parseInt(params[1])), createPointOfPositionStr(params[3]), Integer.parseInt(params[2]));
            totalHealth+=unit.health;
            totalDamage+=unit.damage;
        }

        runRound(5);

        for (Unit unit : Const.game.allUnits){
            if(unit instanceof LaneUnit){
                totalHealth -= unit.health;
            }
        }

        return assertValue(totalDamage, totalHealth);
    }

    public boolean unitAggro_Test(){
        Hero hero0 = Factories.generateHero("VALKYRIE", players.get(0), new Point(40,40));
        hero0.damage = 100;
        players.get(0).heroes.clear();
        players.get(0).addHero(hero0);
        Const.game.allUnits.add(hero0);

        Hero hero1 = Factories.generateHero("VALKYRIE", players.get(1), new Point(42,42));
        hero1.team = 1;
        hero1.damage = 50;
        players.get(1).heroes.clear();
        players.get(1).addHero(hero1);
        Const.game.allUnits.add(hero1);

        int hero0health = hero0.health;

        LaneUnit unit = createPeasant(3, 0);
        int unit0Health = unit.health;

        LaneUnit unit2 = createPeasant(2, 1);

        runRound(5);

        try {
            doHeroCommandAndRun(players.get(0), "ATTACK " + hero1.id, 5);
        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }

        return assertValue(hero0health-unit2.damage, hero0.health) && assertValue(unit0Health-unit2.damage, unit.health);
    }


    public boolean denyingHealthyCreature_NotAllowed_Test(){
        Hero hero0 = createAndReplaceHero("VALKYRIE", 150, 150, 500, 500, 200);
        hero0.damage = 50;
        Const.game.allUnits.add(hero0);

        LaneUnit unit = new LaneUnit(200,200,150,0,50, new Point(0,0), players.get(1));
        unit.maxHealth = 150;
        unit.goldValue = 1337;
        Const.game.allUnits.add(unit);

        try{
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"ATTACK " + unit.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            System.err.println(e);
            return false;
        }
        boolean result = assertValue(0, players.get(0).denies) && assertValue(150, unit.health);
        unit.health = 50;
        try{
            Const.game.beforeTurn(5, players);
            players.get(0).handlePlayerOutputs(new String[]{"ATTACK " + unit.id});
            Const.game.handleTurn(players);
        }catch (Exception e){
            System.err.println(e);
            return false;
        }

        return result && assertValue(1, players.get(0).denies) && assertValue(true, unit.isDead);
    }

    public boolean concurrentHits_highestDamageGetsDeny_Test(){
        Hero hero0 = Factories.generateHero("VALKYRIE", players.get(0), new Point(0,0));
        hero0.damage = 50;
        Hero hero1 = Factories.generateHero("VALKYRIE", players.get(1), new Point(0,0));
        hero1.team = 1;
        hero1.damage = 100;
        players.get(0).heroes.clear();
        players.get(0).addHero(hero0);
        players.get(1).heroes.clear();
        players.get(1).addHero(hero1);
        Const.game.allUnits.add(hero0);
        Const.game.allUnits.add(hero1);

        LaneUnit unit = new LaneUnit(10,10,150,1,100, new Point(0,0), players.get(1));
        unit.maxHealth = 500;
        unit.team = 1;
        unit.goldValue = 1337;
        Const.game.allUnits.add(unit);

        Const.game.beforeTurn(5, players);
        try{
            players.get(0).handlePlayerOutputs(new String[]{"ATTACK " + unit.id});
            players.get(1).handlePlayerOutputs(new String[]{"ATTACK " + unit.id});
        }catch (Exception e){
            System.err.println(e);
            return false;
        }

        Const.game.handleTurn(players);

        return assertValue(0, players.get(0).unitKills)
                && assertValue(1, players.get(1).denies)
                && assertValue(0, players.get(0).gold);
    }

    public boolean concurrentHits_highestDamageGetsKill_Test(){
        Hero hero0 = Factories.generateHero("VALKYRIE", players.get(0), new Point(0,0));
        hero0.damage = 100;
        Hero hero1 = Factories.generateHero("VALKYRIE", players.get(1), new Point(0,0));
        hero1.team = 1;
        hero1.damage = 50;
        players.get(0).heroes.clear();
        players.get(0).addHero(hero0);
        players.get(1).heroes.clear();
        players.get(1).addHero(hero1);
        Const.game.allUnits.add(hero0);
        Const.game.allUnits.add(hero1);

        LaneUnit unit = new LaneUnit(10,10,150,1,100, new Point(0,0), players.get(1));
        unit.maxHealth = 500;
        unit.team = 1;
        unit.goldValue = 1337;
        Const.game.allUnits.add(unit);

        Const.game.beforeTurn(5, players);
        try{
            players.get(0).handlePlayerOutputs(new String[]{"ATTACK " + unit.id});
            players.get(1).handlePlayerOutputs(new String[]{"ATTACK " + unit.id});
        }catch (Exception e){
            System.err.println(e);
            return false;
        }

        Const.game.handleTurn(players);

        return assertValue(1, players.get(0).unitKills)
                && assertValue(0, players.get(1).denies)
                && assertValue(1337, players.get(0).gold);
    }


    public boolean playercommand_moveAttack_Test(){
        Hero hero = players.get(0).heroes.get(0);
        Hero otherHero = players.get(1).heroes.get(0);
        Const.game.allUnits.add(hero);
        Const.game.allUnits.add(otherHero);
        double health = otherHero.health;

        Player player = players.get(0);
        try{
            doHeroCommandAndRun(player, "MOVE_ATTACK 35 30 " + otherHero.id, 5);

        }catch (Exception e){
            System.err.println(e.getStackTrace());
            return false;
        }


        return assertDouble(35, hero.x) && assertDouble(30, hero.y) && assertDouble(health-hero.damage, otherHero.health);
    }

    public boolean item_sellAndBuy_sameRound_Test(){
        String itemName = "testitem";
        players.get(0).heroes.add(Factories.generateHero("IRONMAN", players.get(0), new Point(0,0)));
        players.get(0).setGold(100);

        Const.game.items = new HashMap<>();
        Const.game.items.put(itemName, new Item(itemName, new HashMap<>(), 50, "none", false));

        String[] inputs = {"BUY " +itemName, "WAIT"};
        try{
            players.get(0).handlePlayerOutputs(inputs);

        }catch (Exception e){
            System.err.println(e.getMessage());
            return false;
        }

        boolean result = assertValue(50, players.get(0).getGold());

        players.get(0).setGold(25);
        inputs = new String[]{"SELL " + itemName, "BUY " +itemName};
        try{
            players.get(0).handlePlayerOutputs(inputs);

        }catch (Exception e){
            System.err.println(e.getMessage());
            return false;
        }

        result = result && assertValue(0, players.get(0).getGold());

        players.get(0).setGold(25);
        inputs = new String[]{ "BUY " +itemName, "SELL " + itemName};
        try{
            players.get(0).handlePlayerOutputs(inputs);

        }catch (Exception e){
            System.err.println(e.getMessage());
            return false;
        }

        return result && assertValue(0, players.get(0).getGold());
    }

    public boolean visibility_EnemyHeroEntersBush_Test(){
        boolean result = visibility_UnitInvisBeforeAttack_NotAfter_Test();
        players.get(1).heroes.get(0).x=0;
        players.get(1).heroes.get(0).y=0;

        runRound(7);

        return result && assertValue(true, players.get(0).heroes.get(0).visible);
    }

    public boolean visibility_UnitInvisBeforeAttack_NotAfter_Test(){
        players.get(1).heroes.get(0).x=1000;
        players.get(1).heroes.get(0).y=1000;

        Hero hero = players.get(0).heroes.get(0);
        Const.game.allUnits.add(hero);
        Const.game.bushes.add(Factories.generateBush(new Point(0,0)));

        int health = hero.health;

        runRound(5);

        boolean result = assertValue(false, hero.visible);

        LaneUnit unit = new LaneUnit(50,50,10000,1,100, new Point(0,0), players.get(1));
        unit.range = 10;
        unit.damage = 10;
        Const.game.allUnits.add(unit);

        Const.game.beforeTurn(5, players);
        String[] inputs = {"ATTACK " + unit.id};
        try{
            players.get(0).handlePlayerOutputs(inputs);
        }
        catch (Exception e){  return false; }

        Const.game.handleTurn(players);

        result = result && assertValue(true, hero.visible);

        runRound(5);

        result = result && assertValue(false, hero.visible)
                && assertValue(health-unit.damage, hero.health);

        runRound(5);
        runRound(5);

        return result && assertValue(false, hero.visible)
                && assertValue(health-unit.damage, hero.health);

    }

    public boolean attackMoveTest(){
        removeHeroes();
        LaneUnit team0Unit = new LaneUnit(0,0,1,0,200, new Point(400,0), new Player());
        team0Unit.range = 200;
        team0Unit.damage = 0;
        LaneUnit team1Unit = new LaneUnit(400,0,1,1,200, new Point(0,0), new Player());
        team1Unit.range = 200;
        team1Unit.damage = 0;

        Const.game.allUnits.add(team0Unit);
        Const.game.allUnits.add(team1Unit);

        runRound(5);

        return assertDouble(100, team0Unit.x)
                && assertDouble(300, team1Unit.x);
    }

    public boolean collision_none_Test(){
        LaneUnit unit1 = createUnitSimple(0,0);
        LaneUnit unit2 = createUnitSimple(200, 200);
        return assertDouble(-1, Utilities.getCollisionTime(unit1,unit2, 100));
    }

    public boolean collision_instant_Test(){
        LaneUnit unit1 = createUnitSimple(0,0);
        LaneUnit unit2 = createUnitSimple(200, 200);
        return assertDouble(0.0, Utilities.getCollisionTime(unit1,unit2, 283));
    }


    public boolean viewController_VerifySomeoneChangedItWithoutTesting_Test(){
        Class c = ViewController.class;
        Method[] m = c.getDeclaredMethods();
        Class c2 = ViewControllerTest.class;
        Method[] m2 = c2.getDeclaredMethods();
        boolean totalcoverage = true;
        for(Method method : m){
            if(!Modifier.isPublic(method.getModifiers())) continue;
            boolean found = false;
            for(Method method2 : m2){
                if(method.getName().equals(method2.getName())) found=true;
            }
            if(!found){
                System.err.println("---------------------------- MISSING METHOD: " + method.getName());
                totalcoverage = false;
            }
        }

        return totalcoverage;
    }

    // TEST END

    private Hero createAndReplaceHero(String heroName, int x, int y, int health, int maxHealth, int damage){
        Hero hero0 = Factories.generateHero(heroName, players.get(0), new Point(x, y));
        hero0.health = health;
        hero0.maxHealth = maxHealth;
        hero0.damage = damage;
        players.get(0).heroes.clear();
        players.get(0).addHero(hero0);
        Const.game.allUnits.add(hero0);
        return hero0;
    }
    private void moveHero(Point point, Hero hero){
        hero.x = point.x;
        hero.y = point.y;
    }

    private int totalHealth(int team){
        int health = 0;
        for(Unit unit : Const.game.allUnits){
            if(unit.team==team) health+= unit.health;
        }

        return health;
    }

    private Point createPointOfPositionStr(String str){
        //{641.0,553.0}
        String[] points = str.substring(1, str.length()-1).split(",");
        return new Point(Double.parseDouble(points[0]), Double.parseDouble(points[1]));
    }

    private LaneUnit createRealUnit(int team, int type, Player player, Point point, int health){
        LaneUnit unit = Factories.generateUnit(type, team, team, player);
        unit.x = point.x;
        unit.y = point.y;
        unit.health = health;
        Const.game.allUnits.add(unit);
        return unit;
    }

    private LaneUnit createPeasant(int damage, int team){
        LaneUnit unit = new LaneUnit(33,33,150,team,100, new Point(0,0), players.get(team));
        unit.range = 70;
        unit.damage = damage;
        Const.game.allUnits.add(unit);
        return unit;
    }

    private void doHeroCommandAndRun(Player player, String command, int round) throws Exception {
        Const.game.beforeTurn(round, players);
        player.handlePlayerOutputs(new String[]{command});
        Const.game.handleTurn(players);
    }

    private void runRound(int round){
        Const.game.beforeTurn(round, players);
        Const.game.handleTurn(players);
    }

    private LaneUnit createUnitSimple(int x, int y){
        return new LaneUnit(x,y,1,0,0, null, null);
    }

    private static < T > boolean assertValue(T expected, T actual){
        if((expected==null) != (actual==null))
            return throwError("Values not equal - \nexpected: "  +expected + "\nactual:   " + actual);
        if(!expected.equals(actual))
            return throwError("Values not equal - \nexpected: "  +expected + "\nactual:   " + actual);
        return true;
    }

    private static boolean assertDouble(double expected, double actual)
    {
        if(Math.abs(expected-actual) > 0.00001)
            return throwError("Values not equal: \nexpected: "  +expected + "\nactual:   " + actual);
        return true;
    }

    private static boolean throwError(String message){
        System.err.println("----------");
        System.err.println(message);
        System.err.println("----------");
        return false;
    }



    public static class ViewControllerTest extends com.codingame.game.ViewController {


        public ViewControllerTest() {
            super();
        }

        @Override
        public void addEffect(Unit unit, Point target, String effect, double duration){

        }

        @Override
        public void addSummary(String summary){

        }

        @Override
        public void setRotation(Unit unit, Point target, double t){

        }

        @Override
        public void removeEntityTooltip(Entity<?> entity){

        }

        @Override
        public void initRound(int round){

        }
        @Override
        public void addObstacle(Bush obstacle){

        }
        @Override
        public void addSprite(Unit unit, int team) {       }

        @Override
        public void updateViewForUnit(Unit unit, double t){

        }
        @Override
        public void removeItem(Hero hero, Item item){

        }
        @Override
        public void addItem(Hero hero, Item item){

        }
        @Override
        public void afterRound(){

        }
        @Override
        public void updateView(double t) {

        }
        @Override
        public void displayDamages(Unit unit, int damage, double t) {

        }
        @Override
        public void killUnit(Unit unit, UnitKilledState unitKilledState) {

        }

        @Override
        public void initialize(GraphicEntityModule entityManager, TooltipModule tooltipModule, List<Player> players, GameManager<Player> player) {

        }
        @Override
        public void addViewerMessage(Player player, String message){
        }

        @Override
        public void addMessageToHeroHud(Hero hero, String message) {

        }

        @Override
        public void updateEntityTooltip(Entity<?> entity, String... string) {

        }
    }
}
