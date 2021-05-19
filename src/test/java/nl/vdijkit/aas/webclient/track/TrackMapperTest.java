package nl.vdijkit.aas.webclient.track;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.ItemType;
import nl.vdijkit.aas.domain.ReactiveItem;
import nl.vdijkit.aas.webclient.track.TrackMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackMapperTest {
    private final TrackMapper trackMapper = new TrackMapper();

    @Test
    void nullResponseShouldReturnEmptyList() {
        List<ReactiveItem> mappedResponse = trackMapper.mapResponse(null);
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void emptyResponseShouldReturnEmptyList() {
        List<ReactiveItem> mappedResponse = trackMapper.mapResponse(new JsonObject());
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void responseWithNullTrackShouldReturnListWithTrackNull() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.putNull("1");

        List<ReactiveItem> mappedResponse = trackMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            ReactiveItem item = mappedResponse.get(0);
            assertEquals("1", item.getItem());
            assertEquals(ItemType.TRACK, item.getType());
            assertNull(item.getStatus());
        });
    }

    @Test
    void responseWithProcessedTrackShouldReturnListWithTrackAsProcessed() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("1", "PROCESSED");

        List<ReactiveItem> mappedResponse = trackMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            ReactiveItem item = mappedResponse.get(0);
            assertEquals(ItemType.TRACK, item.getType());
            assertEquals("1", item.getItem());
            assertEquals("PROCESSED", item.getStatus());
        });
    }

}