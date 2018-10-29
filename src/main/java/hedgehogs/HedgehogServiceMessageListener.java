package hedgehogs;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hedgehogs.data.EvolutionPulse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that runs listener service on SQS, using long polling
 */
@Slf4j
class HedgehogServiceMessageListener extends Thread {

    private final HedghehogsConfiguration configuration;
    private final String queueUrl;
    private final HedgehogService hedgehogService;

    public HedgehogServiceMessageListener(final HedghehogsConfiguration configuration,
                                          final String queueUrl,
                                          final HedgehogService hedgehogService) {
        this.configuration = configuration;
        this.queueUrl = queueUrl;
        this.hedgehogService = hedgehogService;
    }

    @Override
    public void run() {
        try {
            final Gson gson = new Gson();
            final Set<String> ids = new HashSet<>();
            for (; ; ) {
                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
                receiveMessageRequest.setQueueUrl(queueUrl);
                receiveMessageRequest.setWaitTimeSeconds(20);
                ReceiveMessageResult receiveMessageResult = configuration.getAmazonSQS().receiveMessage(receiveMessageRequest);
                log.debug("received message for pulse:" + receiveMessageResult);

                final List<Message> messageList = receiveMessageResult.getMessages();

                for (int i = messageList.size() - 1; i >= 0; i--) {
                    Message message = messageList.get(i); // KEEPING ORDER
                    final String messageJson = message.getBody();
                    try {

                        JsonObject jp = (JsonObject)new JsonParser().parse(messageJson);
                        final String id = jp.get("MessageId").getAsString();
                        if (ids.contains(id)) {
                            log.warn("duplicate message {} ",id);
                        }
                        ids.add(id);
                        final String messageBody = jp.get("Message").getAsString();
                        final EvolutionPulse evolutionPulse = gson.fromJson(messageBody, EvolutionPulse.class);
                        if (evolutionPulse.getAge() == -1) {
                            hedgehogService.shutdown();
                            break;
                        }
                        else {
                            hedgehogService.handle(evolutionPulse);
                        }
                    }
                    catch (Exception ex) {
//                        log.error(ex.getMessage() + " while processing " + messageJson, ex);
                    }
                }
            }
        } catch (Exception ex) {
            hedgehogService.shutdown();
            log.error(ex.getMessage(), ex);
        }
    }
}
