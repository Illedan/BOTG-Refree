package com.codingame.game;

public class Point {
    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Point p) {
        return Math.sqrt(distance2(p));
    }

    public double distance2(Point p) {
    	return ((this.x - p.x) * (this.x - p.x) + (this.y - p.y) * (this.y - p.y));
    }
}
