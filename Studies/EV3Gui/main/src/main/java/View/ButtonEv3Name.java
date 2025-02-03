package View;


import javafx.scene.Cursor;
import javafx.scene.control.Button;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;

import static java.lang.System.currentTimeMillis;

public class ButtonEv3Name extends Button {
    long instant;
    long previnstant=0;
    MqttClient client;

    public void setClient(MqttClient client) {
        this.client = client;
    }

    public ButtonEv3Name() {

        this.setOnMousePressed(press -> {
            instant = currentTimeMillis();
            if ((instant - previnstant <= 500)) {
                //////////////////////
                JSONObject obj = new JSONObject();

                obj.put("ev3", "ev3B");
                obj.put("1", "outA");
                obj.put("2", "Large");
                System.out.print(obj);
                MqttMessage message = new MqttMessage();
                message.setPayload(obj.toJSONString().getBytes());
                try {
                   client.publish("ev3_config", message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                ////
                previnstant = 0;
            } else previnstant = instant;
        });
        this.setCursor(Cursor.HAND);
    }
}
