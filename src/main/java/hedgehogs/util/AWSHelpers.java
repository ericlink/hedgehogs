package hedgehogs.util;

import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import hedgehogs.HedghehogsConfiguration;

public class AWSHelpers {
    public static String findTopic(final HedghehogsConfiguration configuration, final String evolutionPulseTopicPattern) throws IllegalArgumentException {
        String evolutionPulseTopicArn = null;

        final ListTopicsResult listTopicsResult = configuration.getAmazonSNS().listTopics();
        for (Topic topic : listTopicsResult.getTopics()) {
            if (topic.getTopicArn().contains(evolutionPulseTopicPattern)) {
                evolutionPulseTopicArn = topic.getTopicArn();
            }
        }
        if (evolutionPulseTopicArn == null) {
            throw new IllegalArgumentException("unable to find topic " + evolutionPulseTopicPattern);
        }
        return evolutionPulseTopicArn;
    }
    public static String findQueue(final HedghehogsConfiguration configuration, final String queuePattern) throws IllegalArgumentException {
//        final ListQueuesResult listQueuesResult = configuration.getAmazonSQS().listQueues();
//        String queue = null;
//        for (String queueUrl: listQueuesResult.getQueueUrls()) {
//            if (queueUrl.contains(queuePattern)) {
//                queue = queueUrl;
//                break;
//            }
//        }
//
//        if (queue == null) {
//            throw new IllegalArgumentException("cant find queue " + queuePattern + " in list");
//        }
        return configuration.getAmazonSQS().getQueueUrl(queuePattern).getQueueUrl();
    }
}
