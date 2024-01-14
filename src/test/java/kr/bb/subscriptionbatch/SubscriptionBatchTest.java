package kr.bb.subscriptionbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import bloomingblooms.domain.batch.SubscriptionBatchDtoList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.bb.subscriptionbatch.dto.OrderSubscriptionBatchDto;
import kr.bb.subscriptionbatch.entity.OrderSubscription;
import kr.bb.subscriptionbatch.repository.OrderSubscriptionRepository;
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
  @Autowired private OrderSubscriptionRepository orderSubscriptionRepository;
  @MockBean private KafkaTemplate<String, SubscriptionBatchDtoList> kafkaTemplate;

  @AfterEach
  public void deleteData() {
    orderSubscriptionRepository.deleteAll();
  }

  @Test
  public void batchSubscriptionTest() throws Exception {
    LocalDateTime startDate =
        LocalDateTime.parse(
            "2023-12-01 19:12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    LocalDateTime paymentDate =
        LocalDateTime.parse(
            "2023-12-31 19:12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    OrderSubscription orderSubscription =
        OrderSubscription.builder()
            .orderSubscriptionId("qwer")
            .userId(1L)
            .subscriptionProductId("asdf")
            .deliveryId(1L)
            .productName("상품명")
            .productPrice(45000L)
            .deliveryDay(startDate.plusDays(3).toLocalDate())
            .phoneNumber("010-1111-1111")
            .paymentDate(paymentDate)
            .build();

    orderSubscriptionRepository.save(orderSubscription);

    JobParameters jobParameters =
        new JobParametersBuilder().addString("date", "20231231").toJobParameters();

    List<String> orderSubscriptionIds = List.of(orderSubscription.getOrderSubscriptionId());
    OrderSubscriptionBatchDto subscriptionBatchDto = OrderSubscriptionBatchDto.builder()
            .orderSubscriptionIds(orderSubscriptionIds)
            .build();

    when(kafkaTemplate.send(eq("subscription-batch"), any(SubscriptionBatchDtoList.class))).thenReturn(null);

    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    for (OrderSubscription target : orderSubscriptionRepository.findAll()) {
      assertEquals(target.getPaymentDate(), paymentDate.plusDays(30));
    }
  }
}
