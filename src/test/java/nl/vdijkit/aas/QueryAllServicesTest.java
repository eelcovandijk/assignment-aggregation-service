package nl.vdijkit.aas;

import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.collection.IsIn;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

import static io.restassured.RestAssured.given;

/**
 * I've chosen to test against running services.
 * This made the implementation easier to do because now i know it works with the real implementation.
 * Normally i would switch the service for a mock object, e.g.using Mockito, for my unit tests.
 * Now the unit test could be flaky because of failures from the services and it is impossible to test the failure situations.
 *
 */
@QuarkusTest
public class QueryAllServicesTest {

    public static final BiFunction<Void, Throwable, Executable> EXECUTABLE_BI_FUNCTION = (ok, exception) -> () -> {
        if (null != exception) {
            throw exception;
        }
    };

    @Test
    public void aggregationEndpointShouldAcceptTrack() {
        List<String> expectedStatusses = List.of("NEW", "IN TRANSIT", "COLLECTING", "COLLECTED", "DELIVERING", "DELIVERED");

        given()
                .when().get("/aggregation?track=231")
                .then()
                .log().all()
                .statusCode(200)
                .body("track.231", IsIn.in(expectedStatusses));
    }

    @Test
    public void aggregationEndpointShouldAcceptTracks() {
        List<String> expectedStatusses = List.of("NEW", "IN TRANSIT", "COLLECTING", "COLLECTED", "DELIVERING", "DELIVERED");

        given()
                .when().get("/aggregation?track=231,232,233,234,235")
                .then()
                .log().all()
                .statusCode(200)
                .body("track.231", IsIn.in(expectedStatusses),
                        "track.232", IsIn.in(expectedStatusses),
                        "track.233", IsIn.in(expectedStatusses),
                        "track.234", IsIn.in(expectedStatusses),
                        "track.235", IsIn.in(expectedStatusses));
    }

    @Test
    public void aggregationEndpointShouldAcceptTracksFromMultipleRequests() throws InterruptedException, ExecutionException {
        List<String> expectedStatusses = List.of("NEW", "IN TRANSIT", "COLLECTING", "COLLECTED", "DELIVERING", "DELIVERED");


        Future<Executable> resultCall1 = CompletableFuture.runAsync(() -> {
            given()
                    .when().get("/aggregation?track=231,232,233")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("track.231", IsIn.in(expectedStatusses),
                            "track.232", IsIn.in(expectedStatusses),
                            "track.233", IsIn.in(expectedStatusses)).and();

        }).handle(EXECUTABLE_BI_FUNCTION);
        Future<Executable> resultCall2 = CompletableFuture.runAsync(() -> {
            given()
                    .when().get("/aggregation?track=234,235")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("track.234", IsIn.in(expectedStatusses),
                            "track.235", IsIn.in(expectedStatusses));
        }).handle(EXECUTABLE_BI_FUNCTION);

        Assertions.assertTimeoutPreemptively(Duration.ofMillis(8000), () -> {
            waitUntilDone(resultCall1, resultCall2);
            Assertions.assertDoesNotThrow(resultCall1.get());
            Assertions.assertDoesNotThrow(resultCall2.get());
        });
    }

    @Test
    public void aggregationEndpointShouldReturnTrackNullWhenCallFails() {

    }

    @Test
    public void aggregationEndpointShouldReturnTrackNullWhenCallTimedOut() {

    }

    @Test
    public void aggregationEndpointShouldAcceptPricing() {
        given()
                .when().get("/aggregation?pricing=XX")
                .then()
                .log().all()
                .statusCode(200)
                .body("pricing.XX", IsNull.notNullValue());
    }

    @Test
    public void aggregationEndpointShouldAcceptPricings() {
        given()
                .when().get("/aggregation?pricing=XX,AA,BB,CC,DD")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    public void aggregationEndpointShouldAcceptShipment() {
        given()
                .when().get("/aggregation?shipments=5")
                .then()
                .log().all()
                .statusCode(200)
                .body("shipments.5", IsNull.notNullValue());
    }

    @Test
    public void aggregationEndpointShouldAcceptShipments() {
        given()
                .when().get("/aggregation?shipments=1,2,3,4,5")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    public void aggregationEndpointShouldAcceptShipmentsTracksPrices() {
        given()
                .when().get("/aggregation?shipments=124,6,7,8,9&track=1,2,3,4,5&pricing=CH,TT,XX,II,NL")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    public void aggregationEndpointShouldReturnSameRequestWithSameResponse() {
        List<String> expectedStatusses = List.of("NEW", "IN TRANSIT", "COLLECTING", "COLLECTED", "DELIVERING", "DELIVERED");

        Future<Executable> resultCall1 = CompletableFuture.runAsync(() -> {
            given()
                    .when().get("/aggregation?track=231,232,233")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("track.231", IsIn.in(expectedStatusses),
                            "track.232", IsIn.in(expectedStatusses),
                            "track.233", IsIn.in(expectedStatusses));

        }).handle(EXECUTABLE_BI_FUNCTION);

        Future<Executable> resultCall2 = CompletableFuture.runAsync(() -> {
            given()
                    .when().get("/aggregation?track=231,232,233")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("track.231", IsIn.in(expectedStatusses),
                            "track.232", IsIn.in(expectedStatusses),
                            "track.233", IsIn.in(expectedStatusses));
        }).handle(EXECUTABLE_BI_FUNCTION);


        Assertions.assertTimeoutPreemptively(Duration.ofMillis(8000), () -> {
            waitUntilDone(resultCall1, resultCall2);
            Assertions.assertDoesNotThrow(resultCall1.get());
            Assertions.assertDoesNotThrow(resultCall2.get());
        });
    }

    public void waitUntilDone(Future<Executable> ...toWaitFor) throws InterruptedException {
        while (!Arrays.stream(toWaitFor).allMatch(Future::isDone)) {
            Thread.sleep(100);
        }
    }
}