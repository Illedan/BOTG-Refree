package com.codingame.game;

import java.util.HashMap;
import java.util.Map;

public class Factories {


    enum HeroType {
        IRONMAN,
        DEADPOOL,
        DOCTOR_STRANGE,
        VALKYRIE,
        HULK
    }

    public static LaneUnit generateUnit(int type, int team, int number, Player player) {
        Point spawn = team == 0 ? Const.SPAWNTEAM0 : Const.SPAWNTEAM1;
        Point target = team == 1 ? Const.TOWERTEAM0 : Const.TOWERTEAM1;
        LaneUnit unit = new LaneUnit(spawn.x  + 50 * type *(team*2-1), spawn.y + number * 50, 1, team, 150, target, player);
        unit.targetPoint = new Point(unit.targetPoint.x, unit.y); // Move in a straight line
        //Need more data?
        unit.skin = "racoon-";
        if (team == 1) {
            unit.skin += "blue";
        } else {
            unit.skin += "red";
        }

        if (type == 0) {
            unit.health = unit.maxHealth = 400;
            unit.damage = 25;
            unit.range = 90;
            unit.goldValue = Const.MELEE_UNIT_GOLD_VALUE;
            unit.skin += ".png";
        } else {
            unit.health = unit.maxHealth = 250;
            unit.damage = 35;
            unit.range = 300;
            unit.goldValue = Const.RANGER_UNIT_GOLD_VALUE;
            unit.skin += ".png";
        }
        unit.attackTime = 0.2;

        Const.viewController.addSprite(unit, team);
        return unit;
    }

    public static Hero generateHero(String type, Player player, Point spawn) throws IllegalArgumentException {
        int team = player.getIndex();
        Hero hero = new Hero(spawn.x, spawn.y, 1, team, 200, player, type);
        hero.skills[0] = new Skills.EmptySkill();
        hero.skills[1] = new Skills.EmptySkill();
        hero.skills[2] = new Skills.EmptySkill();

        // Since stub doesn't support IFs, just take heroes
        if(type.startsWith("WAIT")){
            if(Const.game.round==0) type = HeroType.IRONMAN.name();
            else if(Const.game.round==1) type = HeroType.HULK.name();
            hero.heroType = type;
        }

        if (type.equals(HeroType.IRONMAN.name())) {
            hero.health =  hero.maxHealth = 820;
            hero.mana = hero.maxMana = 200;
            hero.damage = 60;
            hero.skin = Const.IRONMAN;
            hero.range = 270;
            hero.manaregeneration = 2;
            hero.skills[0] = new Skills.BlinkSkill(hero, 16, "BLINK", 0.05, true, 200, 3);
            hero.skills[1] = new Skills.LineSkill(hero, 60, "FIREBALL", 900, 50,  0.9, 6);
            hero.skills[2] = new Skills.BurningGround(hero, 50, "BURNING", 0.01, 250, 100, "ENERGY-BALL.png", 5);

        } else if (type.equals(HeroType.VALKYRIE.name())) {
            hero.health = hero.maxHealth = 1400;
            hero.mana = hero.maxMana = 155;
            hero.damage = 65;
            hero.skin = Const.VALKYRIE;
            hero.range = 130;
            hero.manaregeneration = 2;
            hero.skills[0] = new Skills.FlipSkill(hero, 20, "SPEARFLIP", 0.1, 155, 3);
            hero.skills[1] = new Skills.JumpSkill(hero, 35, "JUMP", 0.15, 250, 3);
            hero.skills[2] = new Skills.PowerUpSkill(hero, 50, "POWERUP", 4, 0, 7);

        } else if (type.equals(HeroType.DEADPOOL.name())) {
            hero.health =  hero.maxHealth = 1380;
            hero.mana = hero.maxMana = 100;
            hero.damage = 80;
            hero.skin = Const.DEADPOOL;
            hero.range = 110;
            hero.manaregeneration = 1;
            hero.skills[0] = new Skills.CounterSkill(hero, 40, "COUNTER", 1, 350, 5);
            hero.skills[1] = new Skills.WireHookSkill(hero, 50, "WIRE", 200, 25, 2, 0.3, 9);
            hero.skills[2] = new Skills.StealthSkill(hero, 30, "STEALTH", 0, 5, 6);

        } else if (type.equals(HeroType.DOCTOR_STRANGE.name())) {
            hero.health =  hero.maxHealth = 955;
            hero.mana = hero.maxMana = 300;
            hero.damage = 50;
            hero.skin = Const.DOCTOR_STRANGE;
            hero.range = 245;
            hero.manaregeneration = 2;
            hero.skills[0] = new Skills.AOEHealSkill(hero, 50, "AOEHEAL", 0.01, 250, 100, "HEALING-BALL.png", 6);
            hero.skills[1] = new Skills.ShieldSkill(hero, 40, "SHIELD", 3, 500, 6);
            hero.skills[2] = new Skills.PullSkill(hero, 40, "PULL", 0.3, 400, 5, 0.1);

        } else if (type.equals(HeroType.HULK.name())) {
            hero.health =  hero.maxHealth = 1450;
            hero.mana = hero.maxMana = 90;
            hero.damage = 80;
            hero.skin = Const.HULK;
            hero.range = 95;
            hero.manaregeneration = 1;
            hero.skills[0] = new Skills.ChargeSkill(hero, 20, "CHARGE", 0.05, 300, 4);
            hero.skills[1] = new Skills.ExplosiveSkill(hero, 30, "EXPLOSIVESHIELD", 4, 100, 8);
            hero.skills[2] = new Skills.BashSkill(hero, 40, "BASH", 2, 150, 10);

        } else {
            throw new IllegalArgumentException("Hero not supported");
        }

        if(Const.IGNORESKILLS){
            hero.skills[0] = new Skills.EmptySkill();
            hero.skills[1] = new Skills.EmptySkill();
            hero.skills[2] = new Skills.EmptySkill();
        }

        Const.viewController.addSprite(hero, team);
        return hero;
    }

    public static Tower generateTower(Player player, int team) {
        Point spawn = team == 0 ? Const.TOWERTEAM0 : Const.TOWERTEAM1;
        Tower tower = new Tower(spawn.x, spawn.y, (int)(Const.TOWERHEALTH*Const.TOWERHEALTHSCALE), team, player);
        tower.skin = team == 1 ? Const.BLUETOWER : Const.REDTOWER;
        tower.range = 400;
        tower.damage = 100;
        player.tower = tower;
        tower.attackTime = 0.2;

        Const.viewController.addSprite(tower, team);
        return tower;
    }

    public static Bush generateBush(Point point) {
        Bush bush = new Bush(point.x, point.y);
        bush.skin = Const.BUSH;

        Const.viewController.addObstacle(bush);
        return bush;
    }

    public static Creature generateCreature(Point point, double amplitude) {
        Creature creature = new Creature(point.x, point.y);
        creature.skin = Const.GROOT;
        creature.health = creature.maxHealth = (int)(400*amplitude);
        creature.damage = (int)(35*amplitude);
        creature.range = 150;
        creature.moveSpeed = 250;
        creature.goldValue = (int)(Const.NEUTRALGOLD*amplitude);
        creature.creatureType = "GROOT";
        Const.viewController.addSprite(creature, -1);
        creature.attackTime = 0.2;

        return creature;
    }

    public static Map<String, Item> createItems(int playersGold){
        if(Const.IGNOREITEMS) return new HashMap<>();
        return Const.mapFactory.generateItems(playersGold);
    }
}
