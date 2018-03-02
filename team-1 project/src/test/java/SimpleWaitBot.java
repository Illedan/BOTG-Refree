import java.util.*;

public class SimpleWaitBot {
    public static void main(String[] args) {
        String[] inputs;

        Scanner scanner = new Scanner(System.in);
        int myTeam = Integer.parseInt(readLine(scanner));
        int spawnPoints = Integer.parseInt(readLine(scanner));

        for (int i = 0; i < spawnPoints; i++) {
            inputs = readLine(scanner).split(" ");
            String type = inputs[0]; // BUSH or GROOT
            int x = Integer.parseInt(inputs[1]);
            int y = Integer.parseInt(inputs[2]);
            int radius = Integer.parseInt(inputs[3]);
        }

        int items = Integer.parseInt(readLine(scanner));
        for (int i = 0; i < items; i++) {
            inputs = readLine(scanner).split(" ");
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
        }

        while (true) {

            int gold = Integer.parseInt(readLine(scanner));
            int enemyGold = Integer.parseInt(readLine(scanner));
            int roundType = Integer.parseInt(readLine(scanner));
            int entities = Integer.parseInt(readLine(scanner));

            for (int i = 0; i < entities; i++) {
                inputs = readLine(scanner).split(" ");
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
                int isVisible = Integer.parseInt(inputs[20]); // 1 == visible
                int itemCount = Integer.parseInt(inputs[21]);
            }

            if(roundType == -1) System.out.println("IRONMAN");
            else if( roundType == -2) System.out.println("HULK"); // Used from wood1.
            else
            {
                for(int i = 0; i < roundType; i++){
                    System.out.println("WAIT;BotG, best game");
                }
            }
        }
    }
    private static String readLine(Scanner scanner){
        String s = scanner.nextLine();
        System.err.println(s);
        return s;
    }
}
