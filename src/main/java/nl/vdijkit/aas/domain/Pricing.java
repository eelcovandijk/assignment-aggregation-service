package nl.vdijkit.aas.domain;

import io.vertx.core.json.JsonObject;

public interface Pricing {
    JsonObject toJson();
    String getItem();
}
