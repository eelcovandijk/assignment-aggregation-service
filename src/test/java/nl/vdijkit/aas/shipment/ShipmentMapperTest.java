package nl.vdijkit.aas.shipment;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.ItemType;
import nl.vdijkit.aas.domain.ReactiveItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentMapperTest {
    private final ShipmentMapper shipmentMapper = new ShipmentMapper();

    @Test
    void nullResponseShouldReturnEmptyList() {
        List<ReactiveItem> mappedResponse = shipmentMapper.mapResponse(null);
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void emptyResponseShouldReturnEmptyList() {
        List<ReactiveItem> mappedResponse = shipmentMapper.mapResponse(new JsonObject());
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void responseWithNullShipmentShouldReturnListWithShipmentNull() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.putNull("1");

        List<ReactiveItem> mappedResponse = shipmentMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            ReactiveItem item = mappedResponse.get(0);
            assertEquals(ItemType.SHIPMENTS, item.getType());
            assertEquals("1", item.getItem());
            assertNull(item.getProducts());
        });
    }

    @Test
    void responseWithInvalidShipmentShouldReturnListWithNullValue() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("5", "...");

        List<ReactiveItem> mappedResponse = shipmentMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            ReactiveItem item = mappedResponse.get(0);
            assertEquals(ItemType.SHIPMENTS, item.getType());
            assertEquals("5", item.getItem());
            assertNull(item.getProducts());
        });
    }
}