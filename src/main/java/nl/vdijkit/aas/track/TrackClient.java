package nl.vdijkit.aas.track;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import nl.vdijkit.aas.domain.TimedOutItem;
import nl.vdijkit.aas.domain.Track;
import nl.vdijkit.aas.domain.UnavailableItem;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class TrackClient {
    private static final Logger LOGGER = Logger.getLogger(TrackClient.class);
    private static final Function<HttpResponse<Buffer>, List<Track>> HTTP_RESPONSE_TRACK_FUNCTION = httpResponse -> {
        if (httpResponse.statusCode() == 200) {
            return new TrackMapper(httpResponse.bodyAsJsonObject()).mapResponse();
        }
        return List.of(new UnavailableItem());
    };
    private final WebClient client;

    @Inject
    public TrackClient(Vertx vertx) {
        this.client = WebClient.create(vertx,
                new WebClientOptions().setDefaultHost("localhost").setDefaultPort(8080));
    }

    public Uni<List<Track>> track(List<String> items) {
        return client.get("/track")
                .addQueryParam("q", String.join(",", items))
                .send()
                .map(HTTP_RESPONSE_TRACK_FUNCTION)
                .ifNoItem().after(Duration.ofMillis(5000))
                .recoverWithItem(List.of(new TimedOutItem()));
    }

    private static class TrackMapper {
        private final JsonObject response;

        public TrackMapper(JsonObject response) {
            this.response = response;
        }

        public List<Track> mapResponse() {
            LOGGER.infof("tracks received: '%s'", response.toString());
            response.getString("");
            return response.stream()
                    .map(entry -> new TrackImpl(entry.getKey(), (String) entry.getValue()))
                    .collect(Collectors.toList());
        }
    }
}
