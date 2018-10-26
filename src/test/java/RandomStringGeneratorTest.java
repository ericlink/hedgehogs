import org.junit.Test;
import hedgehogs.util.RandomStringGenerator;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertNotNull;

public class RandomStringGeneratorTest {

    @Test
    public void testFactory() throws IOException {
        RandomStringGenerator v1 = RandomStringGenerator.RandomStringGeneratorFactory.buildRandomStringGenerator("animal", "/CSV_Database_of_AnimalWords.csv");
        RandomStringGenerator v2 = RandomStringGenerator.RandomStringGeneratorFactory.buildRandomStringGenerator("animal", "/CSV_Database_of_AnimalWords.csv");
        assertNotNull("expecting to get not null generator instance", v1);
        assertTrue("expecting to get new generator instance", v1 != v2);
        assertNotNull("expecting to get not null generator instance", RandomStringGenerator.RandomStringGeneratorFactory.getRandomStringGenerator("animal"));
    }

    @Test
    public void testGenerator() throws IOException {
        RandomStringGenerator.RandomStringGeneratorFactory.buildRandomStringGenerator("animal", "/CSV_Database_of_AnimalWords.csv");

        final String v1 = RandomStringGenerator.RandomStringGeneratorFactory.getRandomStringGenerator("animal").getValue();
        final String v2 = RandomStringGenerator.RandomStringGeneratorFactory.getRandomStringGenerator("animal").getValue();

        assertNotNull("expecting to get not null word", v1);
        assertTrue("expecting to get different words", v1 != v2);
    }
}
