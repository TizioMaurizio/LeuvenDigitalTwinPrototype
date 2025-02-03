package View;

import Controller.MqttButton;
import Model.Component;
import Model.Ev3;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.*;

import static View.EV3GUI.*;

public class ButtonAddSensor extends MqttButton {
    MqttClient client2;
    GridPane grid = new GridPane();
    Scene addSensor = new Scene(grid, 230, 180);
    Button confirm = new Button("Confirm");
    Label sensor = new Label("Configure\nnew sensor");
    Label portText = new Label("EV3 Port: \n(in1,2,3,4)");
    Label sensorText = new Label("Sensor type: \n(COL-COLOR\n /TOUCH)");
    String port;
    String type;

    public ButtonAddSensor(Ev3 ev3){
        ButtonAddSensor buttonAddSensor = this;
        this.setText("+Sensor");

        this.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                GridPane secondaryLayout = new GridPane();

                Scene secondScene = new Scene(secondaryLayout, 300, 200);

                // New window (Stage)
                Stage newWindow = new Stage();
                newWindow.setTitle("New Sensor");
                newWindow.setScene(secondScene);

                // Specifies the modality for new window.
                newWindow.initModality(Modality.WINDOW_MODAL);

                Stage primaryStage = (Stage) buttonAddSensor.getScene().getWindow();
                // Specifies the owner Window (parent) for new window
                newWindow.initOwner(primaryStage);

                // Set position of second window, related to primary window.
                newWindow.setX(primaryStage.getX() + 200);
                newWindow.setY(primaryStage.getY() + 100);

                newWindow.show();

                sensor.setStyle("-fx-text-fill: BLUE");

                secondaryLayout.add(sensor,0,0);
                secondaryLayout.add(portText,0,2);
                secondaryLayout.add(sensorText,4,2);
                int in = 1;
                int line = 2;
                for(int l = 0; l < 4; l++) {
                    Button bt = new Button("in" + Integer.toString(in++));
                    secondaryLayout.add(bt, 1, 2 + l);
                    line += l;
                    bt.setOnMousePressed(e ->{
                        port = bt.getText();
                        bt.setStyle("-fx-background-color: BLUE;");
                    });
                }
                Button color = new Button("COL-COLOR");
                color.setOnMousePressed(e ->{
                    type = color.getText();
                    color.setStyle("-fx-background-color: BLUE;");
                });
                secondaryLayout.add(color,5,2);
                Button touch = new Button("TOUCH");
                touch.setOnMousePressed(e ->{
                    type = touch.getText();
                    touch.setStyle("-fx-background-color: BLUE;");
                });
                secondaryLayout.add(touch,5,3);
                secondaryLayout.add(confirm,0,line+= 2);

                confirm.setOnMousePressed(e -> {
                    try {
                        Component newComponent = new Component(ev3, port, type);
                        //JSONObject obj2 = new JSONObject();
                        //obj2.put("ev3", ev3.getName());
                        //obj2.put(Integer.toString(k), port.getCharacters().toString());
                        //int k = Integer.parseInt(port.getCharacters().toString());
                        //obj2.put(Integer.toString(k+1), type.getCharacters().toString());

                        for (Ev3 ev: Ev3List) {
                            if(ev.getName().equals(ev3.getName())){
                                ev.jsonPut("ev3", ev3.getName());
                                ev.jsonPut(Integer.toString(newComponent.getPort()), port);
                            }
                        }
                        int i=0;
                        for(i=1;i<13;i++){  // fill the json with None values for the EV3 python function
                            if(ev3.jsonGet(Integer.toString(i))!=null){
                                //obj2.put(Integer.toString(i), obj2.get(Integer.toString(i)));
                            }
                            else
                                ev3.jsonPut(Integer.toString(i), null);
                            //obj2.put(Integer.toString(i), null);
                        }

                        System.out.println(ev3.getJsonObject());
                        MqttMessage message2 = new MqttMessage();
                        message2.setPayload(ev3.getJsonObject().toJSONString().getBytes());
                        GuiClient.publish("ev3_config", message2);
                        SensorPanel newPanel = new SensorPanel(newComponent);
                        arena.getChildren().addAll((newPanel.getButtons()));
                        EV3GUI.panels.add(newPanel);
                        newPanel.getEv3Name().setStyle("-fx-background-color: " + ev3.getColor() + "; ");
                        newPanel.setLayout(400 + EV3GUI.panels.indexOf(newPanel) * 10,200 + EV3GUI.panels.indexOf(newPanel) * 10);
                        newWindow.close();
                    } catch(Exception q){
                        //q.printStackTrace();
                        System.out.println("ERROR: Invalid configuration by catching GENERIC exception (printStackTrace to debug)");
                        Label error = new Label("Invalid configuration\nRetry or exit");
                        secondaryLayout.add(error,1,0);
                        error.setStyle("-fx-text-fill: RED");
                    }
                });
            }
        });
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
