package com.codingame.game;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utilities {

    public static boolean isNumber(String s){
        try
        {
            double d = Double.parseDouble(s);
            if(Double.isInfinite(d)) return false;
            if(Double.isNaN(d)) return false;
        }catch (Exception e){
            return false;
        }

        return true;
    }

    public static int rndInt(int min, int max){
        return (int)(Const.random.nextDouble()*(max-min)+min);
    }

    public static int round(double x) {
        int s = x < 0 ? -1 : 1;
        return s * (int) ((s * x) + 0.5);
    }


    public static double timeToReachTarget(Point start, Point stop, double speed){
        return start.distance(stop)/speed;
    }

    public static double getCollisionTime(MovingEntity entity, MovingEntity entity1, double radius){
        // Check instant collision
        if (entity.distance(entity1) <= radius) {
            return 0.0;
        }

        // Fixes rounding errors.
        radius-=Const.EPSILON;

        // Both units are motionless
        if (entity.vx == 0.0 && entity.vy == 0.0 && entity1.vx == 0.0 && entity1.vy == 0.0) {
            return -1;
        }

        // Change referencial
        // Unit u is not at point (0, 0) with a speed vector of (0, 0)
        double x2 = entity.x - entity1.x;
        double y2 = entity.y - entity1.y;
        double r2 = radius;
        double vx2 = entity.vx - entity1.vx;
        double vy2 = entity.vy - entity1.vy;

        double a = vx2 * vx2 + vy2 * vy2;

        if (a <= 0.0) {
            return -1;
        }

        double b = 2.0 * (x2 * vx2 + y2 * vy2);
        double c = x2 * x2 + y2 * y2 - r2 * r2;
        double delta = b * b - 4.0 * a * c;

        if (delta < 0.0) {
            return -1;
        }

        double t = (-b - Math.sqrt(delta)) / (2.0 * a);

        if (t <= 0.0) {
            return -1;
        }

        return t;
    }
}
