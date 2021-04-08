package nl.vdijkit.aas;

import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.collection.IsIn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class QueryAllServicesTest {


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

        Future<Void> test1 = CompletableFuture.runAsync(() -> {
            given()
                    .when().get("/aggregation?track=231,232,233")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("track.231", IsIn.in(expectedStatusses),
                            "track.232", IsIn.in(expectedStatusses),
                            "track.233", IsIn.in(expectedStatusses)).and();

        });
        Future<Void> test2 = CompletableFuture.runAsync(() -> {
            given()
                    .when().get("/aggregation?track=234,235")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("track.234", IsIn.in(expectedStatusses),
                            "track.235", IsIn.in(expectedStatusses));
        });

        Thread.sleep(8000);
        Assertions.assertTrue(test1.isDone());
        Assertions.assertTrue(test2.isDone());
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
                .when().get("/aggregation?pricing=XX,AA,BB,CC,DD")
                .then()
                .log().all()
                .statusCode(200);
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

}