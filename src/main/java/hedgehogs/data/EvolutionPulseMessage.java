package hedgehogs.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EvolutionPulseMessage {
    EvolutionPulse message;
    String         topicArn;
    String         messageId;
    String         type;
}
