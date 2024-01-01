package kr.bb.subscriptionbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import kr.bb.subscriptionbatch.entity.Subscription;
import kr.bb.subscriptionbatch.repository.SubscriptionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBatchTest
@SpringBootTest
public class SubscriptionBatchTest {
  @Autowired private JobLauncherTestUtils jobLauncherTestUtils;
  @Autowired private SubscriptionRepository subscriptionRepository;

  @AfterEach
  public void deleteData() {
    subscriptionRepository.deleteAll();
  }

  @Test
  public void batchSubscriptionTest() throws Exception {
    LocalDateTime startDate =
        LocalDateTime.parse(
            "2023-12-01 19:12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    LocalDateTime paymentDate =
        LocalDateTime.parse(
            "2023-12-31 19:12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    Subscription subscription =
        Subscription.builder()
            .subscriptionCid("TCSUBSCRIP")
            .subscriptionTid("임시tid")
            .subscriptionSid("임시sid")
            .subscriptionQuantity(1L)
            .subscriptionTotalAmount(45000L)
            .paymentDate(paymentDate)
            .startDate(startDate)
            .build();

    subscriptionRepository.save(subscription);

    JobParameters jobParameters = new JobParametersBuilder().addString("date", "20231231").toJobParameters();

    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    for(Subscription target : subscriptionRepository.findAll()){
      assertEquals(target.getPaymentDate(), paymentDate.plusDays(30));
    }

  }
}
