package animation;

import java.util.*;
import java.util.stream.Collectors;

import com.codingame.game.*;
import com.codingame.gameengine.module.entities.*;

public class AnimationGroup {
    public Group sprite;
    public Sprite glow;
    Map<String, SpriteAnimation> animations;
    private String[] sortOrder = new String[] {"dead", "spell", "attack", "movement", "default"};

    public Unit unit;
    int prevStun;
    Sprite stunIndicator; // Optimization
    Circle outLine; // Optimization
    Circle shield;

    GraphicEntityModule module;
    private static ArrayList<Sprite> _stunIndicators = new ArrayList<>();
    private static ArrayList<Circle> _shields = new ArrayList<>();

    public AnimationGroup(GraphicEntityModule module, Group group, Unit unit, String outlineSprite) {
        this.sprite = group;
        this.unit = unit;
        this.module = module;
        animations = new HashMap<>();
        if(outlineSprite != null){
            int color = unit.player.getIndex()==0?0xff0000:0x0000ff;
            sprite.add(outLine = module.createCircle().setFillAlpha(0).setFillColor(color, Curve.ELASTIC).setAlpha(0.5).setZIndex(-101).setLineColor(color).setLineWidth(8).setRadius(45));
        }
    }

    public void addShield(){
        if(shield != null || unit.getShield() <= 0) return;
        if(_shields.size()==0) _shields.add(module.createCircle().setFillColor(0xffffff, Curve.LINEAR).setFillAlpha(0.4).setLineWidth(3, Curve.ELASTIC).setZIndex(100).setRadius(55).setLineColor(0xcccccc));
        shield = _shields.get(0).setScale(1, Curve.IMMEDIATE);
        _shields.remove(shield);
        sprite.add(shield);
        module.commitEntityState(Const.game.t, shield);
    }

    public void removeShield(){
        if(shield == null) return;
        if(unit.getShield() > 0) return;
        _shields.add(shield);
        sprite.remove(shield);
        shield.setAlpha(0, Curve.IMMEDIATE);
        module.commitEntityState(Const.game.t, shield);
    }

    public void explodeShield(){
        if(shield == null) return;
        double t = Math.max(0,Const.game.t-0.1);
        module.commitEntityState(t, shield);
        shield.setScale(1.5, Curve.ELASTIC).setAlpha(0);
        module.commitEntityState(Math.min(1.0, t+0.1), shield);
        _shields.add(shield);
        sprite.remove(shield);
        shield.setAlpha(0, Curve.IMMEDIATE);
        module.commitEntityState(Math.min(1.0, t+0.15), shield);
    }

    public Entity updateAngle(double rotation){
        return sprite.setRotation(rotation);
    }

    public Entity updatePosition(){
        return sprite.setX((int)unit.x).setY((int)unit.y);
    }

    public void updateStunIndicator(){
        if(unit.stunTime == 0 && stunIndicator != null){
            _stunIndicators.add(stunIndicator);
            sprite.remove(stunIndicator);
            stunIndicator.setVisible(false);
            stunIndicator = null;
            prevStun = 0;
            return;
        }

        if(unit.stunTime==prevStun || unit.isDead) return;
        prevStun = unit.stunTime;
        if(stunIndicator == null){
            if(_stunIndicators.size()==0) _stunIndicators.add(module.createSprite().setImage("stunindicator.png").setZIndex(1000).setTint(0x4e5056).setAnchor(0.5));
            stunIndicator = _stunIndicators.get(0).setVisible(true);
            _stunIndicators.remove(stunIndicator);
            sprite.add(stunIndicator);
        }

        stunIndicator.setRotation(stunIndicator.getRotation()+Math.PI/10.0).setVisible(true);
        module.commitEntityState(0.1, stunIndicator);
        stunIndicator.setRotation(stunIndicator.getRotation()+Math.PI/10.0*9.0-0.00001).setVisible(true);
        module.commitEntityState(1.0, stunIndicator);
        // you spin my head right round :)
    }

    public void kill(){
        if(stunIndicator!= null && stunIndicator.isVisible()){
            _stunIndicators.add(stunIndicator);
            sprite.remove(stunIndicator);
            stunIndicator.setVisible(false);
            module.commitEntityState(Const.game.t, stunIndicator);
            stunIndicator = null;
        }

        if(outLine != null){
            outLine.setVisible(false);
            module.commitEntityState(Const.game.t, outLine);
        }
    }

    private SpriteAnimation active;
    public void endOfRound(){
        updateStunIndicator();
        if(active==null){
            for(SpriteAnimation animation : animations.values()){
                if(animation.isStarted()) active = animation;
            }
        }
        for(AnimationCommit commit : commits.stream().sorted(Comparator.comparing(AnimationCommit::getT)).collect(Collectors.toList())){
            SpriteAnimation next = animations.get(commit.name);
            if(active == next && !commit.name.equals("attack")){
                continue;
            }else if(active == next && commit.name.equals("attack")){
                active.reset();
            }else {
                if(active != null) {
                    active.stop();
                    module.commitEntityState(commit.t, active);
                }
                active = next;
                active.start();
                module.commitEntityState(commit.t, active);
            }
        }
        commits.clear();
    }

    public void add(String name, SpriteAnimation spriteAnimation) {
        animations.put(name, spriteAnimation);
        sprite.add(spriteAnimation);
        if(!name.equals("default"))
            spriteAnimation.stop();
        else {
            spriteAnimation.start();    
            
            module.commitEntityState(0, spriteAnimation);
            active = spriteAnimation;
        }
    }

    public AnimationGroup activate(String name, double t, double duration) {
        for(int i = commits.size()-1; i>= 0; i--){
            AnimationCommit animationCommit = commits.get(i);
            if(Math.abs(t-animationCommit.t) < Const.EPSILON){
                if(sortOrderOf(name) > sortOrderOf(animationCommit.name)){
                    return this;
                }else{
                    commits.remove(animationCommit);
                }
            }
        }
        commits.add(new AnimationCommit(t, name, duration));
        return this;
    }

    private int sortOrderOf(String s){
        for(int i = 0; i < sortOrder.length; i++){
            if(sortOrder[i].equals(s)) return i;
        }

        return sortOrder.length+1;
    }

    private ArrayList<AnimationCommit> commits = new ArrayList<>();

    static class AnimationCommit{
        double t;
        String name;
        double duration;
        public AnimationCommit(double t, String name, double duration){
            this.t = t;
            this.name = name;
            this.duration = duration;
        }
        double getT() { return t;}
    }
}