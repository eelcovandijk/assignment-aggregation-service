package nl.vdijkit.aas.domain;

import io.vertx.core.json.JsonObject;

public class TimedOutItem implements Track, Shipment, Pricing {



    @Override
    public JsonObject toJson() {
        return new JsonObject();
    }
}
