package kr.bb.subscriptionbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import bloomingblooms.domain.batch.SubscriptionBatchDto;
import bloomingblooms.domain.batch.SubscriptionBatchDtoList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.bb.subscriptionbatch.entity.Subscription;
import kr.bb.subscriptionbatch.mapper.SubscriptionMapper;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBatchTest
@SpringBootTest
public class SubscriptionBatchTest {
  @Autowired private JobLauncherTestUtils jobLauncherTestUtils;
  @Autowired private SubscriptionRepository subscriptionRepository;
  @MockBean private KafkaTemplate<String, SubscriptionBatchDtoList> kafkaTemplate;

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
            .orderSubscriptionId("임시구독주문id")
            .subscriptionCid("TCSUBSCRIP")
            .subscriptionTid("임시tid")
            .subscriptionSid("임시sid")
            .subscriptionQuantity(1L)
            .subscriptionTotalAmount(45000L)
            .paymentDate(paymentDate)
            .startDate(startDate)
            .userId(1L)
            .phoneNumber("010-1111-1111")
            .build();

    subscriptionRepository.save(subscription);

    JobParameters jobParameters =
        new JobParametersBuilder().addString("date", "20231231").toJobParameters();

    SubscriptionBatchDto subscriptionBatchDto = SubscriptionMapper.convertToDto(subscription);

    SubscriptionBatchDtoList subscriptionBatchDtoList =
        SubscriptionBatchDtoList.builder()
            .subscriptionBatchDtoList(List.of(subscriptionBatchDto))
            .build();
    when(kafkaTemplate.send(eq("subscription-batch"), any(SubscriptionBatchDtoList.class))).thenReturn(null);

    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    for (Subscription target : subscriptionRepository.findAll()) {
      assertEquals(target.getPaymentDate(), paymentDate.plusDays(30));
    }
  }
}
