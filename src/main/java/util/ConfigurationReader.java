package util;
import java.io.InputStream;
import java.util.Properties;
/**
 * Utility class to read configuration properties from application.properties.
 */
public final class ConfigurationReader {
    private static final Properties properties = new Properties();
    // Static block to load properties once
    static {
        try (InputStream input = ConfigurationReader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Cannot find 'application.properties' file.");
            }
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Error loading configuration properties", e);
        }
    }
    /**
     * Returns the property value for the given key.
     *
     * @param key The configuration key.
     * @return The corresponding property value.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}