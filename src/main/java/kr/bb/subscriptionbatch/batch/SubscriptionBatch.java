package kr.bb.subscriptionbatch.batch;

import bloomingblooms.domain.batch.SubscriptionBatchDto;
import bloomingblooms.domain.batch.SubscriptionBatchDtoList;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import kr.bb.subscriptionbatch.entity.OrderSubscription;
import kr.bb.subscriptionbatch.repository.OrderSubscriptionRepository;
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
  private final OrderSubscriptionRepository orderSubscriptionRepository;
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final EntityManagerFactory emf;
  private final KafkaTemplate<String, SubscriptionBatchDtoList> kafkaTemplate;
  private int chunkSize = 500;

  @Bean
  public Job subscriptionJob() {
    return jobBuilderFactory.get("subscriptionJob").start(subscriptionStep()).build();
  }

  @Bean
  public Step subscriptionStep() {
    return stepBuilderFactory
        .get("susbcriptionStep")
        .<OrderSubscription, OrderSubscription>chunk(chunkSize)
        .reader(subscriptionReader())
        .writer(subscriptionWriter(null))
        .build();
  }

  @Bean
  public JpaPagingItemReader<OrderSubscription> subscriptionReader() {
    return new JpaPagingItemReaderBuilder<OrderSubscription>()
        .name("subscriptionReader")
        .entityManagerFactory(emf)
        .pageSize(chunkSize)
        .queryString("SELECT os FROM OrderSubscription os WHERE os.subscriptionStatus = 'COMPLETED'")
        .build();
  }

  @Bean
  @StepScope
  public JpaItemWriter<OrderSubscription> subscriptionWriter(
      @Value("#{jobParameters[date]}") String date) {
    System.out.println("date is  = " + date);
    List<SubscriptionBatchDto> subscriptionBatchDtos = new ArrayList<>();

    JpaItemWriter<OrderSubscription> jpaItemWriter =
        new JpaItemWriter<OrderSubscription>() {

          @Override
          public void write(List<? extends OrderSubscription> items) {
            List<OrderSubscription> orderSubscriptionList =
                orderSubscriptionRepository.findAllByPaymentDate(date);

            List<String> orderSubscriptionIds = new ArrayList<>();

            for (OrderSubscription orderSubscription : orderSubscriptionList) {
              orderSubscription.addTime();
              orderSubscriptionIds.add(orderSubscription.getOrderSubscriptionId());
              SubscriptionBatchDto subscriptionBatchDto = SubscriptionBatchDto.builder()
                      .userId(orderSubscription.getUserId())
                      .orderSubscriptionId(orderSubscription.getOrderSubscriptionId())
                      .build();
              subscriptionBatchDtos.add(subscriptionBatchDto);
            }

            SubscriptionBatchDtoList subscriptionBatchDtoList =
                    SubscriptionBatchDtoList.builder()
                    .subscriptionBatchDtoList(subscriptionBatchDtos)
                    .build();

            if (!orderSubscriptionIds.isEmpty()) {
              kafkaTemplate.send("subscription-batch", subscriptionBatchDtoList);
            }
          }
        };

    jpaItemWriter.setEntityManagerFactory(emf);
    return jpaItemWriter;
  }
}
