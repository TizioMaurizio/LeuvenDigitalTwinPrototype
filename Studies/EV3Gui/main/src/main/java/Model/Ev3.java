package Model;

import View.EV3GUI;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

public class Ev3 {
    public String getName() {
        return Name;
    }

    String Name;

    public void setColor(String color) {
        Color = color;
    }

    public String getColor() {
        return Color;
    }

    public String nextColor(){
        i++;
        Color = EV3GUI.colors.get(i);
        return Color;
    }

    public String jsonGet(String key){
        if(jsonObject.get(key)!=null)
            return jsonObject.get(key).toString();
        else return null;
    }

    public void jsonPut(String key, String value){
        jsonObject.put(key, value);
    }

    JSONParser parser = new JSONParser();

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    JSONObject jsonObject = new JSONObject();
    String Color;
    Map<String, Component> componentFromName = new HashMap<>();

    private int i=0;

    public Ev3(String name){
        Name = name;
    }

    public void add(Component component){
        componentFromName.put(component.getName(), component);
    }

    public Component get(String search){
        return componentFromName.get(search);
    }

    public void remove(String search){
        componentFromName.remove(search);
    }
}
