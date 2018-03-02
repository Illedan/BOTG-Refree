package viewFactory;

import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Sprite;

import java.util.ArrayList;
import java.util.HashMap;

public class AoeHealFactory {

    private static ArrayList<Sprite> _instances = new ArrayList<>();
    private static HashMap<Integer, ArrayList<Sprite>> _returnedInstances = new HashMap<>();
    private static int _lastFetched = 0;

    public static Sprite getInstance(int round, GraphicEntityModule module){
        for(int i = _lastFetched+1; i <= round;i++){
            if(_returnedInstances.containsKey(i)){
                _instances.addAll(_returnedInstances.get(i));
            }

            _lastFetched = round;
        }

        if(_instances.size() == 0) {
            _instances.add(module.createSprite().setImage("HEALING-BALL.png").setScale(1.5).setAnchor(0.5));
        }
        Sprite t = _instances.get(0);
        _instances.remove(t);
        return t;
    }

    public static  void returnInstance(int availiableRound, Sprite text){
        if(!_returnedInstances.containsKey(availiableRound)) _returnedInstances.put(availiableRound, new ArrayList<>());
        _returnedInstances.get(availiableRound).add(text);
    }
}