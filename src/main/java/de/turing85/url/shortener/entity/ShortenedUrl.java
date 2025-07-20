package de.turing85.url.shortener.entity;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.Nullable;

@Entity
@Table(schema = "public", name = "shortened_url",
    uniqueConstraints = {
        @UniqueConstraint(name = "shortened_url__unique__url", columnNames = "url"),
        @UniqueConstraint(name = "shortened_url__unique__shortened", columnNames = "shortened")})
@Setter(AccessLevel.PROTECTED)
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ShortenedUrl extends PanacheEntityBase {
  public static final int SHORTENED_MAX_LENGTH = 8;

  @Id
  @SequenceGenerator(name = "UploadIdGenerator", sequenceName = "shortened_url__seq__id",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "UploadIdGenerator")
  @Column(name = "id")
  @Getter
  @Nullable
  private Long id;

  @Column(name = "url", nullable = false)
  @Nullable
  private String url;

  @Column(name = "shortened", nullable = false)
  @Length(min = 1, max = SHORTENED_MAX_LENGTH)
  @Nullable
  private String shortened;

  public ShortenedUrl(final String url, final String shortened) {
    this(null, url, shortened);
  }

  public String url() {
    return Objects.requireNonNull(url);
  }

  public String shortened() {
    return Objects.requireNonNull(shortened);
  }

  public static Optional<ShortenedUrl> findByShortened(final String shortened) {
    return find("shortened", shortened).singleResultOptional();
  }

  public static Optional<ShortenedUrl> findByUrl(final URL url) {
    return findByUrl(url.toString());
  }

  public static Optional<ShortenedUrl> findByUrl(final String url) {
    return find("url", url).singleResultOptional();
  }
}
