package com.codingame.game;

public class Damage {
    public Damage(Unit target, Unit attacker, int damage){
        this.target = target;
        this.attacker = attacker;
        this.damage = damage;
    }
    //Unit unit, int damage, Unit attacker, double currentT
    public Unit target;
    public Unit attacker;
    public int damage;
}
