package tooltipModule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.codingame.game.Item;
import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.codingame.gameengine.module.entities.Entity;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

public class TooltipModule implements Module {

    GameManager<AbstractPlayer> gameManager;
    @Inject GraphicEntityModule entityModule;
    Map<Integer, Map<String, Object>> registrations;
    Map<Integer, Map<String, Object>> newRegistrations;
    Map<Integer, String[]> extra, newExtra;
    Set<Integer> removals;


    @Inject
    TooltipModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
        registrations = new HashMap<>();
        newRegistrations = new HashMap<>();
        extra = new HashMap<>();
        newExtra = new HashMap<>();
        removals = new HashSet<>();
    }

    @Override
    public void onGameInit() {
        // gameManager.setViewGlobalData("tooltips", null);
        sendFrameData();
    }

    @Override
    public void onAfterGameTurn() {
        sendFrameData();
    }

    @Override
    public void onAfterOnEnd() {
        sendFrameData();
    }

    private void sendFrameData() {
        Object[] data = {newRegistrations, newExtra, removals};
        gameManager.setViewData("tooltips", data);

        removals.stream().forEach(entity -> {
            registrations.remove(entity);
            extra.remove(entity);
        });

        removals.clear();
        newRegistrations.clear();
        newExtra.clear();
    }

    public void registerEntity(Entity<?> entity) {
        registerEntity(entity, new HashMap<>());
    }

    public void registerEntity(Entity<?> entity, Map<String, Object> params) {
        int id = entity.getId();
        if (!params.equals(registrations.get(id))) {
            newRegistrations.put(id, params);
            registrations.put(id, params);
        }
    }

    public void registerItem(Entity<?> entity, Item item) {
        int id = entity.getId();
        newRegistrations.put(id, convertItem(item));
        registrations.put(id, convertItem(item));
    }

    private Map<String, Object> convertItem(Item item) {
        Map<String, Object> itemStats = new HashMap<>();
        itemStats.put("Name", item.name);
        itemStats.put("Price", item.cost);
        itemStats.put("Type", "Item");
        for (Map.Entry<String, Integer> entry : item.stats.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (value == 0) continue;
            itemStats.put(key, value + "");
        }
        return itemStats;
    }

    public void unregisterItem(Entity<?> entity, Item item) {
        removals.add(entity.getId());
    }

    public void removeEntity(Entity<?> entity) {
        removals.add(entity.getId());
    }

    public void updateExtraTooltipText(Entity<?> entity, String... lines) {
        int id = entity.getId();
        if (!Arrays.equals(lines, extra.get(id))) {
            newExtra.put(id, lines);
            extra.put(id, lines);
        }
    }

    public void removeUnitToolTip(Entity<?> entity) {
        entity.setVisible(false); // erases from the view the unit sprite too
    }
}
