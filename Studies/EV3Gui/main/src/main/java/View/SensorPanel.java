package View;

import Model.Component;
import View.ControlPanel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Region;
import org.json.simple.JSONObject;

import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.*;
import org.json.simple.parser.JSONParser;

import static java.lang.System.currentTimeMillis;

public class SensorPanel extends ControlPanel implements MqttCallback{

    private Button colorBox = new Button();
    private Button color = new Button();

    public SensorPanel(Component newComponent){

        component = newComponent;
        dragBar();
        closePanel();
        ev3Name();
        componentName();
        name();
        colorBox();
        color();
        colorHistory();

        try {
            client2 = new MqttClient("tcp://localhost:1883", component.getName()+component.getEv3());
            client2.connect();
            client2.setCallback(this);
            client2.subscribe("sensor/on_demand/answer", 2);
            client2.subscribe("sensor/on_change", 2);
            client2.subscribe("time0_init", 2);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // MotorPanel functions

    public List<Region> getButtons(){
        return buttons;
    }

    // panel parts methods

    private void colorBox() {
        colorBox.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight());
        colorBox.setPrefWidth(width);
        colorBox.setPrefHeight(60);
        colorBox.setStyle("-fx-background-color: WHITE; -fx-border-color: GREY");
        colorBox.setOnMousePressed(press -> {
            //
        });
        buttons.add(colorBox);
    }

    Button col = new Button();
    List<Button> colList = new ArrayList<>();
    int colorNum = 20;
    int timeSpan = 2000;

    private void colorHistory(){
        double xOffset = 0;
        for(int i = 0; i < colorNum; i++){
            col = new Button();
            col.setPrefWidth(width / colorNum);
            col.setMinWidth(width / colorNum);
            col.setMaxWidth(width / colorNum);
            col.setTranslateY(dragBar.getPrefHeight() + ev3Name.getPrefHeight() + colorBox.getPrefHeight());
            col.setTranslateX(xOffset);
            xOffset += col.getPrefWidth();
            col.setStyle("-fx-background-color: WHITE; -fx-border-color: GREY");
            colList.add(col);
            buttons.add(col);
        }
        colorHistoryLoop();
        System.out.println(component.getName() + " color refresh every " + (double) sleepTime / 1000 + " seconds");
    }

    private int sleepTime = timeSpan / colorNum;
    Button prevBt = null;
    int index = 0;
    int counter = 0;
    private void colorHistoryLoop() {

        // TODO PESANTISSIMO? preleva in maniera asincrona da color

        // TODO continua a andare se si chiude il pannello?
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                counter++;
                for (Button bt : colList) {
                    index = colList.size() - 1 - colList.indexOf(bt);
                    if (index == 0) {   // first element
                        colList.get(index).setText(Integer.toString(counter));
                        colList.get(index).setStyle(color.getStyle());
                    } else {
                        colList.get(index).setStyle(colList.get(index-1).getStyle());
                    }
                    prevBt = bt;
                }
                // System.out.println(component.getName() + " update");
                colorHistoryLoop();
            }
        });
        new Thread(sleeper).start();
    }

    private void color(){
        color.setPrefWidth(colorBox.getPrefWidth() - 30);
        color.setPrefHeight(colorBox.getPrefHeight() - 20);
        color.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight() + colorBox.getPrefHeight()/2 - color.getPrefHeight()/2 );
        color.setTranslateX(colorBox.getPrefWidth()/2 - color.getPrefWidth()/2 );
        color.setStyle("-fx-background-color: BLACK; -fx-border-color: GREY");
        color.setOnMousePressed(e ->{
            instant = currentTimeMillis();
            if ((instant - previnstant <= 500)) {
                System.out.println("double clicked color " + component.getName());
            } else previnstant = instant;
        });
        buttons.add(color);
    }
    int i=0;
    /*private void slider() {
        slider.setOnMousePressed(t -> {
            dragDelta.x = slider.getLayoutX() - t.getSceneX();
        });

        slider.setOnMouseDragged(t -> {
            double result = t.getSceneX() + dragDelta.x;
            if((result > (colorBox.getLayoutX() + 5))||(result < (colorBox.getLayoutX() + colorBox.getPrefWidth() - 10)))
                slider.setTranslateX(result);
        });
    }*/

    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message){
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(message.toString());
            String received = jsonObject.get("value").toString();
            //if (topic.equals("sensor/on_change")) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        color.setStyle("-fx-background-color: " + received + "; ");
                    }
                });

            System.out.println("Seeing color: " + received);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub

    }
}
