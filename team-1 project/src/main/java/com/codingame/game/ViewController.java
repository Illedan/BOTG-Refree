package com.codingame.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import animation.AnimationGroup;
import animation.AnimationGroupFactory;
import animation.AnimationGroupWithHealth;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Tooltip;
import com.codingame.gameengine.module.entities.*;

import tooltipModule.TooltipModule;
import viewFactory.*;

public class ViewController {
    private final String fontFamily = "Trebuchet MS";

    GameManager<Player> gameManager;
    HashMap<Integer, Tuple> spriteMap;
    private GraphicEntityModule entityManager;
    private TooltipModule tooltipModule;
    int currentRound = 0;
    ArrayList<PlayerHud> playerHuds = new ArrayList<>();
    ArrayList<String> summaries = new ArrayList<>();
    HashMap<Entity, AnimationGroup> animationGroups = new HashMap<>();

    public ViewController() {
        spriteMap = new HashMap<>();
    }

    public void initRound(int round){
        currentRound = round;

        summaries.clear();
    }

    public void addEffect(Unit unit, Point target, String effect, double duration){
        double t = Const.game.t;
        Tuple tuple = spriteMap.get(unit.id);
        if(tuple==null) return;
        if(effect.equals("default")){
            animationGroups.get(tuple.sprite).activate("default", t, 0);
        }
        if(effect.equals("spell")){
            animationGroups.get(tuple.sprite).activate("attack", t, 1.0);
            setRotation(unit, target, duration);
        }
        else if(effect.equals("potion")){

        }
        else if(effect.equals("movement")){
            //Animate movement
            if(!(unit instanceof Tower) && unit.stunTime <= 0) {
                if(Math.abs(unit.x-target.x) > Const.EPSILON || Math.abs(unit.y-target.y) > Const.EPSILON){
                    animationGroups.get(tuple.sprite).activate("movement", t, 2);
                    setRotation(unit, target, t);
                }
            }
        }
        else if(effect.equals("attack")){
            if(!(unit instanceof Tower)) {
                animationGroups.get(tuple.sprite).activate("attack", t, duration);
                setRotation(unit, target, t);
            }else{
                AnimationGroup group = animationGroups.get(tuple.sprite);
                group.glow.setX((int)target.x).setY((int)target.y).setAlpha(0, Curve.NONE).setScale(1.5);
                double flyAnimation = unit.attackTime+unit.distance(target)/unit.range*unit.attackTime;
                commitSprite(t+ flyAnimation, group.glow);
                group.glow.setX((int)unit.x, Curve.NONE).setY((int)unit.y, Curve.NONE).setAlpha(1, Curve.ELASTIC).setScale(1);

                commitSprite(t + flyAnimation+0.1, group.glow);
            }

        }
        else if(effect.equals("stun")){
            animationGroups.get(tuple.sprite).updateStunIndicator();
        }
        else if(effect.equals("heal")){
            Sprite aoeHeal = AoeHealFactory.getInstance(Const.game.round, entityManager);
            aoeHeal.setX((int)target.x, Curve.NONE).setY((int)target.y, Curve.NONE).setAlpha(1, Curve.NONE);
            commitSprite(0, aoeHeal);
            aoeHeal.setAlpha(0, Curve.LINEAR);
            commitSprite(1, aoeHeal);
            AoeHealFactory.returnInstance(Const.game.round+1, aoeHeal);
        }
        else if(effect.equals("burning")){
            Sprite burning = BurningGroundFactory.getInstance(Const.game.round, entityManager);
            burning.setX((int)target.x, Curve.NONE).setY((int)target.y, Curve.NONE).setAlpha(1, Curve.NONE);
            commitSprite(0, burning);
            burning.setAlpha(0, Curve.LINEAR);
            commitSprite(1, burning);
            BurningGroundFactory.returnInstance(Const.game.round+1, burning);
        }
        else if(effect.equals("fireball"))
        {
            SpriteAnimation fireball = FireBallFactory.getInstance(Const.game.round, entityManager);
            fireball.setX((int)unit.x, Curve.IMMEDIATE).setY((int)unit.y, Curve.IMMEDIATE).setZIndex(8).setAlpha(1);
            commitSprite(t, fireball);

            fireball.setAlpha(0, Curve.NONE).setX((int)target.x).setY((int)target.y);
            commitSprite(t+duration, fireball);

            FireBallFactory.returnInstance(Const.game.round+1, fireball);
        }
        else if(effect.equals("shield"))
        {
          //  if(unit.getShield() > 0) animationGroups.get(tuple.sprite).addShield();
          //  else animationGroups.get(tuple.sprite).removeShield();
        }
        else if(effect.equals("shieldexplosion"))
        {
           // animationGroups.get(tuple.sprite).explodeShield();
        }
        else if(effect.equals("wirehook")){
            Sprite wirehook = WireHookFactory.getInstance(Const.game.round, entityManager);
            wirehook.setX((int)unit.x, Curve.IMMEDIATE)
                    .setY((int)unit.y, Curve.IMMEDIATE)
                    .setZIndex(8)
                    .setAlpha(1, Curve.IMMEDIATE)
            .setRotation(Math.atan2(target.y-unit.y, target.x-unit.x), Curve.IMMEDIATE);
            commitSprite(t, wirehook);

            wirehook.setAlpha(0, Curve.NONE).setX((int)target.x).setY((int)target.y);
            commitSprite(t+duration, wirehook);

            WireHookFactory.returnInstance(Const.game.round+1, wirehook);
            // Anker flying
        }
        else if(effect.equals("blink")){
            //Little indicator
        }
        else if(effect.equals("counter")){
            //Send bullet towards pos.
        }
    }

