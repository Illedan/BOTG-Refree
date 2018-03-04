package com.codingame.game;

import java.io.Console;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.Tooltip;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

import tooltipModule.TooltipModule;

public class Referee extends AbstractReferee {
    @Inject private GameManager<Player> gameManager;
    @Inject private GraphicEntityModule entityManager;
    @Inject private TooltipModule tooltipModule;
    private final int LostScore = -100000000;

    public static void setupLeague(int league){
        switch (league){
            case 0:{
                Const.IGNOREITEMS = true;
                Const.HEROCOUNT = 1;
                Const.IGNORESKILLS = true;
                Const.REMOVEFORESTCREATURES = true;
                Const.TOWERHEALTHSCALE = 0.5;
                Const.IGNOREBUSHES = true;
                Const.Rounds = 200;
            }case 1:{
                Const.HEROCOUNT = 1;
                Const.IGNORESKILLS = true;
                Const.REMOVEFORESTCREATURES = true;
                Const.TOWERHEALTHSCALE = 0.5;
                Const.IGNOREBUSHES = true;
                Const.Rounds = 200;
            }case 2:{
                Const.IGNORESKILLS = true;
                Const.Rounds = 200;
            }default:{
                //normal.
            }
        }
    }


    @Override
    public Properties init(Properties params) {
        long seed = 42;
        try{
            seed = Long.parseLong(params.getProperty("seed"));
        }catch (Exception e){
            seed = new Random().nextLong();
        }
        Const.random = new Random(seed);

        setupLeague(gameManager.getLeagueLevel() - 1); // gameManager.getLeagueLevel() - 1

        Const.viewController.initialize(entityManager, tooltipModule, gameManager.getActivePlayers(), gameManager);
        gameManager.setMaxTurns(Const.Rounds);
        entityManager.createSprite().setImage(Const.BACKGROUND).setAnchor(0).setZIndex(-1000);
        Const.game.initialize(gameManager.getActivePlayers());

        for (Unit unit : Const.game.allUnits) {
            if (unit instanceof Tower) gameManager.getActivePlayers().get(unit.team).tower = (Tower) unit;
        }

        return params;
    }

    @Override
    public void gameTurn(int turn) {
        Const.game.beforeTurn(turn, gameManager.getActivePlayers());
        Const.viewController.initRound(turn);

        if(turn == 0) sendInitialData();

        for (Player player : gameManager.getPlayers()) {
            player.sendInputLine(String.format("%d", player.getGold()));
            player.sendInputLine(String.format("%d", getOther(player).getGold()));
            player.sendInputLine(String.format("%d", turn >= Const.HEROCOUNT ? player.heroes.stream().filter(h -> !h.isDead).count() : (turn - Const.HEROCOUNT)));
            player.sendInputLine(String.format("%d", (Const.game.allUnits.stream().filter(u -> shouldSendToPlayer(player, u)).count())));

            for (Unit unit : Const.game.allUnits) {
                if (shouldSendToPlayer(player, unit)) {
                    player.sendInputLine(unit.getPlayerString());
                }
            }
        }

        for (Player player : gameManager.getPlayers ()) {
            player.execute();
            String[] strinOutputs = new String[0];
            if (turn < Const.HEROCOUNT) {
                gameManager.setFrameDuration(100);
                pickHero(player);
            } else {
                gameManager.setFrameDuration(1000);
                try {
                    Object[] outputs = player.getOutputs().toArray();
                    strinOutputs = Arrays.copyOf(outputs, outputs.length, String[].class);
                    player.handlePlayerOutputs(strinOutputs);
                } catch (AbstractPlayer.TimeoutException e) {
                    player.setScore(LostScore);
                    player.deactivate(player.getNicknameToken() + " timeout");
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    player.setScore(LostScore);
                    player.deactivate(player.getNicknameToken() + " Invalid input. Expected a number. Input was: " + Arrays.toString(strinOutputs));
                } catch (Exception e) {
                    player.setScore(LostScore);
                    player.deactivate(player.getNicknameToken() + " " + e.getMessage());
                }
            }
        }

        if(gameManager.getActivePlayers().size()==2) Const.game.handleTurn(gameManager.getActivePlayers());



        for (Player player : gameManager.getActivePlayers()) {
            player.setScore(0);
            boolean deadHeroes = player.heroes.stream().allMatch(h -> h.isDead);
            if ( deadHeroes || player.tower.isDead) {
                player.setScore(LostScore);
                player.deactivate(player.getNicknameToken() +  " lost. " + (deadHeroes ? "All heroes dead" : "Tower destroyed"));
            }else{
                player.setScore(player.unitKills+player.denies);
            }
        }

        if(turn == Const.Rounds-1 && gameManager.getActivePlayers().size() == 2){
            int score0 = gameManager.getActivePlayers().get(0).getScore();
            int score1 = gameManager.getActivePlayers().get(1).getScore();
            if(score0==score1){
                gameManager.addTooltip(new Tooltip(0, gameManager.getActivePlayers().get(0).getNicknameToken()+" draw"));
                gameManager.addTooltip(new Tooltip(1, gameManager.getActivePlayers().get(1).getNicknameToken()+" draw"));
            }else if(score0 > score1){
                gameManager.addTooltip(new Tooltip(1, gameManager.getActivePlayers().get(1).getNicknameToken()+ " loses. Has less creep kills + denies"));
            }else{
                gameManager.addTooltip(new Tooltip(0, gameManager.getActivePlayers().get(0).getNicknameToken()+ " loses. Has less creep kills + denies"));
            }
        }
        if (gameManager.getActivePlayers().size() < 2 ) {
            gameManager.endGame();
        }else{
            for(String msg : Const.viewController.summaries) {
                gameManager.addToGameSummary(msg);
            }
        }
    }

