package View;

import Model.Component;
import Model.Ev3;
import Utility.CircularArrayList;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.*;


import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

public class EV3GUI extends Stage{

    public static MqttClient GuiClient;
    public static List<Ev3> Ev3List = new ArrayList<>();
    public static List<ControlPanel> panels = new ArrayList<>();
    public static AnchorPane arena = new AnchorPane();
    public Scene scene1;
    MotorPanel motorPanel;
    SensorPanel sensorPanel;
    Menu menu = new Menu();
    public static List<String> colors = new CircularArrayList<>();

    // button and pane are created
    public EV3GUI() throws MqttException, InvalidKeyException {
        GuiClient = new MqttClient("tcp://localhost:1883", "GuiClient");
        GuiClient.connect();
        Ev3 testEv3 = new Ev3("ev3B");
        Component testMotor = new Component(testEv3, "MotorA", "Large");
        testEv3.add(testMotor);
        motorPanel = new MotorPanel(testEv3.get("MotorA"));
        // sensorPanel = new SensorPanel(testEv3.get("")); TODO impossibile senza ev3 fisico
        initColors();
        arena.getChildren().add(menu.getPanel());
        scene1 = new Scene(arena);
        this.setScene(scene1);
        this.setWidth(1920/2);
        this.setHeight(1080/2);
        this.show();
    }

    public void initColors(){
        colors.add("red");
        colors.add("green");
        colors.add("blue");
        colors.add("yellow");
    }
}
