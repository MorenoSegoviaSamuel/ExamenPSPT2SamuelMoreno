package meteo;

import constantes.Constantes;
import org.eclipse.paho.client.mqttv3.*;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class MeteoServer {
    public static void main(String[] args) {
        String publisherId = UUID.randomUUID().toString();
        try (Jedis jedis = new Jedis(Constantes.REDIS_SERVER_URI, Constantes.REDIS_SERVER_PORT)) {

            try (MqttClient client = new MqttClient(Constantes.MQTT_SERVER_URI, publisherId)){
                Constantes.connectMqttClient(client);
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        System.out.println("Connection to Solace broker lost! " + throwable.getMessage());
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                        String[] topicSplit = topic.substring(1).split("/");
                        String[] msgSplit = new String(mqttMessage.getPayload()).split("#");
                        String jedisHash = String.format(Constantes.HASH_KEY_LAST_TEMPERATURE_REDIS, Constantes.INICIALES_ALUMNO, topicSplit[2]);
                        String jedisList = String.format("%s:TEMPERATURES:%s", Constantes.INICIALES_ALUMNO, topicSplit[2]);
                        String dateTime = String.format("%s-%s", msgSplit[0], msgSplit[1]);
                        jedis.hset(jedisHash, "datetime", dateTime);
                        jedis.rpush(jedisList, "temperature", msgSplit[2]);
                        float degrees = Float.parseFloat(msgSplit[2]);
                        if (degrees > 30f || degrees < 0f) {
                            jedis.set(Constantes.ALERTS_KEY_REDIS, String.format(Constantes.FORMAT_ALERT_STRING, topicSplit));
                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    }
                });

                client.subscribe(Constantes.subscribeAllStationsTopics, 0);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
