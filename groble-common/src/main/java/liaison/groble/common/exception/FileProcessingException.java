package liaison.groble.common.exception;

/** 파일 처리 과정에서 발생하는 예외를 표현하는 클래스 */
public class FileProcessingException extends RuntimeException {

  /**
   * 메시지를 포함한 예외 생성
   *
   * @param message 예외 메시지
   */
  public FileProcessingException(String message) {
    super(message);
  }

  /**
   * 메시지와 원인 예외를 포함한 예외 생성
   *
   * @param message 예외 메시지
   * @param cause 원인 예외
   */
  public FileProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
