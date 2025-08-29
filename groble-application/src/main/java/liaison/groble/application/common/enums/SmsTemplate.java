package liaison.groble.application.common.enums;

public enum SmsTemplate {
  VERIFICATION_CODE("[Groble] 인증코드 [%s]를 입력해주세요.");

  private final String template;

  SmsTemplate(String template) {
    this.template = template;
  }

  public String format(Object... args) {
    return String.format(template, args);
  }
}
