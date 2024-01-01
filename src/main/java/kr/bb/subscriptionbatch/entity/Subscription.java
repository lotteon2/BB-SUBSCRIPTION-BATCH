package kr.bb.subscriptionbatch.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import kr.bb.subscriptionbatch.entity.common.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@Table(name = "subscription")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "subscription_id")
  private Long subscriptionId;

  @Builder.Default
  @OneToMany(mappedBy = "subscription", cascade = CascadeType.PERSIST, orphanRemoval = true)
  private List<SubscriptionRecords> subscriptionRecordsList = new ArrayList<>();

  @Column(name = "order_subscription_id", unique = true, nullable = false)
  private String orderSubscriptionId;

  @Column(name = "subscription_cid", nullable = false)
  private String subscriptionCid;

  @Column(name = "subscription_tid", unique = true, nullable = false)
  private String subscriptionTid;

  @Column(name = "subscription_sid", unique = true, nullable = false)
  private String subscriptionSid;

  @Column(name = "subscription_quantity", nullable = false)
  private Long subscriptionQuantity;

  @Column(name = "subscription_total_amount", nullable = false)
  private Long subscriptionTotalAmount;

  @Column(name = "payment_date", nullable = false)
  private LocalDateTime paymentDate;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  public void addSubscriptionTime() {
    this.endDate = this.endDate.plusDays(30);
  }
}
