package nl.vdijkit.aas.webclient;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import nl.vdijkit.aas.domain.Item;
import nl.vdijkit.aas.domain.TimedOutItem;
import nl.vdijkit.aas.domain.UnavailableItem;
import nl.vdijkit.aas.track.TrackClient;
import nl.vdijkit.aas.track.Track;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractTntWebClient<T> implements TntWebClient {
    private static final Logger LOGGER = Logger.getLogger(TrackClient.class);
    private final WebClient client;
    private final String path;
    private final ResponseItemMapper mapper;

    public AbstractTntWebClient() {
        client = null;
        path = null;
        mapper = null;
    }

    public AbstractTntWebClient(Vertx vertx, ResponseItemMapper mapper, String path, String host, int port) {
        this.client = WebClient.create(vertx,
                new WebClientOptions().setDefaultHost(host).setDefaultPort(port));
        this.mapper = mapper;
        this.path = path;
    }

    @Override
    public Uni<List<Item>> makeRequest(List<String> items) {
        LOGGER.infof("%s to be requested: '%s'", getItemClass().getSimpleName(), items);
        return client.get(path)
                .addQueryParam("q", String.join(",", items))
                .send()
                .map(mapResponse(items))
                .ifNoItem().after(Duration.ofMillis(5000))
                .recoverWithItem(items.stream().map((item) -> new TimedOutItem<>(item, Track.class)).collect(Collectors.toList()));
    }

    private Function<HttpResponse<Buffer>, List<Item>> mapResponse(List<String> requestedItems) {
        return bufferHttpResponse -> {
            if (bufferHttpResponse.statusCode() == 200) {
                return mapper.mapResponse(bufferHttpResponse.bodyAsJsonObject());
            }
            return requestedItems.stream().map((item) -> new UnavailableItem<>(item, getItemClass())).collect(Collectors.toList());
        };
    }

    protected abstract Class<T> getItemClass();
}
