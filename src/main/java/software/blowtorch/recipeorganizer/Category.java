package software.blowtorch.recipeorganizer;



import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static software.blowtorch.recipeorganizer.DBHelper.*;


public class Category {

    private int    id;
    private String name;

    protected Category() {  }

    protected static List<String> getCategoryAll() {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.connectDB();
            statement = conn.createStatement();
            rs = statement.executeQuery("SELECT "+ CATEGORY_ID +", "+ CATEGORY_NAME +" FROM "+ CATEGORY_TABLE);
            try {
                List<String> category = new ArrayList<>();
                if (rs.next() == false) {
                    return category;
                } else {
                    do {
                        category.add(rs.getString(CATEGORY_NAME));
                    } while (rs.next());
                }
                return category;
            } catch (SQLException ex) {
                System.err.println("Exception: " + ex.getMessage()+" in getCategoryAll");
            }
            return null;
        } catch (SQLException ex) {
            System.err.println("Exception: "+ex.getMessage()+" in getCategoryAll");
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception: " + ex.getMessage()+" in getCategoryAll");
            }
        }
    }

    protected static boolean editCategoryName(String oldName, String newName) {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DBHelper.connectDB();
            statement = conn.prepareStatement("UPDATE "+ CATEGORY_TABLE +" SET "+ CATEGORY_NAME +"=? WHERE "+ CATEGORY_ID +" = ?");
            statement.setString(1, newName);
            statement.setInt(2, getCategoryID(oldName));
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Exception: "+ex.getMessage()+" in editCategoryName");
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception: "+ex.getMessage()+" in editCategoryName");
            }
        }
    }

    protected static String getCategoryByID(int id) {
        if (id == 0) {
            return "";
        }
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.connectDB();
            statement = conn.prepareStatement("SELECT "+ CATEGORY_NAME +" FROM "+ CATEGORY_TABLE +" WHERE "+ CATEGORY_ID +" = ?");
            statement.setInt(1, id);
            rs = statement.executeQuery();
            rs.next();
            return rs.getString(CATEGORY_NAME);
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage()+" in getCategoryByID");
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception: " + ex.getMessage()+" in getCategoryByID");
            }
        }
    }

    protected static int getCategoryID(String cbName) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.connectDB();
            statement = conn.prepareStatement("SELECT "+ CATEGORY_ID +" FROM "+ CATEGORY_TABLE +" WHERE "+ CATEGORY_NAME +" = ?");
            statement.setString(1, cbName);
            rs = statement.executeQuery();
            if (rs.next() == false) {
                return 0;
            }
            return rs.getInt(CATEGORY_ID);
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage()+" in getCategoryID");
            return 0;
        } finally {
            try {
                if (rs != null)rs.close();
                if (statement != null)statement.close();
                if (conn != null)conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception: " + ex.getMessage()+" in getCategoryID");
            }
        }
    }

    protected static int deleteCategory(String cb) {
        try {
            Connection conn = DBHelper.connectDB();
            // Orphan any recipes from this Category
            PreparedStatement statement1 = conn.prepareStatement("UPDATE "+RECIPE_TABLE+" SET "+ RECIPE_CATEGORY +"= 0 WHERE "+ RECIPE_CATEGORY +" = ?");
            statement1.setInt(1, Category.getCategoryID(cb));
            int rows = statement1.executeUpdate();
            PreparedStatement statement = conn.prepareStatement("DELETE FROM "+ CATEGORY_TABLE +" WHERE "+ CATEGORY_NAME +" = ?");
            statement.setString(1, cb);
            statement.executeUpdate();
            statement.close();
            statement1.close();
            conn.close();
            return rows;
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage()+" in deleteCategory");
            return 0;
        }
    }

    protected static int addCategory(String value) {
        Connection conn = null;
        PreparedStatement statement = null;
        String sql = "INSERT INTO "+ CATEGORY_TABLE +" ("+ CATEGORY_NAME +") VALUES (?)";
        try {
            conn = DBHelper.connectDB();
            statement = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, value);
            return statement.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Exception: "+ex.getMessage()+" in addCategory");
            return 0;
        } finally {
            try {
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception: "+ex.getMessage()+" in addCategory");
            }
        }
    }
}
