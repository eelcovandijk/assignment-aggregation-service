package nl.vdijkit.aas.track;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Item;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackMapperTest {
    private final TrackMapper trackMapper = new TrackMapper();

    @Test
    void nullResponseShouldReturnEmptyList() {
        List<Item> mappedResponse = trackMapper.mapResponse(null);
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void emptyResponseShouldReturnEmptyList() {
        List<Item> mappedResponse = trackMapper.mapResponse(new JsonObject());
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void responseWithNullTrackShouldReturnListWithTrackNull() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.putNull("1");

        List<Item> mappedResponse = trackMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            Item item = mappedResponse.get(0);
            assertTrue(item instanceof Track);
            Track track = (Track) item;
            assertEquals("1", track.getItem());
            assertNull(track.getStatus());
        });
    }

    @Test
    void responseWithProcessedTrackShouldReturnListWithTrackAsProcessed() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("1", "PROCESSED");

        List<Item> mappedResponse = trackMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            Item item = mappedResponse.get(0);
            assertTrue(item instanceof Track);
            Track track = (Track) item;
            assertEquals("1", track.getItem());
            assertEquals("PROCESSED", track.getStatus());
        });
    }

}