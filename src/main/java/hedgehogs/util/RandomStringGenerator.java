package hedgehogs.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Just your basic random string generator. Useful to play with random names and so on.
 */
public class RandomStringGenerator {
    private final List<String> values;

    /**
     *
     * @param fileName
     * @throws IOException
     */
    private RandomStringGenerator(final String fileName)  throws IOException {
        this.values = readFileFromResourcesPack(fileName);
    }

    /**
     * loads file from resource / classpath bundle
     * @param name
     * @return
     * @throws IOException
     */
    private List<String> readFileFromResourcesPack(final String name) throws IOException {
        Class clazz = RandomStringGenerator.class;
        InputStream inputStream = clazz.getResourceAsStream(name);

        final List<String> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }

    /**
     * returns random value from loaded set
     * @return
     */
    public String getValue() {
        return values.get(generateIndex());
    }

    /**
     * generates random index from 0 to size - 1
     * @return
     */
    private int generateIndex() {
        return Math.max(0, (int)(Math.random() * (values.size() - 1f)));
    }

    /**
     * factory class to control instances
     */
    public static class RandomStringGeneratorFactory {
        private static final Map<String, RandomStringGenerator> generatorMap = new ConcurrentHashMap<>();

        /**
         * builds new instance
         * @param name name of instance
         * @param fileName csv file with strings
         * @return instance of newly loaded file
         * @throws IOException
         */
        public static RandomStringGenerator buildRandomStringGenerator(final String name, final String fileName) throws IOException{
            final RandomStringGenerator v = new RandomStringGenerator(fileName);
            generatorMap.put(name, v);
            return v;
        }

        /**
         * retrieves existing instance by name
         * @param name
         * @return
         */
        public static RandomStringGenerator getRandomStringGenerator(final String name) {
            return generatorMap.get(name);
        }
    }
}
