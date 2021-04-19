package nl.vdijkit.aas.pricing;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Item;

import java.util.Objects;

public class Pricing implements Item {
    private final String country;
    private final Double amount;


    public Pricing(String country, Double amount) {
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

    public Double getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pricing pricing = (Pricing) o;
        return Objects.equals(country, pricing.country) &&
                Objects.equals(amount, pricing.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, amount);
    }

    @Override
    public String toString() {
        return "Pricing{" +
                "country='" + country + '\'' +
                ", amount=" + amount +
                '}';
    }
}
