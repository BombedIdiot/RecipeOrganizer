package software.blowtorch.recipeorganizer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override public void start(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        double appDefHeight = bounds.getHeight()*.6;
        double appDefWidth = bounds.getWidth()*.55;

        // Category read from database and built into a ListView
        ListView categoryList = new ListView();
        categoryList.getItems().clear();
        categoryList.setPrefHeight(appDefHeight);
        List<String> listOfBooks = Category.getCategoryAll();
        if (listOfBooks.isEmpty()) {
            categoryList.getItems().add("No Category");
        } else {
            categoryList.getItems().add("No Category");
            listOfBooks.forEach((s) -> {
                categoryList.getItems().add(s);
            });
        }

        //Toolbar for Category manipulation Add/Edit/Delete
        ToolBar categoryTlbr = new ToolBar();
        Label categoryLabel = new Label("Categories");
        Button addCategoryBtn = new Button("Add");
        addCategoryBtn.setTooltip(new Tooltip("Add Category"));
        Button removeCategoryBtn = new Button("Remove");
        removeCategoryBtn.setTooltip(new Tooltip("Remove Category"));
        Button editCategoryBtn = new Button("Edit");
        editCategoryBtn.setTooltip(new Tooltip("Edit Category"));
        
        // VBox for top left selector holding Categories
        categoryTlbr.getItems().addAll(categoryLabel, addCategoryBtn, editCategoryBtn, removeCategoryBtn);
        VBox categorySelectorBox = new VBox();
        categorySelectorBox.getChildren().addAll(categoryTlbr, categoryList);

        // Edit category dialog.
        editCategoryBtn.setOnAction((ActionEvent actionEvent) -> {
            TextInputDialog editCategoryDialog = new TextInputDialog();
            editCategoryDialog.setTitle("Edit Category");
            editCategoryDialog.setContentText("Please enter a name for the category");
            Optional<String> result = editCategoryDialog.showAndWait();
            Category.editCategoryName(categoryList.getSelectionModel().getSelectedItem().toString(),result.get());
            categoryList.getItems().set(categoryList.getSelectionModel().getSelectedIndex(), result.get());
        });

        // Remove category handler
        removeCategoryBtn.setOnAction((ActionEvent actionEvent) -> {
            Category.deleteCategory(categoryList.getSelectionModel().getSelectedItem().toString());
            categoryList.getItems().remove(categoryList.getSelectionModel().getSelectedItem());
        });

        // Add Category dialog
        addCategoryBtn.setOnAction((ActionEvent actionEvent) -> {
            TextInputDialog categoryInputDialog = new TextInputDialog();
            categoryInputDialog.setTitle("New Category Name");
            categoryInputDialog.setContentText("Please enter a name for the new category");
            Optional<String> result = categoryInputDialog.showAndWait();
            if (!result.get().isBlank()) {
                Category.addCategory(result.get());
                categoryList.getItems().add(result.get());
            }
        });
        
        //VBox for bottom-left ListView holding recipes from selected category
        VBox recipeSelectorBox = new VBox();
        ListView displayRecipesList = new ListView();
        displayRecipesList.setPrefHeight(appDefHeight);

        // Handler for the recipes ListView that populates the recipe pane
        categoryList.setOnMouseClicked(mouseEvent -> {
            displayRecipesList.getItems().clear();
            List<String> recipesFromCategoryList = Recipe.getRecipeListFromCategory(categoryList.getSelectionModel().getSelectedItem().toString());
            if (recipesFromCategoryList.isEmpty()) {
                displayRecipesList.getItems().add("No Recipes in this Category");
            } else {
                recipesFromCategoryList.forEach((s) -> {
                    displayRecipesList.getItems().add(s);
                });
            }
        });
        recipeSelectorBox.getChildren().addAll(displayRecipesList);

        
        //Create a SplitPane->leftpane holding the category and recipe ListViews
        // Vertical SplitPane holds 2 VBoxes and will be put in the main Split Pane
        SplitPane leftPane = new SplitPane(categorySelectorBox, recipeSelectorBox);
        leftPane.setOrientation(Orientation.VERTICAL);
        leftPane.setDividerPositions(0.5);
        leftPane.setPrefHeight(appDefHeight);

        // VBox to be put in the SplitPane->rightpane to hold a recipe
        VBox rightPane = new VBox();
        // Toolbar for recipe manipulation Add/Edit/Remove
        ToolBar recipeTlbr = new ToolBar();
        Button addRecipeButton = new Button("+ Add Recipe");
        Button editRecipeButton = new Button("Edit Recipe");
        Button removeRecipeButton = new Button("- Delete Recipe");
        recipeTlbr.getItems().addAll(addRecipeButton, editRecipeButton, removeRecipeButton);
        rightPane.getChildren().add(recipeTlbr);

        //Remove recipe handler
        removeRecipeButton.setOnAction(mouseEvent -> {
            Recipe.removeRecipe(new Recipe(displayRecipesList.getSelectionModel().getSelectedItem().toString()));
            displayRecipesList.getItems().remove(displayRecipesList.getSelectionModel().getSelectedItem());
        });

        // Add recipe handler
        DisplayRecipe displayedRecipe = new DisplayRecipe();
        addRecipeButton.setOnAction(mouseEvent -> {
           displayedRecipe.setRecipe(new Recipe());
           displayedRecipe.getRecipe().setDR(displayedRecipe);
           displayedRecipe.editRecipe();
       });
        
        //Handler when a recipe is selected from the bottom left recipe list
        displayRecipesList.setOnMouseClicked(mouseEvent -> {
            if (!displayRecipesList.getSelectionModel().isEmpty()) {
                if (!displayRecipesList.getSelectionModel().getSelectedItem().toString().equals("No Recipes in this Category")) {
                    displayedRecipe.setRecipe(new Recipe(displayRecipesList.getSelectionModel().getSelectedItem().toString()));
                    displayedRecipe.viewRecipe();
                }
            }
        });
        
        //Handler when a recipe is selected from the bottom left recipe list and Edit button clicked
        editRecipeButton.setOnAction(mouseEvent -> {
           displayedRecipe.setRecipe(new Recipe(displayRecipesList.getSelectionModel().getSelectedItem().toString()));
           displayedRecipe.getRecipe().setDR(displayedRecipe);
           displayedRecipe.editRecipe();
        });
        rightPane.getChildren().add(displayedRecipe.getRecipeBox());

        // Main Window setup
        SplitPane mainWindow = new SplitPane(leftPane, rightPane);
        mainWindow.setMinSize(appDefWidth, appDefHeight);
        leftPane.prefHeightProperty().bind(mainWindow.heightProperty());
        VBox.setVgrow(categorySelectorBox, Priority.ALWAYS);
        VBox.setVgrow(recipeSelectorBox, Priority.ALWAYS);
        mainWindow.setDividerPositions(0.2);
        Scene scene = new Scene(mainWindow);
        scene.getStylesheets().add("file:///home/vinny/NetBeansProjects/RecipeOrganizer/src/main/java/software/blowtorch/recipeorganizer/styles.css");
        stage.setTitle("Recipe Organizer");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        stage.setOnCloseRequest((WindowEvent we) -> {
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
        });
    }

    public static void main(String[] args) {
        DBHelper.createDatabaseTables();
        Application.launch(args);
    }
}
