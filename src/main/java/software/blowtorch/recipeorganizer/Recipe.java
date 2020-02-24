package software.blowtorch.recipeorganizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static software.blowtorch.recipeorganizer.DBHelper.*;

public class Recipe {

    private int                   recipeID;
    private String                name;
    private String                description;
    private String                category;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<Directions> directions;


    protected Recipe (String name) {
        this.name = name;
        try (Connection conn = DBHelper.connectDB(); 
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM "+RECIPE_TABLE+" WHERE "+RECIPE_NAME+" = ?")) {
            stmt.setString(1, this.name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() != false) {
                    do {
                        this.recipeID = rs.getInt(RECIPE_ID);
                        this.description = rs.getString(RECIPE_DESCRIPTION);
                        this.category = Category.getCategoryByID(rs.getInt(RECIPE_CATEGORY));
                    } while (rs.next());
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error :" + ex.getMessage()+" in getRecipe");
        }
        this.ingredients = Ingredient.getIngredientsByRecipeID(this.recipeID);
        this.directions = Directions.getDirectionsByRecipeID(this.recipeID);
    }
    
    protected Recipe (String name, String description) {
        this.name = name;
        this.description = description;
        this.category = "";
    }
    
    protected Recipe(String name, int category, String description) {
        this.name = name;
        this.category = Category.getCategoryByID(category);
        this.description = description;
    }

    // Getters
    protected int                   getRecipeID()       { return this.recipeID; }
    protected String                getRecipeName()     { return this.name; }
    protected String                getRecipeDesc()     { return this.description; }
    protected String                getRecipeCategory() { return this.category; }
    protected ArrayList<Ingredient> getIngredients()    { return this.ingredients; }
    protected ArrayList<Directions> getDirections()     { return this.directions; }

    // Setters
    protected void setIngredients(ArrayList<Ingredient> i)  { this.ingredients = i; }
    protected void setDirections(ArrayList<Directions> d)   { this.directions = d; }


    protected void removeDirection(Directions d) {
       try (Connection conn = DBHelper.connectDB();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM "+DIRECTIONS_RECIPE_TABLE+" WHERE "+DIRECTIONS_RECIPE_ID+" = ?")) {
            stmt.setInt(1, d.getDirectionDatabaseID());
            stmt.execute();
            this.directions.remove(d);
        } catch (SQLException ex) {
            System.err.println("Error :" + ex.getMessage()+" in removeDirection");
        } 
    }
    
    protected void removeIngredient(Ingredient i) {
       try (Connection conn = DBHelper.connectDB(); 
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM "+RECIPE_INGREDIENT_TABLE+" WHERE "+INGREDIENT_FK+" = ? AND "+RECIPE_FK_INGRED+" = ?")) {
            stmt.setInt(2, this.getRecipeID());
            stmt.setInt(1, i.getIngredientID());
            stmt.execute();
            this.ingredients.remove(i);
            System.out.println("Deleted "+i.getIngredient()+" From Recipe "+this.getRecipeName());
        } catch (SQLException ex) {
            System.err.println("Error :" + ex.getMessage()+" in removeDirection");
        } 
    }
    
    
    protected void saveRecipeIngredients() {
        deleteIngredientsFromRecipe(this.name);
        String sql = "INSERT INTO "+RECIPE_INGREDIENT_TABLE+" ("+INGREDIENT_FK+","+ RECIPE_FK_INGRED +","+AMOUNT+","+MEASUREMENT_FK+") VALUES (?, ?, ?, ?)";
        try (Connection conn = DBHelper.connectDB(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int x = 0; x<this.ingredients.size(); x++) {
                stmt.setInt(1, this.ingredients.get(x).getIngredientID());
                stmt.setInt(2, this.getRecipeID());
                stmt.setFloat(3, this.ingredients.get(x).getAmount());
                stmt.setInt(4, Ingredient.newMeasurement(this.ingredients.get(x).getMeasure()));
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            System.err.println("Error :" + ex.getMessage()+" in saveRecipeIngredients");
        }
    }

    protected void saveDirections() {
        deleteDirectionsFromRecipe(this.name);
        String sql = "INSERT INTO "+DIRECTIONS_RECIPE_TABLE+" ("+DIRECTIONS_FK+","+ RECIPE_FK_DIRECTION +") VALUES (?, ?)";
        try (Connection conn = DBHelper.connectDB();
               PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);) {
            for (int x = 0; x<this.directions.size(); x++) {
                stmt.setInt(1, this.directions.get(x).getDirectionID());
                stmt.setInt(2, this.getRecipeID());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys();) {
                    if (rs.next()) {
                        this.directions.get(x).setDirectionDatabaseID(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error :" + ex.getMessage()+" in saveRecipeIngredients");
        }
    }

    protected void saveRecipe() {
        try {
            Connection conn = DBHelper.connectDB();
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO "+RECIPE_TABLE+" ("+RECIPE_NAME+", "+RECIPE_DESCRIPTION+", "+ RECIPE_CATEGORY +") VALUES " +
                    "(?, ?, ?)");
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            if (this.category.equals("No Category")) {
                preparedStatement.setInt(3, 0);
            } else {
                preparedStatement.setInt(3, Category.getCategoryID(this.category));
            }
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException ex) {
            System.err.println("Error :" + ex.getMessage()+" in saveRecipe");
        }
    }
    
    protected static void removeRecipe(String recipe) {
        deleteIngredientsFromRecipe(recipe);
        try {
            try (Connection conn = DBHelper.connectDB(); PreparedStatement statement = conn.prepareStatement("DELETE FROM "+ RECIPE_TABLE +" WHERE "+ RECIPE_NAME +" = ?")) {
                statement.setString(1, recipe);
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage()+" in removeRecipe");
        }
    }

    protected static void deleteIngredientsFromRecipe(String recipe) {
        try {
            Connection conn = DBHelper.connectDB();
            PreparedStatement statement = conn.prepareStatement("DELETE FROM "+ RECIPE_INGREDIENT_TABLE +" WHERE "+ RECIPE_FK_INGRED +
                                                                "= (SELECT "+RECIPE_ID+" FROM "+RECIPE_TABLE+" WHERE "+RECIPE_NAME+"=?)");
            statement.setString(1, recipe);
            statement.executeUpdate();
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage()+" in deleteIngredientsFromRecipe");
        }
    }

    protected static void deleteDirectionsFromRecipe(String recipe) {
        try {
            Connection conn = DBHelper.connectDB();
            PreparedStatement statement = conn.prepareStatement("DELETE FROM "+ DIRECTIONS_RECIPE_TABLE +" WHERE "+ RECIPE_FK_DIRECTION +
                    "= (SELECT "+RECIPE_ID+" FROM "+RECIPE_TABLE+" WHERE "+RECIPE_NAME+"=?)");
            statement.setString(1, recipe);
            statement.executeUpdate();
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage()+" in deleteDirectionsFromRecipe");
        }
    }

    protected static List<String> getRecipeListFromCategory(String cb) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql;
        try {
            conn = DBHelper.connectDB();
            if (cb.equals("No Category")) {
                sql = "SELECT "+RECIPE_NAME+" FROM "+RECIPE_TABLE+" WHERE "+ RECIPE_CATEGORY +" = 0";
                stmt = conn.prepareStatement(sql);
            } else {
                sql = "SELECT "+RECIPE_NAME+" FROM "+RECIPE_TABLE+" WHERE "+ RECIPE_CATEGORY +" = (SELECT "+ CATEGORY_ID +" FROM "+ CATEGORY_TABLE +" WHERE "+ CATEGORY_NAME +" = ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, cb);
            }
            rs = stmt.executeQuery();
            List<String> recipeList = new ArrayList<>();
            if (rs.next() != false) {
                do {
                    recipeList.add(rs.getString(RECIPE_NAME));
                } while (rs.next());
            }
            return recipeList;
        } catch (SQLException ex) {
            System.err.println("Exception : " + ex.getMessage()+" in getRecipeListFromCategory");
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception : " + ex.getMessage()+" in getRecipeListFromCategory");
            }
        }
    }
}
