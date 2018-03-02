package com.codingame.game;

import java.util.HashMap;
import java.util.Map;

public class Item {
    public String name;
    public Map<String, Integer> stats;
    public int cost;
    public boolean isPotion;
    String skin;
    
    public Item(String name, Map<String, Integer> stats, int cost, String skin, boolean isPotion) {
        this.name = name;
        this.stats = fillEmptyStats(stats);
        this.cost = cost;
        this.skin = skin;
        this.isPotion = isPotion;
    }

    String getPlayerString(){
        return name +
                " " + cost +
                " " + stats.get(Const.DAMAGE) +
                " " + stats.get(Const.HEALTH) +
                " " + stats.get(Const.MAXHEALTH) +
                " " + stats.get(Const.MANA) +
                " " + stats.get(Const.MAXMANA) +
                " " + stats.get(Const.MOVESPEED) +
                " " + stats.get(Const.MANAREGEN) +
                " " + (isPotion?1:0);
    }

    static Map<String, Integer> fillEmptyStats(Map<String, Integer> stats){
        if(!stats.containsKey(Const.DAMAGE)) stats.put(Const.DAMAGE, 0);
        if(!stats.containsKey(Const.HEALTH)) stats.put(Const.HEALTH, 0);
        if(!stats.containsKey(Const.MAXHEALTH)) stats.put(Const.MAXHEALTH, 0);
        if(!stats.containsKey(Const.MANA)) stats.put(Const.MANA, 0);
        if(!stats.containsKey(Const.MAXMANA)) stats.put(Const.MAXMANA, 0);
        if(!stats.containsKey(Const.MOVESPEED)) stats.put(Const.MOVESPEED, 0);
        if(!stats.containsKey(Const.MANAREGEN)) stats.put(Const.MANAREGEN, 0);
        return stats;
    }
}
