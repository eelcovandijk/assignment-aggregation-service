package nl.vdijkit.aas.webclient;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.ReactiveItem;

import java.util.List;

public interface ResponseItemMapper {

    List<ReactiveItem> mapResponse(JsonObject response);
}
