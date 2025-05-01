package com.umg.covid19;
import scheduler.CovidScheduler;
import util.ConfigurationReader;
/**
 * Clase principal de la aplicación COVID-19.
 * Esta clase se encarga de iniciar un hilo que espera un retardo configurado 
 * (obtenido desde application.properties) antes de ejecutar la lógica principal 
 * definida en el CovidScheduler.
 */
public class Main {
    
    public static void main(String[] args) {
        // Leer la propiedad de retardo inicial (en milisegundos) desde application.properties
        String delayProp = ConfigurationReader.getProperty("scheduler.initial-delay");
        long delay = 15000L; // Valor por defecto: 15 segundos
        // Intentar parsear el valor configurado en caso de que exista
        if (delayProp != null && !delayProp.isEmpty()) {
            try {
                delay = Long.parseLong(delayProp);
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear 'scheduler.initial-delay', usando el valor por defecto (15000 ms).");
            }
        }
        
        System.out.println("Cargando, espere " + (delay / 1000) + " segundos...");
        
        // Declarar la variable delay como final para poder ser utilizada en la lambda
        final long delayFinal = delay;
        
        // Crear un hilo que espere el retardo configurado y luego ejecute la lógica principal del scheduler
        Thread schedulerThread = new Thread(() -> {
            try {
                Thread.sleep(delayFinal);
                // Ejecuta la lógica principal del CovidScheduler
                CovidScheduler.executeMainLogic();
            } catch (InterruptedException e) {
                System.err.println("El hilo fue interrumpido durante la espera: " + e.getMessage());
            }
        });
        
        // Iniciar el hilo creado
        schedulerThread.start();
        
        // Esperar a que el hilo del scheduler finalice su ejecución
        try {
            schedulerThread.join();
        } catch (InterruptedException e) {
            System.err.println("El hilo principal fue interrumpido: " + e.getMessage());
        }
    }
}