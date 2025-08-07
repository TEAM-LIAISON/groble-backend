package liaison.groble.domain.user.exception;

import liaison.groble.common.exception.DomainException;

public class InvalidContactException extends DomainException {
  public InvalidContactException(String message) {
    super(message);
  }
}
