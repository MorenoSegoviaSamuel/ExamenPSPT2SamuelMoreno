package meteo;

import constantes.Constantes;
import org.eclipse.paho.client.mqttv3.*;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class MeteoServer {
    public static void main(String[] args) {
        // Genera un identificador único para el cliente MQTT
        String publisherId = UUID.randomUUID().toString();

        // Crea una instancia de Jedis para interactuar con la base de datos Redis
        try (Jedis jedis = new Jedis(Constantes.REDIS_SERVER_URI, Constantes.REDIS_SERVER_PORT)) {
            // Crea una instancia de cliente MQTT
            try (MqttClient client = new MqttClient(Constantes.MQTT_SERVER_URI, publisherId)) {
                // Conecta y configura el cliente MQTT
                Constantes.connectMqttClient(client);

                // Configura el callback para manejar eventos de MQTT
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        System.out.println("Connection to Solace broker lost! " + throwable.getMessage());
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                        // Divide el topic y el mensaje recibido
                        String[] topicSplit = topic.substring(1).split("/");
                        String[] msgSplit = new String(mqttMessage.getPayload()).split("#");

                        // Crea claves Redis para almacenar información
                        String jedisHash = String.format(Constantes.HASH_KEY_LAST_TEMPERATURE_REDIS, Constantes.INICIALES_ALUMNO, topicSplit[2]);
                        String jedisList = String.format("%s:TEMPERATURES:%s", Constantes.INICIALES_ALUMNO, topicSplit[2]);
                        String dateTime = String.format("%s-%s", msgSplit[0], msgSplit[1]);

                        // Almacena información en Redis
                        jedis.hset(jedisHash, "datetime", dateTime);
                        jedis.rpush(jedisList, "temperature", msgSplit[2]);

                        // Verifica si hay una alerta por temperaturas extremas
                        float degrees = Float.parseFloat(msgSplit[2]);
                        if (degrees > 30f || degrees < 0f) {
                            jedis.set(Constantes.ALERTS_KEY_REDIS, String.format(Constantes.FORMAT_ALERT_STRING, topicSplit));
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    }
                });

                // Subscripción a todos los topics de estaciones meteorológicas
                client.subscribe(Constantes.subscribeAllStationsTopics, 0);
            } catch (MqttException e) {
                // Manejo de excepciones al crear el cliente MQTT
                throw new RuntimeException(e);
            }
        }
    }
}