    private boolean shouldSendToPlayer(Player player, Unit u){
        return !u.isDead && (u.team == player.getIndex() || u.visible || u.becomingInvis || !(u instanceof Hero));
    }

    private Player getOther(Player player){
        return gameManager.getActivePlayers().get(1-player.getIndex());
    }

    private void pickHero(Player player)
    {
        String output = "";
        try {
            Point spawn = player.getIndex() == 0 ? (player.heroes.size() == 0 ? Const.HEROSPAWNTEAM0 : Const.HEROSPAWNTEAM0HERO2) : (player.heroes.size() == 0 ? Const.HEROSPAWNTEAM1 : Const.HEROSPAWNTEAM1HERO2);
            output = player.getOutputs().get(0);
            if( player.heroes.size() > 0 && player.heroes.get(0).heroType.equals(output) ){
                player.setScore(LostScore);
                player.deactivate(player.getNicknameToken() + " tried to pick a hero already owned. Can't have duplicate hero.");
                gameManager.addToGameSummary(player.getNicknameToken() + " tried to pick a hero already owned. Can't have duplicate hero.");
                return;
            } else {
              Hero hero = Factories.generateHero(output, player, spawn);
              player.addHero(hero);
              Const.game.allUnits.add(hero);
            }
        } catch (AbstractPlayer.TimeoutException e) {
            player.setScore(LostScore);
            player.deactivate(player.getNicknameToken() + " timed out.");
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            player.setScore(LostScore);
            player.deactivate(player.getNicknameToken() + " supplied an invalid hero name: " + output);
            gameManager.addToGameSummary(player.getNicknameToken() + " supplied an invalid hero name: " + output);
        } catch (Exception e){
            player.setScore(LostScore);
            String errorMessage = e.getMessage();
            player.deactivate(player.getNicknameToken() + " supplied invalid input. " + errorMessage);
        }
    }

    private void sendInitialData(){
        for(Player player : gameManager.getActivePlayers()){
            player.sendInputLine(String.format("%d", player.getIndex()));

            player.sendInputLine(String.format("%d", Const.game.bushes.size() + Const.game.spawns.length));
            for (Bush bush : Const.game.bushes) {
                player.sendInputLine(bush.getPlayerString());
            }

            for(CreatureSpawn spawn : Const.game.spawns){
                player.sendInputLine(spawn.getPlayerString());
            }

            //ITEMS
            player.sendInputLine(String.format("%d", Const.game.items.size()));
            for(String itemName : Const.game.items.keySet()){
                Item item = Const.game.items.get(itemName);
                player.sendInputLine(item.getPlayerString());
            }
        }
    }
}
