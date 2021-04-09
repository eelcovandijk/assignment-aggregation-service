package nl.vdijkit.aas.aggregate;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Item;
import nl.vdijkit.aas.domain.UnavailableItem;
import nl.vdijkit.aas.pricing.Pricing;
import nl.vdijkit.aas.shipment.Shipment;
import nl.vdijkit.aas.track.Track;
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
    private final AtomicInteger currentItems = new AtomicInteger();
    private final List<String> pricingItems;
    private final List<String> trackItems;
    private final List<String> shipmentItems;
    private final String id;
    private final List<Item> tracks = new ArrayList<>();
    private final List<Item> shipments = new ArrayList<>();
    private final List<Item> pricing = new ArrayList<>();

    public AggregationRequestProcessor(List<String> pricingItems, List<String> trackItems, List<String> shipmentItems) {
        this.pricingItems = pricingItems;
        this.trackItems = trackItems;
        this.shipmentItems = shipmentItems;
        this.totalItems = pricingItems.size() + trackItems.size() + shipmentItems.size();
        this.id = UUID.randomUUID().toString();
    }

    public void registerResponse(Item item) {
        if(item instanceof UnavailableItem) {
            UnavailableItem<? extends Item> unavailableItem = (UnavailableItem<? extends Item>) item;
            registerResponse(unavailableItem.getItemType(), item);
        } else {
            registerResponse(item.getClass(), item);
        }
    }

    private void registerResponse(Class<? extends Item> type, Item item) {
        if(type.isAssignableFrom(Track.class)) {
            registerTrackResponse(item);
        } else if (type.isAssignableFrom(Shipment.class)) {
            registerShipmentResponse(item);
        } else if (type.isAssignableFrom(Pricing.class)) {
            registerPricingResponse(item);
        } else {
            throw new IllegalStateException("Failed to check for response item. Unknown type: " + item);
        }
    }

    private void registerTrackResponse(Item trackResponse) {
        LOGGER.infof("register track response: '%s", trackResponse);
        tracks.add(trackResponse);
        currentItems.incrementAndGet();
    }

    private void registerPricingResponse(Item pricingResponse) {
        LOGGER.infof("register pricing response: '%s", pricingResponse);
        pricing.add(pricingResponse);
        currentItems.incrementAndGet();
    }

    private void registerShipmentResponse(Item shipmentResponse) {
        LOGGER.infof("register shipment response: '%s", shipmentResponse);
        shipments.add(shipmentResponse);
        currentItems.incrementAndGet();
    }

    public boolean containsItem(Item item) {
        if(item instanceof UnavailableItem) {
            UnavailableItem<? extends Item> unavailableItem = (UnavailableItem<? extends Item>) item;
            return containsItem(unavailableItem.getItemType(), item);
        } else {
            return containsItem(item.getClass(), item);
        }
    }

    private boolean containsItem(Class<? extends Item> type, Item item) {
        if(type.isAssignableFrom(Track.class)) {
            return containsTrackItem(item.getItem());
        } else if (type.isAssignableFrom(Shipment.class)) {
            return containsShipmentItem(item.getItem());
        } else if (type.isAssignableFrom(Pricing.class)) {
            return containsPricingItem(item.getItem());
        } else {
            throw new IllegalStateException("Failed to check for response item. Unknown type: " + item);
        }
    }


    private boolean containsTrackItem(String item) {
        return trackItems.contains(item);
    }

    private boolean containsPricingItem(String item) {
        return pricingItems.contains(item);
    }

    private boolean containsShipmentItem(String item) {
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
            return new Aggregate(tracks, shipments, pricing).toJson();
        }).completeOnTimeout(new Aggregate(tracks, shipments, pricing).toJson(), 10, TimeUnit.SECONDS);
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
