package animation;

import com.codingame.game.*;
import com.codingame.gameengine.module.entities.Entity;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.SpriteAnimation;

import java.util.ArrayList;
import java.util.HashMap;

public class AnimationGroupFactory {

    public static HashMap<Integer, ArrayList<AnimationGroup>> RaccoonFactory = new HashMap<>();
    public static ArrayList<AnimationGroupWithHealth> GrootFactory = new ArrayList<>();

    public static AnimationGroup create(GraphicEntityModule module, Unit unit){
        if(unit instanceof LaneUnit) return createRaccoon(module, unit);
        if(unit instanceof Creature) return createGroot(module, unit);
        if(unit instanceof Tower) return createTower(module, unit);
        if(unit instanceof Hero){
            Hero hero = (Hero)unit;
            if(hero.heroType.equals("HULK")) return createHulk(module, unit);
            if(hero.heroType.equals("IRONMAN")) return createIronMan(module, unit);
            if(hero.heroType.equals("DOCTOR_STRANGE")) return createDoctorStrange(module, unit);
            if(hero.heroType.equals("DEADPOOL")) return createDeadpool(module, unit);
            if(hero.heroType.equals("VALKYRIE")) return createValkyrie(module, unit);
        }

        return null;
    }

    public static AnimationGroup createRaccoon(GraphicEntityModule module, Unit unit) {
        if(!RaccoonFactory.containsKey(unit.team)) RaccoonFactory.put(unit.team, new ArrayList<>());
        else if(RaccoonFactory.get(unit.team).size() > 0){
            AnimationGroup cachedGroup = RaccoonFactory.get(unit.team).get(0);
            RaccoonFactory.get(unit.team).remove(cachedGroup);
            cachedGroup.sprite.setAlpha(1);
            cachedGroup.unit = unit;
            cachedGroup.updateStunIndicator();
            return cachedGroup;
        }

        String color = "r";
        if(unit.team == 1) color = "b";
        Group group = module.createGroup();
        AnimationGroup aniGroup = new AnimationGroup(module, group, unit, null);
        aniGroup.add("attack", createAnimation(module, false, 600, "rs"+color+".png", "rf"+color+".png", "rs"+color+".png"));
        aniGroup.add("movement", createAnimation(module, true, 300, "rw1"+color+".png", "rw2"+color+".png"));
        aniGroup.add("default", createAnimation(module, false, 1000000, "rs"+color+".png"));

        return aniGroup;
    }

    public static AnimationGroup createGroot(GraphicEntityModule module, Unit unit){
        if(GrootFactory.size()>0){
            AnimationGroupWithHealth cachedGroup = GrootFactory.get(0);
            GrootFactory.remove(cachedGroup);
            cachedGroup.sprite.setAlpha(1);
            cachedGroup.unit = unit;
            module.commitEntityState(0, cachedGroup.sprite);
            return cachedGroup;
        }

        Group group = module.createGroup();
        AnimationGroupWithHealth aniGroup = new AnimationGroupWithHealth(module, group, unit, null);
        aniGroup.add("attack", createAnimation(module, false, 600, "G-ATTACK-2.png", "G-ATTACK-3.png", "G-ATTACK-1.png"));
        aniGroup.add("movement", createAnimation(module, true, 300, "G-WALK-1.png", "G-WALK-3.png"));
        aniGroup.add("default", createAnimation(module, false, 1000, "G-WALK-2.png"));
        aniGroup.container.setZIndex(0);
        return aniGroup;
    }

    public static AnimationGroup createHulk(GraphicEntityModule module, Unit unit){
        Group group = module.createGroup();
        AnimationGroup aniGroup = new AnimationGroup(module, group, unit, "H-WALK-2.png");
        aniGroup.add("attack", createAnimation(module, false, 300, "H-ATTACK-2.png", "H-ATTACK-3.png", "H-WALK-2.png"));
       // aniGroup.add("spell", createAnimation(module, false, 300, "H-ATTACK-2.png", "H-ATTACK-3.png", "H-WALK-2.png"));
        aniGroup.add("movement", createAnimation(module, true, 300, "H-WALK-1.png", "H-WALK-3.png"));
        aniGroup.add("default", createAnimation(module, false, 1000, "H-WALK-2.png"));

        return aniGroup;
    }

