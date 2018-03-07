package com.codingame.game;

import com.codingame.gameengine.module.entities.Entity;

public abstract class Unit extends MovingEntity {
    public int id;
    public int health;
    public int stunTime;
    public boolean visible;
    public boolean invisibleBySkill;
    public int team;
    public int damage;
    public int moveSpeed;
    public int range;
    public boolean isDead;
    public double attackTime = 0.1;
    public int goldValue;
    public int maxHealth;
    public Player player;
    public int shield = 0;
    public int explosiveShield;
    public boolean moving;
    public boolean becomingInvis;

    Entity sprite;
    public String skin;

    Unit(double x, double y, int health, int team, int moveSpeed, Player player) {
        super(x, y, 0,0);

        id = Const.GLOBAL_ID++;
        this.isDead = false;
        this.team = team;

        this.health = maxHealth = health;
        this.moveSpeed = moveSpeed;
        this.goldValue = 0;
        this.player = player;

        this.invisibleBySkill = false;
        this.visible = true;
        this.stunTime = 0;
    }

    public void adjustShield(int val){
        //remove min first
        if(shield < explosiveShield){
            int toRemove = Math.min(val, shield);
            val-=toRemove;
            shield-=toRemove;
            explosiveShield-=val;
        }else{
            int toRemove = Math.min(val, explosiveShield);
            val-=toRemove;
            explosiveShield-=toRemove;
            shield-=val;
        }
    }

    public int getShield() {
        return shield + explosiveShield;
    }
    public boolean isMelee(){ return range <= 150; }

    public abstract String getType();

    public String getPlayerString() {
        return  id + " " +
                team + " " +
                getType() + " " +
                (int) x + " " +
                (int) y + " " +
                range + " " +
                health + " " +
                maxHealth + " " +
                getShield() + " " +
                damage + " " +
                moveSpeed + " " +
                stunTime + " " +
                goldValue + " " +
                getExtraProperties();
    }

    protected String getExtraProperties(){
        return "0 0 0 0 0 0 - 1 0";
    }

    void setPoint(Point point) {
        move(point.x, point.y);
    }

    @Override
    public void move(double t) {
        if (isDead) return;
        super.move(t);
    }

    void moveAttackTowards(Point point){
        Unit closest = findClosestOnOtherTeam();

        if(canAttack(closest)){
            fireAttack(closest);
        }else if(closest != null && distance2(closest) < Const.AGGROUNITRANGE2){
            Const.game.events.add(new Event.AttackMoveUnitEvent(this, closest));
        }else Const.game.events.add(new Event.AttackMoveEvent(this, point));
    }

    void attackUnitOrMoveTowards(Unit unit, double t) {
        if(!allowedToAttack(unit)) return;
        if(distance2(unit) <= range*range) fireAttack(unit);
        else Const.game.events.add(new Event.AttackMoveUnitEvent(this, unit));
    }

    boolean allowedToTarget(Unit unit){
        if(unit == null) return false; // Nothing to see here
        if(isDead) return false; // Codebusters is another game..
        if(stunTime > 0) return false; // Gimme a break
        if(unit.team != team && !unit.visible && !(this instanceof Tower || this instanceof Creature)) return false; // What you see, is what you get
        return true;
    }

    boolean allowedToAttack(Unit unit){
        if(!allowedToTarget(unit)) return false;
        if(unit == this) return false; // Can't attack self
        if(unit.isDead || unit.health <= 0) return false; // Dead man tell no tale
        return true;
    }

    boolean canAttack(Unit unit){
        if(!allowedToAttack(unit)) return false; // You shall not pass
        if(unit.team == team && unit.health > unit.maxHealth * Const.DENYHEALTH) return false; // Cant deny healthy creep
        if(distance2(unit) > range*range) return false; // Cant attack far far away
        return true;
    }

    void fireAttack(Unit unit){
        if(!canAttack(unit)) return;

        double attackTravelTime = Math.min(1.0, attackTime + (isMelee() ? 0 : attackTime * distance(unit) / range));
        if(Const.game.t+attackTravelTime > 1.0) {
            return; // no attacks cross rounds.
        }

        Const.game.events.add(new Event.DamageEvent(unit, this, attackTravelTime, this.damage));
        Const.viewController.addEffect(this, unit, "attack", attackTime);
        //Creep aggro.
        if(this instanceof Hero && unit instanceof Hero){
            for(Unit u : Const.game.allUnits){
                if(u.team != 1-team) continue;
                if(u instanceof LaneUnit && distance2(u) < Const.AGGROUNITRANGE2){
                    LaneUnit peasant = ((LaneUnit)u);

                    if(peasant.aggroTimeLeft < Const.AGGROUNITTIME){
                        peasant.aggroUnit = this;
                        peasant.aggroTset = Const.game.t;
                    }else if(peasant.aggroTset == Const.game.t && peasant.aggroUnit != null && peasant.aggroUnit.distance2(unit) > this.distance2(unit)){
                        peasant.aggroUnit = this;
                    }

                    peasant.aggroTimeLeft= Const.AGGROUNITTIME;

                }
                if(u instanceof Tower && distance2(u) < Const.AGGROUNITRANGE2){
                    Tower peasant = ((Tower)u);

                    if(peasant.aggroTimeLeft < Const.AGGROUNITTIME){
                        peasant.aggroUnit = this;
                        peasant.aggroTset = Const.game.t;
                    }else if(peasant.aggroTset == Const.game.t && peasant.aggroUnit != null && peasant.aggroUnit.distance2(unit) > this.distance2(unit)){
                        peasant.aggroUnit = this;
                    }

                    peasant.aggroTimeLeft= Const.AGGROUNITTIME;
                }
            }
        }
    }

    Unit findClosestOnOtherTeam(String filter){
        Unit closest = null;
        boolean useFilter = !filter.equals("none");
        double minDist = Const.MAXDOUBLE;
        for(Unit unit : Const.game.allUnits){
            if(useFilter && !unit.getType().equals(filter)) continue;

            double dist = distance2(unit);

            //Closest on other team, if equal take lowest health and if equal highest y (to make equal matches)
            if((unit.team == 1-team || filter.equals("GROOT") )&& allowedToAttack(unit) &&
                    (closest==null || minDist > dist || (minDist==dist && (closest.health > unit.health ||  unit.y > closest.y)))){
                minDist = dist;
                closest = unit;
            }
        }

        return closest;
    }

    Unit findClosestOnOtherTeam(){
        return findClosestOnOtherTeam("none");
    }


    void runTowards(Point p, double speed) {
        double distance = distance(p);

        // Avoid a division by zero
        if (Math.abs(distance) <= Const.EPSILON) {
            return;
        }

        double timeToLocation = Utilities.timeToReachTarget(this, p, speed);

        double coef = (((double) speed)) / distance;
        vx = (p.x - this.x) * coef;
        vy = (p.y - this.y) * coef;
        moving = true;
        Const.viewController.addEffect(this, p, "movement", timeToLocation);
        if (speed > distance) {
            Const.game.events.add(new Event.OnLocationReachedEvent(this, timeToLocation));
        }
    }

    void runTowards(Point p) {
       runTowards(p, moveSpeed);
    }

    void afterRound() {
        if(stunTime>0) stunTime--;
        x = Utilities.round(x);
        y = Utilities.round(y);
        vx = 0;
        vy = 0;
        moving = false;
    }
}
