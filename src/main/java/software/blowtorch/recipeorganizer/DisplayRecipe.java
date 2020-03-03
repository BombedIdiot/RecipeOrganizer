package software.blowtorch.recipeorganizer;

import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

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

/**
 *
 * @author vinny
 */
public class DisplayRecipe {
    
    private       Recipe        recipeToDisplay = null;
    private final GridPane      recipeHead;
    private final GridPane      ingredientsPane;
    private final GridPane      directionsPane;
    private final VBox          mainRecipeBox;
    private final ScrollPane    mainRecipePane;
    private final VBox          recipeBox;
    private final Separator     separator1;
    private final Separator     separator2;

    public DisplayRecipe() {
        recipeHead = new GridPane();
        ingredientsPane = new GridPane();
        directionsPane = new GridPane();
        mainRecipeBox = new VBox();
        mainRecipePane = new ScrollPane();
        recipeBox = new VBox();
        separator1 = new Separator();
        separator2 = new Separator();

    // HBox->viewRecipeHead holds Name/Category/Description on View
    // FlowPane->editRecipeHead holds Name/Category/Description on Edit
    // This is because the we have 3 fields to display on view, 6 to display on edit
        recipeHead.setPadding(new Insets(10));
    // 2 GridPanes for holding ingredients and directions
        ingredientsPane.setPadding(new Insets(30));
        directionsPane.setPadding(new Insets(30));
        ingredientsPane.setHgap(20);
        ingredientsPane.setVgap(10);
        directionsPane.setHgap(20);
        directionsPane.setVgap(10);
    // BorderPane->recipeBox holds it all
        recipeBox.getStyleClass().add("view");
    }

    protected VBox getRecipeBox() { return recipeBox; }
    protected Recipe getRecipe() { return recipeToDisplay; }

    protected void setRecipe(Recipe r) { this.recipeToDisplay = r; }


    public void viewRecipe() {
    //Handler when a recipe is selected from the bottom left recipe list
        recipeHead.getChildren().clear();
        ingredientsPane.getChildren().clear();
        directionsPane.getChildren().clear();
        mainRecipeBox.getChildren().clear();
        recipeBox.getChildren().clear();

        Label nameLabel = new Label("Name : " + recipeToDisplay.getRecipeName());
        Label descriptionLabel = new Label("Description : " + recipeToDisplay.getRecipeDesc());
        Label categoryLabel = new Label("Category : " + recipeToDisplay.getRecipeCategory());
        ingredientsPane.addRow(1, new Label("Qty"), new Label("Measure"), new Label("Ingredients"));
        ArrayList<Ingredient> ingredientsList = recipeToDisplay.getIngredients();
        for (int i = 0; i < ingredientsList.size(); i++) {
            ingredientsPane.addRow(i+2, new Label(""+ingredientsList.get(i).getAmount()), new Label(ingredientsList.get(i).getMeasure()),
                                        new Label(ingredientsList.get(i).getIngredient()));
        }
        directionsPane.addRow(1, new Label("Directions :"));
        ArrayList<Directions> directionsList = recipeToDisplay.getDirections();
        if(directionsList != null) {
            for (int i = 0; i < directionsList.size(); i++) {
                directionsPane.addRow(i + 2, new Label(directionsList.get(i).getDirection()));
            }
        }
        recipeHead.addRow(0, nameLabel);
        recipeHead.addRow(1, new Label());
        recipeHead.addRow(2, categoryLabel);
        recipeHead.addRow(3, new Label());
        recipeHead.addRow(4, descriptionLabel);
        recipeBox.getChildren().add(recipeHead);
        mainRecipeBox.getChildren().addAll(separator1, ingredientsPane, separator2, directionsPane);
        ingredientsPane.setStyle("-fx-border-color: #C5C5CC");
        directionsPane.setStyle("-fx-border-color: #C5C5CC");
        mainRecipePane.setContent(mainRecipeBox);
        recipeBox.getChildren().add(mainRecipePane);
    }

