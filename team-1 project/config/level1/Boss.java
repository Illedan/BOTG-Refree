import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.*;

/*

wood 6 boss

Picks Hulk. Attacks periodically.

This league is meant just to get used with the most basic mechanics.

*/


class Player {

    public static void main(String args[]) {

        int turn = 0;

        Scanner in = new Scanner(System.in);
        int myTeam = in.nextInt();
        int bushAndSpawnPointCount = in.nextInt(); // useful from wood1, represents the number of bushes and the number of places where neutral units can spawn
        for (int i = 0; i < bushAndSpawnPointCount; i++) {
            String entityType = in.next(); // BUSH, from wood1 it can also be SPAWN
            int x = in.nextInt();
            int y = in.nextInt();
            int radius = in.nextInt();
        }
        int itemCount = in.nextInt(); // useful from wood2
        for (int i = 0; i < itemCount; i++) {
            String itemName = in.next(); // contains keywords such as BRONZE, SILVER and BLADE, BOOTS connected by "_" to help you sort easier
            int itemCost = in.nextInt(); // BRONZE items have lowest cost, the most expensive items are LEGENDARY
            int damage = in.nextInt(); // keyword BLADE is present if the most important item stat is damage
            int health = in.nextInt();
            int maxHealth = in.nextInt();
            int mana = in.nextInt();
            int maxMana = in.nextInt();
            int moveSpeed = in.nextInt(); // keyword BOOTS is present if the most important item stat is moveSpeed
            int manaRegeneration = in.nextInt();
            int isPotion = in.nextInt(); // 0 if it's not instantly consumed
        }

        // game loop
        while (true) {
            int gold = in.nextInt();
            int enemyGold = in.nextInt();
            int roundType = in.nextInt(); // a positive value will show the number of heroes that await a command
            int entityCount = in.nextInt();
            for (int i = 0; i < entityCount; i++) {
                int unitId = in.nextInt();
                int team = in.nextInt();
                String unitType = in.next(); // UNIT, HERO, TOWER, can also be GROOT from wood1
                int x = in.nextInt();
                int y = in.nextInt();
                int attackRange = in.nextInt();
                int health = in.nextInt();
                int maxHealth = in.nextInt();
                int shield = in.nextInt(); // useful in bronze
                int attackDamage = in.nextInt();
                int movementSpeed = in.nextInt();
                int stunDuration = in.nextInt(); // useful in bronze
                int goldValue = in.nextInt();
                int countDown1 = in.nextInt(); // all countDown and mana variables are useful starting in bronze
                int countDown2 = in.nextInt();
                int countDown3 = in.nextInt();
                int mana = in.nextInt();
                int maxMana = in.nextInt();
                int manaRegeneration = in.nextInt();
                String heroType = in.next(); // DEADPOOL, VALKYRIE, DOCTOR_STRANGE, HULK, IRONMAN
                int isVisible = in.nextInt(); // 0 if it isn't
                int itemsOwned = in.nextInt(); // useful from wood1
            }

            if(roundType < 0) {
                System.out.println("IRONMAN");
            } else {
                System.out.println("ATTACK_NEAREST HERO");
            }

            turn++;
        }
    }
}
