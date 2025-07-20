package de.turing85.url.shortener;

import java.net.MalformedURLException;
import java.net.URI;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

@QuarkusIntegrationTest
@TestHTTPEndpoint(ShortenedUrlEndpoint.class)
class ShortenedUrlEndpointIT {
  @Test
  void postAndGet() throws MalformedURLException {
    final String expectedUrl = "https://google.com";
    final String expectedShortened = "google";
    // @formatter:off
    RestAssured
        .given().that()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(new ShortenUrlDto(URI.create(expectedUrl).toURL(), expectedShortened))

        .when().post()

        .then().assertThat()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.CREATED.getStatusCode())
            .header(HttpHeaders.LOCATION, ShortenedUrlEndpointTest.pointsTo(expectedShortened))
            .body("url", is(expectedUrl))
            .body("shortened", is(expectedShortened));

    RestAssured
        .given().that()
            .redirects().follow(false)

        .when().get(expectedShortened)

        .then().assertThat()
            .statusCode(Response.Status.PERMANENT_REDIRECT.getStatusCode())
            .header(HttpHeaders.LOCATION, expectedUrl);

    RestAssured
        .when().delete(expectedShortened)

        .then().assertThat()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    RestAssured
        .when().get(expectedShortened)

        .then().assertThat()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    // @formatter:on
  }
}
