package nl.vdijkit.aas.aggregate;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Pricing;
import nl.vdijkit.aas.domain.Shipment;
import nl.vdijkit.aas.domain.Track;
import nl.vdijkit.aas.pricing.PricingClient;
import nl.vdijkit.aas.shipment.ShipmentClient;
import nl.vdijkit.aas.track.TrackClient;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/aggregation")
public class AggregationResource {
    private static final Logger LOGGER = Logger.getLogger(AggregationResource.class);

    @Inject
    TrackClient trackClient;
    @Inject
    ShipmentClient shipmentClient;
    @Inject
    PricingClient pricingClient;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<JsonObject> aggregate(@QueryParam("pricing") List<String> pricingItems,
                                     @QueryParam("track") List<String> trackItems,
                                     @QueryParam("shipments") List<String> shipmentItems) {
        LOGGER.infof("received aggregation request for pricing '%s', tracks '%s', shipments '%s'", new Object[] {pricingItems, trackItems, shipmentItems});

        Uni<List<Track>> tracks = trackClient.track(trackItems);
        Uni<List<Shipment>> shipments = shipmentClient.track(shipmentItems);
        Uni<List<Pricing>> pricing = pricingClient.prices(pricingItems);

        return Uni.combine()
                .all()
                .unis(tracks, shipments, pricing)
                .combinedWith(AggregateImpl::new)
                .map(AggregateImpl::toJson);
    }
}