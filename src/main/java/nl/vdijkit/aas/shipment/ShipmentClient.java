package nl.vdijkit.aas.shipment;

import io.vertx.mutiny.core.Vertx;
import nl.vdijkit.aas.webclient.AbstractTntWebClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ShipmentClient extends AbstractTntWebClient<Shipment> {

    @Inject
    public ShipmentClient(Vertx vertx,
                          @ConfigProperty(name = "shipment.client.host", defaultValue = "localhost") String host,
                          @ConfigProperty(name = "shipment.client.port", defaultValue = "8080") Integer port) {
        super(vertx, new ShipmentMapper(), "/shipments", host, port);
    }

    @Override
    protected Class<Shipment> getItemClass() {
        return Shipment.class;
    }

}
