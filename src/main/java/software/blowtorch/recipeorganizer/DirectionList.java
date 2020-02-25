/*
 * Copyright (C) 2020 vinny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package software.blowtorch.recipeorganizer;

import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

/**
 *
 * @author vinny
 */
public class DirectionList {
    private final Recipe                recipe;
    private final GridPane              directionsDisplay;
    private final ArrayList<TextField>  tf = new ArrayList<>();
    private final ArrayList<Directions> direction;
    private final ArrayList<Button>     insertButton = new ArrayList<>();
    private final ArrayList<Button>     deleteButton = new ArrayList<>();
    private final ArrayList<Button>     moveUpButton = new ArrayList<>();
    private final ArrayList<Button>     moveDownButton = new ArrayList<>();
    private static final int            DELETE = 0;
    private static final int            INSERT_BEFORE = 1;
    private static final int            INSERT_AFTER = 2;
    
    protected DirectionList(GridPane pane, Recipe rec) {
        this.recipe = rec;
        this.directionsDisplay = pane;
        this.direction = rec.getDirections();
        for (int c = 0; c < this.direction.size(); c++) {
            TextField newField = new TextField();
            newField.setText(this.direction.get(c).getDirection());
            newField.setPrefWidth(300);
            tf.add(newField);
       }
    }
    
    protected FlowPane getDirectionsHeader() {
        FlowPane directionHeaderBox = new FlowPane();
        ComboBox actionBox = new ComboBox();
        actionBox.getItems().add("DELETE");
        actionBox.getItems().add("INSERT before");
        actionBox.getItems().add("INSERT after");
        ComboBox valueBox = new ComboBox();
        for (int d = 0; d < this.direction.size(); d++) {
            valueBox.getItems().add(this.direction.get(d).getDirection());
        }
        Button submitButton = new Button("OK");
        directionHeaderBox.getChildren().addAll(actionBox, valueBox, submitButton);
        submitButton.setOnAction(event -> {
            switch (actionBox.getSelectionModel().getSelectedIndex()) {
                case DELETE:
                    directionsDisplay.getChildren().remove(tf.get(valueBox.getSelectionModel().getSelectedIndex()));
                    recipe.removeDirection(direction.get(valueBox.getSelectionModel().getSelectedIndex()));
                    tf.remove(tf.get(valueBox.getSelectionModel().getSelectedIndex()));
                    break;
                case INSERT_BEFORE:
                    int beforeIndex = valueBox.getSelectionModel().getSelectedIndex();
                    tf.add(beforeIndex, new TextField());
                    valueBox.getItems().add(beforeIndex, tf.get(beforeIndex).getText());
                    directionsDisplay.getChildren().clear();
                    directionsDisplay.addRow(0, directionHeaderBox);
                   for (int x = 0; x < tf.size(); x++) {
                        directionsDisplay.addRow(x+2, tf.get(x));
                    }
                    break;
                case INSERT_AFTER:
                    int afterIndex = valueBox.getSelectionModel().getSelectedIndex() + 1;
                    tf.add(afterIndex, new TextField());
                     valueBox.getItems().add(afterIndex, tf.get(afterIndex).getText());
                    directionsDisplay.getChildren().clear();
                    directionsDisplay.addRow(0, directionHeaderBox);
                   for (int x = 0; x < tf.size(); x++) {
                        directionsDisplay.addRow(x+2, tf.get(x));
                    }
                    break;
                default:
                    break;
            }
        });
                
        return directionHeaderBox;
    }
    
    
    protected ArrayList<Directions> getDirections() {
        int numTextFields = this.tf.size();
        int numDirections = this.direction.size();
        
        for (int x = 0; x < numDirections; x++) {
            if (!tf.get(x).getText().equals(this.direction.get(x).getDirection())) {
                Directions d = new Directions(tf.get(x).getText());
                this.direction.remove(x);
                this.direction.add(x, d);
            }
        }
        for (int y = numDirections; y < numTextFields; y++) {
            Directions d = new Directions(tf.get(y).getText());
            this.direction.add(d);
        }

        return this.direction;
    }
    protected ArrayList<TextField> getTextField() { return this.tf; }
    protected ArrayList<Button> getInsertButton() { return this.insertButton; }
    protected ArrayList<Button> getDeleteButton() { return this.deleteButton; }    
}
