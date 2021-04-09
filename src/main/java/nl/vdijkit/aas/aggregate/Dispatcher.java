package nl.vdijkit.aas.aggregate;

import nl.vdijkit.aas.webclient.TntWebClient;
import nl.vdijkit.aas.domain.Item;
import nl.vdijkit.aas.pricing.PricingClient;
import nl.vdijkit.aas.shipment.ShipmentClient;
import nl.vdijkit.aas.track.TrackClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class Dispatcher {
    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class);
    private final AggregationResponseHandler aggregationResponseHandler = new AggregationResponseHandler();
    private final ItemQueue trackItemQueue = new ItemQueue();
    private final ItemQueue shipmentItemQueue = new ItemQueue();
    private final ItemQueue pricingItemQueue = new ItemQueue();
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
        this.processItems(aggregationRequestProcessor.getTrackItems(), trackItemQueue, trackClient);
        this.processItems(aggregationRequestProcessor.getPricingItems(), pricingItemQueue, pricingClient);
        this.processItems(aggregationRequestProcessor.getShipmentItems(), shipmentItemQueue, shipmentClient);
    }

    private void processItems(List<String> items, ItemQueue itemQueue, TntWebClient tntWebClient) {
        items.forEach(item -> {
            Runnable dequeue = () -> {
                List<String> request = itemQueue.dequeueItems();
                tntWebClient.makeRequest(request).subscribe().with(aggregationResponseHandler::registerResponse);
            };
            itemQueue.queueItem(item, dequeue);
        });
    }

    public static class AggregationResponseHandler {
        private final Map<String, AggregationRequestProcessor> requestListByHash = new HashMap<>();

        public void registerAggregationRequest(AggregationRequestProcessor aggregationRequestProcessor) {
            requestListByHash.put(aggregationRequestProcessor.getId(), aggregationRequestProcessor);
            LOGGER.infof("registered request to be handled: '%s'", aggregationRequestProcessor);
        }

        public void registerResponse(List<Item> response) {
            LOGGER.infof("received response: '%s'", response);
            List<String> finished = response.stream()
                    .flatMap(item -> requestListByHash.values().stream()
                            .filter(request -> request.containsItem(item))
                            .filter(request -> {
                                request.registerResponse(item);
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

    public static class ItemQueue {
        private final static int CURRENT_DEQUEUE_SIZE = 5;
        private final Queue<String> items = new ConcurrentLinkedDeque<>();
        private final AtomicInteger counter = new AtomicInteger();
        private Timer timer = new Timer();

        public void queueItem(String item, Runnable dequeu) {
            items.add(item);
            counter.incrementAndGet();
            TimerTask dequeueTask = new TimerTask() {
                @Override
                public void run() {
                    dequeu.run();
                }
            };
            if(isGrouped()) {
                timer.cancel();
                dequeu.run();
            } else {
                timer.cancel();
                timer = new Timer(true);
                timer.schedule(dequeueTask, 5000);
            }
        }

        public boolean isGrouped() {
            return counter.getAcquire() == CURRENT_DEQUEUE_SIZE;
        }

        public List<String> dequeueItems() {
            if (isGrouped()) {
                return dequeueNrOfItems(CURRENT_DEQUEUE_SIZE);
            }
            return dequeueNrOfItems(counter.getAcquire());
        }

        private List<String> dequeueNrOfItems(int itemCount) {
            return IntStream.range(0, itemCount)
                    .mapToObj((index) -> {
                        counter.decrementAndGet();
                        return items.poll();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

}
