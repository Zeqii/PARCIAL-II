package util;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.RegionDTO;
import dto.ProvinceDTO;
import dto.ReportDTO;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
/**
 * ApiClient se encarga de consumir la API externa para obtener datos de regiones,
 * provincias y reportes COVID-19. La configuración (clave y host) se carga desde el archivo
 * application.properties y se utiliza Apache HttpClient para hacer las solicitudes HTTP.
 */
public class ApiClient {
    // Propiedades de la API cargadas desde application.properties
    private static final String RAPID_API_KEY;
    private static final String RAPID_API_HOST;
    // Bloque estático para cargar las propiedades al inicializar la clase
    static {
        Properties properties = new Properties();
        try (InputStream input = ApiClient.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("No se pudo encontrar el archivo 'application.properties'");
            }
            properties.load(input);
            // Leer las propiedades de la API
            RAPID_API_KEY = properties.getProperty("rapidapi.key");
            RAPID_API_HOST = properties.getProperty("rapidapi.host");
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar las propiedades de la API", e);
        }
    }
    /**
     * Obtiene una lista de regiones desde la API.
     *
     * @return Lista de RegionDTO.
     * @throws Exception Si ocurre algún error en la solicitud o mapeo de la respuesta.
     */
    public List<RegionDTO> fetchRegions() throws Exception {
        String url = "https://" + RAPID_API_HOST + "/regions";
        return executeGetRequest(url, RegionDTO.class);
    }
    /**
     * Obtiene una lista de provincias para una región específica (ISO) desde la API.
     *
     * @param iso Código ISO de la región.
     * @return Lista de ProvinceDTO.
     * @throws Exception Si ocurre algún error en la solicitud o mapeo de la respuesta.
     */
    public List<ProvinceDTO> fetchProvinces(String iso) throws Exception {
        String url = "https://" + RAPID_API_HOST + "/provinces?iso=" + iso;
        return executeGetRequest(url, ProvinceDTO.class);
    }
    /**
     * Obtiene una lista de reportes para una provincia específica (ISO) y fecha desde la API.
     *
     * @param provinceCode Código de la provincia.
     * @param date Fecha en la que se requieren los reportes.
     * @return Lista de ReportDTO.
     * @throws Exception Si ocurre algún error en la solicitud o mapeo de la respuesta.
     */
    public List<ReportDTO> fetchReports(String provinceCode, String date) throws Exception {
        // Se utiliza un parámetro de fecha fijo para este ejemplo. Se podría parametrizar para más flexibilidad.
        String url = "https://" + RAPID_API_HOST + "/reports?iso=" + provinceCode + "&date=2020-04-16";
        return executeGetRequest(url, ReportDTO.class);
    }
    /**
     * Método auxiliar que ejecuta una solicitud GET a la URL indicada y mapea la respuesta JSON
     * a una lista de objetos del tipo indicado.
     *
     * @param url URL de la solicitud.
     * @param clazz Clase del objeto DTO al que se hará el mapeo.
     * @return Lista de objetos mapeados desde el JSON.
     * @param <T> Tipo del DTO.
     * @throws Exception Si ocurre algún error durante la solicitud HTTP o el mapeo JSON.
     */
    private <T> List<T> executeGetRequest(String url, Class<T> clazz) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            // Agregar encabezados requeridos por RapidAPI
            request.addHeader("X-RapidAPI-Key", RAPID_API_KEY);
            request.addHeader("X-RapidAPI-Host", RAPID_API_HOST);
            // Ejecutar la solicitud HTTP
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Error al llamar a la API: " 
                        + response.getStatusLine().getReasonPhrase());
                }
                // Convertir la respuesta a String (JSON)
                String jsonResponse = EntityUtils.toString(response.getEntity());
                // Utilizar ObjectMapper para deserializar el JSON
                ObjectMapper objectMapper = new ObjectMapper();
                // Construir el tipo genérico para deserialización: ApiResponse<T>
                JavaType responseType = objectMapper.getTypeFactory()
                        .constructParametricType(ApiResponse.class, clazz);
                // Mapear el JSON a ApiResponse<T>
                ApiResponse<T> apiResponse = objectMapper.readValue(jsonResponse, responseType);
                // Retornar la lista contenida en el atributo "data"
                return apiResponse.getData();
            }
        }
    }
}