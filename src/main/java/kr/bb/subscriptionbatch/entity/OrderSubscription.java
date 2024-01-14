package kr.bb.subscriptionbatch.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import kr.bb.subscriptionbatch.entity.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@Table(name = "order_subscription")
@AllArgsConstructor
@NoArgsConstructor
public class OrderSubscription extends BaseEntity {
  @Id private String orderSubscriptionId;
  @NotNull private Long userId;
  @NotNull private String subscriptionProductId;

  @NotNull
  @Enumerated(EnumType.STRING)
  private SubscriptionStatus subscriptionStatus;

  @NotNull private Long deliveryId;
  @NotNull private String productName;
  @NotNull private Long productPrice;
  @NotNull private LocalDate deliveryDay;
  @NotNull private String phoneNumber;
  @NotNull private LocalDateTime paymentDate;
  private LocalDateTime endDate;

  public void addTime() {
    this.paymentDate = this.paymentDate.plusDays(30);
    this.deliveryDay = this.paymentDate.toLocalDate().plusDays(3);
  }
}
