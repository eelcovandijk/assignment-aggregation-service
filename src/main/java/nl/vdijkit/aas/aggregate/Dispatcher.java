package nl.vdijkit.aas.aggregate;

import nl.vdijkit.aas.domain.Pricing;
import nl.vdijkit.aas.domain.Shipment;
import nl.vdijkit.aas.domain.Track;
import nl.vdijkit.aas.pricing.PricingClient;
import nl.vdijkit.aas.shipment.ShipmentClient;
import nl.vdijkit.aas.track.TrackClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class Dispatcher {
    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class);
    private final AggregationResponseHandler aggregationResponseHandler = new AggregationResponseHandler();
    private final ItemCombiner trackItemCombiner = new ItemCombiner();
    private final ItemCombiner shipmentItemCombiner = new ItemCombiner();
    private final ItemCombiner pricingItemCombiner = new ItemCombiner();
    private final TrackClient trackClient;
    private final PricingClient pricingClient;
    private final ShipmentClient shipmentClient;

    @Inject
    public Dispatcher(TrackClient trackClient, PricingClient pricingClient, ShipmentClient shipmentClient) {
        this.trackClient = trackClient;
        this.pricingClient = pricingClient;
        this.shipmentClient = shipmentClient;
    }

    public synchronized void registerNewRequest(AggregationRequestProcessor aggregationRequestProcessor) {
        aggregationResponseHandler.registerAggregationRequest(aggregationRequestProcessor);
        this.processTrackItems(aggregationRequestProcessor.getTrackItems());
        this.processPricingItems(aggregationRequestProcessor.getPricingItems());
        this.processShipmentItems(aggregationRequestProcessor.getShipmentItems());
    }

    private void processTrackItems(List<String> trackItems) {
        trackItems.forEach(track -> {
            trackItemCombiner.addItem(track);
            LOGGER.infof("current track itemcount: %s", trackItemCombiner.size());
            if (trackItemCombiner.isPickable()) {
                List<String> request = trackItemCombiner.items();
                trackClient.track(request).subscribe().with(aggregationResponseHandler::registerTrackResponse);
            }
        });
    }

    private void processPricingItems(List<String> pricingItems) {
        pricingItems.forEach(track -> {
            pricingItemCombiner.addItem(track);
            LOGGER.infof("current pricing itemcount: %s", pricingItemCombiner.size());
            if (pricingItemCombiner.isPickable()) {
                List<String> request = pricingItemCombiner.items();
                pricingClient.prices(request).subscribe().with(aggregationResponseHandler::registerPricingResponse);
            }
        });
    }

    private void processShipmentItems(List<String> shipmentItems) {
        shipmentItems.forEach(track -> {
            shipmentItemCombiner.addItem(track);
            LOGGER.infof("current shipment itemcount: %s", shipmentItemCombiner.size());
            if (shipmentItemCombiner.isPickable()) {
                List<String> request = shipmentItemCombiner.items();
                shipmentClient.track(request).subscribe().with(aggregationResponseHandler::registerShipmentResponse);
            }
        });
    }

    public static class AggregationResponseHandler {
        private final Map<String, AggregationRequestProcessor> requestListByHash = new HashMap<>();

        public void registerAggregationRequest(AggregationRequestProcessor aggregationRequestProcessor) {
            requestListByHash.put(aggregationRequestProcessor.getId(), aggregationRequestProcessor);
            LOGGER.infof("registered request to be handled: '%s'", aggregationRequestProcessor);
        }

        public void registerTrackResponse(List<Track> trackResponse) {
            LOGGER.infof("received trackResponse: '%s'", trackResponse);
            List<String> finished = trackResponse.stream()
                    .flatMap(track -> requestListByHash.values().stream()
                            .filter(request -> request.containsTrackItem(track.getItem()))
                            .filter(request -> {
                                request.registerTrackResponse(track);
                                if (request.isComplete()) {
                                    LOGGER.infof("return response for: '%s' with hash: '%s'", request, request.hashCode());
                                    return true;
                                }
                                return false;
                            }).map(AggregationRequestProcessor::getId)
                    ).collect(Collectors.toList());

            finished.forEach(requestListByHash::remove);
        }

        public void registerPricingResponse(List<Pricing> pricingResponse) {
            LOGGER.infof("received pricingResponse: '%s'", pricingResponse);
            List<String> finished = pricingResponse.stream()
                    .flatMap(pricing -> requestListByHash.values().stream()
                            .filter(request -> request.containsPricingItem(pricing.getItem()))
                            .filter(request -> {
                                request.registerPricingResponse(pricing);
                                if (request.isComplete()) {
                                    LOGGER.infof("return response for: '%s' with hash: '%s'", request, request.hashCode());
                                    return true;
                                }
                                return false;
                            }).map(AggregationRequestProcessor::getId)
                    ).collect(Collectors.toList());

            finished.forEach(requestListByHash::remove);
        }

        public void registerShipmentResponse(List<Shipment> shipmentResponse) {
            LOGGER.infof("received shipmentResponse: '%s'", shipmentResponse);
            List<String> finished = shipmentResponse.stream()
                    .flatMap(shipment -> requestListByHash.values().stream()
                            .filter(request -> request.containsShipmentItem(shipment.getItem()))
                            .filter(request -> {
                                request.registerShipmentResponse(shipment);
                                if (request.isComplete()) {
                                    LOGGER.infof("return response for: '%s' with hash: '%s'", request, request.hashCode());
                                    return true;
                                }
                                return false;
                            }).map(AggregationRequestProcessor::getId)
                    ).collect(Collectors.toList());

            finished.forEach(requestListByHash::remove);
        }
    }

    public static class ItemCombiner {
        private final static int CURRENT_DEQUEUE_SIZE = 5;
        private final Queue<String> items = new ConcurrentLinkedDeque<>();
        private final AtomicInteger counter = new AtomicInteger();

        public void addItem(String item) {
            items.add(item);
            counter.incrementAndGet();
        }

        public boolean isPickable() {
            if (counter.getAcquire() == CURRENT_DEQUEUE_SIZE) {
                return true;
            }
            return false;
        }

        private int size() {
            return counter.get();
        }

        public List<String> items() {
            if (isPickable()) {
                return IntStream.range(0, CURRENT_DEQUEUE_SIZE)
                        .mapToObj((index) -> {
                            counter.decrementAndGet();
                            return items.poll();
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            throw new IllegalStateException("only pick the items when it matches the expected size");
        }
    }


}
