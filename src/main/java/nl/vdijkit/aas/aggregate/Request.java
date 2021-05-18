package nl.vdijkit.aas.aggregate;

import java.util.List;

public class Request {
    private final List<String> pricingItems;
    private final List<String> trackItems;
    private final List<String> shipmentItems;

    public Request(List<String> pricingItems, List<String> trackItems, List<String> shipmentItems) {
        this.pricingItems = pricingItems;
        this.trackItems = trackItems;
        this.shipmentItems = shipmentItems;
    }

    public List<String> getPricingItems() {
        return pricingItems;
    }

    public List<String> getTrackItems() {
        return trackItems;
    }

    public List<String> getShipmentItems() {
        return shipmentItems;
    }

    @Override
    public String toString() {
        return "Request{" +
                "pricingItems=" + pricingItems +
                ", trackItems=" + trackItems +
                ", shipmentItems=" + shipmentItems +
                '}';
    }
}
