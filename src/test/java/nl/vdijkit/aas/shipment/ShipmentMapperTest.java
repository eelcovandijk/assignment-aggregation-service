package nl.vdijkit.aas.shipment;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Item;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentMapperTest {
    private final ShipmentMapper shipmentMapper = new ShipmentMapper();

    @Test
    void nullResponseShouldReturnEmptyList() {
        List<Item> mappedResponse = shipmentMapper.mapResponse(null);
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void emptyResponseShouldReturnEmptyList() {
        List<Item> mappedResponse = shipmentMapper.mapResponse(new JsonObject());
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void responseWithNullShipmentShouldReturnListWithShipmentNull() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.putNull("1");

        List<Item> mappedResponse = shipmentMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            Item item = mappedResponse.get(0);
            assertTrue(item instanceof Shipment);
            Shipment shipment = (Shipment) item;
            assertEquals("1", shipment.getItem());
            assertNull(shipment.getProducts());
        });
    }

    @Test
    void responseWithInvalidShipmentShouldReturnListWithNullValue() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("5", "...");

        List<Item> mappedResponse = shipmentMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            Item item = mappedResponse.get(0);
            assertTrue(item instanceof Shipment);
            Shipment shipment = (Shipment) item;
            assertEquals("5", shipment.getItem());
            assertNull(shipment.getProducts());
        });
    }
}