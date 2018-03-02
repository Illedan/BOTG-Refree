package com.codingame.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFactory {
    private static int center = Const.MAPWIDTH/2;

    public List<Bush> generateBushes(CreatureSpawn[] spawns){
        ArrayList<Bush> obstacles = new ArrayList<>();
        // bushes below the lane
        int posX = 50;
        int bottomBushCount = (int) (Const.random.nextDouble() * 4 + 1);
        for(int i = 0; i < bottomBushCount; i++) {
            posX = (int) (Const.random.nextDouble() * ((910 - posX) / (bottomBushCount-i)) + posX);
            if(posX > 910) break;
            obstacles.add(Factories.generateBush(new Point(posX, 720)));
            obstacles.add(Factories.generateBush(new Point(1920 - posX, 720)));
            posX += 100;
        }

        // bushes above the lane
        posX = 50;
        bottomBushCount = (int) (Const.random.nextDouble() * 2 + 2);
        for(int i = 0; i < bottomBushCount; i++) {
            posX = (int) (Const.random.nextDouble() * ((910 - posX) / (bottomBushCount-i)) + posX);
            if(posX > 910) break;
            obstacles.add(Factories.generateBush(new Point(posX, 380)));
            obstacles.add(Factories.generateBush(new Point(1920 - posX, 380)));
            posX += 100;
        }

        int extraBushes = (spawns.length);
        for(int i = 0; i < extraBushes; i++){
            int posY = (int)spawns[i].y;
            posX = (int)spawns[i].x;
            spawnExtraBushes(posX-150, posY-150, posX+150, posY+150, obstacles);
        }

        extraBushes = (int)(Const.random.nextDouble()*4);
        for(int i = 0; i < extraBushes; i++){
            spawnExtraBushes(Const.MAPWIDTH/2-300, 50, Const.MAPWIDTH/2+300, 300, obstacles);
        }
        return obstacles;
    }


    void spawnExtraBushes(int startX, int startY, int endX, int endY, List<Bush> bushes) {
        for(int i = 0; i < 10; i++){
            int posX = (int)(Const.random.nextDouble()*(endX-startX)+startX);
            int posY = (int)(Const.random.nextDouble()*(endY-startY)+startY);
            if(posX < Const.BUSHRADIUS
                    || posY < Const.BUSHRADIUS
                    || posX > Const.MAPWIDTH-Const.BUSHRADIUS
                    || posY > Const.MAPHEIGHT - Const.BUSHRADIUS) continue;
            Point newBush = new Point(posX, posY);
            boolean isOverlapping = false;
            for(Bush bush : bushes){
                if(bush.distance(newBush) <= bush.radius*2) isOverlapping = true;
            }

            if(!isOverlapping){
                if(Math.abs(posX-center) < Const.BUSHRADIUS)
                    bushes.add(Factories.generateBush(new Point(center, posY)));
                else {
                    bushes.add(Factories.generateBush(new Point(posX, posY)));
                    bushes.add(Factories.generateBush(new Point(1920 - posX, posY)));
                }
                break;
            }
        }
    }


    CreatureSpawn[] generateSpawns(){
        int forestCampCount = (int) (Const.random.nextDouble() * 2 + 3);
        CreatureSpawn[] spawns = new CreatureSpawn[forestCampCount];
        int posX = 50;
        int campIter = (int)Math.floor(forestCampCount/2);
        int midBoundary = 850;
        if(forestCampCount % 2 > 0) {
            spawns[(int)Math.floor(forestCampCount/2)] = new CreatureSpawn(960, (int) (Const.random.nextDouble() * 230 + 50));
            midBoundary = 750;
        }
        for(int i = 0; i < campIter; i++) {
            posX = (int) (Const.random.nextDouble() * ((midBoundary - posX) / (campIter-i)) + posX);
            if(posX > midBoundary) break;
            int posY = (int) (Const.random.nextDouble() * 230 + 50);
            spawns[i] = new CreatureSpawn(posX, posY);
            spawns[forestCampCount-i-1] = new CreatureSpawn(1920 - posX, posY);
            posX += 150;
        }

        return spawns;
    }

    // starting - early - mid - late

    public static int[][] ItemLevels ={{50, 200}, {200, 550}, {550, 1300}, {1300, 3000}};
    public Map<String, Item> generateItems(int playersGold){
        Map<String, Item> items = new HashMap<>();

        for (int[] level : ItemLevels) {
            for (int i = 0; i < Const.NB_ITEMS_PER_LEVEL; i++) {
                addItem(generateItem(level[0], level[1], items.size()+1), items);
            }
        }
        addItem(generateManaPot(), items);
        addItem(generateLargePot(), items);
        addItem(generateXXLPot(), items);
        return items;
    }

    static void addItem(Item item, Map<String, Item> items){
        items.put(item.name, item);
    }

    static Item generateItem(int lowerLimit, int upperLimit, int nb) {
        Map<String, Integer> stats = new HashMap<>();

        int spent = 0;
        String prefix = "";
        String suffix = "Gadget";
        int cost = Utilities.rndInt(lowerLimit, upperLimit);

        while (spent + Const.MINIMUM_STAT_COST < cost) {

            int investment = Math.min(cost-spent, Utilities.rndInt(Const.MINIMUM_STAT_COST, cost));
            String statType = Const.STATS[Utilities.rndInt(0, Const.STATS.length)];

            if(stats.containsKey(statType)){
                if(stats.get(statType) < getLimit(statType)){
                    int current = stats.get(statType);
                    int extra = (int)(investment/getPrice(statType));
                    int actual = Math.min(getLimit(statType), current+extra);
                    stats.put(statType, actual);
                    investment = (int)((actual-current)*getPrice(statType));
                }else continue;
            }else{
                int extra = (int)(investment/getPrice(statType));
                int actual = Math.min(getLimit(statType), extra);
                stats.put(statType, actual);
                investment = (int)((actual)*getPrice(statType));
            }

            spent += investment;
        }

        if(stats.containsKey(Const.HEALTH)) {
            stats.put(Const.MAXHEALTH, stats.get(Const.HEALTH));
        }
        if(stats.containsKey(Const.MANA)) {
            stats.put(Const.MAXMANA, stats.get(Const.MANA));
        }

        int dmg = stats.containsKey(Const.DAMAGE) ? stats.get(Const.DAMAGE) : 0;
        int speed = stats.containsKey(Const.MOVESPEED) ? stats.get(Const.MOVESPEED) : 0;

        if(dmg> 0 && dmg > speed) {
            suffix = "Blade";
        } else if(speed > 0) {
            suffix = "Boots";
        }
        if(lowerLimit == ItemLevels[0][0]) prefix += "Bronze";
        else if(lowerLimit == ItemLevels[1][0]) prefix += "Silver";
        else if(lowerLimit == ItemLevels[2][0]) prefix += "Golden";
        else if(lowerLimit == ItemLevels[3][0]) prefix += "Legendary";
        return new Item(prefix + "_" + suffix + "_" + nb, stats, findCost(stats), "", false);
    }

    static Item generateManaPot() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put(Const.MANA, 50);
        Item pot = new Item("mana_potion", stats, findCost(stats), Const.POTION, true);
        pot.isPotion = true;
        return pot;
    }

    static Item generateLargePot() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put(Const.HEALTH, 100);
        Item pot = new Item("larger_potion", stats, findCost(stats), Const.POTION, true);
        pot.isPotion = true;
        return pot;
    }

    static Item generateXXLPot() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put(Const.HEALTH, 500);
        Item pot = new Item("xxl_potion", stats, findCost(stats), Const.POTION, true);
        pot.isPotion = true;
        return pot;
    }

    public static double healthPrice = 0.7;
    public static double maxHealthPrice = 0;
    public static double manaPrice = 0.5;
    public static double maxManaPrice = 0;
    public static double damagePrice = 7.2;
    public static double moveSpeedPrice = 3.6;
    public static double manaRegPrice = 50;

    static int findCost(Map<String, Integer> stats){
        double totalCost = 0.0;
        for (String stat : stats.keySet()){
            int val = stats.get(stat);
            totalCost += val*getPrice(stat);
        }

        return (int)Math.max(Math.ceil(totalCost/2), Math.ceil(totalCost-(totalCost*totalCost/6000)) );
    }

    public static int getLimit(String type){
        switch (type) {
            case Const.HEALTH: return 2500;
            case Const.MAXHEALTH: return 2500;
            case Const.MANA: return 100;
            case Const.MAXMANA: return 100;
            case Const.DAMAGE: return 100000000;
            case Const.MOVESPEED: return 150;
            case Const.MANAREGEN: return 50;
        }

        return 0;
    }

    public static double getPrice(String type){
        switch (type) {
            case Const.HEALTH: return healthPrice;
            case Const.MAXHEALTH: return maxHealthPrice;
            case Const.MANA: return manaPrice;
            case Const.MAXMANA: return maxManaPrice;
            case Const.DAMAGE: return damagePrice;
            case Const.MOVESPEED: return moveSpeedPrice;
            case Const.MANAREGEN: return manaRegPrice;
        }

        return 0;
    }

    static double addItemCost(int val, String stat, String type, double amplitude){
        if(stat.equals(type)) return val*amplitude;
        return 0.0;
    }
}
