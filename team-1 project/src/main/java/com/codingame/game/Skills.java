package com.codingame.game;

import sun.font.CreatedFontTracker;

import java.util.ArrayList;



public class Skills {

    //Skill is something the hero uses
    public static abstract class SkillBase {
        public final Hero hero;
        public final int manaCost;
        public final String skillName;
        public final int range;
        public int cooldown;
        public int initialCooldown;
        public double duration = 1;

        SkillBase(Hero hero, int manaCost, String skillName, int range, int cooldown) {
            this.hero = hero;
            this.manaCost = manaCost;
            this.skillName = skillName;
            this.range = range;
            this.initialCooldown = cooldown;
            this.cooldown = 0;
        }

        public abstract double CastTime();
        abstract void doSkill(Game game, double x, double y, int unitId);

        public int getDuration(){return (duration<1?1:(int)(Math.round(duration))); }
        public abstract String getTargetTeam();
        public abstract SkillType getTargetType();

    }

    static class EmptySkill extends SkillBase{
        EmptySkill() { super(null, 100000, "NONE", 0, Const.Rounds+1); }
        @Override
        void doSkill(Game game, double x, double y, int unitId) { }

        @Override
        public String getTargetTeam() {
            return "NONE";
        }

        @Override
        public SkillType getTargetType() { return SkillType.SELF; }
        @Override
        public double CastTime(){return 0.0;}
    }

    static class BlinkSkill extends SkillBase{
        boolean instant;
        BlinkSkill(Hero hero, int manaCost, String skillName, double duration, boolean instant, int range, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
            this.instant = instant;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Point target = new Point(x,y);
            if(target.distance(hero) > range){
                double distance = target.distance(hero);
                target.x = hero.x + ((target.x-hero.x) / distance * range);
                target.y = hero.y + ((target.y-hero.y) / distance * range);
            }

            hero.mana = Math.min(hero.mana+20, hero.maxMana);

            game.events.add(new Event.BlinkEvent(hero, duration, Utilities.round(target.x), Utilities.round(target.y)));
            Const.viewController.addEffect(hero, target, "blink", duration);
        }

        @Override
        public double CastTime(){return duration;}

        @Override
        public String getTargetTeam() {
            return "NONE";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.POSITION;
        }
    }

    // Lancer skills
    static class JumpSkill extends SkillBase{
        JumpSkill(Hero hero, int manaCost, String skillName, double duration, int range, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Point target = new Point(x,y);
            double distance = target.distance(hero);
            if(distance > range){
                target.x = hero.x + ((target.x-hero.x) / distance * range);
                target.y = hero.y + ((target.y-hero.y) / distance * range);
            }

            game.events.add(new Event.BlinkEvent(hero, duration, target.x, target.y));
            game.events.add(new Event.AttackNearestDelayed(hero, duration+Const.EPSILON));
           // Const.viewController.addEffect(hero, target, "jump", duration);
        }

        @Override
        public double CastTime(){return duration;}

        @Override
        public String getTargetTeam() {
            return "ENEMY";
        }

        @Override
        public SkillType getTargetType() {
            return  SkillType.POSITION;
        }
    }

    static class FlipSkill extends SkillBase{
        FlipSkill(Hero hero, int manaCost, String skillName, double duration, int range, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Unit target = Const.game.getUnitOfId(unitId);
            if(target.distance(hero) <= range && !(target instanceof Tower)){
                game.events.add(new Event.StunEvent(target, 0, 1));
                if(target.team != hero.team)
                    game.events.add(new Event.DamageEvent(target, hero, duration, (int)(hero.damage*0.4)));

                double vx = (hero.x-target.x)*2/duration;
                double vy = (hero.y-target.y)*2/duration;
                game.events.add(new Event.SpeedChangedForceEvent(target, Const.EPSILON, vx, vy));
                game.events.add(new Event.SpeedChangedForceEvent(target, duration, vx*-1, vy*-1));
            } else Const.viewController.addSummary("Can't flip target outside range.");
        }
        @Override
        public double CastTime(){return 0.0;}

        @Override
        public String getTargetTeam() {
            return "BOTH";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.UNIT;
        }
    }

    static class PowerUpSkill extends SkillBase{
        PowerUpSkill(Hero hero, int manaCost, String skillName, double duration, int range, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            int dmgIncrease = (int)(hero.moveSpeed*Const.POWERUPDAMAGEINCREASE);
            hero.moveSpeed += 0;
            hero.range += 10;
            hero.damage += dmgIncrease;
            game.events.add(new Event.PowerUpEvent(hero, 0, 10, dmgIncrease, (int)duration));
            Const.viewController.addEffect(hero, null, "powerup", duration);
        }
        @Override
        public double CastTime(){return 0.0;}

        @Override
        public String getTargetTeam() {
            return "NONE";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.SELF;
        }
    }
    // Lancer skills end

    // AOE health Skills
    static class AOEHealSkill extends SkillBase{
        private String skin;
        int radius;

