package com.codingame.game;

public class MovingEntity extends Point {
    double vx;
    double vy;
    double forceVX;
    double forceVY;
    public MovingEntity(double x, double y, double vx, double vy) {
        super(x, y);
        this.vx = vx;
        this.vy = vy;
    }

    public Point targetPointOfMovement(){
        return new Point(x+vx, y+vy);
    }

    public void moveIgnoreEdges(double t){
        this.x += vx*t;
        this.y += vy*t;
    }

    public void move(double t) {
        move(x + vx * t, y + vy * t);
    }

    // Move the point to x and y
    public void move(double x, double y) {
        this.x = x;
        this.y = y;
        if (this.x < 30) this.x = 30;
        if (this.y < 30) this.y = 30;
        if (this.x >= Const.MAPWIDTH - 30) this.x = Const.MAPWIDTH - 30;
        if (this.y >= Const.MAPHEIGHT - 30) this.y = Const.MAPHEIGHT - 30;
    }

    // Move the point to an other point for a given distance
    public void moveTo(Point p, double distance) {
        double d = distance(p);

        if (d < Const.EPSILON) {
            return;
        }

        double dx = p.x - x;
        double dy = p.y - y;
        double coef = distance / d;

        move(x + dx * coef, y + dy * coef);
    }
}
