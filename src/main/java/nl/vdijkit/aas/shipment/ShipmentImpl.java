package nl.vdijkit.aas.shipment;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Shipment;

import java.util.Objects;

public class ShipmentImpl implements Shipment {
    private final String item;
    private final JsonArray products;


    public ShipmentImpl(String item, JsonArray products) {
        this.products = products;
        this.item = item;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject().put(item, products);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShipmentImpl track = (ShipmentImpl) o;
        return Objects.equals(products, track.products) &&
                Objects.equals(item, track.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(products, item);
    }

    @Override
    public String toString() {
        return "ShipmentImpl{" +
                "products='" + products + '\'' +
                ", item='" + item + '\'' +
                '}';
    }
}
