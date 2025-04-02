package liaison.groblecore.exception;

/** 이메일 중복 예외 회원가입 또는 이메일 변경 시 이미 존재하는 이메일인 경우 발생 */
public class EmailAlreadyExistsException extends RuntimeException {

  public EmailAlreadyExistsException() {
    super("이미 사용 중인 이메일입니다.");
  }

  public EmailAlreadyExistsException(String email) {
    super("이미 사용 중인 이메일입니다: " + email);
  }
}