        AOEHealSkill(Hero hero, int manaCost, String skillName, double duration, int range, int radius, String skin, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
            this.radius = radius;
            this.skin = skin;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Point target = new Point(x,y);
            if(target.distance(hero) <= range){
                game.events.add(new Event.HealthChangeEvent(target, duration, radius, (int)(0.2*(hero.mana+manaCost)), false, hero));
                Const.viewController.addEffect(hero, target, "heal", duration);
            }else Const.viewController.addSummary("Can't heal outside range.");
        }
        @Override
        public double CastTime(){return duration;}

        @Override
        public String getTargetTeam() {
            return "ALLIED";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.POSITION;
        }
    }


    static class BurningGround extends SkillBase{
        private String skin;
        int radius;

        BurningGround(Hero hero, int manaCost, String skillName, double duration, int range, int radius, String skin, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
            this.radius = radius;
            this.skin = skin;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Point target = new Point(x,y);
            if(target.distance(hero) <= range+Const.EPSILON){
                game.events.add(new Event.HealthChangeEvent(target, duration, radius, -1*(hero.manaregeneration*5+30), true, hero));
                Const.viewController.addEffect(hero, target, "burning", duration);
            } else Const.viewController.addSummary("Can't burn ground outside range.");
        }
        @Override
        public double CastTime(){return duration;}

        @Override
        public String getTargetTeam() {
            return "ENEMY";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.POSITION;
        }
    }


    static class ShieldSkill extends SkillBase{
        ShieldSkill(Hero hero, int manaCost, String skillName, int duration, int range, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Unit target = Const.game.getUnitOfId(unitId);
            if(target.distance(hero) <= range){
                target.shield = Math.max(target.shield, (int)(0.5*hero.maxMana + 50));
                game.events.add(new Event.ShieldEvent(target, (int)duration));
                Const.viewController.addEffect(hero, target, "shield", duration);
            } else Const.viewController.addSummary("Can't shield hero outside range");
        }

        @Override
        public double CastTime(){return 0.0;}

        @Override
        public String getTargetTeam() {
            return "ALLIED";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.UNIT;
        }
    }

    static class PullSkill extends SkillBase{
        double delay;
        PullSkill(Hero hero, int manaCost, String skillName, double duration, int range, int cooldown, double delay) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
            this.delay = delay;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Unit unit = Const.game.getUnitOfId(unitId);
            double distance =unit.distance(hero);
            if(distance <= range && !(unit instanceof Tower)){
                if(distance > Const.EPSILON){
                    double vx = (hero.x-unit.x)/distance*200/duration;
                    double vy = (hero.y-unit.y)/distance*200/duration;
                    game.events.add(new Event.SpeedChangedForceEvent(unit, delay, vx, vy));
                    game.events.add(new Event.SpeedChangedForceEvent(unit, delay+duration, vx*-1, vy*-1));
                    game.events.add(new Event.StunEvent(unit, delay, 1));
                }

                if(unit instanceof Hero && unit.team != hero.team)
                    game.events.add(new Event.DrainManaEvent(unit, delay+duration, ((Hero)unit).manaregeneration*3 + 5, hero));

            }else Const.viewController.addSummary("Can't pull target outside range.");
        }
        @Override
        public double CastTime(){return delay;}

