package liaison.groblecore.domain;

/** 사용자 인증 제공자 유형을 나타내는 열거형 */
public enum ProviderType {
  LOCAL, // 일반 이메일/비밀번호 회원가입
  GOOGLE,
  KAKAO,
  NAVER
}
