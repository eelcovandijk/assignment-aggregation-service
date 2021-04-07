package nl.vdijkit.aas.aggregate;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Pricing;
import nl.vdijkit.aas.domain.Shipment;
import nl.vdijkit.aas.domain.Track;

import java.util.List;
import java.util.Objects;

public class AggregateImpl {

    private final List<Track> track;
    private final List<Shipment> shipments;
    private final List<Pricing> pricings;

    public AggregateImpl(List<Track> track, List<Shipment>shipments, List<Pricing> pricings) {
        this.track = track;
        this.pricings = pricings;
        this.shipments = shipments;
    }

    public List<Track> getTrack() {
        return track;
    }

    public JsonObject toJson() {
        JsonObject tracks = track.stream().map(Track::toJson).reduce(new JsonObject(), JsonObject::mergeIn);
        JsonObject shipments = this.shipments.stream().map(Shipment::toJson).reduce(new JsonObject(), JsonObject::mergeIn);
        JsonObject pricing = this.pricings.stream().map(Pricing::toJson).reduce(new JsonObject(), JsonObject::mergeIn);

        return new JsonObject().put("pricing", pricing).put("track", tracks).put("shipments", shipments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregateImpl aggregate = (AggregateImpl) o;
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
        return "AggregateImpl{" +
                "track=" + track +
                ", shipments=" + shipments +
                ", pricings=" + pricings +
                '}';
    }
}
