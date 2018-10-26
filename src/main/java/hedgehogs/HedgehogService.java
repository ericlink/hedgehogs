package hedgehogs;

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
     * publishes new hedgehog with given name as a newborn
     * @param name
     */
    void createNewbornHedgehog(final String name);


    /**
     * lists all current hedgehogs
     */
    void listHedgehogs();

    /**
     * invokes next evolutionCycle for given number of weeks
     * @param period
     */
    void evolutionCycle(final int period);

   
    /**
     * runs death cycle on hedgehogs
     * @param cutOffAge
     */
    void deathCycle(final int cutOffAge);
}