    public void addSummary(String message) {
    	summaries.add(message);
    }

    public void addObstacle(Bush obstacle){
        entityManager.createSprite().setImage(obstacle.skin).setX((int)obstacle.x).setY((int)obstacle.y).setZIndex(-150).setAnchor(0.5);
    }

    public void addSprite(Unit unit, int team) {
        if(unit instanceof Tower) spriteMap.put(unit.id, new Tuple(unit, createTower(unit)));
        else spriteMap.put(unit.id, new Tuple(unit, createUnitSprite(unit)));

        if(unit instanceof Hero) playerHuds.get(unit.player.getIndex()).addHero((Hero)unit);
    }

    private Entity createTower(Unit unit){
        AnimationGroup group = AnimationGroupFactory.create(entityManager, unit);
        Entity entity = group.updatePosition().setZIndex(-150);
        unit.sprite = entity;

        createToolTip(unit, entity);

        animationGroups.put(entity, group);
        commitSprite(0, entity);

        entity.setZIndex(-10);

        group.glow = entityManager.createSprite()
                .setImage("tower-glow-"+(unit.team==0?"red":"blue")+".png")
                .setX((int)unit.x)
                .setY((int)unit.y)
                .setAnchor(0.5)
                .setZIndex(7);
        createToolTip(unit, entity);

        commitSprite(0, entity);
        return entity;
    }

    private Entity createUnitSprite(Unit unit){

        AnimationGroup group = AnimationGroupFactory.create(entityManager, unit);

        Entity entity = group.updatePosition().setZIndex(-10);
        Entity other = null;
        unit.sprite = entity;
        if(unit instanceof Hero) entity.setZIndex(4);
        if(unit instanceof Creature){
            other = group.updateAngle(Math.atan2(100,0)).setZIndex(3);
        }

        createToolTip(unit, entity);
        group.activate("default", 0, 0);

        animationGroups.put(entity, group);
        if(other==null) commitSprite(0, entity);
        else commitSprite(0, entity, other);

        return entity;
    }

