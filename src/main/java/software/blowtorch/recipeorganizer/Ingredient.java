package software.blowtorch.recipeorganizer;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static software.blowtorch.recipeorganizer.DBHelper.*;

public class Ingredient {

    private int ingredientID = 0;
    private String ingredient;
    private float amount;
    private String measure;


    protected Ingredient(String ingredient) {

        this.ingredient = ingredient;
        this.ingredientID = DBHelper.doesXExist(INGREDIENT_TABLE, INGREDIENT_ID, INGREDIENT_NAME, this.ingredient);
        if (this.ingredientID == 0) {
            this.ingredientID = DBHelper.writeOneValueToDB(INGREDIENT_TABLE, INGREDIENT_NAME, this.ingredient);
        }
    }

    // Getters
    protected int getIngredientID() { return this.ingredientID; }
    protected String getIngredient() { return this.ingredient; }
    protected float getAmount() { return this.amount; }
    protected String getMeasure() { return this.measure; }

    // Setters
    protected void setIngredientID(int id) { this.ingredientID = id; }
    protected void setIngredient(String i) { this.ingredient = i; }
    protected void setAmount(float amount) { this.amount = amount; }
    protected void setMeasure(String m) {
        newMeasurement(m);
        this.measure = m;
    }


    protected static ArrayList<Ingredient> getIngredientsByRecipeID(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<Ingredient> ing = new ArrayList<>();
        try {
            conn = DBHelper.connectDB();
            String sql = "SELECT " + AMOUNT + "," + MEASUREMENT_NAME + "," + INGREDIENT_NAME + " FROM " + INGREDIENT_TABLE + "," + MEASUREMENT_TABLE + "," + RECIPE_INGREDIENT_TABLE +
                    " WHERE " + INGREDIENT_FK + "=" + INGREDIENT_ID + " AND " + MEASUREMENT_ID + "=" + MEASUREMENT_FK + " AND " + RECIPE_FK_INGRED + "=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next() != false) {
                do {
                    Ingredient i = new Ingredient(rs.getString(INGREDIENT_NAME));
                    i.setAmount(rs.getFloat(AMOUNT));
                    i.setMeasure(rs.getString(MEASUREMENT_NAME));
                    ing.add(i);
                } while (rs.next());
                return ing;
            }
            return ing;
        } catch (SQLException ex) {
            System.err.println("Exception :" + ex.getMessage() + " in getIngredientsByRecipeID");
            return ing;
        } finally {
            try {
                if (rs != null)rs.close();
                if (stmt != null)stmt.close();
                if (conn != null)conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception :" + ex.getMessage() + " in getIngredientsByRecipeID");
            }
        }
    }

    public static int newMeasurement(String measure) {

        int measurementID = DBHelper.doesXExist(MEASUREMENT_TABLE, MEASUREMENT_ID, MEASUREMENT_NAME, measure);

        if (measurementID == 0) {
            measurementID = DBHelper.writeOneValueToDB(MEASUREMENT_TABLE, MEASUREMENT_NAME, measure);
        }
        return measurementID;
    }

    public static ArrayList<String> getMeasurements() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String> y = new ArrayList<>();
        try {
            conn = DBHelper.connectDB();
            stmt = conn.prepareStatement("SELECT " + MEASUREMENT_NAME + " FROM " + MEASUREMENT_TABLE);
            rs = stmt.executeQuery();
            if (rs.next() != false) {
                do {
                    y.add(rs.getString(MEASUREMENT_NAME));
                } while (rs.next());
                return y;
            }
            return y;
        } catch (SQLException ex) {
            System.err.println("Exception :" + ex.getMessage() + " in getIngredientsByRecipeID");
            return y;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception :" + ex.getMessage() + " in getIngredientsByRecipeID");
            }
        }
    }
}
