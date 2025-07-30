package liaison.groble.application.content.exception;

import liaison.groble.common.exception.GrobleException;

public class ContentEditException extends GrobleException {
  public ContentEditException(String message) {
    super("판매 중인 콘텐츠는 수정할 수 없습니다. 먼저 판매를 중단해주세요.", 400);
  }
}
