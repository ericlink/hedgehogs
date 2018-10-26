package hedgehogs;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import hedgehogs.data.Hedgehog;
import lombok.extern.slf4j.Slf4j;
import hedgehogs.util.RandomStringGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

@Slf4j
public class HedgehogServiceImpl implements HedgehogService {

    private static final int   CYCLES      = 20;
    private static final int   NEWBORN_RATE = 4;
    private static final int   ADULT_AGE    = 24; // age at which babies can be made
    private static final int   MAX_AGE    = 96; // max age
    private static final float MAX_WEIGHT = 10; // max weight
    private static final float INCREASE_IN_TORQUE = 0.4f; // how much torque increase per week as hedgehog develops
    private static final float INCREASE_IN_WEIGHT = 0.5f; // how much weight might increase per week

    private static AmazonDynamoDB client = AmazonDynamoDBClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4569/", "us-east-1"))
            .build();

    /**
     * publishes new hedgehog with given name as a newborn
     * @param name
     */
    public void createNewbornHedgehog(final String name) {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        Hedgehog hedgehog = Hedgehog.builder().id(UUID.randomUUID().toString()).name(name).weightInApples(0.1f).build();
        mapper.save(hedgehog);
    }


    /**
     * lists all current hedgehogs
     */
    public void listHedgehogs() {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        List<Hedgehog> list = mapper.scan(Hedgehog.class, new DynamoDBScanExpression());

        if (list.isEmpty()) {
            log.info("no hedgehogs found in forest");
        }
        else {
            for (Hedgehog hedgehog: list) {
                log.info("{} age={} weeks, weight={} apples", hedgehog.getName(), hedgehog.getAgeInWeeks(), hedgehog.getAgeInWeeks());
            }
        }
    }

    /**
     * invokes next evolutionCycle for given number of weeks
     * @param period
     */
    public void evolutionCycle(final int period) {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        List<Hedgehog> list = mapper.scan(Hedgehog.class, new DynamoDBScanExpression());

        if (list.isEmpty()) {
            log.info("no hedgehogs found in forest");
            // need to make new ones
            for (int i = 0; i < NEWBORN_RATE; i++) {
                createNewbornHedgehog(RandomStringGenerator.RandomStringGeneratorFactory.getRandomStringGenerator("names").getValue());
            }
            log.info(NEWBORN_RATE + " newborn hedgehogs were brought from zoo");
        }
        else {
            int count = 0;
            for (Hedgehog hedgehog: list) {
                updateHedgehogStats(period, hedgehog);
                if (hedgehog.getWeightInApples() > MAX_WEIGHT) {
                    log.info("{} died from overeating, gaining weight of {} apples", hedgehog.getName(), hedgehog.getWeightInApples());
                    mapper.delete(hedgehog);
                }
                else
                if (hedgehog.getAgeInWeeks() > MAX_AGE) {
                    log.info("{} died of old age", hedgehog.getName());
                    mapper.delete(hedgehog);
                }
                else {
                    if (hedgehog.getAgeInWeeks() >= ADULT_AGE) {
                        count++;
                    }
                    mapper.save(hedgehog);
                }
            }
            // every two adult hedgehogs can give birth to babies, we ignore genders for MVP

            if (count > 0) {
                log.info("meanwhile {} pairs were formed", count / 2);
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

    /**
     * updates given hedgehog
     * @param period in week
     * @param hedgehog
     */
    private void updateHedgehogStats(final int period, Hedgehog hedgehog) {
        hedgehog.setAgeInWeeks(hedgehog.getAgeInWeeks() + period);
        hedgehog.setTorqueInApplePerInch(hedgehog.getTorqueInApplePerInch() + period * INCREASE_IN_TORQUE);
        hedgehog.setWeightInApples(hedgehog.getWeightInApples() + calculateNewWeight(period));
    }

    /**
     * randomized weight gain
     * @param period
     * @return
     */
    private float calculateNewWeight(final int period) {
        float v = 0;
        for (int i = 0; i < period; i++ ) {
            v += (INCREASE_IN_WEIGHT - Math.random() * INCREASE_IN_WEIGHT);
        }
        return v;
    }

    /**
     * runs death cycle on hedgehogs
     * @param cutOffAge
     */
    public void deathCycle(final int cutOffAge) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withN(String.valueOf(cutOffAge)));
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("ageInWeeks > :val1").withExpressionAttributeValues(eav);

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        List<Hedgehog> list = mapper.scan(Hedgehog.class, scanExpression);

        if (list.isEmpty()) {
            log.info("no hedgehogs found in forest, nothing to kill");
        }
        else {
            for (Hedgehog hedgehog: list) {
                log.info(hedgehog.getName() + " died  at the tender age of " + hedgehog.getAgeInWeeks() + " weeks");
            }
        }
    }

    public static void main(String[] args) {
        log.info("testing hedgehogs here");

        HedgehogServiceImpl service = new HedgehogServiceImpl();


        try {

            DescribeTableResult describeTableResult = client.describeTable("hedgehogs");
            RandomStringGenerator.RandomStringGeneratorFactory.buildRandomStringGenerator("names", "/CSV_Database_of_First_Names.csv");

            log.info(describeTableResult.toString());

            int cycles = CYCLES;

            log.info("Forest comes to life for " + cycles + " cycles");

            while (cycles-- > 0) {
                log.info("********* CYCLE: " + (CYCLES - cycles) + " **********");
                service.listHedgehogs();
                service.evolutionCycle(2); // 2 weeks periods
            }


            log.info("final cleanup");
            service.deathCycle(0);

        }
        catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }
}
