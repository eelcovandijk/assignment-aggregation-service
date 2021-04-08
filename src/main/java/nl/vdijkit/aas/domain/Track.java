package nl.vdijkit.aas.domain;

import io.vertx.core.json.JsonObject;

public interface Track {
    String getItem();
    JsonObject toJson();
}
