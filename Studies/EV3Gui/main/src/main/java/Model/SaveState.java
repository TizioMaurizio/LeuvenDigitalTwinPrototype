package Model;

import java.io.FileWriter;
import java.io.IOException;

public class SaveState {
    public static void Load(){

    }

    public static void Save(){
        try {
            FileWriter writer = new FileWriter("C:/Users/Maurizio/Documents/Ev3GuiV2/GUIState.txt");
            writer.write("Line1");
            writer.write("\nLine2");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
