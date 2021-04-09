package nl.vdijkit.aas.aggregate;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Path("/aggregation")
public class AggregationResource {
    private static final Logger LOGGER = Logger.getLogger(AggregationResource.class);

    @Inject
    Dispatcher dispatcher;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<JsonObject> aggregate(@QueryParam("pricing") String pricingItems,
                                     @QueryParam("track") String trackItems,
                                     @QueryParam("shipments") String shipmentItems) {
        LOGGER.infof("Received aggregation request for pricing '%s', tracks '%s', shipments '%s'", new Object[] {pricingItems, trackItems, shipmentItems});

        AggregationRequestProcessor aggregationRequestProcessor = new AggregationRequestProcessor(convertToList(pricingItems), convertToList(trackItems), convertToList(shipmentItems));
        dispatcher.registerNewRequest(aggregationRequestProcessor);

        return Uni.createFrom().future(aggregationRequestProcessor.toResponse());
    }

    private List<String> convertToList(String items) {
        if(null != items) {
            return Arrays.asList(items.split(","));
        }
        return Collections.emptyList();
    }
}