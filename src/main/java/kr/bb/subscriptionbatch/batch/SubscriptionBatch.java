package kr.bb.subscriptionbatch.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import kr.bb.subscriptionbatch.dto.SubscriptionBatchDto;
import kr.bb.subscriptionbatch.entity.Subscription;
import kr.bb.subscriptionbatch.mapper.SubscriptionMapper;
import kr.bb.subscriptionbatch.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class SubscriptionBatch {
  private final SubscriptionRepository subscriptionRepository;
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final EntityManagerFactory emf;
  private final KafkaTemplate<String, List<SubscriptionBatchDto>> kafkaTemplate;
//  private final KafkaTemplate<String, UserInfoForNotification>
//      memberInfoForNotificationDtoKafkaProcessor;
  private int chunkSize = 500;

  @Bean
  public Job subscriptionJob() {
    return jobBuilderFactory.get("subscriptionJob").start(subscriptionStep()).build();
  }

  @Bean
  public Step subscriptionStep() {
    return stepBuilderFactory
        .get("susbcriptionStep")
        .<Subscription, Subscription>chunk(chunkSize)
        .reader(subscriptionReader())
        .writer(subscriptionWriter(null))
        .build();
  }

  @Bean
  public JpaPagingItemReader<Subscription> subscriptionReader() {
    Map<String, Object> params = new HashMap<>();
    params.put("isDeleted", null);

    return new JpaPagingItemReaderBuilder<Subscription>()
        .name("subscriptionReader")
        .entityManagerFactory(emf)
        .pageSize(chunkSize)
        .queryString("SELECT s FROM Subscription s WHERE s.isDeleted = :isDeleted")
        .parameterValues(params)
        .build();
  }

  @Bean
  @StepScope
  public JpaItemWriter<Subscription> subscriptionWriter(
      @Value("#{jobParameters[date]}") String date) {
    List<SubscriptionBatchDto> subscriptionBatchDtoList = new ArrayList<>();

    JpaItemWriter<Subscription> jpaItemWriter =
        new JpaItemWriter<>() {

          @Override
          public void write(List<? extends Subscription> items) {
            List<Subscription> subscriptionList =
                subscriptionRepository.findSubscriptionsByPaymentDate(date);
            for (Subscription subscription : subscriptionList) {
              subscription.addSubscriptionTime();
              subscriptionBatchDtoList.add(SubscriptionMapper.convertToDto(subscription));
            }

            if (!subscriptionList.isEmpty()) {
              kafkaTemplate.send("subscription-batch", subscriptionBatchDtoList);

              // TODO: SNS 로 배치 정기결제 알려주기



            }
          }
        };

    jpaItemWriter.setEntityManagerFactory(emf);
    return jpaItemWriter;
  }
}
