package liaison.groble.api.model.user.enums;

import liaison.groble.domain.user.enums.UserType;

import lombok.Getter;

@Getter
public enum UserTypeDto {
  BUYER("구매자"),
  SELLER("판매자");

  private final String description;

  UserTypeDto(String description) {
    this.description = description;
  }

  public UserType toDomainType() {
    return switch (this) {
      case SELLER -> UserType.SELLER;
      case BUYER -> UserType.BUYER;
    };
  }
}
