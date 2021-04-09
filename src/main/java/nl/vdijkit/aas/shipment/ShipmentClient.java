package nl.vdijkit.aas.shipment;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import nl.vdijkit.aas.webclient.ResponseItemMapper;
import nl.vdijkit.aas.domain.Item;
import nl.vdijkit.aas.webclient.AbstractTntWebClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShipmentClient extends AbstractTntWebClient<Shipment> {
    private static final Logger LOGGER = Logger.getLogger(ShipmentClient.class);

    @Inject
    public ShipmentClient(Vertx vertx) {
        super(vertx, new ShipmentMapper(), "/shipments", "localhost", 8080);
    }

    @Override
    protected Class<Shipment> getItemClass() {
        return Shipment.class;
    }

    private static class ShipmentMapper implements ResponseItemMapper {
        public List<Item> mapResponse(JsonObject response) {
            LOGGER.infof("shipments received: '%s'", response.toString());
            response.getString("");
            return response.stream()
                    .map(entry -> new Shipment(entry.getKey(), (JsonArray) entry.getValue()))
                    .collect(Collectors.toList());
        }
    }
}
