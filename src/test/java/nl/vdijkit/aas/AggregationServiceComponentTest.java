package nl.vdijkit.aas;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.collection.IsIn;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;

/**
 * It's possible to test against running services.
 * This made the implementation easier to do because now i know it works with the real implementation.
 * With the variable IS_WITH_MOCKED_SERVICES it is possible to switch the real services for wiremock services
 */
@QuarkusTest
public class AggregationServiceComponentTest {
    private static final Boolean IS_WITH_MOCKED_SERVICES = true;
    private static final BiFunction<Void, Throwable, Executable> EXECUTABLE_BI_FUNCTION = (ok, exception) -> () -> {
        if (null != exception) {
            throw exception;
        }
    };
    private static final List<String> EXPECTED_TRACK_STATUSSES = List.of("NEW", "IN TRANSIT", "COLLECTING", "COLLECTED", "DELIVERING", "DELIVERED");
    private static WireMockServer wireMockServer;


    @BeforeAll
    static void setup() {
        if (IS_WITH_MOCKED_SERVICES) {
            wireMockServer = new WireMockServer();
            wireMockServer.start();
        }
    }

    @AfterAll
    static void tearDown() {
        if (IS_WITH_MOCKED_SERVICES) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void reset() {
        if (IS_WITH_MOCKED_SERVICES) {
            wireMockServer.resetAll();
        }
    }


    @Test
    public void aggregationEndpointShouldAcceptTrack() {
        setMockOnlyWhenEnabled(this::mockTrackCall);

        given()
                .when().get("/aggregation?track=231")
                .then()
                .log().all()
                .statusCode(200)
                .body("track.231", IsIn.in(EXPECTED_TRACK_STATUSSES));
    }

    @Test
    public void aggregationEndpointShouldAcceptTracks() {
        setMockOnlyWhenEnabled(this::mockTracksCall);


        given()
                .when().get("/aggregation?track=231,232,233,234,235")
                .then()
                .log().all()
                .statusCode(200)
                .body("track.231", IsIn.in(EXPECTED_TRACK_STATUSSES),
                        "track.232", IsIn.in(EXPECTED_TRACK_STATUSSES),
                        "track.233", IsIn.in(EXPECTED_TRACK_STATUSSES),
                        "track.234", IsIn.in(EXPECTED_TRACK_STATUSSES),
                        "track.235", IsIn.in(EXPECTED_TRACK_STATUSSES));
    }

    @Test
    public void aggregationEndpointShouldAcceptTracksFromMultipleRequests() {

        setMockOnlyWhenEnabled(this::mockTracksCall);

        Future<Executable> resultCall1 = CompletableFuture.runAsync(() -> {
            given()
                    .when().get("/aggregation?track=231,232,233")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("track.231", IsIn.in(EXPECTED_TRACK_STATUSSES),
                            "track.232", IsIn.in(EXPECTED_TRACK_STATUSSES),
                            "track.233", IsIn.in(EXPECTED_TRACK_STATUSSES)).and();

        }).handle(EXECUTABLE_BI_FUNCTION);
        Future<Executable> resultCall2 = CompletableFuture.runAsync(() -> {
            given()
                    .when().get("/aggregation?track=234,235")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("track.234", IsIn.in(EXPECTED_TRACK_STATUSSES),
                            "track.235", IsIn.in(EXPECTED_TRACK_STATUSSES));
        }).handle(EXECUTABLE_BI_FUNCTION);

        Assertions.assertTimeoutPreemptively(Duration.ofMillis(8000), () -> {
            waitUntilDone(resultCall1, resultCall2);
            Assertions.assertDoesNotThrow(resultCall1.get());
            Assertions.assertDoesNotThrow(resultCall2.get());
        });
    }

    @Test
    public void aggregationEndpointShouldAcceptPricing() {
        setMockOnlyWhenEnabled(this::mockPricingCall);

        given()
                .when().get("/aggregation?pricing=XX")
                .then()
                .log().all()
                .statusCode(200)
                .body("pricing.XX", Is.is(14.234567F));
    }

    @Test
    public void aggregationEndpointShouldAcceptPricings() {
        setMockOnlyWhenEnabled(this::mockPrincingsCall);

        given()
                .when().get("/aggregation?pricing=XX,AA,BB,CC,DD")
                .then()
                .log().all()
                .statusCode(200)
                .body("pricing.XX", Is.is(14.234567F),
                        "pricing.AA", Is.is(14.234567F),
                        "pricing.BB", Is.is(14.234567F),
                        "pricing.CC", Is.is(14.000000F),
                        "pricing.DD", Is.is(14.000000F));
    }

    @Test
    public void aggregationEndpointShouldAcceptShipment() {

        setMockOnlyWhenEnabled(this::mockShipmentCall);

        given()
                .when().get("/aggregation?shipments=5")
                .then()
                .log().all()
                .statusCode(200)
                .body("shipments.5", IsNull.notNullValue());
    }

    @Test
    public void aggregationEndpointShouldAcceptShipments() {
        setMockOnlyWhenEnabled(this::mockShipmentsCall);


        given()
                .when().get("/aggregation?shipments=1,2,3,4,5")
                .then()
                .log().all()
                .statusCode(200)
                .body("shipments.1", IsNull.notNullValue(),
                        "shipments.2", IsNull.notNullValue(),
                        "shipments.3", IsNull.notNullValue(),
                        "shipments.4", IsNull.notNullValue(),
                        "shipments.5", IsNull.notNullValue());
    }

    @Test
    public void aggregationEndpointShouldAcceptShipmentsTracksPrices() {
        setMockOnlyWhenEnabled(this::mockPrincingsCall);
        setMockOnlyWhenEnabled(this::mockShipmentsCall);
        setMockOnlyWhenEnabled(this::mockTracksCall);

        given()
                .when().get("/aggregation?shipments=1,2,3,4,5&track=231,232,233,234,235&pricing=XX,AA,BB,CC,DD")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    public void aggregationEndpointShouldReturnSameRequestWithSameResponse() {
        setMockOnlyWhenEnabled(this::mockTracksCallWithSameItems);

        Future<Executable> resultCall1 = CompletableFuture.runAsync(() -> {
            given()
                    .when().get("/aggregation?track=231,232,233")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("track.231", IsIn.in(EXPECTED_TRACK_STATUSSES),
                            "track.232", IsIn.in(EXPECTED_TRACK_STATUSSES),
                            "track.233", IsIn.in(EXPECTED_TRACK_STATUSSES));

        }).handle(EXECUTABLE_BI_FUNCTION);

        Future<Executable> resultCall2 = CompletableFuture.runAsync(() -> {
            given()
                    .when().get("/aggregation?track=231,232,233")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("track.231", IsIn.in(EXPECTED_TRACK_STATUSSES),
                            "track.232", IsIn.in(EXPECTED_TRACK_STATUSSES),
                            "track.233", IsIn.in(EXPECTED_TRACK_STATUSSES));
        }).handle(EXECUTABLE_BI_FUNCTION);


        Assertions.assertTimeoutPreemptively(Duration.ofMillis(11000), () -> {
            waitUntilDone(resultCall1, resultCall2);
            Assertions.assertDoesNotThrow(resultCall1.get());
            Assertions.assertDoesNotThrow(resultCall2.get());
        });
    }

    public void waitUntilDone(Future<Executable>... toWaitFor) throws InterruptedException {
        while (!Arrays.stream(toWaitFor).allMatch(Future::isDone)) {
            Thread.sleep(100);
        }
    }

    void setMockOnlyWhenEnabled(Runnable mockCall) {
        if (IS_WITH_MOCKED_SERVICES) {
            mockCall.run();
        }
    }

    private void mockTrackCall() {
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/track")).willReturn(WireMock.aResponse()
                .withBody(
                        "{\"231\":\"NEW\"}")));
    }

