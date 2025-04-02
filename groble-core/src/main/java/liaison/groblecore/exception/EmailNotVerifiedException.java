package liaison.groblecore.exception;

/** 이메일 인증이 완료되지 않은 경우 발생하는 예외 미인증 사용자가 로그인 또는 특정 기능을 사용하려고 할 때 발생 */
public class EmailNotVerifiedException extends RuntimeException {

  public EmailNotVerifiedException() {
    super("이메일 인증이 필요합니다.");
  }

  public EmailNotVerifiedException(String email) {
    super("이메일 인증이 필요합니다: " + email);
  }
}
