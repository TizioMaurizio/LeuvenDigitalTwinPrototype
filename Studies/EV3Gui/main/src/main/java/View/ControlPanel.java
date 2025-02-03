package View;

import Model.Component;
import View.ButtonCompName;
import View.ButtonEv3Name;
import View.Dragbar;
import View.EV3GUI;
import Model.Ev3;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class ControlPanel {
    public Component getComponent() {
        return component;
    }

    public Button getEv3Name() {
        return ev3Name;
    }

    public List<Region> getButtons(){
        return buttons;
    }

    protected void componentName(){
        buttonCompName.setTranslateY(dragBar.getPrefHeight());
        buttonCompName.setTranslateX(ev3Name.getPrefWidth());
        buttonCompName.setPrefWidth(width - ev3Name.getPrefWidth());
        buttonCompName.setPrefHeight(ev3Name.getPrefHeight());
        buttonCompName.setText("\n" + component.getType());
        buttonCompName.setStyle("-fx-background-color: WHITE; -fx-border-color: GREY");
        buttons.add(buttonCompName);
    }

    protected void closePanel(){
        closePanel.setPrefWidth(20);
        closePanel.setPrefHeight(20);
        closePanel.setMinHeight(20);
        closePanel.setMaxHeight(20);
        closePanel.setTranslateX(dragBar.getPrefWidth() - 19);
        closePanel.setStyle("-fx-background-color: BLACK; -fx-border-color: GREY");
        closePanel.setOnMousePressed(press ->{
            instant = currentTimeMillis();
            if ((instant - previnstant <= 500)) {
                stop.fireEvent(press);
                EV3GUI.panels.remove(this);
                EV3GUI.arena.getChildren().removeAll(buttons);
                previnstant = 0;
                // update JSON
                Ev3 ev3 = component.getEv3();
                ev3.jsonPut(Integer.toString(component.getPort()), null);
                ev3.jsonPut(Integer.toString(component.getPort() + 1), null);
                MqttMessage message2 = new MqttMessage();
                message2.setPayload(ev3.getJsonObject().toJSONString().getBytes());
                System.out.println(ev3.getJsonObject().toJSONString());
                try {
                    EV3GUI.GuiClient.publish("ev3_config", message2);
                } catch (MqttException q) {
                    q.printStackTrace();
                }
            } else previnstant = instant;
        });
        buttons.add(closePanel);
    }

    protected void stop(){
        stop.setPrefWidth(20);
        stop.setPrefHeight(20);
        stop.setMinHeight(20);
        stop.setMaxHeight(20);
        stop.setTranslateX(dragBar.getPrefWidth() - 40);
        stop.setStyle("-fx-background-color: RED; -fx-border-color: GREY");
        stop.setOnMousePressed(e ->{
            JSONObject obj2 = new JSONObject();
            obj2.put("ev3", component.getEv3().getName());
            obj2.put("motor", component.getName());
            obj2.put("value", "stop");
            MqttMessage message2 = new MqttMessage();
            message2.setPayload(obj2.toJSONString().getBytes());
            System.out.println(obj2.toJSONString());
            try {
                EV3GUI.GuiClient.publish("motor/action/stop", message2);
            } catch (MqttException q) {
                q.printStackTrace();
            }
            // stop scheduled tasks (scheduled Json sends)
            for (Task<Void> task: activeCommands) {
                task.cancel();
            }
            System.out.println(component.getName() + " stopped");
        });
        stop.setCursor(Cursor.CROSSHAIR);
        buttons.add(stop);
    }

    protected void dragBar(){
        dragBar.setPrefWidth(this.getWidth());
        dragBar.setPrefHeight(20);
        dragBar.setMinHeight(20);
        dragBar.setMaxHeight(20);
        dragBar.setStyle("-fx-background-color: WHITE; -fx-border-color: GREY");
        buttons.add(dragBar);
    }

    protected void stopMotor(){
        JSONObject obj2 = new JSONObject();
        obj2.put("ev3", component.getEv3().getName());
        obj2.put("motor", component.getName());
        obj2.put("value", "stop");
        MqttMessage message2 = new MqttMessage();
        message2.setPayload(obj2.toJSONString().getBytes());
        System.out.println(obj2.toJSONString());
        try {
            EV3GUI.GuiClient.publish("motor/action/stop", message2);
        } catch (MqttException q) {
            q.printStackTrace();
        }
        // stop scheduled tasks (scheduled Json sends)
        for (Task<Void> task: activeCommands) {
            task.cancel();
        }
        System.out.println(component.getName() + " stopped");
    }
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setLayout(double x, double y) {
        for (Region bt : buttons) {
            bt.setLayoutX(bt.getLayoutX() + x);
            bt.setLayoutY(bt.getLayoutY() + y);
        }
    }

    protected void name(){
        name.setStyle("-fx-background-color: WHITE; -fx-border-color: GREY");
        name.setText("insert name");
        name.setOnMousePressed(e ->{
            if(name.getText().equals("insert name"))
                name.setText("");
        });
        name.setTranslateY(dragBar.getPrefHeight());
        name.setTranslateX(ev3Name.getPrefWidth());
        name.setPrefWidth(width - ev3Name.getPrefWidth());
        name.setPrefHeight(buttonCompName.getPrefHeight()/2);
        buttons.add(name);
    }

    protected void ev3Name(){
        ev3Name.setTranslateY(dragBar.getPrefHeight());
        ev3Name.setPrefWidth(60);
        ev3Name.setPrefHeight(60);
        ev3Name.setText(component.getEv3().getName() + "\n" + component.getName());
        buttons.add(ev3Name);
    }

    protected List<Task> activeCommands = new ArrayList<>();
    protected MqttClient client2;
    protected TextField name = new TextField();
    protected long instant;
    protected long previnstant=0;
    protected double width = 180;
    protected double height = 200;
    protected Component component;
    protected ButtonCompName buttonCompName = new ButtonCompName();
    protected Button ev3Name = new ButtonEv3Name();
    protected Button closePanel = new Button();
    protected Button stop = new Button();
    protected List<Region> buttons = new ArrayList<>();
    protected Dragbar dragBar = new Dragbar(buttons);
}
