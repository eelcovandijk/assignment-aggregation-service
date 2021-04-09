package nl.vdijkit.aas.webclient;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Item;

import java.util.List;

public interface ResponseItemMapper {

    List<Item> mapResponse(JsonObject response);
}
