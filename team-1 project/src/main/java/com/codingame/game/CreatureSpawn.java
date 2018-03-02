package com.codingame.game;

public class CreatureSpawn extends Point {
    CreatureSpawn(int x, int y) {
        super(x, y);
    }

    public String getPlayerString() {
        return "SPAWN " + (int) x + " " + (int) y + " 0";
    }
}
