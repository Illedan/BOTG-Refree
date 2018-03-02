package viewFactory;

import animation.AnimationGroupFactory;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.SpriteAnimation;
import com.codingame.gameengine.module.entities.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class FireBallFactory {

    private static ArrayList<SpriteAnimation> _instances = new ArrayList<>();
    private static HashMap<Integer, ArrayList<SpriteAnimation>> _returnedInstances = new HashMap<>();
    private static int _lastFetched = 0;

    public static SpriteAnimation getInstance(int round, GraphicEntityModule module){
        for(int i = _lastFetched+1; i <= round;i++){
            if(_returnedInstances.containsKey(i)){
                _instances.addAll(_returnedInstances.get(i));
            }

            _lastFetched = round;
        }

        if(_instances.size() == 0) _instances.add(createFireball(module));
        SpriteAnimation t = _instances.get(0);
        _instances.remove(t);
        return t;
    }

    public static  void returnInstance(int availiableRound, SpriteAnimation text){
        if(!_returnedInstances.containsKey(availiableRound)) _returnedInstances.put(availiableRound, new ArrayList<>());
        _returnedInstances.get(availiableRound).add(text);
    }

    private static SpriteAnimation createFireball(GraphicEntityModule module){
        SpriteAnimation animation = createAnimation(module, true, 300, "FIRE-CYCLE-1.png", "FIRE-CYCLE-2.png", "FIRE-CYCLE-3.png", "FIRE-CYCLE-4.png");
        animation.start();
        return animation;
    }

    private static SpriteAnimation createAnimation(GraphicEntityModule module, boolean loop, int duration, String... images){
        return module.createSpriteAnimation().setImages(images).setAnchor(0.5).setLoop(loop).setDuration(duration);
    }
}