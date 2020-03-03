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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

/**
 *
 * @author vinny
 */
public class IngredientList {

    private final Recipe                recipe;
    private final GridPane              ingredientsDisplay;
    private final ArrayList<Integer>    recipeIngredientID;
    private final ArrayList<Ingredient> ingredients;
    private final ArrayList<TextField>  quantityArrayList;
    private final ArrayList<ComboBox>   measureComboBoxArrayList;
    private final ArrayList<TextField>  ingredientArrayList;
    private final ArrayList<String>     measures;
    private       Button                addFieldButton;
    private       ArrayList<Button>     deleteIngredientBtn;


    protected IngredientList (GridPane pane, Recipe rec) {
        this.recipe = rec;
        this.ingredientsDisplay = pane;
        this.ingredients = rec.getIngredients();
        recipeIngredientID = new ArrayList<>();
        quantityArrayList = new ArrayList<>();
        measureComboBoxArrayList = new ArrayList<>();
        deleteIngredientBtn = new ArrayList<>();
        measures = Ingredient.getMeasurements();
        
        ingredientArrayList = new ArrayList<>();
        for (int c = 0; c < this.ingredients.size(); c++) {
            System.out.println(""+this.ingredients.size());
            TextField quantityField = new TextField(""+this.ingredients.get(c).getAmount());
            quantityArrayList.add(quantityField);
            quantityArrayList.get(quantityArrayList.size()-1).setMaxWidth(50);
            ComboBox measureComboBox = new ComboBox();
            for (int t=0; t<measures.size(); t++) {
                measureComboBox.getItems().add(measures.get(t));
            }
            measureComboBox.setEditable(true);
            measureComboBox.setPrefWidth(125.0);
            measureComboBoxArrayList.add(measureComboBox);
            measureComboBoxArrayList.get(c).getSelectionModel().select(this.ingredients.get(c).getMeasure());
            TextField ingredientField = new TextField(this.ingredients.get(c).getIngredient());
            ingredientField.setPrefWidth(400.0);
            ingredientArrayList.add(ingredientField);
            deleteIngredientBtn.add(new Button("-"));
            deleteIngredientBtn.get(deleteIngredientBtn.size()-1).setTooltip(new Tooltip("Remove Ingredient"));
            recipeIngredientID.add(this.ingredients.get(c).getRecipeIngredientID());
            deleteIngredientBtn.get(c).setOnAction(deleteEvent -> {
                    int z = deleteIngredientBtn.indexOf(deleteEvent.getSource());
                    System.out.println(""+z);
                    ingredientsDisplay.getChildren().removeAll(quantityArrayList.get(z), measureComboBoxArrayList.get(z), ingredientArrayList.get(z), deleteIngredientBtn.get(z));
                    recipe.removeIngredient(this.ingredients.get(z), this.recipeIngredientID.get(z));
                    quantityArrayList.remove(z);
                    measureComboBoxArrayList.remove(z);
                    ingredientArrayList.remove(z);
                    deleteIngredientBtn.remove(z);
                    recipe.getDR().editRecipe();
                });
       }
    }

    protected TextField getQuantityTextField(int index) { return this.quantityArrayList.get(index); }
    protected ComboBox getMeasure(int index) { return this.measureComboBoxArrayList.get(index); }
    protected TextField getIngredientField(int index) { return this.ingredientArrayList.get(index); }
    protected Button getDeleteButton(int index) { return this.deleteIngredientBtn.get(index); }
    
    
    
    protected void getIngredientsHeader() {
        addFieldButton = new Button("Add Ingredient Line");
        ingredientsDisplay.add(addFieldButton, 0, 0, 3, 1);
        ingredientsDisplay.addRow(1, new Label("Qty"), new Label("Measure"), new Label("Ingredients"));
        addFieldButton.setOnAction(mouseEvent -> {
            quantityArrayList.add(new TextField());
            quantityArrayList.get(quantityArrayList.size()-1).setMaxWidth(50);
            measureComboBoxArrayList.add(new ComboBox());
            measureComboBoxArrayList.get(measureComboBoxArrayList.size()-1).setEditable(true);
            measureComboBoxArrayList.get(measureComboBoxArrayList.size()-1).setPrefWidth(125.0);
            for (int t=0; t<measures.size(); t++) {
                measureComboBoxArrayList.get(measureComboBoxArrayList.size()-1).getItems().add(measures.get(t));
            }
            ingredientArrayList.add(new TextField());
            int nextRow = quantityArrayList.size();
            ingredientsDisplay.addRow(nextRow+1, quantityArrayList.get(nextRow-1), measureComboBoxArrayList.get(nextRow-1), ingredientArrayList.get(nextRow-1));
        });
    }
        
