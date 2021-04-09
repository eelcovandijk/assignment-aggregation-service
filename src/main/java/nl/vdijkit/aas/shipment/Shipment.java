package nl.vdijkit.aas.shipment;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Item;

import java.util.Objects;

public class Shipment implements Item {
    private final String item;
    private final JsonArray products;


    public Shipment(String item, JsonArray products) {
        this.products = products;
        this.item = item;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject().put(item, products);
    }

    @Override
    public String getItem() {
        return item;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shipment track = (Shipment) o;
        return Objects.equals(products, track.products) &&
                Objects.equals(item, track.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(products, item);
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "products='" + products + '\'' +
                ", item='" + item + '\'' +
                '}';
    }
}
