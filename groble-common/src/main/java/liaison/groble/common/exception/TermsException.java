package liaison.groble.common.exception;

public class TermsException extends RuntimeException {
  public TermsException(String message) {
    super(message);
  }

  public TermsException(String message, Throwable cause) {
    super(message, cause);
  }

  public static class TermsNotFoundException extends TermsException {
    public TermsNotFoundException(String message) {
      super(message);
    }
  }

  public static class RequiredTermsNotAgreedException extends TermsException {
    public RequiredTermsNotAgreedException(String message) {
      super(message);
    }
  }
}
