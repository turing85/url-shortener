package de.turing85.url.shortener;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import de.turing85.url.shortener.entity.ShortenedUrl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

@Path(ShortenedUrlEndpoint.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
@RequiredArgsConstructor
public final class ShortenedUrlEndpoint {
  public static final String PATH = "shorten";

  @POST
  @Transactional
  public Response shortenUrl(@Valid final ShortenUrlDto shortenUrlDto)
      throws MalformedURLException {
    final Optional<ShortenedUrl> maybeShortenedUrl = ShortenedUrl.findByUrl(shortenUrlDto.url());
    if (maybeShortenedUrl.isPresent()) {
      final ShortenedUrl shortenedUrl = maybeShortenedUrl.get();
      return Response.ok(toDto(shortenedUrl)).location(constructLocationHeader(shortenedUrl)).build();
    }

    final String shortened = Objects.requireNonNullElseGet(shortenUrlDto.shortened(),
        () -> RandomStringUtils.secure().nextAlphanumeric(ShortenedUrl.SHORTENED_MAX_LENGTH));
    if (ShortenedUrl.count("shortened", shortened) > 0) {
      return Response.status(Response.Status.CONFLICT).build();
    }

    final ShortenedUrl persisted = persist(shortenUrlDto.url().toString(), shortened);
    return Response.created(constructLocationHeader(persisted)).entity(toDto(persisted)).build();
  }

  private static ShortenedUrl persist(final String url, final String shortened) {
    final ShortenedUrl persisted = new ShortenedUrl(url, shortened);
    persisted.persist();
    return persisted;
  }

  private static URI constructLocationHeader(final ShortenedUrl persisted) {
    return URI.create("%s/%s".formatted(PATH, persisted.shortened()));
  }

  private static ShortenUrlDto toDto(final ShortenedUrl shortenedUrl) throws MalformedURLException {
    return new ShortenUrlDto(URI.create(shortenedUrl.url()).toURL(), shortenedUrl.shortened());
  }

  @GET
  @Path("{shortened}")
  public Response getShortened(@PathParam("shortened") @Valid @NotNull final String shortened) {
    // @formatter:off
    return ShortenedUrl.findByShortened(shortened)
        .map(shortenedUrl -> Response
            .status(Response.Status.PERMANENT_REDIRECT)
            .location(URI.create(shortenedUrl.url())))
        .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
        .build();
    // @formatter:on
  }

  @DELETE
  @Path("{shortened}")
  @Transactional
  public Response deleteShortened(@PathParam("shortened") @Valid @NotNull final String shortened) {
    ShortenedUrl.delete("shortened", shortened);
    return Response.noContent().build();
  }
}
