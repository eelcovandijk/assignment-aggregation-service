package nl.vdijkit.aas.aggregate;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import io.smallrye.mutiny.tuples.Tuple2;
import nl.vdijkit.aas.domain.ItemInProcess;
import nl.vdijkit.aas.domain.ItemType;
import nl.vdijkit.aas.domain.ReactiveItem;
import nl.vdijkit.aas.webclient.TntWebClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.vdijkit.aas.domain.ItemType.*;

@ApplicationScoped
public class ReactiveDispatcher {
    private static final Logger LOGGER = Logger.getLogger(ReactiveDispatcher.class);
    private static final Function<Request, List<ItemInProcess>> MAP_REQUEST_TO_ITEMS = (req) -> {

        Stream<ItemInProcess> shipmentItems = req.getShipmentItems().stream().map(item -> new ItemInProcess(SHIPMENTS, item));
        Stream<ItemInProcess> trackItems = req.getTrackItems().stream().map(item -> new ItemInProcess(ItemType.TRACK, item));
        Stream<ItemInProcess> pricingItems = req.getPricingItems().stream().map(item -> new ItemInProcess(ItemType.PRICING, item));

        return Stream.of(shipmentItems, trackItems, pricingItems).flatMap(items -> items).collect(Collectors.toList());
    };

    private final Map<ItemType, TntWebClient> webClients;
    private final BroadcastProcessor<RequestHandler> processor = BroadcastProcessor.create();
    private final BroadcastProcessor<ReactiveItem> handler = BroadcastProcessor.create();


    @Inject
    public ReactiveDispatcher(Instance<TntWebClient> webClientInstances) {
        this.webClients = webClientInstances.stream().collect(Collectors.toMap(TntWebClient::getType, (webclient) -> webclient));
        registerProcessing();
    }

    public Uni<List<ReactiveItem>> process(Request request) {
        RequestHandler requestHandler = new RequestHandler(request);
        processor.onNext(requestHandler);
        return requestHandler.registerResponseConsumer();
    }

    public void registerProcessing() {

        processor.emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem()
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
                .subscribe()
                .withSubscriber(handler);
    }

    private Multi<Tuple2<ItemType, List<ItemInProcess>>> groupOf5ForType(Multi<RequestHandler> requestHandler, ItemType type) {
        return requestHandler.flatMap(handler -> Multi.createFrom().iterable(handler.itemsInProcess))
                .filter(itemInProcess -> type.equals(itemInProcess.getType()))
                .group()
                .intoLists()
                .of(5, Duration.of(5, ChronoUnit.SECONDS))
                .map(items -> Tuple2.of(type, items));
    }

    public class RequestHandler {

        private final List<ItemInProcess> itemsInProcess;
        private long total;
        private final AtomicInteger counter = new AtomicInteger();


        private final Predicate<ReactiveItem> isResponseComplete = (item -> {
            LOGGER.infof("is complete: %s < %s", counter.get(), total);
            return counter.get() < total;
        });

        public RequestHandler(Request request) {
            LOGGER.infof("Create RequestHandler: %s", request);

            itemsInProcess = MAP_REQUEST_TO_ITEMS.apply(request);
            total = itemsInProcess.size();
        }

        public Uni<List<ReactiveItem>> registerResponseConsumer() {
            return handler.onItem().transform(i -> i)
                    .filter(item -> itemsInProcess.stream().anyMatch(iip -> iip.getItem().equals(item.getItem())))
                    .onItem().invoke(counter::incrementAndGet)
                    .toUni().repeat().whilst(isResponseComplete)
                    .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                    .collect().asList();
        }
    }
}
