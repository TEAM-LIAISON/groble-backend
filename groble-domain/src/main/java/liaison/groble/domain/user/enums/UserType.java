package liaison.groble.domain.user.enums;

import lombok.Getter;

@Getter
public enum UserType {
  BUYER("구매자"),
  SELLER("판매자");

  private final String description;

  UserType(String description) {
    this.description = description;
  }
}
