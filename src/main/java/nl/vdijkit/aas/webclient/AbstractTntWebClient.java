package nl.vdijkit.aas.webclient;

import io.smallrye.mutiny.Multi;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import nl.vdijkit.aas.aggregate.ItemHandler;
import nl.vdijkit.aas.domain.*;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractTntWebClient implements TntWebClient {
    private static final Logger LOGGER = Logger.getLogger(AbstractTntWebClient.class);
    private final WebClient client;
    private final String path;
    private final ResponseItemMapper mapper;
    private final ItemType type;

    public AbstractTntWebClient() {
        client = null;
        path = null;
        mapper = null;
        type = null;
    }

    public AbstractTntWebClient(Vertx vertx, ResponseItemMapper mapper, String path, String host, int port, ItemType type) {
        this.client = WebClient.create(vertx,
                new WebClientOptions().setDefaultHost(host).setDefaultPort(port));
        this.mapper = mapper;
        this.path = path;
        this.type = type;
    }

    @Override
    public Multi<ItemCompleted> makeRequest(List<ItemInProcess> items) {
        LOGGER.infof("%s to be requested: '%s'", getType(), items);

        List<String> requestedItems = items.stream().map(ItemInProcess::getItem).collect(Collectors.toList());

        return client.get(path)
                .addQueryParam("q", String.join(",", requestedItems))
                .send()
                .map(mapResponse(requestedItems))
                .ifNoItem().after(Duration.ofMillis(6000))
                .recoverWithItem(() -> {
                    LOGGER.errorf("Failed to request %s for items: '%s' with resource: %s", this.getType(), items, this.path);
                    return items.stream().map((item) -> new TimedOutItem(item.getItem(), getType())).collect(Collectors.toList());
                })

                .toMulti()
                .flatMap(responseItems -> Multi.createFrom().iterable(responseItems))
                .map(responseItem -> {
                    ItemHandler itemHandler = items.stream()
                            .filter(inprogress -> inprogress.getItem().equals(responseItem.getItem()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No handler found for response: " + responseItem))
                            .getItemHandler();
                    return new ItemCompleted(itemHandler, responseItem);
                });
    }

    private Function<HttpResponse<Buffer>, List<ReactiveItem>> mapResponse(List<String> requestedItems) {
        return bufferHttpResponse -> {
            if (bufferHttpResponse.statusCode() == 200) {
               return mapResponseObject(bufferHttpResponse, requestedItems);
            }
            LOGGER.warnf("Received invalid response status while requesting %s for items: '%s' with path: %s. Response: %s", this.getType(), requestedItems, this.path, bufferHttpResponse.toString());
            return requestedItems.stream().map((item) -> new UnavailableItem(item, getType())).collect(Collectors.toList());
        };
    }

    private List<ReactiveItem> mapResponseObject(HttpResponse<Buffer> response, List<String> requestedItems) {
        try {
            JsonObject responseObject = response.bodyAsJsonObject();
            return mapper.mapResponse(responseObject);
        } catch (DecodeException decodeException) {
            LOGGER.error("Failed to decode json response while requesting items:  " + requestedItems, decodeException);
            return requestedItems.stream().map((item) -> new UnavailableItem(item, getType())).collect(Collectors.toList());
        }
    }

    public ItemType getType() {
        return type;
    }
}
