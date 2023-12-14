package meteo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MeteoStationsPool {
    public static void main(String[] args) {
        // Número máximo de estaciones meteorológicas (hilos) en el pool
        int maxThreads = 10;

        // Crea un pool de hilos con un tamaño fijo
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);

        // Itera para crear y ejecutar instancias de MeteoStations
        for (int i = 0; i < maxThreads; i++){
            // Agrega una nueva instancia de MeteoStations al pool
            executor.execute(new MeteoStations());
        }

        // Cierra el pool de hilos después de que todas las estaciones meteorológicas han terminado
        executor.shutdown();
    }
}
