package Model;

import Controller.MqttButton;

import java.security.InvalidKeyException;
import java.util.InvalidPropertiesFormatException;

//  Represents a Motor or a Sensor
public class Component {
    public String getName() {
        return name;
    }

    public Ev3 getEv3(){
        return ev3;
    }
    public String getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public int getPort() {
        return port;
    }

    public MqttButton getPanel() {
        return panel;
    }

    public void setPanel(MqttButton panel) {
        this.panel = panel;
    }

    private String name;
    private Ev3 ev3;
    private String kind;
    private String type;

    private MqttButton panel = new MqttButton();

    private void setPort() throws InvalidKeyException{
        switch(name){
            case("outA"):
                this.port=1;
                break;
            case("outB"):
                this.port=3;
                break;
            case("outC"):
                this.port=5;
                break;
            case("outD"):
                this.port=7;
                break;
            case("in1"):
                this.port=9;
                break;
            case("in2"):
                this.port=10;
                break;
            case("in3"):
                this.port=11;
                break;
            case("in4"):
                this.port=12;
                break;
            default:

        }
    }

    private int port; // (position in ev3 initialize array, 1-3-5-7 for motors, 9-10-11-12 for sensors)

    public Component(Ev3 newEv3, String newName, String newType) throws InvalidKeyException{
        ev3 = newEv3;
        name = newName;
        type = newType;
        setPort();
    }

    public static int parsePort(String port){
        try {
            //  port assegnato dagli switch dei ButtonAdd
            return Integer.parseInt(port.toString());
        } catch(NumberFormatException q){
            return -1;
        }
    }

}
