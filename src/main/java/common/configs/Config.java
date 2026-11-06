package common.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";

    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties");
        }
    }

    public static String getProperty(String key) {
        /*
        Приоритеты:
        1. System Properties (-Dkey=value): для CI/CD
        2. Environment Variables (KEY=value): для Docker-а, переменная  в environment в Docker compose
        3. config.properties (значения по умолчанию): для локального запуска
         */

        // ПРИОРИТЕТ 1 - это системное свойство (-DapiBaseUrl =..)
        String value = System.getProperty(key);

        if (value != null && !value.isBlank()) {
            return value;
        }

        // ПРИОРИТЕТ 2 - это переменная окружения контейнера (APIBASEURL=)
        // Пример 1: apiBaseUrl -> переменная окружения будет APIBASEURL
        // Пример 2: admin.username -> ADMIN_USERNAME
        String envKey = key.toUpperCase().replace('.', '_');
        value = System.getenv(envKey);

        if (value != null && !value.isBlank()) {
            return value;
        }

        // ПРИОРИТЕТ 3 - это config.properties
        return INSTANCE.properties.getProperty(key);
    }

    public static String getProperty(Property property) {
        return getProperty(property.getKey());
    }
}
