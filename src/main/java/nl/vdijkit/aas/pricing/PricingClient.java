package nl.vdijkit.aas.pricing;

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
public class PricingClient extends AbstractTntWebClient<Pricing> {
    private static final Logger LOGGER = Logger.getLogger(PricingClient.class);

    @Inject
    public PricingClient(Vertx vertx) {
        super(vertx, new PricingMapper(), "/pricing", "localhost", 8080);
    }

    @Override
    protected Class<Pricing> getItemClass() {
        return Pricing.class;
    }

    private static class PricingMapper implements ResponseItemMapper {
        public List<Item> mapResponse(JsonObject response) {
            LOGGER.infof("prices received: '%s'", response.toString());
            response.getString("");
            return response.stream()
                    .map(entry -> new Pricing(entry.getKey(), (Double) entry.getValue()))
                    .collect(Collectors.toList());
        }
    }
}
