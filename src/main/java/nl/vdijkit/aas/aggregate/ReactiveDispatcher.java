package nl.vdijkit.aas.aggregate;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import io.smallrye.mutiny.tuples.Tuple2;
import nl.vdijkit.aas.domain.*;
import nl.vdijkit.aas.webclient.TntWebClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.vdijkit.aas.domain.ItemType.*;

@ApplicationScoped
public class ReactiveDispatcher {
    private static final Logger LOGGER = Logger.getLogger(ReactiveDispatcher.class);
    private static final Consumer<ItemCompleted> responseSubscription = (ItemCompleted item) -> {
        item.getItemHandler().handle(item);
        LOGGER.infof("received: %s", item);
    };

    private final Map<ItemType, TntWebClient> webClients;
    private final BroadcastProcessor<RequestHandler> processor = BroadcastProcessor.create();

    @Inject
    public ReactiveDispatcher(Instance<TntWebClient> webClientInstances) {
        this.webClients = webClientInstances.stream().collect(Collectors.toMap(TntWebClient::getType, (webclient) -> webclient));
        registerProcessing();
    }

    public Uni<List<ItemCompleted>> process(Request request) {
        RequestHandler requestHandler = new RequestHandler(request);
        processor.onNext(requestHandler);
        return requestHandler.registerResponseConsumer();
    }

    public void registerProcessing() {

        processor.onItem()
                .transform(i -> i)
                .stage(listenerMulti -> {

                    Multi<Tuple2<ItemType, List<ItemInProcess>>> shippingGroup = groupOf5ForType(listenerMulti, SHIPMENTS);
                    Multi<Tuple2<ItemType, List<ItemInProcess>>> trackGroup = groupOf5ForType(listenerMulti, TRACK);
                    Multi<Tuple2<ItemType, List<ItemInProcess>>> pricingGroup = groupOf5ForType(listenerMulti, PRICING);

                    return Multi.createBy().merging().streams(shippingGroup, trackGroup, pricingGroup);
                })
                .flatMap(items -> {
                    TntWebClient client = webClients.get(items.getItem1());
                    return client.makeRequest(items.getItem2());
                })
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .subscribe().with(responseSubscription, (error) -> {
            LOGGER.errorf("blug: %s", error, error);
        });
    }

    private Multi<Tuple2<ItemType, List<ItemInProcess>>> groupOf5ForType(Multi<RequestHandler> requestHandler, ItemType type) {
        return requestHandler.flatMap(listener -> listener.itemsInProcess)
                .filter(itemInProcess -> type.equals(itemInProcess.getType()))
                .group()
                .intoLists()
                .of(5, Duration.of(5, ChronoUnit.SECONDS))
                .map(items -> Tuple2.of(type, items));
    }

    public static class RequestHandler implements ItemHandler {
        private final Multi<ItemInProcess> itemsInProcess;
        private final Queue<ItemCompleted> queue = new ConcurrentLinkedDeque<>();
        private int total;
        private final AtomicInteger counter = new AtomicInteger();
        private final Function<Request, List<ItemInProcess>> mapReqToItems = (req) -> {

            Stream<ItemInProcess> shipmentItems = req.getShipmentItems().stream().map(item -> new ItemInProcess(this, SHIPMENTS, item));
            Stream<ItemInProcess> trackItems = req.getTrackItems().stream().map(item -> new ItemInProcess(this, ItemType.TRACK, item));
            Stream<ItemInProcess> pricingItems = req.getPricingItems().stream().map(item -> new ItemInProcess(this, ItemType.PRICING, item));

            List<ItemInProcess> itemInProcessList = Stream.of(shipmentItems, trackItems, pricingItems).flatMap(items -> items).collect(Collectors.toList());
            total = itemInProcessList.size();
            return itemInProcessList;
        };

        private final Predicate<ItemCompleted> shouldHandleResponseItem;

        private final Predicate<ItemCompleted> isResponseComplete = (itemCompleted -> counter.get() < total);

        public RequestHandler(Request request) {
            registerResponseConsumer();

            itemsInProcess = Multi.createFrom()
                    .item(request)
                    .map(mapReqToItems)
                    .flatMap(items -> Multi.createFrom().iterable(items));

            shouldHandleResponseItem = (itemCompleted) -> itemsInProcess
                    .subscribe()
                    .asStream()
                    .anyMatch(itemInProcess -> itemInProcess.getItem().equals(itemCompleted.getReactiveItem().getItem()));
        }

        public Uni<List<ItemCompleted>> registerResponseConsumer() {
            return Multi.createBy()
                    .repeating()
                    .supplier(this::get)
                    .whilst(isResponseComplete)
                    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                    .collect().asList();
        }

        public void handle(ItemCompleted completed) {
            if (shouldHandleResponseItem.test(completed)) {
                queue.offer(completed);
                LOGGER.infof("handle completed item: %s", completed);
            }
        }

        public ItemCompleted get() {
            ItemCompleted completed = queue.poll();
            if (completed != null) {
                counter.incrementAndGet();
                LOGGER.infof("poll %s: %s", counter.get(), completed);
            }
            return completed;
        }
    }


}
