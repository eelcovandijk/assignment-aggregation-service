package nl.vdijkit.aas.aggregate;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;


/**
 * Implementation of the aggregation api.
 * The interface description is published with Swagger #see: http://{host}:{port}/q/swagger-ui/#/default/get_aggregation
 *
 */
@Path("/aggregation")
public class AggregationResource {
    private static final Logger LOGGER = Logger.getLogger(AggregationResource.class);

    private final Dispatcher dispatcher;

    @Inject
    public AggregationResource(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

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
            return splitItems(items).stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<String> splitItems(String items) {
        return Arrays.asList(items.split(","));
    }
}