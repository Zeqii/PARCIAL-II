package repository;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
/**
 * Repository to manage execution reports.
 * It verifies if a country (ISO) has been processed for a specific date
 * and registers new executions to avoid duplicate processing.
 *
 * Esta clase sigue los principios de Responsabilidad Única (SRP) y
 * permite gestionar los registros de ejecución sin afectar otras partes del sistema.
 */
public class ExecutionReportRepository {
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;
    // Bloque estático para cargar las propiedades de la base de datos desde application.properties
    static {
        Properties properties = new Properties();
        try (InputStream input = ExecutionReportRepository.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Cannot find 'application.properties' file.");
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
     * Verifica si un país (ISO) ya fue procesado para la fecha de ejecución dada.
     *
     * @param countryIso    El código ISO del país.
     * @param executionDate La fecha de ejecución en formato "yyyy-MM-dd".
     * @return true si el registro de ejecución existe; false de lo contrario.
     */
    public boolean hasExecutionBeenProcessed(String countryIso, String executionDate) {
        String sql = "SELECT COUNT(*) FROM executed_reports WHERE country_iso = ? AND execution_date = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, countryIso);
            statement.setString(2, executionDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking execution record: " + e.getMessage());
        }
        return false;
    }
    /**
     * Registra un nuevo registro de ejecución que indica que el país ha sido procesado.
     *
     * @param countryIso    El código ISO del país.
     * @param executionDate La fecha de ejecución en formato "yyyy-MM-dd".
     */
    public void saveExecution(String countryIso, String executionDate) {
        String sql = "INSERT INTO executed_reports (execution_date, country_iso) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, executionDate);
            statement.setString(2, countryIso);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving execution record: " + e.getMessage());
        }
    }
}