package nl.vdijkit.aas.track;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Item;
import nl.vdijkit.aas.webclient.ResponseItemMapper;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class TrackMapper implements ResponseItemMapper {
    private static final Logger LOGGER = Logger.getLogger(TrackMapper.class);

    public List<Item> mapResponse(JsonObject response) {
        LOGGER.infof("tracks received: '%s'", response);
        if (null != response) {
            return response.stream()
                    .map(entry -> new Track(entry.getKey(), mapTrack(entry.getValue())))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String mapTrack(Object trackToMap) {
        if (trackToMap instanceof String) {
            return String.valueOf(trackToMap);
        }

        LOGGER.warnf("track in not mappable: %s", trackToMap);
        return null;
    }
}
