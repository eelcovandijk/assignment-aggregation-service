package nl.vdijkit.aas.webclient.pricing;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.ItemType;
import nl.vdijkit.aas.domain.ReactiveItem;
import nl.vdijkit.aas.webclient.pricing.PricingMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PricingClientMapperTest {
    private final PricingMapper pricingMapper = new PricingMapper();

    @Test
    void nullResponseShouldReturnEmptyList() {
        List<ReactiveItem> mappedResponse = pricingMapper.mapResponse(null);
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void emptyResponseShouldReturnEmptyList() {
        List<ReactiveItem> mappedResponse = pricingMapper.mapResponse(new JsonObject());
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void responseWithCountryCodeNullAmountShouldReturnListWithNullAmount() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.putNull("NL");

        List<ReactiveItem> mappedResponse = pricingMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            ReactiveItem item = mappedResponse.get(0);
            assertEquals(ItemType.PRICING, item.getType());
            assertEquals("NL", item.getItem());
            assertNull(item.getAmount());
        });
    }

    @Test
    void responseWithOneCountryCodeAndAmountShouldReturnListWithOneAmount() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("NL", 14.24209);

        List<ReactiveItem> mappedResponse = pricingMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            ReactiveItem item = mappedResponse.get(0);
            assertEquals(ItemType.PRICING, item.getType());
            assertEquals("NL", item.getItem());
            assertEquals(14.24209, item.getAmount());
        });
    }

    @Test
    void responseWithCountryCodeAndInvalidAmountShouldReturnListWithNullAmount() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("NL", "amount");

        List<ReactiveItem> mappedResponse = pricingMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            ReactiveItem item = mappedResponse.get(0);
            assertEquals(ItemType.PRICING, item.getType());
            assertEquals("NL", item.getItem());
            assertNull(item.getAmount());
        });
    }

    @Test
    void responseWithOneCountryCodeAndAmountAsStringShouldReturnListWithOneAmount() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("NL", "14.24209");

        List<ReactiveItem> mappedResponse = pricingMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            ReactiveItem item = mappedResponse.get(0);
            assertEquals(ItemType.PRICING, item.getType());
            assertEquals("NL", item.getItem());
            assertEquals(14.24209, item.getAmount());
        });
    }

    @Test
    void responseWithOneCountryCodeAndAmountAsIntegerShouldReturnListWithOneAmount() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("NL", 14);

        List<ReactiveItem> mappedResponse = pricingMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            ReactiveItem item = mappedResponse.get(0);
            assertEquals(ItemType.PRICING, item.getType());
            assertEquals("NL", item.getItem());
            assertEquals(14.00, item.getAmount());
        });
    }
}