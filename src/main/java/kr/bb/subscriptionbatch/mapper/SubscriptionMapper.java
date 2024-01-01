package kr.bb.subscriptionbatch.mapper;

import kr.bb.subscriptionbatch.dto.SubscriptionBatchDto;
import kr.bb.subscriptionbatch.entity.Subscription;

public class SubscriptionMapper {
  public static SubscriptionBatchDto convertToDto(Subscription subscription) {
    return SubscriptionBatchDto.builder()
        .cid(subscription.getSubscriptionCid())
        .sid(subscription.getSubscriptionSid())
        .partnerOrderId(subscription.getOrderSubscriptionId())
        .quantity(subscription.getSubscriptionQuantity())
        .totalAmount(subscription.getSubscriptionTotalAmount())
        .build();
  }
}
