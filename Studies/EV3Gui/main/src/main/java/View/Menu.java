package View;

import Model.Ev3;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.json.simple.JSONObject;

import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.*;
import org.json.simple.parser.JSONParser;

import static View.EV3GUI.*;

public class Menu implements MqttCallback {
    MqttClient client;
    JSONParser parser = new JSONParser();
    int ev3count = 0; // ev3 and initialized ports
    GridPane configGrid = new GridPane();
    Button refresh = new Button("⟲");
    Text title = new Text("Menu");
    Line line = new Line();
    public static List<Button> ev3ButtonList = new ArrayList<>();

    public Menu() throws MqttException {
        client = new MqttClient("tcp://localhost:1883", "menu");
        client.connect();
        client.setCallback(this);
        client.subscribe("hello", 2);
        client.subscribe("motor/state/running", 2);
        client.subscribe("motor/state/overloaded", 2);
        client.subscribe("motor/state/holding", 2);
        client.subscribe("motor/state/stalled", 2);

        configGrid.setPadding(new Insets(10, 10, 10, 10));
        configGrid.setMinSize(200, 9999);
        configGrid.setMaxSize(200, 9999);
        configGrid.setVgap(5);
        configGrid.setHgap(5);


        configGrid.add(title, 0, 0);

        configGrid.add(refresh, 0, 1);
        refresh();

        configGrid.add(line, 0, 0);

        configGrid.setStyle("-fx-background-color: GREY;");

    }

    public GridPane getPanel() {
        return configGrid;
    }

    private void refresh() {
        refresh.setOnMouseClicked(press -> {
            ev3count = 2;
            configGrid.getChildren().removeAll(ev3ButtonList);
            ev3ButtonList.clear();
            JSONObject obj2 = new JSONObject();
            obj2.put("scan","ping");
            MqttMessage message2 = new MqttMessage();
            message2.setPayload(obj2.toJSONString().getBytes());
            try {
                client.publish("scan", message2);
                System.out.println(message2);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    int i = 0;
    private boolean isPresent = false;

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        isPresent = false;
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(message.toString());
            String ev3name = jsonObject.get("name").toString();
            String motors = null;
            String sensors = null;
            if (jsonObject.get("motors") != null) {
                motors = jsonObject.get("motors").toString();
            }
            if (jsonObject.get("sensors") != null) {
                sensors = jsonObject.get("sensors").toString();
            }
            System.out.println("EV3 added: " + ev3name);
            System.out.println(ev3name + " Motors: " + motors);
            System.out.println(ev3name + " Sensors: " + sensors);
            //  GUI UPDATE (javafx thread)
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    for (Button bt : ev3ButtonList) {
                        if (bt.getText().equals(ev3name)) {
                            isPresent = true;
                        }
                    }
                    if (!isPresent) {
                        Button newEv3 = new Button(ev3name);
                        Ev3 ev3 = new Ev3(ev3name);
                        EV3GUI.Ev3List.add(ev3);
                        ev3.setColor(EV3GUI.colors.get(0));
                        newEv3.setStyle("-fx-background-color: " + ev3.getColor() + "; -fx-border-color: GREY");
                        newEv3.setOnMousePressed(e -> {
                            newEv3.setStyle("-fx-background-color: " + ev3.nextColor() + "; -fx-border-color: GREY");
                            for (ControlPanel pn : panels) {
                                if (pn != null) {
                                    if (pn.getComponent().getEv3() == ev3) {
                                        if (pn.getEv3Name() != null) {
                                            pn.getEv3Name().setStyle("-fx-background-color: " + ev3.getColor() + "; -fx-text-fill: " + colors.get(colors.indexOf(ev3.getColor()) + 1) + "; -fx-border-color: GREY");
                                            //pn.getEv3Name().setBackground(new Background(newEv3.getBackground().getFills().get(0)));
                                        }
                                    }
                                }
                            }
                            i++;

                        });
                        ev3ButtonList.add(newEv3);
                        Button addSensor = new ButtonAddSensor(ev3);
                        Button addMotor = new ButtonAddMotor(ev3);
                        ev3ButtonList.add(addSensor);
                        ev3ButtonList.add(addMotor);
                        configGrid.add(newEv3, 0, 4 + ev3count);
                        configGrid.add(addMotor, 1, 4 + ev3count);
                        configGrid.add(addSensor, 2, 4 + ev3count);
                    }
                }
            });
            ev3count++;
        } catch (Exception pe) {
            System.out.println("state update:" + message.getPayload());
            pe.printStackTrace();
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub

    }
}
