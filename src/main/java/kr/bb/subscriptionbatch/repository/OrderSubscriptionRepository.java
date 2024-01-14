package kr.bb.subscriptionbatch.repository;

import java.time.LocalDateTime;
import java.util.List;
import kr.bb.subscriptionbatch.entity.OrderSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderSubscriptionRepository extends JpaRepository<OrderSubscription, String> {
    @Query("SELECT os FROM OrderSubscription os WHERE REPLACE(SUBSTRING(os.paymentDate, 1, 10), '-', '') = :paymentDate AND os.subscriptionStatus = 'COMPLETED'")
    List<OrderSubscription> findAllByPaymentDate(String paymentDate);
}
