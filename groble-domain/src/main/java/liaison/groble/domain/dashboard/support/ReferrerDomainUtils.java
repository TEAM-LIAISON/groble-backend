package liaison.groble.domain.dashboard.support;

import java.util.Set;

import org.springframework.util.StringUtils;

public final class ReferrerDomainUtils {

  private static final Set<String> INTERNAL_DOMAIN_KEYWORDS =
      Set.of("groble.im", "groble-dev", "groble.kr", "localhost", "127.0.0.1");

  private ReferrerDomainUtils() {}

  public static boolean isInternalDomain(String domain) {
    if (!StringUtils.hasText(domain)) {
      return false;
    }
    String lower = domain.toLowerCase();
    return INTERNAL_DOMAIN_KEYWORDS.stream().anyMatch(lower::contains);
  }
}
