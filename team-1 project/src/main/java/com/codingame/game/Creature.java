package com.codingame.game;

import java.util.List;

public class Creature extends Unit {

    Point camp;
    CreatureState state = CreatureState.peacefull;
    String creatureType;
    public Creature(double x, double y) {
        super(x, y, 1, -1, 300, null);
        this.camp = new Point(x, y);
    }

    @Override
    public String getType() {
        return this.creatureType;
    }


    void findAction(List<Unit> allUnits) {
        if (isDead || stunTime>0) return;
        if (this.state.equals(CreatureState.aggressive)) {
            aggressiveBehavior(allUnits);
        } else if (this.state.equals(CreatureState.runningback)) {
            runningBackBehavior();
        }
    }

    void aggressiveBehavior(List<Unit> allUnits) {
        Unit attacker = allUnits.stream()
                .filter(u -> u instanceof Hero)
                .sorted((u1, u2) -> u1.distance2(this) < u2.distance2(this) ? -1 : 1)
                .findFirst()
                .get();
        Point target = attacker;
        if (distance2(camp) < Const.AGGROUNITRANGE2) {
            this.attackUnitOrMoveTowards((Unit) target, 0.0);
        } else {
            this.state = CreatureState.runningback;
            this.runTowards(camp);
        }
    }

    void runningBackBehavior() {
        this.runTowards(camp);
        if (distance(camp) < 2) {
            this.state = CreatureState.peacefull;
            this.health = maxHealth;
            Const.viewController.addEffect(this, this, "default", 0);
        }
    }
}
