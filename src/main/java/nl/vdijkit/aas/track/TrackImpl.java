package nl.vdijkit.aas.track;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.Track;

import java.util.Objects;

public class TrackImpl implements Track {
    private final String item;
    private final String status;


    public TrackImpl(String item, String status) {
        this.item = item;
        this.status = status;
    }

    @Override
    public String getItem() {
        return item;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject().put(item, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackImpl track = (TrackImpl) o;
        return Objects.equals(item, track.item) &&
                Objects.equals(status, track.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, status);
    }

    @Override
    public String toString() {
        return "TrackImpl{" +
                "item='" + item + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
