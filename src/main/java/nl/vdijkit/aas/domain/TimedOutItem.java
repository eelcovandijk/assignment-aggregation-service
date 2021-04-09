package nl.vdijkit.aas.domain;

public class TimedOutItem<T> extends UnavailableItem<T> {

    public TimedOutItem(String item, Class<T> itemType) {
        super(item, itemType);
    }
}
