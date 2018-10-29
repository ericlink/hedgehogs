package hedgehogs;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sqs.model.ListQueuesRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.gson.Gson;
import hedgehogs.data.EvolutionPulse;
import hedgehogs.data.Hedgehog;
import hedgehogs.util.AWSHelpers;
import lombok.extern.slf4j.Slf4j;
import hedgehogs.util.RandomStringGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import static java.lang.Thread.sleep;


@Slf4j
public class HedgehogServiceImpl implements HedgehogService {

    private static final int CYCLES = 20;
    private static final int NEWBORN_RATE = 4;
    private static final int ADULT_AGE = 12; // age at which babies can be made
    private static final int MAX_AGE = 96; // max age
    private static final float MAX_WEIGHT = 13; // max weight
    private static final float INCREASE_IN_TORQUE = 0.4f; // how much torque increase per week as hedgehog develops
    private static final float INCREASE_IN_WEIGHT = 0.8f; // how much weight might increase per week

    private HedghehogsConfiguration configuration;
    private Boolean                 keepRunning;

    public void init(HedghehogsConfiguration configuration) {
        this.configuration = configuration;
        this.keepRunning = true;
    }

    /**
     * publishes new hedgehog with given name as a newborn
     *
     * @param name
     */
    public void createNewbornHedgehog(final String name) {
        DynamoDBMapper mapper = new DynamoDBMapper(configuration.getAmazonDynamoDB());
        Hedgehog hedgehog = Hedgehog.builder().id(UUID.randomUUID().toString()).name(name).weightInApples((float) (0.5f * Math.random())).build();
        log.info("{} was born", hedgehog.getName());
        mapper.save(hedgehog);
    }


    /**
     * lists all current hedgehogs
     */
    public void listHedgehogs() {
        DynamoDBMapper mapper = new DynamoDBMapper(configuration.getAmazonDynamoDB());
        List<Hedgehog> list = mapper.scan(Hedgehog.class, new DynamoDBScanExpression());

        if (list.isEmpty()) {
            log.info("no hedgehogs found in forest");
        } else {
            for (Hedgehog hedgehog : list) {
                log.info("{} age={} weeks, weight={} apples", hedgehog.getName(), hedgehog.getAgeInWeeks(), hedgehog.getAgeInWeeks());
            }
        }
    }

    /**
     * updates given hedgehog
     *
     * @param period   in week
     * @param hedgehog
     */
    private void updateHedgehogStats(final int period, Hedgehog hedgehog) {
        hedgehog.setAgeInWeeks(hedgehog.getAgeInWeeks() + period);
        hedgehog.setTorqueInApplePerInch(hedgehog.getTorqueInApplePerInch() + period * INCREASE_IN_TORQUE);
        hedgehog.setWeightInApples(hedgehog.getWeightInApples() + calculateNewWeight(period));
    }

    /**
     * randomized weight gain
     *
     * @param period
     * @return
     */
    private float calculateNewWeight(final int period) {
        float v = 0;
        for (int i = 0; i < period; i++) {
            v += (INCREASE_IN_WEIGHT - Math.random() * INCREASE_IN_WEIGHT);
        }
        return v;
    }

