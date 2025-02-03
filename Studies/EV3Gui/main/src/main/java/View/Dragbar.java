package View;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;

import java.util.List;

public class Dragbar extends Button {

    final Dragbar.Delta dragDelta = new Dragbar.Delta();

    class Delta {
        double x, y;
    }

    public Dragbar(List<Region> linkedNodes) {
        // dragBar functions
        this.setOnMousePressed(t -> {
            dragDelta.x = this.getLayoutX() - t.getSceneX();
            dragDelta.y = this.getLayoutY() - t.getSceneY();
        });
        this.setOnMouseDragged(t -> {
                for (Region bt : linkedNodes) {
                    bt.setLayoutX(t.getSceneX() + dragDelta.x);
                    bt.setLayoutY(t.getSceneY() + dragDelta.y);
                }
        });
        this.setCursor(Cursor.OPEN_HAND);
    }
}
