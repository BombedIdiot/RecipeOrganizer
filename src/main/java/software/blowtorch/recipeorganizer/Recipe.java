package software.blowtorch.recipeorganizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static software.blowtorch.recipeorganizer.DBHelper.*;

public class Recipe {

    private DisplayRecipe dr;
    private int                   recipeID;
    private String                name;
    private String                description;
    private String                category;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<Directions> directions;

    protected Recipe () {
        recipeID = 0;
        name = "";
        description = "";
        category = "";
        ingredients = new ArrayList<>();
        directions = new ArrayList<>();
    }
    
    protected Recipe (String name) {
        try (Connection conn = DBHelper.connectDB(); 
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM "+RECIPE_TABLE+" WHERE "+RECIPE_NAME+" = ?")) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() != false) {
                    do {
                        this.recipeID = rs.getInt(RECIPE_ID);
                        this.name = rs.getString(RECIPE_NAME);
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

    // Getters
    protected int                   getRecipeID()       { return this.recipeID; }
    protected String                getRecipeName()     { return this.name; }
    protected String                getRecipeDesc()     { return this.description; }
    protected String                getRecipeCategory() { return this.category; }
    protected ArrayList<Ingredient> getIngredients()    { return this.ingredients; }
    protected ArrayList<Directions> getDirections()     { return this.directions; }
    protected DisplayRecipe getDR() { return this.dr; }

    // Setters
    protected void setName(String name) { this.name = name; }
    protected void setDescription(String descrip) { this.description = descrip; }
    protected void setCategory(String category) { this.category = category; }
    protected void setIngredients(ArrayList<Ingredient> i)  { this.ingredients = i; }
    protected void setDirections(ArrayList<Directions> d)   { this.directions = d; }
    protected void addDirections(Directions d)   { this.directions.add(d); }
    protected void setDR(DisplayRecipe q) { this.dr = q; }


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
    
    protected void removeIngredient(Ingredient i, int id) {
       try (Connection conn = DBHelper.connectDB(); 
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM "+RECIPE_INGREDIENT_TABLE+" WHERE "+RECIPE_INGREDIENT_ID+" = ?")) {
            stmt.setInt(1, id);
            stmt.execute();
            this.ingredients.remove(i);
        } catch (SQLException ex) {
            System.err.println("Error :" + ex.getMessage()+" in removeDirection");
        } 
    }
    
    
    protected void saveRecipe(Recipe enteredRecipe) {
        Recipe savedVersion = new Recipe(enteredRecipe.getRecipeName());
        String sql;
        if (savedVersion.getRecipeName() == null) {
            sql = "INSERT INTO "+RECIPE_TABLE+" ("+RECIPE_NAME+", "+RECIPE_DESCRIPTION+", "+ RECIPE_CATEGORY +") VALUES " + "(?, ?, ?)";
        } else {
            sql = "UPDATE "+RECIPE_TABLE+" SET "+RECIPE_NAME+"=?, "+RECIPE_DESCRIPTION+"=?, "+ RECIPE_CATEGORY +"=? WHERE "+RECIPE_ID+" = "+enteredRecipe.getRecipeID();
        }
        try (Connection conn = DBHelper.connectDB();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, enteredRecipe.getRecipeName());
            if (enteredRecipe.getRecipeDesc().equals("")) {
                preparedStatement.setInt(2, 0);
            } else {
                preparedStatement.setString(2, enteredRecipe.getRecipeDesc());
            }
            if (enteredRecipe.getRecipeCategory().equals("")) {
                preparedStatement.setInt(3, 0);
            } else {
                preparedStatement.setInt(3, Category.getCategoryID(enteredRecipe.getRecipeCategory()));
            }
            preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                System.err.println("Error :" + ex.getMessage()+" in saveRecipe INSERT new");
            }
   // here recipe exists or was created and enteredRecipe is king. We could check to see if the category or description have changed but
   // it doesn't really matter, we can just UPDATE with enteredRecipe seeing as that's what the user wants anyway.  
        
// Is it really worthwhile to check every ingredient saved vs. entered and UPDATE. Just delete the ingredient list and re-INSERT the new one
// Maybe if it is completely unchanged, leave it alone.
        deleteIngredientsFromRecipe(enteredRecipe.recipeID);
            String ingredientSql = "INSERT INTO "+RECIPE_INGREDIENT_TABLE+" ("+INGREDIENT_FK+","+ RECIPE_FK_INGRED +","+AMOUNT+","+MEASUREMENT_FK+") VALUES (?, ?, ?, ?)";
            try (Connection conn = DBHelper.connectDB(); PreparedStatement stmt = conn.prepareStatement(ingredientSql)) {
                for (int x = 0; x < enteredRecipe.getIngredients().size(); x++) {
                    stmt.setInt(1, enteredRecipe.getIngredients().get(x).getIngredientID());
                    stmt.setInt(2, enteredRecipe.getRecipeID());
                    stmt.setFloat(3, enteredRecipe.getIngredients().get(x).getAmount());
                    stmt.setInt(4, Ingredient.newMeasurement(enteredRecipe.getIngredients().get(x).getMeasure()));
                    stmt.executeUpdate();
                }
            } catch (SQLException ex) {
                System.err.println("Error :" + ex.getMessage()+" in saveRecipe INSERT Ingredients");
            }
    
    //check directions with directions saved and update if necessary
        deleteDirectionsFromRecipe(this.recipeID);
        String directionSql = "INSERT INTO "+DIRECTIONS_RECIPE_TABLE+" ("+DIRECTIONS_FK+","+ RECIPE_FK_DIRECTION +") VALUES (?, ?)";
        try (Connection conn = DBHelper.connectDB();
               PreparedStatement stmt = conn.prepareStatement(directionSql, PreparedStatement.RETURN_GENERATED_KEYS);) {
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
    
    protected static void removeRecipe(Recipe recipe) {
        deleteIngredientsFromRecipe(recipe.getRecipeID());
        deleteDirectionsFromRecipe(recipe.getRecipeID());
        System.out.println("DELETE FROM "+ RECIPE_TABLE +" WHERE "+ RECIPE_ID +" = " + recipe.getRecipeID());
        try (Connection conn = DBHelper.connectDB(); PreparedStatement statement = conn.prepareStatement("DELETE FROM "+ RECIPE_TABLE +" WHERE "+ RECIPE_ID +" = ?")) {
            statement.setInt(1, recipe.getRecipeID());
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage()+" in removeRecipe");
        }
    }

    protected static void deleteIngredientsFromRecipe(int recipe) {
        try ( Connection conn = DBHelper.connectDB();
            PreparedStatement statement = conn.prepareStatement("DELETE FROM "+ RECIPE_INGREDIENT_TABLE +" WHERE "+ RECIPE_FK_INGRED + "= ?")) {
            statement.setInt(1, recipe);
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage()+" in deleteIngredientsFromRecipe");
        }
    }

    protected static void deleteDirectionsFromRecipe(int recipe) {
        try ( Connection conn = DBHelper.connectDB();
            PreparedStatement statement = conn.prepareStatement("DELETE FROM "+ DIRECTIONS_RECIPE_TABLE +" WHERE "+ RECIPE_FK_DIRECTION + "= ?")) {
            statement.setInt(1, recipe);
            statement.executeUpdate();
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
