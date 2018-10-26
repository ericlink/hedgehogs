package hedgehogs.data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@DynamoDBTable(tableName="hedgehogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hedgehog {
    @DynamoDBHashKey(attributeName="id")
    private String id;

    private String name;
    private String color;

    private int    appleCapacity;
    private int    ageInWeeks;
    private float  weightInApples;
    private float  torqueInApplePerInch;
}