    /**
     * runs death cycle on hedgehogs
     *
     * @param cutOffAge
     */
    public void deathCycle(final int cutOffAge) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withN(String.valueOf(cutOffAge)));
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("ageInWeeks > :val1").withExpressionAttributeValues(eav);

        DynamoDBMapper mapper = new DynamoDBMapper(configuration.getAmazonDynamoDB());

        List<Hedgehog> list = mapper.scan(Hedgehog.class, scanExpression);

        if (list.isEmpty()) {
            log.info("no hedgehogs found in forest, nothing to kill");
        } else {
            log.info("{} hedgehogs will die", list.size());
            for (Hedgehog hedgehog : list) {
                log.info(hedgehog.getName() + " died  at the tender age of " + hedgehog.getAgeInWeeks() + " weeks");
                mapper.delete(hedgehog);
            }
        }

    }

    public void start() {
        log.info("setting up");

        String topic = "HedgehogsLifecycleEventsTopic";
        String queue = "HedgehogsLifecycleEventsQueue.fifo";
        HedghehogsConfiguration configuration = HedghehogsConfiguration.instance();
        int cycles = CYCLES;


        init(configuration);


        Gson gson = new Gson();

        try {
            String evolutionPulseTopicArn = AWSHelpers.findTopic(configuration, topic);
            String evolutionPulseQueue = AWSHelpers.findQueue(configuration, queue);

            HedgehogServiceMessageListener listener = new HedgehogServiceMessageListener(configuration, evolutionPulseQueue, this);
            listener.start();


            log.info("Forest comes to life for " + cycles + " cycles");

            while (cycles-- > 0) {
                final EvolutionPulse evolutionPulse = EvolutionPulse.builder()
                        .age(CYCLES - cycles)
                        .astrology(RandomStringGenerator.RandomStringGeneratorFactory.getRandomStringGenerator("names").getValue())
                        .build();
                configuration.getAmazonSNS().publish(evolutionPulseTopicArn, gson.toJson(evolutionPulse));
                log.debug("published message for pulse:" + evolutionPulse);
            }


            final EvolutionPulse evolutionPulse = EvolutionPulse.builder()
                    .age(-1) // close
                    .astrology("DEATH")
                    .build();
            configuration.getAmazonSNS().publish(evolutionPulseTopicArn, gson.toJson(evolutionPulse));

            while (keepRunning) {
//                log.info("keepRunning {}", keepRunning);
                sleep(100);
            }

            System.out.println("DONE");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void shutdown() {
        keepRunning = false;
        log.info("final cleanup");
        deathCycle(0);
        System.exit(1);
    }

    @Override
    public void handle(EvolutionPulse evolutionPulse) {
        final DynamoDBMapper mapper = new DynamoDBMapper(configuration.getAmazonDynamoDB());
        final List<Hedgehog> list = mapper.scan(Hedgehog.class, new DynamoDBScanExpression());

        log.info("NEXT EVOLUTION CYCLE:" + evolutionPulse.getAge() + ", week of " + evolutionPulse.getAstrology());

        if (list.isEmpty()) {
            log.info("no hedgehogs found in forest");
            // need to make new ones
            for (int i = 0; i < NEWBORN_RATE; i++) {
                createNewbornHedgehog(RandomStringGenerator.RandomStringGeneratorFactory.getRandomStringGenerator("names").getValue());
            }
            log.info(NEWBORN_RATE + " newborn hedgehogs were brought from zoo");
        } else {
            int count = 0;
            for (Hedgehog hedgehog : list) {
                updateHedgehogStats(2, hedgehog);
                if (hedgehog.getWeightInApples() > MAX_WEIGHT) {
                    log.info("{} died from overeating, gaining weight of {} apples", hedgehog.getName(), hedgehog.getWeightInApples());
                    mapper.delete(hedgehog);
                } else if (hedgehog.getAgeInWeeks() > MAX_AGE) {
                    log.info("{} died of old age", hedgehog.getName());
                    mapper.delete(hedgehog);
                } else {
                    if (hedgehog.getAgeInWeeks() >= ADULT_AGE) {
                        count++;
                    }
                    if (hedgehog.getName().equals(evolutionPulse.getAstrology())) {
                        log.info("{} having time of its life because of lucky name ", hedgehog);
                        // when its a week of one of hedgehogs - they got to have extra kids
                        count+=2;
                    }
                    mapper.save(hedgehog);
                }
            }
            // every two adult hedgehogs can give birth to babies, we ignore genders for MVP

            if (count > 0) {
//                log.info("meanwhile {} pairs were formed", count / 2);
                while (count > 0) {
                    int r = (int) (Math.random() * NEWBORN_RATE);
                    while (r-- > 0) {
                        createNewbornHedgehog(RandomStringGenerator.RandomStringGeneratorFactory.getRandomStringGenerator("names").getValue());
                    }
                    count -= 2;
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            RandomStringGenerator.RandomStringGeneratorFactory.buildRandomStringGenerator("names", "/CSV_Database_of_First_Names.csv");
            HedgehogService service = new HedgehogServiceImpl();
            service.start();
        }
        catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }
}