    protected ArrayList<Ingredient> getIngredients() {
        int numQuantities = this.quantityArrayList.size();
        int numIngredients = this.ingredients.size();
        for (int x = 0; x < numIngredients; x++) {
            if (!quantityArrayList.get(x).getText().equals(this.ingredients.get(x).getAmount()) ||
                !measureComboBoxArrayList.get(x).getSelectionModel().getSelectedItem().equals(this.ingredients.get(x).getMeasure()) ||
                !ingredientArrayList.get(x).getText().equals(this.ingredients.get(x).getIngredient())) {
                Ingredient newIngredient = new Ingredient(ingredientArrayList.get(x).getText());
                newIngredient.setAmount(Float.parseFloat(quantityArrayList.get(x).getText()));
                newIngredient.setMeasure(measureComboBoxArrayList.get(x).getValue().toString());
                this.ingredients.remove(x);
                this.ingredients.add(x, newIngredient);
            }
        }
        for (int y = numIngredients; y < numQuantities; y++) {
            Ingredient newIngredient = new Ingredient(ingredientArrayList.get(y).getText());
            newIngredient.setAmount(Float.parseFloat(quantityArrayList.get(y).getText()));
            newIngredient.setMeasure(measureComboBoxArrayList.get(y).getValue().toString());
            this.ingredients.add(newIngredient);
        }

        return this.ingredients;
    }
}
// ArrayLists of 2 TextFields and a ComboBox to hold ingredient details and a delete button
// ArrayList for ComboBox holding measurement types should be populated with those stored in database
/*        ArrayList<TextField> ingredientArrayList = new ArrayList<>();
        ArrayList<Button> deleteIngredientBtn = new ArrayList<>();
// ArrayList of Ingredients class populated
         if (this.recipeToDisplay != null) {
            ArrayList<Ingredient> ingredientsList = recipeToDisplay.getIngredients();
            for (int i = 0; i < ingredientsList.size(); i++) {
                quantityArrayList.add(new TextField(""+ingredientsList.get(i).getAmount()));
                quantityArrayList.get(quantityArrayList.size()-1).setPrefWidth(50);
                measureComboBox.add(new ComboBox());
                measureComboBox.get(measureComboBox.size()-1).setPrefWidth(90);
                measureComboBox.get(measureComboBox.size()-1).setEditable(true);
// Measures is just 2 static methods in Ingredients class. Does not require much
// manipulation and 1 variable read from the database. Making it's own class likely unnecessary
// Do we need a remove method?
 // Here the database values are read and put into an ArrayList of String
                ArrayList<String> measures = Ingredient.getMeasurements();
                for (int t=0; t<measures.size(); t++) {
                    measureComboBox.get(measureComboBox.size() - 1).getItems().add(measures.get(t));
                }
                measureComboBox.get(i).getSelectionModel().select(ingredientsList.get(i).getMeasure());
                ingredientArrayList.add(new TextField(ingredientsList.get(i).getIngredient()));
                deleteIngredientBtn.add(new Button("-"));
                deleteIngredientBtn.get(i).setTooltip(new Tooltip("Remove Ingredient"));
                ingredientsPane.addRow(i+2, quantityArrayList.get(i), measureComboBox.get(i), ingredientArrayList.get(i), deleteIngredientBtn.get(i));
                
            }
        }
// Here we add TextFields and ComboBox for more ingredients
        
        // Retrieve user input and put into an ArrayList of Ingredients
            for (int i = 0; i < quantityArrayList.size(); i++) {
                if (measureComboBox.get(i).getValue() != null) {
                    Ingredient ing = new Ingredient(ingredientArrayList.get(i).getText());
                    ing.setMeasure(measureComboBox.get(i).getValue().toString());
                    ing.setAmount(Float.parseFloat(quantityArrayList.get(i).getText()));
                    recipeToDisplay.getIngredients().add(ing);
                }
            }
}
*/