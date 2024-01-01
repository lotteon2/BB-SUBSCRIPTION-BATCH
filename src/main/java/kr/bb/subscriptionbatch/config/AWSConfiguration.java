package kr.bb.subscriptionbatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

public class AWSConfiguration {
  @Value("${cloud.aws.credentials.ACCESS_KEY_ID}")
  private String accessKeyId;

  @Value("${cloud.aws.credentials.SECRET_ACCESS_KEY}")
  private String secretAccessKey;


  public AwsCredentialsProvider getAwsCredentials() {
    AwsBasicCredentials awsBasicCredentials =
        AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    return () -> awsBasicCredentials;
  }

  @Primary
  @Bean
  public SnsClient snsClient() {
    return SnsClient.builder()
        .credentialsProvider(getAwsCredentials())
        .region(Region.AP_NORTHEAST_1)
        .build();
  }
}
