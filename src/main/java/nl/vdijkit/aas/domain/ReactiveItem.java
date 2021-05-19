package nl.vdijkit.aas.domain;

import io.vertx.core.json.JsonArray;

public class ReactiveItem {
    private final String item;
    private Double amount;
    private JsonArray products;
    private String status;
    private ItemType type;

    private ReactiveItem(String item) {
        this.item = item;
    }

    protected ReactiveItem(String item, ItemType type) {
        this(item);
        this.type = type;
    }

    public static ReactiveItem shipment(String itemKey, JsonArray products) {
        var item = new ReactiveItem(itemKey);
        item.products = products;
        item.type = ItemType.SHIPMENTS;
        return item;
    }

    public static ReactiveItem pricing(String itemKey, Double amount) {
        var item = new ReactiveItem(itemKey);
        item.amount = amount;
        item.type = ItemType.PRICING;
        return item;
    }

    public static ReactiveItem track(String itemKey, String status) {
        var item = new ReactiveItem(itemKey);
        item.status = status;
        item.type = ItemType.TRACK;
        return item;
    }

    public String getItem() {
        return item;
    }

    public ItemType getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public Double getAmount() {
        return amount;
    }

    public JsonArray getProducts() {
        return products;
    }

    @Override
    public String toString() {
        return "ReactiveItem{" +
                "item='" + item + '\'' +
                ", type=" + type +
                '}';
    }
}
