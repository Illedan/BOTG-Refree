package com.codingame.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.codingame.gameengine.core.AbstractPlayer;

public class Player extends AbstractPlayer {
    public List<Hero> heroes = new ArrayList<>();
    public Tower tower;
    public int gold;
    public int denies;
    public int unitKills;

    public void addHero(Hero hero) {
        this.heroes.add(hero);
    }

    public void handlePlayerOutputs(String[] outputs) throws Exception{
        List<Hero> internalHeroes = new ArrayList<>(heroes);
        if(outputs.length==2 && outputs[1].startsWith("SELL") && heroesAlive() == 2){ //Flip direction to allow other way commands
            Hero first = internalHeroes.get(0);
            internalHeroes.remove(0);
            internalHeroes.add(first);

            String temp = outputs[0];
            outputs[0] = outputs[1];
            outputs[1] = temp;
        }

        int outputCounter = 0;
        for (int i = 0; i < internalHeroes.size(); i++) {
            Hero hero = internalHeroes.get(i);
            if (hero.isDead) {
                continue;
            }

            if(hero.stunTime > 0){
                outputCounter++;
                continue;
            }

            String roundOutput = outputs[outputCounter++];
            doHeroCommand(roundOutput, hero);
        }
    }

    private void doHeroCommand(String roundOutput, Hero hero){
        String[] roundOutputSplitted = roundOutput.split(";", 2);

        try{

            String message = roundOutputSplitted.length > 1 ? roundOutputSplitted[1] : "";
            Const.viewController.addMessageToHeroHud(hero, message);

            String [] outputValues = roundOutputSplitted[0].split(" ");
            String command = outputValues[0];
            int arguments = outputValues.length-1;

            // Verification
            boolean allNumbers = true;
            for(int num = 1; num < outputValues.length;num++){
                if(!Utilities.isNumber(outputValues[num])) allNumbers = false;
            }

            if (command.equals("MOVE_ATTACK") && arguments == 3 && allNumbers) {
                // MOVE_ATTACK x y unitID
                double x = Double.parseDouble(outputValues[1]);
                double y = Double.parseDouble(outputValues[2]);
                int id = Integer.parseInt(outputValues[3]);
                Point target = new Point(x, y);
                hero.runTowards(target);
                Unit unit = Const.game.getUnitOfId(id);
                if (unit != null) {
                    Const.game.events.add(new Event.DelayedAttackEvent(unit, hero, Utilities.timeToReachTarget(hero, target, hero.moveSpeed)));
                }else printError("Can't attack unit of id: " + id);
            }

            else if (command.equals("MOVE") && arguments == 2  && allNumbers) {
                double x = Double.parseDouble(outputValues[1]);
                double y = Double.parseDouble(outputValues[2]);
                hero.runTowards(new Point(x, y));
            }

            else if (command.equals("ATTACK_NEAREST") && arguments == 1  && !allNumbers) {
                String unitType = outputValues[1];
                double dist = 99999999;
                Unit toHit = hero.findClosestOnOtherTeam(unitType);
                if (toHit != null) {
                    hero.attackUnitOrMoveTowards(toHit, 0.0);
                }
            }

            else if (command.equals("ATTACK") && arguments == 1 && allNumbers) {
                int id = Integer.parseInt(outputValues[1]);
                Unit unit = Const.game.getUnitOfId(id);
                if (unit != null && hero.allowedToAttack(unit)) {
                    hero.attackUnitOrMoveTowards(unit, 0.0);
                }else printError("Cant attack: " + id);

            }

            else if (command.equals("BUY") && arguments == 1 && !allNumbers) {
                Item item = Const.game.items.get(outputValues[1]);

                if(item == null){
                    printError(getNicknameToken() + " tried to buy item: " + outputValues[1] + ", but it does not exist");
                }else if(gold < item.cost){
                    printError(getNicknameToken() +" can't afford " + outputValues[1]);
                }else if(hero.items.size()>=Const.MAXITEMCOUNT){
                    printError("Can't have more than " + Const.MAXITEMCOUNT + " items. " + (item.isPotion? "Potions need a free item slot to be bought.":"" ));
                }else{
                    hero.addItem(item);
                    gold -= item.cost;
                    Const.viewController.addItem(hero, item);
                }
            }

            else if (command.equals("SELL") && arguments == 1 && !allNumbers) {
                String itemName = outputValues[1];
                Optional<Item> foundItem = hero.items.stream().filter(currItem -> currItem.name.equals(itemName)).findFirst();

                if(foundItem == null || !foundItem.isPresent()){
                    printError("Selling not owned item " + foundItem.get().name);
                }else{
                    hero.removeItem(foundItem.get());
                    gold += Utilities.round(foundItem.get().cost*Const.SELLITEMREFUND);
                    Const.viewController.removeItem(hero, foundItem.get());
                }
            }

            else if(command.equals("WAIT") && arguments == 0 ){
                return;
            }

            else if(allNumbers)
            { // skillz
                for(Skills.SkillBase skill : hero.skills){
                    if(skill==null) continue;
                    if(command.equals(skill.skillName)){
                        if(skill.manaCost > hero.mana){
                            printError("Not enough mana to use " + skill.skillName);
                        }else if(skill.cooldown > 0){
                            printError("Skill on cooldown: " + skill.skillName+ ". Cooldown left: " + skill.cooldown);
                        } else{
                            double x = -1;
                            double y = -1;
                            int unitId = -1;
                            if(outputValues.length==3 && skill.getTargetType() == SkillType.POSITION){
                                x = Double.parseDouble(outputValues[1]);
                                y = Double.parseDouble(outputValues[2]);
                                Const.viewController.addEffect(hero, new Point(x,y), "spell", 0);
                            }
                            else if(outputValues.length==2 && skill.getTargetType() == SkillType.UNIT){
                                unitId = Integer.parseInt(outputValues[1]);
                                Unit unit = Const.game.getUnitOfId(unitId);
                                Const.viewController.addEffect(hero, unit, "spell", 0);

                                if(!hero.allowedToTarget(unit)) {
                                    printError(hero.heroType + " can't target unit with spell. Either invisible or not existing.");
                                    return;
                                }
                            }
                            else if(outputValues.length==1 && skill.getTargetType() == SkillType.SELF){ }
                            else {
                                printError(hero.heroType + " invalid number of parameters on spell. " + roundOutputSplitted[0]);
                                return;
                            }

                            hero.mana-= skill.manaCost;
                            skill.cooldown = skill.initialCooldown;
                            hero.invisibleBySkill = false;
                            skill.doSkill(Const.game, x, y, unitId);
                        }

                        return;
                    }
                }

                printError(getNicknameToken() + " tried to use a spell not found on  " + hero.heroType+ ". Input was: " + roundOutputSplitted[0]);
            }else{
                printError(getNicknameToken() + " tried to use an invalid command. Invalid parameters or name. Command was: " + roundOutputSplitted[0]);
            }
        }catch (Exception e){
            printError(getNicknameToken() + " tried to use an invalid command. Invalid parameters or name. Command was: " + roundOutputSplitted[0]);
        }
    }

    public int heroesAlive(){
        int alive = 0;
        for(Hero hero : heroes){
            if(!hero.isDead) alive++;
        }
        return alive;
    }

    @Override
    public int getExpectedOutputLines() {
        if (this.heroes.size() < Const.HEROCOUNT) return 1;
        return (int) this.heroes.stream().filter(h -> !h.isDead).count();
    }

    public int getGold() {
        return this.gold;
    }

    public void setGold(int amount) {
    	this.gold = amount;
    }

    private void printError(String message){
        Const.viewController.addSummary(message);
    }
}
