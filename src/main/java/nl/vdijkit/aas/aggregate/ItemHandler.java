package nl.vdijkit.aas.aggregate;

import nl.vdijkit.aas.domain.ItemCompleted;

public interface ItemHandler {
    void handle(ItemCompleted completed);
}
