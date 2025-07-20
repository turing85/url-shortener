package de.turing85.url.shortener;

import java.net.URL;

import jakarta.validation.constraints.NotNull;

import de.turing85.url.shortener.entity.ShortenedUrl;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.Nullable;

// @formatter:off
public record ShortenUrlDto(
    @NotNull URL url,
    @Nullable @Length(min = 1, max = ShortenedUrl.SHORTENED_MAX_LENGTH) String shortened){}
// @formatter:on