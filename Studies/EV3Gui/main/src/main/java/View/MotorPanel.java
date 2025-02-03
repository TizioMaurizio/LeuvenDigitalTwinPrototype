package View;

import Controller.MinimizeButton;
import Model.Component;
import View.ControlPanel;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
//import javafx.scene.robot.Robot;
import javafx.scene.input.MouseButton;
import org.json.simple.JSONObject;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class MotorPanel extends ControlPanel implements MqttCallback{

    private Button runForever = new Button("speed");
    private TextField motorSpeed = new TextField();
    private Button relRotate = new Button("angle\n\nsecs");
    private TextField relAngle = new TextField();
    private TextField relTime = new TextField();
    //private Button slider = new Button();
    private MinimizeButton minimize = new MinimizeButton(runForever, buttons);
    private MinimizeButton minimizeRotate = new MinimizeButton(relRotate, buttons);

    public MotorPanel(Component newComponent){

        component = newComponent;
        dragBar();
        minimize();
        minimizeRotate();
        closePanel();
        stop();
        ev3Name();
        componentName();
        name();
        runForever();
        relRotate();
        relAngle();
        relTime();
        //slider();
        motorSpeed();

        try {
            client2 = new MqttClient("tcp://localhost:1883", component.getName()+component.getEv3());
            client2.connect();
            client2.setCallback(this);
            client2.subscribe("motor/action/stop", 2);
            client2.subscribe("motor/action/rel", 2);
            client2.subscribe("motor/action/rel1verse", 2);
            client2.subscribe("motor/action/forever", 2);
            client2.subscribe("motor/action/timed", 2);
            client2.subscribe("motor/action/abs", 2);
            client2.subscribe("time0_init", 2);
            client2.subscribe("motor/action/stop", 2);
            //  client2.subscribe("ev3_config", 2);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // MotorPanel functions


    // panel parts methods
    private void minimize(){
        buttons.add(minimize);
    }

    private void minimizeRotate(){
        minimizeRotate.setTranslateX(minimize.getPrefWidth());
        buttons.add(minimizeRotate);
    }


    private void runForever() {
        runForever.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight());
        runForever.setPrefWidth(width);
        runForever.setPrefHeight(60);
        runForever.setStyle("-fx-alignment: center-left; -fx-background-color: WHITE; -fx-border-color: GREY ");
        buttons.add(runForever);
        runForever.setOnMousePressed(press -> {
            instant = currentTimeMillis();
            if ((instant - previnstant <= 500)) {
                ////
                sendValue(motorSpeed);
                ////////////////
                previnstant = 0;
            } else previnstant = instant;
        });

        /*runForever.setOnMouseReleased(e -> {
            runForever.setStyle("-fx-background-color: GREY; ");
            runForever.setText("Run");
        });*/
    }

    private void relRotate(){
        relRotate.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight()+ runForever.getPrefHeight());
        relRotate.setPrefWidth(width);
        relRotate.setPrefHeight(60);
        relRotate.setStyle("-fx-alignment: center-left; -fx-background-color: WHITE; -fx-border-color: GREY");
        buttons.add(relRotate);
        rotateHistory();
    }

    public ArrayList<String> stringToArray(String string){
        ArrayList<String> array = new ArrayList<>();
        boolean finish = false;
        StringBuilder sb = new StringBuilder();
        for (char c: string.toCharArray()) {
            if(c != ',')
                sb.append(c);
            else {
                array.add(sb.toString());
                sb = new StringBuilder();
            }
        }
        array.add(sb.toString());
        return array;
    }

    Button bt = new Button("400,2");
    Button bt2 = new Button("500,");
    Button bt3 = new Button(",");
    Button bt4 = new Button(",");

    private void rotateHistory(){
        //Robot robot = new Robot();
        for(int i = 0; i < 4; i++){

        }


        buttons.add(bt);
        bt.setOnMousePressed(e -> {
            ArrayList<String> values = stringToArray(bt.getText());
            relAngle.setText(values.get(0));
            relTime.setText(values.get(1));
            relAngle.fireEvent(e);
            //robot.keyPress(KeyCode.ALT);
            //robot.keyRelease(KeyCode.ALT);
            sendRepeat(relAngle);
        });
        bt.setOnKeyPressed(e -> {
            if(e.getCode()== KeyCode.ENTER)
                bt.setText(relAngle.getText()+","+relTime.getText());
        });
        bt.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight()+ runForever.getPrefHeight() + 2);
        bt.setTranslateX(relRotate.getPrefWidth()/2);

        buttons.add(bt2);
        bt2.setOnMousePressed(e -> {
            ArrayList<String> values = stringToArray(bt2.getText());
            relAngle.setText(values.get(0));
            relTime.setText(values.get(1));
            relAngle.fireEvent(e);
            //robot.keyPress(KeyCode.ALT);
            //robot.keyRelease(KeyCode.ALT);
            sendRepeat(relAngle);
        });
        bt2.setOnKeyPressed(e -> {
            if(e.getCode()== KeyCode.ENTER)
                bt2.setText(bt.getText());
        });
        bt2.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight()+ runForever.getPrefHeight() + 2);
        bt2.setTranslateX(relRotate.getPrefWidth()/2 + relRotate.getPrefWidth()/4);

        buttons.add(bt3);
        bt3.setOnMousePressed(e -> {
            ArrayList<String> values = stringToArray(bt3.getText());
            relAngle.setText(values.get(0));
            relTime.setText(values.get(1));
            relAngle.fireEvent(e);
            //robot.keyPress(KeyCode.ALT);
            //robot.keyRelease(KeyCode.ALT);
            sendRepeat(relAngle);
        });
        bt3.setOnKeyPressed(e -> {
            if(e.getCode()== KeyCode.ENTER)
                bt3.setText(bt2.getText());
        });
        bt3.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight()+ runForever.getPrefHeight() + relRotate.getPrefHeight()/2+ 2);
        bt3.setTranslateX(relRotate.getPrefWidth()/2);
        buttons.add(bt4);

        bt4.setOnMousePressed(e -> {
            ArrayList<String> values = stringToArray(bt4.getText());
            relAngle.setText(values.get(0));
            relTime.setText(values.get(1));
            relAngle.fireEvent(e);
            //robot.keyPress(KeyCode.ALT);
            //robot.keyRelease(KeyCode.ALT);
            sendRepeat(relAngle);
        });
        bt4.setOnKeyPressed(e -> {
            if(e.getCode()== KeyCode.ENTER)
                bt4.setText(bt3.getText());
        });
        bt4.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight()+ runForever.getPrefHeight() + relRotate.getPrefHeight()/2+ 2);
        bt4.setTranslateX(relRotate.getPrefWidth()/2 + relRotate.getPrefWidth()/4);
    }

    Button bt5 = new Button("200");
    Button bt6 = new Button("500");
    Button bt7 = new Button("-300");
    Button bt8 = new Button("");

    private void speedHistory(){
        //Robot robot = new Robot();
        buttons.add(bt5);
        bt5.setOnMousePressed(e -> {
            ArrayList<String> values = stringToArray(bt5.getText());
            motorSpeed.setText(values.get(0));
            motorSpeed.fireEvent(e);
            //robot.keyPress(KeyCode.ALT);
            //robot.keyRelease(KeyCode.ALT);
            sendValue(motorSpeed);
        });
        bt5.setOnKeyPressed(e -> {
            if(e.getCode()== KeyCode.ENTER)
                bt5.setText(motorSpeed.getText());
        });
        bt5.setOnMouseReleased(e -> {
            if(e.getButton() == MouseButton.SECONDARY) {
                motorSpeed.setText("0");
                sendValue(motorSpeed);
            }
        });
        bt5.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight() + 2);
        bt5.setTranslateX(relRotate.getPrefWidth()/2);

        buttons.add(bt6);
        bt6.setOnMousePressed(e -> {
            ArrayList<String> values = stringToArray(bt6.getText());
            motorSpeed.setText(values.get(0));
            motorSpeed.fireEvent(e);
            //robot.keyPress(KeyCode.ALT);
            //robot.keyRelease(KeyCode.ALT);
            sendValue(motorSpeed);
        });
        bt6.setOnKeyPressed(e -> {
            if(e.getCode()== KeyCode.ENTER)
                bt6.setText(bt5.getText());
        });
        bt6.setOnMouseReleased(e -> {
            if(e.getButton() == MouseButton.SECONDARY) {
                motorSpeed.setText("0");
                sendValue(motorSpeed);
            }
        });
        bt6.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight() + 2);
        bt6.setTranslateX(relRotate.getPrefWidth()/2 + relRotate.getPrefWidth()/4);

        buttons.add(bt7);
        bt7.setOnMousePressed(e -> {
            ArrayList<String> values = stringToArray(bt7.getText());
            motorSpeed.setText(values.get(0));
            motorSpeed.fireEvent(e);
            //robot.keyPress(KeyCode.ALT);
            //robot.keyRelease(KeyCode.ALT);
            sendValue(motorSpeed);
        });
        bt7.setOnMouseReleased(e -> {
            if(e.getButton() == MouseButton.SECONDARY) {
                motorSpeed.setText("0");
                sendValue(motorSpeed);
            }
        });
        bt7.setOnKeyPressed(e -> {
            if(e.getCode()== KeyCode.ENTER)
                bt7.setText(bt6.getText());
        });
        bt7.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight()+ runForever.getPrefHeight()/2 + 2);
        bt7.setTranslateX(relRotate.getPrefWidth()/2);

        buttons.add(bt8);
        bt8.setOnMousePressed(e -> {
            ArrayList<String> values = stringToArray(bt8.getText());
            motorSpeed.setText(values.get(0));
            motorSpeed.fireEvent(e);
            //robot.keyPress(KeyCode.ALT);
            //robot.keyRelease(KeyCode.ALT);
            sendValue(motorSpeed);
        });
        bt8.setOnMouseReleased(e -> {
            if(e.getButton() == MouseButton.SECONDARY) {
                motorSpeed.setText("0");
                sendValue(motorSpeed);
            }
        });
        bt8.setOnKeyPressed(e -> {
            if(e.getCode()== KeyCode.ENTER)
                bt8.setText(bt7.getText());
        });
        bt8.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight()+ runForever.getPrefHeight()/2 + 2);
        bt8.setTranslateX(relRotate.getPrefWidth()/2 + relRotate.getPrefWidth()/4);
    }

    /*private void slider(){
        slider.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight() + 15);
        slider.setTranslateX(10);
        slider.setPrefWidth(10);
        slider.setMinWidth(10);
        slider.setMaxWidth(10);
        slider.setPrefWidth(15);
        slider.setMinHeight(15);
        slider.setMaxHeight(15);
        slider.setOnMousePressed(t -> {
            dragDelta.x = slider.getLayoutX() - t.getSceneX();
        });

        slider.setOnMouseDragged(t -> {
            double result = t.getSceneX() + dragDelta.x;
            if((result > (runForever.getLayoutX() + 5))||(result < (runForever.getLayoutX() + runForever.getPrefWidth() - 10)))
                slider.setTranslateX(result);
        });
        //buttons.add(slider);
    }*/

    private void motorSpeed(){
        motorSpeed.setId("forever");
        motorSpeed.setStyle("-fx-background-color: WHITE; -fx-border-color: GREY");
        motorSpeed.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight() + 19 );
        motorSpeed.setTranslateX(40);
        motorSpeed.setPrefWidth(runForever.getPrefWidth()/4);
        buttons.add(motorSpeed);
        motorSpeed.setOnKeyPressed(e ->{
            if(e.getCode()== KeyCode.ENTER || e.getCode()== KeyCode.ALT){
                bt8.fireEvent(e);
                bt7.fireEvent(e);
                bt6.fireEvent(e);
                bt5.fireEvent(e);
                //sendValue(motorSpeed);
            }
        });
        speedHistory();
    }

    private void relAngle(){
        relAngle.setId("rel");
        relAngle.setStyle("-fx-background-color: WHITE; -fx-border-color: GREY");
        relAngle.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight() + runForever.getPrefHeight() +2);
        relAngle.setTranslateX(40);
        relAngle.setPrefWidth(relRotate.getPrefWidth()/4);
        buttons.add(relAngle);
        relAngle.setOnKeyPressed(e ->{
            if(e.getCode()== KeyCode.ENTER || e.getCode()== KeyCode.ALT){
                bt4.fireEvent(e);
                bt3.fireEvent(e);
                bt2.fireEvent(e);
                bt.fireEvent(e);
                relAngle.setId("rel");
                /*sendValue(relAngle);
                Task<Void> sleeper = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            System.out.println("retract in " + Integer.parseInt(relTime.getText()) + " seconds");
                            Thread.sleep(Integer.parseInt(relTime.getText())*1000);
                        } catch (InterruptedException e) {
                            // e.printStackTrace();
                            System.out.println("Relative rotation on " + component.getName() + "" +
                                    " cancelled");
                        }
                        return null;
                    }
                };
                activeCommands.add(sleeper);

                sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        int value = Integer.parseInt(relAngle.getText().toString());
                        value = -value;
                        relAngle.setText(Integer.toString(value));
                        sendValue(relAngle);
                        relAngle.setText(Integer.toString(-value));
                    }
                });
                new Thread(sleeper).start();*/
            }
        });
    }

    private void relTime() {
        relTime.setId("rel");
        relTime.setStyle("-fx-background-color: WHITE; -fx-border-color: GREY");
        relTime.setTranslateY(ev3Name.getPrefHeight() + dragBar.getPrefHeight() + runForever.getPrefHeight() + relRotate.getPrefHeight()/2 + 2);
        relTime.setTranslateX(40);
        relTime.setPrefWidth(relRotate.getPrefWidth() / 4);
        buttons.add(relTime);
        relTime.setOnKeyPressed(e ->{
            relAngle.fireEvent(e);
        });
    }


    int i=0;

    private void sendValue(TextField field){
        JSONObject obj2 = new JSONObject();

        obj2.put("ev3", component.getEv3().getName());
        obj2.put("motor", component.getName());
        if(field.getId().equals("rel")){
            try {
                List<String> test = new ArrayList<>();
                test.add(field.getText());
                obj2.put("value", test.get(0));
            } catch (NumberFormatException e) {
                obj2.put("value", 0);
            }
        }
        else {
            try {
                obj2.put("value", Integer.parseInt(field.getCharacters().toString()));
            } catch (NumberFormatException e) {
                obj2.put("value", 0);
            }
        }

        System.out.println(component.getName() + " command '" + field.getId() + "': " +obj2);
        MqttMessage message2 = new MqttMessage();
        message2.setPayload(obj2.toJSONString().getBytes());
        try {
            client2.publish("motor/action/" + field.getId(), message2);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void sendRepeat(TextField field){
        sendValue(field);
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    System.out.println("retract in " + Integer.parseInt(relTime.getText()) + " seconds");
                    Thread.sleep(Integer.parseInt(relTime.getText())*1000);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                    System.out.println("Relative rotation on " + component.getName() + "" +
                            " cancelled");
                }
                return null;
            }
        };
        activeCommands.add(sleeper);

        sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                int value = Integer.parseInt(field.getText().toString());
                value = -value;
                field.setText(Integer.toString(value));
                sendValue(field);
                field.setText(Integer.toString(-value));
            }
        });
        new Thread(sleeper).start();
    }
    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub

    }
}
