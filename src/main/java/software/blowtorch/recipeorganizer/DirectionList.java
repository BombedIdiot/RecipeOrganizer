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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

/**
 *
 * @author vinny
 */
public class DirectionList {
    private final Recipe recipe;
    private final GridPane directionsDisplay;
    private final ArrayList<TextField> tf = new ArrayList<>();
    private final ArrayList<Directions> direction;
    private final ArrayList<Button> insertButton = new ArrayList<>();
    private final ArrayList<Button> deleteButton = new ArrayList<>();
    private ArrayList<Button> moveUpButton;
    private ArrayList<Button> moveDownButton;
    private static final int DELETE = 1;
    private static final int INSERT = 2;
    
    protected DirectionList(ArrayList<Directions> d, GridPane pane, Recipe rec) {
        this.recipe = rec;
        this.directionsDisplay = pane;
        this.direction = d;
        for (int c=0; c < d.size(); c++) {
            TextField newField = new TextField();
            newField.setText(d.get(c).getDirection());
            newField.setPrefWidth(300);
            tf.add(newField);
            Button newDeleteButton  = new Button("-");
            newDeleteButton.setTooltip(new Tooltip("Remove"));
            newDeleteButton.setOnAction(directionHandler(DELETE, c));
            deleteButton.add(newDeleteButton);
            Button newInsertButton = new Button("+");
            newInsertButton.setTooltip(new Tooltip("Insert"));
            newInsertButton.setOnAction(directionHandler(INSERT, c));
            insertButton.add(newInsertButton);
       }
        TextField defaultField = new TextField();
        defaultField.setPrefWidth(300);
        tf.add(defaultField);
        Button defaultDeleteButton = new Button("-");
        defaultDeleteButton.setTooltip(new Tooltip("Delete"));
        defaultDeleteButton.setOnAction(directionHandler(DELETE, deleteButton.size()));
        deleteButton.add(defaultDeleteButton);
        Button defaultInsertButton = new Button("+");
        defaultInsertButton.setTooltip(new Tooltip("Insert"));
        defaultInsertButton.setOnAction(directionHandler(INSERT, insertButton.size()));
        insertButton.add(defaultInsertButton);
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
    
    
    private EventHandler<ActionEvent> directionHandler(int action, int index) {
        if (action == DELETE) {
            return event -> deleteDirection(index);
        } else if (action == INSERT) {
            return event ->insertDirection(index);
        }
        return null;
    }
    private void deleteDirection(int index) {
        directionsDisplay.getChildren().removeAll(tf.get(index), insertButton.get(index), deleteButton.get(index));
        recipe.removeDirection(direction.get(index));
        tf.remove(tf.get(index));
        insertButton.remove(insertButton.get(index));
        deleteButton.remove(deleteButton.get(index));
    }
    
    private void insertDirection(int index) {
        tf.add(index, new TextField());
        directionsDisplay.addRow(index+2, tf.get(index));
   }
}
