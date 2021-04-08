package nl.vdijkit.aas.aggregate;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Pricing;
import nl.vdijkit.aas.domain.Shipment;
import nl.vdijkit.aas.domain.Track;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AggregationRequestProcessor {
    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class);
    private final int totalItems;
    private AtomicInteger currentItems = new AtomicInteger();
    private final List<String> pricingItems;
    private final List<String> trackItems;
    private final List<String> shipmentItems;
    private final String id;
    private List<Track> tracks = new ArrayList<>();
    private List<Shipment> shipments = new ArrayList<>();
    private List<Pricing> pricing = new ArrayList<>();

    public AggregationRequestProcessor(List<String> pricingItems, List<String> trackItems, List<String> shipmentItems) {
        this.pricingItems = pricingItems;
        this.trackItems = trackItems;
        this.shipmentItems = shipmentItems;
        this.totalItems = pricingItems.size() + trackItems.size() + shipmentItems.size();
        this.id = UUID.randomUUID().toString();
    }

    public void registerTrackResponse(Track trackResponse) {
        LOGGER.infof("register track response: '%s", trackResponse);
        tracks.add(trackResponse);
        currentItems.incrementAndGet();
    }

    public void registerPricingResponse(Pricing pricingResponse) {
        LOGGER.infof("register pricing response: '%s", pricingResponse);
        pricing.add(pricingResponse);
        currentItems.incrementAndGet();
    }

    public void registerShipmentResponse(Shipment shipmentResponse) {
        LOGGER.infof("register shipment response: '%s", shipmentResponse);
        shipments.add(shipmentResponse);
        currentItems.incrementAndGet();
    }

    public boolean containsTrackItem(String item) {
        return trackItems.contains(item);
    }

    public boolean containsPricingItem(String item) {
        return pricingItems.contains(item);
    }

    public boolean containsShipmentItem(String item) {
        return shipmentItems.contains(item);
    }

    public boolean isComplete() {
        return currentItems.get() == totalItems;
    }

    public Future<JsonObject> toResponse() {
        return CompletableFuture.supplyAsync(() -> {
            while (!isComplete()) {
                //block until complete
            }
            return new AggregateImpl(tracks, shipments, pricing).toJson();
        }).completeOnTimeout(new AggregateImpl(tracks, shipments, pricing).toJson(), 5, TimeUnit.SECONDS);
    }

    public List<String> getTrackItems() {
        return trackItems;
    }

    public List<String> getPricingItems() {
        return pricingItems;
    }

    public List<String> getShipmentItems() {
        return shipmentItems;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregationRequestProcessor that = (AggregationRequestProcessor) o;
        return totalItems == that.totalItems &&
                Objects.equals(id, that.id) &&
                Objects.equals(tracks, that.tracks) &&
                Objects.equals(shipments, that.shipments) &&
                Objects.equals(pricing, that.pricing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalItems, id, tracks, shipments, pricing);
    }

    @Override
    public String toString() {
        return "AggregationRequestProcessor{" +
                "totalItems=" + totalItems +
                ", currentItems=" + currentItems +
                ", pricingItems=" + pricingItems +
                ", trackItems=" + trackItems +
                ", shipmentItems=" + shipmentItems +
                ", id='" + id + '\'' +
                ", tracks=" + tracks +
                ", shipments=" + shipments +
                ", pricing=" + pricing +
                '}';
    }
}
