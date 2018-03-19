import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int myTeam = in.nextInt();
        int bushAndSpawnPointCount = in.nextInt(); // the number of bushes and the number of places where neutral units can spawn
        for (int i = 0; i < bushAndSpawnPointCount; i++) {
            String entityType = in.next();
            int x = in.nextInt();
            int y = in.nextInt();
            int radius = in.nextInt();
        }
        int itemCount = in.nextInt();
        for (int i = 0; i < itemCount; i++) {
            int itemCost = in.nextInt();
            int damage = in.nextInt();
            int health = in.nextInt();
            int maxHealth = in.nextInt();
            int mana = in.nextInt();
            int maxMana = in.nextInt();
            int moveSpeed = in.nextInt();
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
                int id = in.nextInt();
                int team = in.nextInt();
                String unitType = in.next();
                int x = in.nextInt();
                int y = in.nextInt();
                int range = in.nextInt();
                int health = in.nextInt();
                int maxHealth = in.nextInt();
                int shield = in.nextInt();
                int attackDamage = in.nextInt();
                int movementSpeed = in.nextInt();
                int stunDuration = in.nextInt();
                int goldValue = in.nextInt();
                int countDown1 = in.nextInt();
                int countDown2 = in.nextInt();
                int countDown3 = in.nextInt();
                int mana = in.nextInt();
                int maxMana = in.nextInt();
                int manaRegeneration = in.nextInt();
                String heroType = in.next();
                int isVisible = in.nextInt(); // 0 if it isn't
                int itemsOwned = in.nextInt();
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");


            // If roundType has a negative value then you need to output a Hero name, such as "DEADPOOL" or "VALKYRIE".
            // Else you need to output roundType number of any valid action, such as "WAIT" or "ATTACK unitId"
            System.out.println("WAIT");
            System.out.println("WAIT");
        }
    }
}