    public void editRecipe() {
        recipeHead.getChildren().clear();
        ingredientsPane.getChildren().clear();
        directionsPane.getChildren().clear();
        mainRecipeBox.getChildren().clear();
        recipeBox.getChildren().clear();
        Label nameLabel = new Label("Name :");
        TextField nameField = new TextField();
        nameField.setPrefWidth(250.0);
        Label descriptionLabel = new Label("Description :");
        TextField descriptionField = new TextField();
        descriptionField.setPrefWidth(400.0);
        Label categoryLabel = new Label("Category :");
        TextField categoryField = new TextField();
        if (this.recipeToDisplay != null) {
            nameField.setText(recipeToDisplay.getRecipeName());
            descriptionField.setText(recipeToDisplay.getRecipeDesc());
            categoryField.setText(recipeToDisplay.getRecipeCategory());
        }
        
// add that FlowPane->directionHeaderBox to GridPane->directionsPane then each directions line is added
        DirectionList directionList = new DirectionList(directionsPane, recipeToDisplay);
        if (this.recipeToDisplay != null) {
            FlowPane directionHeaderBox = directionList.getDirectionsHeader();
            directionsPane.addRow(1, directionHeaderBox);
            for (int x=0; x<directionList.getTextField().size(); x++) {
                directionsPane.addRow(x+2, directionList.getTextField().get(x));
            }
        }
        IngredientList ingredientList = new IngredientList(ingredientsPane, recipeToDisplay);
        if (this.recipeToDisplay != null) {
            ingredientList.getIngredientsHeader();
            int total = ingredientList.getIngredients().size();
                for (int x=0; x < total; x++) {
                    ingredientsPane.addRow(x+2, ingredientList.getQuantityTextField(x), ingredientList.getMeasure(x), ingredientList.getIngredientField(x), ingredientList.getDeleteButton(x));
                }
        }

//Save & Cancel buttons put into a ButtonBar then added to it's own HBox
        ButtonBar saveCancelBtnBar = new ButtonBar();
        Button buttonSave = new Button("Save");
        ButtonBar.setButtonData(buttonSave, ButtonBar.ButtonData.OK_DONE);
        Button buttonCancel = new Button("Cancel");
        ButtonBar.setButtonData(buttonCancel, ButtonBar.ButtonData.CANCEL_CLOSE);
        saveCancelBtnBar.getButtons().addAll(buttonSave, buttonCancel);

// Handler for Save Recipe
        buttonSave.setOnAction(saveEvent -> {
            // set the Recipe object from the recipe form, remove any data stored from database
            recipeToDisplay.setName(nameField.getText());
            recipeToDisplay.setDescription(descriptionField.getText());
            recipeToDisplay.setCategory(categoryField.getText());
            recipeToDisplay.setIngredients(ingredientList.getIngredients());
            recipeToDisplay.setDirections(directionList.getDirections());
            recipeToDisplay.saveRecipe(recipeToDisplay);
        });

// GridPane->nameDescriptionBox into an HBox->recipeTopBox, put into a VBox->recipeBox
// 2 GridPanes holding instructions and recipes put into an HBox and then added to the HBox->bottomBox
// VBox->recipeBox holds recipeTopBox, the newly created HBox->bottomBox and the buttonBarBox
// that added into the rightPane for main SplitPane->mainWindow
        recipeHead.addRow(0, nameLabel, nameField);
        recipeHead.addRow(1, categoryLabel, categoryField);
        recipeHead.addRow(2, descriptionLabel, descriptionField);
        ingredientsPane.setStyle("-fx-border-color: black");
        directionsPane.setStyle("-fx-border-color: black");
        double width = recipeBox.getWidth()-100.0;
        ingredientsPane.setPrefWidth(width);
        directionsPane.setPrefWidth(width);
        mainRecipeBox.getChildren().addAll(ingredientsPane, directionsPane);
        mainRecipePane.setContent(mainRecipeBox);
        mainRecipeBox.setSpacing(25);
        recipeBox.setPadding(new Insets(25));
        recipeBox.getChildren().addAll(recipeHead, mainRecipePane, saveCancelBtnBar);
    }
}
