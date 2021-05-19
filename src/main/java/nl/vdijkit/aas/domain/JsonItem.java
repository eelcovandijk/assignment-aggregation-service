package nl.vdijkit.aas.domain;

import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.function.Function;

public class JsonItem {
    private static final Map<ItemType, Function<ReactiveItem, JsonObject>> MAPPERS = Map.of(
            ItemType.TRACK, item -> new JsonObject().put(item.getItem(), item.getStatus()),
            ItemType.SHIPMENTS, item -> new JsonObject().put(item.getItem(), item.getProducts()),
            ItemType.PRICING, item -> new JsonObject().put(item.getItem(), item.getAmount())
    );
    private final ItemType itemType;
    private final JsonObject jsonObject;

    public JsonItem(ReactiveItem reactiveItem) {
        this.itemType = reactiveItem.getType();
        jsonObject = MAPPERS.get(this.itemType).apply(reactiveItem);
    }

    public ItemType getItemType() {
        return itemType;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }
}
