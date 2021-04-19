package nl.vdijkit.aas.pricing;

import io.vertx.mutiny.core.Vertx;
import nl.vdijkit.aas.webclient.AbstractTntWebClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PricingClient extends AbstractTntWebClient<Pricing> {

    @Inject
    public PricingClient(Vertx vertx,
                         @ConfigProperty(name = "track.client.host", defaultValue = "localhost") String host,
                         @ConfigProperty(name = "track.client.port", defaultValue = "8080") Integer port) {
        super(vertx, new PricingMapper(), "/pricing", host, port);
    }

    @Override
    protected Class<Pricing> getItemClass() {
        return Pricing.class;
    }

}
