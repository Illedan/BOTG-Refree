package viewFactory;

import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class TextFactory {

    private static ArrayList<Text> _instances = new ArrayList<>();
    private static HashMap<Integer, ArrayList<Text>> _returnedInstances = new HashMap<>();
    private static int _lastFetched = 0;

    public static Text getInstance(int round, GraphicEntityModule module, String s){
        for(int i = _lastFetched+1; i <= round;i++){
            if(_returnedInstances.containsKey(i)){
                _instances.addAll(_returnedInstances.get(i));
            }

            _lastFetched = round;
        }

        if(_instances.size() == 0) {
            _instances.add(module.createText(s));
        }
        Text t = _instances.get(0).setText(s);
        _instances.remove(t);
        return t;
    }

    public static  void returnInstance(int availiableRound, Text text){
        if(!_returnedInstances.containsKey(availiableRound)) _returnedInstances.put(availiableRound, new ArrayList<>());
        _returnedInstances.get(availiableRound).add(text);
    }
}