    private void mockPricingCall() {
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/pricing")).willReturn(WireMock.aResponse()
                .withBody(
                        "{\"XX\":14.234566456456546}")));
    }

    private void mockShipmentCall() {
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/shipments")).willReturn(WireMock.aResponse()
                .withBody(
                        "{\"5\":[\"box\",\"pallet\",\"envelope\",\"box\",\"box\"]}")));
    }

    private void mockShipmentsCall() {
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/shipments")).willReturn(WireMock.aResponse()
                .withBody(
                        "{\"1\":[\"box\"]," +
                                "\"2\":[\"box\",\"pallet\"]," +
                                "\"3\":[\"box\",\"pallet\",\"envelope\"]," +
                                "\"4\":[\"box\",\"pallet\",\"envelope\",\"box\"]," +
                                "\"5\":[\"box\",\"pallet\",\"envelope\",\"box\",\"box\"]}")));
    }

    private void mockPrincingsCall() {
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/pricing")).willReturn(WireMock.aResponse()
                .withBody(
                        "{\"XX\":14.234566456456546," +
                                "\"AA\":14.234566456456546," +
                                "\"BB\":\"14.234566456456546\"," +
                                "\"CC\":14.00000," +
                                "\"DD\":14}")));
    }

    private void mockTracksCall() {
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/track")).willReturn(WireMock.aResponse()
                .withBody(
                        "{\"231\":\"NEW\"," +
                                "\"232\":\"COLLECTED\"," +
                                "\"233\":\"IN TRANSIT\"," +
                                "\"234\":\"DELIVERING\"," +
                                "\"235\":\"DELIVERED\"}")));
    }

    private void mockTracksCallWithSameItems() {
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/track")).willReturn(WireMock.aResponse()
                .withBody(
                        "{\"231\":\"NEW\"," +
                                "\"232\":\"COLLECTED\"," +
                                "\"233\":\"IN TRANSIT\"," +
                                "\"231\":\"NEW\"," +
                                "\"232\":\"COLLECTED\"}")));
    }
}