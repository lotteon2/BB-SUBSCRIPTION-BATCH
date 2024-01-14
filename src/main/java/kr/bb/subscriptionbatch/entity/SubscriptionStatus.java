package kr.bb.subscriptionbatch.entity;

import lombok.Getter;

@Getter
public enum SubscriptionStatus {

  COMPLETED("구독 완료"),
  CANCELED("구독 취소");

  private final String message;

  SubscriptionStatus(String message) {
    this.message = message;
  }
}
