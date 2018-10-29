package hedgehogs;

import hedgehogs.data.EvolutionPulse;
import hedgehogs.data.Hedgehog;

/**
 * Simple service that maintains hedgehogs in forest
 * Using dynamoDb as storage and DynamoDb mapper
 * 
 * @see Hedgehog
 * @see com.amazonaws.services.dynamodbv2.document.DynamoDB
 */

interface HedgehogService {
    /**
     * fires service up
     */
    void start();

    /**
     * called for next evolution cycle
     * @param evolutionPulse
     */
    void handle(final EvolutionPulse evolutionPulse);

    /**
     * tears service down
     */
    void shutdown();
}
