package com.codingame.game;

import java.util.*;

public class Game {
    // following two lists will be sent individually to the players, based on what heroes are visible

    public ArrayList<Unit> allUnits = new ArrayList<>();
    public List<Bush> bushes = new ArrayList<>();
    public List<Event> events = new ArrayList<>();
    public Map<String, Item> items;
    public int round;
    public List<Hero> visibleHeroes = new ArrayList<>();
    public int forestCampCount = 0;
    public Map<Unit, List<Damage>> damages = new HashMap<>();
    public double t;

    public void handleTurn(List<Player> players) {

        // Make dummies do their actions
        for (Unit unit : allUnits) {
            if(unit.stunTime>0) continue;
            if (unit instanceof LaneUnit)
                ((LaneUnit) unit).findAction();

            else if(unit instanceof Tower)
                ((Tower)unit).findAction(allUnits);

            else if (unit instanceof Creature)
                ((Creature)unit).findAction(allUnits);
        }

        // Inform events about speed changes of moving units.
        for(Unit unit : allUnits){
            if(Math.abs(unit.vx) > Const.EPSILON || Math.abs(unit.vy) > Const.EPSILON){
                Const.viewController.addEffect(unit, unit.targetPointOfMovement(), "movement", 1.0);
                for(int i = events.size()-1; i >= 0; i--){
                    Event event = events.get(i);
                    if(event.afterAnotherEvent(unit, Event.SPEEDCHANGED, 0.0)){
                        events.remove(event);
                    }
                }
            }
        }

        while (t <= Const.ROUNDTIME) {
            if(isGameOver(players)) {
                Const.viewController.afterRound();
                return;
            }

            Event nextEvent = findNextEvent();
            if (nextEvent == null || nextEvent.t + t > Const.ROUNDTIME) {
                break;
            }

            events.remove(nextEvent);

            for (Unit unit : allUnits) {
                unit.move(nextEvent.t);
            }

            for(Event event : events){
                event.t-= nextEvent.t;
            }

            t += nextEvent.t;

            ArrayList<Event> occuringEvents = new ArrayList<>();
            occuringEvents.add(nextEvent);
            for(int i = events.size() - 1; i >= 0; i--) {
                Event event = events.get(i);
                if(event.t < Const.EPSILON && event.t+t <= Const.ROUNDTIME){
                    occuringEvents.add(event);
                    events.remove(i);
                }
            }

            Collections.sort(occuringEvents, Comparator.comparingInt(Event::getOutcome));

            for(Event currentEvent : occuringEvents){
                ArrayList<Unit> affectedUnits = currentEvent.onEventTime(t);

                handleAfterEvents(affectedUnits, currentEvent.getOutcome());
                if(t > 0){
                    if((currentEvent.getOutcome() & Event.SPEEDCHANGED) != 0 || (currentEvent.getOutcome() & Event.TELEPORTED) != 0){
                        for (Unit unit : affectedUnits){
                            Const.viewController.updateViewForUnit(unit, t);
                        }
                    }
                }
            }

            handleDamage();
        } // end while

        for(Event event : events){
            event.t-= 1.0 - t;
        }

        for (Unit unit : allUnits) {
            unit.move(1.0 - t);
        }

        for(int i = events.size()-1; i >= 0; i--){
            Event event = events.get(i);
            if(!event.useAcrossRounds()){
                events.remove(event);
            }
        }

        for (Unit unit : allUnits) {
            unit.afterRound();
        }

        updateVisibility(players);
        Const.viewController.afterRound();
    }

