import java.util.ArrayList;

import com.codingame.game.*;

public class PrintStats {

    public static void main(String[] args) {
        Const.game = new Game();
        Const.viewController = new UnitTests.ViewControllerTest();
        ArrayList<Unit> units = new ArrayList<>();
        units.add(Factories.generateTower(new Player(), 0));
        units.add(Factories.generateUnit(0, 0, 0, new Player()));
        units.add(Factories.generateUnit(1, 0, 0, new Player()));
        units.add(Factories.generateCreature(new Point(0,0), 1.0));
        units.add(Factories.generateHero("VALKYRIE", new Player(), new Point(0,0)));
        units.add(Factories.generateHero("DEADPOOL", new Player(), new Point(0,0)));
        units.add(Factories.generateHero("IRONMAN", new Player(), new Point(0,0)));
        units.add(Factories.generateHero("DOCTOR_STRANGE", new Player(), new Point(0,0)));
        units.add(Factories.generateHero("HULK", new Player(), new Point(0,0)));

        //TODO: add html / markdown / ascii or whatever format CG uses to create a nice little table for the rules
        System.out.println("### UNITS:");
        System.out.println();
        System.out.println("|TYPE|MEELE|HEALTH|DAMAGE|RANGE|MOVESPEED|ATTACKTIME|GOLD|MANA|MANAREG|");
        System.out.println("|--|--|--|--|--|--|--|--|--|--|");
        for(Unit unit : units){
            System.out.println("|"+ getType(unit) + "|" + unit.isMelee() + "|" + unit.health + "|" + unit.damage + "|" + unit.range + "|" + unit.moveSpeed + "|" + unit.attackTime + "|" + unit.goldValue + "|" + getHeroStats(unit)+"|");
        }

        System.out.println();
        System.out.println("### SKILLS:");
        System.out.println();
        System.out.println("|HERO|NAME|MANACOST|COOLDOWN|DURATION|CASTTIME|RANGE|TARGETTYPE|TARGETTEAM|" );
        System.out.println("|--|--|--|--|--|--|--|--|--|");
        for(Unit unit : units){
            if(unit instanceof Hero){
                Hero hero = (Hero)unit;
                for(Skills.SkillBase skill : hero.skills){
                    System.out.println("|" + hero.heroType + "|"+skill.skillName + "|" + skill.manaCost + "|" + skill.initialCooldown + "|"+ skill.getDuration() + "|" + skill.CastTime() + "|" + skill.range + "|" + skill.getTargetType()+"|"+skill.getTargetTeam()+"|");
                }
            }
        }

        System.out.println();
        System.out.println("### ITEMS:");
        System.out.println();
        System.out.println("|STAT|PRICE|MAXCOUNT|");
        System.out.println("|--|--|--|");
        for(String stat : Const.STATS){
            System.out.println("|"+stat+"|"+MapFactory.getPrice(stat)+"|"+MapFactory.getLimit(stat)+"|");
        }

        System.out.println();
        System.out.println("### ITEM PRICERANGES:");
        System.out.println();
        System.out.println("Every game includes 3 potions: 500 health, 100 health and 50 mana.");
        System.out.println();
        System.out.println("Items in each pricerange: " + Const.NB_ITEMS_PER_LEVEL + ".");
        System.out.println();


        System.out.println();
        System.out.println("|PRICE RANGE|MIN|MAX|" );
        System.out.println("|--|--|--|");
        for(int i = 0; i < MapFactory.ItemLevels.length; i++){
            String prefix = "";
            if(i==0) prefix += "Bronze";
            else if(i==1) prefix += "Silver";
            else if(i==2) prefix += "Golden";
            else if(i==3) prefix += "Legendary";
            System.out.println("|"+prefix+"|" + MapFactory.ItemLevels[i][0] + "|" + MapFactory.ItemLevels[i][1]+"|");
        }

        System.out.println();
        System.out.println("Formula used to calculate end price. This is done after the creation given a price range:\n```\nMath.max(Math.ceil(totalCost/2), Math.ceil(totalCost-(totalCost*totalCost/6000)))\n```");

    }

    private static String getHeroStats(Unit unit){
        if(unit instanceof Hero){
            Hero hero = (Hero)unit;
            return hero.maxMana + "|" + hero.manaregeneration;
        }

        return "0|0";
    }

    private static String getType(Unit unit){
        if(unit instanceof Hero) return "HERO-"+((Hero)unit).heroType;
        return unit.getType();
    }
}
