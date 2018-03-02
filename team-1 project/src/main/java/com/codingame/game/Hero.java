package com.codingame.game;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Hero extends Unit {

    public Skills.SkillBase[] skills = new Skills.SkillBase[Const.SKILLCOUNT];
    public String heroType;
    public int mana;
    public int manaregeneration;
    public int maxMana;
    int channeling;
    int visibilityTimer;
    public ArrayList<Item> items = new ArrayList<>();
    String avatar = Const.MAGEAVATAR;

    public Hero(double x, double y, int health, int team, int moveSpeed, Player player, String heroType) {
        super( x, y, health, team, moveSpeed, player);
        this.goldValue = Const.HERO_GOLD_VALUE;
        this.heroType = heroType;
        this.channeling = 0;
    }

    @Override
    void afterRound(){
    	super.afterRound();
        visibilityTimer = Math.max(0, visibilityTimer-1);
        mana += manaregeneration;
        if(mana > maxMana) mana = maxMana;
        for (Skills.SkillBase skill : skills){
            if(skill == null) continue;
            skill.cooldown = Math.max(0, skill.cooldown-1);
        }
    }

    void addItem(Item item) {
        if (item == null) {
            return;
        }

        addCharacteristics(item, 1);
        if (!item.isPotion) {
            items.add(item);
        }
    }

    void removeItem(Item item){
        if(!items.contains(item)) return;
        items.remove(item);
        addCharacteristics(item, -1);
    }

    void addCharacteristics(Item item, int amplitude) {
        Map<String, Integer> characteristics = item.stats;
        Class<?> c = this.getClass();
        characteristics.forEach((k, v) -> {
            try {
                Field f = c.getDeclaredField(k);
                f.set(this, f.getInt(this) + v * amplitude);
            } catch (Exception e) {
            }
        });

        Class<?> c2 = this.getClass().getSuperclass();
        characteristics.forEach((k, v) -> {
            try {
                Field f = c2.getDeclaredField(k);
                f.set(this, f.getInt(this) + v * amplitude);
            } catch (Exception e) {
            }
        });

        if (mana > maxMana) mana = maxMana;
        if (health > maxHealth) health = maxHealth;
        if (mana < 0) mana = 0;
        if (health <= 0) health = 1;
        if (moveSpeed > Const.MAXMOVESPEED) moveSpeed = Const.MAXMOVESPEED;
    }

    public boolean isVisible(Player player){
        return (!isDead && (visible || player == this.player));
    }

    @Override
    public String getType() {
        return "HERO";
    }

    @Override
    protected String getExtraProperties(){
        return
            skills[0].cooldown + " " +
            skills[1].cooldown + " " +
            skills[2].cooldown + " " +
            mana + " " +
            maxMana + " " +
            manaregeneration + " " +
            heroType + " " +
            (visible?1:0)+" "+
            items.size();
    }
}
