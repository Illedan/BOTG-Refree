package com.codingame.game;

import java.util.ArrayList;

public abstract class Event {
    public static int NONE = 0;
    public static int LIFECHANGED = 1;
    public static int SPEEDCHANGED = 2;
    public static int TELEPORTED = 4;
    public static int STUNNED = 8;

    private static ArrayList<Unit> EMPTYLIST = new ArrayList<>();

    double _t;
    double t;
    Unit unit;

    Event(Unit unit, double t) {
        this.unit = unit;
        this.t = t;
        _t = t;
    }

    int getOutcome(){ return NONE; }

    abstract ArrayList<Unit> onEventTime(double currentTime);

    boolean useAcrossRounds() { return false; }

    abstract boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime);

    protected boolean unitAlive(Unit unit, int outcome) {
        return !unitDead(this.unit, unit, outcome);
    }

    protected boolean unitStopped(Unit unit){
        if(unit.isDead) return true;
        if(unit.stunTime>0) return true;
        return false;
    }

    protected boolean unitStopped(Unit unit, Unit affected, int outcome){
        if(unit != affected) return false;
        if(unitDead(unit, affected, outcome)) return true;
        if(hasOutcome(outcome, STUNNED) && unit.stunTime > 0) return true;
        return false;
    }

    protected void setSpeedAndAlertChange(Unit unit, double vx, double vy){
        Const.game.events.add(new SpeedChangedEvent(unit, 0.0, vx, vy));
    }

    protected boolean unitDead(Unit unit, Unit affected, int outcome){
        if(unit != affected) return false;
        return unit.isDead;
    }

    protected void runSilentlyTowards(Unit unit, Point targetPoint){
        double targetDist = targetPoint.distance(unit);
        double coef = (((double) unit.moveSpeed)) / targetDist;
        unit.vx = (targetPoint.x - unit.x) * coef;
        unit.vy = (targetPoint.y - unit.y) * coef;
        unit.moving = true;
        Const.viewController.addEffect(unit, targetPoint, "movement", 1.0);
    }

    protected boolean hasAnyOutcome(int outcome, int expected1, int expected2){
        return hasOutcome(outcome, expected1) || hasOutcome(outcome, expected2);
    }

    protected boolean hasOutcome(int outcome, int expectedOutcome){
        return (outcome & expectedOutcome) != 0;
    }


    protected Unit getClosestUnitInRange(Point root, double range, int team, boolean targetEnemies, Unit ignoredUnit){
        double closestDist = Const.MAXDOUBLE;
        Unit closest = null;
        for(Unit unit : Const.game.allUnits){
            if(unit == ignoredUnit || (unit.team == team && targetEnemies) || (unit.team != team && !targetEnemies)) continue;
            if(unit instanceof Tower) continue;
            double dist = unit.distance2(root);
            if(dist < closestDist && dist <= range*range) {
                closestDist = dist;
                closest = unit;
            }
        }

        return closest;
    }


    protected void doDamage(Unit unit, int damage, Unit attacker){
        if(Const.game.damages.containsKey(unit)) Const.game.damages.get(unit).add(new Damage(unit, attacker, damage));
        else {
            ArrayList<Damage> damages = new ArrayList<>();
            damages.add(new Damage(unit, attacker, damage));
            Const.game.damages.put(unit, damages);
        }
    }

    protected ArrayList<Unit> createListOfUnit() {
        ArrayList<Unit> units = new ArrayList<>();
        units.add(unit);
        return units;
    }

    public static class OnLocationReachedEvent extends Event {
        OnLocationReachedEvent(Unit unit, double t) {
            super(unit, t);
        }

        @Override
        int getOutcome() {
            return SPEEDCHANGED;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            unit.vx = 0;
            unit.vy = 0;
            unit.moving = false;
            if(currentTime < 0.99)
                Const.viewController.addEffect(unit, new Point(unit.x+unit.vx, unit.y+unit.vy), "default", 1.0);

            return createListOfUnit();
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return unitStopped(unit, affectedUnit, outcome) ;
        }
    }

    public static class SpeedChangedForceEvent extends Event {
        double vx, vy;
        SpeedChangedForceEvent(Unit unit, double t, double forcevx, double forcevy) {
            super(unit, t);
            this.vx = forcevx;
            this.vy = forcevy;
        }

        @Override
        int getOutcome() {
            return SPEEDCHANGED;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            unit.forceVX += vx;
            unit.forceVY += vy;
            unit.vx = unit.forceVX;
            unit.vy = unit.forceVY;

            return createListOfUnit();
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return !unitAlive(affectedUnit, outcome);
        }
    }

    public static class SpeedChangedEvent extends Event {
        double vx, vy;


        SpeedChangedEvent(Unit unit, double t, double vx, double vy) {
            super(unit, t);
            this.vx = vx;
            this.vy = vy;
        }

        @Override
        int getOutcome() {
            return SPEEDCHANGED;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            unit.vx = vx;
            unit.vy = vy;

            return createListOfUnit();
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return !unitAlive(affectedUnit, outcome);
        }
    }


    public static class DelayedAttackEvent extends Event {
        Unit attacker;

        DelayedAttackEvent(Unit unit, Unit attacker, double t) {
            super(unit, t);
            this.attacker = attacker;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            this.attacker.fireAttack(this.unit);
            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return unitStopped(this.attacker) || unitDead(this.unit, affectedUnit, outcome) || (affectedUnit == unit && hasOutcome(outcome, TELEPORTED));
        }
    }

    public static class DamageEvent extends Event {
        int damage;
        Unit attacker;

        DamageEvent(Unit unit, Unit attacker, double t, int damage) {
            super(unit, t);
            this.attacker = attacker;
            this.damage = damage;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            doDamage(unit, damage, attacker);
            return createListOfUnit();
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            if(affectedUnit == this.unit && hasOutcome(outcome, TELEPORTED)){
                return true;
            }

            return unitDead(this.unit, affectedUnit, outcome);
        }
    }

    // when hit throws a dagger on nearby enemy
    public static class CounterEvent extends Event{
        int health, range;
        CounterEvent(Unit defender, double t, int range) {
            super(defender, t);
            this.health = defender.health;
            this.range = range;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            if(this.health > unit.health){
                Unit closest = getClosestUnitInRange(this.unit, this.range, this.unit.team, true, this.unit);
                int damage = (int)((this.health-unit.health)*1.5);
                if(closest != null) {
                    doDamage(closest, damage, this.unit);
                }

                unit.health = this.health;
            }
            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return !unitAlive(affectedUnit, outcome);
        }
    }

    public static class StunEvent extends Event{
        int stunTime;
        StunEvent(Unit unit, double t, int stunTime) {
            super(unit, t);
            this.stunTime = stunTime;
        }

        @Override
        boolean useAcrossRounds() { return true; }

        @Override
        int getOutcome() {
            return STUNNED;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            unit.stunTime = Math.max(unit.stunTime, stunTime);
            Const.viewController.addEffect(unit, null, "stun", stunTime);
            unit.vx = unit.forceVX;
            unit.vy = unit.forceVY;
            return createListOfUnit();
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return !unitAlive(affectedUnit, outcome);
        }
    }


    public static class BlinkEvent extends Event{
        double x,y;
        BlinkEvent(Unit unit, double t, double x, double y) {
            super(unit, t);
            this.x = x;
            this.y = y;
        }

        @Override
        int getOutcome() {
            return TELEPORTED;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            unit.move(x, y);
            return createListOfUnit();
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return unitDead(this.unit, affectedUnit, outcome);
        }
    }

    public static class AttackNearestDelayed extends Event{
        AttackNearestDelayed( Hero hero, double t){
            super(hero, t);
        }

        @Override
        int getOutcome() {
            return NONE;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            Unit toHit = getClosestUnitInRange(this.unit, this.unit.range, this.unit.team, true, this.unit);
            if(toHit != null) this.unit.fireAttack(toHit);
            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return unitStopped(this.unit, affectedUnit, outcome);
        }
    }

    public static class PowerUpEvent extends Event{
        int x,y, moveSpeed, range, damage, rounds;
        PowerUpEvent(Unit unit, int moveSpeed, int range, int damage, int rounds) {
            super(unit, Const.MAXINT);
            this.moveSpeed = moveSpeed;
            this.range = range;
            this.damage = damage;
            this.rounds = rounds;
        }

        @Override
        boolean useAcrossRounds() {
            rounds--;
            if(rounds <= 0) {
                onEventTime(0);
                return false;
            }
            return true;
        }

        @Override
        int getOutcome() {
            return NONE;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            unit.moveSpeed -= moveSpeed;
            unit.range -= range;
            unit.damage -= damage;
            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return unitDead(unit, affectedUnit, outcome);
        }
    }

    public static class StealthEvent extends Event{
        int x,y;
        double mana;
        StealthEvent(Unit unit, double t, double mana) {
            super(unit, t);
            this.mana = mana;
        }

        @Override
        boolean useAcrossRounds() { return !unit.visible; }

        @Override
        int getOutcome() {
            return NONE;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            unit.invisibleBySkill = false;
            unit.visible = true;
            return createListOfUnit();
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return !unitAlive(affectedUnit, outcome);
        }
    }

    public static class HealthChangeEvent extends Event{
        int  dHealth, range;
        Point targetPos;
        boolean hitEnemies;
        HealthChangeEvent(Point targetPos, double t, int range, int dHealth, boolean hitEnemies, Unit user) {
            super(user, t);
            this.range = range;
            this.targetPos = targetPos;
            this.dHealth = dHealth;
            this.hitEnemies = hitEnemies;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            for(int i = Const.game.allUnits.size()-1; i >= 0; i--){
                Unit target = Const.game.allUnits.get(i);
                if(unit.team == target.team && hitEnemies) continue;
                if(unit.team != target.team && !hitEnemies) continue;
                if((unit instanceof Tower)) continue;

                double dist2 =targetPos.distance2(target);
                if(dist2 <= range*range){
                    doDamage(target, -1*dHealth, this.unit);
                }
            }

            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return false;
        }
    }

    public static class ShieldEvent extends Event{
        int rounds;
        ShieldEvent(Unit unit, int t) {
            super(unit, t+1); // avoid rounding errors.
            rounds = t;
        }

        @Override
        boolean useAcrossRounds() {
            rounds--;
            if(rounds<=0){
                unit.shield = 0;
                Const.viewController.addEffect(unit, unit, "shield", 0);
                return false;
            }

            return true;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            unit.shield = 0;
            Const.viewController.addEffect(unit, unit, "shield", 0);
            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return affectedUnit==this.unit && affectedUnit.shield <= 0;
        }
    }

    public static class ExplosiveShieldEvent extends Event{
        int rounds;
        ExplosiveShieldEvent(Unit unit, int t) {
            super(unit, t+1);
            rounds = t;
        }

        @Override
        boolean useAcrossRounds() {
            rounds--;
            if(rounds<=0){
                unit.explosiveShield = 0;
                Const.viewController.addEffect(unit, unit, "shield", 0);
                return false;
            }

            return true;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            unit.explosiveShield = 0;
            Const.viewController.addEffect(unit, unit, "shield", 0);

            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            if(affectedUnit == this.unit && this.unit.explosiveShield <= 0){
                Const.game.events.add(new Event.ExplosionEvent(affectedUnit, 0, this.unit.x, this.unit.y));
                Const.viewController.addEffect(unit, null, "shieldexplosion", 0);

                return true;
            }

            return false;
        }
    }

    public static class ExplosionEvent extends Event{
        double x,y;
        ExplosionEvent(Unit unit, double t, double x, double y) {
            super(unit, t);
            this.x = x;
            this.y = y;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            for(Unit target : Const.game.allUnits){
                if(unit instanceof Tower || unit.team == target.team) continue;

                double dist2 = unit.distance2(target);
                if(dist2 <= Const.EXPLOSIVESHIELDRANGE2){
                    doDamage(target, Const.EXPLOSIVESHIELDDAMAGE, unit);
                }
            }
            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return false;
        }
    }

    public static class DrainManaEvent extends Event{
        Hero attacker;
        int manaToDrain;
        DrainManaEvent(Unit unit, double t, int manaToDrain, Hero attacker){
            super(unit, t);
            this.manaToDrain = manaToDrain;
            this.attacker = attacker;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            if(unit.isDead) return EMPTYLIST;

            //If attack is dead mana is still drained since spell would be in motion.
            Hero target = (Hero)unit;
            manaToDrain = Math.min(target.mana, manaToDrain);
            target.mana-=manaToDrain;
            attacker.mana+= manaToDrain;
            if(attacker.maxMana < attacker.mana) attacker.mana = attacker.maxMana;
            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            return false;
        }

    }

    public static class LineEffectEvent extends Event{
        MovingEntity movingSpell;
        Hero attacker;
        int damage, damageByTime;
        double radius, duration;
        LineEffectEvent(Unit unit, double t, MovingEntity movingSpell, int damage, Hero attacker, double radius, double duration, int damageByTime)
        {
            super(unit, t);
            this.attacker = attacker;
            this.movingSpell = movingSpell;
            this.damage = damage;
            this.radius = radius;
            this.duration = duration;
            this.damageByTime = damageByTime;
        }

        @Override
        int getOutcome() {
            return NONE;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            movingSpell.moveIgnoreEdges(currentTime);
            double dist2 = movingSpell.distance2(unit);
            double compareDist = Math.pow(radius, 2);
            if(currentTime<=duration && dist2 <= compareDist){
                doDamage(unit, (int)(damage+damageByTime*currentTime), attacker);
            }
            movingSpell.moveIgnoreEdges(currentTime*-1);

            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            if(currentTime > duration || !unitAlive(affectedUnit, outcome)) return true;

            if(affectedUnit == this.unit && hasAnyOutcome(outcome, SPEEDCHANGED, TELEPORTED)){
                movingSpell.moveIgnoreEdges(currentTime);
                double colT = Utilities.getCollisionTime(movingSpell, this.unit, radius-Const.EPSILON);
                if(colT >= 0)
                    t = colT;
                else t = 2;
                movingSpell.moveIgnoreEdges(currentTime*-1);
            }

            return false;
        }
    }

    public static class WireEvent extends Event{
        MovingEntity movingSpell;
        Hero attacker;
        int stun_time;
        double radius, duration, dmgMultiplier;
        ArrayList<Hero> potentialTargets;
        WireEvent(ArrayList<Hero> potentialTargets, double t, MovingEntity movingSpell, int stun_time, Hero attacker, double radius, double duration, double dmgMultiplier)
        {
            super(null, t);
            this.dmgMultiplier = dmgMultiplier;
            this.attacker = attacker;
            this.movingSpell = movingSpell;
            this.stun_time = stun_time;
            this.radius = radius;
            this.duration = duration;
            this.potentialTargets = potentialTargets;
        }

        @Override
        int getOutcome() {
            return STUNNED;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            movingSpell.moveIgnoreEdges(currentTime);
            if(currentTime <= duration ){
                Hero closest = null;
                double minDist2 = 0.0;

                for(Hero potentialTarget : potentialTargets){
                    double dist2 = potentialTarget.distance2(movingSpell);
                    if(dist2 <= Math.pow(radius+Const.EPSILON, 2) && (closest==null || dist2 < minDist2)){
                        minDist2 = dist2;
                        closest = potentialTarget;
                    }
                }

                if(closest!= null) {
                    Const.game.events.add(new StunEvent(closest, 0, 2));
                    doDamage(closest, (int)(closest.maxMana*dmgMultiplier), attacker);
                    return createListOfUnit();
                }
            }

            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
          if (affectedUnit == null) {
              return false;
          }
          if(affectedUnit.team != attacker.team && (affectedUnit instanceof Hero || affectedUnit instanceof Creature) && hasAnyOutcome(outcome, SPEEDCHANGED, TELEPORTED)){
              movingSpell.moveIgnoreEdges(currentTime);

              double affectedT = Utilities.getCollisionTime(movingSpell, affectedUnit, radius);
              if(affectedT < t && !affectedUnit.isDead) {
                  t = affectedT;
              }else{
                  t = 2;
                  for(Unit unit : potentialTargets){
                      if(unit.isDead) continue;
                      double colT = Utilities.getCollisionTime(movingSpell, unit, radius);
                      if(colT >= 0 && colT < t) t = colT;
                  }
              }

              movingSpell.moveIgnoreEdges(currentTime*-1);
          }

          return false;
        }
    }

    public static class AttackMoveEvent extends Event{
        Point targetPos;
        AttackMoveEvent(Unit unit, Point targetPos) {
            super(unit, 1.0);
            this.targetPos = targetPos;

            runSilentlyTowards(unit, targetPos);

            recalculate( null);
        }

        @Override
        int getOutcome() {
            return SPEEDCHANGED;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {

            Unit closest = this.unit.findClosestOnOtherTeam();
            if(unit.canAttack(closest)){
                this.unit.vx = 0;
                this.unit.vy = 0;
                unit.fireAttack(closest);
                return createListOfUnit();
            }

            if(closest == null || closest.isDead){
                this.unit.vx = 0;
                this.unit.vy = 0;
                return createListOfUnit();
            }

            if(currentTime <= 1.0) unit.attackUnitOrMoveTowards(closest, currentTime);
            return EMPTYLIST;
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            if(unitStopped(this.unit, affectedUnit, outcome)){
                return true;
            }
            if(affectedUnit == this.unit || affectedUnit == null || affectedUnit.team != 1-this.unit.team) return false;
            if(this.unit.distance2(affectedUnit) <= Const.AGGROUNITRANGE2){
                t = 0.0;
            }
            else if(hasAnyOutcome(outcome, SPEEDCHANGED, TELEPORTED)){
                recalculate(affectedUnit);
            }

            return false;
        }

        void recalculate(Unit changedUnit){
            if(changedUnit==null){
                t = 1.0;
                for (Unit unit : Const.game.allUnits){
                    if(unit.team != 1-this.unit.team || !this.unit.allowedToAttack(unit)) continue;
                    double time =Utilities.getCollisionTime(unit, this.unit, Const.AGGROUNITRANGE-Const.EPSILON);
                    if(time < t && time >= 0)
                        t = time;
                }
            }else{
                double time =Utilities.getCollisionTime(changedUnit, this.unit, Const.AGGROUNITRANGE-Const.EPSILON);
                if(time < t && time >= 0)
                    t = time;
            }
        }
    }

    public static class AttackMoveUnitEvent extends Event{
        Unit nextTarget;
        AttackMoveUnitEvent(Unit unit, Unit targetUnit) {
            super(unit, Utilities.timeToReachTarget(unit, targetUnit, unit.moveSpeed));
            this.nextTarget = targetUnit;
            recalculate(true);
        }

        @Override
        int getOutcome() {
            return SPEEDCHANGED;
        }

        @Override
        ArrayList<Unit> onEventTime(double currentTime) {
            this.unit.vx = 0;
            this.unit.vy = 0;
            if(currentTime < 0.99)
                Const.viewController.addEffect(unit, new Point(unit.x+unit.vx, unit.y+unit.vy), "default", 1.0);

            if(nextTarget.isDead || unitStopped(unit)){
                return createListOfUnit();
            }

            unit.fireAttack(nextTarget);
            return createListOfUnit();
        }

        @Override
        boolean afterAnotherEvent(Unit affectedUnit, int outcome, double currentTime) {
            if(this.unit != affectedUnit && this.nextTarget != affectedUnit) return false;
            if(unitStopped(this.unit, affectedUnit, outcome)) {
                t = 0.0;
            }
            else if(nextTarget == affectedUnit && hasOutcome(outcome, TELEPORTED)){
                setSpeedAndAlertChange(this.unit, 0,0); //when target teleports we lose track and just stops.
                return true;
            }else if(nextTarget== affectedUnit && hasOutcome(outcome, SPEEDCHANGED)){
                recalculate(false);
            }

            return false;
        }

        void recalculate(boolean run){
            double prevVx = this.unit.vx;
            double prevVy = this.unit.vy;

            if(unit.canAttack(nextTarget)){
                t = 0.0;
            }else{
                if(run) runSilentlyTowards(this.unit, nextTarget);
                double timeToTarget = Utilities.getCollisionTime(this.unit, this.nextTarget, this.unit.range-Const.EPSILON);
                if(timeToTarget < 0)
                    t = Utilities.timeToReachTarget(unit, nextTarget, unit.moveSpeed);
                else t = timeToTarget;
            }

            if(Math.abs(this.unit.vx-prevVx) > Const.EPSILON || Math.abs(this.unit.vy-prevVy) > Const.EPSILON){
                Const.game.events.add(new SpeedChangedEvent(this.unit, 0.0, this.unit.vx, this.unit.vy)); //Alert speed changed
            }
        }
    }
}
