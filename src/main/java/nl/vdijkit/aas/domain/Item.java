package nl.vdijkit.aas.domain;

import io.vertx.core.json.JsonObject;

public interface Item {
    String getItem();
    JsonObject toJson();
}
