package liaison.groble.domain.market.exception;

import liaison.groble.common.exception.DomainException;

public class InvalidMarketNameException extends DomainException {
  public InvalidMarketNameException(String message) {
    super(message);
  }
}
