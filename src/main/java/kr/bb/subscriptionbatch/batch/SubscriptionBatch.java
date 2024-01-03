package kr.bb.subscriptionbatch.batch;

import bloomingblooms.domain.batch.SubscriptionBatchDto;
import bloomingblooms.domain.batch.SubscriptionBatchDtoList;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManagerFactory;
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
  private final KafkaTemplate<String, SubscriptionBatchDtoList> kafkaTemplate;
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
    return new JpaPagingItemReaderBuilder<Subscription>()
        .name("subscriptionReader")
        .entityManagerFactory(emf)
        .pageSize(chunkSize)
        .queryString("SELECT s FROM Subscription s WHERE s.isDeleted is null")
        .build();
  }

  @Bean
  @StepScope
  public JpaItemWriter<Subscription> subscriptionWriter(
      @Value("#{jobParameters[date]}") String date) {
    System.out.println("date = " + date);
    List<SubscriptionBatchDto> subscriptionBatchDtos = new ArrayList<>();

    JpaItemWriter<Subscription> jpaItemWriter =
        new JpaItemWriter<>() {

          @Override
          public void write(List<? extends Subscription> items) {
            List<Subscription> subscriptionList =
                subscriptionRepository.findSubscriptionsByPaymentDate(date);
            for (Subscription subscription : subscriptionList) {
              subscription.addSubscriptionTime();
              subscriptionBatchDtos.add(SubscriptionMapper.convertToDto(subscription));
            }

            SubscriptionBatchDtoList subscriptionBatchDtoList = SubscriptionBatchDtoList.builder()
                    .subscriptionBatchDtoList(subscriptionBatchDtos)
                    .build();

            System.out.println("subscriptionList = " + subscriptionList);
            if (!subscriptionList.isEmpty()) {
              kafkaTemplate.send("subscription-batch", subscriptionBatchDtoList);

              // TODO: SNS로 정기결제 발생 알려주기

            }
          }
        };

    jpaItemWriter.setEntityManagerFactory(emf);
    return jpaItemWriter;
  }
}
