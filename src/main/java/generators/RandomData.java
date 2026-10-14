package generators;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomData {

    private RandomData() {
    }

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(4).toUpperCase() +
                RandomStringUtils.randomAlphabetic(3).toLowerCase() +
                RandomStringUtils.randomNumeric(2) +
                "$)";
    }
}
