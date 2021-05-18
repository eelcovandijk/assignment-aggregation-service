package nl.vdijkit.aas.webclient;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import nl.vdijkit.aas.aggregate.ItemHandler;
import nl.vdijkit.aas.domain.*;

import java.util.List;

public interface TntWebClient {

    Multi<ItemCompleted> makeRequest(List<ItemInProcess> items);

    ItemType getType();
}
