package nl.vdijkit.aas.shipment;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import nl.vdijkit.aas.domain.Shipment;
import nl.vdijkit.aas.domain.TimedOutItem;
import nl.vdijkit.aas.domain.Track;
import nl.vdijkit.aas.domain.UnavailableItem;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShipmentClient {
    private static final Logger LOGGER = Logger.getLogger(ShipmentClient.class);
    private static final Function<HttpResponse<Buffer>, List<Shipment>> HTTP_RESPONSE_TRACK_FUNCTION = httpResponse -> {
        if (httpResponse.statusCode() == 200) {
            return new ShipmentMapper(httpResponse.bodyAsJsonObject()).mapResponse();
        }
        return List.of(new UnavailableItem());
    };
    private final WebClient client;

    @Inject
    public ShipmentClient(Vertx vertx) {
        this.client = WebClient.create(vertx,
                new WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080));
    }

    public Uni<List<Shipment>> track(List<String> items) {
        return client.get("/shipments")
                .addQueryParam("q", String.join(",", items))
                .send()
                .map(HTTP_RESPONSE_TRACK_FUNCTION)
                .ifNoItem().after(Duration.ofMillis(5000))
                .recoverWithItem(List.of(new TimedOutItem()));
    }

    private static class ShipmentMapper {
        private final JsonObject response;

        public ShipmentMapper(JsonObject response) {
            this.response = response;
        }

        public List<Shipment> mapResponse() {
            LOGGER.infof("shipments received: '%s'", response.toString());
            response.getString("");
            return response.stream()
                    .map(entry -> new ShipmentImpl(entry.getKey(), (JsonArray) entry.getValue()))
                    .collect(Collectors.toList());
        }
    }
}
