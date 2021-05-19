package nl.vdijkit.aas.webclient;

import io.smallrye.mutiny.Multi;
import nl.vdijkit.aas.domain.*;

import java.util.List;

public interface TntWebClient {

    Multi<ReactiveItem> makeRequest(List<ItemInProcess> items);

    ItemType getType();
}
