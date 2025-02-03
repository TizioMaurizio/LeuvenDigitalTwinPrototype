import View.EV3GUI;
import javafx.application.*;
import javafx.stage.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;

import static java.lang.Thread.sleep;


public class Main extends Application{
    //TODO AdrenalinaButton extends Button, attributo CHOICE

    //TODO dividere le classi in GUIModel (output) e GUIController (input)
    public EV3GUI ev3;

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException, MalformedURLException, MqttException, InterruptedException, InvalidKeyException {
        boolean connected = false;
        while(!connected) {
            try {
                MqttDefaultFilePersistence a = new MqttDefaultFilePersistence("C:\\Users\\Maurizio\\Documents\\Ev3GuiV2\\out\\artifacts\\main\\MqttPersistence");
                ev3 = new EV3GUI(); //initializes launcher stage, from which you can browse various menus or start the game stage
                connected = true;
            } catch (MqttException e) {
                System.out.println("Mqtt broker not found on localhost, retrying in 3 seconds...");
                sleep(3000);
            }
        }
    }

}