    private void createToolTip(Unit unit, Entity entity){
        Map<String, Object> params = new HashMap<>();
        params.put("type", unit.getType());
        params.put("id", unit.id);
        params.put("attack_type", unit.isMelee());
        params.put("damage", unit.damage);

        //TODO: load parameters the viewer needs for the general tooltip contents.
        tooltipModule.registerEntity(entity, params);

        //TODO: this can be used to change the extra contents of the tooltip throughout the game.
        tooltipModule.updateExtraTooltipText(entity, "health: " + unit.health +
                                          "\nteam: " + unit.team);
    }

    public void setRotation(Unit unit, Point target, double t){
        Tuple unitSprite = spriteMap.get(unit.id);
        if(unitSprite==null || unit instanceof Tower) return;

        double rotation = Math.atan2(target.y-unit.y, target.x - unit.x);
        Entity e1 = animationGroups.get(unitSprite.sprite).updateAngle(rotation);
        Entity e2 = animationGroups.get(unitSprite.sprite).updatePosition();
        if(e1 == e2) commitSprite(t, e1);
        else commitSprite(t, e1, e2);
    }

    public void updateViewForUnit(Unit unit, double t){
        Tuple unitSprite = spriteMap.get(unit.id);
        if(unitSprite==null || unit instanceof Tower) return;

        Entity entity = animationGroups.get(unitSprite.sprite).updatePosition();
        double alpha = unitSprite.sprite.getAlpha();
        double newAlpha = !unit.visible?0.7:1.0;
        if(Math.abs(alpha-newAlpha) > Const.EPSILON){
            entity.setAlpha(newAlpha);
        }
        commitSprite(t, entity);
    }

    public void removeItem(Hero hero, Item item){
        getHud(hero).removeItem(item);
    }

    private HeroHud getHud(Hero hero){
        return playerHuds.get(hero.player.getIndex()).heroHuds[hero.player.heroes.indexOf(hero)];
    }

    public void addItem(Hero hero, Item item){
        if(item.isPotion){
            addEffect(hero, null, "potion", 0);
        }else{
            getHud(hero).addItem(item);
        }
    }

    public void afterRound(){
        updateView(1);
        for(PlayerHud hud : playerHuds){
            hud.onRoundEnd();
        }
        for(AnimationGroup animationGroup : animationGroups.values()){
            animationGroup.endOfRound();
        }
    }

    public void updateView(double t) {
        for (Tuple unitSprite : spriteMap.values()) {
            updateViewForUnit(unitSprite.unit, t);
        }
    }

    public void displayDamages(Unit unit, int damage, double t) {
        int color = damage < 0 ? 0x00ff00 : 0xff0000;
        createUnitText(String.format("%d", Math.abs(damage)), unit, color, 40);
    }

    public void killUnit(Unit unit, UnitKilledState unitKilledState) {
        Tuple tuple = spriteMap.get(unit.id);
        if(tuple == null) return;

        animationGroups.get(tuple.sprite).kill();
        tuple.sprite.setAlpha(0, Curve.LINEAR);
        commitSprite(Const.game.t, tuple.sprite);
        spriteMap.remove(unit.id);

        SpriteAnimation death = DeathAnimationFactory.getInstance(Const.game.round, entityManager);
        death.setX((int)unit.x, Curve.IMMEDIATE).setY((int)unit.y, Curve.IMMEDIATE);
        commitSprite(Const.game.t, death);

        DeathAnimationFactory.returnInstance(Const.game.round+2, death);

        if(animationGroups.containsKey(tuple.sprite)){
            AnimationGroup animationGroup = animationGroups.get(tuple.sprite);
            animationGroups.remove(animationGroup);
            if(animationGroup.unit instanceof Creature) AnimationGroupFactory.GrootFactory.add((AnimationGroupWithHealth) animationGroup);
            if(animationGroup.unit instanceof LaneUnit) AnimationGroupFactory.RaccoonFactory.get(animationGroup.unit.team).add(animationGroup);
        }

        if ( unitKilledState == UnitKilledState.farmed) {
            createUnitText(String.format("%d", unit.goldValue), unit, 0xffff00, 50);
        }else if(unitKilledState == UnitKilledState.denied){
            createUnitText("denied", unit, 0xff0000, 50);
        }else{
            //Nothing?
        }
        if(unit instanceof Hero){
            addViewerMessage(unit.player, ((Hero)unit).heroType + " died");
        } else if(unit instanceof Tower){
            addViewerMessage(unit.player, "Player " + unit.team + " lost their tower.");
        }
    }

