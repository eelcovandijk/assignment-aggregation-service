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

    @Override
    public String getItem() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PricingImpl pricing = (PricingImpl) o;
        return Objects.equals(country, pricing.country) &&
                Objects.equals(amount, pricing.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, amount);
    }

    @Override
    public String toString() {
        return "PricingImpl{" +
                "country='" + country + '\'' +
                ", amount=" + amount +
                '}';
    }
}
