package repository;
import model.Report;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.io.InputStream;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Repository for querying Report data from the database.
 * This class retrieves all reports stored for a given country ISO and date,
 * grouping them in a TreeMap to remove duplicates and enforce alphabetical order.
 */
public class ReportRepository {
    private static final Logger logger = LoggerFactory.getLogger(ReportRepository.class);
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;
    // Static block to load database properties from application.properties
    static {
        Properties properties = new Properties();
        try (InputStream input = ReportRepository.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties file not found");
            }
            properties.load(input);
            DB_URL = properties.getProperty("spring.datasource.url");
            DB_USER = properties.getProperty("spring.datasource.username");
            DB_PASSWORD = properties.getProperty("spring.datasource.password");
        } catch (Exception e) {
            throw new RuntimeException("Error loading database properties", e);
        }
    }
    /**
     * Retrieves reports from the database for a specified country ISO and date.
     * Duplicates (multiple records for the same province) are eliminated by using
     * a TreeMap in which the key is the province code (or province name if available)
     * and the value is the Report object.
     * 
     * @param countryIso The ISO code of the country (e.g., "GTM").
     * @param date       The date to filter reports (format: yyyy-MM-dd).
     * @return           A TreeMap<String, Report> grouped by province, ordered alphabetically.
     */
    public TreeMap<String, Report> getReportsByCountryAndDate(String countryIso, String date) {
        if(countryIso == null || countryIso.isEmpty() || date == null || date.isEmpty()){
            throw new IllegalArgumentException("Country ISO and date must not be empty");
        }
        // TreeMap to store results, it will automatically order keys alphabetically.
        TreeMap<String, Report> reportsByProvince = new TreeMap<>();
        String sql = "SELECT * FROM reports WHERE regionIso = ? AND date = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, countryIso);
            statement.setString(2, date);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                // Create Report object and assign its properties
                Report report = new Report();
                report.setId(rs.getString("id"));
                report.setProvinceCode(rs.getString("provinceCode"));
                report.setRegionIso(rs.getString("regionIso"));
                report.setDate(rs.getString("date"));
                report.setConfirmed(rs.getInt("confirmed"));
                report.setDeaths(rs.getInt("deaths"));
                report.setRecovered(rs.getInt("recovered"));
                // Using provinceCode as key for the TreeMap.
                String key = report.getProvinceCode();
                if (reportsByProvince.containsKey(key)) {
                    logger.info("Duplicate report found for province: {}. Ignoring duplicate.", key);
                } else {
                    reportsByProvince.put(key, report);
                }
            }
        } catch (Exception e) {
            logger.error("Error querying reports: {}", e.getMessage(), e);
        }
        // Display the grouped data in the console
        logger.info("\n--- Reports Grouped by Province ---");
        for (String key : reportsByProvince.keySet()) {
            logger.info("Province: {} -> {}", key, reportsByProvince.get(key));
        }
        return reportsByProvince;
    }
}