package nl.vdijkit.aas.domain;

import io.vertx.core.json.JsonObject;

public class UnavailableItem<T> implements Item {
    private final String item;
    private final Class<T> itemType;

    public UnavailableItem(String item, Class<T> itemType) {
        this.item = item;
        this.itemType = itemType;
    }

    @Override
    public String getItem() {
        return item;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject().putNull(item);
    }

    public Class<T> getItemType() {
        return itemType;
    }

    @Override
    public String toString() {
        return "UnavailableItem{" +
                "item='" + item + '\'' +
                ", itemType=" + itemType +
                '}';
    }
}

