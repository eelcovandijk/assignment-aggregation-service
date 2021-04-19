package nl.vdijkit.aas.track;

import io.vertx.mutiny.core.Vertx;
import nl.vdijkit.aas.webclient.AbstractTntWebClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TrackClient extends AbstractTntWebClient<Track> {

    @Inject
    public TrackClient(Vertx vertx,
                       @ConfigProperty(name = "track.client.host", defaultValue = "localhost") String host,
                       @ConfigProperty(name = "track.client.port", defaultValue = "8080") Integer port) {
        super(vertx, new TrackMapper(), "/track", host, port);
    }

    @Override
    protected Class<Track> getItemClass() {
        return Track.class;
    }

}
