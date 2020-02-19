package software.blowtorch.recipeorganizer;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

    @Override public void start(Stage stage) {
        ListView categoryList = new ListView();
        categoryList.getItems().clear();
        List<String> listOfBooks = Category.getCategoryAll();
        if (listOfBooks.isEmpty()) {
            categoryList.getItems().add("No Category");
        } else {
            categoryList.getItems().add("No Category");
            for (String s : listOfBooks) {
                categoryList.getItems().add(s);
            }
        }

        //Toolbar
        ToolBar categoryTlbr = new ToolBar();
        Button addCategoryBtn = new Button("Add Category");
        addCategoryBtn.setTooltip(new Tooltip("Add Category"));
        Button removeCategoryBtn = new Button("Remove Category");
        removeCategoryBtn.setTooltip(new Tooltip("Remove Category"));
        Button editCategoryBtn = new Button("Edit Category");
        editCategoryBtn.setTooltip(new Tooltip("Edit Category"));
        // Box for top left selector holding Categories
        categoryTlbr.getItems().addAll(addCategoryBtn, editCategoryBtn, removeCategoryBtn);
        VBox categorySelectorBox = new VBox();
        categorySelectorBox.setAlignment(Pos.CENTER);
        categorySelectorBox.getChildren().addAll(categoryTlbr, categoryList);


        editCategoryBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TextInputDialog editCategoryDialog = new TextInputDialog();
                editCategoryDialog.setTitle("Edit Category");
                editCategoryDialog.setContentText("Please enter a name for the category");
                Optional<String> result = editCategoryDialog.showAndWait();
                Category.editCategoryName(categoryList.getSelectionModel().getSelectedItem().toString(),result.get());
                categoryList.getItems().set(categoryList.getSelectionModel().getSelectedIndex(), result.get());
            }
        });

        removeCategoryBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Category.deleteCategory(categoryList.getSelectionModel().getSelectedItem().toString());
                categoryList.getItems().remove(categoryList.getSelectionModel().getSelectedItem());
             }
        });

        addCategoryBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TextInputDialog categoryInputDialog = new TextInputDialog();
                categoryInputDialog.setTitle("New Category Name");
                categoryInputDialog.setContentText("Please enter a name for the new category");
                Optional<String> result = categoryInputDialog.showAndWait();
                if (!result.get().isBlank()) {
                    Category.addCategory(result.get());
                    categoryList.getItems().add(result.get());
                }
            }
        });
        //Secondary (bottom left) selector. Populates from selection in categorySelectorBox
        VBox recipeSelectorBox = new VBox();
        ListView displayRecipesList = new ListView();

        categoryList.setOnMouseClicked(mouseEvent -> {
            displayRecipesList.getItems().clear();
            List<String> recipesFromCategoryList = Recipe.getRecipeListFromCategory(categoryList.getSelectionModel().getSelectedItem().toString());
            if (recipesFromCategoryList.isEmpty()) {
                displayRecipesList.getItems().add(new String("No Recipes in this Category"));
            } else {
                for (String s : recipesFromCategoryList) {
                    displayRecipesList.getItems().add(s);
                }
            }
        });
        recipeSelectorBox.getChildren().addAll(displayRecipesList);

        //the left pane holding the categorized or category items
        SplitPane leftPane = new SplitPane(categorySelectorBox, recipeSelectorBox);
        leftPane.setOrientation(Orientation.VERTICAL);
        leftPane.setDividerPositions(0.5);

        VBox rightPane = new VBox();
        ToolBar recipeTlbr = new ToolBar();
        Button addRecipeButton = new Button("+ Add Recipe");
        Button editRecipeButton = new Button("Edit Recipe");
        Button removeRecipeButton = new Button("- Delete Recipe");
        recipeTlbr.getItems().addAll(addRecipeButton, editRecipeButton, removeRecipeButton);
        rightPane.getChildren().add(recipeTlbr);

        removeRecipeButton.setOnAction(event -> {
            Recipe.removeRecipe(displayRecipesList.getSelectionModel().getSelectedItem().toString());
            displayRecipesList.getItems().remove(displayRecipesList.getSelectionModel().getSelectedItem());
        });

        addRecipeButton.setOnAction((event) -> {
            String category = categoryList.getSelectionModel().getSelectedItem().toString();
            Dialog<Recipe> rNameDialog = new Dialog<>();
            rNameDialog.setTitle("New Recipe in ".concat(category));
            Label nameLabel = new Label("Name");
            TextField nameField = new TextField();
            Label categoryLabel = new Label("Category");
            TextField categoryField = new TextField(category);
            Label descriptionLabel = new Label("Enter a description");
            TextField descriptionField = new TextField();
            GridPane addRecipeGrid = new GridPane();
            addRecipeGrid.setHgap(10);
            addRecipeGrid.add(nameLabel, 1, 1);
            addRecipeGrid.add(nameField, 2, 1);
            addRecipeGrid.add(categoryLabel, 1, 2);
            addRecipeGrid.add(categoryField, 2, 2);
            addRecipeGrid.add(descriptionLabel, 1,3);
            addRecipeGrid.add(descriptionField, 2,3);
            rNameDialog.getDialogPane().setContent(addRecipeGrid);
            ButtonType buttonSave = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            rNameDialog.getDialogPane().getButtonTypes().addAll(buttonSave, buttonCancel);
            nameField.requestFocus();
            rNameDialog.setResultConverter(new Callback<ButtonType, Recipe>() {
                @Override
                public Recipe call(ButtonType buttonType) {
                    if (buttonType == buttonSave) {
                        int cat;
                        if (nameField.getText().equals("")) {
                            return null;
                        }
                        if (categoryField.getText().equals("")) {
                            cat = 0;
                        } else {
                            cat = Category.getCategoryID(categoryField.getText());
                        }
                        Recipe newRecipe =  new Recipe(nameField.getText(), cat, descriptionField.getText());
                        newRecipe.saveRecipe();
                        return newRecipe;
                    } else {
                        return null;
                    }
                }
            });
            Optional<Recipe> addRecipe = rNameDialog.showAndWait();
        });

        // The main information window populated from recipeSelectorBox
        VBox recipeBox = new VBox();
        GridPane nameDescriptionBox = new GridPane();
        nameDescriptionBox.setVgap(40);
        nameDescriptionBox.setHgap(10);
        HBox recipeTopBox = new HBox();
        recipeTopBox.setSpacing(50);
        nameDescriptionBox.setAlignment(Pos.CENTER);

        displayRecipesList.setOnMouseClicked(mouseEvent -> {
            if (!displayRecipesList.getSelectionModel().isEmpty()) {
                if (!displayRecipesList.getSelectionModel().getSelectedItem().toString().equals("No Recipes in this Category")) {
                    Recipe displayedRecipe = new Recipe(displayRecipesList.getSelectionModel().getSelectedItem().toString());
                    ImageView recipeImageView = null;
                    try {
                        Image recipeImage = new Image(new FileInputStream("/home/vinny/IdeaProjects/RecipeOrganizer/src/software/blowtorch/image/wide_32.jpg"));
                        recipeImageView = new ImageView(recipeImage);
                    } catch (IOException ex) {
                        System.err.println("Exception :" + ex.getMessage() + " in displayRecipes Event Handler");
                    }
                    recipeImageView.setFitWidth(400);
                    recipeImageView.setPreserveRatio(true);
                    Label nameLabel = new Label("Name :" + displayedRecipe.getRecipeName());
                    Label descriptionLabel = new Label("Description :" + displayedRecipe.getRecipeDesc());
                    Label categoryLabel = new Label("Category :" + displayedRecipe.getRecipeCategory());
                    HBox bottomBox = new HBox();
                    bottomBox.setSpacing(100);
                    GridPane ingredientsPane = new GridPane();
                    ingredientsPane.setPrefHeight(500);
                    ingredientsPane.setHgap(10);
                    ingredientsPane.setVgap(10);
                    ingredientsPane.addRow(0, new Text(" "));
                    ingredientsPane.addRow(1, new Label("Qty"), new Label("Measure"), new Label("Ingredients"));
                    ArrayList<Ingredient> ingredientsList = displayedRecipe.getIngredients();
                    for (int i = 0; i < ingredientsList.size(); i++) {
                        ingredientsPane.addRow(i+2, new Label(""+ingredientsList.get(i).getAmount()), new Label(ingredientsList.get(i).getMeasure()),
                                new Label(ingredientsList.get(i).getIngredient()));
                    }
                    GridPane directionsPane = new GridPane();
                    directionsPane.setPrefHeight(500);
                    directionsPane.setHgap(10);
                    directionsPane.setVgap(10);
                    directionsPane.addRow(0, new Text(" "));
                    directionsPane.addRow(1, new Label("Directions :"));
                    ArrayList<Directions> directionsList = displayedRecipe.getDirections();
                    if(directionsList != null) {
                        for (int i = 0; i < directionsList.size(); i++) {
                            directionsPane.addRow(i + 2, new Label(directionsList.get(i).getDirection()));
                        }
                    }
                    recipeTopBox.getChildren().clear();
                    nameDescriptionBox.getChildren().clear();
                    recipeBox.getChildren().clear();
                    recipeTopBox.getChildren().add(recipeImageView);
                    nameDescriptionBox.addRow(0, nameLabel);
                    nameDescriptionBox.addRow(1, categoryLabel);
                    nameDescriptionBox.addRow(2, descriptionLabel);
                    recipeTopBox.getChildren().add(nameDescriptionBox);
                    bottomBox.getChildren().addAll(ingredientsPane, directionsPane);
                    recipeBox.getChildren().addAll(recipeTopBox, bottomBox);
                }
            }
        });

        editRecipeButton.setOnAction((event) -> {
            Recipe displayedRecipe = new Recipe(displayRecipesList.getSelectionModel().getSelectedItem().toString());
            displayedRecipe.getRecipe();
            ImageView recipeImageView = null;
            try {
                Image recipeImage = new Image(new FileInputStream("/home/vinny/IdeaProjects/RecipeOrganizer/src/software/blowtorch/image/wide_32.jpg"));
                recipeImageView = new ImageView(recipeImage);
            } catch (IOException ex) {
                System.err.println("Exception :" + ex.getMessage()+" in editRecipeButton Event Handler");
            }
            recipeImageView.setFitWidth(400);
            recipeImageView.setPreserveRatio(true);
            Label nameLabel = new Label("Name :");
            TextField nameField = new TextField(displayedRecipe.getRecipeName());
            Label descriptionLabel = new Label("Description :");
            TextField descriptionField = new TextField(displayedRecipe.getRecipeDesc());
            Label categoryLabel = new Label("Category :");
            TextField categoryField = new TextField(displayedRecipe.getRecipeCategory());

            HBox bottomBox = new HBox();
            bottomBox.setSpacing(100);
            GridPane ingredientsPane = new GridPane();
            ingredientsPane.setPrefHeight(500);
            ingredientsPane.setHgap(10);
            ingredientsPane.setVgap(10);
            ingredientsPane.addRow(0, new Text(" "));
            Button addFieldTextBtn = new Button("+");
            addFieldTextBtn.setTooltip(new Tooltip("Add Ingredient Line"));
            ingredientsPane.addRow(1, new Label("Qty"), new Label("Measure"), new Label("Ingredients"), addFieldTextBtn);
            ArrayList<TextField> quantityArrayList = new ArrayList<TextField>();
            ArrayList<ComboBox> measureComboBox = new ArrayList<ComboBox>();
            ArrayList<TextField> ingredientArrayList = new ArrayList<TextField>();
            ArrayList<Ingredient> ingredientsList = displayedRecipe.getIngredients();
            for (int i = 0; i < ingredientsList.size(); i++) {
                quantityArrayList.add(new TextField(""+ingredientsList.get(i).getAmount()));
                measureComboBox.add(new ComboBox());
                ArrayList<String> measures = Ingredient.getMeasurements();
                for (int t=0; t<measures.size(); t++) {
                    measureComboBox.get(measureComboBox.size() - 1).getItems().add(measures.get(t));
                }
                measureComboBox.get(measureComboBox.size()-1).setPrefWidth(90);
                measureComboBox.get(measureComboBox.size()-1).setEditable(true);
                quantityArrayList.get(quantityArrayList.size()-1).setPrefWidth(50);
                ingredientArrayList.add(new TextField(ingredientsList.get(i).getIngredient()));
                measureComboBox.get(i).getSelectionModel().select(ingredientsList.get(i).getMeasure());
                ingredientsPane.addRow(i+2, quantityArrayList.get(i), measureComboBox.get(i), ingredientArrayList.get(i));
            }
            addFieldTextBtn.setOnAction(mouseEvent -> {
                quantityArrayList.add(new TextField());
                quantityArrayList.get(quantityArrayList.size()-1).setPrefWidth(50);
                measureComboBox.add(new ComboBox());
                ArrayList<String> measures = Ingredient.getMeasurements();
                for (int t=0; t<measures.size(); t++) {
                    measureComboBox.get(measureComboBox.size() - 1).getItems().add(measures.get(t));
                }
                measureComboBox.get(measureComboBox.size()-1).setPrefWidth(90);
                measureComboBox.get(measureComboBox.size()-1).setEditable(true);
                ingredientArrayList.add(new TextField());
                int nextRow = quantityArrayList.size();
                ingredientsPane.addRow(nextRow+1, quantityArrayList.get(nextRow-1), measureComboBox.get(nextRow-1), ingredientArrayList.get(nextRow-1));
            });

            GridPane directionsPane = new GridPane();
            directionsPane.setPrefHeight(500);
            directionsPane.setHgap(10);
            directionsPane.setVgap(10);
            directionsPane.addRow(0, new Text(" "));
            Button addDirectionBtn = new Button("Add Direction");
            HBox directionHeaderBox = new HBox(new Label("Directions"), addDirectionBtn);
            directionHeaderBox.setSpacing(90);
            directionHeaderBox.setAlignment(Pos.CENTER);
            directionsPane.addRow(1, directionHeaderBox);
            ArrayList<TextField> directionArrayList = new ArrayList<TextField>();
            ArrayList<Directions> directionsList = displayedRecipe.getDirections();
            for (int i = 0; i < directionsList.size(); i++) {
            directionArrayList.add(new TextField(directionsList.get(i).getDirection()));
            directionArrayList.get(directionArrayList.size()-1).setPrefWidth(500);
                directionsPane.addRow(i+2, directionArrayList.get(i));
            }
            addDirectionBtn.setOnAction(mouseEvent -> {
                directionArrayList.add(new TextField());
                directionArrayList.get(directionArrayList.size()-1).setPrefWidth(500);
                int nextRow = directionArrayList.size();
                directionsPane.addRow(nextRow+1, directionArrayList.get(nextRow-1));
            });

            ButtonBar saveCancelBtnBar = new ButtonBar();
            Button buttonSave = new Button("Save");
            ButtonBar.setButtonData(buttonSave, ButtonBar.ButtonData.OK_DONE);
            Button buttonCancel = new Button("Cancel");
            ButtonBar.setButtonData(buttonCancel, ButtonBar.ButtonData.CANCEL_CLOSE);
            saveCancelBtnBar.getButtons().addAll(buttonSave, buttonCancel);

            buttonSave.setOnAction(saveEvent -> {
                ArrayList<Ingredient> ingredientList = new ArrayList<>();
                for (int i = 0; i < ingredientArrayList.size(); i++) {
                    if (measureComboBox.get(i).getValue() != null) {
                        Ingredient ing = new Ingredient(ingredientArrayList.get(i).getText());
                        ing.setMeasure(measureComboBox.get(i).getValue().toString());
                        ing.setAmount(Float.parseFloat(quantityArrayList.get(i).getText()));
                        ingredientList.add(ing);
                    }
                }
                displayedRecipe.setIngredients(ingredientList);
                displayedRecipe.saveRecipeIngredients();
                for (int i=0; i < directionArrayList.size(); i++) {
                    Directions dir = new Directions(directionArrayList.get(i).getText());
                    directionsList.add(dir);
                }
                displayedRecipe.setDirections(directionsList);
                displayedRecipe.saveDirections();
            });
            recipeTopBox.getChildren().clear();
            nameDescriptionBox.getChildren().clear();
            recipeBox.getChildren().clear();
            bottomBox.getChildren().clear();
            recipeTopBox.getChildren().add(recipeImageView);
            nameDescriptionBox.addRow(0, nameLabel, nameField);
            nameDescriptionBox.addRow(1, categoryLabel, categoryField);
            nameDescriptionBox.addRow(2, descriptionLabel, descriptionField);
            recipeTopBox.getChildren().add(nameDescriptionBox);
            bottomBox.getChildren().addAll(ingredientsPane, directionsPane);
            HBox buttonBarBox = new HBox();
            buttonBarBox.getChildren().add(saveCancelBtnBar);
            buttonBarBox.setAlignment(Pos.BOTTOM_CENTER);
            recipeBox.getChildren().addAll(recipeTopBox, bottomBox, buttonBarBox);
        });

        rightPane.getChildren().add(recipeBox);

        // Main Window setup
        SplitPane mainWindow = new SplitPane(leftPane, rightPane);
        mainWindow.setMinSize(1500, 600);
        mainWindow.setDividerPositions(0.3);
        Scene scene = new Scene(mainWindow);
 //       scene.getStylesheets().add("UI/styles.css");
        stage.setTitle("Recipe Organizer");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                try {
                    String database = "jdbc:derby:;shutdown=true";
                    DriverManager.getConnection(database);
                } catch (SQLException ex) {
                    if (ex.getSQLState().equals("XJ015")) {
                        System.out.println("Derby shutdown normally");
                    } else {
                        System.out.println("Derby didn't shutdown normally");
                    }
                    System.out.println("Closing...");
                }
            }
        });
    }

    public static void main(String[] args) {
        DBHelper.createDatabaseTables();
        Application.launch(args);
    }
}