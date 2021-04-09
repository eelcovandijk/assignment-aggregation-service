package nl.vdijkit.aas.aggregate;


import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Item;

import java.util.List;
import java.util.Objects;

public class Aggregate {

    private final List<Item> track;
    private final List<Item> shipments;
    private final List<Item> pricings;

    public Aggregate(List<Item> track, List<Item> shipments, List<Item> pricings) {
        this.track = track;
        this.pricings = pricings;
        this.shipments = shipments;
    }

    public List<Item> getTrack() {
        return track;
    }

    public JsonObject toJson() {
        JsonObject tracks = track.stream().map(Item::toJson).reduce(new JsonObject(), JsonObject::mergeIn);
        JsonObject shipments = this.shipments.stream().map(Item::toJson).reduce(new JsonObject(), JsonObject::mergeIn);
        JsonObject pricing = this.pricings.stream().map(Item::toJson).reduce(new JsonObject(), JsonObject::mergeIn);

        return new JsonObject().put("pricing", pricing).put("track", tracks).put("shipments", shipments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aggregate aggregate = (Aggregate) o;
        return Objects.equals(track, aggregate.track) &&
                Objects.equals(shipments, aggregate.shipments) &&
                Objects.equals(pricings, aggregate.pricings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(track, shipments, pricings);
    }

    @Override
    public String toString() {
        return "Aggregate{" +
                "track=" + track +
                ", shipments=" + shipments +
                ", pricings=" + pricings +
                '}';
    }
}
