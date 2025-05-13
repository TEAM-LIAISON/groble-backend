package liaison.groble.domain.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import liaison.groble.domain.user.enums.WithdrawalReason;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_withdrawal_history")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserWithdrawalHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  private String email;

  @Enumerated(EnumType.STRING)
  private WithdrawalReason reason;

  private String additionalComment;

  private LocalDateTime withdrawalDate;

  @Builder
  public UserWithdrawalHistory(
      Long userId,
      String email,
      WithdrawalReason reason,
      String additionalComment,
      LocalDateTime withdrawalDate) {
    this.userId = userId;
    this.email = email;
    this.reason = reason;
    this.additionalComment = additionalComment;
    this.withdrawalDate = withdrawalDate;
  }
}
