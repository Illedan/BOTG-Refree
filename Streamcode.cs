using System;
using System.Linq;
using System.IO;
using System.Text;
using System.Collections;
using System.Collections.Generic;

/**
 * Made with love by AntiSquid, Illedan and Wildum.
 * You can help children learn to code while you participate by donating to CoderDojo.
 **/
class Player
{
    public static List<Item> Items = new List<Item>();
    private static int myTeam;
    private static int gold;
    static void Main(string[] args)
    {
        string[] inputs;
        myTeam = int.Parse(Console.ReadLine());
        int bushAndSpawnPointCount = int.Parse(Console.ReadLine()); // usefrul from wood1, represents the number of bushes and the number of places where neutral units can spawn
        for (int i = 0; i < bushAndSpawnPointCount; i++)
        {
            inputs = Console.ReadLine().Split(' ');
            string entityType = inputs[0]; // BUSH, from wood1 it can also be SPAWN
            int x = int.Parse(inputs[1]);
            int y = int.Parse(inputs[2]);
            int radius = int.Parse(inputs[3]);
        }
        int itemCount = int.Parse(Console.ReadLine()); // useful from wood2
        for (int i = 0; i < itemCount; i++)
        {
            Items.Add(new Item(Console.ReadLine().Split(' ')));
        }

        // game loop
        while (true)
        {
            gold = int.Parse(Console.ReadLine());
            int enemyGold = int.Parse(Console.ReadLine());
            int roundType = int.Parse(Console.ReadLine()); // a positive value will show the number of heroes that await a command
            int entityCount = int.Parse(Console.ReadLine());
            var units = new List<Unit>();
            for (int i = 0; i < entityCount; i++)
            {
                inputs = Console.ReadLine().Split(' ');
                string unitType = inputs[2]; // UNIT, HERO, TOWER, can also be GROOT from wood1
                if (unitType.Equals("UNIT")) units.Add(new Unit(inputs));
                else if (unitType.Equals("HERO")) units.Add(new Hero(inputs));
                else if (unitType.Equals("TOWER")) units.Add(new Tower(inputs));
            }
            var myHeroes = units.OfType<Hero>().Where(u => u.team == myTeam).ToArray();
            if (roundType == -1) Console.WriteLine("IRONMAN");
            else if (roundType == -2) Console.WriteLine("VALKYRIE");
            else
            {
                for (int n = 0; n < roundType; n++)
                {
                    var currentHero = myHeroes[n];
                    if (currentHero.heroType.Equals("IRONMAN"))
                    {
                        var target = units.OfType<Hero>().FirstOrDefault(u => u.team == 1 - myTeam);
                        if (target != null && currentHero.mana >= 60 && target.Distance(currentHero) < 900 && currentHero.countDown2 == 0)
                        {
                            Console.WriteLine("FIREBALL " + target.X + " " + target.Y);
                        }
                        else
                        {
                            HandleHero(units, currentHero);
                        }
                    }
                    else if (currentHero.heroType.Equals("VALKYIRE"))
                    {
                        var target = units.OfType<Hero>().FirstOrDefault(u => u.team == 1 - myTeam);
                        if (target != null && currentHero.mana >= 20 && target.Distance(currentHero) < 155 && currentHero.countDown1 == 0)
                        {
                            Console.WriteLine("SPEARFLIP " + target.unitId);
                        }
                        else
                        {
                            HandleHero(units, currentHero);
                        }
                    }
                    else
                    {
                        HandleHero(units, currentHero);
                    }
                }
            }
        }
    }

    public static void HandleHero(List<Unit> units, Hero myHero)
    {
        var myTower = units.First(u => u.team == myTeam && u is Tower);
        var enemyTower = units.First(u => u.team == 1 - myTeam && u is Tower);
        var myUnits = units.Where(u => u.team == myTeam && !(u is Tower) && !(u is Hero)).ToList();
        var enemyUnits = units.Where(u => u.team == 1 - myTeam && !(u is Tower) && !(u is Hero)).ToList();

        var bestItem = Items.Where(i => i.itemCost <= gold && i.itemCost > 200).OrderByDescending(i => i.damage * 50 + i.health).FirstOrDefault();
        var potion = Items.FirstOrDefault(i => i.isPotion && i.itemCost <= gold && i.health > 0);
        if (myHero.itemsOwned < 3 && bestItem != null)
        {
            gold -= bestItem.itemCost;
            Console.WriteLine("BUY " + bestItem.itemName);
        }
        else if (potion != null && myHero.health < 200)
        {
            gold -= potion.itemCost;
            Console.WriteLine("BUY " + potion.itemName);
        }
        else if (enemyTower.Distance(myHero) <= enemyTower.attackRange + myHero.movementSpeed && myUnits.Count(u => u.Distance(enemyTower) < enemyTower.attackRange) > 1)
        {
            Console.WriteLine("MOVE " + myTower.X + " " + myTower.Y);
        }
        else
        {
            var closestEnemyHero = units.OfType<Hero>().FirstOrDefault(
                u => u.team == 1 - myTeam && u.Distance(myHero) < myHero.attackRange + 0.3 * myHero.movementSpeed);
            var lowestUnit = enemyUnits.OrderBy(e => e.Distance(myHero)).FirstOrDefault();
            var farmableUnit = enemyUnits.FirstOrDefault(e => e.health <= myHero.attackDamage);
            if (closestEnemyHero != null && enemyUnits.Count(e => e.Distance(myHero) < 200) < 3)
            {
                Console.WriteLine("ATTACK " + closestEnemyHero.unitId);
            }
            else if (farmableUnit != null)
            {
                Console.WriteLine("ATTACK " + farmableUnit.unitId);
            }
            else if (myUnits.Any() && lowestUnit != null)
            {
                var closestAllied = myUnits.OrderBy(e => e.Distance(lowestUnit)).First();
                var target = closestAllied.MoveTowards(myTower, 50);
                Console.WriteLine("MOVE_ATTACK " + target.X + " " + target.Y + " " + lowestUnit.unitId);
            }
            else if (myUnits.Any())
            {
                var closestAllied = myUnits.OrderBy(e => e.Distance(enemyTower)).First();
                var target = closestAllied.MoveTowards(myTower, 50);
                Console.WriteLine("MOVE " + target.X + " " + target.Y);
            }
            else
            {
                var towerTarget = myHero.MoveTowards(myTower, 100);
                Console.WriteLine("MOVE " + towerTarget.X + " " + towerTarget.Y);
            }
        }
    }

