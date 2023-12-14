package meteo;

import constantes.Constantes;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public class MeteoStations extends Thread {
    // Atributos de la clase
    private String uniqueID = "ID";
    private static int uniqueNumber = 0;

    // Constructor de la clase
    public MeteoStations(){
        uniqueID += uniqueNumber;
        uniqueNumber++;
        setName(uniqueID);
    }

    // Método principal del hilo
    @Override
    public void run() {
        // Genera un identificador único para el cliente MQTT
        String publisherId = UUID.randomUUID().toString();

        try (MqttClient client = new MqttClient(Constantes.MQTT_SERVER_URI, publisherId)){
            // Conecta el cliente MQTT
            Constantes.connectMqttClient(client);

            // Bucle infinito para enviar información por MQTT y esperar 5 segundos
            while (true) {
                // Publica la información en un topic MQTT
                client.publish(createMqttTopic(), new MqttMessage(createMqttMsg()));

                // Duerme o espera durante 5 segundos
                sleep(5000);
            }
        } catch (MqttException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Genera una temperatura aleatoria
    public float generateTemperature(){
        int min = -10;
        int max = 40;

        return new Random().nextFloat() * (max - min) + min;
    }

    // Genera el mensaje MQTT
    public byte[] createMqttMsg(){
        String msg = String.format("%s#%s#%s", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                String.format("%.2f", generateTemperature()));

        return msg.getBytes();
    }

    // Genera el topic MQTT
    public String createMqttTopic(){
        String topic = String.format("/%s/METEO/", Constantes.INICIALES_ALUMNO) + uniqueID +
                "/MEASUREMENTS";

        return topic;
    }
}
