import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.*;

/*

bronze boss - this will be a default AI to be replaced by a player boss

buys cheapest items until inventory is full

it can be easily defeated by just buying better items

hides in bush early game to help players get used to bush mechanic

this league is all about using a second hero

*/


class Player {

    public static void main(String[] args) {
        int turn = 0;

        int lastGold = 0;

        boolean groot = true;

        // lists for bushes trees units and heroes
        ArrayList<Hero> myHeroes = new ArrayList<>();
        ArrayList<Hero> enemyHeroes = new ArrayList<>();

        Unit myTower = null;
        Unit enemyTower = null;

        ArrayList<Unit> myUnits = new ArrayList<>();
        ArrayList<Unit> enemyUnits = new ArrayList<>();
        ArrayList<Unit> creepCamps = new ArrayList<>();

        ArrayList<Doodah> bushes = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);

        int myTeam = Integer.parseInt(readLine(scanner));

        // reading spawn points and bushes
        Doodah hidingSpot = null;
        double bushDist = 1000000;
        Point initial = myTeam == 0 ? new Point(0, 500) : new Point(1920, 500);

        int doodads = Integer.parseInt(readLine(scanner));
        for (int i = 0; i < doodads; i++) {
            String[] s = readLine(scanner).split(" ");
            String type = s[0];
            System.err.println(type);
            int x = Integer.parseInt(s[1]);
            int y = Integer.parseInt(s[2]);
            int radius = Integer.parseInt(s[3]);
            Doodah bewsh = new Doodah(x, y, radius);
            bushes.add(bewsh);
            if(hidingSpot == null) {
                bushDist = initial.distance(bewsh);
                hidingSpot = bewsh;
            } else if( initial.distance(hidingSpot) > initial.distance(bewsh) ){
                bushDist = initial.distance(bewsh);
                hidingSpot = bewsh;
            }
        }

        // items as input
        int items = Integer.parseInt(readLine(scanner));

        String cheapestItem = "";
        int itemCost = 100000;

        for (int i = 0; i < items; i++) {
            String itemStats = readLine(scanner);
            System.err.println("item stats " + itemStats);
            String[] inputs = itemStats.split(" ");
            String name = inputs[0];
            int cost = Integer.parseInt(inputs[1]);
            int damage = Integer.parseInt(inputs[2]);
            int health = Integer.parseInt(inputs[3]);
            int maxHealth = Integer.parseInt(inputs[4]);
            int mana = Integer.parseInt(inputs[5]);
            int maxMana = Integer.parseInt(inputs[6]);
            int moveSpeed = Integer.parseInt(inputs[7]);
            int manaRegeneration = Integer.parseInt(inputs[8]);
            int isPotion = Integer.parseInt(inputs[9]); // 1 == potion
            if(itemCost > cost && isPotion == 0){
                cheapestItem = name;
                itemCost = cost;
            }
        }

