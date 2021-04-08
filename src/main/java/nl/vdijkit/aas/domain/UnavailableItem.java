package nl.vdijkit.aas.domain;

import io.vertx.core.json.JsonObject;

import java.util.List;

public class UnavailableItem implements Track, Shipment, Pricing {
    private final String item;

    public UnavailableItem(String item) {
        this.item = item;
    }

    @Override
    public String getItem() {
        return item;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject().put(item, "'null'");
    }
}

