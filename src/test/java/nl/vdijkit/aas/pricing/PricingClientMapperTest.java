package nl.vdijkit.aas.pricing;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Item;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PricingClientMapperTest {
    private final PricingMapper pricingMapper = new PricingMapper();

    @Test
    void nullResponseShouldReturnEmptyList() {
        List<Item> mappedResponse = pricingMapper.mapResponse(null);
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void emptyResponseShouldReturnEmptyList() {
        List<Item> mappedResponse = pricingMapper.mapResponse(new JsonObject());
        assertNotNull(mappedResponse);
        assertTrue(mappedResponse.isEmpty());
    }

    @Test
    void responseWithCountryCodeNullAmountShouldReturnListWithNullAmount() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.putNull("NL");

        List<Item> mappedResponse = pricingMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            Item item = mappedResponse.get(0);
            assertTrue(item instanceof Pricing);
            Pricing pricing = (Pricing) item;
            assertEquals("NL", pricing.getItem());
            assertNull(pricing.getAmount());
        });
    }

    @Test
    void responseWithOneCountryCodeAndAmountShouldReturnListWithOneAmount() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("NL", 14.24209);

        List<Item> mappedResponse = pricingMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            Item item = mappedResponse.get(0);
            assertTrue(item instanceof Pricing);
            Pricing pricing = (Pricing) item;
            assertEquals("NL", pricing.getItem());
            assertEquals(14.24209, pricing.getAmount());
        });
    }

    @Test
    void responseWithCountryCodeAndInvalidAmountShouldReturnListWithNullAmount() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("NL", "amount");

        List<Item> mappedResponse = pricingMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            Item item = mappedResponse.get(0);
            assertTrue(item instanceof Pricing);
            Pricing pricing = (Pricing) item;
            assertEquals("NL", pricing.getItem());
            assertNull(pricing.getAmount());
        });
    }

    @Test
    void responseWithOneCountryCodeAndAmountAsStringShouldReturnListWithOneAmount() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("NL", "14.24209");

        List<Item> mappedResponse = pricingMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            Item item = mappedResponse.get(0);
            assertTrue(item instanceof Pricing);
            Pricing pricing = (Pricing) item;
            assertEquals("NL", pricing.getItem());
            assertEquals(14.24209, pricing.getAmount());
        });
    }

    @Test
    void responseWithOneCountryCodeAndAmountAsIntegerShouldReturnListWithOneAmount() {
        JsonObject pricingResponse = new JsonObject();
        pricingResponse.put("NL", 14);

        List<Item> mappedResponse = pricingMapper.mapResponse(pricingResponse);

        assertAll(() -> {
            assertEquals(1, mappedResponse.size());
            Item item = mappedResponse.get(0);
            assertTrue(item instanceof Pricing);
            Pricing pricing = (Pricing) item;
            assertEquals("NL", pricing.getItem());
            assertEquals(14.00, pricing.getAmount());
        });
    }
}