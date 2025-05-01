package scheduler;
import dto.RegionDTO;
import dto.ProvinceDTO;
import dto.ReportDTO;
import service.CovidDataService;
import repository.DatabaseHelper;
import repository.ExecutionReportRepository;
import util.ConfigurationReader;
import java.util.List;
import java.util.Scanner;
/**
 * Scheduler to control the execution of COVID data processing.
 * Prevents reprocessing the same country for the specified execution date.
 * All messages and variable names are in English, while user prompts are in Spanish.
 */
public class CovidScheduler {
    /**
     * Main logic method. It allows the user to process regions repeatedly
     * unless the user decides to exit.
     */
    public static void executeMainLogic() {
        CovidDataService covidDataService = new CovidDataService();
        ExecutionReportRepository executionRepo = new ExecutionReportRepository();
        DatabaseHelper dbHelper = new DatabaseHelper();
        
        // Read execution date from application.properties (e.g., covid.report.date=2022-04-16)
        String executionDate = ConfigurationReader.getProperty("covid.report.date");
        if (executionDate == null || executionDate.trim().isEmpty()) {
            System.err.println("La propiedad 'covid.report.date' no está definida o es inválida en application.properties.");
            return;
        }
        
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;
        while (continuar) {
            try {
                // Step 1: Display regions and allow user to select one.
                List<RegionDTO> regions = covidDataService.fetchRegions();
                RegionDTO selectedRegion = displayItemsWithPagination(regions, "Regions", 10, scanner);
                if (selectedRegion == null) {
                    System.out.println("No se seleccionó ninguna región. Saliendo del procesamiento.");
                    break;
                }
                
                // Verificar si el país (ISO) fue procesado previamente para la fecha configurada.
                if (executionRepo.hasExecutionBeenProcessed(selectedRegion.getIso(), executionDate)) {
                    System.out.println("INFO: El país con ISO '" + selectedRegion.getIso()
                            + "' ya fue procesado para la fecha " + executionDate + ". Se omite la ejecución.");
                } else {
                    System.out.println("Selected Region: " + selectedRegion.getName());
                    // Guardar la región en DB
                    dbHelper.saveRegion(selectedRegion.getIso(), selectedRegion.getName());
                    
                    // Step 2: Display provinces for the selected region.
                    List<ProvinceDTO> provinces = covidDataService.fetchProvinces(selectedRegion.getIso());
                    ProvinceDTO selectedProvince = displayItemsWithPagination(provinces, "Provinces", 10, scanner);
                    if (selectedProvince == null) {
                        System.out.println("No se seleccionó ninguna provincia. Saliendo del procesamiento.");
                        continue;
                    }
                    
                    // Guardar provincias (opcional)
                    for (ProvinceDTO province : provinces) {
                        dbHelper.saveProvince(province.getIso(), province.getCode(), province.getProvince());
                    }
                    
                    // Step 3: Display reports for the selected province.
                    System.out.println("Processing reports for province: " + selectedProvince.getProvince());
                    List<ReportDTO> reports = covidDataService.fetchReports(selectedProvince.getIso(), executionDate);
                    displayReports(reports);
                    
                    // Register execution record after successful processing.
                    executionRepo.saveExecution(selectedRegion.getIso(), executionDate);
                    System.out.println("INFO: El país con ISO '" + selectedRegion.getIso()
                            + "' procesado exitosamente para la fecha " + executionDate + " y registrado.");
                }
                
                // Preguntar si desea procesar otra región.
                System.out.println("¿Desea procesar otra región? (S/N)");
                String respuesta = scanner.nextLine().trim();
                if (!respuesta.equalsIgnoreCase("S")) {
                    continuar = false;
                }
            } catch (Exception e) {
                System.err.println("Error durante la ejecución: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Saliendo del programa.");
        scanner.close();
    }
    /**
     * Displays a list of items with pagination and allows the user to select one.
     * If the user enters an invalid option (letter or number outside range), an error is shown and the menu repeats.
     *
     * @param items     The list of items.
     * @param itemType  The type of items (for display header).
     * @param pageSize  Number of items per page.
     * @param scanner   Scanner for user input.
     * @param <T>       Generic type, e.g., RegionDTO or ProvinceDTO.
     * @return The selected item or null if the user decides to exit.
     */
    private static <T> T displayItemsWithPagination(List<T> items, String itemType, int pageSize, Scanner scanner) {
        int totalPages = (int) Math.ceil((double) items.size() / pageSize);
        int currentPage = 1;
        T selectedItem = null;
        while (true) {
            System.out.println("\n--- " + itemType + " - Page " + currentPage + " of " + totalPages + " ---");
            
            // Display header based on item type.
            if (itemType.equals("Regions")) {
                System.out.println("INDEX  | ISO    | NAME");
            } else if (itemType.equals("Provinces")) {
                System.out.println("INDEX  | ISO    | CODE      | NAME                             | LAT                | LONG");
            }
            
            // Calculate indexes for current page.
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, items.size());
            
            // Display items.
            for (int i = startIndex; i < endIndex; i++) {
                if (items.get(i) instanceof dto.RegionDTO) {
                    System.out.println(((dto.RegionDTO) items.get(i)).toFormattedString(i + 1));
                } else if (items.get(i) instanceof dto.ProvinceDTO) {
                    System.out.println(((dto.ProvinceDTO) items.get(i)).toFormattedString(i + 1));
                }
            }
            
            // Menu options.
            System.out.println("\nOPTIONS: 1. Seleccionar un elemento | 2. Página siguiente | 3. Página anterior | 4. Salir");
            System.out.print("Ingrese opción: ");
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1": // Seleccionar un elemento.
                    System.out.print("Ingrese el INDEX: ");
                    String itemInput = scanner.nextLine().trim();
                    try {
                        int itemNumber = Integer.parseInt(itemInput);
                        // Validar que el índice ingresado esté dentro del rango total.
                        if (itemNumber >= 1 && itemNumber <= items.size()) {
                            selectedItem = items.get(itemNumber - 1);
                            return selectedItem;
                        } else {
                            System.out.println("Opción inválida. El número ingresado está fuera de rango. Intente nuevamente.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada inválida. Debe ingresar un número válido. Intente nuevamente.");
                    }
                    break;
                case "2": // Página siguiente.
                    if (currentPage < totalPages) {
                        currentPage++;
                    } else {
                        System.out.println("Ya estás en la última página.");
                    }
                    break;
                case "3": // Página anterior.
                    if (currentPage > 1) {
                        currentPage--;
                    } else {
                        System.out.println("Ya estás en la primera página.");
                    }
                    break;
                case "4": // Salir.
                    return null;
                default:
                    System.out.println("Opción inválida. Por favor, ingrese una de las opciones listadas.");
            }
        }
    }
    /**
     * Displays the list of reports on the console.
     *
     * @param reports The list of report DTOs.
     */
    private static void displayReports(List<ReportDTO> reports) {
        System.out.println("\n--- Reports ---");
        if (reports.isEmpty()) {
            System.out.println("No hay reportes disponibles.");
            return;
        }
        for (ReportDTO report : reports) {
            System.out.println(report.toString());
        }
    }
    public static void main(String[] args) {
        executeMainLogic();
    }
}