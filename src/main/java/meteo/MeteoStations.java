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
    private String uniqueID = "ID";
    private static int uniqueNumber = 0;

    public MeteoStations(){
        uniqueID += uniqueNumber;
        uniqueNumber++;
        setName(uniqueID);
    }

    @Override
    public void run() {
        String publisherId = UUID.randomUUID().toString();
        try (MqttClient client = new MqttClient(Constantes.MQTT_SERVER_URI, publisherId)){
            Constantes.connectMqttClient(client);
            while (true) {
                client.publish(createMqttTopic(), new MqttMessage(createMqttMsg()));
                //Duerme o espera durante 5 segundos
                sleep(5000);
            }
        } catch (MqttException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    //Genera una temperatura aleatoria
    public float generateTemperature(){
        int min = -10;
        int max = 40;

        return new Random().nextFloat(max - min) + min;
    }



    //Genera el mensaje
    public byte[] createMqttMsg(){
        String msg = String.format("%s#%s#%s", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                String.format("%.2f", generateTemperature()));

        return msg.getBytes();
    }

    //Genera el topico
    public String createMqttTopic(){
        String topic = String.format("/%s/METEO/", Constantes.INICIALES_ALUMNO) + uniqueID +
                "/MEASUREMENTS";

        return topic;
    }
}
