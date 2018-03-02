package com.codingame.game;
import com.codingame.game.Const;
import com.codingame.game.Event;
import com.codingame.game.Unit;


public class Bush extends Point {
    double radius;
    String skin;
    public Bush(double x, double y) {
        super(x, y);
        this.radius = Const.BUSHRADIUS;
    }

    public String getPlayerString() {
        return "BUSH" + " " + (int) x + " " + (int) y + " " + (int)radius;
    }
}