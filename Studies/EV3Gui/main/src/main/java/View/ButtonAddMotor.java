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

public class ButtonAddMotor extends MqttButton {

    private String port;
    private String type;

    public ButtonAddMotor(Ev3 ev3){
        ButtonAddMotor buttonAddMotor = this;
        this.setText("+Motor");
        this.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                GridPane secondaryLayout = new GridPane();

                Scene secondScene = new Scene(secondaryLayout, 300, 200);

                // New window (Stage)
                Stage newWindow = new Stage();
                newWindow.setTitle("New Motor");
                newWindow.setScene(secondScene);

                // Specifies the modality for new window.
                newWindow.initModality(Modality.WINDOW_MODAL);

                Stage primaryStage = (Stage) buttonAddMotor.getScene().getWindow();
                // Specifies the owner Window (parent) for new window
                newWindow.initOwner(primaryStage);

                // Set position of second window, related to primary window.
                newWindow.setX(primaryStage.getX() + 200);
                newWindow.setY(primaryStage.getY() + 100);

                newWindow.show();

                Label motor = new Label("Configure\nnew motor");
                motor.setStyle("-fx-text-fill: GREEN");
                Label portText = new Label("EV3 Port: \n(outA,B,C,D)");
                Label motorText = new Label("Motor type: \n(Large/Medium)");
                /*TextField port = new TextField();
                port.setPrefWidth(100);
                TextField type = new TextField();
                type.setPrefWidth(100);*/
                Button confirm = new Button("Confirm");

                secondaryLayout.add(motor,0,0);
                secondaryLayout.add(portText,0,2);
                secondaryLayout.add(motorText,4,2);
                char letter = 'A';
                int line = 2;
                for(int l = 0; l < 4; l++) {
                    Button bt = new Button("out" + Character.toString(letter++));
                    secondaryLayout.add(bt, 1, 2 + l);
                    line += l;
                    bt.setOnMousePressed(e ->{
                        port = bt.getText();
                        bt.setStyle("-fx-background-color: GREEN;");
                    });
                }
                Button large = new Button("Large");
                large.setOnMousePressed(e ->{
                    type = large.getText();
                    large.setStyle("-fx-background-color: GREEN;");
                });
                secondaryLayout.add(large,5,2);
                Button medium = new Button("Medium");
                medium.setOnMousePressed(e ->{
                    type = medium.getText();
                    medium.setStyle("-fx-background-color: GREEN;");
                });
                secondaryLayout.add(medium,5,3);
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
                                ev.jsonPut(Integer.toString(newComponent.getPort()+1), type);
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
                        ControlPanel newPanel = new MotorPanel(newComponent);
                        arena.getChildren().addAll((newPanel.getButtons()));
                        EV3GUI.panels.add(newPanel);
                        newPanel.getEv3Name().setStyle("-fx-background-color: " + ev3.getColor() + "; -fx-border-color: GREY");
                        newPanel.setLayout(400 + EV3GUI.panels.indexOf(newPanel) * 10,200 + EV3GUI.panels.indexOf(newPanel) * 10);
                        newWindow.close();
                    } catch(Exception q){
                        q.printStackTrace();
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
