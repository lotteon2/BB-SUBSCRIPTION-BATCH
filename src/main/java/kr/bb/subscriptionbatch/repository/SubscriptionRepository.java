package kr.bb.subscriptionbatch.repository;

import java.util.List;
import kr.bb.subscriptionbatch.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query("SELECT s FROM Subscription s WHERE REPLACE(SUBSTRING(s.paymentDate, 1, 10), '-', '') = :date")
    List<Subscription> findSubscriptionsByPaymentDate(String date);
}

