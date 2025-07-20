package de.turing85.url.shortener.rider.replacers;

import java.util.Objects;

import com.github.database.rider.core.replacers.Replacer;
import de.turing85.url.shortener.entity.ShortenedUrl;
import org.dbunit.dataset.ReplacementDataSet;

public final class ShortenedLengthReplacer implements Replacer {
  @Override
  public void addReplacements(ReplacementDataSet replacementDataSet) {
    replacementDataSet.addReplacementSubstring("VAR_SHORTENED_LENGTH",
        Integer.toString(ShortenedUrl.SHORTENED_MAX_LENGTH));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o != null && getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass());
  }
}