    private void handleDamage(){
        for (Map.Entry<Unit, List<Damage>> entry : damages.entrySet()) {
            Unit target = entry.getKey();
            List<Damage> dmgs = entry.getValue();
            int totalDamage = 0;
            boolean anyHero = target instanceof Hero;

            Unit highestDamageUnit = null;

            // Used when 2 meele attacks groot
            Unit otherAttacker = null;

            int highestDmg = 0;

            for(Damage dmg : dmgs){
                totalDamage += dmg.damage;
                anyHero = anyHero || dmg.attacker instanceof Hero;

                // hero becomes visible if he damages any on enemy team
                if (dmg.attacker instanceof Hero && target.player != null && target.player != dmg.attacker.player) {
                    ((Hero) dmg.attacker).visibilityTimer = 2;
                    ((Hero) dmg.attacker).visible = true;
                    dmg.attacker.invisibleBySkill = false;
                }else if(dmg.attacker instanceof Hero)
                    dmg.attacker.invisibleBySkill = false;

                // Highest damage or attackers advantage.
                if(dmg.damage > highestDmg || (dmg.damage == highestDmg && dmg.attacker.team == 1 - dmg.target.team)){
                    highestDamageUnit = dmg.attacker;
                    highestDmg = dmg.damage;
                    otherAttacker = null;
                }else if(dmg.damage == highestDmg && dmg.target.team==-1 && dmg.attacker.team != highestDamageUnit.team){
                    otherAttacker = dmg.attacker;
                }
            }
            if(totalDamage > 0 && target.getShield() > 0) {
                int val = Math.min(totalDamage, target.getShield());
                target.adjustShield(val);
                totalDamage -= val;
            }

            target.health = Math.min(target.health - totalDamage, target.maxHealth);
            // updates health of the unit in viewer
            if(target instanceof Hero) {
                Const.viewController.updateEntityTooltip(target.sprite, "health: " + target.health+
                                                  "\nteam: " + target.team);
            }
            if(anyHero){
                Const.viewController.displayDamages(target, totalDamage, t);
            }

            if(target.health <= 0) {
                target.isDead = true;
                Const.viewController.updateEntityTooltip(target.sprite, "0");

                UnitKilledState state = highestDamageUnit instanceof Hero ? UnitKilledState.farmed : UnitKilledState.normal;

                if(highestDamageUnit.team == target.team){
                    target.goldValue=0;
                    if(highestDamageUnit.player != null)
                        highestDamageUnit.player.denies++;

                    state = UnitKilledState.denied;
                }

                if (target instanceof Hero) {
                    // dead men tell no tales
                    Const.viewController.addMessageToHeroHud((Hero)target, "");

                    if (highestDamageUnit instanceof LaneUnit || highestDamageUnit instanceof Tower) {
                        target.goldValue = target.goldValue/2; // hero lost half of its value if killed by creep or tower
                    }else if (highestDamageUnit instanceof Creature) {
                        target.goldValue = 0;
                    }
                } else if (highestDamageUnit instanceof Hero && highestDamageUnit.team != target.team) {
                    if(highestDamageUnit.player != null)
                        highestDamageUnit.player.unitKills++;
                }else{
                    target.goldValue = 0;
                }

                if(otherAttacker != null && otherAttacker instanceof Hero){
                    otherAttacker.player.unitKills++;
                    otherAttacker.player.gold += target.goldValue;
                }

                if(highestDamageUnit.player != null)
                    highestDamageUnit.player.gold += target.goldValue;

                Const.game.allUnits.remove(target);
                Const.viewController.killUnit(target, state);


            }else if (target instanceof Creature) {
                Creature c = (Creature) target;
                if (c.state.equals(CreatureState.peacefull)) {
                    c.state = CreatureState.aggressive;
                }
            }

            ArrayList<Unit> affectedUnits = new ArrayList<>();
            affectedUnits.add(target);
            handleAfterEvents(affectedUnits, Event.LIFECHANGED);
        }

        damages.clear();
    }

    private void handleAfterEvents(ArrayList<Unit> affectedUnits, int outcome){
        for (int i = events.size() - 1; i >= 0; i--) {
            Event event = events.get(i);
            for (Unit unit : affectedUnits){
                if (event.afterAnotherEvent(unit, outcome, t) || event.t < 0) {
                    events.remove(event);
                }
            }
        }
    }

