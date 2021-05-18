package nl.vdijkit.aas.domain;

import nl.vdijkit.aas.aggregate.ItemHandler;

import java.util.UUID;

public class ItemInProcess {
    private final ItemHandler itemHandler;
    private final ItemType type;
    private final String item;
    private final String id;

    public ItemInProcess(ItemHandler itemHandler, ItemType type, String item) {
        this.itemHandler = itemHandler;
        this.type = type;
        this.item = item;
        this.id = UUID.randomUUID().toString();
    }

    public ItemHandler getItemHandler() {
        return itemHandler;
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
