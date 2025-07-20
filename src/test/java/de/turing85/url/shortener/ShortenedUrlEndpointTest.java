package de.turing85.url.shortener;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import de.turing85.url.shortener.entity.ShortenedUrl;
import de.turing85.url.shortener.rider.replacers.ShortenedLengthReplacer;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

@QuarkusTest
@TestHTTPEndpoint(ShortenedUrlEndpoint.class)
@DBRider
@DBUnit(schema = "public", caseSensitiveTableNames = true, cacheConnection = false,
    replacers = ShortenedLengthReplacer.class)
class ShortenedUrlEndpointTest {
  @Test
  @DataSet(cleanBefore = true)
  @ExpectedDataSet("expected/google-google.yml")
  void postWithUrlAndShortened() throws MalformedURLException {
    final URL expectedUrl = URI.create("https://google.com").toURL();
    final String expectedShortened = "google";
    // @formatter:off
    RestAssured
        .given().that()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(new ShortenUrlDto(expectedUrl, expectedShortened))

        .when().post()

        .then().assertThat()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.CREATED.getStatusCode())
            .header(HttpHeaders.LOCATION, pointsTo(expectedShortened))
            .body("url", is(expectedUrl.toString()))
            .body("shortened", is(expectedShortened));
    // @formatter:on
  }

  @Test
  @DataSet("given/google-google.yml")
  @ExpectedDataSet("expected/google-google.yml")
  void postWithUrlAndShortenedUrlAlreadyExisting() throws MalformedURLException {
    final URL epxectedUrl = URI.create("https://google.com").toURL();
    final String expectedShortened = "google";
    // @formatter:off
    RestAssured
        .given().that()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(new ShortenUrlDto(epxectedUrl, "foo"))

        .when().post()

        .then().assertThat()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.OK.getStatusCode())
            .header(HttpHeaders.LOCATION, pointsTo(expectedShortened))
            .body("url", is(epxectedUrl.toString()))
            .body("shortened", is(expectedShortened));
    // @formatter:on
  }

  @Test
  @DataSet("given/google-google.yml")
  @ExpectedDataSet("expected/google-google.yml")
  void postWithUrlAndShortenedShortenedAlreadyExisting() throws MalformedURLException {
    final URL expectedUrl = URI.create("https://something.else").toURL();
    // @formatter:off
    RestAssured
        .given().that()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(new ShortenUrlDto(expectedUrl, "google"))

        .when().post()

        .then().assertThat()
            .statusCode(Response.Status.CONFLICT.getStatusCode());
    // @formatter:on
  }

  @Test
  @DataSet(cleanBefore = true)
  @ExpectedDataSet("expected/google-random.yml")
  void postWithUrl() throws MalformedURLException {
    final URL expectedUrl = URI.create("https://google.com").toURL();
    // @formatter:off
    final ValidatableResponse options = RestAssured
        .given().that()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).body(new ShortenUrlDto(expectedUrl, null))

        .when().post()

        .then().assertThat()
            .contentType(ContentType.JSON)
            .statusCode(Response.Status.CREATED.getStatusCode())
            .header(
                HttpHeaders.LOCATION,
                matchesPattern("%s:%s%s/[a-zA-Z0-9]{%d}".formatted(
                    RestAssured.baseURI,
                    RestAssured.port,
                    RestAssured.basePath,
                    ShortenedUrl.SHORTENED_MAX_LENGTH)))
            .body("url", is(expectedUrl.toString()))
            .body(
                "shortened",
                matchesPattern("[a-zA-Z0-9]{%d}".formatted(ShortenedUrl.SHORTENED_MAX_LENGTH)));
    final String actualLocationHeader = options.extract().header(HttpHeaders.LOCATION);
    final String actualShortenedFromHeader = actualLocationHeader
        .substring(actualLocationHeader.length() - ShortenedUrl.SHORTENED_MAX_LENGTH);
    final String actualShortenedFromBody = options.extract().body().path("shortened");
    assertThat(actualShortenedFromHeader, is(actualShortenedFromBody));
    // @formatter:on
  }

  @Test
  @DataSet(cleanBefore = true)
  @ExpectedDataSet("expected/empty.yml")
  void getNonExisting() {
    // @formatter:off
    RestAssured
        .when().get("foo")

        .then().assertThat()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    // @formatter:on
  }

  @Test
  @DataSet("given/google-google.yml")
  @ExpectedDataSet("expected/google-google.yml")
  void getExisting() {
    // @formatter:off
    RestAssured
        .given().that()
            .redirects().follow(false)

        .when().get("google")

        .then().assertThat()
            .statusCode(Response.Status.PERMANENT_REDIRECT.getStatusCode())
            .header(HttpHeaders.LOCATION, "https://google.com");
    // @formatter:on
  }

  @Test
  @DataSet("given/google-google.yml")
  @ExpectedDataSet("expected/empty.yml")
  void deleteExisting() {
    // @formatter:off
    RestAssured
        .when().delete("google")

        .then().assertThat()
        .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    // @formatter:on
  }

  @Test
  @DataSet(cleanBefore = true)
  @ExpectedDataSet("expected/empty.yml")
  void deleteNonExisting() {
    // @formatter:off
    RestAssured
        .when().delete("foo")

        .then().assertThat()
        .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    // @formatter:on
  }

  static Matcher<String> pointsTo(final String shortened) {
    // @formatter:off
    return is("%s:%s%s/%s".formatted(
        RestAssured.baseURI,
        RestAssured.port,
        RestAssured.basePath,
        shortened));
    // @formatter:of
  }
}