    // updates visibility of heroes based on their positions in relation to bushes, trees and other heroes
    private void updateVisibility(List<Player> players) {
        visibleHeroes.clear();
        for (Player player : players) {
            for (Hero hero : player.heroes) {
                if(hero.isDead) continue;
                visibleHeroes.add(hero);
                if(!hero.invisibleBySkill) {
                    if(hero.visible) hero.becomingInvis = true;
                    else hero.becomingInvis = false;
                    hero.visible = true;
                } else{
                    if(hero.visible) hero.becomingInvis = true;
                    else hero.becomingInvis = false;
                    hero.visible = false;
                }
            }
        }

        List<Hero> hideout = new ArrayList<>();
        List<Integer> teams = new ArrayList<>();
        for (Bush bush : bushes) {
            hideout.clear();
            teams.clear();
            if(visibleHeroes.size() == 0) break;
            for (Hero hero : visibleHeroes) {
                if(hero.visibilityTimer > 0) continue;
                if( hero.distance(bush) <= bush.radius ) {
                    hideout.add(hero);
                    if (!teams.contains(hero.team)) teams.add(hero.team);
                }
            }
            if (teams.size() == 1) {
                for (Hero hero : hideout) {
                    hero.visible = false;
                    visibleHeroes.remove(hero);
                }
            }
        }
    }


    public boolean isGameOver(List<Player> players){
        for(Player player : players){
            if(player.tower.isDead) return true;
            if(player.heroesAlive()==0) return true;
        }

        return false;
    }

    Unit getUnitOfId(int id) {
        for (Unit unit : allUnits) {
            if (unit.id == id) {
                return unit;
            }
        }

        return null;
    }

    Event findNextEvent() {
        double firstTime = Const.ROUNDTIME;
        Event nextEvent = null;
        for (Event event : events) {
            if (event.t < firstTime && event.t >= 0.0) {
                nextEvent = event;
                firstTime = event.t;
            }
        }

        return nextEvent;
    }

    public void beforeTurn(int turn, List<Player> players) {
        t = 0.0;
        round = turn;

        if (turn % Const.SPAWNRATE == Const.HEROCOUNT) {
            spawnUnits(players);
        }

        if(turn % Const.NEUTRALSPAWNRATE == Const.NEUTRALSPAWNTIME+Const.HEROCOUNT) spawnForestCreatures();
    }

    void spawnUnits(List<Player> players) {
        for (Player player: players) {
            for(int i=0;i < Const.MELEE_UNIT_COUNT; i++) {
                allUnits.add(Factories.generateUnit(0, players.indexOf(player), i, player));
            }
            for(int i=0;i < Const.RANGED_UNIT_COUNT; i++) {
                allUnits.add(Factories.generateUnit(1, players.indexOf(player), round/Const.SPAWNRATE % 3, player));
            }
        }
    }

    void initialize(List<Player> players){
        setupGame(players);
        items = Factories.createItems(setupGold(players));
    }

    int setupGold(List<Player> players) {
        int amount = (int) (Const.random.nextDouble() * 551 + 100);
        for (Player p : players) {
            p.setGold(amount);
        }
        return amount;
    }

    void setupGame(List<Player> players) {
        allUnits.add(Factories.generateTower(players.get(0), 0));
        allUnits.add(Factories.generateTower(players.get(1), 1));

        if(Const.REMOVEFORESTCREATURES){
            forestCampCount = 0;
        }else{
            spawns = Const.mapFactory.generateSpawns();
            forestCampCount = spawns.length;
            creatures = new Creature[forestCampCount];
            amplitude = new double[forestCampCount];
            Arrays.fill(amplitude, 1.0);
        }

        List<Bush> tempBushes = Const.mapFactory.generateBushes(spawns);

        // In lower leagues we ignore bushes. But lets add the visuals because it's pretty =)
        if(!Const.IGNOREBUSHES) this.bushes.addAll(tempBushes);
    }


    // random spawn point placement
    CreatureSpawn[] spawns = {};
    Creature[] creatures = new Creature[0];
    double[] amplitude = {};

    void spawnForestCreatures(){

        for(int i = 0; i < forestCampCount; i++) {
            if(creatures[i]==null || creatures[i].isDead){
                allUnits.add(creatures[i] = Factories.generateCreature(spawns[i],  amplitude[i]));
                amplitude[i]*=1.2;
            }
        }
    }
}
