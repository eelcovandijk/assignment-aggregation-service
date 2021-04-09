package nl.vdijkit.aas.track;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import nl.vdijkit.aas.webclient.ResponseItemMapper;
import nl.vdijkit.aas.domain.Item;
import nl.vdijkit.aas.webclient.AbstractTntWebClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TrackClient extends AbstractTntWebClient<Track> {
    private static final Logger LOGGER = Logger.getLogger(TrackClient.class);

    @Inject
    public TrackClient(Vertx vertx) {
        super(vertx, new TrackMapper(), "/track", "localhost", 8080);
    }

    @Override
    protected Class<Track> getItemClass() {
        return Track.class;
    }

    private static class TrackMapper implements ResponseItemMapper {
        public List<Item> mapResponse( JsonObject response) {
            LOGGER.infof("tracks received: '%s'", response.toString());
            response.getString("");
            return response.stream()
                    .map(entry -> new Track(entry.getKey(), (String) entry.getValue()))
                    .collect(Collectors.toList());
        }
    }
}
