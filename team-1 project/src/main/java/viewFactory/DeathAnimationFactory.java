package viewFactory;
import animation.AnimationGroup;
import animation.AnimationGroupFactory;
import com.codingame.game.Const;
import com.codingame.gameengine.module.entities.Curve;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.SpriteAnimation;
import com.codingame.gameengine.module.entities.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class DeathAnimationFactory {

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

        if(_instances.size() == 0) {
            _instances.add(createAnimation(module, false, 100, "d1.png", "d2.png", "d3.png", "Empty.png").setAnchor(0.5));
        }
        SpriteAnimation t = _instances.get(0);
        t.reset();
        _instances.remove(t);
        return t;
    }

    public static  void returnInstance(int availiableRound, SpriteAnimation text){
        if(!_returnedInstances.containsKey(availiableRound)) _returnedInstances.put(availiableRound, new ArrayList<>());
        _returnedInstances.get(availiableRound).add(text);
    }

    private static SpriteAnimation createAnimation(GraphicEntityModule module, boolean loop, int duration, String... images){
        return module.createSpriteAnimation().setImages(images).setAnchor(0.5).setLoop(loop).setDuration(duration);
    }
}
