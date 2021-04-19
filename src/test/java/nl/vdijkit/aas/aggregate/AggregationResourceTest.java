package nl.vdijkit.aas.aggregate;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
class AggregationResourceTest {
    @Inject
    private AggregationResource aggregationResource;
    private static Dispatcher dispatcherMock = Mockito.mock(Dispatcher.class);

    @BeforeAll
    public static void setup() {
        QuarkusMock.installMockForType(dispatcherMock, Dispatcher.class);
    }

    @BeforeEach
    public void resetMocks() {
        Mockito.reset(dispatcherMock);
    }

    @Test
    void endpointShouldExceptNullParameters() {
        Uni<JsonObject> responseUni = aggregationResource.aggregate(null, null, null);
        JsonObject response = responseUni.await().atMost(Duration.ofSeconds(11));
        assertThat(response, Matchers.allOf(
                Matchers.notNullValue(),
                new JsonObjectKeyMatcher("shipments"),
                new JsonObjectKeyMatcher("track"),
                new JsonObjectKeyMatcher("pricing")
        ));
    }

    @Test
    void endpointShouldSkipEmptyParameters() {
        Uni<JsonObject> responseUni = aggregationResource.aggregate("", "", "");
        JsonObject response = responseUni.await().atMost(Duration.ofSeconds(11));
        assertThat(response, Matchers.allOf(
                Matchers.notNullValue(),
                new JsonObjectKeyMatcher("shipments"),
                new JsonObjectKeyMatcher("track"),
                new JsonObjectKeyMatcher("pricing")
        ));
    }

    @Test
    void endpointShouldExceptInvalidCSVSplittedParameters() {
        Uni<JsonObject> responseUni = aggregationResource.aggregate(",,,,,", null, null);
        JsonObject response = responseUni.await().atMost(Duration.ofSeconds(11));
        assertThat(response, Matchers.allOf(
                Matchers.notNullValue(),
                new JsonObjectKeyMatcher("shipments"),
                new JsonObjectKeyMatcher("track"),
                new JsonObjectKeyMatcher("pricing")
        ));
    }

    @Test
    void endpointShouldFilterEmptyItems() {
        Uni<JsonObject> responseUni = aggregationResource.aggregate("1,,2,,3,,4,,5", null, null);
        JsonObject response = responseUni.await().atMost(Duration.ofSeconds(11));

        assertThat(response, Matchers.allOf(
                Matchers.notNullValue(),
                new JsonObjectKeyMatcher("pricing")));

        AggregationRequestProcessor expectedAggregationRequestProcessor = new AggregationRequestProcessor(List.of("1","2","3","4","5"), Collections.emptyList(), Collections.emptyList());
        Mockito.verify(dispatcherMock).registerNewRequest(expectedAggregationRequestProcessor);
    }

    private static class JsonObjectKeyMatcher extends CustomMatcher<JsonObject> {
        private final String key;

        public JsonObjectKeyMatcher(String key) {
            super("has key '" + key + "' property in json");
            this.key = key;
        }

        public boolean matches(Object object) {
            return ((object instanceof JsonObject) && ((JsonObject) object).containsKey(key));
        }
    }
}