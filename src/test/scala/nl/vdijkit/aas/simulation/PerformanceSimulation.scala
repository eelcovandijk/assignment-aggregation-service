package nl.vdijkit.aas.simulation

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class PerformanceSimulation extends Simulation {
  val randomRequests: List[String] = List(
    "1, 2, 3, 4, 5",
    "1, 2, 3",
    "4, 5",
    "5",
    "1,5"
  )
  val productIndex = () => util.Random.nextInt(randomRequests.size)

  val httpProtocol = http
    .baseUrl("http://localhost:8082")
    .acceptEncodingHeader("gzip, deflate")

  val scn = scenario("BasicSimulation")
    .exec(session => {
      session.set("shipment", randomRequests(productIndex())).set("track", randomRequests(productIndex())).set("pricing", randomRequests(productIndex()))
    })
    .exec(http("aggregate")
      .get("/aggregation")
      .queryParam("shipments", "${shipment}")
      .queryParam("track", "${track}")
      .queryParam("pricing", "${pricing}"))

  setUp(
    scn.inject(rampUsers(500).during(10.minutes)).throttle(reachRps(100).in(10.seconds), holdFor(10.minutes))
  ).protocols(httpProtocol)
}