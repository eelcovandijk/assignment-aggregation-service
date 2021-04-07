package nl.vdijkit.aas;

import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.collection.IsIn;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class QueryAllServicesTest {


    @Test
    public void aggregationEndpointShouldAcceptTrack() {
        List<String> expectedStatusses = List.of("NEW", "IN TRANSIT", "COLLECTING", "COLLECTED", "DELIVERING", "DELIVERED");

        given()
                .when().get("/aggregation?track=234")
                .then()
                .log().all()
                .statusCode(200)
                .body("track.234", IsIn.in(expectedStatusses));
    }

    @Test
    public void aggregationEndpointShouldAcceptTracks() {
        List<String> expectedStatusses = List.of("NEW", "IN TRANSIT", "COLLECTING", "COLLECTED", "DELIVERING", "DELIVERED");

        given()
                .when().get("/aggregation?track=123,345")
                .then()
                .log().all()
                .statusCode(200)
                .body("track.123", IsIn.in(expectedStatusses),
                        "track.345", IsIn.in(expectedStatusses));
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
                .statusCode(200);
    }

    @Test
    public void aggregationEndpointShouldAcceptShipments() {
        given()
                .when().get("/aggregation?shipments=124")
                .then()
                .statusCode(200);
    }

    @Test
    public void aggregationEndpointShouldAcceptShipmentsTracksPrices() {
        given()
                .when().get("/aggregation?shipments=124&track=5,6&pricing=CH")
                .then()
                .log().all()
                .statusCode(200);
    }

}