    public void addViewerMessage(Player player, String message){
        gameManager.addTooltip(new Tooltip(player.getIndex(), message));
    }

    private Text createUnitText(String txt, Unit unit, int color, int shift){
        Text text = TextFactory.getInstance(Const.game.round, entityManager, txt);
        text.setX((int) unit.x, Curve.NONE)
                .setY((int)unit.y, Curve.NONE)
                .setZIndex(1000)
                .setFontSize(20)
                .setFillColor(color)
                .setAnchor(0.5)
                .setAlpha(1, Curve.NONE);

        commitSprite(Const.game.t, text);

        text.setY((int)unit.y-shift).setAlpha(0, Curve.NONE);
        commitSprite(Const.game.t+0.5, text);

        TextFactory.returnInstance(Const.game.round+1, text);
        return text;
    }

    private void commitSprite(double t, Entity... entity){
        t = Math.min(t, 1.0);
        entityManager.commitEntityState(t, entity);
    }

    public void initialize(GraphicEntityModule entityManager, TooltipModule tooltipModule, List<Player> players, GameManager<Player> gameManager) {
        this.entityManager = entityManager;
        this.tooltipModule = tooltipModule;
        this.gameManager = gameManager;
        for(Player player : players){
            playerHuds.add(new PlayerHud(player, (int)((player.getIndex())*Const.MAPWIDTH/2), 780));
        }
    }

   public void addMessageToHeroHud(Hero hero, String message) {
       getHud(hero).setText(message);
   }

   public void updateEntityTooltip(Entity<?> entity, String... string) {
       tooltipModule.updateExtraTooltipText(entity, string);
   }

   public void removeEntityTooltip(Entity<?> entity) {
       tooltipModule.removeEntity(entity);
       // tooltipModule.removeUnitToolTip(entity);
   }

    public class Tuple {
        public final Unit unit;
        public final Entity sprite;

        public Tuple(Unit unit, Entity sprite) {
            this.unit = unit;
            this.sprite = sprite;
        }
    }

    public class PlayerHud{
        Group container;
        Player player;
        Text denyCounter;
        Text killCounter;
        Text goldCounter;
        private int lastGold, lastKill, lastDeny;
        HeroHud[] heroHuds = new HeroHud[Const.HEROCOUNT];

        public PlayerHud(Player player, int x, int y){
            container = entityManager.createGroup().setX(x).setY(y);
            this.player = player;
            container.add(entityManager.createSprite().setImage("hud/"+(player.getIndex()==0?"red":"blue")+".png").setX(15).setY(15));
            container.add(entityManager.createSprite().setImage("hud/player.png").setX(23).setY(55));

            goldCounter = createTextWithIcon(173, "hud/coinIcon.png", ""+player.getGold(), 0xe7a93a);
            killCounter = createTextWithIcon(208, "hud/killIcon.png", "0", 0xf20b0b);
            denyCounter = createTextWithIcon(246, "hud/denyIcon.png", "0", 0x6af97d);

            container.add(entityManager.createText(player.getNicknameToken()).setFontFamily(fontFamily).setX(30).setY(52).setZIndex(1).setFontSize(30).setFillColor(player.getColorToken()).setAnchorX(0).setAnchorY(1));
            container.add(entityManager.createSprite().setX(78).setY(109).setZIndex(1).setImage(player.getAvatarToken()).setAnchor(0.5).setBaseHeight(80).setBaseWidth(80));

            lastGold = player.getGold();
        }