        @Override
        public String getTargetTeam() {
            return "BOTH";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.UNIT;
        }
    }
    // DR STRANGE skills end

    // HULK Skills
    static class ChargeSkill extends SkillBase{
        ChargeSkill(Hero hero, int manaCost, String skillName, double duration, int range, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Unit target = Const.game.getUnitOfId(unitId);
            double distance = target.distance(hero);
            if(distance <= range){

                game.events.add(new Event.BlinkEvent(hero, duration, target.x, target.y));
                if (target.team != hero.team) {

                    //Reduce dmg on his attack on the delayed attack
                    int halfDmg = hero.damage/2;
                    hero.damage-=halfDmg;
                    game.events.add(new Event.PowerUpEvent(hero, 0, 0, -halfDmg, 0));
                    game.events.add(new Event.DelayedAttackEvent(target, hero, duration+Const.EPSILON));

                    game.events.add(new Event.PowerUpEvent(target, 150, 0, 0, 0));
                    game.events.add(new Event.PowerUpEvent(target, -150, 0, 0, 3));
                }
            }else Const.viewController.addSummary("Can't charge further than range.");
        }
        @Override
        public double CastTime(){return duration;}

        @Override
        public String getTargetTeam() {
            return "ENEMY";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.UNIT;
        }
    }

    static class BashSkill extends SkillBase{
        BashSkill(Hero hero, int manaCost, String skillName, int duration, int range, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Unit target = Const.game.getUnitOfId(unitId);
            if(target.distance(hero) <= range && !(target instanceof Tower)){
                game.events.add(new Event.DamageEvent(target, hero, hero.attackTime, hero.damage));
                game.events.add(new Event.StunEvent(target, hero.attackTime, (int)duration));
            }else Const.viewController.addSummary("Can't bash unit outside range.");
        }
        @Override
        public double CastTime(){return hero.attackTime;}

        @Override
        public String getTargetTeam() {
            return "ENEMY";
        }

        @Override
        public  SkillType getTargetType() {
            return SkillType.UNIT;
        }
    }

    static class ExplosiveSkill extends SkillBase{
        ExplosiveSkill(Hero hero, int manaCost, String skillName, double duration, int range, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            hero.explosiveShield=(int)(hero.maxHealth*0.07 + 50);
            game.events.add(new Event.ExplosiveShieldEvent(hero, (int)Math.round(duration)));
            Const.viewController.addEffect(hero, hero, "shield", duration);
        }
        @Override
        public double CastTime(){return 0.0;}

        @Override
        public String getTargetTeam() {
            return "ENEMY";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.SELF;
        }
    }
    // Knight Skills end

    // Ninja Skills
    static class CounterSkill extends SkillBase{
        CounterSkill(Hero hero, int manaCost, String skillName, int duration, int range, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.duration = duration;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            game.events.add(new Event.CounterEvent(hero, duration-Const.EPSILON, range));
            Const.viewController.addEffect(hero, null, "counter", duration);
        }
        @Override
        public double CastTime(){return 0.0;}

        @Override
        public String getTargetTeam() {
            return "ENEMY";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.SELF;
        }
    }

    static class StealthSkill extends SkillBase{
        StealthSkill(Hero hero, int manaCost, String skillName, double range, double duration, int cooldown) {
            super(hero, manaCost, skillName, 0, cooldown);
            this.duration = duration;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            hero.invisibleBySkill = true;
            game.events.add(new Event.StealthEvent(hero, duration, hero.mana));
            hero.runTowards(new Point(x, y), hero.moveSpeed);
            Const.viewController.addEffect(hero, null, "invis", duration);
        }
        @Override
        public double CastTime(){return 1.0;}

        @Override
        public String getTargetTeam() {
            return "NONE";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.POSITION;
        }
    }

    static class WireHookSkill extends SkillBase{
        double radius;
        int stun_time;
        double speed;
        double flyTime;


        WireHookSkill(Hero hero, int manaCost, String skillName, int range, int radius, int stun_time, double duration, int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.stun_time = stun_time;
            this.speed = range/duration;
            this.radius = radius;
            this.flyTime = duration;
            this.duration = stun_time;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Point target = new Point(x,y);
            double distance = hero.distance(target);
            target.x= hero.x + (x-hero.x) / distance * range;
            target.y= hero.y + (y-hero.y) / distance * range;

            MovingEntity lineSpellUnit = new MovingEntity(hero.x,hero.y, (target.x-hero.x)/flyTime, (target.y-hero.y)/flyTime);
            Const.viewController.addEffect(hero, target, "wirehook", flyTime);

            double lowestT = 2;
            ArrayList<Hero> possibleTargets = new ArrayList<>();
            for(int i = Const.game.allUnits.size()-1 ; i >= 0; i--) {
                Unit unit = Const.game.allUnits.get(i);
                if(unit.team != hero.team && (unit instanceof Hero)){
                    double collisionT = Utilities.getCollisionTime(lineSpellUnit, unit, radius);
                    possibleTargets.add((Hero)unit);
                    if(collisionT>=0 && collisionT <= lowestT){
                        lowestT = collisionT;
                    }
                }
            }

            Const.game.events.add(new Event.WireEvent(possibleTargets, lowestT, lineSpellUnit, stun_time, hero, radius, duration, 0.5));
        }
        @Override
        public double CastTime(){return 0.0;}

        @Override
        public String getTargetTeam() {
            return "ENEMY";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.POSITION;
        }
    }
    // Ninja Skills end

    static class LineSkill extends SkillBase{
        double radius;
        double speed;


        LineSkill(Hero hero, int manaCost, String skillName, int range, int radius, double duration,  int cooldown) {
            super(hero, manaCost, skillName, range, cooldown);
            this.speed = range/duration;
            this.radius = radius;
            this.duration = duration;
        }

        @Override
        void doSkill(Game game, double x, double y, int unitId) {
            Point target = new Point(x,y);
            double distance = hero.distance(target);
            double vx = (x-hero.x) / distance;
            double vy = (y-hero.y) / distance;

            target.x= hero.x + vx * range;
            target.y= hero.y + vy * range;

            MovingEntity lineSpellUnit = new MovingEntity(hero.x,hero.y,vx*range/duration, vy*range/duration);
            Const.viewController.addEffect(hero, target, "fireball", duration);

            for(Unit unit : Const.game.allUnits){
                if(unit.team != hero.team && (unit instanceof Hero || unit instanceof Creature)){
                    double collisionT = Utilities.getCollisionTime(lineSpellUnit, unit, radius-Const.EPSILON);
                    Const.game.events.add(new Event.LineEffectEvent(unit, collisionT < 0 ? duration : collisionT, lineSpellUnit, (int)(0.2*(hero.mana+manaCost)), hero, radius, duration, 55));
                }
            }
        }
        @Override
        public double CastTime(){return 0.0;}

        @Override
        public String getTargetTeam() {
            return "ENEMY";
        }

        @Override
        public SkillType getTargetType() {
            return SkillType.POSITION;
        }
    }
}
