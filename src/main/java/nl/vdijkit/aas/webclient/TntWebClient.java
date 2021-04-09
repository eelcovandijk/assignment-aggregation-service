package nl.vdijkit.aas.webclient;

import io.smallrye.mutiny.Uni;
import nl.vdijkit.aas.domain.Item;

import java.util.List;

public interface TntWebClient {

    Uni<List<Item>> makeRequest(List<String> items);
}
