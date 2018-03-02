package animation;

import com.codingame.game.Const;
import com.codingame.game.Creature;
import com.codingame.game.Tower;
import com.codingame.game.Unit;
import com.codingame.gameengine.module.entities.*;

public class AnimationGroupWithHealth extends AnimationGroup
{
    public Group container;
    Rectangle dynamicHealthBar;
    Rectangle staticHealthBar;
    int prevHealth;

    public AnimationGroupWithHealth(GraphicEntityModule module, Group group, Unit unit, String outlineSprite) {
        super(module, group, unit, outlineSprite);
        container = module.createGroup(group).setZIndex(-10);
        prevHealth = unit.health;
    }

    @Override
    public Entity updateAngle(double rotation){
        return sprite.setRotation(rotation);
    }

    @Override
    public Entity updatePosition(){
        return container.setX((int)unit.x).setY((int)unit.y);
    }

    @Override
    public void kill(){
        super.kill();
        if(staticHealthBar!= null){
            staticHealthBar.setAlpha(0, Curve.IMMEDIATE);
            dynamicHealthBar.setAlpha(0, Curve.IMMEDIATE);
            module.commitEntityState(Const.game.t, dynamicHealthBar, staticHealthBar);
        }
    }

    @Override
    public void endOfRound(){
        super.endOfRound();
        if(unit instanceof Tower || unit instanceof Creature) {
            if(unit.health!=prevHealth){
                if(staticHealthBar==null) addHealthBar();
                updateHealth();
            }
        }
    }

    private void addHealthBar(){
        container.add(staticHealthBar = module.createRectangle().setFillColor(0x000000).setWidth(unit.maxHealth/18).setHeight(8).setY(-50).setX(-unit.maxHealth/36).setZIndex(10));
        container.add(dynamicHealthBar = module.createRectangle().setFillColor(0x00FF00).setWidth(unit.maxHealth/18).setHeight(8).setY(-50).setX(-unit.maxHealth/36).setZIndex(11));
    }

    public void updateHealth() {
        if(unit.isDead) return;
        if(prevHealth==unit.health) return;
        prevHealth = unit.health; //optimization
        if(dynamicHealthBar == null) addHealthBar();

        dynamicHealthBar.setScaleX(Math.min(1.0,Math.max(0,(double)Math.max(0,unit.health)/(double)unit.maxHealth)));
        if(unit.health==unit.maxHealth){
            staticHealthBar.setAlpha(0, Curve.IMMEDIATE);
            dynamicHealthBar.setAlpha(0, Curve.IMMEDIATE);
        }else if(dynamicHealthBar.getAlpha()<Const.EPSILON){
            staticHealthBar.setAlpha(1, Curve.IMMEDIATE);
            dynamicHealthBar.setAlpha(1, Curve.IMMEDIATE);
        }

        module.commitEntityState(1, dynamicHealthBar, staticHealthBar);
    }
}
