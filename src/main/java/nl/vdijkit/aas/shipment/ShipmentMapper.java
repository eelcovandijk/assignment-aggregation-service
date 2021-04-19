package nl.vdijkit.aas.shipment;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Item;
import nl.vdijkit.aas.webclient.ResponseItemMapper;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class ShipmentMapper implements ResponseItemMapper {
    private static final Logger LOGGER = Logger.getLogger(ShipmentMapper.class);

    public List<Item> mapResponse(JsonObject response) {
        LOGGER.infof("shipments received: '%s'", response);
        if (null != response) {
            return response.stream()
                    .map(entry -> new Shipment(entry.getKey(), mapShipment(entry.getValue())))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private JsonArray mapShipment(Object shipmentToMap) {
        if (shipmentToMap instanceof JsonArray) {
            return (JsonArray) shipmentToMap;
        }

        LOGGER.warnf("shipment in not mappable: %s", shipmentToMap);
        return null;
    }
}
