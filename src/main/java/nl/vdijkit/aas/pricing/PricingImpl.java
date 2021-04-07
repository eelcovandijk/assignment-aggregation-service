package nl.vdijkit.aas.pricing;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Pricing;
import nl.vdijkit.aas.domain.Shipment;

import java.util.Objects;

public class PricingImpl implements Pricing {
    private final String country;
    private final Double amount;


    public PricingImpl(String country, Double amount) {
        this.country = country;
        this.amount = amount;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject().put(country, amount);
    }


}
