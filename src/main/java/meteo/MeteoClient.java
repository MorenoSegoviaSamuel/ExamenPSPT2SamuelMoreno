package meteo;

import constantes.Constantes;
import redis.clients.jedis.Jedis;

import java.util.Scanner;
import java.util.Set;

public class MeteoClient {
    public static void main(String[] args) {
        String command = "";
        Scanner sc = new Scanner(System.in);

        // Crea una instancia de Jedis para interactuar con la base de datos Redis
        try (Jedis jedis = new Jedis(Constantes.REDIS_SERVER_URI, Constantes.REDIS_SERVER_PORT)) {
            // Bucle principal de la aplicación
            while (!command.equals("exit")) {
                System.out.print("Enter the command: ");

                // Lee el comando del usuario desde la consola
                String rawCommand = sc.nextLine() + " ";
                String[] commandSplit = rawCommand.split(" ", 2);
                command = commandSplit[0];

                // Procesa el comando ingresado
                switch (command) {
                    case "LAST" -> {
                        // Obtiene la última temperatura de la estación meteorológica especificada
                        String lastTemperature = jedis.hget(String.format(Constantes.HASH_KEY_LAST_TEMPERATURE_REDIS, Constantes.INICIALES_ALUMNO, commandSplit[1]), "temperature");
                        System.out.printf("Esta es la última temperatura registrada por la estación %s: %s\n", commandSplit[1], lastTemperature);
                    }
                    case "MAXTEMP" -> {

                    }
                    case "ALERTS" -> {
                        // Obtiene y muestra las alertas actuales desde Redis
                        Set<String> keys = jedis.keys("SMS:ALERTS");
                        if (keys != null) {
                            for (String key : keys) {
                                String alert = jedis.get(key);
                                System.out.println(alert);
                                jedis.del(key);  // Elimina la alerta después de mostrarla
                            }
                        }
                    }
                }
            }
        }
    }
}