    public static AnimationGroup createIronMan(GraphicEntityModule module, Unit unit){
        Group group = module.createGroup();
        AnimationGroup aniGroup = new AnimationGroup(module, group, unit, "IM-WALK-2.png");
        aniGroup.add("attack", createAnimation(module, false, 300, "IM-WALK-2.png", "IM-ATTACK-2.png", "IM-WALK-2.png"));
       // aniGroup.add("spell", createAnimation(module, false, 300, "IM-WALK-2.png", "IM-ATTACK-2.png", "IM-WALK-2.png"));
        aniGroup.add("movement", createAnimation(module, true, 300, "IM-WALK-1.png", "IM-WALK-3.png"));
        aniGroup.add("default", createAnimation(module, false, 1000, "IM-WALK-2.png"));

        return aniGroup;
    }

    public static AnimationGroup createDoctorStrange(GraphicEntityModule module, Unit unit){
        Group group = module.createGroup();
        AnimationGroup aniGroup = new AnimationGroup(module, group, unit, "DS-WALK-2.png");
        aniGroup.add("attack", createAnimation(module, false, 300, "DS-WALK-2.png", "DS-ATTACK-2.png", "DS-WALK-2.png"));
      //  aniGroup.add("spell", createAnimation(module, false, 300, "DS-WALK-2.png", "DS-ATTACK-2.png", "DS-WALK-2.png"));
        aniGroup.add("movement", createAnimation(module, true, 300, "DS-WALK-1.png", "DS-WALK-3.png"));
        aniGroup.add("default", createAnimation(module, false, 1000, "DS-WALK-2.png"));


        return aniGroup;
    }

    public static AnimationGroup createDeadpool(GraphicEntityModule module, Unit unit){
        Group group = module.createGroup();
        AnimationGroup aniGroup = new AnimationGroup(module, group, unit, "DP-WALK-2.png");
        aniGroup.add("attack", createAnimation(module, false, 300, "DP-ATTACK2.png", "DP-ATTACK3.png", "DP-WALK-2.png"));
      //  aniGroup.add("spell", createAnimation(module, false, 300, "DP-ATTACK2.png", "DP-ATTACK3.png", "DP-WALK-2.png"));
        aniGroup.add("movement", createAnimation(module, true, 300, "DP-WALK-1.png", "DP-WALK-3.png"));
        aniGroup.add("default", createAnimation(module, false, 1000, "DP-WALK-2.png"));

        return aniGroup;
    }

    public static AnimationGroup createValkyrie(GraphicEntityModule module, Unit unit){
        Group group = module.createGroup();
        AnimationGroup aniGroup = new AnimationGroup(module, group, unit, "V-WALK-2.png");
        aniGroup.add("attack", createAnimation(module, false, 300, "V-ATTACK-2.png", "V-ATTACK-3.png", "V-WALK-2.png"));
       // aniGroup.add("spell", createAnimation(module, false, 300, "V-ATTACK-2.png", "V-ATTACK-3.png", "V-WALK-2.png"));
        aniGroup.add("movement", createAnimation(module, true, 300, "V-WALK-1.png", "V-WALK-3.png"));
        aniGroup.add("default", createAnimation(module, false, 1000, "V-WALK-2.png"));

        return aniGroup;
    }

    public static AnimationGroup createTower(GraphicEntityModule module, Unit unit){
        String color = "red";
        if(unit.team == 1) color = "blue";
        Group group = module.createGroup();
        AnimationGroupWithHealth aniGroup = new AnimationGroupWithHealth(module, group, unit, null);
        aniGroup.add("default", createAnimation(module, false, 1000, "tower-" + color + ".png"));
        return aniGroup;
    }


    private static SpriteAnimation createAnimation(GraphicEntityModule module, boolean loop, int duration, String... images){
        return module.createSpriteAnimation().setImages(images).setAnchor(0.5).setLoop(loop).setDuration(duration);
    }
}
