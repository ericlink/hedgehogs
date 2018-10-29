package hedgehogs;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import lombok.Data;
import lombok.Getter;


/**
 * Starting mock API Gateway (http port 4567)...
 * Starting mock DynamoDB (http port 4569)...
 * Starting mock SES (http port 4579)...
 * Starting mock Kinesis (http port 4568)...
 * Starting mock Redshift (http port 4577)...
 * Starting mock S3 (http port 4572)...
 * Starting mock CloudWatch (http port 4582)...
 * Starting mock CloudFormation (http port 4581)...
 * Starting mock SSM (http port 4583)...
 * Starting mock SQS (http port 4576)...
 * Starting mock Secrets Manager (http port 4584)...
 * Starting local Elasticsearch (http port 4571)...
 * Starting mock SNS (http port 4575)...
 * Starting mock DynamoDB Streams service (http port 4570)...
 * Starting mock Firehose service (http port 4573)...
 * Starting mock Route53 (http port 4580)...
 */

@Getter
public class HedghehogsConfiguration {
    private static final String REGION = "us-east-1";

    private AmazonS3 amazonS3 = AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4572/", REGION))
            .build();
    private AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4569/", REGION))
            .build();
    private AmazonSNS amazonSNS = AmazonSNSClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4575/", REGION))
            .build();
    private AmazonSQS amazonSQS = AmazonSQSClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4576/", REGION))
            .build();

    static HedghehogsConfiguration instance = new HedghehogsConfiguration();

    private HedghehogsConfiguration() {
    }
    
    public static HedghehogsConfiguration instance() {
        return instance;
    }
}