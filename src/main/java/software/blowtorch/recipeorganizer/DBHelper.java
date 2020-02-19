package software.blowtorch.recipeorganizer;

import java.sql.*;

public class DBHelper {

    // Database Tables
    public static final String RECIPE_TABLE = "recipes";
    public static final String CATEGORY_TABLE = "category";
    public static final String INGREDIENT_TABLE = "ingredients";
    public static final String RECIPE_INGREDIENT_TABLE = "ingredientRecipe";
    public static final String MEASUREMENT_TABLE = "measures";
    public static final String DIRECTIONS_TABLE = "directions";
    public static final String DIRECTIONS_RECIPE_TABLE = "directionsRecipe";

    // Recipe Table Columns
    public static final String RECIPE_ID = "recipeID";
    public static final String RECIPE_NAME = "rName";
    public static final String RECIPE_DESCRIPTION = "rDescription";
    public static final String RECIPE_CATEGORY = "rCategory";

    // Category Table Columns
    public static final String CATEGORY_ID = "categoryID";
    public static final String CATEGORY_NAME = "categoryName";

    // Ingredients Table Columns
    public static final String INGREDIENT_ID = "ingredientID";
    public static final String INGREDIENT_NAME = "ingredientName";

    //Ingredients Recipe Relations Table Columns
    public static final String RECIPE_INGREDIENT_ID = "recipeIngredID";
    public static final String INGREDIENT_FK = "ingredientFK";
    public static final String RECIPE_FK_INGRED = "recipeFK";
    public static final String AMOUNT = "ingredientAmount";
    public static final String MEASUREMENT_FK = "measurementFK";

    //Measurements table
    public static final String MEASUREMENT_ID = "measureID";
    public static final String MEASUREMENT_NAME = "measurement";

    //Directions table
    public static final String DIRECTION_ID = "directionID";
    public static final String DIRECTION_NAME = "direction";

    //Directions Recipe Relation table
    public static final String DIRECTIONS_RECIPE_ID = "directionsRecipeID";
    public static final String DIRECTIONS_FK = "directionsFK";
    public static final String RECIPE_FK_DIRECTION = "recipeFK";