        Text createTextWithIcon(int y, String icon, String text, int color){
            Text textSprite = entityManager.createText(text).setX(120).setY(y).setFillColor(color).setAnchorX(1).setAnchorY(0.5).setZIndex(1).setFontFamily(fontFamily);
            container.add(textSprite);
            container.add(entityManager.createSprite().setImage(icon).setX(35).setY(y).setAnchorY(0.5));
            return textSprite;
        }

        void onRoundEnd(){
            lastGold = setValues(lastGold, player.getGold(), goldCounter, true);
            lastKill = setValues(lastKill, player.unitKills, killCounter, true);
            lastDeny = setValues(lastDeny, player.denies, denyCounter, true);
            for(int i = 0; i < Const.HEROCOUNT; i++){
                if(heroHuds[i]!= null) heroHuds[i].onRoundEnd();
            }
            for(int id : spriteMap.keySet()){
                Tuple tuple = spriteMap.get(id);
                if(tuple.unit instanceof Hero) continue;
                updateEntityTooltip(tuple.sprite, "health: " + tuple.unit.health +
                                                  "\nteam: " + tuple.unit.team );
            }
        }

        private int setValues(int oldVal, int newVal, Text text, boolean commit){
            if(newVal != oldVal){
                text.setText(newVal+"");
                if (commit) commitSprite(1.0, text);
            }
            return newVal;
        }

        public void addHero(Hero hero){

            int position = hero.player.heroes.size();
            heroHuds[position] = new HeroHud(hero);
            container.add(heroHuds[position].container.setX(150).setY(55+105*position));

            commitSprite(0.0, heroHuds[position].container);

        }
    }

    public class HeroHud
    {
        Group container;
        Sprite healthBar;
        Sprite manaBar;
        Hero hero;
        Text healthCounter, manaCounter, message, damageCounter, moveSpeedCounter;
        Sprite[] itemSlots = new Sprite[Const.MAXITEMCOUNT];
        Item[] itemBackingFields = new Item[Const.MAXITEMCOUNT];
        private int prevMana, prevHealth, prevDamage, prevMoveSpeed;

        public HeroHud(Hero hero){
            container = entityManager.createGroup();
            this.hero = hero;

            container.add(entityManager.createSprite().setImage("hud/heronamebox.png").setX(0).setY(12));
            container.add(entityManager.createText(hero.heroType).setX(10).setY(22).setFillColor(hero.player.getColorToken()).setFontFamily(fontFamily));
            container.add(message = entityManager.createText("").setX(10).setY(62).setFillColor(hero.player.getColorToken()));
            container.add(entityManager.createSprite().setImage("hud/health-mana-frame.png").setX(650).setY(27).setAnchor(0.5));
            container.add(entityManager.createSprite().setImage("hud/health-mana-frame.png").setX(650).setY(73).setAnchor(0.5));
            container.add(healthBar = entityManager.createSprite().setImage("hud/health-bar.png").setTint(0xaaaaaa).setX(582).setY(27).setAnchorX(0).setAnchorY(0.5));
            container.add(manaBar = entityManager.createSprite().setImage("hud/mana-bar.png").setTint(0xaaaaaa).setX(582).setY(73).setAnchorX(0).setAnchorY(0.5));

            container.add(entityManager.createSprite().setImage("hud/bartextcontainer.png").setY(27).setX(480).setAnchor(0.5));
            container.add(entityManager.createSprite().setImage("hud/bartextcontainer.png").setY(73).setX(480).setAnchor(0.5));
            container.add(healthCounter = entityManager.createText(hero.health+"").setFontSize(18).setY(27).setX(650).setAnchor(0.5).setFillColor(0xDDDDDD));
            container.add(manaCounter = entityManager.createText(hero.mana+"").setFontSize(18).setY(73).setX(650).setAnchor(0.5).setFillColor(0xDDDDDD));

            container.add(damageCounter = entityManager.createText(""+ hero.damage).setY(27).setX(480).setAnchor(0.5).setFillColor(0xFFFFFF));
            container.add(moveSpeedCounter = entityManager.createText("" +hero.moveSpeed).setY(73).setX(480).setAnchor(0.5).setFillColor(0xFFFFFF));
            prevMana = hero.mana;
            prevHealth = hero.health;
            prevDamage = hero.damage;
            prevMoveSpeed = hero.moveSpeed;
            for(int i = 0; i < Const.MAXITEMCOUNT; i++){
                container.add(entityManager.createSprite().setImage("hud/iconframe.png").setX(345+(i%2)*46).setY(27+(i/2)*46).setAnchor(0.5));
            }
        }

