package nl.vdijkit.aas.domain;

import nl.vdijkit.aas.aggregate.ItemHandler;

public class ItemCompleted {
    private final ItemHandler itemHandler;
    private ReactiveItem reactiveItem;

    public ItemCompleted(ItemHandler itemHandler, ReactiveItem reactiveItem) {
        this.itemHandler = itemHandler;
        this.reactiveItem = reactiveItem;
    }

    public ItemHandler getItemHandler() {
        return itemHandler;
    }

    public ReactiveItem getReactiveItem() {
        return reactiveItem;
    }

    @Override
    public String toString() {
        return "ItemCompleted{" +
                "reactiveItem=" + reactiveItem +
                '}';
    }
}
