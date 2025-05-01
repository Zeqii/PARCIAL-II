package service;
import dto.RegionDTO;
import dto.ProvinceDTO;
import dto.ReportDTO;
import repository.DatabaseHelper;
import util.ApiClient;
import java.util.List;
/**
 * Service class responsible for fetching COVID-19 data.
 * It communicates with the ApiClient to retrieve regions, provinces, and reports data.
 */
public class CovidDataService {
    private final ApiClient apiClient = new ApiClient();
    @SuppressWarnings("unused")
	private final DatabaseHelper databaseHelper = new DatabaseHelper();
    /**
     * Obtiene la lista de regiones desde la API.
     *
     * @return Lista de RegionDTO.
     * @throws Exception en caso de error durante la ejecución de la llamada a la API.
     */
    public List<RegionDTO> fetchRegions() throws Exception {
        return apiClient.fetchRegions();
    }
    /**
     * Obtiene la lista de provincias para una región específica.
     *
     * @param iso Código ISO de la región.
     * @return Lista de ProvinceDTO.
     * @throws Exception en caso de error durante la llamada a la API.
     */
    public List<ProvinceDTO> fetchProvinces(String iso) throws Exception {
        return apiClient.fetchProvinces(iso);
    }
    /**
     * Obtiene la lista de reportes para una provincia específica y fecha.
     *
     * @param provinceCode Código de la provincia.
     * @param date         Fecha a consultar en formato ISO, ejemplo "2022-04-16".
     * @return Lista de ReportDTO.
     * @throws Exception en caso de error durante la llamada a la API.
     */
    public List<ReportDTO> fetchReports(String provinceCode, String date) throws Exception {
        return apiClient.fetchReports(provinceCode, date);
    }
}