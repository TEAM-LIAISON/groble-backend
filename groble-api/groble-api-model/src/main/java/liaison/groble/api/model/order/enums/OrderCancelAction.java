package liaison.groble.api.model.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderCancelAction {
  APPROVE("approve"),
  REJECT("reject");

  private final String value;

  OrderCancelAction(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static OrderCancelAction fromValue(String value) {
    for (OrderCancelAction action : OrderCancelAction.values()) {
      if (action.value.equalsIgnoreCase(value)) {
        return action;
      }
    }
    throw new IllegalArgumentException("Invalid action value: " + value);
  }
}
