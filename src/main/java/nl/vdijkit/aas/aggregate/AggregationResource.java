package nl.vdijkit.aas.aggregate;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.ItemCompleted;
import nl.vdijkit.aas.domain.JsonItem;
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
 */
@Path("/aggregation")
public class AggregationResource {
    private static final Logger LOGGER = Logger.getLogger(AggregationResource.class);

    private final ReactiveDispatcher reactiveDispatcher;

    @Inject
    public AggregationResource(ReactiveDispatcher reactiveDispatcher) {
        this.reactiveDispatcher = reactiveDispatcher;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<JsonObject> aggregate(@QueryParam("pricing") String pricingItems,
                                     @QueryParam("track") String trackItems,
                                     @QueryParam("shipments") String shipmentItems) {

        Request request = new Request(convertToList(pricingItems), convertToList(trackItems), convertToList(shipmentItems));
        LOGGER.infof("Received aggregation request: '%s'", request);

        return reactiveDispatcher.process(request).toMulti()
                .flatMap(completedList -> Multi.createFrom().iterable(completedList))
                .map(ItemCompleted::getReactiveItem)
                .map(JsonItem::new)
                .group().by(JsonItem::getItemType)
                .flatMap(m -> m)
                .collect()
                .in(JsonObject::new, (jsonObject, jsonItem) -> {
                    LOGGER.infof("put in response: %s", jsonItem.getJsonObject().toString());
                    if(jsonObject.containsKey(jsonItem.getItemType().name().toLowerCase())) {
                        jsonObject.getJsonObject(jsonItem.getItemType().name().toLowerCase()).mergeIn(jsonItem.getJsonObject());
                    } else {
                        jsonObject.put(jsonItem.getItemType().name().toLowerCase(), jsonItem.getJsonObject());
                    }
                });

    }

    private List<String> convertToList(String items) {
        if (null != items) {
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