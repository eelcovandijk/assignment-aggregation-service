package nl.vdijkit.aas.pricing;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import nl.vdijkit.aas.domain.Pricing;
import nl.vdijkit.aas.domain.Shipment;
import nl.vdijkit.aas.domain.TimedOutItem;
import nl.vdijkit.aas.domain.UnavailableItem;
import nl.vdijkit.aas.shipment.ShipmentClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class PricingClient {
    private static final Logger LOGGER = Logger.getLogger(PricingClient.class);

    private final WebClient client;

    @Inject
    public PricingClient(Vertx vertx) {
        this.client = WebClient.create(vertx,
                new WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080));
    }

    public Uni<List<Pricing>> prices(List<String> items) {
        return client.get("/pricing")
                .addQueryParam("q", String.join(",", items))
                .send()
                .map(mapResponse(items))
                .ifNoItem().after(Duration.ofMillis(5000))
                .recoverWithItem(items.stream().map(TimedOutItem::new).collect(Collectors.toList()));
    }

    private Function<HttpResponse<Buffer>, List<Pricing>> mapResponse(List<String> requestedItems) {
        return bufferHttpResponse -> {
            if (bufferHttpResponse.statusCode() == 200) {
                return new PricingMapper(bufferHttpResponse.bodyAsJsonObject()).mapResponse();
            }
            return requestedItems.stream().map(UnavailableItem::new).collect(Collectors.toList());
        };
    }

    private static class PricingMapper {
        private final JsonObject response;

        public PricingMapper(JsonObject response) {
            this.response = response;
        }

        public List<Pricing> mapResponse() {
            LOGGER.infof("prices received: '%s'", response.toString());
            response.getString("");
            return response.stream()
                    .map(entry -> new PricingImpl(entry.getKey(), (Double) entry.getValue()))
                    .collect(Collectors.toList());
        }
    }
}
