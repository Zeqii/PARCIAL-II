package repository;
import model.Region;
import model.Province;
import model.Report;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
public class DatabaseHelper {
    // Propiedades de la base de datos cargadas desde application.properties
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;
    // Bloque estático para cargar las propiedades al inicializar la clase
    static {
        Properties properties = new Properties();
        try (InputStream input = DatabaseHelper.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("No se pudo encontrar el archivo 'application.properties'");
            }
            properties.load(input);
            // Leer las propiedades de la base de datos
            DB_URL = properties.getProperty("spring.datasource.url");
            DB_USER = properties.getProperty("spring.datasource.username");
            DB_PASSWORD = properties.getProperty("spring.datasource.password");
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar las propiedades de la base de datos", e);
        }
    }
    /**
     * Guarda una región en la base de datos.
     * Primero verifica si la región ya existe (por ISO), de lo contrario, inserta el nuevo registro.
     */
    public void saveRegion(String iso, String name) {
        String checkSql = "SELECT COUNT(*) FROM regions WHERE iso = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
             
            checkStatement.setString(1, iso);
            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);  
            if (count > 0) { 
                // La región ya existe, no se realiza ninguna acción.
            } else {
                // Si no existe, proceder con la inserción
                String sql = "INSERT INTO regions (iso, name) VALUES (?, ?)";
                executeUpdate(sql, iso, name);
            }
        } catch (Exception e) {
            // Se encuentran posibles errores, se podría activar el log para depuración
            // System.err.println("Error al ejecutar la consulta: " + e.getMessage());
            // e.printStackTrace();
        }
    }
    /**
     * Guarda una provincia en la base de datos.
     * Realiza la verificación previa mediante el nombre de la provincia para evitar duplicados.
     */
    public void saveProvince(String iso, String name, String province) {
        String checkSql = "SELECT COUNT(*) FROM provinces WHERE name = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
             
            checkStatement.setString(1, name);
            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);
            if (count > 0) {
                // La provincia ya existe, no se hace nada.
            } else {
                String sql = "INSERT INTO provinces (iso, name, province) VALUES (?, ?, ?)";
                executeUpdate(sql, iso, province, province);
            }
        } catch (Exception e) {
            // Manejo de error en ejecución, se podría activar logging si es necesario
            // System.err.println("Error al ejecutar la consulta: " + e.getMessage());
            // e.printStackTrace();
        }
    }
    /**
     * Método auxiliar para ejecutar consultas SQL de tipo INSERT/UPDATE.
     * Se utilizan parámetros variables para evitar inyecciones SQL y facilitar la reutilización.
     *
     * @param sql    Consulta SQL a ejecutar.
     * @param params Parámetros necesarios para la consulta.
     */
    private void executeUpdate(String sql, Object... params) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
             
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                // Se pudo ejecutar correctamente la consulta
                // System.out.println("Consulta ejecutada correctamente.");
            } else {
                // No se afectaron filas con la consulta
                // System.out.println("No se afectaron filas con la consulta.");
            }
        } catch (Exception e) {
            // Se suprime la traza del error o se puede activar para depuración
            // System.err.println("❌ Error al ejecutar el SQL: " + e.getMessage());
            // e.printStackTrace();
        }
    }
}