package nl.vdijkit.aas.domain;

import io.vertx.core.json.JsonObject;

public class TimedOutItem implements Track, Shipment, Pricing {
    private final String item;

    public TimedOutItem(String item) {
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
