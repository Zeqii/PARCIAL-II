package scheduler;
import dto.ProvinceDTO;
import dto.ReportDTO;
import service.CovidDataService;
import repository.DatabaseHelper;
import repository.ExecutionReportRepository;
import java.util.List;
import java.util.Scanner;
/**
 * Task that executes the COVID data processing for a given country (ISO) and execution date.
 * This class implements Runnable so that it can be executed in a separate thread.
 * 
 * Responsibilities:
 * - Verify if the execution for the given country and date has already been processed.
 * - If not, execute the complete flow: get provinces and their reports.
 * - Register the successful execution.
 */
public class CovidExecutionTask implements Runnable {
    // Dependencies injected through the constructor for easier testing and decoupling
    private final String countryIso;
    private final String executionDate;
    private final CovidDataService covidDataService;
    private final DatabaseHelper dbHelper;
    private final ExecutionReportRepository executionRepo;
    private final Scanner scanner; // Used for user interaction if needed
    /**
     * Constructor which injects the required dependencies.
     *
     * @param countryIso     ISO code of the country to process.
     * @param executionDate  Execution date (should be configured in application.properties).
     * @param scanner        Scanner for reading user input if necessary.
     */
    public CovidExecutionTask(String countryIso, String executionDate, Scanner scanner) {
        this.countryIso = countryIso;
        this.executionDate = executionDate;
        this.covidDataService = new CovidDataService();
        this.dbHelper = new DatabaseHelper();
        this.executionRepo = new ExecutionReportRepository();
        this.scanner = scanner;
    }
    @Override
    public void run() {
        try {
            // Check if the execution has already been processed for this country and date.
            if (executionRepo.hasExecutionBeenProcessed(countryIso, executionDate)) {
                System.out.println("INFO: El país con ISO '" + countryIso 
                    + "' ya fue procesado para la fecha " + executionDate + ". Se omite la ejecución.");
                return;
            }
            // Example processing: Get provinces for the country.
            List<ProvinceDTO> provinces = covidDataService.fetchProvinces(countryIso);
            if (provinces == null || provinces.isEmpty()) {
                System.out.println("No se encontraron provincias para el país con ISO: " + countryIso);
                return;
            }
            // Process each province: display the reports and save the information in the database.
            for (ProvinceDTO province : provinces) {
                System.out.println("Procesando reportes para la provincia: " + province.getProvince());
                List<ReportDTO> reports = covidDataService.fetchReports(province.getIso(), executionDate);
                if (reports != null && !reports.isEmpty()) {
                    for (ReportDTO report : reports) {
                        System.out.println(report.toString());
                    }
                } else {
                    System.out.println("No se encontraron reportes para la provincia: " + province.getProvince());
                }
                // Optional: Save the province in the database if necessary.
                dbHelper.saveProvince(province.getIso(), province.getCode(), province.getProvince());
            }
            // After successful processing, register the execution to avoid duplications in future executions.
            executionRepo.saveExecution(countryIso, executionDate);
            System.out.println("INFO: País con ISO '" + countryIso + "' procesado exitosamente para la fecha " + executionDate + ".");
        } catch (Exception e) {
            System.err.println("Error en la ejecución del task para el país " + countryIso + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}