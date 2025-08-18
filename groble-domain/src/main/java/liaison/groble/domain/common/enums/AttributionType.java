package liaison.groble.domain.common.enums;

public enum AttributionType {

  // '신규 유저는 어디서 많이 유입되는가?' 지표
  FIRST_TOUCH, // 첫 방문 시 유입 경로 유지

  // '어떤 채널이 최종 클릭을 유도하는가?' 지표
  LAST_TOUCH // 마지막 방문 시 유입 경로로 갱신
}
