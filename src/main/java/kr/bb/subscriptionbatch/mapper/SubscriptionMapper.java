package kr.bb.subscriptionbatch.mapper;

import bloomingblooms.domain.batch.SubscriptionBatchDto;
import kr.bb.subscriptionbatch.entity.Subscription;

public class SubscriptionMapper {
  public static SubscriptionBatchDto convertToDto(Subscription subscription) {
    return SubscriptionBatchDto.builder()
        .cid(subscription.getSubscriptionCid())
        .sid(subscription.getSubscriptionSid())
        .partnerOrderId(subscription.getOrderSubscriptionId())
        .partnerUserId(String.valueOf(subscription.getUserId()))
        .quantity(subscription.getSubscriptionQuantity())
        .totalAmount(subscription.getSubscriptionTotalAmount())
        .build();
  }
}
