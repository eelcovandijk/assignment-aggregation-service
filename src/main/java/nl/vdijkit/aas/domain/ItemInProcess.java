package nl.vdijkit.aas.domain;

import java.util.UUID;

public class ItemInProcess {
    private final ItemType type;
    private final String item;
    private final String id;

    public ItemInProcess(ItemType type, String item) {
        this.type = type;
        this.item = item;
        this.id = UUID.randomUUID().toString();
    }

    public ItemType getType() {
        return type;
    }

    public String getItem() {
        return item;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ItemInProcess{" +
                "type=" + type +
                ", item='" + item + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