        while (true) {
            myHeroes.clear();
            enemyHeroes.clear();
            myUnits.clear();
            enemyUnits.clear();
            creepCamps.clear();
            Unit target = null;

            int gold = Integer.parseInt(readLine(scanner));
            int enemyGold = Integer.parseInt(readLine(scanner));
            int roundType = Integer.parseInt(readLine(scanner));
            int entities = Integer.parseInt(readLine(scanner));

            for (int i = 0; i < entities; i++) {
                String[] inputs = readLine(scanner).split(" ");
                int id = Integer.parseInt(inputs[0]);
                int team = Integer.parseInt(inputs[1]);
                String type = inputs[2];
                int x = Integer.parseInt(inputs[3]);
                int y = Integer.parseInt(inputs[4]);
                int range = Integer.parseInt(inputs[5]);
                int health = Integer.parseInt(inputs[6]);
                int maxHealth = Integer.parseInt(inputs[7]);
                int shield = Integer.parseInt(inputs[8]);
                int damage = Integer.parseInt(inputs[9]);
                int moveSpeed = Integer.parseInt(inputs[10]);
                int stunTime = Integer.parseInt(inputs[11]);
                int goldValue = Integer.parseInt(inputs[12]);
                int countDown1 = Integer.parseInt(inputs[13]);
                int countDown2 = Integer.parseInt(inputs[14]);
                int countDown3 = Integer.parseInt(inputs[15]);
                int mana = Integer.parseInt(inputs[16]);
                int maxMana = Integer.parseInt(inputs[17]);
                int manaRegeneration = Integer.parseInt(inputs[18]);
                String heroType = inputs[19];
                int visible = Integer.parseInt(inputs[20]); // 1 == visible
                int itemCount = Integer.parseInt(inputs[21]);


                if(type.equals("HERO")){
                    if(team==myTeam){
                        myHeroes.add(new Hero(id, team, x, y, health, mana, damage, moveSpeed, stunTime, visible, heroType, itemCount));
                    } else {
                        enemyHeroes.add(new Hero(id, team, x, y, health, mana, damage, moveSpeed, stunTime, visible, heroType, itemCount));
                    }
                } else if (team==-1) {
                    creepCamps.add(new Unit(id, team, x, y, health, damage, moveSpeed, stunTime));
                } else if (type.equals("TOWER")){
                    if(team==myTeam){
                        myTower = new Unit(id, team, x, y, health, damage, 0,0);
                    } else {
                        enemyTower = new Unit(id, team, x, y, health, damage, 0,0);
                    }
                } else {
                    if(team==myTeam){
                        myUnits.add(new Unit(id, team, x, y, health, damage, moveSpeed, stunTime));
                    } else if (team >= 0) {
                        enemyUnits.add(new Unit(id, team, x, y, health, damage, moveSpeed, stunTime));
                    }
                }
            }

            if( target == null && enemyUnits.size() > 0 && myUnits.size() > 0 && myHeroes.get(0).distance(enemyUnits.get(0)) > myUnits.get(0).distance(enemyUnits.get(0)) ) { target = enemyUnits.get(0); }
            else target = myTower;

            // groot choice
            if (gold == lastGold + 100) groot = false;

            if( groot && myHeroes.size() > 1 && ( myHeroes.get(0).itemCount < 4 || myHeroes.get(1).itemCount < 4 ) && myHeroes.get(0).health > 600 && myHeroes.get(1).health > 600 )
                if(creepCamps.size() >= 1) {
                    double dist = 99999;
                    for (Unit creep : creepCamps) {
                        if( myTower.distance(creep) < dist && myTower.distance(creep) < 800)
                        if (enemyHeroes.size() > 0 && enemyHeroes.get(0).distance(creep) > 500)
                        {
                            target = creep;
                            dist = myTower.distance(creep);
                        } else {}
                    }
                } else {}
            else
            if (creepCamps.size() >= 1) {
                    for (Unit creep : creepCamps) {
                        if( creep.health < 400 && myHeroes.get(0).distance(creep) < 400) {
                            target = creep;
                        } else if( creep.health < 400 && enemyHeroes.size() > 0 ) {
                            target = enemyHeroes.get(0);
                        }
                    }
                }

            if ( turn >2 && myHeroes.get(0).distance(enemyTower) < 600) target = myTower;

            if(roundType == -1) System.out.println("DOCTOR_STRANGE");
            else if( roundType == -2) System.out.println("IRONMAN"); // Used from bronze.
            else
                for (Hero myHero : myHeroes) {
                    if(turn < 4) {
                        System.out.println("MOVE " + (int)hidingSpot.x + " " + (int)hidingSpot.y);
                    } else if(target.team == myTeam) {
                        System.out.println("MOVE " + ((int)Math.round(target.x)) + " " + ((int)Math.round(target.y)));
                    } else if(gold >= itemCost && myHero.itemCount < 4){
                            gold-=itemCost;
                            System.out.println("BUY " + cheapestItem);
                    } else if ( target.team == -1 ) {
                        System.out.println("ATTACK " + target.id);
                    } else
                        System.out.println("ATTACK_NEAREST UNIT");
                }
            turn++;
            lastGold = gold;
        }
    }

    static String readLine(Scanner scanner) {
        String line = scanner.nextLine();
        //System.err.println(line);
        return line;
    }

    static class Point {
        double x;
        double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        double distance(Point p) {
            return Math.sqrt((this.x - p.x) * (this.x - p.x) + (this.y - p.y) * (this.y - p.y));
        }

        // Move the point to x and y
        void move(double x, double y) {
            this.x = x;
            this.y = y;
        }

        boolean isInRange(Point p, double range) {
            return p != this && distance(p) <= range;
        }
    }

    static class Unit extends Point {
        int type;
        int id;
        double vx;
        double vy;
        int health;
        int stunTime;
        int team;
        int damage;
        int moveSpeed;

        Unit(int id, int team, int x, int y, int health, int damage, int moveSpeed, int stunTime) {
            super(x, y);

            this.id = id;
            this.team = team;
            this.health = health;
            this.damage = damage;
            this.moveSpeed = moveSpeed;
            this.stunTime = stunTime;

            vx = 0.0;
            vy = 0.0;
            this.stunTime = stunTime;
        }
    }

    static class Hero extends Unit {
        boolean isDead;
        String name;
        double mana;
        int itemCount;

        Hero(int id, int team, int x, int y, int health, double mana, int damage, int moveSpeed, int stunTime, int visible, String heroType, int itemCount) {
            super(id, team, x, y, health, damage, moveSpeed, stunTime);
            this.name = heroType;
            this.mana = mana;
            this.itemCount = itemCount;
        }
    }

    static class Item{
        String name;
        int cost;
        boolean isPotion;
        int health;
        int mana;
        int maxMana;
        int maxHealth;
        int damage;
        int moveSpeed;

        Item(String name, int cost, boolean isPotion) {
            this.name = name;
            this.cost = cost;
            this.isPotion = isPotion;
        }
    }

    static class Doodah extends Point{
        int radius;
        Doodah(double x, double y, int radius){
            super(x,y);
            this.radius = radius;
        }
    }
}