        public void removeItem(Item item){
            if(item.isPotion) return; // bug if reached...
            for(int i = 0; i < Const.MAXITEMCOUNT; i++) {
                if(itemBackingFields[i] == item){
                    tooltipModule.unregisterItem(itemSlots[i], item);
                    itemSlots[i].setAlpha(0).setVisible(false);
                    itemSlots[i] = null;
                    itemBackingFields[i] = null;
                    return;
                }
            }
        }

        public void addItem(Item item){
            for(int i = 0; i < Const.MAXITEMCOUNT; i++) {
                if(itemSlots[i] == null){
                    container.add(itemSlots[i] = (entityManager.createSprite().setImage(getItemImage(item)).setTint(getItemTint(item.name.replaceAll("_.*", ""))).setX(345+(i%2)*46).setY(27+(i/2)*46).setAnchor(0.5)));
                    tooltipModule.registerItem(itemSlots[i], item);
                    itemBackingFields[i] = item;
                    return;
                }
            }
        }

        private String getItemImage(Item item){
            if(item.stats.get(Const.DAMAGE) > 0 && item.stats.get(Const.DAMAGE) > item.stats.get(Const.MOVESPEED)) return "hud/knife-white.png";
            if(item.stats.get(Const.MOVESPEED) > 0) return "hud/boots-white.png";
            return "hud/hook-white.png";
        }

        private int getItemTint(String league){
            if(league.equals("Legendary")) return 0xff1a53;
            if(league.equals("Golden")) return 0xffff00;
            if(league.equals("Silver")) return 0xc0c0c0;
            if(league.equals("Bronze")) return 0xad8b6d;
            return 0x087004;
        }

        public void onRoundEnd(){
            prevMana = setValues(prevMana, hero.mana, manaCounter, manaBar, hero.maxMana);
            prevHealth = setValues(prevHealth, hero.health, healthCounter, healthBar, hero.maxHealth);
            prevMoveSpeed = setValues(prevMoveSpeed, hero.moveSpeed, moveSpeedCounter, null, 0);
            prevDamage = setValues(prevDamage, hero.damage, damageCounter, null, 0);

            if(!hero.isDead)
            tooltipModule.updateExtraTooltipText(hero.sprite, "health: " + hero.health +
                                              "\nteam: " + hero.team);
        }

        private int setValues(int oldVal, int newVal, Text text, Sprite bar, int max){
            if(newVal != oldVal){
                newVal = Math.max(newVal, 0);
                text.setText(newVal+"");
                commitSprite(1.0, text);
                if(bar != null){
                    bar.setScaleX(Math.min(1,Math.max(0,newVal)/(double)max));
                    commitSprite(1.0, bar);
                }
            }
            return newVal;
        }

        public void setText(String text){
            if(!text.equals(message.getText())) {
                message.setText(text.substring(0, Math.min(text.length(), 15)));
                commitSprite(0.0, message);
            }
        }
    }
}