    public static Connection connectDB() {
        String database = "jdbc:derby:".concat(System.getProperty("user.home")).concat("/.recipeOrganize/").concat("vinny;create=true");
        try {
            Connection conn = DriverManager.getConnection(database);
            if (conn != null) {
                return conn;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    public static void createDatabaseTables() {
        Connection conn = DBHelper.connectDB();
        Statement statement = null;
        String sql;
        try {
            statement = conn.createStatement();
            // Category Table
            if (!doesTableExist(CATEGORY_TABLE, conn)) {
                sql = "CREATE TABLE " + CATEGORY_TABLE + " (" + CATEGORY_ID + " INTEGER NOT NULL generated always as identity (start with 1, increment by 1), " +
                        CATEGORY_NAME + " varchar(128))";
                statement.execute(sql);
                System.out.println("Created table " + CATEGORY_TABLE);
            } else {
                System.out.println(CATEGORY_TABLE + " Table exists");
            }
            //Recipe Table
            if (!doesTableExist(RECIPE_TABLE, conn)) {
                sql = "CREATE TABLE " + RECIPE_TABLE + " (" + RECIPE_ID + " INTEGER NOT NULL generated always as identity (start with 1, increment by 1), " +
                        RECIPE_NAME + " VARCHAR(128)," +
                        RECIPE_DESCRIPTION + " VARCHAR(255), " +
                        RECIPE_CATEGORY + " INTEGER)";
                statement.execute(sql);
                System.out.println("Created table " + RECIPE_TABLE);
            } else {
                System.out.println(RECIPE_TABLE + " Table exists");
            }
            // Ingredients Table
            if (!doesTableExist(INGREDIENT_TABLE, conn)) {
                sql = "CREATE TABLE " + INGREDIENT_TABLE + " (" + INGREDIENT_ID + " INTEGER NOT NULL generated always as identity (start with 1, increment by 1), " +
                        INGREDIENT_NAME + " VARCHAR(128))";
                statement.execute(sql);
                System.out.println("Created table " + INGREDIENT_TABLE);
            } else {
                System.out.println(INGREDIENT_TABLE + " Table exists");
            }
            //Recipe Ingredients Relation Table
            if (!doesTableExist(RECIPE_INGREDIENT_TABLE, conn)) {
                sql = "CREATE TABLE " + RECIPE_INGREDIENT_TABLE + " (" + RECIPE_INGREDIENT_ID + " INTEGER NOT NULL generated always as identity (start with 1, increment by 1), " +
                        INGREDIENT_FK + " INTEGER," +
                        RECIPE_FK_INGRED + " INTEGER,"+
                        AMOUNT + " DECIMAL(8,2),"+
                        MEASUREMENT_FK+ " INTEGER)";
                statement.execute(sql);
                System.out.println("Created table " + RECIPE_INGREDIENT_TABLE);
            } else {
                System.out.println(RECIPE_INGREDIENT_TABLE + " Table exists");
            }
            //Measurement Table
            if (!doesTableExist(MEASUREMENT_TABLE, conn)) {
                sql = "CREATE TABLE " + MEASUREMENT_TABLE + " (" + MEASUREMENT_ID + " INTEGER NOT NULL generated always as identity (start with 1, increment by 1), " +
                        MEASUREMENT_NAME + " VARCHAR(64))";
                statement.execute(sql);
                System.out.println("Created table " + MEASUREMENT_TABLE);
            } else {
                System.out.println(MEASUREMENT_TABLE + " Table exists");
            }
            //Directions Table
            if (!doesTableExist(DIRECTIONS_TABLE, conn)) {
                sql = "CREATE TABLE " + DIRECTIONS_TABLE + " (" + DIRECTION_ID + " INTEGER NOT NULL generated always as identity (start with 1, increment by 1), " +
                        DIRECTION_NAME + " VARCHAR(255))";
                statement.execute(sql);
                System.out.println("Created table " + DIRECTIONS_TABLE);
            } else {
                System.out.println(DIRECTIONS_TABLE + " Table exists");
            }
            //Directions Recipe Relations Table
            if (!doesTableExist(DIRECTIONS_RECIPE_TABLE, conn)) {
                sql = "CREATE TABLE " + DIRECTIONS_RECIPE_TABLE + " (" + DIRECTIONS_RECIPE_ID + " INTEGER NOT NULL generated always as identity (start with 1, increment by 1), " +
                        DIRECTIONS_FK + " INTEGER,"+
                        RECIPE_FK_DIRECTION + " INTEGER)";
                statement.execute(sql);
                System.out.println("Created table " + DIRECTIONS_RECIPE_TABLE);
            } else {
                System.out.println(DIRECTIONS_RECIPE_TABLE + " Table exists");
            }
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage()+" in createDatabaseTables");
        }
        try {
            if (statement != null)  statement.close();
            conn.close();
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage()+" in createDatabaseTables");
        }
    }

    private static boolean doesTableExist(String tableName, Connection conn)
            throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet result = meta.getTables(null, null, tableName.toUpperCase(), null);

        return result.next();
    }

    public static int doesXExist(String table, String column, String where, String condition) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.connectDB();
            stmt = conn.prepareStatement("SELECT "+column+" FROM "+table+" WHERE "+where+" = '"+condition+"'");
            rs = stmt.executeQuery();
            if (rs.next() == false) {
                return 0;
            }
            return rs.getInt(column);
        } catch (SQLException ex) {
            System.err.println("Exception :"+ex.getMessage()+" in doesXExist");
            return 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception :"+ex.getMessage()+" in doesXExist");
            }
        }
    }

    public static int writeOneValueToDB(String table, String column, String value) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.connectDB();
            stmt = conn.prepareStatement("INSERT INTO "+ table +" ("+ column +") VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, value);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException ex) {
            System.err.println("Exception :"+ex.getMessage()+" in writeIngredient");
            return 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception :"+ex.getMessage()+" in writeOneValueToDB");
            }
        }
    }
}
