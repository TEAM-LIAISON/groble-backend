package liaison.groblecore.domain;

/** 사용자 상태를 나타내는 열거형 실무에서는 보다 세분화된 사용자 상태 관리가 필요합니다. */
public enum UserStatus {
  /** 활성 상태 - 정상적으로 서비스 이용 가능 */
  ACTIVE,

  /** 비활성 상태 - 사용자가 계정을 비활성화함 */
  INACTIVE,

  /** 휴면 상태 - 장기간 로그인하지 않은 사용자 (예: 1년 이상 미접속) */
  DORMANT,

  /** 잠금 상태 - 비정상적인 활동이나 정책 위반으로 관리자가 계정을 잠금 */
  LOCKED,

  /** 정지 상태 - 임시적으로 서비스 이용이 제한된 상태 (일정 기간 후 자동 해제) */
  SUSPENDED,

  /** 탈퇴 대기 상태 - 탈퇴 요청 후 유예 기간 (보통 7-30일) 중인 상태 */
  PENDING_WITHDRAWAL,

  /** 탈퇴 완료 상태 - 계정 정보는 보관되지만 비식별화 처리됨 (개인정보보호법 준수) */
  WITHDRAWN,

  /** 이메일 인증 대기 상태 - 회원가입 완료 후 이메일 인증 대기 중 */
  PENDING_VERIFICATION;

  /**
   * 해당 상태의 사용자가 서비스를 이용할 수 있는지 여부 확인
   *
   * @return 서비스 이용 가능 여부
   */
  public boolean isAccessible() {
    return this == ACTIVE;
  }

  /**
   * 해당 상태의 사용자가 로그인할 수 있는지 여부 확인
   *
   * @return 로그인 가능 여부
   */
  public boolean isLoginable() {
    return this == ACTIVE || this == DORMANT;
  }

  /**
   * 사용자에게 표시할 상태 메시지
   *
   * @return 상태 메시지
   */
  public String getStatusMessage() {
    return switch (this) {
      case ACTIVE -> "정상 이용 가능한 상태입니다.";
      case INACTIVE -> "비활성화된 계정입니다. 계정을 활성화하려면 고객센터에 문의해주세요.";
      case DORMANT -> "휴면 상태의 계정입니다. 로그인하시면 자동으로 활성화됩니다.";
      case LOCKED -> "계정이 잠금 처리되었습니다. 관리자에게 문의해주세요.";
      case SUSPENDED -> "계정이 일시 정지되었습니다. 정지 기간 이후 자동으로 활성화됩니다.";
      case PENDING_WITHDRAWAL -> "탈퇴 대기 중인 계정입니다. 취소하시려면 고객센터에 문의해주세요.";
      case WITHDRAWN -> "탈퇴 처리된 계정입니다.";
      case PENDING_VERIFICATION -> "이메일 인증이 필요합니다. 인증 메일을 확인해주세요.";
    };
  }
}