    public class Item
    {
        public string itemName;
        public int itemCost;
        public int damage;
        public int health;
        public int maxHealth;
        public int mana;
        public int maxMana;
        public int moveSpeed;
        public int manaRegeneration;
        public bool isPotion;
        public Item(string[] inputs)
        {
            itemName = inputs[0]; // contains keywords such as BRONZE, SILVER and BLADE, BOOTS connected by "_" to help you sort easier
            Console.Error.WriteLine(string.Join(" ", inputs));
            itemCost = int.Parse(inputs[1]); // BRONZE items have lowest cost, the most expensive items are LEGENDARY
            damage = int.Parse(inputs[2]); // keyword BLADE is present if the most important item stat is damage
            health = int.Parse(inputs[3]);
            maxHealth = int.Parse(inputs[4]);
            mana = int.Parse(inputs[5]);
            maxMana = int.Parse(inputs[6]);
            moveSpeed = int.Parse(inputs[7]); // keyword BOOTS is present if the most important item stat is moveSpeed
            manaRegeneration = int.Parse(inputs[8]);
            isPotion = int.Parse(inputs[9]) == 1; // 0 if it's not instantly consumed
        }
    }

    public class Tower : Unit
    {
        public Tower(string[] inputs) : base(inputs) { }
    }

    public class Hero : Unit
    {
        public int countDown1;
        public int countDown2;
        public int countDown3;
        public int mana;
        public int maxMana;
        public int manaRegeneration;
        public string heroType;
        public int isVisible;
        public int itemsOwned;
        public Hero(string[] inputs) : base(inputs)
        {
            countDown1 = int.Parse(inputs[13]); // all countDown and mana variables are useful starting in bronze
            countDown2 = int.Parse(inputs[14]);
            countDown3 = int.Parse(inputs[15]);
            mana = int.Parse(inputs[16]);
            maxMana = int.Parse(inputs[17]);
            manaRegeneration = int.Parse(inputs[18]);
            heroType = inputs[19]; // DEADPOOL, VALKYRIE, DOCTOR_STRANGE, HULK, IRONMAN
            isVisible = int.Parse(inputs[20]); // 0 if it isn't
            itemsOwned = int.Parse(inputs[21]); // useful from wood1
            attackTime = 0.1;
        }
    }

    public class Unit : Position
    {
        public int unitId;
        public int team;
        public int attackRange;
        public int health;
        public int maxHealth;
        public int shield;
        public int attackDamage;
        public int movementSpeed;
        public int stunDuration;
        public int goldValue;
        public double attackTime = 0.2;
        public Unit(string[] inputs) : base(int.Parse(inputs[3]), int.Parse(inputs[4]))
        {
            unitId = int.Parse(inputs[0]);
            team = int.Parse(inputs[1]);
            attackRange = int.Parse(inputs[5]);
            health = int.Parse(inputs[6]);
            maxHealth = int.Parse(inputs[7]);
            shield = int.Parse(inputs[8]); // useful in bronze
            attackDamage = int.Parse(inputs[9]);
            movementSpeed = int.Parse(inputs[10]);
            stunDuration = int.Parse(inputs[11]); // useful in bronze
            goldValue = int.Parse(inputs[12]);
        }

        public double AttackTime(Unit unit)
        {
            var dist = Distance(unit);
            double t = 0;
            if (dist > unit.attackRange)
            {
                t = (dist - attackRange) / movementSpeed;
                dist = attackRange;
            }

            t += attackTime;
            if (attackRange > 150)
            {
                t += attackTime * (dist / attackRange);
            }

            return t;
        }

    }

    public class Position
    {
        public double X, Y;

        public Position(double x, double y)
        {
            X = x;
            Y = y;
        }

        public double Distance(Position p2)
        {
            return Math.Sqrt(Math.Pow(p2.X - X, 2) + Math.Pow(p2.Y - Y, 2));
        }

        public Position MoveTowards(Position p2, int speed)
        {
            var dist = Distance(p2);
            if (dist > 0.0001) return new Position((p2.X - X) / dist * speed + X, (p2.Y - Y) / dist * speed + Y);
            return this;
        }
    }
}
