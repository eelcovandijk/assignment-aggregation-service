package nl.vdijkit.aas.aggregate;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    private static ReactiveDispatcher dispatcherMock = Mockito.mock(ReactiveDispatcher.class);

    @BeforeAll
    public static void setup() {
        QuarkusMock.installMockForType(dispatcherMock, ReactiveDispatcher.class);
    }

    @BeforeEach
    public void resetMocks() {
        Mockito.reset(dispatcherMock);
    }

    @Test
    void endpointShouldExceptNullParameters() {
        Uni<JsonObject> responseUni = aggregationResource.aggregate(null, null, null);
        JsonObject response = responseUni.await().atMost(Duration.ofSeconds(11));
        assertThat(response, Matchers.notNullValue());
    }

    @Test
    void endpointShouldSkipEmptyParameters() {
        Uni<JsonObject> responseUni = aggregationResource.aggregate("", "", "");
        JsonObject response = responseUni.await().atMost(Duration.ofSeconds(11));
        assertThat(response, Matchers.notNullValue());
    }

    @Test
    void endpointShouldExceptInvalidCSVSplittedParameters() {
        Uni<JsonObject> responseUni = aggregationResource.aggregate(",,,,,", null, null);
        JsonObject response = responseUni.await().atMost(Duration.ofSeconds(11));
        assertThat(response, Matchers.notNullValue());
    }

    @Test
    void endpointShouldFilterEmptyItems() {
        Uni<JsonObject> responseUni = aggregationResource.aggregate("1,,2,,3,,4,,5", null, null);
        JsonObject response = responseUni.await().atMost(Duration.ofSeconds(11));

        assertThat(response, Matchers.notNullValue());
    }

}